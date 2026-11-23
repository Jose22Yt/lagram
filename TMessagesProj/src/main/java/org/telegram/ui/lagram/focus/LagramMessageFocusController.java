package org.telegram.ui.lagram.focus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.Cells.ChatActionCell;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.ChatScrimPopupContainerLayout;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.PopupSwipeBackLayout;
import org.telegram.ui.Components.ReactionsContainerLayout;

import java.util.Arrays;

public final class LagramMessageFocusController {

    public interface ChromeChildDrawer {
        void drawChromeChild(Canvas canvas, View child);
    }

    public static final class FocusState {
        public final RectF bubbleBounds = new RectF();
        public int containerWidth;
        public int containerHeight;
        public float targetShiftY;
        public boolean isIncoming;
        public boolean isActionCell;
        public boolean isTallScrollable;
        public float topFadeHeight;
        public float maxScrollOffset;
        public float menuTopY;
        public float menuMaxBottomY;
        public float reactionsBottomY;
        public float reactionsGap;
        public float minReactionsBottomY;
        public float menuGap;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static final class HardwareBlurNode {
        private static final ColorMatrixColorFilter SATURATION_FILTER;
        static {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(1.8f);
            SATURATION_FILTER = new ColorMatrixColorFilter(cm);
        }

        private final RenderNode sourceNode = new RenderNode("LagramFocusSource");
        private final RenderNode blurNode = new RenderNode("LagramFocusBlur");
        boolean isRecording;
        private float lastRadius = -1f;

        Canvas beginRecording(int width, int height) {
            sourceNode.setPosition(0, 0, width, height);
            isRecording = true;
            return sourceNode.beginRecording(width, height);
        }

        void drawBlurred(Canvas canvas, int width, int height, float alpha, float radiusPx) {
            sourceNode.endRecording();
            isRecording = false;
            if (alpha < 1.0f) {
                canvas.drawRenderNode(sourceNode);
            }
            if (alpha <= 0.0f) {
                return;
            }
            float downscaledRadius = Math.round((radiusPx / 4.0f) * 4.0f) / 4.0f;
            if (downscaledRadius < 0.5f) {
                downscaledRadius = 0.5f;
            }
            if (lastRadius != downscaledRadius) {
                lastRadius = downscaledRadius;
                blurNode.setRenderEffect(RenderEffect.createColorFilterEffect(
                        SATURATION_FILTER,
                        RenderEffect.createBlurEffect(downscaledRadius, downscaledRadius, Shader.TileMode.CLAMP)
                ));
            }
            int scaledW = (width + 3) / 4;
            int scaledH = (height + 3) / 4;
            blurNode.setPosition(0, 0, scaledW, scaledH);
            RecordingCanvas rec = blurNode.beginRecording(scaledW, scaledH);
            rec.scale(0.25f, 0.25f);
            rec.drawRenderNode(sourceNode);
            blurNode.endRecording();
            blurNode.setAlpha(alpha);
            canvas.save();
            canvas.scale(4.0f, 4.0f);
            canvas.drawRenderNode(blurNode);
            canvas.restore();
        }

        void release() {
            if (isRecording) {
                sourceNode.endRecording();
                isRecording = false;
            }
            sourceNode.discardDisplayList();
            blurNode.discardDisplayList();
        }
    }

    private static final class BlurRenderer {
        int mode = 0; // 0 = dim only, 1 = RenderNode (API 31+), 2 = Bitmap stackBlur
        int dimColor = 0x33000016;
        final Paint dimPaint = new Paint();
        Paint bitmapPaint;
        Object hardwareNode;
        Bitmap blurBitmap;

        void captureFallbackBitmap(ViewGroup container) {
            if (mode != 2) {
                return;
            }
            int w = container.getWidth();
            int h = container.getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            try {
                int bw = Math.max(1, w / 8);
                int bh = Math.max(1, h / 8);
                Bitmap bmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(bmp);
                c.scale((float) bw / w, (float) bh / h);
                container.draw(c);
                Utilities.stackBlurBitmap(bmp, Math.max(4, Math.round((AndroidUtilities.dpf2(24.0f) * 1.2f) / 8.0f)));
                blurBitmap = bmp;
                if (bitmapPaint == null) {
                    ColorMatrix cm = new ColorMatrix();
                    cm.setSaturation(1.8f);
                    bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
                    bitmapPaint.setColorFilter(new ColorMatrixColorFilter(cm));
                }
            } catch (Throwable t) {
                FileLog.e(t);
                blurBitmap = null;
            }
        }

        void release() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hardwareNode != null) {
                ((HardwareBlurNode) hardwareNode).release();
            }
            if (blurBitmap != null) {
                blurBitmap.recycle();
                blurBitmap = null;
            }
        }
    }

    final BlurRenderer blurRenderer = new BlurRenderer();
    public final FocusState state = new FocusState();
    public LagramMessageFocusLayout focusLayout;

    ViewGroup containerView;
    View listView;
    View targetCell;
    View[] chromeViews;

    public boolean isActive;
    public boolean isDismissing;
    public boolean hasFocusLayout;
    public boolean isCapturingFallback;

    public float blurProgress;
    public float clipProgress;
    public float currentShiftY;
    public float scrollOffsetY;
    public float dismissDeltaY;

    private ValueAnimator progressAnimator;
    SpringAnimation shiftSpring;

    private boolean applyTopFade;
    private Paint topFadePaint;
    private float lastTopFadeHeight;
    private float chromeOverlayAlpha;
    private Paint chromeOverlayPaint;

    private final RectF tempRectF = new RectF();
    private final Rect tempRect = new Rect();
    private float listInitialTop;
    private float listInitialBottom;
    private final int[] locationTmp = new int[2];

    int savedStatusBarHeight;
    int savedNavBarInset;
    int savedMaxBottomInset;
    int lastMenuHeight;

    private static boolean overlapsVertically(View view, float top, float bottom) {
        if (view != null && view.getVisibility() == View.VISIBLE && view.getAlpha() > 0.0f) {
            float y = view.getY();
            return y < bottom && y + view.getHeight() > top;
        }
        return false;
    }

    private static void unionCellBounds(RectF rectF, ChatMessageCell cell, float offsetX, float offsetY, boolean union) {
        float left = cell.getX() + offsetX + cell.getBackgroundDrawableLeft();
        float right = cell.getX() + offsetX + cell.getBackgroundDrawableRight();
        float top = cell.getY() + offsetY;
        float bottom = top + cell.getHeight();
        if (union) {
            rectF.union(left, top, right, bottom);
        } else {
            rectF.set(left, top, right, bottom);
        }
    }

    public ViewGroup wrapPopupContainer(
            View targetView,
            ChatScrimPopupContainerLayout scrimPopupContainerLayout,
            ReactionsContainerLayout reactionsLayout,
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout,
            ActionBarMenuSubItem[] items,
            Rect backgroundPaddings,
            boolean canFocus,
            Runnable closeMenuCallback
    ) {
        reset();
        focusLayout = null;
        LagramConfig.ensureLoaded();
        if (canFocus && LagramConfig.messageFocusEnabled) {
            if ((targetView instanceof ChatMessageCell || targetView instanceof ChatActionCell)
                    && scrimPopupContainerLayout.getParent() == null
                    && popupLayout.getParent() == scrimPopupContainerLayout) {
                ReactionsContainerLayout validReactions = (reactionsLayout != null && reactionsLayout.getParent() == scrimPopupContainerLayout)
                        ? reactionsLayout : null;
                focusLayout = new LagramMessageFocusLayout(
                        scrimPopupContainerLayout.getContext(),
                        this,
                        scrimPopupContainerLayout,
                        validReactions,
                        popupLayout,
                        items,
                        backgroundPaddings,
                        closeMenuCallback
                );
                return focusLayout;
            }
        }
        return scrimPopupContainerLayout;
    }

    public boolean hasFocusWrapper() {
        return focusLayout != null;
    }

    public void configurePopupWindow(ActionBarPopupWindow popupWindow) {
        if (focusLayout == null || popupWindow == null) {
            return;
        }
        popupWindow.setAnimationStyle(0);
        popupWindow.setClippingEnabled(false);
        if (Build.VERSION.SDK_INT >= 29) {
            popupWindow.setIsLaidOutInScreen(true);
        } else {
            popupWindow.setLayoutInScreen(true);
        }
    }

    public void start(
            ViewGroup container,
            View chatList,
            View focusedCell,
            boolean isDarkTheme,
            int statusBarHeight,
            int navBarInset,
            int maxBottomInset,
            View... chromeChildren
    ) {
        if (isActive) {
            reset();
        }
        containerView = container;
        listView = chatList;
        LagramConfig.ensureLoaded();
        boolean blurEnabled = LagramConfig.messageFocusEnabled && LagramConfig.messageFocusBlur;
        hasFocusLayout = focusLayout != null;
        if (container == null || chatList == null || !(blurEnabled || hasFocusLayout)) {
            focusLayout = null;
            return;
        }
        targetCell = focusedCell;
        if (chromeChildren != null && chromeChildren.length > 0) {
            View[] sorted = new View[chromeChildren.length];
            int count = 0;
            for (View v : chromeChildren) {
                if (v != null && v.getParent() == container) {
                    int idx = container.indexOfChild(v);
                    int next = count + 1;
                    while (count > 0 && container.indexOfChild(sorted[count - 1]) > idx) {
                        sorted[count] = sorted[count - 1];
                        count--;
                    }
                    sorted[count] = v;
                    count = next;
                }
            }
            chromeViews = count == 0 ? null : (count == sorted.length ? sorted : Arrays.copyOf(sorted, count));
        } else {
            chromeViews = null;
        }

        state.containerWidth = container.getWidth();
        state.containerHeight = container.getHeight();
        float listY = chatList.getY();
        if (chatList.getClipBounds(tempRect)) {
            listInitialTop = listY + tempRect.top;
            listInitialBottom = listY + tempRect.bottom;
        } else {
            listInitialTop = listY;
            listInitialBottom = listY + chatList.getHeight();
        }
        listInitialTop = Math.max(0.0f, listInitialTop);
        listInitialBottom = Math.min(state.containerHeight, listInitialBottom);

        if (hasFocusLayout && focusedCell != null) {
            RectF bounds = state.bubbleBounds;
            float offsetX = chatList.getX();
            float offsetY = chatList.getY();
            bounds.setEmpty();
            if (focusedCell instanceof ChatActionCell) {
                ChatActionCell actionCell = (ChatActionCell) focusedCell;
                float cellX = focusedCell.getX() + offsetX;
                float cellY = focusedCell.getY() + offsetY;
                float centerX = cellX + focusedCell.getWidth() / 2.0f;
                float halfW = Math.max(1.0f, (cellX + actionCell.getBoundsRight()) - centerX);
                bounds.set(centerX - halfW, cellY, centerX + halfW, cellY + focusedCell.getHeight());
            } else if (focusedCell instanceof ChatMessageCell) {
                ChatMessageCell msgCell = (ChatMessageCell) focusedCell;
                MessageObject.GroupedMessages group = msgCell.getCurrentMessagesGroup();
                boolean foundGroup = false;
                if (group != null && chatList instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) chatList;
                    int childCount = vg.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = vg.getChildAt(i);
                        if (child instanceof ChatMessageCell) {
                            ChatMessageCell c = (ChatMessageCell) child;
                            if (c.getCurrentMessagesGroup() == group) {
                                unionCellBounds(bounds, c, offsetX, offsetY, foundGroup);
                                foundGroup = true;
                            }
                        }
                    }
                }
                if (!foundGroup) {
                    unionCellBounds(bounds, msgCell, offsetX, offsetY, false);
                }
            }
            if (bounds.width() <= 0.0f) {
                float cellX = focusedCell.getX() + offsetX;
                bounds.left = cellX;
                bounds.right = cellX + Math.max(1, focusedCell.getWidth());
            }
            if (bounds.height() <= 0.0f) {
                bounds.bottom = bounds.top + 1.0f;
            }
        }

        int blurMode = !blurEnabled ? 0 : (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE) ? 1 : 2;
        blurRenderer.release();
        blurRenderer.mode = blurMode;
        blurRenderer.dimColor = isDarkTheme ? 0x99000000 : 0x33000016;

        isActive = true;
        isDismissing = false;
        blurProgress = 0.0f;
        clipProgress = 0.0f;
        currentShiftY = 0.0f;
        scrollOffsetY = 0.0f;
        dismissDeltaY = 0.0f;

        if (hasFocusLayout) {
            savedStatusBarHeight = statusBarHeight;
            savedNavBarInset = navBarInset;
            savedMaxBottomInset = maxBottomInset;
            int totalPopupHeight = focusLayout.getExtraChildrenHeight() + focusLayout.getPopupContentHeight();
            Rect pad = focusLayout.backgroundPaddings;
            computeLayoutMetrics(statusBarHeight, navBarInset, maxBottomInset, Math.max(0, totalPopupHeight - pad.top - pad.bottom));

            PopupSwipeBackLayout swipeBack = focusLayout.popupLayout.getSwipeBack();
            if (swipeBack != null) {
                swipeBack.setStickToRight(!state.isIncoming);
            }
            ReactionsContainerLayout reactions = focusLayout.reactionsLayout;
            if (reactions != null) {
                reactions.setMirrorX(!state.isIncoming);
            }
            focusLayout.enterAnimStarted = false;
            focusLayout.alphaCompleted = false;
            focusLayout.currentScrollOffset = 0;
            focusLayout.applyEnterProgress(0.0f);
        }

        if (blurRenderer.mode == 2) {
            isCapturingFallback = true;
            try {
                blurRenderer.captureFallbackBitmap(container);
            } finally {
                isCapturingFallback = false;
            }
        }

        chatList.invalidate();
        container.invalidate();

        progressAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        progressAnimator.addUpdateListener(anim -> {
            float val = (Float) anim.getAnimatedValue();
            blurProgress = val;
            clipProgress = val;
            updateTranslations();
        });
        progressAnimator.setDuration(240L);
        progressAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        progressAnimator.start();

        if (hasFocusLayout && state.targetShiftY != 0.0f) {
            startShiftSpring(0.0f);
        }
    }

    void startShiftSpring(float startValue) {
        SpringAnimation spring = new SpringAnimation(new FloatValueHolder(startValue));
        shiftSpring = spring;
        SpringForce force = new SpringForce(state.targetShiftY);
        force.setStiffness(450.0f);
        force.setDampingRatio(0.78f);
        spring.setSpring(force);
        spring.setMinimumVisibleChange(0.5f);
        spring.addUpdateListener((animation, value, velocity) -> {
            currentShiftY = value;
            updateTranslations();
        });
        spring.addEndListener((animation, canceled, value, velocity) -> {
            if (!canceled) {
                currentShiftY = state.targetShiftY;
                updateTranslations();
            }
        });
        spring.start();
    }

    void computeLayoutMetrics(int statusBarHeight, int navBarInset, int maxBottomInset, int menuHeight) {
        RectF bounds = state.bubbleBounds;
        lastMenuHeight = menuHeight;
        state.containerWidth = containerView.getWidth();
        state.containerHeight = containerView.getHeight();
        containerView.getLocationOnScreen(locationTmp);

        float topInset = Math.max(0, statusBarHeight - locationTmp[1]);
        float safeTop = topInset + AndroidUtilities.dp(8.0f);
        float safeBottomNav = (state.containerHeight - navBarInset) - AndroidUtilities.dp(10.0f);
        float safeBottomKeyboard = Math.min(safeBottomNav, (state.containerHeight - maxBottomInset) - AndroidUtilities.dp(8.0f));

        ReactionsContainerLayout reactions = focusLayout != null ? focusLayout.reactionsLayout : null;
        float reactionsH = (reactions == null || reactions.getVisibility() == View.GONE)
                ? 0.0f
                : Math.max(0, reactions.getMeasuredHeight() - reactions.getPaddingTop() - reactions.getPaddingBottom());
        float gap8 = AndroidUtilities.dp(8.0f);
        float minBubbleTop = safeTop + (reactionsH > 0.0f ? reactionsH + gap8 : 0.0f);
        state.reactionsGap = gap8;
        state.minReactionsBottomY = minBubbleTop - gap8;

        float bubbleH = bounds.height();
        float maxBubbleTop = Math.min(safeBottomKeyboard, (safeBottomNav - menuHeight) - gap8) - bubbleH;
        float targetTop;
        if (maxBubbleTop >= minBubbleTop) {
            state.isTallScrollable = false;
            targetTop = Utilities.clamp(bounds.top, maxBubbleTop, minBubbleTop);
            state.menuTopY = targetTop + bubbleH + gap8;
            state.menuMaxBottomY = safeBottomNav;
            state.reactionsBottomY = targetTop - gap8;
            state.maxScrollOffset = 0.0f;
        } else {
            state.isTallScrollable = true;
            float reservedMenuH = Math.min((float) menuHeight, Math.max(AndroidUtilities.dp(160.0f), (safeBottomKeyboard - minBubbleTop) * 0.75f));
            float menuTop = safeBottomKeyboard - reservedMenuH;
            targetTop = (menuTop - gap8) - bubbleH;
            state.menuTopY = menuTop;
            state.menuMaxBottomY = safeBottomKeyboard;
            state.reactionsBottomY = Math.max(targetTop, minBubbleTop) - gap8;
            state.maxScrollOffset = Math.max(0.0f, minBubbleTop - targetTop);
        }

        state.topFadeHeight = topInset + AndroidUtilities.dp(12.0f);
        state.targetShiftY = targetTop - bounds.top;
        state.menuGap = gap8;

        MessageObject msg = targetCell instanceof ChatMessageCell ? ((ChatMessageCell) targetCell).getMessageObject() : null;
        boolean isAction = targetCell instanceof ChatActionCell;
        state.isActionCell = isAction;
        boolean incoming = true;
        if (!isAction && (msg != null ? msg.isOutOwner() : bounds.centerX() >= state.containerWidth / 2.0f)) {
            incoming = false;
        }
        state.isIncoming = incoming;
    }

    void updateTranslations() {
        if (containerView != null) {
            containerView.invalidate();
        }
        if (focusLayout != null) {
            float containerTransY = isDismissing ? (currentShiftY + dismissDeltaY) : (currentShiftY - state.targetShiftY);
            float reactionsTransY = isDismissing
                    ? 0.0f
                    : (Math.max((state.bubbleBounds.top + currentShiftY) - state.reactionsGap, state.minReactionsBottomY) - state.reactionsBottomY) - containerTransY;
            focusLayout.scrimPopupContainerLayout.setTranslationY(containerTransY);
            if (focusLayout.reactionsLayout != null && focusLayout.reactionsLayout.getTranslationY() != reactionsTransY) {
                focusLayout.reactionsLayout.setTranslationY(reactionsTransY);
            }
        }
    }

    public boolean canScrollTallMessage() {
        return isActive && !isDismissing && hasFocusLayout && state.maxScrollOffset > 0.0f;
    }

    public void updateScrollOffset(float newOffset) {
        if (canScrollTallMessage()) {
            float clamped = Utilities.clamp(newOffset, state.maxScrollOffset, 0.0f);
            if (clamped != scrollOffsetY) {
                scrollOffsetY = clamped;
                if (containerView != null) {
                    containerView.invalidate();
                }
                if (focusLayout != null) {
                    focusLayout.onScrollOffsetChanged(scrollOffsetY);
                }
            }
        }
    }

    public Canvas beginBackgroundBlur(Canvas canvas, int width, int height) {
        if (isActive && !isCapturingFallback) {
            if (blurRenderer.mode == 1 && width > 0 && height > 0 && canvas.isHardwareAccelerated() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (blurRenderer.hardwareNode == null) {
                    blurRenderer.hardwareNode = new HardwareBlurNode();
                }
                Canvas rec = ((HardwareBlurNode) blurRenderer.hardwareNode).beginRecording(width, height);
                if (rec != null) {
                    return rec;
                }
            }
        }
        return canvas;
    }

    public void drawBackgroundOverlay(Canvas canvas, int width, int height) {
        if (!isActive || isCapturingFallback) {
            return;
        }
        float progress = blurProgress;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRenderer.hardwareNode != null && ((HardwareBlurNode) blurRenderer.hardwareNode).isRecording) {
            ((HardwareBlurNode) blurRenderer.hardwareNode).drawBlurred(canvas, width, height, progress, AndroidUtilities.dpf2(24.0f));
        } else if (blurRenderer.mode == 2 && blurRenderer.blurBitmap != null && progress > 0.0f) {
            blurRenderer.bitmapPaint.setAlpha((int) (255.0f * progress));
            canvas.save();
            canvas.scale((float) width / blurRenderer.blurBitmap.getWidth(), (float) height / blurRenderer.blurBitmap.getHeight());
            canvas.drawBitmap(blurRenderer.blurBitmap, 0.0f, 0.0f, blurRenderer.bitmapPaint);
            canvas.restore();
        }
        int dimColor = blurRenderer.dimColor;
        int alpha = (int) (((dimColor >>> 24) & 0xFF) * progress);
        if (alpha > 0) {
            blurRenderer.dimPaint.setColor((dimColor & 0x00FFFFFF) | (alpha << 24));
            canvas.drawRect(0.0f, 0.0f, width, height, blurRenderer.dimPaint);
        }
    }

    public int saveFocusedBubbleLayer(Canvas canvas, float scrimViewAlpha, int width, int height) {
        if (!isActive) {
            return -1;
        }
        applyTopFade = hasFocusLayout && state.isTallScrollable;
        chromeOverlayAlpha = chromeViews != null ? 1.0f - blurProgress : 0.0f;
        int saveCount;
        if (scrimViewAlpha < 0.995f || applyTopFade || chromeOverlayAlpha > 0.01f) {
            saveCount = canvas.saveLayerAlpha(0.0f, 0.0f, width, height, (int) (Utilities.clamp(scrimViewAlpha, 1.0f, 0.0f) * 255.0f), Canvas.ALL_SAVE_FLAG);
        } else {
            saveCount = canvas.save();
        }
        canvas.save();
        float shift = currentShiftY + scrollOffsetY;
        if (shift != 0.0f) {
            canvas.translate(0.0f, shift);
        }
        return saveCount;
    }

    public float adjustListTop(float listTop) {
        if (!isActive) {
            return listTop;
        }
        return AndroidUtilities.lerp(listInitialTop, 0.0f, clipProgress) - (currentShiftY + scrollOffsetY);
    }

    public float adjustClipBottom(float clipBottom) {
        if (!isActive) {
            return clipBottom;
        }
        return AndroidUtilities.lerp(listInitialBottom, state.containerHeight, clipProgress) - (currentShiftY + scrollOffsetY);
    }

    public void restoreFocusedBubbleLayer(Canvas canvas, int saveCount, ChromeChildDrawer chromeDrawer) {
        if (saveCount < 0) {
            return;
        }
        canvas.restoreToCount(saveCount + 1);
        if (applyTopFade && state.topFadeHeight > 0.0f && blurProgress > 0.0f) {
            if (topFadePaint == null) {
                topFadePaint = new Paint();
                topFadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            }
            if (lastTopFadeHeight != state.topFadeHeight) {
                lastTopFadeHeight = state.topFadeHeight;
                topFadePaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, lastTopFadeHeight, 0xFF000000, 0x00000000, Shader.TileMode.CLAMP));
            }
            topFadePaint.setAlpha((int) (blurProgress * 255.0f));
            canvas.drawRect(0.0f, 0.0f, state.containerWidth, state.topFadeHeight, topFadePaint);
        }
        if (chromeOverlayAlpha > 0.0f && chromeDrawer != null && chromeViews != null && listView != null && targetCell != null) {
            float listY = listView.getY();
            tempRectF.set(0.0f, targetCell.getY() + listY, state.containerWidth, targetCell.getY() + listY + targetCell.getHeight());
            MessageObject.GroupedMessages group = targetCell instanceof ChatMessageCell ? ((ChatMessageCell) targetCell).getCurrentMessagesGroup() : null;
            if (group != null && listView instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) listView;
                int childCount = vg.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = vg.getChildAt(i);
                    if (child instanceof ChatMessageCell && ((ChatMessageCell) child).getCurrentMessagesGroup() == group) {
                        float cy = child.getY() + listY;
                        tempRectF.top = Math.min(tempRectF.top, cy);
                        tempRectF.bottom = Math.max(tempRectF.bottom, cy + child.getHeight());
                    }
                }
            }
            float shift = currentShiftY + scrollOffsetY;
            float top = Math.max(0.0f, tempRectF.top + shift);
            float bottom = Math.min(state.containerHeight, tempRectF.bottom + shift);
            if (bottom > top) {
                for (View cv : chromeViews) {
                    if (overlapsVertically(cv, top, bottom)) {
                        if (chromeOverlayPaint == null) {
                            chromeOverlayPaint = new Paint();
                            chromeOverlayPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                        }
                        chromeOverlayPaint.setAlpha((int) (chromeOverlayAlpha * 255.0f));
                        int layer = canvas.saveLayer(0.0f, top, state.containerWidth, bottom, chromeOverlayPaint);
                        for (View v2 : chromeViews) {
                            if (overlapsVertically(v2, top, bottom)) {
                                chromeDrawer.drawChromeChild(canvas, v2);
                            }
                        }
                        canvas.restoreToCount(layer);
                        break;
                    }
                }
            }
        }
        canvas.restoreToCount(saveCount);
    }

    public void dismiss() {
        if (!isActive || isDismissing) {
            return;
        }
        float currentContainerTransY = currentShiftY - state.targetShiftY;
        float totalShift = currentShiftY + scrollOffsetY;
        isDismissing = true;
        if (shiftSpring != null) {
            shiftSpring.cancel();
            shiftSpring = null;
        }
        if (focusLayout != null) {
            focusLayout.stopScroll();
        }
        if (progressAnimator != null) {
            progressAnimator.removeAllListeners();
            progressAnimator.cancel();
        }
        dismissDeltaY = currentContainerTransY - totalShift;
        scrollOffsetY = 0.0f;
        currentShiftY = totalShift;
        final float startBlur = blurProgress;
        final float startClip = clipProgress;
        final float startShift = totalShift;

        progressAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        progressAnimator.addUpdateListener(anim -> {
            float rem = 1.0f - (Float) anim.getAnimatedValue();
            blurProgress = startBlur * rem;
            clipProgress = startClip * rem;
            currentShiftY = startShift * rem;
            updateTranslations();
        });
        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressAnimator = null;
                blurProgress = 0.0f;
                clipProgress = 0.0f;
                currentShiftY = 0.0f;
                scrollOffsetY = 0.0f;
                dismissDeltaY = 0.0f;
                focusLayout = null;
                if (containerView != null) {
                    containerView.invalidate();
                }
            }
        });
        progressAnimator.setDuration(220L);
        progressAnimator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
        progressAnimator.start();
    }

    public void reset() {
        if (progressAnimator != null) {
            progressAnimator.removeAllListeners();
            progressAnimator.cancel();
            progressAnimator = null;
        }
        if (shiftSpring != null) {
            shiftSpring.cancel();
            shiftSpring = null;
        }
        if (focusLayout != null) {
            focusLayout.stopScroll();
        }
        boolean wasActive = isActive;
        isActive = false;
        isDismissing = false;
        isCapturingFallback = false;
        blurProgress = 0.0f;
        clipProgress = 0.0f;
        currentShiftY = 0.0f;
        scrollOffsetY = 0.0f;
        dismissDeltaY = 0.0f;
        targetCell = null;
        chromeViews = null;
        blurRenderer.release();
        if (wasActive) {
            if (listView != null) {
                listView.invalidate();
            }
            if (containerView != null) {
                containerView.invalidate();
            }
        }
    }
}
