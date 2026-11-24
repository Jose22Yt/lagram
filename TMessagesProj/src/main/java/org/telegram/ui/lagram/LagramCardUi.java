package org.telegram.ui.lagram;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Switch;

/**
 * Material 3 Graphite Card UI kit built from scratch for Lagram Settings fragments.
 */
public final class LagramCardUi {

    private LagramCardUi() {
    }

    public static GradientDrawable createGraphiteCardDrawable(boolean isDark, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadius(AndroidUtilities.dp(radiusDp));
        d.setColor(LagramThemeEngine.getGraphiteCardColor(isDark));
        d.setStroke(AndroidUtilities.dp(1), LagramThemeEngine.getGraphiteCardStrokeColor(isDark));
        return d;
    }

    public static RippleDrawable createGraphiteCardRipple(boolean isDark, int radiusDp) {
        GradientDrawable content = createGraphiteCardDrawable(isDark, radiusDp);
        GradientDrawable mask = new GradientDrawable();
        mask.setShape(GradientDrawable.RECTANGLE);
        mask.setCornerRadius(AndroidUtilities.dp(radiusDp));
        mask.setColor(Color.WHITE);
        int rippleColor = LagramThemeEngine.withAlpha(LagramThemeEngine.getAccentColors().primary, 48);
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), content, mask);
    }

    public static LinearLayout createGraphiteCard(Context context) {
        boolean isDark = Theme.isCurrentThemeDark();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(createGraphiteCardDrawable(isDark, 20));
        card.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(6), AndroidUtilities.dp(4), AndroidUtilities.dp(6));
        card.setClipToPadding(false);
        return card;
    }

    public static TextView createSectionHeader(Context context, String title) {
        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        tv.setTypeface(AndroidUtilities.bold());
        tv.setAllCaps(false);
        tv.setTextColor(LagramThemeEngine.getAccentColors().primary);
        tv.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(6));
        return tv;
    }

    public static View createDivider(Context context) {
        View divider = new View(context);
        divider.setBackgroundColor(LagramThemeEngine.getGraphiteDivider());
        LinearLayout.LayoutParams lp = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 1, 16, 0, 16, 0);
        lp.height = Math.max(1, AndroidUtilities.dp(0.6f));
        divider.setLayoutParams(lp);
        return divider;
    }

    public static LinearLayout createCategoryNavCard(Context context, String badgeEmoji, String title, String subtitle, int accentColor, Runnable onClick) {
        boolean isDark = Theme.isCurrentThemeDark();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(createGraphiteCardRipple(isDark, 20));
        card.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(15), AndroidUtilities.dp(16), AndroidUtilities.dp(15));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> {
            LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
            if (onClick != null) {
                onClick.run();
            }
        });

        TextView iconBadge = new TextView(context);
        iconBadge.setText(badgeEmoji);
        iconBadge.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        iconBadge.setGravity(Gravity.CENTER);
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setShape(GradientDrawable.RECTANGLE);
        badgeBg.setCornerRadius(AndroidUtilities.dp(14));
        badgeBg.setColor(LagramThemeEngine.withAlpha(accentColor, 46));
        badgeBg.setStroke(AndroidUtilities.dp(1), LagramThemeEngine.withAlpha(accentColor, 110));
        iconBadge.setBackground(badgeBg);
        card.addView(iconBadge, LayoutHelper.createLinear(44, 44, 0, 0, 14, 0));

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView titleTv = new TextView(context);
        titleTv.setText(title);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        titleTv.setTypeface(AndroidUtilities.bold());
        titleTv.setTextColor(LagramThemeEngine.getGraphiteTextPrimary());
        textCol.addView(titleTv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if (!TextUtils.isEmpty(subtitle)) {
            TextView subTv = new TextView(context);
            subTv.setText(subtitle);
            subTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            subTv.setTextColor(LagramThemeEngine.getGraphiteTextSecondary());
            textCol.addView(subTv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));
        }

        card.addView(textCol, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

        TextView arrow = new TextView(context);
        arrow.setText("›");
        arrow.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
        arrow.setTextColor(LagramThemeEngine.getGraphiteTextSecondary());
        card.addView(arrow, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 8, 0, 0, 0));

        return card;
    }

    public static LinearLayout createSwitchRow(Context context, String title, String subtitle, boolean initialValue, ValueCallback<Boolean> onChanged) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(12), AndroidUtilities.dp(14), AndroidUtilities.dp(12));
        row.setClickable(true);
        row.setFocusable(true);

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView titleTv = new TextView(context);
        titleTv.setText(title);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        titleTv.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        titleTv.setTextColor(LagramThemeEngine.getGraphiteTextPrimary());
        textCol.addView(titleTv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if (!TextUtils.isEmpty(subtitle)) {
            TextView subTv = new TextView(context);
            subTv.setText(subtitle);
            subTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.5f);
            subTv.setTextColor(LagramThemeEngine.getGraphiteTextSecondary());
            textCol.addView(subTv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));
        }

        row.addView(textCol, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, 0, 0, 12, 0));

        Switch toggle = new Switch(context);
        toggle.setChecked(initialValue, false);
        toggle.setFocusable(false);
        toggle.setClickable(false);
        row.addView(toggle, LayoutHelper.createLinear(37, 20));

        row.setOnClickListener(v -> {
            boolean next = !toggle.isChecked();
            toggle.setChecked(next, LagramConfig.animationPreset != LagramConfig.ANIM_NONE);
            LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
            if (onChanged != null) {
                onChanged.onValue(next);
            }
        });

        return row;
    }

    public static LinearLayout createSegmentedSelectorRow(Context context, String title, String subtitle, String[] labels, int selectedIndex, ValueCallback<Integer> onSelected) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(12), AndroidUtilities.dp(14), AndroidUtilities.dp(12));

        TextView titleTv = new TextView(context);
        titleTv.setText(title);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        titleTv.setTextColor(LagramThemeEngine.getGraphiteTextPrimary());
        container.addView(titleTv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if (!TextUtils.isEmpty(subtitle)) {
            TextView subTv = new TextView(context);
            subTv.setText(subtitle);
            subTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.5f);
            subTv.setTextColor(LagramThemeEngine.getGraphiteTextSecondary());
            container.addView(subTv, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 8));
        } else {
            titleTv.setPadding(0, 0, 0, AndroidUtilities.dp(8));
        }

        LinearLayout pillsRow = new LinearLayout(context);
        pillsRow.setOrientation(LinearLayout.HORIZONTAL);

        final TextView[] pillViews = new TextView[labels.length];
        final int[] currentSelected = new int[]{selectedIndex};

        Runnable refreshPills = () -> {
            LagramThemeEngine.AccentColors accent = LagramThemeEngine.getAccentColors();
            for (int i = 0; i < pillViews.length; i++) {
                boolean active = i == currentSelected[0];
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setCornerRadius(AndroidUtilities.dp(14));
                if (active) {
                    bg.setColor(accent.primary);
                    pillViews[i].setTextColor(accent.onPrimary);
                    pillViews[i].setTypeface(AndroidUtilities.bold());
                } else {
                    bg.setColor(LagramThemeEngine.GRAPHITE_CARD_ELEVATED);
                    bg.setStroke(AndroidUtilities.dp(1), LagramThemeEngine.GRAPHITE_DIVIDER);
                    pillViews[i].setTextColor(LagramThemeEngine.getGraphiteTextSecondary());
                    pillViews[i].setTypeface(Typeface.DEFAULT);
                }
                pillViews[i].setBackground(bg);
            }
        };

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            TextView pill = new TextView(context);
            pill.setText(labels[i]);
            pill.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.5f);
            pill.setGravity(Gravity.CENTER);
            pill.setSingleLine(true);
            pill.setEllipsize(TextUtils.TruncateAt.END);
            pill.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
            pill.setOnClickListener(v -> {
                if (currentSelected[0] != index) {
                    currentSelected[0] = index;
                    refreshPills.run();
                    LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
                    if (onSelected != null) {
                        onSelected.onValue(index);
                    }
                }
            });
            pillViews[i] = pill;
            pillsRow.addView(pill, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, i == 0 ? 0 : 4, 0, i == labels.length - 1 ? 0 : 4, 0));
        }

        refreshPills.run();
        container.addView(pillsRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        return container;
    }

    public static LinearLayout createSliderRow(Context context, String title, int min, int max, int currentValue, String suffix, ValueCallback<Integer> onChanged) {
        return createSliderRow(context, title, min, max, currentValue, suffix, onChanged, null);
    }

    public static LinearLayout createSliderRow(Context context, String title, int min, int max, int currentValue, String suffix, ValueCallback<Integer> onChanged, Runnable onCommit) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(12), AndroidUtilities.dp(14), AndroidUtilities.dp(10));

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleTv = new TextView(context);
        titleTv.setText(title);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        titleTv.setTextColor(LagramThemeEngine.getGraphiteTextPrimary());
        header.addView(titleTv, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

        TextView valuePill = new TextView(context);
        valuePill.setText(currentValue + suffix);
        valuePill.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.5f);
        valuePill.setTypeface(AndroidUtilities.bold());
        valuePill.setTextColor(LagramThemeEngine.getAccentColors().primary);
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setCornerRadius(AndroidUtilities.dp(10));
        pillBg.setColor(LagramThemeEngine.withAlpha(LagramThemeEngine.getAccentColors().primary, 36));
        valuePill.setBackground(pillBg);
        valuePill.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(3), AndroidUtilities.dp(10), AndroidUtilities.dp(3));
        header.addView(valuePill, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        container.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 6));

        SeekBar seekBar = new SeekBar(context);
        seekBar.setMax(Math.max(1, max - min));
        seekBar.setProgress(Math.max(0, Math.min(max - min, currentValue - min)));
        int accent = LagramThemeEngine.getAccentColors().primary;
        seekBar.setProgressTintList(ColorStateList.valueOf(accent));
        seekBar.setThumbTintList(ColorStateList.valueOf(accent));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                int val = min + progress;
                valuePill.setText(val + suffix);
                if (fromUser && onChanged != null) {
                    onChanged.onValue(val);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                LagramThemeEngine.performHaptic(bar, HapticFeedbackConstants.CLOCK_TICK);
                if (onCommit != null) {
                    onCommit.run();
                }
            }
        });
        container.addView(seekBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        return container;
    }

    public static TextView createActionButton(Context context, String label, int bgColor, int textColor, Runnable onClick) {
        TextView btn = new TextView(context);
        btn.setText(label);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.5f);
        btn.setTypeface(AndroidUtilities.bold());
        btn.setTextColor(textColor);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(AndroidUtilities.dp(16));
        bg.setColor(bgColor);
        btn.setBackground(bg);
        btn.setOnClickListener(v -> {
            LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
            if (onClick != null) {
                onClick.run();
            }
        });
        return btn;
    }

    public interface ValueCallback<T> {
        void onValue(T value);
    }
}
