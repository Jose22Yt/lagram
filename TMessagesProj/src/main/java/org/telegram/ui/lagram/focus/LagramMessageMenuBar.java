package org.telegram.ui.lagram.focus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;

import java.util.ArrayList;

public final class LagramMessageMenuBar {

    public final int style; // 1 = Tiles, 2 = Capsule
    public final boolean interactive;
    public final LinearLayout parentLinearLayout;
    public final ActionBarMenuSubItem[] items;
    public final int[] originalHeights;
    public final boolean[] itemVisible;
    public final boolean[] itemEligibleForBar;
    public final boolean[] itemHiddenByBar;
    public final boolean[] itemCompactStyled;
    public final int[] itemMode; // 0 = hidden, 1 = normal, 2 = in top bar, 3 = compact list, 4 = expandable capsule list

    public final ArrayList<View> intermediateGaps = new ArrayList<>();
    public final Theme.ResourcesProvider resourcesProvider;
    public final ActionBarPopupWindow.ActionBarPopupWindowLayout popupWindowLayout;
    public final int originalMinWidth;
    public final Runnable onLayoutInvalidate;

    public final TopBarContainer topBarContainer;
    public final ActionBarPopupWindow.GapView barBottomGap;
    public final ArrayList<TopBarButton> barButtons = new ArrayList<>();
    public TopBarButton moreButton;

    public boolean barActive;
    public boolean hasMoreButton;
    public int maxCapsuleColumns;
    public boolean isAtTopEdge;
    public boolean isAtBottomEdge;
    public boolean isExpanded;
    public float expandProgress;
    public boolean isAnimatingExpand;
    public SpringAnimation expandSpring;
    private float lastAppliedRadius = -1.0f;

    private LagramMessageMenuBar(
            int style,
            boolean interactive,
            LinearLayout linearLayout,
            ActionBarMenuSubItem[] items,
            Theme.ResourcesProvider resourcesProvider,
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupWindowLayout,
            Runnable onLayoutInvalidate
    ) {
        this.style = style;
        this.interactive = interactive;
        this.parentLinearLayout = linearLayout;
        this.items = items;
        this.resourcesProvider = resourcesProvider;
        this.popupWindowLayout = popupWindowLayout;
        this.originalMinWidth = popupWindowLayout != null ? popupWindowLayout.getMinimumWidth() : 0;
        this.onLayoutInvalidate = onLayoutInvalidate;

        int len = items.length;
        this.originalHeights = new int[len];
        this.itemVisible = new boolean[len];
        this.itemEligibleForBar = new boolean[len];
        this.itemHiddenByBar = new boolean[len];
        this.itemCompactStyled = new boolean[len];
        this.itemMode = new int[len];

        for (int i = 0; i < len; i++) {
            this.originalHeights[i] = items[i].getItemHeight();
        }

        Context context = linearLayout.getContext();
        this.topBarContainer = new TopBarContainer(context);
        this.barBottomGap = new ActionBarPopupWindow.GapView(context, resourcesProvider);

        int firstIdx = linearLayout.indexOfChild(items[0]);
        linearLayout.addView(topBarContainer, firstIdx, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(barBottomGap, firstIdx + 1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(8.0f)));

        int lastIdx = linearLayout.indexOfChild(items[len - 1]);
        for (int i = firstIdx + 2; i < lastIdx; i++) {
            View child = linearLayout.getChildAt(i);
            if (child instanceof ActionBarPopupWindow.GapView) {
                intermediateGaps.add(child);
            }
        }
    }

    public static LagramMessageMenuBar create(
            ActionBarMenuSubItem[] rawItems,
            int style,
            Theme.ResourcesProvider resourcesProvider,
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupWindowLayout,
            int availableWidth,
            boolean interactive,
            Runnable onLayoutInvalidate
    ) {
        if (rawItems == null) {
            return null;
        }
        LinearLayout parent = null;
        int validCount = 0;
        for (ActionBarMenuSubItem item : rawItems) {
            if (item != null) {
                if (parent == null) {
                    if (!(item.getParent() instanceof LinearLayout)) {
                        return null;
                    }
                    parent = (LinearLayout) item.getParent();
                }
                if (item.getParent() == parent) {
                    validCount++;
                }
            }
        }
        if (parent == null || validCount < 2) {
            return null;
        }
        ActionBarMenuSubItem[] filtered = new ActionBarMenuSubItem[validCount];
        int idx = 0;
        for (ActionBarMenuSubItem item : rawItems) {
            if (item != null && item.getParent() == parent) {
                filtered[idx++] = item;
            }
        }
        LagramMessageMenuBar bar = new LagramMessageMenuBar(style, interactive, parent, filtered, resourcesProvider, popupWindowLayout, onLayoutInvalidate);
        bar.maxCapsuleColumns = Math.max(2, (availableWidth - AndroidUtilities.dp(2.0f) * 2) / AndroidUtilities.dp(40.0f));
        bar.rebuildBar();
        return bar;
    }

    private static void setViewVisible(View view, boolean visible) {
        int vis = visible ? View.VISIBLE : View.GONE;
        if (view.getVisibility() != vis) {
            view.setVisibility(vis);
        }
    }

    public void updateAvailableWidth(int availableWidth) {
        if (style == LagramConfig.FOCUS_STYLE_CAPSULE) {
            int cols = Math.max(2, (availableWidth - AndroidUtilities.dp(2.0f) * 2) / AndroidUtilities.dp(40.0f));
            if (cols != maxCapsuleColumns) {
                maxCapsuleColumns = cols;
                rebuildBar();
            }
        }
    }

    public boolean needsRemeasureWidth() {
        return topBarContainer.getVisibility() != View.GONE && topBarContainer.getMeasuredWidth() < topBarContainer.desiredWidth;
    }

    public void cancelExpandAnimation() {
        if (isAnimatingExpand) {
            isAnimatingExpand = false;
            if (expandSpring != null) {
                expandSpring.cancel();
                expandSpring = null;
            }
            finishExpandAnimation();
        }
    }

    private void updateExpandVisuals() {
        if (moreButton != null) {
            if (moreButton.activeHighlightProgress != expandProgress) {
                moreButton.activeHighlightProgress = expandProgress;
                moreButton.invalidate();
            }
        }
        if (popupWindowLayout != null && style == LagramConfig.FOCUS_STYLE_CAPSULE) {
            Drawable bg = popupWindowLayout.getBackgroundDrawable();
            if (bg instanceof BlurredBackgroundDrawable) {
                BlurredBackgroundDrawable blurredBg = (BlurredBackgroundDrawable) bg;
                boolean onlyCapsuleAndExpandable = barActive && isOnlyCapsuleItemsInLayout();
                if (onlyCapsuleAndExpandable || lastAppliedRadius >= 0.0f) {
                    float targetRadius = AndroidUtilities.dp(12.0f);
                    if (onlyCapsuleAndExpandable) {
                        targetRadius = AndroidUtilities.lerp(AndroidUtilities.dp(44.0f) / 2.0f, targetRadius, expandProgress);
                    }
                    if (targetRadius != lastAppliedRadius) {
                        lastAppliedRadius = targetRadius;
                        blurredBg.setRadius(targetRadius);
                        popupWindowLayout.invalidate();
                    }
                }
            }
        }
        int expandableCount = 0;
        for (int i = 0; i < items.length; i++) {
            if (itemMode[i] == 4) {
                expandableCount++;
            }
        }
        if (expandableCount == 0) {
            return;
        }
        float stagger = expandableCount > 1 ? Math.min(0.08f, 0.3f / (expandableCount - 1)) : 0.0f;
        float span = 1.0f - (expandableCount - 1) * stagger;
        float slidePx = AndroidUtilities.dp(6.0f);
        int idx = 0;
        for (int i = 0; i < items.length; i++) {
            if (itemMode[i] == 4) {
                float localProg = span > 0.0f ? (expandProgress - idx * stagger) / span : expandProgress;
                float clamped = Utilities.clamp(localProg, 1.0f, 0.0f);
                items[i].setAlpha(clamped);
                items[i].setTranslationY((1.0f - clamped) * (-slidePx));
                idx++;
            }
        }
    }

    private void updateGapsAndSelectors() {
        int barIndex = parentLinearLayout.indexOfChild(topBarContainer);
        if (barIndex < 0) {
            return;
        }
        setViewVisible(barBottomGap, barActive && findNextVisibleChild(barIndex + 1, true) != null);
        for (int i = 0, n = intermediateGaps.size(); i < n; i++) {
            View gap = intermediateGaps.get(i);
            int gapIdx = parentLinearLayout.indexOfChild(gap);
            if (gapIdx >= 0) {
                View prev = findPrevVisibleChild(gapIdx - 1);
                View next = findNextVisibleChild(gapIdx + 1, false);
                boolean show = prev != null && !(prev instanceof ActionBarPopupWindow.GapView)
                        && next != null && !(next instanceof ActionBarPopupWindow.GapView);
                setViewVisible(gap, show);
            }
        }
        isAtTopEdge = findPrevVisibleChild(barIndex - 1) == null;
        isAtBottomEdge = findNextVisibleChild(barIndex + 1, false) == null;

        for (int i = 0, n = barButtons.size(); i < n; i++) {
            barButtons.get(i).syncItemColors();
        }
        if (moreButton != null && moreButton.activeHighlightProgress != expandProgress) {
            moreButton.activeHighlightProgress = expandProgress;
            moreButton.invalidate();
        }

        if (style == LagramConfig.FOCUS_STYLE_TILES) {
            TopBarButton firstVis = null;
            TopBarButton lastVis = null;
            for (int i = 0, n = barButtons.size(); i < n; i++) {
                TopBarButton btn = barButtons.get(i);
                if (btn.getVisibility() != View.GONE) {
                    if (firstVis == null) {
                        firstVis = btn;
                    }
                    lastVis = btn;
                }
            }
            boolean rtl = LocaleController.isRTL;
            for (int i = 0, n = barButtons.size(); i < n; i++) {
                TopBarButton btn = barButtons.get(i);
                if (btn.getVisibility() != View.GONE) {
                    boolean isLeft = !rtl ? (btn == firstVis) : (btn == lastVis);
                    boolean isRight = !rtl ? (btn == lastVis) : (btn == firstVis);
                    int mask = 0;
                    if (isAtTopEdge && isLeft) mask |= 1;
                    if (isAtTopEdge && isRight) mask |= 2;
                    if (isAtBottomEdge && isRight) mask |= 4;
                    if (isAtBottomEdge && isLeft) mask |= 8;
                    if (mask != btn.cornerMask) {
                        btn.cornerMask = mask;
                        if (interactive) {
                            btn.setBackground(btn.createSelector());
                        }
                    }
                }
            }
        }

        for (int i = 0; i < items.length; i++) {
            if (itemCompactStyled[i] && items[i].getVisibility() == View.VISIBLE) {
                int itemIdx = parentLinearLayout.indexOfChild(items[i]);
                items[i].updateSelectorBackground(
                        findPrevVisibleChild(itemIdx - 1) == null,
                        findNextVisibleChild(itemIdx + 1, false) == null
                );
            }
        }

        if (popupWindowLayout != null && style == LagramConfig.FOCUS_STYLE_CAPSULE) {
            int minW = (barActive && isOnlyCapsuleItemsInLayout()) ? 0 : originalMinWidth;
            if (popupWindowLayout.getMinimumWidth() != minW) {
                popupWindowLayout.setMinimumWidth(minW);
            }
        }
    }

    private void applyCompactSubItemStyle(int index, boolean compact) {
        if (itemCompactStyled[index] == compact) {
            return;
        }
        ActionBarMenuSubItem item = items[index];
        if (item.getIconResId() == 0 || item.getCheckView() != null) {
            return;
        }
        if (item.getRightIcon() != null && item.getRightIcon().getVisibility() != View.GONE) {
            return;
        }
        if (originalHeights[index] != 48) {
            return;
        }
        itemCompactStyled[index] = compact;
        boolean rtl = LocaleController.isRTL;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) item.getImageView().getLayoutParams();
        if (compact) {
            item.setItemHeight(40);
            item.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.0f);
            lp.width = AndroidUtilities.dp(20.0f);
            lp.height = AndroidUtilities.dp(20.0f);
            lp.gravity = (rtl ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL;
            item.getImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
            int pad = AndroidUtilities.dp(32.0f);
            item.getTextView().setPadding(rtl ? pad : 0, 0, rtl ? 0 : pad, 0);
        } else {
            item.setItemHeight(originalHeights[index]);
            item.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = AndroidUtilities.dp(40.0f);
            lp.gravity = (rtl ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL;
            item.getImageView().setScaleType(ImageView.ScaleType.CENTER);
            int pad = AndroidUtilities.dp(43.0f);
            item.getTextView().setPadding(rtl ? 0 : pad, 0, rtl ? pad : 0, 0);
        }
        item.getImageView().setLayoutParams(lp);
        item.requestLayout();
    }

    public int getExpandedListHeight() {
        int startIdx = parentLinearLayout.indexOfChild(topBarContainer) + 1;
        int endIdx = parentLinearLayout.indexOfChild(items[items.length - 1]);
        int total = 0;
        if (startIdx > 0 && endIdx >= startIdx) {
            for (int i = startIdx; i <= endIdx; i++) {
                View child = parentLinearLayout.getChildAt(i);
                if (child.getVisibility() != View.GONE) {
                    total += child.getMeasuredHeight();
                    if (child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                        total += lp.topMargin + lp.bottomMargin;
                    }
                }
            }
        }
        return total;
    }

    private void finishExpandAnimation() {
        isAnimatingExpand = false;
        expandProgress = isExpanded ? 1.0f : 0.0f;
        if (!isExpanded) {
            for (int i = 0; i < items.length; i++) {
                if (itemMode[i] == 4) {
                    setItemHiddenByBar(i, true);
                }
            }
            updateGapsAndSelectors();
        }
        updateExpandVisuals();
        parentLinearLayout.requestLayout();
        if (onLayoutInvalidate != null) {
            onLayoutInvalidate.run();
        }
    }

    private boolean isEligibleForBar(int index) {
        ActionBarMenuSubItem item = items[index];
        if (item.getIconResId() == 0) {
            return false;
        }
        if (item.getCheckView() != null) {
            return false;
        }
        if (item.getRightIcon() != null && item.getRightIcon().getVisibility() != View.GONE) {
            return false;
        }
        return item.openSwipeBackLayout == null && originalHeights[index] == 48;
    }

    private View findNextVisibleChild(int startIndex, boolean stopAtGap) {
        int count = parentLinearLayout.getChildCount();
        for (int i = startIndex; i < count; i++) {
            View child = parentLinearLayout.getChildAt(i);
            if (child != barBottomGap && child.getVisibility() == View.VISIBLE) {
                if (stopAtGap && child instanceof ActionBarPopupWindow.GapView) {
                    return null;
                }
                return child;
            }
        }
        return null;
    }

    private View findPrevVisibleChild(int startIndex) {
        for (int i = startIndex; i >= 0; i--) {
            View child = parentLinearLayout.getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                return child;
            }
        }
        return null;
    }

    private boolean isOnlyCapsuleItemsInLayout() {
        int count = parentLinearLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parentLinearLayout.getChildAt(i);
            if (child != topBarContainer && child != barBottomGap && child.getVisibility() == View.VISIBLE && !intermediateGaps.contains(child)) {
                if (child instanceof ActionBarMenuSubItem) {
                    int mode = 0;
                    for (int j = 0; j < items.length; j++) {
                        if (items[j] == child) {
                            mode = itemMode[j];
                            break;
                        }
                    }
                    if (mode == 4) {
                        continue;
                    }
                }
                return false;
            }
        }
        return true;
    }

    private void rebuildBar() {
        int len = items.length;
        int eligibleCount = 0;
        int nonEligibleVisibleCount = 0;
        for (int i = 0; i < len; i++) {
            boolean vis = itemHiddenByBar[i] || items[i].getVisibility() == View.VISIBLE;
            itemVisible[i] = vis;
            boolean elig = vis && isEligibleForBar(i);
            itemEligibleForBar[i] = elig;
            if (elig) {
                eligibleCount++;
            } else if (vis) {
                nonEligibleVisibleCount++;
            }
        }

        hasMoreButton = false;
        if (style == LagramConfig.FOCUS_STYLE_TILES) {
            eligibleCount = Math.min(4, eligibleCount);
        } else {
            if (eligibleCount > maxCapsuleColumns || nonEligibleVisibleCount != 0) {
                eligibleCount = Math.min(eligibleCount, maxCapsuleColumns - 1);
                hasMoreButton = true;
            }
        }

        barActive = eligibleCount >= 2;
        if (!barActive) {
            hasMoreButton = false;
        }

        int barAssigned = 0;
        for (int i = 0; i < len; i++) {
            int mode = 3;
            if (!itemVisible[i]) {
                mode = 0;
            } else if (!barActive) {
                mode = 1;
            } else if (itemEligibleForBar[i] && barAssigned < eligibleCount) {
                barAssigned++;
                mode = 2;
            } else if (style == LagramConfig.FOCUS_STYLE_CAPSULE) {
                mode = 4;
            }
            itemMode[i] = mode;
        }

        if (!barActive && isExpanded) {
            isExpanded = false;
            isAnimatingExpand = false;
            if (expandSpring != null) {
                expandSpring.cancel();
                expandSpring = null;
            }
            expandProgress = 0.0f;
        }

        boolean showExpandableItems = isExpanded || expandProgress > 0.0f;
        for (int i = 0; i < len; i++) {
            if (itemMode[i] != 4) {
                if (items[i].getAlpha() != 1.0f) {
                    items[i].setAlpha(1.0f);
                }
                if (items[i].getTranslationY() != 0.0f) {
                    items[i].setTranslationY(0.0f);
                }
            }
            int mode = itemMode[i];
            if (mode == 1) {
                setItemHiddenByBar(i, false);
                applyCompactSubItemStyle(i, false);
            } else if (mode == 2) {
                setItemHiddenByBar(i, true);
            } else if (mode == 3) {
                setItemHiddenByBar(i, false);
                applyCompactSubItemStyle(i, true);
            } else if (mode == 4) {
                setItemHiddenByBar(i, !showExpandableItems);
                applyCompactSubItemStyle(i, true);
            }
        }

        boolean isTiles = style == LagramConfig.FOCUS_STYLE_TILES;
        int btnIdx = 0;
        if (barActive) {
            for (int i = 0; i < len; i++) {
                if (itemMode[i] == 2) {
                    TopBarButton btn;
                    if (btnIdx < barButtons.size()) {
                        btn = barButtons.get(btnIdx);
                    } else {
                        btn = new TopBarButton(parentLinearLayout.getContext(), isTiles);
                        if (interactive) {
                            btn.setOnClickListener(v -> {
                                if (v instanceof TopBarButton) {
                                    ActionBarMenuSubItem target = ((TopBarButton) v).boundItem;
                                    if (target != null) {
                                        target.performClick();
                                    }
                                }
                            });
                        }
                        barButtons.add(btn);
                        topBarContainer.addView(btn, barButtons.size() - 1);
                    }
                    ActionBarMenuSubItem src = items[i];
                    btn.boundItem = src;
                    int iconRes = src.getIconResId();
                    if (iconRes != btn.iconResId) {
                        btn.iconResId = iconRes;
                        btn.iconView.setImageResource(iconRes);
                    }
                    CharSequence text = src.getTextView().getText();
                    if (btn.labelView != null && !TextUtils.equals(btn.labelView.getText(), text)) {
                        btn.labelView.setText(text);
                    }
                    if (!TextUtils.equals(btn.getContentDescription(), text)) {
                        btn.setContentDescription(text);
                        if (!btn.showLabel && interactive && Build.VERSION.SDK_INT >= 26) {
                            btn.setTooltipText(text);
                        }
                    }
                    btn.syncItemColors();
                    setViewVisible(btn, true);
                    btnIdx++;
                }
            }
        }
        while (btnIdx < barButtons.size()) {
            TopBarButton unused = barButtons.get(btnIdx);
            unused.boundItem = null;
            setViewVisible(unused, false);
            btnIdx++;
        }

        if (hasMoreButton && moreButton == null) {
            moreButton = new TopBarButton(parentLinearLayout.getContext(), false);
            moreButton.iconView.setImageResource(R.drawable.ic_ab_other);
            moreButton.iconView.setRotation(90.0f);
            moreButton.iconView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon, resourcesProvider), PorterDuff.Mode.SRC_IN));
            moreButton.textColor = Theme.getColor(Theme.key_actionBarDefaultSubmenuItem, resourcesProvider);
            String moreDesc = LocaleController.getString(R.string.AccDescrMoreOptions);
            moreButton.setContentDescription(moreDesc);
            if (interactive && Build.VERSION.SDK_INT >= 26) {
                moreButton.setTooltipText(moreDesc);
            }
            if (interactive) {
                moreButton.setOnClickListener(v -> toggleMoreExpanded());
            }
            topBarContainer.addView(moreButton);
        }
        if (moreButton != null) {
            setViewVisible(moreButton, hasMoreButton);
            if (moreButton.activeHighlightProgress != expandProgress) {
                moreButton.activeHighlightProgress = expandProgress;
                moreButton.invalidate();
            }
        }
        setViewVisible(topBarContainer, barActive);
        updateGapsAndSelectors();
        updateExpandVisuals();
    }

    private void toggleMoreExpanded() {
        if (!barActive || !hasMoreButton) {
            return;
        }
        boolean wasExpanded = isExpanded;
        isExpanded = !wasExpanded;
        if (!wasExpanded) {
            for (int i = 0; i < items.length; i++) {
                if (itemMode[i] == 4) {
                    setItemHiddenByBar(i, false);
                }
            }
            updateGapsAndSelectors();
        }
        if (expandSpring == null) {
            SpringAnimation spring = new SpringAnimation(new FloatValueHolder(expandProgress));
            SpringForce force = new SpringForce();
            force.setStiffness(450.0f);
            force.setDampingRatio(0.78f);
            spring.setSpring(force);
            spring.setMinimumVisibleChange(0.002f);
            spring.addUpdateListener((animation, value, velocity) -> {
                expandProgress = Utilities.clamp(value, 1.0f, 0.0f);
                updateExpandVisuals();
                parentLinearLayout.requestLayout();
                if (onLayoutInvalidate != null) {
                    onLayoutInvalidate.run();
                }
            });
            spring.addEndListener((animation, canceled, value, velocity) -> {
                if (!canceled) {
                    finishExpandAnimation();
                }
            });
            expandSpring = spring;
        }
        isAnimatingExpand = true;
        updateExpandVisuals();
        expandSpring.animateToFinalPosition(isExpanded ? 1.0f : 0.0f);
        parentLinearLayout.requestLayout();
        if (onLayoutInvalidate != null) {
            onLayoutInvalidate.run();
        }
    }

    private void setItemHiddenByBar(int index, boolean hide) {
        View view = items[index];
        if (hide) {
            if (view.getVisibility() != View.GONE) {
                view.setVisibility(View.GONE);
            }
            itemHiddenByBar[index] = true;
        } else if (itemHiddenByBar[index]) {
            itemHiddenByBar[index] = false;
            view.setVisibility(View.VISIBLE);
        }
    }

    public boolean syncState() {
        boolean changed = false;
        for (int i = 0; i < items.length; i++) {
            int vis = items[i].getVisibility();
            boolean hidden = itemHiddenByBar[i];
            if (hidden && vis == View.VISIBLE) {
                changed = true;
                break;
            }
            boolean logicallyVisible = hidden || vis == View.VISIBLE;
            if (logicallyVisible != itemVisible[i] || (logicallyVisible && isEligibleForBar(i) != itemEligibleForBar[i])) {
                changed = true;
                break;
            }
        }
        if (changed) {
            rebuildBar();
        }
        for (int i = 0; i < items.length; i++) {
            if (itemCompactStyled[i]) {
                TextView sub = items[i].subtextView;
                int expectedH = (sub == null || sub.getVisibility() != View.VISIBLE) ? 40 : originalHeights[i];
                if (items[i].getItemHeight() != expectedH) {
                    items[i].setItemHeight(expectedH);
                    items[i].requestLayout();
                    changed = true;
                }
            }
        }
        for (int i = 0, n = barButtons.size(); i < n; i++) {
            barButtons.get(i).syncItemColors();
        }
        return changed;
    }

    public final class TopBarContainer extends ViewGroup {
        private final Paint dividerPaint = new Paint();
        int desiredWidth;

        TopBarContainer(Context context) {
            super(context);
            setWillNotDraw(style != LagramConfig.FOCUS_STYLE_TILES);
            dividerPaint.setColor(Theme.multAlpha(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem, resourcesProvider), 0.12f));
        }

        private int measureVisibleChildren(int totalWidth, int visibleCount, int heightSpec) {
            int baseW = totalWidth / visibleCount;
            int rem = totalWidth - (visibleCount * baseW);
            int count = getChildCount();
            int maxH = 0;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != View.GONE) {
                    int cw = baseW + (rem > 0 ? 1 : 0);
                    if (rem > 0) rem--;
                    child.measure(MeasureSpec.makeMeasureSpec(cw, MeasureSpec.EXACTLY), heightSpec);
                    maxH = Math.max(maxH, child.getMeasuredHeight());
                }
            }
            return maxH;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            boolean isTiles = style == LagramConfig.FOCUS_STYLE_TILES;
            int mode = MeasureSpec.getMode(widthMeasureSpec);
            int size = MeasureSpec.getSize(widthMeasureSpec);
            int visibleCount = 0;
            for (int i = 0, n = getChildCount(); i < n; i++) {
                if (getChildAt(i).getVisibility() != View.GONE) {
                    visibleCount++;
                }
            }
            int barHeight = AndroidUtilities.dp(isTiles ? 56.0f : 44.0f);
            int sidePad = isTiles ? 0 : AndroidUtilities.dp(2.0f);
            int targetWidth = isTiles
                    ? AndroidUtilities.dp(72.0f) * 4
                    : (AndroidUtilities.dp(40.0f) * visibleCount) + (sidePad * 2);
            desiredWidth = targetWidth;

            if (!isTiles && mode != MeasureSpec.EXACTLY) {
                int itemWidthSpec = MeasureSpec.makeMeasureSpec(size, mode == MeasureSpec.UNSPECIFIED ? MeasureSpec.UNSPECIFIED : MeasureSpec.AT_MOST);
                int unspecH = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                int maxExpandableW = 0;
                for (int i = 0; i < items.length; i++) {
                    if (itemMode[i] == 4) {
                        items[i].measure(itemWidthSpec, unspecH);
                        maxExpandableW = Math.max(maxExpandableW, items[i].getMeasuredWidth());
                    }
                }
                targetWidth = Math.max(targetWidth, maxExpandableW);
            }
            if (mode != MeasureSpec.EXACTLY) {
                size = mode == MeasureSpec.AT_MOST ? Math.min(targetWidth, size) : targetWidth;
            }
            if (isTiles && visibleCount > 0) {
                barHeight = Math.max(barHeight, measureVisibleChildren(size, visibleCount, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)));
            }
            setMeasuredDimension(size, barHeight);
            if (visibleCount > 0) {
                measureVisibleChildren(Math.max(0, size - sidePad * 2), visibleCount, MeasureSpec.makeMeasureSpec(barHeight, MeasureSpec.EXACTLY));
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int w = r - l;
            boolean rtl = LocaleController.isRTL;
            int offset = style == LagramConfig.FOCUS_STYLE_TILES ? 0 : AndroidUtilities.dp(2.0f);
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != View.GONE) {
                    int cw = child.getMeasuredWidth();
                    int cl = rtl ? (w - offset - cw) : offset;
                    child.layout(cl, 0, cl + cw, child.getMeasuredHeight());
                    offset += cw;
                }
            }
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            if (style != LagramConfig.FOCUS_STYLE_TILES) {
                return;
            }
            float stroke = AndroidUtilities.getShadowHeight();
            float top = AndroidUtilities.dp(10.0f);
            float bottom = getHeight() - AndroidUtilities.dp(10.0f);
            boolean first = true;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }
                if (first) {
                    first = false;
                } else {
                    float x = LocaleController.isRTL ? child.getRight() : child.getLeft();
                    float half = stroke / 2.0f;
                    canvas.drawRect(x - half, top, x + half, bottom, dividerPaint);
                }
            }
        }
    }

    public final class TopBarButton extends LinearLayout {
        final ImageView iconView;
        final TextView labelView;
        final boolean showLabel;
        ActionBarMenuSubItem boundItem;
        int iconResId;
        int textColor;
        ColorFilter iconColorFilter;
        int selectorColor;
        int cornerMask;
        float activeHighlightProgress;
        private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TopBarButton(Context context, boolean showLabel) {
            super(context);
            this.showLabel = showLabel;
            setOrientation(VERTICAL);
            setGravity(showLabel ? (Gravity.TOP | Gravity.CENTER_HORIZONTAL) : Gravity.CENTER);
            if (showLabel) {
                setPadding(0, AndroidUtilities.dp(8.0f), 0, AndroidUtilities.dp(8.0f));
            }
            setWillNotDraw(false);
            setSoundEffectsEnabled(false);

            iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            addView(iconView, new LinearLayout.LayoutParams(AndroidUtilities.dp(22.0f), AndroidUtilities.dp(22.0f)));

            if (showLabel) {
                labelView = new TextView(context);
                labelView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.0f);
                labelView.setMaxLines(2);
                labelView.setEllipsize(TextUtils.TruncateAt.END);
                labelView.setIncludeFontPadding(false);
                labelView.setGravity(Gravity.CENTER_HORIZONTAL);
                labelView.setPadding(AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(4.0f), 0);
                labelView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = AndroidUtilities.dp(4.0f);
                addView(labelView, lp);
            } else {
                labelView = null;
            }

            setClickable(interactive);
            setFocusable(interactive);
            selectorColor = Theme.getColor(Theme.key_dialogButtonSelector, resourcesProvider);
            if (interactive) {
                setBackground(createSelector());
            }
        }

        Drawable createSelector() {
            if (!showLabel) {
                return Theme.createSelectorDrawable(selectorColor, Theme.RIPPLE_MASK_CIRCLE_20DP);
            }
            int rad = 12;
            int tl = (cornerMask & 1) != 0 ? rad : 0;
            int tr = (cornerMask & 2) != 0 ? rad : 0;
            int br = (cornerMask & 4) != 0 ? rad : 0;
            int bl = (cornerMask & 8) != 0 ? rad : 0;
            return Theme.createRadSelectorDrawable(selectorColor, tl, tr, br, bl);
        }

        void syncItemColors() {
            if (boundItem == null) {
                return;
            }
            int curTextCol = boundItem.getTextView().getCurrentTextColor();
            if (curTextCol != textColor) {
                textColor = curTextCol;
                if (labelView != null) {
                    labelView.setTextColor(curTextCol);
                }
            }
            ColorFilter cf = boundItem.getImageView().getColorFilter();
            if (cf != iconColorFilter) {
                iconColorFilter = cf;
                iconView.setColorFilter(cf);
            }
            int selCol = boundItem.getSelectorColor();
            if (selCol != selectorColor) {
                selectorColor = selCol;
                if (interactive) {
                    setBackground(createSelector());
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (activeHighlightProgress > 0.0f) {
                highlightPaint.setColor(Theme.multAlpha(textColor, activeHighlightProgress * 0.1f));
                canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, AndroidUtilities.dp(18.0f), highlightPaint);
            }
        }
    }
}
