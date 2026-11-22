package org.telegram.ui.lagram;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatBackgroundDrawable;
import org.telegram.ui.Components.MotionBackgroundDrawable;
import org.telegram.ui.Components.blur3.utils.BitmapChangeTracker;
import org.telegram.utils.glass.source.GlassSource;
import org.telegram.utils.glass.source.GlassSourceBitmap;
import org.telegram.utils.glass.source.GlassSourceWrapped;

import java.lang.ref.WeakReference;

/**
 * Authentic Opexgram Liquid Glass message bubble rendering engine.
 * Samples and stack-blurs the live chat wallpaper (MotionBackgroundDrawable / GlassSourceBitmap)
 * into a shared BitmapShader aligned pixel-perfectly behind every MessageDrawable via
 * Theme.applyServiceShaderMatrix, with zero per-frame allocations.
 */
public final class LagramGlassBubbleEngine {

    private static GlassSourceBitmap primaryBitmapSource;
    private static Object currentOwner;
    private static WeakReference<Drawable> wallpaperDrawableRef;

    private static final GlassSourceBitmap capturedBitmapSource = new GlassSourceBitmap();
    private static final BitmapChangeTracker bgTracker = new BitmapChangeTracker();
    private static final BitmapChangeTracker patternTracker = new BitmapChangeTracker();
    private static final Rect tmpBounds = new Rect();

    private static Bitmap captureBitmap;
    private static Canvas captureCanvas;
    private static boolean captureValid;
    private static long lastCaptureTime;
    private static int lastMotionIntensity;

    private static int viewportWidth;
    private static int viewportHeight;

    private static final Paint glassFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private static final Paint glassBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint glassHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Matrix shaderMatrix = new Matrix();

    private static Bitmap lastShaderBitmap;
    private static BitmapShader cachedShader;
    private static int lastTintColor;
    private static PorterDuffColorFilter cachedColorFilter;

    private static int lastHighlightTop = Integer.MIN_VALUE;
    private static int lastHighlightBottom = Integer.MIN_VALUE;
    private static boolean lastHighlightDark;
    private static LinearGradient cachedHighlightGradient;

    static {
        glassFillPaint.setFilterBitmap(true);
        glassBorderPaint.setStyle(Paint.Style.STROKE);
        glassBorderPaint.setStrokeWidth(AndroidUtilities.dpf2(1.0f));
        glassHighlightPaint.setStyle(Paint.Style.FILL);
    }

    private LagramGlassBubbleEngine() {
    }

    public static void setViewportSize(int width, int height) {
        if (width > 0 && height > 0) {
            viewportWidth = width;
            viewportHeight = height;
        }
    }

    public static void setSource(Object owner, @Nullable GlassSource source, @Nullable Drawable wallpaperDrawable) {
        currentOwner = owner;
        while (source instanceof GlassSourceWrapped) {
            source = ((GlassSourceWrapped) source).getSource();
        }
        GlassSourceBitmap bitmapSource = source instanceof GlassSourceBitmap ? (GlassSourceBitmap) source : null;
        if (primaryBitmapSource != bitmapSource) {
            primaryBitmapSource = bitmapSource;
            invalidateShaderCache();
        }
        Drawable prev = wallpaperDrawableRef == null ? null : wallpaperDrawableRef.get();
        if (prev != wallpaperDrawable) {
            wallpaperDrawableRef = wallpaperDrawable == null ? null : new WeakReference<>(wallpaperDrawable);
            invalidateCapture();
            if (getCapturableDrawable() != null) {
                if (LagramConfig.glassMessages) {
                    ensureCapturedBitmap();
                }
            } else {
                capturedBitmapSource.setBitmap(null);
                captureBitmap = null;
                captureCanvas = null;
                invalidateShaderCache();
            }
        }
    }

    public static void clearSource(Object owner, @Nullable GlassSource source, @Nullable Drawable wallpaperDrawable) {
        if (currentOwner != owner) {
            return;
        }
        currentOwner = null;
        while (source instanceof GlassSourceWrapped) {
            source = ((GlassSourceWrapped) source).getSource();
        }
        GlassSourceBitmap bitmapSource = source instanceof GlassSourceBitmap ? (GlassSourceBitmap) source : null;
        if (bitmapSource != null && primaryBitmapSource == bitmapSource) {
            primaryBitmapSource = null;
            invalidateShaderCache();
        }
        if (wallpaperDrawable != null) {
            Drawable prev = wallpaperDrawableRef == null ? null : wallpaperDrawableRef.get();
            if (prev == wallpaperDrawable) {
                wallpaperDrawableRef = null;
                invalidateCapture();
                capturedBitmapSource.setBitmap(null);
                captureBitmap = null;
                captureCanvas = null;
                invalidateShaderCache();
            }
        }
    }

    public static void invalidateCapture() {
        captureValid = false;
        lastCaptureTime = 0L;
        bgTracker.invalidate();
        patternTracker.invalidate();
    }

    private static void invalidateShaderCache() {
        lastShaderBitmap = null;
        cachedShader = null;
        glassFillPaint.setShader(null);
    }

    public static boolean isAvailable() {
        if (!LagramConfig.glassMessages) {
            return false;
        }
        if (viewportWidth > 0 && viewportHeight > 0 && getCapturableDrawable() != null) {
            return true;
        }
        if (primaryBitmapSource != null) {
            Bitmap bmp = primaryBitmapSource.getBitmap();
            if (bmp != null && !bmp.isRecycled()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Drawable getCapturableDrawable() {
        Drawable drawable = wallpaperDrawableRef == null ? null : wallpaperDrawableRef.get();
        if (drawable instanceof ChatBackgroundDrawable) {
            drawable = ((ChatBackgroundDrawable) drawable).getDrawable(false);
        }
        if (drawable instanceof MotionBackgroundDrawable) {
            MotionBackgroundDrawable motion = (MotionBackgroundDrawable) drawable;
            if (motion.hasPattern() || motion.getIntensity() < 0) {
                return motion;
            }
            if (primaryBitmapSource == null || primaryBitmapSource.getBitmap() == null) {
                return motion;
            }
            return null;
        }
        // Fallback for custom non-bitmap drawables when primaryBitmapSource has no bitmap
        if (drawable != null && (primaryBitmapSource == null || primaryBitmapSource.getBitmap() == null)) {
            return drawable;
        }
        return null;
    }

    @Nullable
    private static GlassSourceBitmap ensureCapturedBitmap() {
        Drawable capturable = getCapturableDrawable();
        if (capturable == null || viewportWidth <= 0 || viewportHeight <= 0) {
            return null;
        }
        long now = SystemClock.elapsedRealtime();
        if (now - lastCaptureTime >= 250L || !captureValid) {
            lastCaptureTime = now;
            boolean needsRecapture = !captureValid;
            if (capturable instanceof MotionBackgroundDrawable) {
                MotionBackgroundDrawable motion = (MotionBackgroundDrawable) capturable;
                if (!needsRecapture) {
                    needsRecapture = lastMotionIntensity != motion.getIntensity()
                            || bgTracker.isInvalidated(motion.getBitmap())
                            || patternTracker.isInvalidated(motion.getPatternBitmap());
                }
                if (needsRecapture && (!captureValid || motion.getPosAnimationProgress() >= 1.0f)) {
                    bgTracker.set(motion.getBitmap());
                    patternTracker.set(motion.getPatternBitmap());
                    lastMotionIntensity = motion.getIntensity();
                    captureDrawableToBlurredBitmap(motion);
                }
            } else if (needsRecapture) {
                captureDrawableToBlurredBitmap(capturable);
            }
        }
        return captureValid ? capturedBitmapSource : null;
    }

    private static void captureDrawableToBlurredBitmap(Drawable drawable) {
        int targetW = Math.max(40, Math.round((viewportWidth * 240.0f) / viewportHeight));
        int targetH = 240;
        if (captureBitmap == null || captureBitmap.isRecycled()
                || captureBitmap.getWidth() != targetW || captureBitmap.getHeight() != targetH) {
            captureBitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888);
            captureBitmap.setHasAlpha(false);
            captureCanvas = new Canvas(captureBitmap);
            capturedBitmapSource.setBitmap(null);
            invalidateShaderCache();
        }
        captureBitmap.eraseColor(Color.BLACK);
        tmpBounds.set(drawable.getBounds());
        try {
            drawable.setBounds(0, 0, targetW, targetH);
            drawable.draw(captureCanvas);
            Utilities.stackBlurBitmap(captureBitmap, 12);
            capturedBitmapSource.setBitmap(captureBitmap);
            captureValid = true;
        } catch (Throwable ignored) {
            captureValid = false;
        } finally {
            drawable.setBounds(tmpBounds);
        }
    }

    @Nullable
    public static Bitmap getBlurredWallpaperBitmap() {
        GlassSourceBitmap src = ensureCapturedBitmap();
        if (src == null && primaryBitmapSource != null) {
            Bitmap b = primaryBitmapSource.getBitmap();
            if (b != null && !b.isRecycled()) {
                src = primaryBitmapSource;
            }
        }
        if (src == null) {
            return null;
        }
        Bitmap bmp = src.getBitmap();
        return (bmp != null && !bmp.isRecycled()) ? bmp : null;
    }

    /**
     * Draws an authentic Opexgram Liquid Glass message bubble using the live blurred wallpaper
     * BitmapShader aligned to the bubble's exact screen coordinates.
     *
     * @return true if glass was drawn; false if caller should fall back to standard bubble paint.
     */
    public static boolean drawGlassBubble(
            Canvas canvas,
            Path bubblePath,
            Rect bounds,
            float glassX,
            float glassY,
            int glassWidth,
            int glassHeight,
            int bubbleColor,
            int drawableAlpha,
            boolean isDarkTheme
    ) {
        if (drawableAlpha <= 0) {
            return true;
        }
        Bitmap blurredBitmap = getBlurredWallpaperBitmap();
        if (blurredBitmap == null) {
            return false;
        }

        if (blurredBitmap != lastShaderBitmap || cachedShader == null) {
            lastShaderBitmap = blurredBitmap;
            cachedShader = new BitmapShader(blurredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            glassFillPaint.setShader(cachedShader);
        }

        int w = glassWidth > 0 ? glassWidth : (viewportWidth > 0 ? viewportWidth : AndroidUtilities.displaySize.x);
        int h = glassHeight > 0 ? glassHeight : (viewportHeight > 0 ? viewportHeight : AndroidUtilities.displaySize.y);

        Theme.applyServiceShaderMatrix(blurredBitmap, cachedShader, shaderMatrix, w, h, glassX, glassY);

        // LagramConfig.glassMessagesAlpha is bubble tint opacity (20..100, default 55 -> 45% transparency, matching Opexgram's 100 - transparency).
        int tintAlpha = (Color.alpha(bubbleColor) * Math.max(15, Math.min(95, LagramConfig.glassMessagesAlpha))) / 100;
        int tintColor = ColorUtils.setAlphaComponent(bubbleColor, tintAlpha);
        if (tintColor != lastTintColor) {
            lastTintColor = tintColor;
            cachedColorFilter = Color.alpha(tintColor) != 0
                    ? new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_OVER)
                    : null;
        }
        glassFillPaint.setColorFilter(cachedColorFilter);
        glassFillPaint.setAlpha(drawableAlpha);
        canvas.drawPath(bubblePath, glassFillPaint);

        // Optional subtle Liquid Glass specular highlight along the top edge
        if (LagramConfig.glassHighlights && bounds != null && bounds.height() > 0) {
            if (bounds.top != lastHighlightTop || bounds.bottom != lastHighlightBottom
                    || isDarkTheme != lastHighlightDark || cachedHighlightGradient == null) {
                lastHighlightTop = bounds.top;
                lastHighlightBottom = bounds.bottom;
                lastHighlightDark = isDarkTheme;
                int topHighlight = isDarkTheme ? 0x1EFFFFFF : 0x38FFFFFF;
                cachedHighlightGradient = new LinearGradient(
                        0, bounds.top, 0, bounds.top + Math.min(bounds.height(), AndroidUtilities.dp(44)),
                        topHighlight, 0x00FFFFFF, Shader.TileMode.CLAMP
                );
                glassHighlightPaint.setShader(cachedHighlightGradient);
            }
            glassHighlightPaint.setAlpha(drawableAlpha);
            canvas.drawPath(bubblePath, glassHighlightPaint);
        }

        // Authentic Opexgram 1dp frosted glass rim stroke
        if (LagramConfig.glassMessagesBorder) {
            int borderAlpha = ((isDarkTheme ? 56 : 36) * drawableAlpha) / 255;
            if (borderAlpha > 0) {
                glassBorderPaint.setColor(isDarkTheme ? Color.WHITE : Color.BLACK);
                glassBorderPaint.setAlpha(borderAlpha);
                canvas.drawPath(bubblePath, glassBorderPaint);
            }
        }

        return true;
    }
}
