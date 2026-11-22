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
    // 4. OpexColorSwatchesCell
    // Tactile circular color swatches with selection ring & checkmark (0 = Default / None swatch)
    // =============================================================================================
    public static class OpexColorSwatchesCell extends LinearLayout {
        private final Theme.ResourcesProvider resourcesProvider;
        private final TextView titleView;
        private final SwatchesView swatchesView;

        public OpexColorSwatchesCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(VERTICAL);
            setPadding(dp(16), dp(12), dp(16), dp(12));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.5f);
            titleView.setTypeface(AndroidUtilities.bold());
            addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 10));

            swatchesView = new SwatchesView(context, resourcesProvider);
            addView(swatchesView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 42));
        }

        public void bind(CharSequence title, int[] palette, int selectedColor, Utilities.Callback<Integer> onPick) {
            if (!TextUtils.isEmpty(title)) {
                titleView.setVisibility(VISIBLE);
                titleView.setText(title);
                titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
            } else {
                titleView.setVisibility(GONE);
            }
            swatchesView.bind(palette, selectedColor, onPick);
        }

        private static class SwatchesView extends View {
            private final Theme.ResourcesProvider resourcesProvider;
            private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private int[] colors = new int[0];
            private int selectedColor = 0;
            private Utilities.Callback<Integer> onPick;

            SwatchesView(Context context, Theme.ResourcesProvider resourcesProvider) {
                super(context);
                this.resourcesProvider = resourcesProvider;
                ringPaint.setStyle(Paint.Style.STROKE);
                ringPaint.setStrokeWidth(dpf2(2.2f));
                checkPaint.setStyle(Paint.Style.STROKE);
                checkPaint.setStrokeCap(Paint.Cap.ROUND);
                checkPaint.setStrokeJoin(Paint.Join.ROUND);
                checkPaint.setStrokeWidth(dpf2(2.1f));
            }

            void bind(int[] palette, int selected, Utilities.Callback<Integer> cb) {
                this.colors = palette != null ? palette : new int[0];
                this.selectedColor = selected;
                this.onPick = cb;
                invalidate();
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (colors.length == 0) return false;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int n = colors.length;
                    float slotW = getWidth() / (float) n;
                    int idx = Math.max(0, Math.min(n - 1, (int) (event.getX() / Math.max(1f, slotW))));
                    selectedColor = colors[idx];
                    try {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    } catch (Exception ignored) {}
                    invalidate();
                    if (onPick != null) {
                        onPick.run(selectedColor);
                    }
                    return true;
                }
                return super.onTouchEvent(event);
            }

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                int n = colors.length;
                if (n == 0) return;
                int w = getWidth();
                int h = getHeight();
                float cy = h * 0.5f;
                float slotW = w / (float) n;
                float r = Math.min(dp(15), slotW * 0.38f);
                boolean isDark = Theme.isCurrentThemeDark();
                int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);

                for (int i = 0; i < n; i++) {
                    float cx = slotW * (i + 0.5f);
                    int col = colors[i];
                    boolean sel = (col == selectedColor);

                    if (col == 0) {
                        // Default / None swatch with diagonal slash
                        fillPaint.setColor(isDark ? 0xFF282C34 : 0xFFE6EAF0);
                        canvas.drawCircle(cx, cy, r, fillPaint);
                        checkPaint.setColor(isDark ? 0xFF9EA6B2 : 0xFF6A7280);
                        float d = r * 0.45f;
                        canvas.drawLine(cx - d, cy + d, cx + d, cy - d, checkPaint);
                    } else {
                        fillPaint.setColor(col);
                        canvas.drawCircle(cx, cy, r, fillPaint);
                    }

                    if (sel) {
                        ringPaint.setColor(col == 0 ? accent : col);
                        canvas.drawCircle(cx, cy, r + dp(3.2f), ringPaint);
                        if (col != 0) {
                            checkPaint.setColor(Color.WHITE);
                            canvas.drawLine(cx - dp(4.5f), cy + dp(0.2f), cx - dp(1.2f), cy + dp(3.5f), checkPaint);
                            canvas.drawLine(cx - dp(1.2f), cy + dp(3.5f), cx + dp(4.8f), cy - dp(3.2f), checkPaint);
                        }
                    }
                }
            }
        }

        public static class Factory extends UItem.UItemFactory<OpexColorSwatchesCell> {
            static { setup(new Factory()); }

            @Override
            public OpexColorSwatchesCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new OpexColorSwatchesCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                int[] palette = item.object instanceof int[] ? (int[]) item.object : new int[0];
                ((OpexColorSwatchesCell) view).bind(item.text, palette, item.intValue, item.intCallback);
            }

            @Override public boolean isClickable() { return false; }
            @Override public boolean equals(UItem a, UItem b) { return a.id == b.id; }
            @Override public boolean contentsEquals(UItem a, UItem b) {
                return a.intValue == b.intValue && TextUtils.equals(a.text, b.text);
            }

            public static UItem of(int id, CharSequence title, int[] palette, int selectedColor, Utilities.Callback<Integer> onPick) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.text = title;
                item.object = palette;
                item.intValue = selectedColor;
                item.intCallback = onPick;
                return item;
            }
        }
    }
}
