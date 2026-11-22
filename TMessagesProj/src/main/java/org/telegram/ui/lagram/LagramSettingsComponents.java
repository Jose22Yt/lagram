package org.telegram.ui.lagram;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

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

/**
 * Opexgram-inspired Material 3 Expressive Settings UI Components & Real Lagram Interface Previews.
 */
public final class LagramSettingsComponents {

    private LagramSettingsComponents() {}

    // =============================================================================================
    // 1. SubCategoryCardCell
    // Elegant 2-line navigation card with gradient icon badge, title, subtitle, value pill & arrow.
    // =============================================================================================
    public static class SubCategoryCardCell extends FrameLayout {

        private final Theme.ResourcesProvider resourcesProvider;
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final TextView badgeView;
        private final ImageView arrowView;
        private final Paint iconBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF iconRect = new RectF();
        private int topColor;
        private int bottomColor;
        private int lastShaderHeight = -1;
        private boolean needDivider;

        public SubCategoryCardCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);

            iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.CENTER);
            iconView.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
            addView(iconView, LayoutHelper.createFrame(38, 38,
                    (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL,
                    16, 0, 16, 0));

            LinearLayout textCol = new LinearLayout(context);
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            addView(textCol, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL,
                    LocaleController.isRTL ? 92 : 68, 8, LocaleController.isRTL ? 68 : 92, 8));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.5f);
            titleView.setTypeface(AndroidUtilities.bold());
            titleView.setSingleLine(true);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            textCol.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            subtitleView = new TextView(context);
            subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.5f);
            subtitleView.setSingleLine(true);
            subtitleView.setEllipsize(TextUtils.TruncateAt.END);
            textCol.addView(subtitleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));

            badgeView = new TextView(context);
            badgeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.5f);
            badgeView.setTypeface(AndroidUtilities.bold());
            badgeView.setGravity(Gravity.CENTER);
            badgeView.setPadding(dp(8), dp(2), dp(8), dp(2));
            addView(badgeView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 22,
                    (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL,
                    40, 0, 40, 0));

            arrowView = new ImageView(context);
            arrowView.setImageResource(R.drawable.msg_arrowright);
            arrowView.setScaleType(ImageView.ScaleType.CENTER);
            if (LocaleController.isRTL) {
                arrowView.setScaleX(-1f);
            }
            addView(arrowView, LayoutHelper.createFrame(24, 24,
                    (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL,
                    12, 0, 12, 0));
        }

        public void bind(int topGrad, int bottomGrad, int iconResId, CharSequence title, CharSequence subtitle, CharSequence badgeText, boolean divider) {
            this.needDivider = divider;
            if (this.topColor != topGrad || this.bottomColor != bottomGrad) {
                this.topColor = topGrad;
                this.bottomColor = bottomGrad;
                this.lastShaderHeight = -1;
            }
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
                badgeView.setBackground(Theme.createRoundRectDrawable(dp(11),
                        ColorUtils.setAlphaComponent(accent, Theme.isCurrentThemeDark() ? 45 : 28)));
            } else {
                badgeView.setVisibility(GONE);
            }

            arrowView.setColorFilter(new PorterDuffColorFilter(
                    Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon, resourcesProvider),
                    PorterDuff.Mode.SRC_IN
            ));
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(64) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY)
            );
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int h = getMeasuredHeight();
            int w = getMeasuredWidth();
            float cy = h / 2f;
            float size = dp(38);
            float left = LocaleController.isRTL ? (w - dp(16) - size) : dp(16);
            float top = cy - size / 2f;
            iconRect.set(left, top, left + size, top + size);
            if (lastShaderHeight != h) {
                lastShaderHeight = h;
                iconBgPaint.setShader(new LinearGradient(left, top, left, top + size, topColor, bottomColor, Shader.TileMode.CLAMP));
            }
            canvas.drawRoundRect(iconRect, dp(12), dp(12), iconBgPaint);

            if (needDivider) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                float divLeft = LocaleController.isRTL ? dp(16) : dp(68);
                float divRight = LocaleController.isRTL ? (w - dp(68)) : (w - dp(16));
                canvas.drawLine(divLeft, h - 1, divRight, h - 1, dividerPaint);
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
                int[] colors = (int[]) item.object;
                ((SubCategoryCardCell) view).bind(colors[0], colors[1], item.iconResId, item.text, item.subtext, item.textValue, divider);
            }

            public static UItem of(int id, IconBackgroundColors colors, int iconResId, CharSequence title, CharSequence subtitle, CharSequence badge) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.iconResId = iconResId;
                item.text = title;
                item.subtext = subtitle;
                item.textValue = badge;
                item.object = new int[]{colors.top, colors.bottom};
                return item;
            }
        }
    }

    // =============================================================================================
    // 2. IconSwitchCell
    // Polished toggle cell with soft tinted icon badge, title, optional subtitle, and Switch.
    // =============================================================================================
    public static class IconSwitchCell extends FrameLayout {

        private final Theme.ResourcesProvider resourcesProvider;
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final Switch checkBox;
        private final Paint iconBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF iconRect = new RectF();
        private boolean needDivider;

        public IconSwitchCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);

            iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.CENTER);
            addView(iconView, LayoutHelper.createFrame(34, 34,
                    (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL,
                    16, 0, 16, 0));

            LinearLayout textCol = new LinearLayout(context);
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            addView(textCol, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL,
                    LocaleController.isRTL ? 68 : 64, 8, LocaleController.isRTL ? 64 : 68, 8));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f);
            titleView.setTypeface(AndroidUtilities.bold());
            titleView.setSingleLine(true);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            textCol.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            subtitleView = new TextView(context);
            subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
            subtitleView.setMaxLines(2);
            subtitleView.setEllipsize(TextUtils.TruncateAt.END);
            textCol.addView(subtitleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));

            checkBox = new Switch(context, resourcesProvider);
            checkBox.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
            addView(checkBox, LayoutHelper.createFrame(37, 20,
                    (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL,
                    18, 0, 18, 0));
        }

        public void bind(int iconResId, CharSequence title, CharSequence subtitle, boolean checked, boolean divider) {
            this.needDivider = divider;
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            int grayIcon = Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon, resourcesProvider);
            int iconTint = checked ? accent : grayIcon;

            iconView.setImageResource(iconResId);
            iconView.setColorFilter(new PorterDuffColorFilter(iconTint, PorterDuff.Mode.SRC_IN));
            iconBgPaint.setColor(ColorUtils.setAlphaComponent(iconTint, Theme.isCurrentThemeDark() ? (checked ? 42 : 24) : (checked ? 28 : 18)));

            titleView.setText(title);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));

            if (!TextUtils.isEmpty(subtitle)) {
                subtitleView.setVisibility(VISIBLE);
                subtitleView.setText(subtitle);
                subtitleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            } else {
                subtitleView.setVisibility(GONE);
            }

            checkBox.setChecked(checked, false);
            invalidate();
        }

        public void setChecked(boolean checked) {
            checkBox.setChecked(checked, true);
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            int grayIcon = Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon, resourcesProvider);
            int iconTint = checked ? accent : grayIcon;
            iconView.setColorFilter(new PorterDuffColorFilter(iconTint, PorterDuff.Mode.SRC_IN));
            iconBgPaint.setColor(ColorUtils.setAlphaComponent(iconTint, Theme.isCurrentThemeDark() ? (checked ? 42 : 24) : (checked ? 28 : 18)));
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            boolean hasSub = subtitleView.getVisibility() == VISIBLE;
            int h = dp(hasSub ? 64 : 52) + (needDivider ? 1 : 0);
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
            );
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int h = getMeasuredHeight();
            int w = getMeasuredWidth();
            float cy = h / 2f;
            float size = dp(34);
            float left = LocaleController.isRTL ? (w - dp(16) - size) : dp(16);
            float top = cy - size / 2f;
            iconRect.set(left, top, left + size, top + size);
            canvas.drawRoundRect(iconRect, dp(10), dp(10), iconBgPaint);

            if (needDivider) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                float divLeft = LocaleController.isRTL ? dp(16) : dp(64);
                float divRight = LocaleController.isRTL ? (w - dp(64)) : (w - dp(16));
                canvas.drawLine(divLeft, h - 1, divRight, h - 1, dividerPaint);
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
                ((IconSwitchCell) view).bind(item.iconResId, item.text, item.subtext, item.checked, divider);
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
    // 3. SegmentedPillCell (Opexgram lc/j3.java & lc/i3.java)
    // Compact single-row or titled segmented pill selector with spring width & checkmark animation.
    // =============================================================================================
    public static class SegmentedPillCell extends FrameLayout {

        private final Theme.ResourcesProvider resourcesProvider;
        private final TextView titleView;
        private final LinearLayout pillsContainer;
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean needDivider;
        private int selectedIndex = -1;
        private String[] currentLabels;
        private Utilities.Callback<Integer> onSelectCallback;
        private final PillButton[] pillButtons = new PillButton[4];

        public SegmentedPillCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            titleView.setTypeface(AndroidUtilities.bold());
            titleView.setSingleLine(true);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            titleView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,
                    (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 20, 12, 20, 0));

            pillsContainer = new LinearLayout(context);
            pillsContainer.setOrientation(LinearLayout.HORIZONTAL);
            pillsContainer.setGravity(Gravity.CENTER_VERTICAL);
            addView(pillsContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 40,
                    Gravity.TOP, 16, 40, 16, 12));

            for (int i = 0; i < pillButtons.length; i++) {
                final int index = i;
                PillButton btn = new PillButton(context, resourcesProvider);
                btn.setOnClickListener(v -> {
                    if (selectedIndex == index) {
                        return;
                    }
                    AndroidUtilities.vibrate(v);
                    setSelectedIndex(index, true);
                    if (onSelectCallback != null) {
                        onSelectCallback.run(index);
                    }
                });
                pillButtons[i] = btn;
                pillsContainer.addView(btn);
            }
        }

        public void bind(CharSequence title, String[] labels, int selected, Utilities.Callback<Integer> callback, boolean divider) {
            this.needDivider = divider;
            this.onSelectCallback = callback;
            this.currentLabels = labels;

            boolean hasTitle = !TextUtils.isEmpty(title);
            titleView.setVisibility(hasTitle ? VISIBLE : GONE);
            if (hasTitle) {
                titleView.setText(title);
                titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
            }
            LayoutParams lp = (LayoutParams) pillsContainer.getLayoutParams();
            lp.topMargin = dp(hasTitle ? 38 : 10);
            lp.bottomMargin = dp(10);
            pillsContainer.setLayoutParams(lp);

            int count = labels != null ? Math.min(labels.length, pillButtons.length) : 0;
            for (int i = 0; i < pillButtons.length; i++) {
                PillButton btn = pillButtons[i];
                if (i < count) {
                    btn.setVisibility(VISIBLE);
                    btn.setText(labels[i]);
                    LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(0, dp(36), 1.0f);
                    if (i > 0) {
                        btnLp.leftMargin = dp(6);
                    }
                    btn.setLayoutParams(btnLp);
                } else {
                    btn.setVisibility(GONE);
                }
            }
            setSelectedIndex(selected, false);
            invalidate();
        }

        public void setSelectedIndex(int selected, boolean animated) {
            this.selectedIndex = selected;
            int count = currentLabels != null ? Math.min(currentLabels.length, pillButtons.length) : 0;
            for (int i = 0; i < count; i++) {
                boolean isSel = (i == selected);
                pillButtons[i].setSelectedState(isSel, animated);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) pillButtons[i].getLayoutParams();
                float targetWeight = isSel ? 1.18f : 0.94f;
                if (Math.abs(lp.weight - targetWeight) > 0.01f) {
                    lp.weight = targetWeight;
                    pillButtons[i].setLayoutParams(lp);
                }
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            boolean hasTitle = titleView.getVisibility() == VISIBLE;
            int h = dp(hasTitle ? 86 : 56) + (needDivider ? 1 : 0);
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
            );
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (needDivider) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                canvas.drawLine(dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - dp(20), getMeasuredHeight() - 1, dividerPaint);
            }
        }

        private static class PillButton extends View {
            private final Theme.ResourcesProvider resourcesProvider;
            private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            private final RectF rect = new RectF();
            private final Path checkPath = new Path();
            private String text = "";
            private boolean selected;
            private float progress;
            private ValueAnimator animator;

            PillButton(Context context, Theme.ResourcesProvider resourcesProvider) {
                super(context);
                this.resourcesProvider = resourcesProvider;
                textPaint.setTextSize(dp(12.5f));
                textPaint.setTypeface(AndroidUtilities.bold());
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(dpf2(1f));
                checkPaint.setStyle(Paint.Style.STROKE);
                checkPaint.setStrokeCap(Paint.Cap.ROUND);
                checkPaint.setStrokeJoin(Paint.Join.ROUND);
                checkPaint.setStrokeWidth(dpf2(1.8f));
            }

            void setText(String label) {
                this.text = label != null ? label : "";
                invalidate();
            }

            void setSelectedState(boolean sel, boolean animated) {
                if (this.selected == sel && animated) {
                    return;
                }
                this.selected = sel;
                if (animator != null) {
                    animator.cancel();
                    animator = null;
                }
                float target = sel ? 1f : 0f;
                if (!animated) {
                    progress = target;
                    invalidate();
                    return;
                }
                animator = ValueAnimator.ofFloat(progress, target);
                animator.setDuration(220);
                animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                animator.addUpdateListener(a -> {
                    progress = (float) a.getAnimatedValue();
                    invalidate();
                });
                animator.start();
            }

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) return;

                int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
                int unselectedBg = ColorUtils.setAlphaComponent(
                        Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider),
                        Theme.isCurrentThemeDark() ? 28 : 22
                );
                int selectedBg = ColorUtils.setAlphaComponent(accent, Theme.isCurrentThemeDark() ? 56 : 38);
                int bgColor = ColorUtils.blendARGB(unselectedBg, selectedBg, progress);

                int unselectedText = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider);
                int textColor = ColorUtils.blendARGB(unselectedText, accent, progress);

                float pad = dpf2(1f);
                rect.set(pad, pad, w - pad, h - pad);
                float radius = AndroidUtilities.lerp(dpf2(11f), h / 2f, progress);

                bgPaint.setColor(bgColor);
                canvas.drawRoundRect(rect, radius, radius, bgPaint);

                if (progress > 0.01f) {
                    borderPaint.setColor(ColorUtils.setAlphaComponent(accent, (int) (130 * progress)));
                    canvas.drawRoundRect(rect, radius, radius, borderPaint);
                }

                float iconSpace = dp(16) * progress;
                float maxTextW = Math.max(dp(20), w - dp(16) - iconSpace);
                CharSequence ellipsized = TextUtils.ellipsize(text, textPaint, maxTextW, TextUtils.TruncateAt.END);
                String drawStr = ellipsized.toString();
                float textW = textPaint.measureText(drawStr);
                float totalW = textW + iconSpace;
                float startX = (w - totalW) / 2f;

                if (progress > 0.05f) {
                    float cx = startX + dp(6) * progress;
                    float cy = h / 2f;
                    canvas.save();
                    canvas.scale(progress, progress, cx, cy);
                    checkPaint.setColor(textColor);
                    checkPath.rewind();
                    checkPath.moveTo(cx - dp(4.2f), cy + dp(0.2f));
                    checkPath.lineTo(cx - dp(1.2f), cy + dp(3.2f));
                    checkPath.lineTo(cx + dp(4.5f), cy - dp(2.8f));
                    canvas.drawPath(checkPath, checkPaint);
                    canvas.restore();
                }

                textPaint.setColor(textColor);
                Paint.FontMetrics fm = textPaint.getFontMetrics();
                float textY = h / 2f - (fm.ascent + fm.descent) / 2f;
                canvas.drawText(drawStr, startX + iconSpace, textY, textPaint);
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

            public static UItem of(int id, CharSequence title, String[] choices, int selectedIndex, Utilities.Callback<Integer> onSelected) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.text = title;
                item.texts = choices;
                item.intValue = selectedIndex;
                item.intCallback = onSelected;
                return item;
            }
        }
    }

    // =============================================================================================
    // 4. InlineSliderCell (Opexgram lc/z5.java & lc/b6.java)
    // Compact inline SeekBarView with title, live pill badge, and 1-tap default reset button.
    // =============================================================================================
    public static class InlineSliderCell extends FrameLayout {

        private final Theme.ResourcesProvider resourcesProvider;
        private final TextView titleView;
        private final TextView valueBadgeView;
        private final TextView resetBadgeView;
        private final SeekBarView seekBarView;
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private int minValue = 0;
        private int maxValue = 100;
        private int currentValue = 50;
        private int defaultValue = 50;
        private String suffix = "";
        private boolean needDivider;
        private Utilities.Callback<Integer> onChangeCallback;

        public InlineSliderCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            titleView.setTypeface(AndroidUtilities.bold());
            titleView.setSingleLine(true);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                    (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 20, 12, 110, 0));

            LinearLayout rightRow = new LinearLayout(context);
            rightRow.setOrientation(LinearLayout.HORIZONTAL);
            rightRow.setGravity(Gravity.CENTER_VERTICAL);
            addView(rightRow, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 26,
                    (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, 20, 10, 20, 0));

            resetBadgeView = new TextView(context);
            resetBadgeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
            resetBadgeView.setTypeface(AndroidUtilities.bold());
            resetBadgeView.setText("↺");
            resetBadgeView.setGravity(Gravity.CENTER);
            resetBadgeView.setPadding(dp(7), dp(2), dp(7), dp(2));
            resetBadgeView.setOnClickListener(v -> {
                if (currentValue != defaultValue) {
                    AndroidUtilities.vibrate(v);
                    applyValue(defaultValue, true);
                }
            });
            rightRow.addView(resetBadgeView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 22, 0, 0, 6, 0));

            valueBadgeView = new TextView(context);
            valueBadgeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            valueBadgeView.setTypeface(AndroidUtilities.bold());
            valueBadgeView.setGravity(Gravity.CENTER);
            valueBadgeView.setPadding(dp(9), dp(2), dp(9), dp(2));
            rightRow.addView(valueBadgeView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 22));

            seekBarView = new SeekBarView(context, true, resourcesProvider);
            seekBarView.setReportChanges(true);
            seekBarView.setDelegate(new SeekBarView.SeekBarViewDelegate() {
                @Override
                public void onSeekBarDrag(boolean stop, float progress) {
                    int val = Math.round(minValue + progress * (maxValue - minValue));
                    val = Utilities.clamp(val, maxValue, minValue);
                    if (val != currentValue) {
                        currentValue = val;
                        updateBadges();
                        if (onChangeCallback != null) {
                            onChangeCallback.run(currentValue);
                        }
                    }
                }

                @Override
                public void onSeekBarPressed(boolean pressed) {}
            });
            addView(seekBarView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.TOP, 6, 36, 6, 0));
        }

        private void applyValue(int newVal, boolean notify) {
            currentValue = Utilities.clamp(newVal, maxValue, minValue);
            float progress = maxValue > minValue ? (currentValue - minValue) / (float) (maxValue - minValue) : 0f;
            seekBarView.setProgress(progress, true);
            updateBadges();
            if (notify && onChangeCallback != null) {
                onChangeCallback.run(currentValue);
            }
        }

        private void updateBadges() {
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            valueBadgeView.setText(currentValue + suffix);
            valueBadgeView.setTextColor(accent);
            valueBadgeView.setBackground(Theme.createRoundRectDrawable(dp(11),
                    ColorUtils.setAlphaComponent(accent, Theme.isCurrentThemeDark() ? 45 : 30)));

            boolean modified = currentValue != defaultValue;
            resetBadgeView.setVisibility(modified ? VISIBLE : GONE);
            if (modified) {
                int gray = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider);
                resetBadgeView.setTextColor(gray);
                resetBadgeView.setBackground(Theme.createRoundRectDrawable(dp(11),
                        ColorUtils.setAlphaComponent(gray, 28)));
            }
        }

        public void bind(CharSequence title, int min, int max, int current, int defVal, String unitSuffix, Utilities.Callback<Integer> callback, boolean divider) {
            this.minValue = min;
            this.maxValue = max;
            this.defaultValue = defVal;
            this.currentValue = Utilities.clamp(current, max, min);
            this.suffix = unitSuffix != null ? unitSuffix : "";
            this.onChangeCallback = callback;
            this.needDivider = divider;

            titleView.setText(title);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
            float progress = max > min ? (currentValue - min) / (float) (max - min) : 0f;
            seekBarView.setProgress(progress, false);
            updateBadges();
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(76) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY)
            );
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (needDivider) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                canvas.drawLine(dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - dp(20), getMeasuredHeight() - 1, dividerPaint);
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
                int[] meta = (int[]) item.object; // [min, max, defVal]
                String unit = item.textValue != null ? item.textValue.toString() : "";
                ((InlineSliderCell) view).bind(item.text, meta[0], meta[1], item.intValue, meta[2], unit, item.intCallback, divider);
            }

            @Override
            public boolean isClickable() {
                return false;
            }

            public static UItem of(int id, CharSequence title, int min, int max, int current, int defaultValue, String suffix, Utilities.Callback<Integer> onChange) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.text = title;
                item.intValue = current;
                item.textValue = suffix;
                item.object = new int[]{min, max, defaultValue};
                item.intCallback = onChange;
                return item;
            }
        }
    }

    // =============================================================================================
    // 5. RealChatMessagesPreviewCell
    // Embeds REAL Telegram ChatMessageCell instances over the user's actual chat wallpaper
    // (`Theme.getCachedWallpaperNonBlocking()`), rendering through the real `MessageDrawable`.
    // =============================================================================================
    public static class RealChatMessagesPreviewCell extends LinearLayout {

        public static final int MODE_BUBBLES_AND_GLASS = 0;
        public static final int MODE_STICKER_AND_FOCUS = 1;

        private final Theme.ResourcesProvider resourcesProvider;
        private final ChatMessageCell[] cells = new ChatMessageCell[2];
        private final MessageObject[] messageObjects = new MessageObject[2];
        private BackgroundGradientDrawable.Disposable backgroundGradientDisposable;
        private ColorDrawable fallbackBgDrawable;
        private int mode = -1;

        @SuppressLint("ClickableViewAccessibility")
        public RealChatMessagesPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);
            setOrientation(LinearLayout.VERTICAL);
            setPadding(0, dp(10), 0, dp(10));

            int currentAccount = UserConfig.selectedAccount;
            for (int a = 0; a < cells.length; a++) {
                cells[a] = new ChatMessageCell(context, currentAccount, false, null, resourcesProvider) {
                    @Override
                    public boolean onTouchEvent(MotionEvent event) {
                        return false;
                    }
                };
                cells[a].setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
                    @Override
                    public boolean canPerformActions() {
                        return false;
                    }
                });
                cells[a].isChat = false;
                cells[a].setFullyDraw(true);
                addView(cells[a], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            }
            buildMessages(MODE_BUBBLES_AND_GLASS);
        }

        private void buildMessages(int targetMode) {
            this.mode = targetMode;
            int currentAccount = UserConfig.selectedAccount;
            int now = (int) (System.currentTimeMillis() / 1000);
            long selfId = UserConfig.getInstance(currentAccount).getClientUserId();

            // Reply target message
            TLRPC.Message replyMsg = new TLRPC.TL_message();
            replyMsg.message = targetMode == MODE_STICKER_AND_FOCUS
                    ? "Mantén pulsada una burbuja en cualquier chat ✨"
                    : "Burbujas Liquid Glass con borde especular y redondeo";
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

            // 1. Incoming message with reply & forwarded info
            TLRPC.Message inMsg = new TLRPC.TL_message();
            inMsg.message = targetMode == MODE_STICKER_AND_FOCUS
                    ? "Menú Focus real + Stickers y GIFs escalables en vivo"
                    : "¿Qué tal se ven las burbujas reales de Lagram sobre tu fondo?";
            inMsg.date = now - 60;
            inMsg.dialog_id = 1;
            inMsg.flags = 257 + 8 + 4;
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

            // 2. Outgoing message
            TLRPC.Message outMsg = new TLRPC.TL_message();
            outMsg.message = targetMode == MODE_STICKER_AND_FOCUS
                    ? ("Estilo Focus: " + getFocusStyleName() + " • Sticker " + LagramConfig.stickerSize + " • GIF " + LagramConfig.gifSize)
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

        public void setMode(int newMode) {
            buildMessages(newMode);
            refreshPreview();
        }

        public void refreshPreview() {
            buildMessages(this.mode < 0 ? MODE_BUBBLES_AND_GLASS : this.mode);
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
                wallpaper.setAlpha(255);
                if (wallpaper instanceof ColorDrawable || wallpaper instanceof GradientDrawable || wallpaper instanceof MotionBackgroundDrawable) {
                    wallpaper.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
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
                    cell.currentBackgroundDrawable.draw(canvas);
                    canvas.restore();
                }
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
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
                ((RealChatMessagesPreviewCell) view).setMode(item.intValue);
            }

            @Override
            public boolean isClickable() {
                return false;
            }

            public static UItem of(int id, int mode) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.intValue = mode;
                return item;
            }
        }
    }

    // =============================================================================================
    // 6. RealFolderTabsPreviewCell
    // Embeds the REAL Telegram FilterTabsView inside the settings card so folder tabs look &
    // animate identically to the main chat list header.
    // =============================================================================================
    public static class RealFolderTabsPreviewCell extends FrameLayout {

        private final Theme.ResourcesProvider resourcesProvider;
        private final FilterTabsView filterTabsView;
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean needDivider;

        private static final int[] SAMPLE_COUNTS = new int[]{14, 3, 28, 7};

        public RealFolderTabsPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);

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
                public boolean didSelectTab(FilterTabsView.TabView tabView, boolean selected) {
                    return false;
                }

                @Override
                public boolean isTabMenuVisible() {
                    return false;
                }

                @Override
                public void onDeletePressed(int id) {}

                @Override
                public void onPageReorder(int fromId, int toId) {}

                @Override
                public boolean canPerformActions() {
                    return true;
                }
            });
            addView(filterTabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.CENTER_VERTICAL, 4, 4, 4, 4));
            rebuildTabs();
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
            filterTabsView.updateTabsWidths();
            filterTabsView.invalidate();
            if (filterTabsView.getListView() != null) {
                filterTabsView.getListView().invalidateViews();
            }
            invalidate();
        }

        public void setNeedDivider(boolean divider) {
            this.needDivider = divider;
            rebuildTabs();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(56) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY)
            );
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (needDivider) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                canvas.drawLine(dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - dp(20), getMeasuredHeight() - 1, dividerPaint);
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
                ((RealFolderTabsPreviewCell) view).setNeedDivider(divider);
            }

            @Override
            public boolean isClickable() {
                return false;
            }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                return item;
            }
        }
    }
}
