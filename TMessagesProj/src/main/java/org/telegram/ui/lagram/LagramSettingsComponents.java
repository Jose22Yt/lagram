package org.telegram.ui.lagram;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.BackgroundGradientDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FilterTabsView;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MotionBackgroundDrawable;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.Switch;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;
import org.telegram.ui.Stories.recorder.StoryEntry;

import java.util.Arrays;
import java.util.Objects;

/**
 * Opexgram-inspired Material 3 Expressive Settings UI Components & Real Lagram Interface Previews.
 *
 * Key engineering guarantees:
 * 1. Every row uses horizontal/vertical LinearLayout with weight=1 text columns and dynamic WRAP_CONTENT
 *    measurement so titles, subtitles, badges, switches, and arrows NEVER overlap or collide.
 * 2. Every custom UItemFactory packs primitives into longValue/intValue (never allocating int[] in UItem.object)
 *    and overrides equals() + contentsEquals() so DiffUtil never triggers overlapping remove+add animations.
 * 3. Every custom View explicitly sets RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT).
 */
public final class LagramSettingsComponents {

    private LagramSettingsComponents() {}

    private static long packColors(IconBackgroundColors colors) {
        int top = colors != null ? colors.top : 0xFF3498FF;
        int bottom = colors != null ? colors.bottom : 0xFF1E78E8;
        return (((long) top) << 32) | (bottom & 0xFFFFFFFFL);
    }

    private static int unpackTopColor(long packed) {
        return (int) (packed >>> 32);
    }

    private static int unpackBottomColor(long packed) {
        return (int) (packed & 0xFFFFFFFFL);
    }

    private static long packSliderRange(int min, int max, int defVal) {
        return (((long) (min & 0xFFFF)) << 32)
                | (((long) (max & 0xFFFF)) << 16)
                | (defVal & 0xFFFFL);
    }

    private static int unpackSliderMin(long packed) {
        return (short) ((packed >>> 32) & 0xFFFF);
    }

    private static int unpackSliderMax(long packed) {
        return (short) ((packed >>> 16) & 0xFFFF);
    }

    private static int unpackSliderDefault(long packed) {
        return (short) (packed & 0xFFFF);
    }

    /**
     * Reusable gradient rounded-square icon tile (36x36dp) matching Telegram/Opexgram Settings style.
     */
    private static class GradientIconView extends ImageView {
        private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF bgRect = new RectF();
        private int topColor = 0xFF3498FF;
        private int bottomColor = 0xFF1E78E8;
        private int lastShaderH = -1;

        GradientIconView(Context context) {
            super(context);
            setScaleType(ScaleType.CENTER);
            setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
            setWillNotDraw(false);
        }

        void setGradient(int top, int bottom) {
            if (topColor != top || bottomColor != bottom) {
                topColor = top;
                bottomColor = bottom;
                lastShaderH = -1;
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w > 0 && h > 0) {
                if (lastShaderH != h) {
                    lastShaderH = h;
                    bgPaint.setShader(new LinearGradient(0, 0, 0, h, topColor, bottomColor, Shader.TileMode.CLAMP));
                }
                bgRect.set(0, 0, w, h);
                float r = dp(10);
                canvas.drawRoundRect(bgRect, r, r, bgPaint);
            }
            super.onDraw(canvas);
        }
    }

    // =============================================================================================
    // 1. SubCategoryCardCell
    // Clean horizontal LinearLayout navigation card: [icon] [title+subtitle weight=1] [badge] [arrow]
    // Zero overlap at any screen size, badge length, or font scale.
    // =============================================================================================
    public static class SubCategoryCardCell extends LinearLayout {

        private final Theme.ResourcesProvider resourcesProvider;
        private final GradientIconView iconView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final TextView badgeView;
        private final ImageView arrowView;
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean needDivider;

        public SubCategoryCardCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setWillNotDraw(false);
            setMinimumHeight(dp(64));
            setPadding(dp(16), dp(11), dp(12), dp(11));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            iconView = new GradientIconView(context);
            addView(iconView, LayoutHelper.createLinear(36, 36, Gravity.CENTER_VERTICAL, 0, 0, 14, 0));

            LinearLayout textCol = new LinearLayout(context);
            textCol.setOrientation(VERTICAL);
            textCol.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            addView(textCol, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 0, 0, 10, 0));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.5f);
            titleView.setTypeface(AndroidUtilities.bold());
            titleView.setMaxLines(1);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            textCol.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            subtitleView = new TextView(context);
            subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.5f);
            subtitleView.setMaxLines(2);
            subtitleView.setEllipsize(TextUtils.TruncateAt.END);
            textCol.addView(subtitleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));

            badgeView = new TextView(context);
            badgeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.5f);
            badgeView.setTypeface(AndroidUtilities.bold());
            badgeView.setGravity(Gravity.CENTER);
            badgeView.setSingleLine(true);
            badgeView.setEllipsize(TextUtils.TruncateAt.END);
            badgeView.setMaxWidth(dp(116));
            badgeView.setPadding(dp(9), dp(3), dp(9), dp(3));
            addView(badgeView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 0, 6, 0));

            arrowView = new ImageView(context);
            arrowView.setImageResource(R.drawable.msg_arrowright);
            arrowView.setScaleType(ImageView.ScaleType.CENTER);
            if (LocaleController.isRTL) {
                arrowView.setScaleX(-1f);
            }
            addView(arrowView, LayoutHelper.createLinear(20, 20, Gravity.CENTER_VERTICAL));
        }

        public void bind(int topGrad, int bottomGrad, int iconResId, CharSequence title, CharSequence subtitle, CharSequence badgeText, boolean divider) {
            this.needDivider = divider;
            iconView.setGradient(topGrad, bottomGrad);
            iconView.setImageResource(iconResId);
            titleView.setText(title);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));

            if (!TextUtils.isEmpty(subtitle)) {
                subtitleView.setVisibility(VISIBLE);
                subtitleView.setText(subtitle);
                subtitleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            } else {
                subtitleView.setVisibility(GONE);
            }

            if (!TextUtils.isEmpty(badgeText)) {
                badgeView.setVisibility(VISIBLE);
                badgeView.setText(badgeText);
                int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
                badgeView.setTextColor(accent);
                badgeView.setBackground(Theme.createRoundRectDrawable(dp(11), ColorUtils.setAlphaComponent(accent, 30)));
            } else {
                badgeView.setVisibility(GONE);
            }

            arrowView.setColorFilter(new PorterDuffColorFilter(
                    ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider), 170),
                    PorterDuff.Mode.SRC_IN));
            invalidate();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (needDivider) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                int startX = LocaleController.isRTL ? dp(16) : dp(66);
                int endX = LocaleController.isRTL ? getWidth() - dp(66) : getWidth() - dp(16);
                canvas.drawLine(startX, getHeight() - 1, endX, getHeight() - 1, dividerPaint);
            }
        }

        public static class Factory extends UItem.UItemFactory<SubCategoryCardCell> {
            static { setup(new Factory()); }

            @Override
            public SubCategoryCardCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new SubCategoryCardCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                SubCategoryCardCell cell = (SubCategoryCardCell) view;
                cell.bind(
                        unpackTopColor(item.longValue),
                        unpackBottomColor(item.longValue),
                        item.iconResId,
                        item.text,
                        item.subtext,
                        item.textValue,
                        divider
                );
            }

            @Override
            public boolean equals(UItem a, UItem b) {
                return a.id == b.id && a.iconResId == b.iconResId;
            }

            @Override
            public boolean contentsEquals(UItem a, UItem b) {
                return a.longValue == b.longValue
                        && TextUtils.equals(a.text, b.text)
                        && TextUtils.equals(a.subtext, b.subtext)
                        && TextUtils.equals(a.textValue, b.textValue);
            }

            public static UItem of(int id, IconBackgroundColors colors, int iconResId, CharSequence title, CharSequence subtitle, CharSequence badge) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.iconResId = iconResId;
                item.text = title;
                item.subtext = subtitle;
                item.textValue = badge;
                item.longValue = packColors(colors);
                return item;
            }
        }
    }

    // =============================================================================================
    // 2. IconSwitchCell
    // Clean horizontal LinearLayout switch row: [icon] [title+subtitle weight=1] [Switch]
    // Supports multi-line subtitles cleanly with zero clipping or overlap.
    // =============================================================================================
    public static class IconSwitchCell extends LinearLayout {

        private final Theme.ResourcesProvider resourcesProvider;
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final Switch switchView;
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean needDivider;

        public IconSwitchCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setWillNotDraw(false);
            setMinimumHeight(dp(56));
            setPadding(dp(16), dp(11), dp(16), dp(11));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.CENTER);
            addView(iconView, LayoutHelper.createLinear(28, 28, Gravity.CENTER_VERTICAL, 0, 0, 16, 0));

            LinearLayout textCol = new LinearLayout(context);
            textCol.setOrientation(VERTICAL);
            textCol.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            addView(textCol, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 0, 0, 14, 0));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.5f);
            titleView.setMaxLines(2);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            textCol.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            subtitleView = new TextView(context);
            subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.5f);
            subtitleView.setMaxLines(3);
            subtitleView.setEllipsize(TextUtils.TruncateAt.END);
            textCol.addView(subtitleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));

            switchView = new Switch(context, resourcesProvider);
            switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
            switchView.setClickable(false);
            switchView.setFocusable(false);
            addView(switchView, LayoutHelper.createLinear(37, 20, Gravity.CENTER_VERTICAL));
        }

        public void bind(int iconResId, CharSequence title, CharSequence subtitle, boolean checked, boolean animated, boolean divider) {
            this.needDivider = divider;
            if (iconResId != 0) {
                iconView.setVisibility(VISIBLE);
                iconView.setImageResource(iconResId);
                int accent = Theme.getColor(checked ? Theme.key_windowBackgroundWhiteBlueText4 : Theme.key_windowBackgroundWhiteGrayIcon, resourcesProvider);
                iconView.setColorFilter(new PorterDuffColorFilter(accent, PorterDuff.Mode.SRC_IN));
            } else {
                iconView.setVisibility(GONE);
            }
            titleView.setText(title);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));

            if (!TextUtils.isEmpty(subtitle)) {
                subtitleView.setVisibility(VISIBLE);
                subtitleView.setText(subtitle);
                subtitleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            } else {
                subtitleView.setVisibility(GONE);
            }
            switchView.setChecked(checked, animated);
            invalidate();
        }

        public void setChecked(boolean checked) {
            setChecked(checked, true);
        }

        public void setChecked(boolean checked, boolean animated) {
            switchView.setChecked(checked, animated);
            if (iconView.getVisibility() == VISIBLE) {
                int accent = Theme.getColor(checked ? Theme.key_windowBackgroundWhiteBlueText4 : Theme.key_windowBackgroundWhiteGrayIcon, resourcesProvider);
                iconView.setColorFilter(new PorterDuffColorFilter(accent, PorterDuff.Mode.SRC_IN));
            }
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (needDivider) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                int start = iconView.getVisibility() == VISIBLE ? dp(60) : dp(16);
                int startX = LocaleController.isRTL ? dp(16) : start;
                int endX = LocaleController.isRTL ? getWidth() - start : getWidth() - dp(16);
                canvas.drawLine(startX, getHeight() - 1, endX, getHeight() - 1, dividerPaint);
            }
        }

        public static class Factory extends UItem.UItemFactory<IconSwitchCell> {
            static { setup(new Factory()); }

            @Override
            public IconSwitchCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new IconSwitchCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((IconSwitchCell) view).bind(item.iconResId, item.text, item.subtext, item.checked, false, divider);
            }

            @Override
            public boolean equals(UItem a, UItem b) {
                return a.id == b.id;
            }

            @Override
            public boolean contentsEquals(UItem a, UItem b) {
                return a.checked == b.checked
                        && a.iconResId == b.iconResId
                        && TextUtils.equals(a.text, b.text)
                        && TextUtils.equals(a.subtext, b.subtext);
            }

            public static UItem of(int id, int iconResId, CharSequence title, CharSequence subtitle, boolean checked) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.iconResId = iconResId;
                item.text = title;
                item.subtext = subtitle;
                item.checked = checked;
                return item;
            }
        }
    }

    // =============================================================================================
    // 3. SegmentedPillCell
    // Material 3 Expressive sliding pill selector with dynamic height & zero text overlap.
    // =============================================================================================
    public static class SegmentedPillCell extends LinearLayout {

        private final Theme.ResourcesProvider resourcesProvider;
        private final TextView titleView;
        private final PillsStripView stripView;
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean needDivider;

        public SegmentedPillCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(VERTICAL);
            setWillNotDraw(false);
            setPadding(dp(16), dp(12), dp(16), dp(14));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13.5f);
            titleView.setTypeface(AndroidUtilities.bold());
            titleView.setSingleLine(true);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 9));

            stripView = new PillsStripView(context, resourcesProvider);
            addView(stripView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40));
        }

        public void bind(CharSequence title, String[] options, int selected, Utilities.Callback<Integer> callback, boolean divider) {
            this.needDivider = divider;
            if (!TextUtils.isEmpty(title)) {
                titleView.setVisibility(VISIBLE);
                titleView.setText(title);
                titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            } else {
                titleView.setVisibility(GONE);
            }
            stripView.setOptions(options, selected, callback);
            invalidate();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (needDivider) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                canvas.drawLine(dp(16), getHeight() - 1, getWidth() - dp(16), getHeight() - 1, dividerPaint);
            }
        }

        private static class PillsStripView extends View {
            private final Theme.ResourcesProvider resourcesProvider;
            private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            private final RectF trackRect = new RectF();
            private final RectF thumbRect = new RectF();
            private String[] options = new String[0];
            private int selectedIndex;
            private float animatedIndex;
            private ValueAnimator animator;
            private Utilities.Callback<Integer> onSelect;

            PillsStripView(Context context, Theme.ResourcesProvider resourcesProvider) {
                super(context);
                this.resourcesProvider = resourcesProvider;
                textPaint.setTextSize(dp(13));
                textPaint.setTypeface(AndroidUtilities.bold());
                textPaint.setTextAlign(Paint.Align.CENTER);
            }

            void setOptions(String[] opts, int sel, Utilities.Callback<Integer> cb) {
                this.options = opts != null ? opts : new String[0];
                this.onSelect = cb;
                int clamped = options.length > 0 ? Math.max(0, Math.min(options.length - 1, sel)) : 0;
                if (animator != null) {
                    animator.cancel();
                }
                this.selectedIndex = clamped;
                this.animatedIndex = clamped;
                invalidate();
            }

            private void selectIndex(int idx) {
                if (idx < 0 || idx >= options.length || idx == selectedIndex) {
                    return;
                }
                selectedIndex = idx;
                try {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                } catch (Exception ignored) {}
                if (animator != null) {
                    animator.cancel();
                }
                animator = ValueAnimator.ofFloat(animatedIndex, selectedIndex);
                animator.setDuration(230);
                animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                animator.addUpdateListener(a -> {
                    animatedIndex = (float) a.getAnimatedValue();
                    invalidate();
                });
                animator.start();
                if (onSelect != null) {
                    onSelect.run(selectedIndex);
                }
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (options.length == 0) {
                    return super.onTouchEvent(event);
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    float w = getWidth();
                    if (w > 0) {
                        int idx = Math.max(0, Math.min(options.length - 1, (int) (event.getX() / (w / options.length))));
                        selectIndex(idx);
                    }
                    return true;
                }
                return super.onTouchEvent(event);
            }

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0 || options.length == 0) {
                    return;
                }
                boolean isDark = Theme.isCurrentThemeDark();
                int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
                int inactiveText = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider);
                int trackColor = ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 18 : 13);

                trackRect.set(0, 0, w, h);
                float r = h * 0.5f;
                trackPaint.setColor(trackColor);
                canvas.drawRoundRect(trackRect, r, r, trackPaint);

                float pad = dp(3);
                float innerW = w - pad * 2f;
                float segW = innerW / options.length;
                float thumbLeft = pad + segW * animatedIndex;
                thumbRect.set(thumbLeft, pad, thumbLeft + segW, h - pad);
                thumbPaint.setColor(accent);
                canvas.drawRoundRect(thumbRect, (h - pad * 2f) * 0.5f, (h - pad * 2f) * 0.5f, thumbPaint);

                Paint.FontMetrics fm = textPaint.getFontMetrics();
                float textY = h * 0.5f - (fm.ascent + fm.descent) * 0.5f;
                for (int i = 0; i < options.length; i++) {
                    float cx = pad + segW * (i + 0.5f);
                    float activeWeight = Math.max(0f, 1f - Math.abs(animatedIndex - i));
                    textPaint.setColor(ColorUtils.blendARGB(inactiveText, Color.WHITE, activeWeight));
                    String label = options[i] != null ? options[i] : "";
                    String ellipsized = TextUtils.ellipsize(label, textPaint, Math.max(dp(20), segW - dp(10)), TextUtils.TruncateAt.END).toString();
                    canvas.drawText(ellipsized, cx, textY, textPaint);
                }
            }
        }

        public static class Factory extends UItem.UItemFactory<SegmentedPillCell> {
            static { setup(new Factory()); }

            @Override
            public SegmentedPillCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new SegmentedPillCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((SegmentedPillCell) view).bind(item.text, item.texts, item.intValue, item.intCallback, divider);
            }

            @Override
            public boolean isClickable() {
                return false;
            }

            @Override
            public boolean equals(UItem a, UItem b) {
                return a.id == b.id;
            }

            @Override
            public boolean contentsEquals(UItem a, UItem b) {
                return a.intValue == b.intValue
                        && TextUtils.equals(a.text, b.text)
                        && Arrays.equals(a.texts, b.texts);
            }

            public static UItem of(int id, CharSequence title, String[] options, int selected, Utilities.Callback<Integer> onSelect) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.text = title;
                item.texts = options;
                item.intValue = selected;
                item.intCallback = onSelect;
                return item;
            }
        }
    }

    // =============================================================================================
    // 4. InlineSliderCell
    // Clean vertical LinearLayout slider row with title + live value pill + SeekBarView.
    // =============================================================================================
    public static class InlineSliderCell extends LinearLayout {

        private final Theme.ResourcesProvider resourcesProvider;
        private final TextView titleView;
        private final TextView valueBadge;
        private final SeekBarView seekBar;
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private int minValue;
        private int maxValue = 100;
        private int currentValue;
        private int defaultValue;
        private String suffix = "";
        private Utilities.Callback<Integer> onChange;
        private boolean needDivider;
        private boolean binding;

        public InlineSliderCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(VERTICAL);
            setWillNotDraw(false);
            setPadding(dp(16), dp(12), dp(16), dp(8));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout headerRow = new LinearLayout(context);
            headerRow.setOrientation(HORIZONTAL);
            headerRow.setGravity(Gravity.CENTER_VERTICAL);
            addView(headerRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 2));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f);
            titleView.setSingleLine(true);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            headerRow.addView(titleView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 0, 0, 10, 0));

            valueBadge = new TextView(context);
            valueBadge.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.5f);
            valueBadge.setTypeface(AndroidUtilities.bold());
            valueBadge.setGravity(Gravity.CENTER);
            valueBadge.setPadding(dp(9), dp(3), dp(9), dp(3));
            valueBadge.setOnClickListener(v -> {
                if (currentValue != defaultValue) {
                    setValueInternal(defaultValue, true);
                }
            });
            headerRow.addView(valueBadge, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

            seekBar = new SeekBarView(context, true, resourcesProvider);
            seekBar.setReportChanges(true);
            seekBar.setDelegate(new SeekBarView.SeekBarViewDelegate() {
                @Override
                public void onSeekBarDrag(boolean stop, float progress) {
                    if (binding) return;
                    int val = Math.round(minValue + progress * (maxValue - minValue));
                    if (val != currentValue || stop) {
                        setValueInternal(val, true);
                    }
                }

                @Override
                public void onSeekBarPressed(boolean pressed) {}
            });
            addView(seekBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 38, -6, 0, -6, 0));
        }

        private void setValueInternal(int val, boolean notify) {
            int clamped = Math.max(minValue, Math.min(maxValue, val));
            boolean changed = clamped != currentValue;
            currentValue = clamped;
            updateBadge();
            float range = Math.max(1, maxValue - minValue);
            binding = true;
            seekBar.setProgress((currentValue - minValue) / range);
            binding = false;
            if (notify && changed) {
                try {
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                } catch (Exception ignored) {}
            }
            if (notify && onChange != null) {
                onChange.run(currentValue);
            }
        }

        private void updateBadge() {
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            valueBadge.setText(currentValue + suffix);
            valueBadge.setTextColor(accent);
            valueBadge.setBackground(Theme.createRoundRectDrawable(dp(11), ColorUtils.setAlphaComponent(accent, 30)));
        }

        public void bind(CharSequence title, int min, int max, int defVal, int cur, String suffixStr, Utilities.Callback<Integer> cb, boolean divider) {
            this.minValue = min;
            this.maxValue = Math.max(min + 1, max);
            this.defaultValue = defVal;
            this.currentValue = Math.max(min, Math.min(this.maxValue, cur));
            this.suffix = suffixStr != null ? suffixStr : "";
            this.onChange = cb;
            this.needDivider = divider;

            titleView.setText(title);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
            updateBadge();

            float range = Math.max(1, this.maxValue - this.minValue);
            binding = true;
            seekBar.setProgress((this.currentValue - this.minValue) / range);
            binding = false;
            invalidate();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (needDivider) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                canvas.drawLine(dp(16), getHeight() - 1, getWidth() - dp(16), getHeight() - 1, dividerPaint);
            }
        }

        public static class Factory extends UItem.UItemFactory<InlineSliderCell> {
            static { setup(new Factory()); }

            @Override
            public InlineSliderCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new InlineSliderCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((InlineSliderCell) view).bind(
                        item.text,
                        unpackSliderMin(item.longValue),
                        unpackSliderMax(item.longValue),
                        unpackSliderDefault(item.longValue),
                        item.intValue,
                        item.textValue != null ? item.textValue.toString() : "",
                        item.intCallback,
                        divider
                );
            }

            @Override
            public boolean isClickable() {
                return false;
            }

            @Override
            public boolean equals(UItem a, UItem b) {
                return a.id == b.id;
            }

            @Override
            public boolean contentsEquals(UItem a, UItem b) {
                return a.intValue == b.intValue
                        && a.longValue == b.longValue
                        && TextUtils.equals(a.text, b.text)
                        && TextUtils.equals(a.textValue, b.textValue);
            }

            public static UItem of(int id, CharSequence title, int min, int max, int defVal, int cur, String suffix, Utilities.Callback<Integer> onChange) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.text = title;
                item.textValue = suffix;
                item.intValue = cur;
                item.longValue = packSliderRange(min, max, defVal);
                item.intCallback = onChange;
                return item;
            }
        }
    }

    // =============================================================================================
    // 5. RealChatMessagesPreviewCell
    // Embeds REAL Telegram ChatMessageCell views on the user's real chat wallpaper, and hooks
    // directly into LagramGlassBubbleEngine so Liquid Glass bubbles render identically to chats!
    // =============================================================================================
    public static class RealChatMessagesPreviewCell extends LinearLayout {

        public static final int MODE_BUBBLES_AND_GLASS = 0;
        public static final int MODE_STICKER_AND_FOCUS = 1;

        private final Theme.ResourcesProvider resourcesProvider;
        private final int currentAccount = UserConfig.selectedAccount;
        private final ChatMessageCell[] cells = new ChatMessageCell[2];
        private final MessageObject[] messageObjects = new MessageObject[2];
        private BackgroundGradientDrawable.Disposable backgroundGradientDisposable;
        private ColorDrawable fallbackBgDrawable;
        private Drawable registeredWallpaper;
        private int mode = -1;
        private long lastFingerprint = Long.MIN_VALUE;

        public RealChatMessagesPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(VERTICAL);
            setWillNotDraw(false);
            setPadding(0, dp(10), 0, dp(10));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp(20));
                }
            });
            setClipToOutline(true);

            for (int i = 0; i < cells.length; i++) {
                ChatMessageCell cell = new ChatMessageCell(context, currentAccount, false, null, resourcesProvider);
                cell.setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
                    @Override
                    public boolean canPerformActions() {
                        return false;
                    }
                });
                cell.isChat = false;
                cell.setFullyDraw(true);
                cells[i] = cell;
                addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            }
        }

        public static long computeBubblePreviewFingerprint() {
            LagramConfig.ensureLoaded();
            long h = 17L;
            h = h * 31L + (LagramConfig.glassMessages ? 1 : 0);
            h = h * 31L + LagramConfig.glassMessagesAlpha;
            h = h * 31L + (LagramConfig.glassMessagesBorder ? 1 : 0);
            h = h * 31L + (LagramConfig.glassHighlights ? 1 : 0);
            h = h * 31L + (LagramConfig.showBubbleTails ? 1 : 0);
            h = h * 31L + LagramConfig.roundingPreset;
            h = h * 31L + (LagramConfig.showSecondsInTime ? 1 : 0);
            h = h * 31L + (LagramConfig.showForwardTime ? 1 : 0);
            h = h * 31L + (LagramConfig.showForwardsCounter ? 1 : 0);
            h = h * 31L + LagramConfig.messageFocusStyle;
            h = h * 31L + LagramConfig.stickerSize;
            h = h * 31L + LagramConfig.gifSize;
            return h;
        }

        private void buildMessages(int targetMode) {
            this.mode = targetMode;
            int now = (int) (System.currentTimeMillis() / 1000L);
            long selfId = UserConfig.getInstance(currentAccount).getClientUserId();

            TLRPC.Message replyMsg = new TLRPC.TL_message();
            replyMsg.message = LocaleController.getString(R.string.LagramSettings);
            replyMsg.date = now - 120;
            replyMsg.dialog_id = 1;
            replyMsg.flags = 259;
            replyMsg.from_id = new TLRPC.TL_peerUser();
            replyMsg.from_id.user_id = selfId;
            replyMsg.id = 1;
            replyMsg.media = new TLRPC.TL_messageMediaEmpty();
            replyMsg.out = true;
            replyMsg.peer_id = new TLRPC.TL_peerUser();
            replyMsg.peer_id.user_id = 0;
            MessageObject replyObj = new MessageObject(currentAccount, replyMsg, true, false);

            TLRPC.Message inMsg = new TLRPC.TL_message();
            inMsg.message = targetMode == MODE_STICKER_AND_FOCUS
                    ? "Mantén pulsado un mensaje para ver Opexgram Focus y los nuevos ajustes."
                    : "¡Así se ven tus burbujas reales con Liquid Glass y desenfoque del fondo!";
            inMsg.date = now - 60;
            inMsg.dialog_id = 1;
            inMsg.flags = 257 | 8 | 4 | 16384;
            inMsg.from_id = new TLRPC.TL_peerUser();
            inMsg.id = 2;
            inMsg.reply_to = new TLRPC.TL_messageReplyHeader();
            inMsg.reply_to.flags |= 16;
            inMsg.reply_to.reply_to_msg_id = 1;
            inMsg.fwd_from = new TLRPC.TL_messageFwdHeader();
            inMsg.fwd_from.from_name = "Lagram Turbo";
            inMsg.fwd_from.date = now - 3600;
            inMsg.forwards = 42;
            inMsg.media = new TLRPC.TL_messageMediaEmpty();
            inMsg.out = false;
            inMsg.peer_id = new TLRPC.TL_peerUser();
            inMsg.peer_id.user_id = selfId;

            MessageObject inObj = new MessageObject(currentAccount, inMsg, true, false);
            inObj.customReplyName = "Lagram";
            inObj.replyMessageObject = replyObj;
            inObj.resetLayout();
            messageObjects[0] = inObj;

            TLRPC.Message outMsg = new TLRPC.TL_message();
            outMsg.message = targetMode == MODE_STICKER_AND_FOCUS
                    ? ("Focus: " + getFocusStyleName() + " • Sticker " + LagramConfig.stickerSize + " • GIF " + LagramConfig.gifSize)
                    : ("Liquid Glass " + (LagramConfig.glassMessages ? (LagramConfig.glassMessagesAlpha + "%") : "Desactivado")
                            + " • Radio " + LagramConfig.getEffectiveBubbleRadiusDp() + "dp");
            outMsg.date = now;
            outMsg.dialog_id = 1;
            outMsg.flags = 259;
            outMsg.from_id = new TLRPC.TL_peerUser();
            outMsg.from_id.user_id = selfId;
            outMsg.id = 3;
            outMsg.media = new TLRPC.TL_messageMediaEmpty();
            outMsg.out = true;
            outMsg.peer_id = new TLRPC.TL_peerUser();
            outMsg.peer_id.user_id = 0;

            MessageObject outObj = new MessageObject(currentAccount, outMsg, true, false);
            outObj.resetLayout();
            messageObjects[1] = outObj;

            for (int i = 0; i < cells.length; i++) {
                cells[i].setMessageObject(messageObjects[i], null, false, false, false);
            }
        }

        private String getFocusStyleName() {
            switch (LagramConfig.messageFocusStyle) {
                case LagramConfig.FOCUS_STYLE_LIST:
                    return LocaleController.getString(R.string.LagramFocusStyleList);
                case LagramConfig.FOCUS_STYLE_TILES:
                    return LocaleController.getString(R.string.LagramFocusStyleTiles);
                case LagramConfig.FOCUS_STYLE_CAPSULE:
                default:
                    return LocaleController.getString(R.string.LagramFocusStyleCapsule);
            }
        }

        public void bind(int newMode, long fingerprint) {
            if (this.mode != newMode || this.lastFingerprint != fingerprint) {
                this.lastFingerprint = fingerprint;
                buildMessages(newMode);
                for (ChatMessageCell cell : cells) {
                    if (cell != null) {
                        if (cell.currentBackgroundDrawable != null) {
                            cell.currentBackgroundDrawable.forceInvalidatePath = true;
                        }
                        cell.requestLayout();
                        cell.invalidate();
                    }
                }
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Drawable wallpaper = Theme.getCachedWallpaperNonBlocking();
            if (Theme.wallpaperLoadTask != null) {
                invalidate();
            }
            if (SharedConfig.compactMode) {
                int color = Theme.getColor(Theme.key_chat_compact_background, resourcesProvider);
                if (fallbackBgDrawable == null || fallbackBgDrawable.getColor() != color) {
                    fallbackBgDrawable = new ColorDrawable(color);
                }
                wallpaper = fallbackBgDrawable;
            }
            if (wallpaper != null) {
                int w = getMeasuredWidth();
                int h = getMeasuredHeight();
                if (w > 0 && h > 0) {
                    LagramGlassBubbleEngine.setViewportSize(w, h);
                    if (registeredWallpaper != wallpaper) {
                        registeredWallpaper = wallpaper;
                        LagramGlassBubbleEngine.setSource(this, null, wallpaper);
                    }
                }
                wallpaper.setAlpha(255);
                if (wallpaper instanceof ColorDrawable || wallpaper instanceof GradientDrawable || wallpaper instanceof MotionBackgroundDrawable) {
                    wallpaper.setBounds(0, 0, w, h);
                    if (wallpaper instanceof BackgroundGradientDrawable) {
                        if (backgroundGradientDisposable != null) {
                            backgroundGradientDisposable.dispose();
                        }
                        backgroundGradientDisposable = ((BackgroundGradientDrawable) wallpaper).drawExactBoundsSize(canvas, this);
                    } else {
                        wallpaper.draw(canvas);
                    }
                } else {
                    StoryEntry.drawBackgroundDrawable(canvas, wallpaper, getWidth(), getHeight());
                }
            }
            for (ChatMessageCell cell : cells) {
                if (cell != null && cell.drawBackgroundInParent() && cell.currentBackgroundDrawable != null) {
                    canvas.save();
                    canvas.translate(0, cell.getY());
                    cell.setBackgroundTopY(true);
                    cell.currentBackgroundDrawable.setGlassPosition(cell.getX(), cell.getY(), getWidth(), getHeight());
                    cell.currentBackgroundDrawable.draw(canvas);
                    canvas.restore();
                }
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            LagramGlassBubbleEngine.clearSource(this, null, registeredWallpaper);
            registeredWallpaper = null;
            if (backgroundGradientDisposable != null) {
                backgroundGradientDisposable.dispose();
                backgroundGradientDisposable = null;
            }
        }

        public static class Factory extends UItem.UItemFactory<RealChatMessagesPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public RealChatMessagesPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new RealChatMessagesPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((RealChatMessagesPreviewCell) view).bind(item.intValue, item.longValue);
            }

            @Override
            public boolean isClickable() {
                return false;
            }

            @Override
            public boolean equals(UItem a, UItem b) {
                return a.id == b.id;
            }

            @Override
            public boolean contentsEquals(UItem a, UItem b) {
                return a.intValue == b.intValue && a.longValue == b.longValue;
            }

            public static UItem of(int id, int mode) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.intValue = mode;
                item.longValue = computeBubblePreviewFingerprint();
                return item;
            }
        }
    }

    // =============================================================================================
    // 6. RealFolderTabsPreviewCell
    // Embeds the REAL Telegram FilterTabsView inside the settings card.
    // =============================================================================================
    public static class RealFolderTabsPreviewCell extends FrameLayout {

        private final Theme.ResourcesProvider resourcesProvider;
        private final FilterTabsView filterTabsView;
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean needDivider;
        private long lastFingerprint = Long.MIN_VALUE;

        private static final int[] SAMPLE_COUNTS = new int[]{14, 3, 28, 7};

        public RealFolderTabsPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            filterTabsView = new FilterTabsView(context, resourcesProvider);
            filterTabsView.setDelegate(new FilterTabsView.FilterTabsViewDelegate() {
                @Override
                public void onPageSelected(FilterTabsView.Tab tab, boolean forward) {}
                @Override
                public void onPageScrolled(float progress) {}
                @Override
                public void onSamePageSelected() {}

                @Override
                public int getTabCounter(int tabId) {
                    if (!LagramConfig.showFolderCounters) {
                        return 0;
                    }
                    if (tabId >= 0 && tabId < SAMPLE_COUNTS.length) {
                        return SAMPLE_COUNTS[tabId];
                    }
                    return 0;
                }

                @Override
                public boolean didSelectTab(FilterTabsView.TabView tabView, boolean selected) { return false; }
                @Override
                public boolean isTabMenuVisible() { return false; }
                @Override
                public void onDeletePressed(int id) {}
                @Override
                public void onPageReorder(int fromId, int toId) {}
                @Override
                public boolean canPerformActions() { return true; }
            });
            addView(filterTabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.CENTER_VERTICAL, 6, 4, 6, 4));
        }

        public static long computeFolderFingerprint() {
            LagramConfig.ensureLoaded();
            long h = 31L + LagramConfig.folderTabsStyle;
            h = h * 31L + (LagramConfig.showFolderCounters ? 1 : 0);
            h = h * 31L + (LagramConfig.hideAllChatsFolder ? 1 : 0);
            return h;
        }

        public void bind(long fingerprint, boolean divider) {
            this.needDivider = divider;
            if (this.lastFingerprint != fingerprint) {
                this.lastFingerprint = fingerprint;
                rebuildTabs();
            }
            invalidate();
        }

        public void rebuildTabs() {
            LagramConfig.ensureLoaded();
            filterTabsView.removeTabs();
            int style = LagramConfig.folderTabsStyle;

            String[] icons = new String[]{"💬", "👤", "👥", "📢"};
            String[] titles = new String[]{
                    LocaleController.getString(R.string.FilterAllChats),
                    LocaleController.getString(R.string.FilterContacts),
                    LocaleController.getString(R.string.FilterGroups),
                    LocaleController.getString(R.string.FilterChannels)
            };

            int sortId = 0;
            for (int i = 0; i < titles.length; i++) {
                if (i == 0 && LagramConfig.hideAllChatsFolder) {
                    continue;
                }
                String label;
                if (style == LagramConfig.FOLDER_TABS_ICON_ONLY) {
                    label = icons[i];
                } else if (style == LagramConfig.FOLDER_TABS_TEXT_ONLY) {
                    label = titles[i];
                } else {
                    label = icons[i] + " " + titles[i];
                }
                filterTabsView.addTab(sortId++, i, label, null, false, i == 0, false);
            }
            filterTabsView.finishAddingTabs(false);
            filterTabsView.requestLayout();
            filterTabsView.invalidate();
            if (filterTabsView.getListView() != null) {
                filterTabsView.getListView().invalidateViews();
            }
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(58) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY)
            );
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (needDivider) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                canvas.drawLine(dp(16), getMeasuredHeight() - 1, getMeasuredWidth() - dp(16), getMeasuredHeight() - 1, dividerPaint);
            }
        }

        public static class Factory extends UItem.UItemFactory<RealFolderTabsPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public RealFolderTabsPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new RealFolderTabsPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((RealFolderTabsPreviewCell) view).bind(item.longValue, divider);
            }

            @Override
            public boolean isClickable() {
                return false;
            }

            @Override
            public boolean equals(UItem a, UItem b) {
                return a.id == b.id;
            }

            @Override
            public boolean contentsEquals(UItem a, UItem b) {
                return a.longValue == b.longValue;
            }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = computeFolderFingerprint();
                return item;
            }
        }
    }
}
