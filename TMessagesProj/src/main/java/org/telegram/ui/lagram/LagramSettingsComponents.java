package org.telegram.ui.lagram;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Opexgram-inspired Material 3 Expressive Settings UI Components & Interactive Live Previews.
 */
public final class LagramSettingsComponents {

    private LagramSettingsComponents() {}

    // =============================================================================================
    // 1. SegmentedPillCell (Opexgram lc/j3.java & lc/i3.java)
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
                    try {
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                    } catch (Throwable ignore) {}
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
    // 2. InlineSliderCell (Opexgram lc/z5.java & lc/b6.java)
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
                    try {
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                    } catch (Throwable ignore) {}
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
    // 3. ChatAppearancePreviewCell (Opexgram lc/x1.java & lc/e4.java)
    // Live interactive preview card showing chat wallpaper, Liquid Glass incoming & outgoing bubbles,
    // specular border, corner radius preset, bubble tails, sticker/GIF size, seconds in time, & chat bar.
    // =============================================================================================
    public static class ChatAppearancePreviewCell extends View {

        public static final int MODE_BUBBLES_AND_GLASS = 0;
        public static final int MODE_STICKER_AND_FOCUS = 1;

        private final Theme.ResourcesProvider resourcesProvider;
        private final Paint cardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint patternDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint inBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint outBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glassSheenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glassBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint chatBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pillBadgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint msgPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint timePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint badgeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final RectF cardRect = new RectF();
        private final RectF bubbleRect = new RectF();
        private final RectF barRect = new RectF();
        private final Path tailPath = new Path();
        private int mode = MODE_BUBBLES_AND_GLASS;

        public ChatAppearancePreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            glassBorderPaint.setStyle(Paint.Style.STROKE);
            glassBorderPaint.setStrokeWidth(dpf2(1.1f));

            namePaint.setTextSize(dp(12));
            namePaint.setTypeface(AndroidUtilities.bold());

            msgPaint.setTextSize(dp(13.5f));

            timePaint.setTextSize(dp(10.5f));

            badgeTextPaint.setTextSize(dp(10.5f));
            badgeTextPaint.setTypeface(AndroidUtilities.bold());
        }

        public void setMode(int mode) {
            this.mode = mode;
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int h = dp(mode == MODE_STICKER_AND_FOCUS ? 184 : 176);
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
            );
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            boolean isDark = Theme.isCurrentThemeDark();
            float cardPadH = dp(14);
            float cardPadV = dp(10);
            cardRect.set(cardPadH, cardPadV, w - cardPadH, h - cardPadV);
            float cardRadius = dp(16);

            // Rich gradient wallpaper backdrop inside preview card
            int bgStart = isDark ? 0xFF131B2E : 0xFFD4E6F8;
            int bgEnd = isDark ? 0xFF1E293B : 0xFFE5F0DC;
            cardBgPaint.setShader(new LinearGradient(
                    cardRect.left, cardRect.top, cardRect.right, cardRect.bottom,
                    new int[]{bgStart, bgEnd}, null, Shader.TileMode.CLAMP
            ));
            canvas.drawRoundRect(cardRect, cardRadius, cardRadius, cardBgPaint);

            // Subtle ambient orbs to visibly demonstrate Liquid Glass translucency
            patternDotPaint.setColor(isDark ? 0x2838BDF8 : 0x353B82F6);
            canvas.drawCircle(cardRect.left + dp(75), cardRect.top + dp(52), dp(34), patternDotPaint);
            patternDotPaint.setColor(isDark ? 0x28A855F7 : 0x3010B981);
            canvas.drawCircle(cardRect.right - dp(80), cardRect.top + dp(94), dp(38), patternDotPaint);

            float cornerRad = dp(LagramConfig.getEffectiveBubbleRadiusDp());
            boolean glass = LagramConfig.glassMessages;
            int alphaPercent = glass ? Utilities.clamp(LagramConfig.glassMessagesAlpha, 100, 20) : 100;
            boolean glassBorder = glass && LagramConfig.glassMessagesBorder;
            boolean tails = LagramConfig.showBubbleTails;

            int rawInColor = Theme.getColor(Theme.key_chat_inBubble, resourcesProvider);
            int rawOutColor = Theme.getColor(Theme.key_chat_outBubble, resourcesProvider);
            int inAlpha = Math.round(Color.alpha(rawInColor) * (alphaPercent / 100f));
            int outAlpha = Math.round(Color.alpha(rawOutColor) * (alphaPercent / 100f));
            inBubblePaint.setColor(ColorUtils.setAlphaComponent(rawInColor, inAlpha));
            outBubblePaint.setColor(ColorUtils.setAlphaComponent(rawOutColor, outAlpha));

            SimpleDateFormat sdf = new SimpleDateFormat(LagramConfig.showSecondsInTime ? "HH:mm:ss" : "HH:mm", Locale.getDefault());
            String timeStr = sdf.format(new Date());

            // Top live status pill badge
            String badgeLabel;
            if (mode == MODE_STICKER_AND_FOCUS) {
                String styleStr = LagramConfig.messageFocusStyle == LagramConfig.FOCUS_STYLE_CAPSULE ? "Cápsula"
                        : (LagramConfig.messageFocusStyle == LagramConfig.FOCUS_STYLE_TILES ? "Cuadrícula" : "Lista");
                badgeLabel = "Focus: " + styleStr + "  •  Sticker " + LagramConfig.stickerSize + "  •  GIF " + LagramConfig.gifSize;
            } else {
                badgeLabel = (glass ? ("Liquid Glass " + alphaPercent + "%") : "Sólido")
                        + "  •  Radio " + LagramConfig.getEffectiveBubbleRadiusDp() + "dp"
                        + (tails ? "  •  Colas" : "");
            }
            float badgeW = badgeTextPaint.measureText(badgeLabel) + dp(18);
            RectF badgeRect = new RectF(cardRect.centerX() - badgeW / 2f, cardRect.top + dp(8),
                    cardRect.centerX() + badgeW / 2f, cardRect.top + dp(26));
            pillBadgePaint.setColor(isDark ? 0x66000000 : 0x55FFFFFF);
            canvas.drawRoundRect(badgeRect, dp(9), dp(9), pillBadgePaint);
            badgeTextPaint.setColor(isDark ? 0xFFE2E8F0 : 0xFF1E293B);
            canvas.drawText(badgeLabel, badgeRect.left + dp(9), badgeRect.centerY() + dp(3.8f), badgeTextPaint);

            // 1. Incoming Bubble (top-left)
            float inLeft = cardRect.left + dp(14);
            float inTop = cardRect.top + dp(34);
            float inWidth = Math.min(dp(230), cardRect.width() * 0.68f);
            float inHeight = dp(48);
            bubbleRect.set(inLeft, inTop, inLeft + inWidth, inTop + inHeight);
            float rIn = Math.min(cornerRad, inHeight / 2f);
            canvas.drawRoundRect(bubbleRect, rIn, rIn, inBubblePaint);

            if (tails) {
                tailPath.rewind();
                tailPath.moveTo(bubbleRect.left + dp(4), bubbleRect.bottom - dp(8));
                tailPath.quadTo(bubbleRect.left - dp(5), bubbleRect.bottom, bubbleRect.left - dp(6), bubbleRect.bottom);
                tailPath.lineTo(bubbleRect.left + dp(8), bubbleRect.bottom);
                tailPath.close();
                canvas.drawPath(tailPath, inBubblePaint);
            }

            if (glass) {
                drawGlassOverlay(canvas, bubbleRect, rIn, isDark, glassBorder);
            }

            namePaint.setColor(Theme.getColor(Theme.key_chat_inMenu, resourcesProvider));
            String senderText = mode == MODE_STICKER_AND_FOCUS ? "Lagram Focus ✨" : "Lagram Liquid Glass";
            canvas.drawText(senderText, bubbleRect.left + dp(11), bubbleRect.top + dp(17), namePaint);

            msgPaint.setColor(Theme.getColor(Theme.key_chat_messageTextIn, resourcesProvider));
            String inMsg = mode == MODE_STICKER_AND_FOCUS
                    ? "Mantén pulsado para menú " + (LagramConfig.messageFocusBlur ? "Blur" : "Focus")
                    : "Burbujas traslúcidas y bordes suaves";
            CharSequence inEllip = TextUtils.ellipsize(inMsg, msgPaint, bubbleRect.width() - dp(22), TextUtils.TruncateAt.END);
            canvas.drawText(inEllip, 0, inEllip.length(), bubbleRect.left + dp(11), bubbleRect.top + dp(36), msgPaint);

            // 2. Outgoing Bubble (bottom-right)
            float scaleFactor = mode == MODE_STICKER_AND_FOCUS ? (0.72f + (LagramConfig.stickerSize / 20f) * 0.35f) : 1.0f;
            float outWidth = Math.min(dp(220) * scaleFactor, cardRect.width() * 0.68f);
            float outHeight = dp(38);
            float outRight = cardRect.right - dp(14);
            float outTop = inTop + inHeight + dp(8);
            bubbleRect.set(outRight - outWidth, outTop, outRight, outTop + outHeight);
            float rOut = Math.min(cornerRad, outHeight / 2f);
            canvas.drawRoundRect(bubbleRect, rOut, rOut, outBubblePaint);

            if (tails) {
                tailPath.rewind();
                tailPath.moveTo(bubbleRect.right - dp(4), bubbleRect.bottom - dp(8));
                tailPath.quadTo(bubbleRect.right + dp(5), bubbleRect.bottom, bubbleRect.right + dp(6), bubbleRect.bottom);
                tailPath.lineTo(bubbleRect.right - dp(8), bubbleRect.bottom);
                tailPath.close();
                canvas.drawPath(tailPath, outBubblePaint);
            }

            if (glass) {
                drawGlassOverlay(canvas, bubbleRect, rOut, isDark, glassBorder);
            }

            msgPaint.setColor(Theme.getColor(Theme.key_chat_messageTextOut, resourcesProvider));
            String outMsg = mode == MODE_STICKER_AND_FOCUS
                    ? ("Tamaño Sticker: " + LagramConfig.stickerSize + " / GIF: " + LagramConfig.gifSize)
                    : "¡Estilo Opexgram optimizado!";
            float timeW = timePaint.measureText(timeStr);
            CharSequence outEllip = TextUtils.ellipsize(outMsg, msgPaint, Math.max(dp(40), bubbleRect.width() - timeW - dp(28)), TextUtils.TruncateAt.END);
            canvas.drawText(outEllip, 0, outEllip.length(), bubbleRect.left + dp(11), bubbleRect.centerY() + dp(4.5f), msgPaint);

            timePaint.setColor(Theme.getColor(Theme.key_chat_outSentCheck, resourcesProvider));
            canvas.drawText(timeStr, bubbleRect.right - timeW - dp(10), bubbleRect.bottom - dp(7), timePaint);

            // 3. Bottom Chat Input Bar Preview
            float barTop = cardRect.bottom - dp(28);
            barRect.set(cardRect.left + dp(12), barTop, cardRect.right - dp(12), cardRect.bottom - dp(6));
            int barColor = ColorUtils.setAlphaComponent(rawInColor, glass ? Math.max(150, inAlpha) : 235);
            chatBarPaint.setColor(barColor);
            canvas.drawRoundRect(barRect, barRect.height() / 2f, barRect.height() / 2f, chatBarPaint);
            if (glass && glassBorder) {
                glassBorderPaint.setColor(isDark ? 0x40FFFFFF : 0x70FFFFFF);
                canvas.drawRoundRect(barRect, barRect.height() / 2f, barRect.height() / 2f, glassBorderPaint);
            }
            timePaint.setColor(Theme.getColor(Theme.key_chat_messagePanelHint, resourcesProvider));
            canvas.drawText(LocaleController.getString(R.string.Message), barRect.left + dp(12), barRect.centerY() + dp(3.5f), timePaint);
        }

        private void drawGlassOverlay(Canvas canvas, RectF rect, float radius, boolean isDark, boolean drawBorder) {
            glassSheenPaint.setShader(new LinearGradient(
                    rect.left, rect.top, rect.left, rect.bottom,
                    new int[]{isDark ? 0x26FFFFFF : 0x44FFFFFF, 0x00FFFFFF},
                    null, Shader.TileMode.CLAMP
            ));
            canvas.drawRoundRect(rect, radius, radius, glassSheenPaint);

            if (drawBorder) {
                glassBorderPaint.setShader(new LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        new int[]{isDark ? 0x66FFFFFF : 0x99FFFFFF, isDark ? 0x1AFFFFFF : 0x28FFFFFF},
                        null, Shader.TileMode.CLAMP
                ));
                canvas.drawRoundRect(rect, radius, radius, glassBorderPaint);
            }
        }

        public static class Factory extends UItem.UItemFactory<ChatAppearancePreviewCell> {
            static { setup(new Factory()); }

            @Override
            public ChatAppearancePreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new ChatAppearancePreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((ChatAppearancePreviewCell) view).setMode(item.intValue);
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
    // 4. FolderTabsPreviewCell (Opexgram lc/t2.java)
    // Live folder tabs preview showing Icon+Text / Icon Only / Text Only and Unread Counters.
    // =============================================================================================
    public static class FolderTabsPreviewCell extends View {

        private final Theme.ResourcesProvider resourcesProvider;
        private final Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint activePillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint counterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint tabTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint counterTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final RectF cardRect = new RectF();
        private final RectF pillRect = new RectF();
        private final RectF badgeRect = new RectF();
        private boolean needDivider;

        private static final String[] TAB_NAMES = new String[]{"Todos", "Personal", "Grupos", "Canales"};
        private static final String[] TAB_COUNTS = new String[]{"12", "3", "28", "5"};
        private static final int[] TAB_ICONS = new int[]{
                R.drawable.msg_chats_remove,
                R.drawable.msg_contacts,
                R.drawable.msg_groups,
                R.drawable.msg_channel
        };

        public FolderTabsPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            tabTextPaint.setTextSize(dp(13));
            tabTextPaint.setTypeface(AndroidUtilities.bold());
            counterTextPaint.setTextSize(dp(10.5f));
            counterTextPaint.setTypeface(AndroidUtilities.bold());
        }

        public void setNeedDivider(boolean divider) {
            this.needDivider = divider;
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
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            int grayText = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider);
            boolean isDark = Theme.isCurrentThemeDark();

            cardRect.set(dp(14), dp(8), w - dp(14), h - dp(8));
            cardPaint.setColor(ColorUtils.setAlphaComponent(grayText, isDark ? 24 : 16));
            canvas.drawRoundRect(cardRect, dp(14), dp(14), cardPaint);

            int style = LagramConfig.folderTabsStyle;
            boolean showIcons = style == LagramConfig.FOLDER_TABS_ICON_AND_TEXT || style == LagramConfig.FOLDER_TABS_ICON_ONLY;
            boolean showText = style == LagramConfig.FOLDER_TABS_ICON_AND_TEXT || style == LagramConfig.FOLDER_TABS_TEXT_ONLY;
            boolean showCounters = LagramConfig.showFolderCounters;
            boolean hideAllChats = LagramConfig.hideAllChatsFolder;

            int startIdx = hideAllChats ? 1 : 0;
            int tabCount = TAB_NAMES.length - startIdx;
            float slotW = (cardRect.width() - dp(12)) / tabCount;

            for (int i = startIdx; i < TAB_NAMES.length; i++) {
                int slot = i - startIdx;
                boolean isSelected = (slot == 0);
                float slotLeft = cardRect.left + dp(6) + slot * slotW;
                float slotRight = slotLeft + slotW;
                float cy = cardRect.centerY();

                if (isSelected) {
                    pillRect.set(slotLeft + dp(2), cardRect.top + dp(5), slotRight - dp(2), cardRect.bottom - dp(5));
                    activePillPaint.setColor(ColorUtils.setAlphaComponent(accent, isDark ? 48 : 32));
                    canvas.drawRoundRect(pillRect, pillRect.height() / 2f, pillRect.height() / 2f, activePillPaint);
                }

                int fgColor = isSelected ? accent : grayText;
                tabTextPaint.setColor(fgColor);

                float iconW = showIcons ? dp(16) : 0;
                float textW = showText ? tabTextPaint.measureText(TAB_NAMES[i]) : 0;
                float countW = showCounters ? (counterTextPaint.measureText(TAB_COUNTS[i]) + dp(10)) : 0;
                float gap1 = (showIcons && showText) ? dp(4) : 0;
                float gap2 = (showCounters && (showIcons || showText)) ? dp(4) : 0;
                float totalW = iconW + gap1 + textW + gap2 + countW;

                float curX = (slotLeft + slotRight - totalW) / 2f;

                if (showIcons) {
                    Drawable d = ContextCompat.getDrawable(getContext(), TAB_ICONS[i]);
                    if (d != null) {
                        d = d.mutate();
                        d.setTint(fgColor);
                        int iy = (int) (cy - dp(8));
                        d.setBounds((int) curX, iy, (int) (curX + dp(16)), iy + dp(16));
                        d.draw(canvas);
                    }
                    curX += iconW + gap1;
                }

                if (showText) {
                    Paint.FontMetrics fm = tabTextPaint.getFontMetrics();
                    float ty = cy - (fm.ascent + fm.descent) / 2f;
                    canvas.drawText(TAB_NAMES[i], curX, ty, tabTextPaint);
                    curX += textW + gap2;
                }

                if (showCounters) {
                    float bh = dp(15);
                    badgeRect.set(curX, cy - bh / 2f, curX + countW, cy + bh / 2f);
                    counterPaint.setColor(isSelected ? accent : ColorUtils.setAlphaComponent(grayText, 60));
                    canvas.drawRoundRect(badgeRect, bh / 2f, bh / 2f, counterPaint);
                    counterTextPaint.setColor(isSelected ? Color.WHITE : fgColor);
                    Paint.FontMetrics fm = counterTextPaint.getFontMetrics();
                    float cty = cy - (fm.ascent + fm.descent) / 2f;
                    canvas.drawText(TAB_COUNTS[i], badgeRect.left + dp(5), cty, counterTextPaint);
                }
            }

            if (needDivider) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                canvas.drawLine(dp(20), h - 1, w - dp(20), h - 1, dividerPaint);
            }
        }

        public static class Factory extends UItem.UItemFactory<FolderTabsPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public FolderTabsPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new FolderTabsPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((FolderTabsPreviewCell) view).setNeedDivider(divider);
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
