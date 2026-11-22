package org.telegram.ui.lagram.focus;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.Components.ChatScrimPopupContainerLayout;
import org.telegram.ui.Components.PopupSwipeBackLayout;
import org.telegram.ui.Components.ReactionsContainerLayout;

public final class LagramMessageFocusLayout extends FrameLayout {

    public final LagramMessageFocusController controller;
    public final ChatScrimPopupContainerLayout scrimPopupContainerLayout;
    public final ReactionsContainerLayout reactionsLayout;
    public final ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout;
    public final Rect backgroundPaddings = new Rect();
    public final Runnable closeMenuCallback;
    public final LagramMessageMenuBar menuBar;

    private final int[] selfLocation = new int[2];
    private final int[] containerLocation = new int[2];
    private float screenOffsetX;
    private float screenOffsetY;

    private final int touchSlop;
    private final OverScroller scroller;
    private VelocityTracker velocityTracker;
    private boolean isHandlingTouch;
    private boolean isDragging;
    private boolean touchedReactions;
    private float downX;
    private float downY;
    private float lastY;
    private boolean popupScrollable;

    int currentScrollOffset;
    private SpringAnimation enterSpring;
    boolean enterAnimStarted;
    boolean alphaCompleted;
    private float pivotX;
    private float pivotY;

    private final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if (menuBar == null || !menuBar.syncState()) {
                return true;
            }
            requestLayout();
            return false;
        }
    };

    private final Runnable flingRunnable = new Runnable() {
        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                controller.updateScrollOffset(scroller.getCurrY());
                postOnAnimation(this);
            }
        }
    };

    public LagramMessageFocusLayout(
            Context context,
            LagramMessageFocusController controller,
            ChatScrimPopupContainerLayout scrimPopupContainerLayout,
            ReactionsContainerLayout reactionsLayout,
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout,
            ActionBarMenuSubItem[] items,
            Rect paddings,
            Runnable closeMenuCallback
    ) {
        super(context);
        this.controller = controller;
        this.scrimPopupContainerLayout = scrimPopupContainerLayout;
        this.reactionsLayout = reactionsLayout;
        this.popupLayout = popupLayout;
        this.closeMenuCallback = closeMenuCallback;
        this.backgroundPaddings.set(paddings);

        setClipChildren(false);
        setClipToPadding(false);
        scrimPopupContainerLayout.setClipChildren(false);
        scrimPopupContainerLayout.setClipToPadding(false);
        addView(scrimPopupContainerLayout, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (reactionsLayout != null) {
            reactionsLayout.setParentLayout(null);
        }

        PopupSwipeBackLayout swipeBack = popupLayout.getSwipeBack();
        if (swipeBack != null) {
            swipeBack.addOnSwipeBackProgressListener((layout, toProgress, progress) -> resetChildrenTranslationX());
        }

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        scroller = new OverScroller(context);

        int maxBarWidth = computeAvailablePopupWidth(AndroidUtilities.displaySize.x);
        int style = LagramConfig.messageFocusStyle;
        this.menuBar = style != LagramConfig.FOCUS_STYLE_LIST
                ? LagramMessageMenuBar.create(items, style, popupLayout.getResourcesProvider(), popupLayout, maxBarWidth, true, this::requestLayout)
                : null;
    }

    public void applyEnterProgress(float progress) {
        float scale = 0.01f + 0.99f * progress;
        float alpha = Utilities.clamp(progress * 5.0f, 1.0f, 0.0f);
        boolean wasCompleted = alphaCompleted;
        alphaCompleted = alpha >= 1.0f;
        int count = scrimPopupContainerLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = scrimPopupContainerLayout.getChildAt(i);
            if (child != reactionsLayout) {
                child.setScaleX(scale);
                child.setScaleY(scale);
                if (!wasCompleted) {
                    child.setAlpha(alpha);
                }
            }
        }
    }

    private int computeAvailablePopupWidth(int totalWidth) {
        int containerW = controller.state.containerWidth;
        int w = (containerW > 0 ? Math.min(totalWidth, containerW) : totalWidth) - AndroidUtilities.dp(12.0f) * 2;
        int result = w + backgroundPaddings.left + backgroundPaddings.right;
        if (popupLayout.getLayoutParams() instanceof MarginLayoutParams) {
            MarginLayoutParams lp = (MarginLayoutParams) popupLayout.getLayoutParams();
            totalWidth = totalWidth - lp.leftMargin - lp.rightMargin;
            result = Math.min(result, totalWidth);
        }
        return result - popupLayout.getPaddingLeft() - popupLayout.getPaddingRight();
    }

    public int getExtraChildrenHeight() {
        int count = scrimPopupContainerLayout.getChildCount();
        int total = 0;
        for (int i = 0; i < count; i++) {
            View child = scrimPopupContainerLayout.getChildAt(i);
            if (child != reactionsLayout && child != popupLayout && child.getVisibility() != View.GONE) {
                total += child.getMeasuredHeight();
                if (child.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
                    total += lp.topMargin + lp.bottomMargin;
                }
            }
        }
        return total;
    }

    public int getPopupContentHeight() {
        PopupSwipeBackLayout swipeBack = popupLayout.getSwipeBack();
        if (swipeBack == null || swipeBack.getChildCount() <= 0) {
            return popupLayout.getMeasuredHeight();
        }
        return Math.min(
                popupLayout.getMeasuredHeight(),
                popupLayout.getPaddingTop() + popupLayout.getPaddingBottom() + swipeBack.getChildAt(0).getMeasuredHeight()
        );
    }

    private boolean isPointInsideChild(View child, MotionEvent ev) {
        if (child == null || child.getVisibility() != View.VISIBLE) {
            return false;
        }
        float x = ev.getX() - scrimPopupContainerLayout.getTranslationX();
        float y = ev.getY() - scrimPopupContainerLayout.getTranslationY();
        return x >= child.getLeft() && x <= child.getRight() && y >= child.getTop() && y <= child.getBottom();
    }

    private void resetChildrenTranslationX() {
        int count = scrimPopupContainerLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = scrimPopupContainerLayout.getChildAt(i);
            if (child != reactionsLayout && child.getTranslationX() != 0.0f) {
                child.setTranslationX(0.0f);
            }
        }
    }

    public void onScrollOffsetChanged(float offset) {
        int rounded = Math.round(offset);
        int delta = rounded - currentScrollOffset;
        if (delta == 0) {
            return;
        }
        currentScrollOffset = rounded;
        int count = scrimPopupContainerLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = scrimPopupContainerLayout.getChildAt(i);
            if (child != reactionsLayout) {
                child.offsetTopAndBottom(delta);
            }
        }
        scrimPopupContainerLayout.invalidate();
    }

    public void stopScroll() {
        scroller.forceFinished(true);
        removeCallbacks(flingRunnable);
        isHandlingTouch = false;
        isDragging = false;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() != KeyEvent.KEYCODE_BACK) {
            return super.dispatchKeyEvent(event);
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0 && closeMenuCallback != null) {
            closeMenuCallback.run();
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            stopScroll();
            downX = ev.getX();
            downY = ev.getY();
            lastY = downY;
            touchedReactions = isPointInsideChild(reactionsLayout, ev);
            if (controller.canScrollTallMessage() && !touchedReactions) {
                boolean hitPopupChild = false;
                int count = scrimPopupContainerLayout.getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = scrimPopupContainerLayout.getChildAt(i);
                    if (child != reactionsLayout && isPointInsideChild(child, ev)) {
                        hitPopupChild = true;
                        break;
                    }
                }
                if (!hitPopupChild) {
                    isHandlingTouch = true;
                    isDragging = false;
                    lastY = ev.getY();
                    if (velocityTracker != null) {
                        velocityTracker.recycle();
                    }
                    velocityTracker = VelocityTracker.obtain();
                    velocityTracker.addMovement(ev);
                    return true;
                }
            }
        }
        if (!isHandlingTouch) {
            return super.dispatchTouchEvent(ev);
        }
        handleScrollTouch(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() != MotionEvent.ACTION_MOVE
                || isHandlingTouch
                || touchedReactions
                || popupScrollable
                || !controller.canScrollTallMessage()
                || Math.abs(ev.getY() - downY) <= touchSlop
                || Math.abs(ev.getY() - downY) <= Math.abs(ev.getX() - downX)) {
            return false;
        }
        isHandlingTouch = true;
        isDragging = true;
        lastY = ev.getY();
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(ev);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isHandlingTouch) {
            return super.onTouchEvent(event);
        }
        handleScrollTouch(event);
        return true;
    }

    private void handleScrollTouch(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
        }
        if (action == MotionEvent.ACTION_MOVE) {
            float y = ev.getY();
            if (!isDragging && Math.abs(y - downY) > touchSlop) {
                isDragging = true;
                lastY = y;
            }
            if (isDragging) {
                controller.updateScrollOffset(controller.scrollOffsetY + (y - lastY));
                lastY = y;
            }
            return;
        }
        if (action == MotionEvent.ACTION_UP) {
            if (isDragging) {
                if (velocityTracker != null) {
                    velocityTracker.computeCurrentVelocity(1000);
                    float yVel = velocityTracker.getYVelocity();
                    scroller.fling(
                            0,
                            Math.round(controller.scrollOffsetY),
                            0,
                            Math.round(yVel),
                            0,
                            0,
                            0,
                            (int) Math.floor(controller.state.maxScrollOffset)
                    );
                    removeCallbacks(flingRunnable);
                    postOnAnimation(flingRunnable);
                }
            } else if (closeMenuCallback != null) {
                closeMenuCallback.run();
            }
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            isHandlingTouch = false;
            isDragging = false;
            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (menuBar != null) {
            menuBar.syncState();
            getViewTreeObserver().addOnPreDrawListener(preDrawListener);
        }
        if (enterAnimStarted) {
            return;
        }
        enterAnimStarted = true;
        setAnimatedChildrenLayerType(LAYER_TYPE_HARDWARE);
        SpringAnimation spring = new SpringAnimation(new FloatValueHolder(0.0f));
        enterSpring = spring;
        SpringForce force = new SpringForce(1.0f);
        force.setStiffness(450.0f);
        force.setDampingRatio(0.78f);
        spring.setSpring(force);
        spring.setMinimumVisibleChange(0.002f);
        spring.addUpdateListener((animation, value, velocity) -> applyEnterProgress(value));
        spring.addEndListener((animation, canceled, value, velocity) -> {
            enterSpring = null;
            applyEnterProgress(1.0f);
            setAnimatedChildrenLayerType(LAYER_TYPE_NONE);
        });
        spring.start();
    }

    private void setAnimatedChildrenLayerType(int layerType) {
        int count = scrimPopupContainerLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = scrimPopupContainerLayout.getChildAt(i);
            if (child != reactionsLayout && child.getLayerType() != layerType) {
                child.setLayerType(layerType, null);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (menuBar != null) {
            getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
            menuBar.cancelExpandAnimation();
        }
        stopScroll();
        if (enterSpring != null) {
            enterSpring.cancel();
            enterSpring = null;
        }
        setAnimatedChildrenLayerType(LAYER_TYPE_NONE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (menuBar != null) {
            int availW = computeAvailablePopupWidth(width);
            menuBar.updateAvailableWidth(availW);
        }

        int childWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
        int tallHeightSpec = MeasureSpec.makeMeasureSpec(Math.max(height, AndroidUtilities.dp(3000.0f)), MeasureSpec.AT_MOST);
        scrimPopupContainerLayout.setMaxHeight(0);
        scrimPopupContainerLayout.measure(childWidthSpec, tallHeightSpec);

        if (menuBar != null && menuBar.needsRemeasureWidth()) {
            int adjW = width;
            if (popupLayout.getLayoutParams() instanceof MarginLayoutParams) {
                MarginLayoutParams lp = (MarginLayoutParams) popupLayout.getLayoutParams();
                adjW = width - lp.leftMargin - lp.rightMargin;
            }
            popupLayout.measure(MeasureSpec.makeMeasureSpec(Math.max(0, adjW), MeasureSpec.AT_MOST), tallHeightSpec);
        }

        if (menuBar != null) {
            int currentMenuH = Math.max(0, getExtraChildrenHeight() + getPopupContentHeight() - backgroundPaddings.top - backgroundPaddings.bottom)
                    - ((menuBar.isAnimatingExpand && !menuBar.isExpanded) ? menuBar.getExpandedListHeight() : 0);
            if (controller.isActive && !controller.isDismissing && controller.hasFocusLayout && controller.containerView != null && currentMenuH != controller.lastMenuHeight) {
                float prevTarget = controller.state.targetShiftY;
                controller.computeLayoutMetrics(controller.savedStatusBarHeight, controller.savedNavBarInset, controller.savedMaxBottomInset, currentMenuH);
                float clampedScroll = Utilities.clamp(controller.scrollOffsetY, controller.state.maxScrollOffset, 0.0f);
                if (clampedScroll != controller.scrollOffsetY) {
                    controller.scrollOffsetY = clampedScroll;
                    onScrollOffsetChanged(clampedScroll);
                }
                if (controller.state.targetShiftY != prevTarget) {
                    if (controller.shiftSpring == null) {
                        controller.startShiftSpring(controller.currentShiftY);
                    } else {
                        controller.shiftSpring.animateToFinalPosition(controller.state.targetShiftY);
                    }
                }
                controller.updateTranslations();
            }
        }

        popupScrollable = false;
        LagramMessageFocusController.FocusState st = controller.state;
        if (st.menuMaxBottomY > st.menuTopY) {
            int maxPopupH = (((int) (st.menuMaxBottomY - st.menuTopY)) + backgroundPaddings.top + backgroundPaddings.bottom - getExtraChildrenHeight())
                    + ((menuBar != null && menuBar.isAnimatingExpand && !menuBar.isExpanded) ? menuBar.getExpandedListHeight() : 0);
            if (maxPopupH > 0 && popupLayout.getMeasuredHeight() > maxPopupH) {
                popupScrollable = getPopupContentHeight() > maxPopupH;
                if (menuBar != null) {
                    int mh = popupLayout.getMeasuredHeight();
                    if (menuBar.isAnimatingExpand) {
                        mh -= Math.round((1.0f - menuBar.expandProgress) * menuBar.getExpandedListHeight());
                    }
                    maxPopupH = Math.min(maxPopupH, mh);
                }
                if (maxPopupH < popupLayout.getMeasuredHeight()) {
                    popupLayout.measure(
                            MeasureSpec.makeMeasureSpec(popupLayout.getMeasuredWidth(), MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(maxPopupH, MeasureSpec.AT_MOST)
                    );
                    return;
                }
            }
        }
        if (menuBar != null && menuBar.isAnimatingExpand) {
            int mh = popupLayout.getMeasuredHeight() - Math.round((1.0f - menuBar.expandProgress) * menuBar.getExpandedListHeight());
            if (mh < popupLayout.getMeasuredHeight()) {
                popupLayout.measure(
                        MeasureSpec.makeMeasureSpec(popupLayout.getMeasuredWidth(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(mh, MeasureSpec.AT_MOST)
                );
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        scrimPopupContainerLayout.layout(0, 0, right - left, bottom - top);
        getLocationOnScreen(selfLocation);
        ViewGroup container = controller.containerView;
        LagramMessageFocusController.FocusState st = controller.state;
        if (container != null) {
            container.getLocationOnScreen(containerLocation);
        } else {
            containerLocation[0] = 0;
            containerLocation[1] = 0;
        }
        screenOffsetX = containerLocation[0] - (selfLocation[0] - getTranslationX());
        screenOffsetY = containerLocation[1] - (selfLocation[1] - getTranslationY());

        float margin12 = AndroidUtilities.dp(12.0f);
        RectF bounds = st.bubbleBounds;

        if (reactionsLayout != null && reactionsLayout.getVisibility() != View.GONE) {
            int rw = reactionsLayout.getMeasuredWidth();
            int rh = reactionsLayout.getMeasuredHeight();
            int pl = reactionsLayout.getPaddingLeft();
            int pr = reactionsLayout.getPaddingRight();
            float rx;
            if (st.isActionCell) {
                rx = bounds.centerX() - ((rw + pl - pr) / 2.0f);
            } else if (st.isIncoming) {
                rx = (bounds.right + AndroidUtilities.dp(22.0f) + pr) - rw;
            } else {
                rx = (bounds.left - AndroidUtilities.dp(26.0f)) - pl;
            }
            float minRx = margin12 - pl;
            float maxRx = (st.containerWidth - margin12 + pr) - rw;
            float clampedRx = maxRx < minRx ? (st.containerWidth - rw) / 2.0f : Utilities.clamp(rx, maxRx, minRx);
            float ry = (st.reactionsBottomY + reactionsLayout.getPaddingBottom()) - rh;

            boolean mirror = LocaleController.isRTL || !st.isIncoming;
            float anchorX = st.isActionCell
                    ? bounds.centerX()
                    : (st.isIncoming ? bounds.right - AndroidUtilities.dp(12.0f) : bounds.left + AndroidUtilities.dp(12.0f));
            float circleOff = mirror ? (anchorX - clampedRx) : (clampedRx + rw - anchorX);
            reactionsLayout.bigCircleOffset = (int) Math.max(AndroidUtilities.dp(36.0f), Math.min(rw - AndroidUtilities.dp(36.0f), circleOff));

            int l = Math.round(clampedRx + screenOffsetX);
            int t = Math.round(ry + screenOffsetY);
            reactionsLayout.layout(l, t, l + rw, t + rh);
        }

        int pw = popupLayout.getMeasuredWidth();
        int ph = popupLayout.getMeasuredHeight();
        PopupSwipeBackLayout swipeBack = popupLayout.getSwipeBack();
        int visiblePw = (swipeBack == null || swipeBack.getChildCount() <= 0)
                ? pw
                : pw - Math.max(0, swipeBack.getMeasuredWidth() - swipeBack.getChildAt(0).getMeasuredWidth());
        float contentPw = visiblePw - backgroundPaddings.left - backgroundPaddings.right;
        float px;
        if (st.isActionCell) {
            float cx = bounds.centerX() - contentPw / 2.0f;
            px = Utilities.clamp(cx - backgroundPaddings.left, (st.containerWidth - margin12 + backgroundPaddings.right) - visiblePw, margin12 - backgroundPaddings.left);
        } else if (st.isIncoming) {
            px = Utilities.clamp(bounds.left - backgroundPaddings.left, (st.containerWidth - margin12 + backgroundPaddings.right) - visiblePw, margin12 - backgroundPaddings.left);
        } else {
            px = Utilities.clamp(bounds.right + backgroundPaddings.right, st.containerWidth - margin12 + backgroundPaddings.right, margin12 - backgroundPaddings.left + visiblePw) - pw;
        }
        float py = (st.menuTopY - backgroundPaddings.top) + currentScrollOffset;
        int pl = Math.round(px + screenOffsetX);
        int pt = Math.round(py + screenOffsetY);
        int pr = pl + pw;
        int pb = pt + ph;
        popupLayout.layout(pl, pt, pr, pb);

        pivotX = st.isIncoming ? (pl + backgroundPaddings.left + contentPw / 2.0f) : (pr - backgroundPaddings.right - contentPw / 2.0f);
        pivotY = (pt + backgroundPaddings.top) - st.menuGap;
        popupLayout.setPivotX(pivotX - pl);
        popupLayout.setPivotY(pivotY - pt);

        LinearLayout.LayoutParams popupLp = popupLayout.getLayoutParams() instanceof LinearLayout.LayoutParams
                ? (LinearLayout.LayoutParams) popupLayout.getLayoutParams() : null;
        int nextTop = pb + (popupLp != null ? popupLp.bottomMargin : 0);
        int count = scrimPopupContainerLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = scrimPopupContainerLayout.getChildAt(i);
            if (child == reactionsLayout || child == popupLayout || child.getVisibility() == View.GONE) {
                continue;
            }
            LinearLayout.LayoutParams lp = child.getLayoutParams() instanceof LinearLayout.LayoutParams
                    ? (LinearLayout.LayoutParams) child.getLayoutParams() : null;
            if (lp != null) {
                nextTop += lp.topMargin;
            }
            int cw = child.getMeasuredWidth();
            int ch = child.getMeasuredHeight();
            int cl;
            if (st.isActionCell) {
                cl = Math.round(pl + backgroundPaddings.left + (contentPw - cw) / 2.0f);
            } else if (st.isIncoming) {
                int marginDiff = (lp != null && popupLp != null) ? (lp.leftMargin - popupLp.leftMargin) : 0;
                cl = pl + marginDiff;
            } else {
                cl = pr - cw;
            }
            child.layout(cl, nextTop, cl + cw, nextTop + ch);
            child.setPivotX(pivotX - cl);
            child.setPivotY(pivotY - nextTop);
            nextTop += ch + (lp != null ? lp.bottomMargin : 0);
        }
        resetChildrenTranslationX();
    }

    @Override
    public void setBackgroundColor(int color) {
        if (popupLayout != null) {
            popupLayout.setBackgroundColor(color);
        }
    }
}
