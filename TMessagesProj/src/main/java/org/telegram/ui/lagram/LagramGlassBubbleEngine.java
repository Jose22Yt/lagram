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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Refined, Async-Optimized Liquid Glass message bubble engine for Lagram.
 *
 * Performance guarantees:
 * - Captures a lightweight 144px-tall wallpaper thumbnail and runs Utilities.stackBlurBitmap()
 *   OFF the UI thread (Utilities.globalQueue), atomically swapping the finished blurred Bitmap.
 * - Zero per-frame Bitmap / Shader / Paint / ColorFilter allocations during scroll.
 * - Supports full customization: opacity, blur intensity (Soft/Medium/Strong), finish/tint mode
 *   (Theme/Crystal/Frost/Smoked), specular liquid highlight, and configurable rim border opacity.
 */
public final class LagramGlassBubbleEngine {

    private static GlassSourceBitmap primaryBitmapSource;
    private static Object currentOwner;
    private static WeakReference<Drawable> wallpaperDrawableRef;

    private static final BitmapChangeTracker bgTracker = new BitmapChangeTracker();
    private static final BitmapChangeTracker patternTracker = new BitmapChangeTracker();
    private static final Rect tmpBounds = new Rect();

    private static Bitmap stagingBitmap;
    private static Canvas stagingCanvas;
    private static volatile Bitmap readyBlurredBitmap;
    private static volatile boolean captureValid;
    private static long lastCaptureTime;
    private static int lastMotionIntensity;
    private static int lastCapturedBlurLevel = -1;
    private static final AtomicBoolean blurInFlight = new AtomicBoolean(false);
    private static final AtomicInteger captureGeneration = new AtomicInteger(0);

    private static int viewportWidth;
    private static int viewportHeight;

    private static final Paint glassFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private static final Paint glassBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint glassHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Matrix shaderMatrix = new Matrix();

    private static Bitmap lastShaderBitmap;
    private static BitmapShader cachedShader;

    // 4-slot tiny cache eliminates PorterDuffColorFilter allocations when alternating incoming/outgoing/selected bubbles
    private static final int[] tintCacheColors = new int[4];
    private static final PorterDuffColorFilter[] tintCacheFilters = new PorterDuffColorFilter[4];
    private static int tintCacheCursor = 0;

    // Normalized [0..1] LinearGradient + Matrix eliminates per-bubble GPU shader uploads on every frame!
    private static final Matrix highlightMatrix = new Matrix();
    private static int lastHighlightMode = Integer.MIN_VALUE;
    private static boolean lastHighlightDark;
    private static LinearGradient cachedHighlightGradient;

    static {
        glassFillPaint.setFilterBitmap(true);
        glassBorderPaint.setStyle(Paint.Style.STROKE);
        glassBorderPaint.setStrokeWidth(AndroidUtilities.dpf2(0.95f));
        glassHighlightPaint.setStyle(Paint.Style.FILL);
    }

    private LagramGlassBubbleEngine() {
    }

    public static void clearCache() {
        captureGeneration.incrementAndGet();
        captureValid = false;
        lastCaptureTime = 0L;
        lastCapturedBlurLevel = -1;
        readyBlurredBitmap = null;
        backBufferBitmap = null;
        backBufferCanvas = null;
        lastFastCheckTime = 0L;
        stagingBitmap = null;
        stagingCanvas = null;
        invalidateShaderCache();
        for (int i = 0; i < tintCacheColors.length; i++) {
            tintCacheColors[i] = 0;
            tintCacheFilters[i] = null;
        }
        cachedHighlightGradient = null;
        lastHighlightMode = Integer.MIN_VALUE;
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
                readyBlurredBitmap = null;
                stagingBitmap = null;
                stagingCanvas = null;
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
                readyBlurredBitmap = null;
                stagingBitmap = null;
                stagingCanvas = null;
                invalidateShaderCache();
            }
        }
    }

    public static void invalidateCapture() {
        captureGeneration.incrementAndGet();
        captureValid = false;
        lastCaptureTime = 0L;
        lastCapturedBlurLevel = -1;
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
        if (readyBlurredBitmap != null && !readyBlurredBitmap.isRecycled()) {
            return true;
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
        if (drawable != null) {
            return drawable;
        }
        return null;
    }

    private static void ensureCapturedBitmap() {
        Drawable capturable = getCapturableDrawable();
        if (capturable == null || viewportWidth <= 0 || viewportHeight <= 0) {
            return;
        }
        int desiredBlurLevel = LagramConfig.glassBlurLevel;
        boolean blurChanged = lastCapturedBlurLevel != desiredBlurLevel;
        if (!blurChanged && captureValid) {
            if (!(capturable instanceof MotionBackgroundDrawable)) {
                // Static wallpaper already blurred at this level: zero-overhead fast path!
                return;
            }
            long now = SystemClock.elapsedRealtime();
            if ((now - lastCaptureTime) < 450L) {
                return;
            }
            lastCaptureTime = now;
        } else {
            lastCaptureTime = SystemClock.elapsedRealtime();
        }

        boolean needsRecapture = !captureValid || blurChanged;
        if (!needsRecapture && capturable instanceof MotionBackgroundDrawable) {
            MotionBackgroundDrawable motion = (MotionBackgroundDrawable) capturable;
            needsRecapture = lastMotionIntensity != motion.getIntensity()
                    || bgTracker.isInvalidated(motion.getBitmap())
                    || patternTracker.isInvalidated(motion.getPatternBitmap());
            if (needsRecapture && motion.getPosAnimationProgress() < 1.0f) {
                return;
            }
        }
        if (!needsRecapture) {
            return;
        }

        if (capturable instanceof MotionBackgroundDrawable) {
            MotionBackgroundDrawable motion = (MotionBackgroundDrawable) capturable;
            bgTracker.set(motion.getBitmap());
            patternTracker.set(motion.getPatternBitmap());
            lastMotionIntensity = motion.getIntensity();
        }
        lastCapturedBlurLevel = desiredBlurLevel;
        scheduleAsyncCapture(capturable, LagramConfig.getGlassBlurRadius());
    }

    private static Bitmap backBufferBitmap;
    private static Canvas backBufferCanvas;
    private static long lastFastCheckTime;

    private static void scheduleAsyncCapture(Drawable drawable, int blurRadius) {
        if (!blurInFlight.compareAndSet(false, true)) {
            return;
        }
        // Lightweight 144px double-buffered thumbnail: ~50KB RAM, 0 per-capture Bitmap allocations!
        int targetH = 144;
        int targetW = Math.max(36, Math.min(256, Math.round((viewportWidth * (float) targetH) / Math.max(1, viewportHeight))));
        Bitmap targetBuf = (backBufferBitmap != null && !backBufferBitmap.isRecycled() && backBufferBitmap != readyBlurredBitmap
                && backBufferBitmap.getWidth() == targetW && backBufferBitmap.getHeight() == targetH)
                ? backBufferBitmap
                : null;
        if (targetBuf == null) {
            targetBuf = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888);
            targetBuf.setHasAlpha(false);
            backBufferBitmap = targetBuf;
            backBufferCanvas = new Canvas(targetBuf);
        } else if (backBufferCanvas == null) {
            backBufferCanvas = new Canvas(targetBuf);
        } else {
            backBufferCanvas.setBitmap(targetBuf);
        }
        targetBuf.eraseColor(Color.BLACK);
        tmpBounds.set(drawable.getBounds());
        try {
            drawable.setBounds(0, 0, targetW, targetH);
            drawable.draw(backBufferCanvas);
        } catch (Throwable ignored) {
            blurInFlight.set(false);
            return;
        } finally {
            drawable.setBounds(tmpBounds);
        }

        final Bitmap finalWork = targetBuf;
        final int gen = captureGeneration.get();
        final int radius = Math.max(2, blurRadius);

        // First frame blurs synchronously at 144px (~0.6ms) so bubbles never flash unblurred;
        // subsequent animated wallpaper refreshes run 100% off the UI thread with zero allocations.
        if (readyBlurredBitmap == null) {
            try {
                Utilities.stackBlurBitmap(finalWork, radius);
                readyBlurredBitmap = finalWork;
                backBufferBitmap = null;
                captureValid = true;
                invalidateShaderCache();
            } catch (Throwable ignored) {
                captureValid = false;
            } finally {
                blurInFlight.set(false);
            }
            return;
        }

        Utilities.globalQueue.postRunnable(() -> {
            try {
                Utilities.stackBlurBitmap(finalWork, radius);
                if (gen == captureGeneration.get()) {
                    AndroidUtilities.runOnUIThread(() -> {
                        if (gen == captureGeneration.get()) {
                            Bitmap prevReady = readyBlurredBitmap;
                            readyBlurredBitmap = finalWork;
                            if (prevReady != null && !prevReady.isRecycled() && prevReady != finalWork) {
                                backBufferBitmap = prevReady;
                            }
                            captureValid = true;
                            invalidateShaderCache();
                        }
                    });
                }
            } catch (Throwable ignored) {
                // Keep previous readyBlurredBitmap intact on error
            } finally {
                blurInFlight.set(false);
            }
        });
    }

    @Nullable
    public static Bitmap getBlurredWallpaperBitmap() {
        long now = SystemClock.elapsedRealtime();
        if (!captureValid || readyBlurredBitmap == null || (now - lastFastCheckTime) >= 120L) {
            lastFastCheckTime = now;
            ensureCapturedBitmap();
        }
        Bitmap ready = readyBlurredBitmap;
        if (ready != null && !ready.isRecycled()) {
            return ready;
        }
        if (primaryBitmapSource != null) {
            Bitmap b = primaryBitmapSource.getBitmap();
            if (b != null && !b.isRecycled()) {
                return b;
            }
        }
        return null;
    }

    private static int resolveTintColor(int themeBubbleColor, boolean isDarkTheme) {
        int alphaPct = Math.max(15, Math.min(95, LagramConfig.glassMessagesAlpha));
        int baseColor;
        switch (LagramConfig.glassTintMode) {
            case LagramConfig.GLASS_TINT_CRYSTAL:
                baseColor = isDarkTheme ? 0xFF1F242D : 0xFFFFFFFF;
                alphaPct = Math.max(12, Math.round(alphaPct * 0.62f));
                break;
            case LagramConfig.GLASS_TINT_FROST:
                baseColor = isDarkTheme ? 0xFFCBD8EA : 0xFFF5F9FF;
                alphaPct = Math.max(18, Math.round(alphaPct * (isDarkTheme ? 0.34f : 0.85f)));
                break;
            case LagramConfig.GLASS_TINT_SMOKED:
                baseColor = 0xFF0C0F14;
                alphaPct = Math.max(24, Math.round(alphaPct * 0.88f));
                break;
            case LagramConfig.GLASS_TINT_THEME:
            default:
                baseColor = themeBubbleColor;
                break;
        }
        int tintAlpha = (Color.alpha(baseColor) * alphaPct) / 100;
        return ColorUtils.setAlphaComponent(baseColor, tintAlpha);
    }

    @Nullable
    private static PorterDuffColorFilter obtainTintFilter(int tintColor) {
        if (Color.alpha(tintColor) == 0) {
            return null;
        }
        for (int i = 0; i < tintCacheColors.length; i++) {
            if (tintCacheColors[i] == tintColor && tintCacheFilters[i] != null) {
                return tintCacheFilters[i];
            }
        }
        PorterDuffColorFilter filter = new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_OVER);
        tintCacheColors[tintCacheCursor] = tintColor;
        tintCacheFilters[tintCacheCursor] = filter;
        tintCacheCursor = (tintCacheCursor + 1) & 3;
        return filter;
    }

    /**
     * Draws a refined, hardware-friendly Liquid Glass message bubble aligned pixel-perfectly to the
     * active chat wallpaper with ZERO per-frame allocations.
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

        int tintColor = resolveTintColor(bubbleColor, isDarkTheme);
        glassFillPaint.setColorFilter(obtainTintFilter(tintColor));
        glassFillPaint.setAlpha(drawableAlpha);
        canvas.drawPath(bubblePath, glassFillPaint);

        // Specular liquid sheen along the top edge using a single reusable GPU LinearGradient + local Matrix
        if (LagramConfig.glassHighlights && bounds != null && bounds.height() > 0) {
            int gradH = Math.min(bounds.height(), AndroidUtilities.dp(46));
            int tintMode = LagramConfig.glassTintMode;
            if (isDarkTheme != lastHighlightDark || tintMode != lastHighlightMode || cachedHighlightGradient == null) {
                lastHighlightDark = isDarkTheme;
                lastHighlightMode = tintMode;
                int topHighlight;
                if (tintMode == LagramConfig.GLASS_TINT_FROST) {
                    topHighlight = isDarkTheme ? 0x36FFFFFF : 0x54FFFFFF;
                } else if (tintMode == LagramConfig.GLASS_TINT_SMOKED) {
                    topHighlight = 0x1AFFFFFF;
                } else {
                    topHighlight = isDarkTheme ? 0x26FFFFFF : 0x42FFFFFF;
                }
                cachedHighlightGradient = new LinearGradient(
                        0f, 0f, 0f, 1f,
                        topHighlight, 0x00FFFFFF, Shader.TileMode.CLAMP
                );
                glassHighlightPaint.setShader(cachedHighlightGradient);
            }
            highlightMatrix.setScale(1f, Math.max(1f, gradH));
            highlightMatrix.postTranslate(0f, bounds.top);
            cachedHighlightGradient.setLocalMatrix(highlightMatrix);
            glassHighlightPaint.setAlpha(drawableAlpha);
            canvas.drawPath(bubblePath, glassHighlightPaint);
        }

        // Configurable frosted glass rim stroke
        if (LagramConfig.glassMessagesBorder && LagramConfig.glassBorderOpacity > 0) {
            float borderScale = LagramConfig.glassBorderOpacity / 100f;
            int maxAlpha = isDarkTheme ? 96 : 64;
            int borderAlpha = Math.round(maxAlpha * borderScale * (drawableAlpha / 255f));
            if (borderAlpha > 0) {
                int rimColor = (LagramConfig.glassTintMode == LagramConfig.GLASS_TINT_FROST || isDarkTheme)
                        ? Color.WHITE
                        : Color.BLACK;
                glassBorderPaint.setColor(rimColor);
                glassBorderPaint.setAlpha(borderAlpha);
                canvas.drawPath(bubblePath, glassBorderPaint);
            }
        }

        return true;
    }
}
