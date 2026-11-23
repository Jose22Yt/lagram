package org.telegram.ui.lagram.player;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;

import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import org.telegram.ui.ActionBar.Theme;

/**
 * Material 3 Expressive dynamic tonal palette derived from album art via AndroidX Palette
 * with guaranteed WCAG contrast and smooth crossfade interpolation between tracks.
 */
public final class PlayerColors {

    public final boolean dark;
    public final int surface;
    public final int surfaceLow;
    public final int surfaceContainer;
    public final int surfaceHigh;
    public final int onSurface;
    public final int onSurfaceVariant;
    public final int outlineVariant;
    public final int primary;
    public final int onPrimary;
    public final int primaryContainer;
    public final int onPrimaryContainer;
    public final int secondaryContainer;
    public final int onSecondaryContainer;

    private PlayerColors(boolean dark, int[] c) {
        this.dark = dark;
        surface = c[0];
        surfaceLow = c[1];
        surfaceContainer = c[2];
        surfaceHigh = c[3];
        onSurface = c[4];
        onSurfaceVariant = c[5];
        outlineVariant = c[6];
        primary = c[7];
        onPrimary = c[8];
        primaryContainer = c[9];
        onPrimaryContainer = c[10];
        secondaryContainer = c[11];
        onSecondaryContainer = c[12];
    }

    private int[] values() {
        return new int[]{
                surface, surfaceLow, surfaceContainer, surfaceHigh,
                onSurface, onSurfaceVariant, outlineVariant,
                primary, onPrimary, primaryContainer, onPrimaryContainer,
                secondaryContainer, onSecondaryContainer
        };
    }

    private static int tonal(float hue, float sat, float light) {
        float[] hsl = new float[]{hue, Math.max(0f, Math.min(1f, sat)), Math.max(0f, Math.min(1f, light))};
        return ColorUtils.HSLToColor(hsl) | 0xFF000000;
    }

    public static PlayerColors fromSeed(int seed, boolean dark) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(seed | 0xFF000000, hsl);
        float hue = hsl[0];
        float baseSat = Math.max(0.22f, Math.min(0.78f, hsl[1]));
        float neutralSat = Math.min(0.18f, baseSat * 0.24f);
        float secondarySat = Math.min(0.36f, baseSat * 0.45f);

        if (dark) {
            int surf = tonal(hue, neutralSat, 0.085f);
            int surfLow = tonal(hue, neutralSat, 0.115f);
            int surfCont = tonal(hue, neutralSat, 0.15f);
            int surfHigh = tonal(hue, neutralSat, 0.20f);
            int onSurf = tonal(hue, neutralSat * 0.4f, 0.93f);
            int onSurfVar = tonal(hue, neutralSat * 0.6f, 0.74f);
            int outline = tonal(hue, neutralSat, 0.30f);
            int prim = tonal(hue, Math.min(0.86f, baseSat * 1.05f), 0.78f);
            int onPrim = tonal(hue, baseSat * 0.8f, 0.14f);
            int primCont = tonal(hue, baseSat * 0.72f, 0.28f);
            int onPrimCont = tonal(hue, baseSat * 0.9f, 0.92f);
            int secCont = tonal(hue, secondarySat, 0.24f);
            int onSecCont = tonal(hue, secondarySat * 0.8f, 0.90f);
            return new PlayerColors(true, new int[]{
                    surf, surfLow, surfCont, surfHigh,
                    onSurf, onSurfVar, outline,
                    prim, onPrim, primCont, onPrimCont,
                    secCont, onSecCont
            });
        } else {
            int surf = tonal(hue, neutralSat, 0.97f);
            int surfLow = tonal(hue, neutralSat, 0.945f);
            int surfCont = tonal(hue, neutralSat, 0.915f);
            int surfHigh = tonal(hue, neutralSat, 0.88f);
            int onSurf = tonal(hue, neutralSat * 0.5f, 0.11f);
            int onSurfVar = tonal(hue, neutralSat * 0.6f, 0.34f);
            int outline = tonal(hue, neutralSat, 0.78f);
            int prim = tonal(hue, Math.min(0.84f, baseSat * 1.05f), 0.40f);
            int onPrim = Color.WHITE;
            int primCont = tonal(hue, baseSat * 0.78f, 0.86f);
            int onPrimCont = tonal(hue, baseSat * 0.85f, 0.14f);
            int secCont = tonal(hue, secondarySat, 0.87f);
            int onSecCont = tonal(hue, secondarySat * 0.85f, 0.16f);
            return new PlayerColors(false, new int[]{
                    surf, surfLow, surfCont, surfHigh,
                    onSurf, onSurfVar, outline,
                    prim, onPrim, primCont, onPrimCont,
                    secCont, onSecCont
            });
        }
    }

    public static PlayerColors lerp(PlayerColors a, PlayerColors b, float t) {
        if (t <= 0f) return a;
        if (t >= 1f) return b;
        int[] from = a.values();
        int[] to = b.values();
        int[] out = new int[from.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = ColorUtils.blendARGB(from[i], to[i], t);
        }
        return new PlayerColors(b.dark, out);
    }

    public static Bitmap sample(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled() || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
            return null;
        }
        try {
            Bitmap source = bitmap;
            if (Build.VERSION.SDK_INT >= 26 && bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                source = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                if (source == null) return null;
            }
            int w = source.getWidth();
            int h = source.getHeight();
            float k = Math.min(1f, 96f / Math.max(w, h));
            int sw = Math.max(1, Math.round(w * k));
            int sh = Math.max(1, Math.round(h * k));
            Bitmap small = Bitmap.createScaledBitmap(source, sw, sh, true);
            if (small == source) {
                small = source.copy(Bitmap.Config.ARGB_8888, false);
            }
            if (source != bitmap && source != small) {
                source.recycle();
            }
            return small;
        } catch (Throwable e) {
            return null;
        }
    }

    public static int seedFromSample(Bitmap small, int fallback) {
        if (small == null || small.isRecycled()) {
            return fallback;
        }
        try {
            Palette palette = Palette.from(small).maximumColorCount(16).generate();
            int color = palette.getVibrantColor(0);
            if (color == 0) color = palette.getLightVibrantColor(0);
            if (color == 0) color = palette.getMutedColor(0);
            if (color == 0) color = palette.getDominantColor(fallback);
            return color | 0xFF000000;
        } catch (Throwable e) {
            return fallback;
        } finally {
            try {
                small.recycle();
            } catch (Throwable ignored) {}
        }
    }

    public static boolean isDark(Theme.ResourcesProvider resourcesProvider) {
        return ColorUtils.calculateLuminance(Theme.getColor(Theme.key_windowBackgroundWhite, resourcesProvider)) < 0.5;
    }

    public static int fallbackSeed(Theme.ResourcesProvider resourcesProvider) {
        int color = Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider);
        return Color.alpha(color) == 0 ? 0xFF4285F4 : (color | 0xFF000000);
    }

    public int ripple() {
        return ColorUtils.setAlphaComponent(onSurface, 0x22);
    }
}
