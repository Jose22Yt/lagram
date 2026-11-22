package org.telegram.ui.lagram;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

/**
 * Crisp typographic "Lagram" brand wordmark drawable used in DialogStoriesCell and IntroActivity.
 * Renders white glyphs by default so PorterDuff.Mode.MULTIPLY color filters tint it accurately
 * for light, dark, and AMOLED themes with zero bitmap memory overhead.
 */
public final class LagramLogoDrawable extends Drawable {

    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final String brandText;
    private final int intrinsicWidth;
    private final int intrinsicHeight;

    public LagramLogoDrawable(float textSizePx) {
        String label = LocaleController.getString(R.string.AppName);
        this.brandText = (label != null && !label.isEmpty()) ? label : "Lagram";
        textPaint.setTypeface(AndroidUtilities.bold());
        textPaint.setTextSize(textSizePx);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setLetterSpacing(-0.015f);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        this.intrinsicWidth = (int) Math.ceil(textPaint.measureText(brandText)) + AndroidUtilities.dp(2);
        this.intrinsicHeight = (int) Math.ceil(fm.descent - fm.ascent);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect b = getBounds();
        if (b.isEmpty()) {
            return;
        }
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float baseline = b.centerY() - (fm.ascent + fm.descent) * 0.5f;
        canvas.drawText(brandText, b.left, baseline, textPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        if (textPaint.getAlpha() != alpha) {
            textPaint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        textPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return intrinsicWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return intrinsicHeight;
    }
}
