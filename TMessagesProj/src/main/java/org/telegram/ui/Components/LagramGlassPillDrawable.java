package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;

public class LagramGlassPillDrawable extends Drawable {

    private static final float[] RIM_POSITIONS = new float[]{0f, 0.22f, 0.50f, 0.78f, 1f};

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final RectF rimRect = new RectF();
    private final RectF innerRect = new RectF();

    @Nullable
    private BlurredBackgroundDrawable backdropBlur;

    private boolean isDark = true;
    private int accentColor = 0;
    private int alpha = 255;
    private boolean shadersDirty = true;

    public LagramGlassPillDrawable() {
        fillPaint.setStyle(Paint.Style.FILL);
        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setStrokeWidth(AndroidUtilities.dpf2(1.05f));
        innerHighlightPaint.setStyle(Paint.Style.STROKE);
        innerHighlightPaint.setStrokeWidth(AndroidUtilities.dpf2(0.8f));
    }

    public LagramGlassPillDrawable setBackdropBlur(@Nullable BlurredBackgroundDrawable backdropBlur) {
        this.backdropBlur = backdropBlur;
        updateBackdropBounds();
        invalidateSelf();
        return this;
    }

    public LagramGlassPillDrawable setDarkMode(boolean dark) {
        if (this.isDark != dark) {
            this.isDark = dark;
            this.shadersDirty = true;
            invalidateSelf();
        }
        return this;
    }

    public LagramGlassPillDrawable setAccentTint(int color) {
        if (this.accentColor != color) {
            this.accentColor = color;
            this.shadersDirty = true;
            invalidateSelf();
        }
        return this;
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        rect.set(bounds);
        float halfStroke = rimPaint.getStrokeWidth() * 0.5f;
        rimRect.set(rect);
        rimRect.inset(halfStroke, halfStroke);
        innerRect.set(rect);
        innerRect.inset(rimPaint.getStrokeWidth() + innerHighlightPaint.getStrokeWidth() * 0.5f,
                rimPaint.getStrokeWidth() + innerHighlightPaint.getStrokeWidth() * 0.5f);
        shadersDirty = true;
        updateBackdropBounds();
    }

    private void updateBackdropBounds() {
        if (backdropBlur != null) {
            Rect b = getBounds();
            backdropBlur.setBounds(b);
            float radius = Math.min(b.width(), b.height()) * 0.5f;
            if (radius > 0f) {
                backdropBlur.setRadius(radius);
            }
        }
    }

    private void ensureShaders() {
        if (!shadersDirty || rect.height() <= 0f) {
            return;
        }
        shadersDirty = false;

        final float top = rect.top;
        final float bottom = rect.bottom;

        int fillTop;
        int fillBottom;
        int rimTopColor;
        int rimMidColor;
        int rimSideColor;
        int rimBottomColor;

        if (accentColor != 0) {
            int base = ColorUtils.setAlphaComponent(accentColor, isDark ? 190 : 210);
            fillTop = ColorUtils.blendARGB(base, 0xFFFFFFFF, 0.10f);
            fillBottom = ColorUtils.blendARGB(base, 0xFF000000, 0.16f);
            rimTopColor = ColorUtils.blendARGB(0x99FFFFFF, accentColor, 0.25f);
            rimMidColor = ColorUtils.setAlphaComponent(accentColor, 34);
            rimSideColor = ColorUtils.setAlphaComponent(accentColor, 10);
            rimBottomColor = ColorUtils.blendARGB(0x70FFFFFF, accentColor, 0.25f);
        } else if (isDark) {
            fillTop = 0x9E23272B;
            fillBottom = 0xAD171A1D;
            rimTopColor = 0x73FFFFFF;
            rimMidColor = 0x16FFFFFF;
            rimSideColor = 0x05FFFFFF;
            rimBottomColor = 0x52FFFFFF;
        } else {
            fillTop = 0xCCFFFFFF;
            fillBottom = 0xB8F0F2F5;
            rimTopColor = 0xE6FFFFFF;
            rimMidColor = 0x38FFFFFF;
            rimSideColor = 0x12000000;
            rimBottomColor = 0x80FFFFFF;
        }

        fillPaint.setShader(new LinearGradient(0f, top, 0f, bottom, fillTop, fillBottom, Shader.TileMode.CLAMP));
        rimPaint.setShader(new LinearGradient(
                0f, top, 0f, bottom,
                new int[]{rimTopColor, rimMidColor, rimSideColor, rimMidColor, rimBottomColor},
                RIM_POSITIONS,
                Shader.TileMode.CLAMP
        ));
        innerHighlightPaint.setShader(new LinearGradient(
                0f, top, 0f, bottom,
                new int[]{isDark ? 0x22FFFFFF : 0x44FFFFFF, 0x00FFFFFF, 0x00FFFFFF, 0x00FFFFFF, isDark ? 0x16FFFFFF : 0x26FFFFFF},
                RIM_POSITIONS,
                Shader.TileMode.CLAMP
        ));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (rect.isEmpty()) {
            return;
        }
        ensureShaders();

        if (backdropBlur != null) {
            backdropBlur.setAlpha(alpha);
            backdropBlur.draw(canvas);
        }

        float radius = Math.min(rect.width(), rect.height()) * 0.5f;
        fillPaint.setAlpha(alpha);
        canvas.drawRoundRect(rect, radius, radius, fillPaint);

        float innerRadius = Math.max(0f, radius - rimPaint.getStrokeWidth());
        innerHighlightPaint.setAlpha(alpha);
        canvas.drawRoundRect(innerRect, innerRadius, innerRadius, innerHighlightPaint);

        float rimRadius = Math.max(0f, radius - rimPaint.getStrokeWidth() * 0.5f);
        rimPaint.setAlpha(alpha);
        canvas.drawRoundRect(rimRect, rimRadius, rimRadius, rimPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        if (this.alpha != alpha) {
            this.alpha = alpha;
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        fillPaint.setColorFilter(colorFilter);
        rimPaint.setColorFilter(colorFilter);
        innerHighlightPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
