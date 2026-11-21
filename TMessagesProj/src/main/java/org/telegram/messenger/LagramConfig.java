package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class LagramConfig {
    private static final String PREFS_NAME = "lagram_config";
    private static boolean loaded;

    public static boolean ios27BottomBar = true;
    public static boolean turboDownloads = true;
    public static boolean highRefreshRateScroll = true;
    public static boolean disableSponsoredMessages = true;

    private LagramConfig() {}

    public static void ensureLoaded() {
        if (loaded || ApplicationLoader.applicationContext == null) {
            return;
        }
        loaded = true;
        final SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        ios27BottomBar = prefs.getBoolean("ios27BottomBar", true);
        turboDownloads = prefs.getBoolean("turboDownloads", true);
        highRefreshRateScroll = prefs.getBoolean("highRefreshRateScroll", true);
        disableSponsoredMessages = prefs.getBoolean("disableSponsoredMessages", true);
    }

    private static void saveBoolean(String key, boolean value) {
        if (ApplicationLoader.applicationContext == null) {
            return;
        }
        ApplicationLoader.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    public static boolean toggleIos27BottomBar() {
        ensureLoaded();
        ios27BottomBar = !ios27BottomBar;
        saveBoolean("ios27BottomBar", ios27BottomBar);
        return ios27BottomBar;
    }

    public static boolean toggleTurboDownloads() {
        ensureLoaded();
        turboDownloads = !turboDownloads;
        saveBoolean("turboDownloads", turboDownloads);
        return turboDownloads;
    }

    public static boolean toggleHighRefreshRateScroll() {
        ensureLoaded();
        highRefreshRateScroll = !highRefreshRateScroll;
        saveBoolean("highRefreshRateScroll", highRefreshRateScroll);
        return highRefreshRateScroll;
    }

    public static boolean toggleDisableSponsoredMessages() {
        ensureLoaded();
        disableSponsoredMessages = !disableSponsoredMessages;
        saveBoolean("disableSponsoredMessages", disableSponsoredMessages);
        return disableSponsoredMessages;
    }

    public static CharSequence withNewBadge(CharSequence title) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder(title);
        ssb.append("  NEW");
        ssb.setSpan(new NewPillBadgeSpan(), ssb.length() - 3, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    private static final class NewPillBadgeSpan extends ReplacementSpan {
        private static final String LABEL = "NEW";
        private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        NewPillBadgeSpan() {
            bgPaint.setColor(0xFF2196F3);
            textPaint.setColor(0xFFFFFFFF);
            textPaint.setTypeface(AndroidUtilities.bold());
            textPaint.setTextSize(AndroidUtilities.dp(10));
        }

        @Override
        public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
            return Math.round(textPaint.measureText(LABEL) + AndroidUtilities.dp(12));
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
            final float textW = textPaint.measureText(LABEL);
            final float pillW = textW + AndroidUtilities.dp(12);
            final float pillH = AndroidUtilities.dp(17);
            final Paint.FontMetrics pm = paint.getFontMetrics();
            final float centerY = y + (pm.ascent + pm.descent) * 0.5f;
            final float pillTop = centerY - pillH * 0.5f;
            rect.set(x, pillTop, x + pillW, pillTop + pillH);
            final float r = pillH * 0.5f;
            canvas.drawRoundRect(rect, r, r, bgPaint);

            final Paint.FontMetrics tm = textPaint.getFontMetrics();
            final float textBaseline = centerY - (tm.ascent + tm.descent) * 0.5f;
            canvas.drawText(LABEL, x + (pillW - textW) * 0.5f, textBaseline, textPaint);
        }
    }
}
