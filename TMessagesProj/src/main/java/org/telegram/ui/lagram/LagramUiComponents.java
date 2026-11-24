package org.telegram.ui.lagram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Custom Material 3 Expressive components for Lagram:
 * - M3BottomNavigationBar with spring-animated indicator pill (Chats, Contactos, Llamadas, Ajustes)
 * - ProfileM3ActionBanner with segmented pill buttons (Llamar, Silenciar, Buscar, Más)
 */
public final class LagramUiComponents {

    private LagramUiComponents() {
    }

    public interface OnTabSelectedListener {
        void onTabSelected(int index);
    }

    /**
     * Material 3 Expressive Bottom Navigation Bar with 120 Hz spring-animated pill indicator.
     */
    public static final class M3BottomNavigationBar extends FrameLayout {
        private static final String[] LABELS = new String[]{"Chats", "Contactos", "Llamadas", "Ajustes"};
        private static final String[] ICONS = new String[]{"💬", "👤", "📞", "⚙"};

        private final LinearLayout row;
        private final TextView[] tabLabels = new TextView[LABELS.length];
        private final Paint pillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF pillRect = new RectF();

        private int selectedIndex = 0;
        private float indicatorPosition = 0f;
        private SpringAnimation indicatorSpring;
        private OnTabSelectedListener listener;

        public M3BottomNavigationBar(Context context) {
            super(context);
            setWillNotDraw(false);
            boolean isDark = Theme.isCurrentThemeDark();
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(AndroidUtilities.dp(24));
            bg.setColor(LagramThemeEngine.getGraphiteCardColor(isDark));
            bg.setStroke(AndroidUtilities.dp(1), LagramThemeEngine.getGraphiteCardStrokeColor(isDark));
            setBackground(bg);

            row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            addView(row, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            for (int i = 0; i < LABELS.length; i++) {
                final int idx = i;
                LinearLayout tab = new LinearLayout(context);
                tab.setOrientation(LinearLayout.VERTICAL);
                tab.setGravity(Gravity.CENTER);
                tab.setPadding(0, AndroidUtilities.dp(6), 0, AndroidUtilities.dp(6));

                TextView icon = new TextView(context);
                icon.setText(ICONS[i]);
                icon.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                icon.setGravity(Gravity.CENTER);
                tab.addView(icon, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

                TextView label = new TextView(context);
                label.setText(LABELS[i]);
                label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
                label.setGravity(Gravity.CENTER);
                tabLabels[i] = label;
                tab.addView(label, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));

                tab.setOnClickListener(v -> {
                    selectTab(idx, true);
                    LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
                    if (listener != null) {
                        listener.onTabSelected(idx);
                    }
                });
                row.addView(tab, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f));
            }

            indicatorSpring = new SpringAnimation(this, new FloatPropertyCompat<M3BottomNavigationBar>("indicatorPosition") {
                @Override
                public float getValue(M3BottomNavigationBar view) {
                    return view.indicatorPosition;
                }

                @Override
                public void setValue(M3BottomNavigationBar view, float value) {
                    view.indicatorPosition = value;
                    view.invalidate();
                }
            });
            SpringForce force = new SpringForce(0f);
            force.setStiffness(SpringForce.STIFFNESS_MEDIUM);
            force.setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
            indicatorSpring.setSpring(force);

            updateLabelColors();
        }

        public void setOnTabSelectedListener(OnTabSelectedListener listener) {
            this.listener = listener;
        }

        public int getSelectedIndex() {
            return selectedIndex;
        }

        public void selectTab(int index, boolean animate) {
            int clamped = Math.max(0, Math.min(LABELS.length - 1, index));
            selectedIndex = clamped;
            updateLabelColors();
            if (animate && LagramConfig.animationPreset != LagramConfig.ANIM_NONE) {
                indicatorSpring.animateToFinalPosition(clamped);
            } else {
                indicatorSpring.cancel();
                indicatorPosition = clamped;
                invalidate();
            }
        }

        private void updateLabelColors() {
            LagramThemeEngine.AccentColors accent = LagramThemeEngine.getAccentColors();
            for (int i = 0; i < tabLabels.length; i++) {
                boolean active = i == selectedIndex;
                tabLabels[i].setTextColor(active ? accent.primary : LagramThemeEngine.getGraphiteTextSecondary());
                tabLabels[i].setTypeface(active ? AndroidUtilities.bold() : Typeface.DEFAULT);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            float segW = w / (float) LABELS.length;
            float cx = segW * indicatorPosition + segW * 0.5f;
            float pillW = Math.min(segW - AndroidUtilities.dp(14), AndroidUtilities.dp(68));
            float pillH = AndroidUtilities.dp(42);
            float cy = h / 2f;

            LagramThemeEngine.AccentColors accent = LagramThemeEngine.getAccentColors();
            pillPaint.setColor(accent.containerDark);
            pillRect.set(cx - pillW / 2f, cy - pillH / 2f, cx + pillW / 2f, cy + pillH / 2f);
            canvas.drawRoundRect(pillRect, pillH / 2f, pillH / 2f, pillPaint);
        }
    }

    /**
     * Material 3 Expressive segmented pill action bar for Profiles (Llamar, Silenciar, Buscar, Más).
     */
    public static LinearLayout createProfileSegmentedActions(Context context, Runnable onCall, Runnable onMute, Runnable onSearch, Runnable onMore) {
        LinearLayout bar = new LinearLayout(context);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(8), AndroidUtilities.dp(12), AndroidUtilities.dp(8));

        String[] labels = new String[]{"Llamar", "Silenciar", "Buscar", "Más"};
        Runnable[] actions = new Runnable[]{onCall, onMute, onSearch, onMore};
        LagramThemeEngine.AccentColors accent = LagramThemeEngine.getAccentColors();

        for (int i = 0; i < labels.length; i++) {
            final Runnable action = actions[i];
            TextView pill = new TextView(context);
            pill.setText(labels[i]);
            pill.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.5f);
            pill.setTypeface(AndroidUtilities.bold());
            pill.setGravity(Gravity.CENTER);
            pill.setTextColor(i == 0 ? accent.onPrimary : LagramThemeEngine.getGraphiteTextPrimary());
            pill.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(10), AndroidUtilities.dp(8), AndroidUtilities.dp(10));

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(AndroidUtilities.dp(16));
            bg.setColor(i == 0 ? accent.primary : LagramThemeEngine.getGraphiteCardColor(Theme.isCurrentThemeDark()));
            if (i != 0) {
                bg.setStroke(AndroidUtilities.dp(1), LagramThemeEngine.getGraphiteCardStrokeColor(Theme.isCurrentThemeDark()));
            }
            pill.setBackground(bg);
            pill.setOnClickListener(v -> {
                LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
                if (action != null) {
                    action.run();
                }
            });
            bar.addView(pill, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, i == 0 ? 0 : 4, 0, i == labels.length - 1 ? 0 : 4, 0));
        }

        return bar;
    }
}
