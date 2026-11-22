package org.telegram.ui.lagram;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.util.Arrays;

/**
 * Pixel-accurate Opexgram Material Design 3 Expressive Settings Widgets & Live Previews:
 * - OpexSwitchCell: M3 Expressive pill switch row (zero dividers when card-per-row is active)
 * - OpexSliderCell: Thick 16dp M3 pill slider with cutout vertical pill thumb & stop dot
 * - OpexCheckPillsCell: Rounded M3 toggle pills with checkmark (single-select & multi-select)
 * - OpexMaterialHeroPreviewCell: Miniature dual-screen Material Design 3 live preview
 * - OpexRoundingPreviewCell: Miniature Unified Rounding & Avatar Shape live preview
 * - OpexVisualPickerCell: 3-card visual picker for Chat Box & Folder Bar styles
 * - OpexMessageMenuPreviewCell: Live preview of List / Tiles / Capsule message menu
 * - OpexTextAnimPreviewCell: Live looping animated typing bar preview
 */
public final class OpexgramStyleWidgets {

    private OpexgramStyleWidgets() {}

    private static long packRange(int min, int max, int defVal) {
        return (((long) (min & 0xFFFF)) << 32)
                | (((long) (max & 0xFFFF)) << 16)
                | (defVal & 0xFFFFL);
    }

    private static int unpackMin(long p) { return (short) ((p >>> 32) & 0xFFFF); }
    private static int unpackMax(long p) { return (short) ((p >>> 16) & 0xFFFF); }
    private static int unpackDef(long p) { return (short) (p & 0xFFFF); }

    // =============================================================================================
    // 1. M3 Expressive Switch View + OpexSwitchCell
    // Exact Opexgram Material 3 pill toggle: 50x28dp track, big circular thumb, crisp outline off.
    // =============================================================================================
    public static class M3SwitchView extends View {
        private final Theme.ResourcesProvider resourcesProvider;
        private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF trackRect = new RectF();
        private boolean checked;
        private float progress;
        private ValueAnimator animator;

        public M3SwitchView(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(dpf2(1.8f));
        }

        public void setChecked(boolean value, boolean animated) {
            if (checked == value && (animated || progress == (value ? 1f : 0f))) {
                return;
            }
            checked = value;
            float target = checked ? 1f : 0f;
            if (animator != null) {
                animator.cancel();
            }
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
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(dp(50), dp(28));
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            boolean isDark = Theme.isCurrentThemeDark();
            boolean useM3 = LagramConfig.materialControls;
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            int offTrack = isDark ? 0xFF2B2D30 : 0xFFE3E6EA;
            int offStroke = isDark ? 0xFF7B8086 : 0xFF8B929B;
            int offThumb = isDark ? 0xFF9EA4AC : 0xFF757B84;
            int onThumb = isDark ? 0xFF0E1B10 : Color.WHITE;

            float r = h * 0.5f;
            trackRect.set(dpf2(1f), dpf2(1f), w - dpf2(1f), h - dpf2(1f));
            trackPaint.setColor(ColorUtils.blendARGB(offTrack, accent, progress));
            canvas.drawRoundRect(trackRect, r, r, trackPaint);

            if (useM3 && progress < 1f) {
                strokePaint.setColor(ColorUtils.setAlphaComponent(offStroke, Math.round(255 * (1f - progress))));
                canvas.drawRoundRect(trackRect, r, r, strokePaint);
            }

            float thumbR = useM3
                    ? AndroidUtilities.lerp(dpf2(7.5f), dpf2(11.0f), progress)
                    : dpf2(10.0f);
            float cxOff = dpf2(14f);
            float cxOn = w - dpf2(14f);
            float cx = AndroidUtilities.lerp(cxOff, cxOn, progress);
            float cy = h * 0.5f;

            thumbPaint.setColor(ColorUtils.blendARGB(offThumb, onThumb, progress));
            canvas.drawCircle(cx, cy, thumbR, thumbPaint);
        }
    }

    public static class OpexSwitchCell extends LinearLayout {
        private final Theme.ResourcesProvider resourcesProvider;
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final M3SwitchView switchView;
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean needDivider;

        public OpexSwitchCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setWillNotDraw(false);
            setMinimumHeight(dp(56));
            setPadding(dp(18), dp(11), dp(16), dp(11));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.CENTER);
            addView(iconView, LayoutHelper.createLinear(26, 26, Gravity.CENTER_VERTICAL, 0, 0, 14, 0));

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

            switchView = new M3SwitchView(context, resourcesProvider);
            addView(switchView, LayoutHelper.createLinear(50, 28, Gravity.CENTER_VERTICAL));
        }

        public void bind(int iconResId, CharSequence title, CharSequence subtitle, boolean checked, boolean divider) {
            this.needDivider = divider;
            if (iconResId != 0) {
                iconView.setVisibility(VISIBLE);
                iconView.setImageResource(iconResId);
                int iconColor = Theme.getColor(checked ? Theme.key_windowBackgroundWhiteBlueText4 : Theme.key_windowBackgroundWhiteGrayIcon, resourcesProvider);
                iconView.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
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
            switchView.setChecked(checked, false);
            invalidate();
        }

        public void setChecked(boolean checked) {
            switchView.setChecked(checked, true);
            if (iconView.getVisibility() == VISIBLE) {
                int iconColor = Theme.getColor(checked ? Theme.key_windowBackgroundWhiteBlueText4 : Theme.key_windowBackgroundWhiteGrayIcon, resourcesProvider);
                iconView.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
            }
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (needDivider && !LagramConfig.materialContainers) {
                dividerPaint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                canvas.drawLine(dp(18), getHeight() - 1, getWidth() - dp(18), getHeight() - 1, dividerPaint);
            }
        }

        public static class Factory extends UItem.UItemFactory<OpexSwitchCell> {
            static { setup(new Factory()); }

            @Override
            public OpexSwitchCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new OpexSwitchCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((OpexSwitchCell) view).bind(item.iconResId, item.text, item.subtext, item.checked, divider);
            }

            @Override
            public boolean equals(UItem a, UItem b) { return a.id == b.id; }

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
    // 2. OpexSliderCell (Exact Opexgram Material 3 Expressive Thick Pill Slider)
    // Thick 16dp pill bar, active left accent fill, 4dp gap, tall 4x26dp vertical thumb, right stop dot.
    // =============================================================================================
    public static class OpexSliderCell extends LinearLayout {
        private final Theme.ResourcesProvider resourcesProvider;
        private final TextView titleView;
        private final TextView valueView;
        private final PillTrackView trackView;
        private int minValue;
        private int maxValue = 100;
        private int defaultValue;
        private int currentValue;
        private String prefix = "";
        private String suffix = "";
        private Utilities.Callback<Integer> onChange;

        public OpexSliderCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(VERTICAL);
            setPadding(dp(18), dp(12), dp(18), dp(14));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout header = new LinearLayout(context);
            header.setOrientation(HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);
            addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f);
            titleView.setSingleLine(true);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            header.addView(titleView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 0, 0, 10, 0));

            valueView = new TextView(context);
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
            valueView.setTypeface(AndroidUtilities.bold());
            valueView.setOnClickListener(v -> {
                if (currentValue != defaultValue) {
                    applyValue(defaultValue, true);
                }
            });
            header.addView(valueView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

            trackView = new PillTrackView(context, resourcesProvider);
            addView(trackView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 30));
        }

        private void applyValue(int v, boolean notify) {
            int clamped = Math.max(minValue, Math.min(maxValue, v));
            boolean changed = clamped != currentValue;
            currentValue = clamped;
            valueView.setText(prefix + currentValue + suffix);
            trackView.setProgress((currentValue - minValue) / (float) Math.max(1, maxValue - minValue));
            if (notify && changed) {
                try {
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                } catch (Exception ignored) {}
                if (onChange != null) {
                    onChange.run(currentValue);
                }
            }
        }

        public void bind(CharSequence title, int min, int max, int defVal, int cur, String pre, String suf, Utilities.Callback<Integer> cb) {
            this.minValue = min;
            this.maxValue = Math.max(min + 1, max);
            this.defaultValue = defVal;
            this.currentValue = Math.max(min, Math.min(this.maxValue, cur));
            this.prefix = pre != null ? pre : "";
            this.suffix = suf != null ? suf : "";
            this.onChange = cb;

            titleView.setText(title);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
            valueView.setText(this.prefix + this.currentValue + this.suffix);
            valueView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider));

            trackView.setProgress((this.currentValue - this.minValue) / (float) Math.max(1, this.maxValue - this.minValue));
            trackView.setOnProgressChanged(p -> {
                int val = Math.round(minValue + p * (maxValue - minValue));
                applyValue(val, true);
            });
        }

        private static class PillTrackView extends View {
            private final Theme.ResourcesProvider resourcesProvider;
            private final Paint activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final RectF rect = new RectF();
            private final Path path = new Path();
            private final float[] leftRadii = new float[8];
            private final float[] rightRadii = new float[8];
            private float progress;
            private boolean dragging;
            private Utilities.Callback<Float> listener;

            PillTrackView(Context context, Theme.ResourcesProvider resourcesProvider) {
                super(context);
                this.resourcesProvider = resourcesProvider;
            }

            void setProgress(float p) {
                progress = Math.max(0f, Math.min(1f, p));
                invalidate();
            }

            void setOnProgressChanged(Utilities.Callback<Float> cb) {
                this.listener = cb;
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                int action = ev.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    dragging = true;
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                    updateFromX(ev.getX());
                    return true;
                } else if (action == MotionEvent.ACTION_MOVE && dragging) {
                    updateFromX(ev.getX());
                    return true;
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    if (dragging) {
                        updateFromX(ev.getX());
                        dragging = false;
                        if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    }
                }
                return super.onTouchEvent(ev);
            }

            private void updateFromX(float x) {
                float pad = dp(6);
                float usable = Math.max(1f, getWidth() - pad * 2f);
                float p = Math.max(0f, Math.min(1f, (x - pad) / usable));
                if (listener != null) {
                    listener.run(p);
                }
            }

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) return;

                boolean isDark = Theme.isCurrentThemeDark();
                int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
                int trackOff = ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 28 : 20);

                float cy = h * 0.5f;
                float trackH = LagramConfig.materialControls ? dp(15) : dp(6);
                float top = cy - trackH * 0.5f;
                float bottom = cy + trackH * 0.5f;
                float outerR = trackH * 0.5f;
                float innerR = dp(2.5f);

                float thumbW = dp(4);
                float thumbH = dp(25);
                float gap = dp(5);
                float pad = dp(6);
                float thumbX = pad + (w - pad * 2f) * progress;

                // Active left pill segment
                float leftEnd = thumbX - thumbW * 0.5f - gap;
                if (leftEnd > dp(2)) {
                    activePaint.setColor(accent);
                    rect.set(0, top, leftEnd, bottom);
                    leftRadii[0] = leftRadii[1] = outerR;
                    leftRadii[2] = leftRadii[3] = innerR;
                    leftRadii[4] = leftRadii[5] = innerR;
                    leftRadii[6] = leftRadii[7] = outerR;
                    path.rewind();
                    path.addRoundRect(rect, leftRadii, Path.Direction.CW);
                    canvas.drawPath(path, activePaint);
                }

                // Inactive right pill segment
                float rightStart = thumbX + thumbW * 0.5f + gap;
                if (rightStart < w - dp(2)) {
                    inactivePaint.setColor(trackOff);
                    rect.set(rightStart, top, w, bottom);
                    rightRadii[0] = rightRadii[1] = innerR;
                    rightRadii[2] = rightRadii[3] = outerR;
                    rightRadii[4] = rightRadii[5] = outerR;
                    rightRadii[6] = rightRadii[7] = innerR;
                    path.rewind();
                    path.addRoundRect(rect, rightRadii, Path.Direction.CW);
                    canvas.drawPath(path, inactivePaint);

                    // Signature Opexgram stop dot at right end
                    if (w - rightStart > dp(12)) {
                        dotPaint.setColor(ColorUtils.setAlphaComponent(accent, 170));
                        canvas.drawCircle(w - outerR, cy, dp(2.2f), dotPaint);
                    }
                }

                // Vertical M3 pill handle
                thumbPaint.setColor(accent);
                rect.set(thumbX - thumbW * 0.5f, cy - thumbH * 0.5f, thumbX + thumbW * 0.5f, cy + thumbH * 0.5f);
                canvas.drawRoundRect(rect, thumbW * 0.5f, thumbW * 0.5f, thumbPaint);
            }
        }

        public static class Factory extends UItem.UItemFactory<OpexSliderCell> {
            static { setup(new Factory()); }

            @Override
            public OpexSliderCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new OpexSliderCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                String pre = item.subtext != null ? item.subtext.toString() : "";
                String suf = item.textValue != null ? item.textValue.toString() : "";
                ((OpexSliderCell) view).bind(
                        item.text,
                        unpackMin(item.longValue),
                        unpackMax(item.longValue),
                        unpackDef(item.longValue),
                        item.intValue,
                        pre,
                        suf,
                        item.intCallback
                );
            }

            @Override
            public boolean isClickable() { return false; }

            @Override
            public boolean equals(UItem a, UItem b) { return a.id == b.id; }

            @Override
            public boolean contentsEquals(UItem a, UItem b) {
                return a.intValue == b.intValue
                        && a.longValue == b.longValue
                        && TextUtils.equals(a.text, b.text)
                        && TextUtils.equals(a.subtext, b.subtext)
                        && TextUtils.equals(a.textValue, b.textValue);
            }

            public static UItem of(int id, CharSequence title, int min, int max, int defVal, int cur, String suffix, Utilities.Callback<Integer> onChange) {
                return of(id, title, min, max, defVal, cur, "", suffix, onChange);
            }

            public static UItem of(int id, CharSequence title, int min, int max, int defVal, int cur, String prefix, String suffix, Utilities.Callback<Integer> onChange) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.text = title;
                item.subtext = prefix;
                item.textValue = suffix;
                item.intValue = cur;
                item.longValue = packRange(min, max, defVal);
                item.intCallback = onChange;
                return item;
            }
        }
    }

    // =============================================================================================
    // 3. OpexCheckPillsCell (Exact Opexgram M3 Checkmark Toggle Pills)
    // Supports BOTH single-select ([✓ Tone] [Blur], [Shrink] [✓ Drum]) AND multi-select bitmask
    // ([✓ Voice] [✓ Round] [✓ Music]).
    // =============================================================================================
    public static class OpexCheckPillsCell extends LinearLayout {
        private final Theme.ResourcesProvider resourcesProvider;
        private final TextView titleView;
        private final CheckPillsStrip strip;

        public OpexCheckPillsCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(VERTICAL);
            setPadding(dp(18), dp(12), dp(18), dp(14));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.5f);
            titleView.setSingleLine(true);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 10));

            strip = new CheckPillsStrip(context, resourcesProvider);
            addView(strip, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40));
        }

        public void bind(CharSequence title, String[] options, int stateValue, boolean multiSelect, Utilities.Callback<Integer> callback) {
            if (!TextUtils.isEmpty(title)) {
                titleView.setVisibility(VISIBLE);
                titleView.setText(title);
                titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
            } else {
                titleView.setVisibility(GONE);
            }
            strip.bind(options, stateValue, multiSelect, callback);
        }

        private static class CheckPillsStrip extends View {
            private final Theme.ResourcesProvider resourcesProvider;
            private final Paint pillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            private final RectF rect = new RectF();
            private String[] options = new String[0];
            private int stateValue;
            private boolean multiSelect;
            private Utilities.Callback<Integer> callback;

            CheckPillsStrip(Context context, Theme.ResourcesProvider resourcesProvider) {
                super(context);
                this.resourcesProvider = resourcesProvider;
                textPaint.setTextSize(dp(13.5f));
                textPaint.setTypeface(AndroidUtilities.bold());
                checkPaint.setStyle(Paint.Style.STROKE);
                checkPaint.setStrokeWidth(dpf2(2.0f));
                checkPaint.setStrokeCap(Paint.Cap.ROUND);
                checkPaint.setStrokeJoin(Paint.Join.ROUND);
            }

            void bind(String[] opts, int val, boolean multi, Utilities.Callback<Integer> cb) {
                this.options = opts != null ? opts : new String[0];
                this.stateValue = val;
                this.multiSelect = multi;
                this.callback = cb;
                invalidate();
            }

            private boolean isSelected(int i) {
                return multiSelect ? ((stateValue & (1 << i)) != 0) : (stateValue == i);
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                if (options.length == 0) return super.onTouchEvent(ev);
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                    int idx = Math.max(0, Math.min(options.length - 1, (int) (ev.getX() / (Math.max(1f, getWidth()) / options.length))));
                    try {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                    } catch (Exception ignored) {}
                    if (multiSelect) {
                        stateValue ^= (1 << idx);
                        invalidate();
                        if (callback != null) callback.run(idx);
                    } else if (stateValue != idx) {
                        stateValue = idx;
                        invalidate();
                        if (callback != null) callback.run(idx);
                    }
                    return true;
                }
                return super.onTouchEvent(ev);
            }

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0 || options.length == 0) return;

                boolean isDark = Theme.isCurrentThemeDark();
                int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
                int activeFill = ColorUtils.setAlphaComponent(accent, isDark ? 68 : 42);
                int inactiveFill = ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 16 : 11);
                int activeText = isDark ? ColorUtils.blendARGB(accent, Color.WHITE, 0.55f) : ColorUtils.blendARGB(accent, Color.BLACK, 0.25f);
                int inactiveText = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider);

                float gap = dp(8);
                float pillW = (w - gap * (options.length - 1)) / options.length;
                float r = h * 0.5f;
                Paint.FontMetrics fm = textPaint.getFontMetrics();
                float textY = h * 0.5f - (fm.ascent + fm.descent) * 0.5f;

                for (int i = 0; i < options.length; i++) {
                    float left = i * (pillW + gap);
                    boolean sel = isSelected(i);
                    rect.set(left, 0, left + pillW, h);
                    pillPaint.setColor(sel ? activeFill : inactiveFill);
                    canvas.drawRoundRect(rect, r, r, pillPaint);

                    String label = options[i] != null ? options[i] : "";
                    float checkSpace = sel ? dp(16) : 0f;
                    float maxTextW = Math.max(dp(20), pillW - dp(16) - checkSpace);
                    String ellip = TextUtils.ellipsize(label, textPaint, maxTextW, TextUtils.TruncateAt.END).toString();
                    float tw = textPaint.measureText(ellip);
                    float totalW = tw + checkSpace;
                    float startX = left + (pillW - totalW) * 0.5f;

                    if (sel) {
                        checkPaint.setColor(activeText);
                        float cx = startX + dp(5);
                        float cy = h * 0.5f;
                        canvas.drawLine(cx - dp(3.8f), cy, cx - dp(0.8f), cy + dp(3.0f), checkPaint);
                        canvas.drawLine(cx - dp(0.8f), cy + dp(3.0f), cx + dp(4.2f), cy - dp(3.2f), checkPaint);
                        startX += dp(16);
                    }
                    textPaint.setColor(sel ? activeText : inactiveText);
                    canvas.drawText(ellip, startX, textY, textPaint);
                }
            }
        }

        public static class Factory extends UItem.UItemFactory<OpexCheckPillsCell> {
            static { setup(new Factory()); }

            @Override
            public OpexCheckPillsCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new OpexCheckPillsCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((OpexCheckPillsCell) view).bind(item.text, item.texts, item.intValue, item.checked, item.intCallback);
            }

            @Override
            public boolean isClickable() { return false; }

            @Override
            public boolean equals(UItem a, UItem b) { return a.id == b.id; }

            @Override
            public boolean contentsEquals(UItem a, UItem b) {
                return a.intValue == b.intValue
                        && a.checked == b.checked
                        && TextUtils.equals(a.text, b.text)
                        && Arrays.equals(a.texts, b.texts);
            }

            public static UItem single(int id, CharSequence title, String[] options, int selectedIndex, Utilities.Callback<Integer> onSelect) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.text = title;
                item.texts = options;
                item.intValue = selectedIndex;
                item.checked = false;
                item.intCallback = onSelect;
                return item;
            }

            public static UItem multi(int id, CharSequence title, String[] options, int bitmask, Utilities.Callback<Integer> onToggleIndex) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.text = title;
                item.texts = options;
                item.intValue = bitmask;
                item.checked = true;
                item.intCallback = onToggleIndex;
                return item;
            }
        }
    }

    // =============================================================================================
    // 4. OpexMaterialHeroPreviewCell (Opexgram Appearance Top Hero Preview Card - Sheet 1 Frame 3)
    // =============================================================================================
    public static class OpexMaterialHeroPreviewCell extends View {
        private final Theme.ResourcesProvider resourcesProvider;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        public OpexMaterialHeroPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(dpf2(1f));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        public static long fingerprint() {
            LagramConfig.ensureLoaded();
            long h = 17L;
            h = h * 31L + (LagramConfig.materialContainers ? 1 : 0);
            h = h * 31L + LagramConfig.sectionSpacingDp;
            h = h * 31L + (LagramConfig.materialChatList ? 1 : 0);
            h = h * 31L + (LagramConfig.materialControls ? 1 : 0);
            h = h * 31L + (LagramConfig.materialAppBar ? 1 : 0);
            h = h * 31L + LagramConfig.appBarBackground;
            return h;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(148));
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            boolean isDark = Theme.isCurrentThemeDark();
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            int frameBg = isDark ? 0xFF0A0B0D : 0xFFEBEEF3;
            int cardBg = isDark ? 0xFF1A1C1F : Color.WHITE;
            int lineDim = ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 45 : 30);

            // Outer mini display canvas
            rect.set(dp(12), dp(10), w - dp(12), h - dp(10));
            paint.setColor(frameBg);
            canvas.drawRoundRect(rect, dp(16), dp(16), paint);
            strokePaint.setColor(ColorUtils.setAlphaComponent(accent, 40));
            canvas.drawRoundRect(rect, dp(16), dp(16), strokePaint);

            float innerL = dp(20);
            float innerR = w - dp(20);
            float midX = (innerL + innerR) * 0.5f;

            // Top mini app bar
            float barY = dp(18);
            paint.setColor(LagramConfig.appBarBackground == LagramConfig.APP_BAR_BG_BLUR
                    ? ColorUtils.setAlphaComponent(accent, 42)
                    : ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, 18));
            rect.set(innerL, barY, innerR, barY + dp(16));
            canvas.drawRoundRect(rect, dp(8), dp(8), paint);
            // App bar title pill (centered if materialAppBar)
            paint.setColor(ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, 140));
            float titleW = dp(46);
            float titleX = LagramConfig.materialAppBar ? (midX - titleW * 0.5f) : (innerL + dp(10));
            rect.set(titleX, barY + dp(5.5f), titleX + titleW, barY + dp(10.5f));
            canvas.drawRoundRect(rect, dp(2.5f), dp(2.5f), paint);

            // Left panel: Mini Chat List (cards if materialChatList)
            float leftL = innerL;
            float leftR = midX - dp(6);
            float rowTop = barY + dp(24);
            float rowGap = LagramConfig.materialChatList ? dp(4) : dp(1);
            float rowH = dp(22);
            int[] dotColors = {0xFF4BA3FF, 0xFF7ED957, 0xFFFFB74D};
            for (int i = 0; i < 3; i++) {
                float y = rowTop + i * (rowH + rowGap);
                rect.set(leftL, y, leftR, y + rowH);
                paint.setColor(cardBg);
                float rad = LagramConfig.materialChatList ? dp(9) : dp(2);
                canvas.drawRoundRect(rect, rad, rad, paint);
                paint.setColor(dotColors[i]);
                canvas.drawCircle(leftL + dp(11), y + rowH * 0.5f, dp(6), paint);
                paint.setColor(lineDim);
                rect.set(leftL + dp(22), y + dp(6), leftR - dp(14), y + dp(10));
                canvas.drawRoundRect(rect, dp(2), dp(2), paint);
                rect.set(leftL + dp(22), y + dp(13), leftR - dp(30), y + dp(16));
                canvas.drawRoundRect(rect, dp(1.5f), dp(1.5f), paint);
            }

            // Right panel: Mini Settings Cards + M3 Slider + Switch
            float rightL = midX + dp(6);
            float rightR = innerR;
            float secGap = LagramConfig.materialContainers ? Math.min(dp(6), dp(2 + LagramConfig.sectionSpacingDp * 0.25f)) : 0f;
            float cardRad = LagramConfig.materialContainers ? dp(8) : dp(2);

            // Mini Settings card 1 (switch row)
            rect.set(rightL, rowTop, rightR, rowTop + dp(22));
            paint.setColor(cardBg);
            canvas.drawRoundRect(rect, cardRad, cardRad, paint);
            paint.setColor(lineDim);
            rect.set(rightL + dp(8), rowTop + dp(8.5f), rightR - dp(34), rowTop + dp(13.5f));
            canvas.drawRoundRect(rect, dp(2.5f), dp(2.5f), paint);
            // Mini M3 switch
            paint.setColor(accent);
            rect.set(rightR - dp(26), rowTop + dp(5), rightR - dp(6), rowTop + dp(17));
            canvas.drawRoundRect(rect, dp(6), dp(6), paint);
            paint.setColor(isDark ? 0xFF0E1B10 : Color.WHITE);
            canvas.drawCircle(rightR - dp(11.5f), rowTop + dp(11), dp(4.2f), paint);

            // Mini Settings card 2 (thick M3 pill slider)
            float sTop = rowTop + dp(22) + secGap;
            rect.set(rightL, sTop, rightR, sTop + dp(24));
            paint.setColor(cardBg);
            canvas.drawRoundRect(rect, cardRad, cardRad, paint);
            float slY = sTop + dp(12);
            paint.setColor(accent);
            rect.set(rightL + dp(8), slY - dp(3.5f), rightL + (rightR - rightL) * 0.58f, slY + dp(3.5f));
            canvas.drawRoundRect(rect, dp(3.5f), dp(3.5f), paint);
            paint.setColor(lineDim);
            rect.set(rightL + (rightR - rightL) * 0.66f, slY - dp(3.5f), rightR - dp(8), slY + dp(3.5f));
            canvas.drawRoundRect(rect, dp(3.5f), dp(3.5f), paint);
            paint.setColor(accent);
            float handleX = rightL + (rightR - rightL) * 0.62f;
            rect.set(handleX - dp(1.5f), slY - dp(6.5f), handleX + dp(1.5f), slY + dp(6.5f));
            canvas.drawRoundRect(rect, dp(1.5f), dp(1.5f), paint);

            // Bottom floating tab bar pill
            float navTop = h - dp(28);
            float navW = dp(112);
            rect.set(midX - navW * 0.5f, navTop, midX + navW * 0.5f, navTop + dp(13));
            paint.setColor(cardBg);
            canvas.drawRoundRect(rect, dp(6.5f), dp(6.5f), paint);
            for (int i = 0; i < 4; i++) {
                float cx = (midX - navW * 0.34f) + i * (navW * 0.225f);
                paint.setColor(i == 0 ? accent : lineDim);
                canvas.drawCircle(cx, navTop + dp(6.5f), dp(2.8f), paint);
            }
        }

        public static class Factory extends UItem.UItemFactory<OpexMaterialHeroPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public OpexMaterialHeroPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new OpexMaterialHeroPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                view.invalidate();
            }

            @Override
            public boolean isClickable() { return false; }

            @Override
            public boolean equals(UItem a, UItem b) { return a.id == b.id; }

            @Override
            public boolean contentsEquals(UItem a, UItem b) { return a.longValue == b.longValue; }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = fingerprint();
                return item;
            }
        }
    }

    // =============================================================================================
    // 5. OpexRoundingPreviewCell (Opexgram Shape / Unified Rounding Preview - Sheet 1 Frame 4)
    // =============================================================================================
    public static class OpexRoundingPreviewCell extends View {
        private final Theme.ResourcesProvider resourcesProvider;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        public OpexRoundingPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        public static long fingerprint() {
            return (LagramConfig.roundingPreset * 31L) + LagramConfig.avatarShape;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(112));
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            boolean isDark = Theme.isCurrentThemeDark();
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            int innerCard = ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 16 : 10);
            int lineDim = ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 55 : 38);

            float elemR;
            switch (LagramConfig.roundingPreset) {
                case LagramConfig.ROUNDING_ROUND: elemR = dp(16); break;
                case LagramConfig.ROUNDING_SQUIRCLE: elemR = dp(11); break;
                case LagramConfig.ROUNDING_SQUARE: elemR = dp(4); break;
                default: elemR = dp(10); break;
            }

            // Row 1: Avatar + title bars
            int avSize = dp(36);
            int avR = LagramConfig.getAvatarRoundRadius(avSize);
            rect.set(dp(18), dp(14), dp(18) + avSize, dp(14) + avSize);
            paint.setColor(ColorUtils.setAlphaComponent(accent, 180));
            canvas.drawRoundRect(rect, avR, avR, paint);

            paint.setColor(lineDim);
            rect.set(dp(64), dp(21), w * 0.56f, dp(29));
            canvas.drawRoundRect(rect, dp(4), dp(4), paint);
            rect.set(dp(64), dp(35), w * 0.38f, dp(41));
            canvas.drawRoundRect(rect, dp(3), dp(3), paint);

            // Row 2: Two action buttons previewing unified rounding
            float btnTop = dp(62);
            float btnH = dp(34);
            float midX = w * 0.5f;
            paint.setColor(accent);
            rect.set(dp(18), btnTop, midX - dp(6), btnTop + btnH);
            canvas.drawRoundRect(rect, elemR, elemR, paint);

            paint.setColor(innerCard);
            rect.set(midX + dp(6), btnTop, w - dp(18), btnTop + btnH);
            canvas.drawRoundRect(rect, elemR, elemR, paint);
        }

        public static class Factory extends UItem.UItemFactory<OpexRoundingPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public OpexRoundingPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new OpexRoundingPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                view.invalidate();
            }

            @Override
            public boolean isClickable() { return false; }

            @Override
            public boolean equals(UItem a, UItem b) { return a.id == b.id; }

            @Override
            public boolean contentsEquals(UItem a, UItem b) { return a.longValue == b.longValue; }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = fingerprint();
                return item;
            }
        }
    }

    // =============================================================================================
    // 6. OpexVisualPickerCell (Exact Opexgram 3-Card Visual Picker for Chat Box & Folder Bar!)
    // =============================================================================================
    public static class OpexVisualPickerCell extends View {
        public static final int MODE_CHAT_BOX = 0;
        public static final int MODE_FOLDERS = 1;

        private final Theme.ResourcesProvider resourcesProvider;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        private int mode;
        private int selectedIndex;
        private String[] labels = new String[3];
        private Utilities.Callback<Integer> onSelect;

        public OpexVisualPickerCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            strokePaint.setStyle(Paint.Style.STROKE);
            labelPaint.setTextSize(dp(12.5f));
            labelPaint.setTypeface(AndroidUtilities.bold());
            labelPaint.setTextAlign(Paint.Align.CENTER);
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        public void bind(int mode, String[] labels, int selected, Utilities.Callback<Integer> cb) {
            this.mode = mode;
            this.labels = labels != null && labels.length == 3 ? labels : new String[]{"", "", ""};
            this.selectedIndex = Math.max(0, Math.min(2, selected));
            this.onSelect = cb;
            requestLayout();
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int h = mode == MODE_CHAT_BOX ? dp(178) : dp(104);
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), h);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                int idx = Math.max(0, Math.min(2, (int) (ev.getX() / (Math.max(1f, getWidth()) / 3f))));
                if (idx != selectedIndex) {
                    selectedIndex = idx;
                    try {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                    } catch (Exception ignored) {}
                    invalidate();
                    if (onSelect != null) onSelect.run(idx);
                }
                return true;
            }
            return super.onTouchEvent(ev);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            boolean isDark = Theme.isCurrentThemeDark();
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            int cardSurface = ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 16 : 10);
            int dimColor = ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 65 : 42);
            int activeText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider);
            int inactiveText = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider);

            float cardsTop = dp(12);
            if (mode == MODE_CHAT_BOX) {
                // Live Chat Input Box Preview banner at the top (matches Sheet 2 Frame 4)
                float bannerH = dp(80);
                rect.set(dp(16), dp(10), w - dp(16), dp(10) + bannerH);
                paint.setColor(ColorUtils.blendARGB(isDark ? 0xFF1A2218 : 0xFFD9E6D6, accent, 0.25f));
                canvas.drawRoundRect(rect, dp(16), dp(16), paint);

                // Tiny incoming + outgoing chat bubbles inside preview
                paint.setColor(ColorUtils.setAlphaComponent(isDark ? 0xFF282C26 : Color.WHITE, 210));
                rect.set(dp(26), dp(18), dp(124), dp(34));
                canvas.drawRoundRect(rect, dp(8), dp(8), paint);
                paint.setColor(ColorUtils.setAlphaComponent(accent, 210));
                rect.set(w - dp(128), dp(28), w - dp(26), dp(44));
                canvas.drawRoundRect(rect, dp(8), dp(8), paint);

                // Live input bar styled by selectedIndex (0=Telegram, 1=Expressive, 2=Buttons)
                float contrast = Math.max(15, LagramConfig.chatBoxContrast) / 100f;
                int boxFill = ColorUtils.setAlphaComponent(isDark ? 0xFF141618 : Color.WHITE, Math.round(240 * contrast));
                float barY = dp(52);
                float barH = dp(30);
                if (selectedIndex == LagramConfig.CHAT_BOX_TELEGRAM) {
                    paint.setColor(boxFill);
                    rect.set(dp(20), barY, w - dp(20), barY + barH);
                    canvas.drawRoundRect(rect, dp(8), dp(8), paint);
                } else if (selectedIndex == LagramConfig.CHAT_BOX_EXPRESSIVE) {
                    paint.setColor(boxFill);
                    rect.set(dp(24), barY, w - dp(60), barY + barH);
                    canvas.drawRoundRect(rect, dp(15), dp(15), paint);
                    paint.setColor(accent);
                    canvas.drawCircle(w - dp(39), barY + barH * 0.5f, dp(15), paint);
                } else {
                    // Buttons only
                    paint.setColor(boxFill);
                    canvas.drawCircle(dp(38), barY + barH * 0.5f, dp(13), paint);
                    canvas.drawCircle(w - dp(74), barY + barH * 0.5f, dp(13), paint);
                    paint.setColor(accent);
                    canvas.drawCircle(w - dp(39), barY + barH * 0.5f, dp(15), paint);
                }
                cardsTop = dp(100);
            }

            float padH = dp(16);
            float gap = dp(10);
            float cardW = (w - padH * 2f - gap * 2f) / 3f;
            float cardH = mode == MODE_CHAT_BOX ? dp(44) : dp(60);

            for (int i = 0; i < 3; i++) {
                float l = padH + i * (cardW + gap);
                float r = l + cardW;
                boolean sel = (i == selectedIndex);
                rect.set(l, cardsTop, r, cardsTop + cardH);
                paint.setColor(sel ? ColorUtils.setAlphaComponent(accent, isDark ? 42 : 24) : cardSurface);
                canvas.drawRoundRect(rect, dp(12), dp(12), paint);

                if (sel) {
                    strokePaint.setStrokeWidth(dpf2(2f));
                    strokePaint.setColor(accent);
                    canvas.drawRoundRect(rect, dp(12), dp(12), strokePaint);
                }

                // Draw miniature icon diagram inside each picker card
                float cy = cardsTop + cardH * 0.5f;
                if (mode == MODE_CHAT_BOX) {
                    if (i == 0) {
                        paint.setColor(sel ? accent : dimColor);
                        rect.set(l + dp(8), cy - dp(7), r - dp(8), cy + dp(7));
                        canvas.drawRoundRect(rect, dp(4), dp(4), paint);
                    } else if (i == 1) {
                        paint.setColor(sel ? accent : dimColor);
                        rect.set(l + dp(8), cy - dp(7), r - dp(26), cy + dp(7));
                        canvas.drawRoundRect(rect, dp(7), dp(7), paint);
                        canvas.drawCircle(r - dp(15), cy, dp(7), paint);
                    } else {
                        paint.setColor(sel ? accent : dimColor);
                        canvas.drawCircle(l + dp(15), cy, dp(6), paint);
                        canvas.drawCircle(r - dp(30), cy, dp(6), paint);
                        canvas.drawCircle(r - dp(15), cy, dp(7), paint);
                    }
                } else {
                    // MODE_FOLDERS diagram
                    float topY = cardsTop + dp(12);
                    if (i == 0) {
                        // Telegram classic underline
                        paint.setColor(dimColor);
                        rect.set(l + dp(10), topY, l + dp(34), topY + dp(4));
                        canvas.drawRoundRect(rect, dp(2), dp(2), paint);
                        rect.set(l + dp(40), topY, r - dp(10), topY + dp(4));
                        canvas.drawRoundRect(rect, dp(2), dp(2), paint);
                        paint.setColor(sel ? accent : dimColor);
                        rect.set(l + dp(10), topY + dp(7), l + dp(34), topY + dp(10));
                        canvas.drawRoundRect(rect, dp(1.5f), dp(1.5f), paint);
                    } else if (i == 1) {
                        // Tabs bar
                        paint.setColor(dimColor);
                        rect.set(l + dp(8), topY, r - dp(8), topY + dp(12));
                        canvas.drawRoundRect(rect, dp(6), dp(6), paint);
                        paint.setColor(sel ? accent : dimColor);
                        rect.set(l + dp(10), topY + dp(2), l + cardW * 0.5f, topY + dp(10));
                        canvas.drawRoundRect(rect, dp(4), dp(4), paint);
                    } else {
                        // Buttons capsules
                        paint.setColor(sel ? accent : dimColor);
                        rect.set(l + dp(8), topY, l + cardW * 0.52f, topY + dp(13));
                        canvas.drawRoundRect(rect, dp(6.5f), dp(6.5f), paint);
                        paint.setColor(dimColor);
                        rect.set(l + cardW * 0.58f, topY, r - dp(8), topY + dp(13));
                        canvas.drawRoundRect(rect, dp(6.5f), dp(6.5f), paint);
                    }
                    // Tiny chat lines below folder bar
                    paint.setColor(ColorUtils.setAlphaComponent(dimColor, 85));
                    rect.set(l + dp(10), cardsTop + dp(34), r - dp(14), cardsTop + dp(40));
                    canvas.drawRoundRect(rect, dp(3), dp(3), paint);
                    rect.set(l + dp(10), cardsTop + dp(45), r - dp(24), cardsTop + dp(50));
                    canvas.drawRoundRect(rect, dp(2.5f), dp(2.5f), paint);
                }

                // Bottom label
                labelPaint.setColor(sel ? activeText : inactiveText);
                canvas.drawText(labels[i], l + cardW * 0.5f, cardsTop + cardH + dp(18), labelPaint);
            }
        }

        public static class Factory extends UItem.UItemFactory<OpexVisualPickerCell> {
            static { setup(new Factory()); }

            @Override
            public OpexVisualPickerCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new OpexVisualPickerCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((OpexVisualPickerCell) view).bind((int) item.longValue, item.texts, item.intValue, item.intCallback);
            }

            @Override
            public boolean isClickable() { return false; }

            @Override
            public boolean equals(UItem a, UItem b) { return a.id == b.id; }

            @Override
            public boolean contentsEquals(UItem a, UItem b) {
                return a.intValue == b.intValue
                        && a.longValue == b.longValue
                        && a.id == b.id
                        && Arrays.equals(a.texts, b.texts);
            }

            public static UItem of(int id, int mode, String[] labels, int selected, Utilities.Callback<Integer> onSelect) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = mode;
                item.texts = labels;
                item.intValue = selected;
                item.intCallback = onSelect;
                return item;
            }
        }
    }

    // =============================================================================================
    // 7. OpexMessageMenuPreviewCell (Exact Opexgram Message Menu Live Preview - Sheet 2 Frames 1-2)
    // =============================================================================================
    public static class OpexMessageMenuPreviewCell extends View {
        private final Theme.ResourcesProvider resourcesProvider;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private int style;

        public OpexMessageMenuPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            textPaint.setTextSize(dp(13));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        public void setStyle(int s) {
            this.style = s;
            requestLayout();
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int h = style == LagramConfig.FOCUS_STYLE_CAPSULE ? dp(74) : dp(138);
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), h);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            boolean isDark = Theme.isCurrentThemeDark();
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            int menuBg = isDark ? 0xFF1F2124 : Color.WHITE;
            int tileBg = isDark ? 0xFF2B2E33 : 0xFFF0F2F5;
            int fg = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider);

            if (style == LagramConfig.FOCUS_STYLE_CAPSULE) {
                // Opexgram Capsule Pill Bar (Sheet 2 Frame 2)
                float barW = Math.min(w - dp(36), dp(292));
                float barH = dp(46);
                float l = (w - barW) * 0.5f;
                float t = (h - barH) * 0.5f;
                rect.set(l, t, l + barW, t + barH);
                paint.setColor(menuBg);
                canvas.drawRoundRect(rect, barH * 0.5f, barH * 0.5f, paint);

                String[] glyphs = {"↩", "⧉", "🔗", "↪", "📌", "🗑", "•••"};
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTextSize(dp(16));
                Paint.FontMetrics fm = textPaint.getFontMetrics();
                float ty = t + barH * 0.5f - (fm.ascent + fm.descent) * 0.5f;
                float step = barW / glyphs.length;
                for (int i = 0; i < glyphs.length; i++) {
                    textPaint.setColor(i == 5 ? 0xFFEF5350 : fg);
                    canvas.drawText(glyphs[i], l + step * (i + 0.5f), ty, textPaint);
                }
            } else if (style == LagramConfig.FOCUS_STYLE_TILES) {
                // Opexgram Tiles Bar (4 top cards + list card)
                float cardW = Math.min(w - dp(48), dp(264));
                float l = (w - cardW) * 0.5f;
                float gap = dp(6);
                float tileW = (cardW - gap * 3f) / 4f;
                String[] tileLabels = {"↩", "⧉", "↪", "📌"};
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTextSize(dp(16));
                for (int i = 0; i < 4; i++) {
                    float tl = l + i * (tileW + gap);
                    rect.set(tl, dp(14), tl + tileW, dp(64));
                    paint.setColor(i == 0 ? ColorUtils.setAlphaComponent(accent, 45) : tileBg);
                    canvas.drawRoundRect(rect, dp(14), dp(14), paint);
                    textPaint.setColor(i == 0 ? accent : fg);
                    canvas.drawText(tileLabels[i], tl + tileW * 0.5f, dp(45), textPaint);
                }
                rect.set(l, dp(72), l + cardW, dp(124));
                paint.setColor(menuBg);
                canvas.drawRoundRect(rect, dp(14), dp(14), paint);
                textPaint.setTextAlign(Paint.Align.LEFT);
                textPaint.setTextSize(dp(13));
                textPaint.setColor(fg);
                canvas.drawText("🔗  " + LocaleController.getString(R.string.CopyLink), l + dp(16), dp(95), textPaint);
                canvas.drawText("🗑  " + LocaleController.getString(R.string.Delete), l + dp(16), dp(115), textPaint);
            } else {
                // Classic List Card (Sheet 1 Frame 12)
                float cardW = Math.min(w - dp(64), dp(248));
                float l = (w - cardW) * 0.5f;
                rect.set(l, dp(14), l + cardW, dp(124));
                paint.setColor(menuBg);
                canvas.drawRoundRect(rect, dp(16), dp(16), paint);
                textPaint.setTextAlign(Paint.Align.LEFT);
                textPaint.setTextSize(dp(13.5f));
                textPaint.setColor(fg);
                canvas.drawText("↩   " + LocaleController.getString(R.string.Reply), l + dp(18), dp(42), textPaint);
                canvas.drawText("⧉   " + LocaleController.getString(R.string.Copy), l + dp(18), dp(74), textPaint);
                canvas.drawText("🔗   " + LocaleController.getString(R.string.CopyLink), l + dp(18), dp(106), textPaint);
            }
        }

        public static class Factory extends UItem.UItemFactory<OpexMessageMenuPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public OpexMessageMenuPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new OpexMessageMenuPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((OpexMessageMenuPreviewCell) view).setStyle(item.intValue);
            }

            @Override
            public boolean isClickable() { return false; }

            @Override
            public boolean equals(UItem a, UItem b) { return a.id == b.id; }

            @Override
            public boolean contentsEquals(UItem a, UItem b) { return a.intValue == b.intValue; }

            public static UItem of(int id, int style) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.intValue = style;
                return item;
            }
        }
    }

    // =============================================================================================
    // 8. OpexTextAnimPreviewCell (Exact Opexgram Live Typing Animation Preview - Sheet 1 Frame 11)
    // =============================================================================================
    public static class OpexTextAnimPreviewCell extends View {
        private static final String SAMPLE = "Lagram Turbo ✨";
        private final Theme.ResourcesProvider resourcesProvider;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private float animProgress;
        private ValueAnimator loopAnimator;

        public OpexTextAnimPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            textPaint.setTextSize(dp(15));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (loopAnimator == null) {
                loopAnimator = ValueAnimator.ofFloat(0f, 1f);
                loopAnimator.setDuration(2600);
                loopAnimator.setRepeatCount(ValueAnimator.INFINITE);
                loopAnimator.addUpdateListener(a -> {
                    animProgress = (float) a.getAnimatedValue();
                    invalidate();
                });
                loopAnimator.start();
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (loopAnimator != null) {
                loopAnimator.cancel();
                loopAnimator = null;
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(76));
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            boolean isDark = Theme.isCurrentThemeDark();
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            int pillBg = ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 20 : 12);
            int fg = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider);

            // Capsule input field + Send circle
            float barH = dp(42);
            float top = (h - barH) * 0.5f;
            paint.setColor(pillBg);
            rect.set(dp(16), top, w - dp(66), top + barH);
            canvas.drawRoundRect(rect, barH * 0.5f, barH * 0.5f, paint);

            paint.setColor(accent);
            canvas.drawCircle(w - dp(37), h * 0.5f, dp(20), paint);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("➤", w - dp(36), h * 0.5f + dp(5), textPaint);

            // Animated letters
            textPaint.setTextAlign(Paint.Align.LEFT);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float baseY = h * 0.5f - (fm.ascent + fm.descent) * 0.5f;
            float x = dp(34);
            int len = SAMPLE.length();
            float cursorPos = Math.min(len, animProgress * (len + 4));

            for (int i = 0; i < len; i++) {
                float p = LagramConfig.textAppearAnimation
                        ? Math.max(0f, Math.min(1f, cursorPos - i))
                        : (i < cursorPos ? 1f : 0f);
                if (p <= 0f) break;
                String ch = SAMPLE.substring(i, i + 1);
                float cw = textPaint.measureText(ch);
                canvas.save();
                float dy = (LagramConfig.textAppearAnimation && LagramConfig.textAnimSlide) ? (1f - p) * -dp(8) : 0f;
                float sc = (LagramConfig.textAppearAnimation && LagramConfig.textAnimScale) ? (0.45f + 0.55f * p) : 1f;
                float rot = (LagramConfig.textAppearAnimation && LagramConfig.textAnimRotate) ? (1f - p) * -18f : 0f;
                int alpha = (LagramConfig.textAppearAnimation && LagramConfig.textAnimBlur) ? Math.round(255 * p) : 255;
                canvas.translate(x + cw * 0.5f, baseY + dy);
                canvas.scale(sc, sc);
                canvas.rotate(rot);
                textPaint.setColor(ColorUtils.setAlphaComponent(fg, alpha));
                canvas.drawText(ch, -cw * 0.5f, 0, textPaint);
                canvas.restore();
                x += cw;
            }
            // Blinking cursor
            paint.setColor(accent);
            rect.set(x + dp(2), h * 0.5f - dp(9), x + dp(4), h * 0.5f + dp(9));
            canvas.drawRoundRect(rect, dp(1), dp(1), paint);
        }

        public static class Factory extends UItem.UItemFactory<OpexTextAnimPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public OpexTextAnimPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new OpexTextAnimPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                view.invalidate();
            }

            @Override
            public boolean isClickable() { return false; }

            @Override
            public boolean equals(UItem a, UItem b) { return a.id == b.id; }

            @Override
            public boolean contentsEquals(UItem a, UItem b) { return a.longValue == b.longValue; }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                long fp = (LagramConfig.textAppearAnimation ? 1 : 0)
                        | (LagramConfig.textAnimBlur ? 2 : 0)
                        | (LagramConfig.textAnimSlide ? 4 : 0)
                        | (LagramConfig.textAnimScale ? 8 : 0)
                        | (LagramConfig.textAnimRotate ? 16 : 0);
                item.longValue = fp;
                return item;
            }
        }
    }
}
