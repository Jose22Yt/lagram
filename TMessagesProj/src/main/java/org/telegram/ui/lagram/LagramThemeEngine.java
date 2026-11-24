package org.telegram.ui.lagram;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.Theme;

/**
 * Visual engine for Lagram: Graphite fragment card palette, Material 3 accent palettes,
 * real AMOLED pure black overrides, avatar geometry, typography, animation scaling,
 * haptics, and link/phone utilities.
 */
public final class LagramThemeEngine {

    // Graphite palette for settings fragment cards
    public static final int GRAPHITE_BG_DARK = 0xFF15171B;
    public static final int GRAPHITE_BG_AMOLED = 0xFF000000;
    public static final int GRAPHITE_BG_LIGHT = 0xFFF1F3F6;

    public static final int GRAPHITE_CARD_DARK = 0xFF252830;
    public static final int GRAPHITE_CARD_AMOLED = 0xFF1B1D23;
    public static final int GRAPHITE_CARD_LIGHT = 0xFF2E323B;

    public static final int GRAPHITE_CARD_ELEVATED = 0xFF313540;
    public static final int GRAPHITE_CARD_STROKE_DARK = 0xFF3A3F4B;
    public static final int GRAPHITE_CARD_STROKE_AMOLED = 0xFF2B2E38;
    public static final int GRAPHITE_CARD_STROKE_LIGHT = 0xFF3E434F;

    public static final int GRAPHITE_TEXT_PRIMARY = 0xFFF2F4F8;
    public static final int GRAPHITE_TEXT_SECONDARY = 0xFFA7B0C0;
    public static final int GRAPHITE_DIVIDER = 0xFF343844;

    public static final class AccentColors {
        public final int primary;
        public final int primaryDark;
        public final int containerDark;
        public final int containerLight;
        public final int onPrimary;
        public final int outgoingBubbleDark;
        public final int outgoingBubbleLight;

        public AccentColors(int primary, int primaryDark, int containerDark, int containerLight, int onPrimary, int outgoingBubbleDark, int outgoingBubbleLight) {
            this.primary = primary;
            this.primaryDark = primaryDark;
            this.containerDark = containerDark;
            this.containerLight = containerLight;
            this.onPrimary = onPrimary;
            this.outgoingBubbleDark = outgoingBubbleDark;
            this.outgoingBubbleLight = outgoingBubbleLight;
        }
    }

    private LagramThemeEngine() {
    }

    public static int getSettingsBackgroundColor(boolean isDark) {
        LagramConfig.ensureLoaded();
        if (LagramConfig.amoledPureBlack && isDark) {
            return GRAPHITE_BG_AMOLED;
        }
        return isDark ? GRAPHITE_BG_DARK : GRAPHITE_BG_LIGHT;
    }

    /**
     * Graphite surface color used across all Lagram settings fragment cards.
     */
    public static int getGraphiteCardColor(boolean isDark) {
        LagramConfig.ensureLoaded();
        if (LagramConfig.amoledPureBlack && isDark) {
            return GRAPHITE_CARD_AMOLED;
        }
        return isDark ? GRAPHITE_CARD_DARK : GRAPHITE_CARD_LIGHT;
    }

    public static int getGraphiteCardStrokeColor(boolean isDark) {
        LagramConfig.ensureLoaded();
        if (LagramConfig.amoledPureBlack && isDark) {
            return GRAPHITE_CARD_STROKE_AMOLED;
        }
        return isDark ? GRAPHITE_CARD_STROKE_DARK : GRAPHITE_CARD_STROKE_LIGHT;
    }

    public static int getGraphiteTextPrimary() {
        return GRAPHITE_TEXT_PRIMARY;
    }

    public static int getGraphiteTextSecondary() {
        return GRAPHITE_TEXT_SECONDARY;
    }

    public static int getGraphiteDivider() {
        return GRAPHITE_DIVIDER;
    }

    // Cached Material 3 accent palettes (zero allocations on 120 Hz hot paths)
    private static final AccentColors PALETTE_BLUE = new AccentColors(0xFF3B8BFF, 0xFF256FE0, 0xFF182C4A, 0xFFDCEBFF, 0xFFFFFFFF, 0xFF1E447A, 0xFFD9EAFF);
    private static final AccentColors PALETTE_EMERALD = new AccentColors(0xFF22C58B, 0xFF169C6B, 0xFF15382C, 0xFFD9F7EC, 0xFF062117, 0xFF14543E, 0xFFD8F7EA);
    private static final AccentColors PALETTE_VIOLET = new AccentColors(0xFF8B6CFF, 0xFF6B4CE6, 0xFF29214A, 0xFFECE6FF, 0xFFFFFFFF, 0xFF3E2D7A, 0xFFEAE3FF);
    private static final AccentColors PALETTE_AMBER = new AccentColors(0xFFF5A623, 0xFFD4840B, 0xFF3E2D13, 0xFFFFF0D4, 0xFF221600, 0xFF5D4014, 0xFFFFEED0);
    private static final AccentColors PALETTE_CRIMSON = new AccentColors(0xFFE54D66, 0xFFC2324B, 0xFF401B23, 0xFFFFE2E7, 0xFFFFFFFF, 0xFF662230, 0xFFFFDFE5);
    private static final AccentColors PALETTE_MONO = new AccentColors(0xFFE4E7EC, 0xFFB8C0CC, 0xFF262930, 0xFFE6E9EE, 0xFF101216, 0xFF2B2F38, 0xFFE9ECF1);
    private static volatile AccentColors cachedMonetPalette;
    private static final Typeface[][] cachedTypefaces = new Typeface[5][4];

    public static int getChatRowCardColor(boolean isDark) {
        if (LagramConfig.amoledPureBlack && isDark) {
            return 0xFF14161B;
        }
        return isDark ? 0xFF21242C : 0xFFF4F6FA;
    }

    public static AccentColors getAccentColors() {
        LagramConfig.ensureLoaded();
        return getAccentColorsForPalette(LagramConfig.accentPalette);
    }

    public static void invalidateMonetCache() {
        cachedMonetPalette = null;
    }

    public static AccentColors getAccentColorsForPalette(int palette) {
        switch (palette) {
            case LagramConfig.ACCENT_SYSTEM_MONET: {
                AccentColors monet = cachedMonetPalette;
                if (monet == null) {
                    int monetPrimary = resolveSystemMonetColor(0xFF4C8DF6);
                    int darkContainer = blendColor(monetPrimary, 0xFF141820, 0.72f);
                    int lightContainer = blendColor(monetPrimary, 0xFFFFFFFF, 0.80f);
                    int outDark = blendColor(monetPrimary, 0xFF12151C, 0.45f);
                    int outLight = blendColor(monetPrimary, 0xFFFFFFFF, 0.78f);
                    monet = new AccentColors(monetPrimary, blendColor(monetPrimary, 0xFF000000, 0.18f), darkContainer, lightContainer, 0xFFFFFFFF, outDark, outLight);
                    cachedMonetPalette = monet;
                }
                return monet;
            }
            case LagramConfig.ACCENT_EMERALD:
                return PALETTE_EMERALD;
            case LagramConfig.ACCENT_VIOLET:
                return PALETTE_VIOLET;
            case LagramConfig.ACCENT_AMBER:
                return PALETTE_AMBER;
            case LagramConfig.ACCENT_CRIMSON:
                return PALETTE_CRIMSON;
            case LagramConfig.ACCENT_AMOLED_MONO:
                return PALETTE_MONO;
            case LagramConfig.ACCENT_LAGRAM_BLUE:
            default:
                return PALETTE_BLUE;
        }
    }

    private static int resolveSystemMonetColor(int fallback) {
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                Context ctx = ApplicationLoader.applicationContext;
                if (ctx != null) {
                    return ctx.getColor(android.R.color.system_accent1_400);
                }
            } catch (Throwable ignored) {
            }
        }
        return fallback;
    }

    /**
     * Applies Lagram AMOLED Pure Black and M3 Accent overrides on top of Telegram's Theme color table.
     * Fast-path exits immediately with zero allocations when defaults are active.
     */
    public static int resolveColorOverride(int key, int baseColor, boolean isDark) {
        if (!LagramConfig.amoledPureBlack && LagramConfig.accentPalette == LagramConfig.ACCENT_LAGRAM_BLUE) {
            return baseColor;
        }
        boolean isAmoledBgKey = key == Theme.key_windowBackgroundWhite
                || key == Theme.key_windowBackgroundGray
                || key == Theme.key_chat_wallpaper
                || key == Theme.key_actionBarDefault
                || key == Theme.key_actionBarDefaultArchived;
        boolean isAccentKey = key == Theme.key_chats_unreadCounter
                || key == Theme.key_switchTrackChecked
                || key == Theme.key_checkboxCheck
                || key == Theme.key_chats_actionBackground;
        boolean isAccentPressedKey = key == Theme.key_chats_actionPressedBackground;
        boolean isOutBubbleKey = key == Theme.key_chat_outBubble;
        return resolveColorOverride(isAmoledBgKey, isAccentKey, isAccentPressedKey, isOutBubbleKey, baseColor, isDark);
    }

    public static int resolveColorOverride(boolean isAmoledBgKey, boolean isAccentKey, boolean isAccentPressedKey, boolean isOutBubbleKey, int baseColor, boolean isDark) {
        if (LagramConfig.amoledPureBlack && isDark && isAmoledBgKey) {
            return 0xFF000000;
        }

        // Only override accent keys when user chose a non-default palette,
        // or when AMOLED pure black is actively rendering in dark mode.
        if (LagramConfig.accentPalette != LagramConfig.ACCENT_LAGRAM_BLUE || (LagramConfig.amoledPureBlack && isDark)) {
            AccentColors accent = getAccentColorsForPalette(LagramConfig.accentPalette);
            if (isAccentKey) {
                return accent.primary;
            }
            if (isAccentPressedKey) {
                return accent.primaryDark;
            }
            if (isOutBubbleKey) {
                return isDark ? accent.outgoingBubbleDark : accent.outgoingBubbleLight;
            }
        }
        return baseColor;
    }

    public static int computeAvatarRadiusDp() {
        int maxHalf = Math.max(1, LagramConfig.avatarSizeDp / 2);
        if (LagramConfig.avatarShape == LagramConfig.AVATAR_SHAPE_CIRCLE) {
            return maxHalf;
        }
        return Math.min(maxHalf, Math.max(0, LagramConfig.avatarCornerRadius));
    }

    public static int computeAvatarRadiusPx(float density) {
        return Math.round(computeAvatarRadiusDp() * density);
    }

    public static int getMessageVerticalSpacingExtraDp() {
        switch (LagramConfig.messageVerticalSpacing) {
            case LagramConfig.SPACING_COMPACT:
                return 0;
            case LagramConfig.SPACING_COMFORTABLE:
                return 6;
            case LagramConfig.SPACING_NORMAL:
            default:
                return 2;
        }
    }

    public static float getStickerScaleFactor() {
        return LagramConfig.stickerSizePercent / 100f;
    }

    public static float getTextScaleFactor() {
        return LagramConfig.textScalePercent / 100f;
    }

    public static float getAnimationDurationScale() {
        switch (LagramConfig.animationPreset) {
            case LagramConfig.ANIM_SNAPPY:
                return 0.6f;
            case LagramConfig.ANIM_NONE:
                return 0.0f;
            case LagramConfig.ANIM_FLUID_120HZ_M3:
            default:
                return 1.0f;
        }
    }

    public static long scaleDuration(long baseMs) {
        return Math.max(0L, Math.round(baseMs * getAnimationDurationScale()));
    }

    public static boolean performHaptic(View view, int feedbackConstant) {
        if (!LagramConfig.hapticFeedbackEnabled || view == null) {
            return false;
        }
        try {
            return view.performHapticFeedback(feedbackConstant);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static String getFontFamilySystemName(int family) {
        switch (family) {
            case LagramConfig.FONT_ROUNDED:
                return "casual";
            case LagramConfig.FONT_SANS_MODERN:
                return "sans-serif-condensed";
            case LagramConfig.FONT_SERIF:
                return "serif";
            case LagramConfig.FONT_MONO:
                return "monospace";
            case LagramConfig.FONT_SYSTEM:
            default:
                return null;
        }
    }

    public static Typeface resolveCustomTypeface(String assetPath) {
        int family = LagramConfig.fontFamily;
        if (family == LagramConfig.FONT_SYSTEM || family < 0 || family >= cachedTypefaces.length) {
            return null;
        }
        boolean bold = assetPath != null && (assetPath.contains("medium") || assetPath.contains("bold"));
        boolean italic = assetPath != null && assetPath.contains("italic");
        int style = (bold && italic) ? Typeface.BOLD_ITALIC : (bold ? Typeface.BOLD : (italic ? Typeface.ITALIC : Typeface.NORMAL));
        Typeface cached = cachedTypefaces[family][style];
        if (cached != null) {
            return cached;
        }
        Typeface created;
        switch (family) {
            case LagramConfig.FONT_SERIF:
                created = Typeface.create(Typeface.SERIF, style);
                break;
            case LagramConfig.FONT_MONO:
                created = Typeface.create(Typeface.MONOSPACE, style);
                break;
            case LagramConfig.FONT_ROUNDED:
                created = Typeface.create("casual", style);
                break;
            case LagramConfig.FONT_SANS_MODERN:
                created = Typeface.create("sans-serif-condensed", style);
                break;
            default:
                return null;
        }
        cachedTypefaces[family][style] = created;
        return created;
    }

    /**
     * Formats original + translated text side-by-side when parallel translation is enabled.
     */
    public static String buildParallelTranslatedText(String original, String translated) {
        if (translated == null || translated.isEmpty()) {
            return original != null ? original : "";
        }
        LagramConfig.ensureLoaded();
        if (!LagramConfig.parallelTranslatorEnabled || original == null || original.isEmpty() || original.equals(translated)) {
            return translated;
        }
        String separator = "\n──────────\n";
        if (translated.startsWith(original + separator)) {
            return translated;
        }
        return original + separator + translated;
    }

    /**
     * Strips tracking query parameters from Telegram and web URLs when clean link copy is enabled,
     * preserving any URL #fragment anchor.
     */
    public static String cleanTelegramLink(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        LagramConfig.ensureLoaded();
        if (!LagramConfig.copyCleanLink) {
            return url;
        }
        int hashIdx = url.indexOf('#');
        String fragment = hashIdx >= 0 ? url.substring(hashIdx) : "";
        String withoutFragment = hashIdx >= 0 ? url.substring(0, hashIdx) : url;

        int qIdx = withoutFragment.indexOf('?');
        if (qIdx < 0) {
            return url;
        }
        String base = withoutFragment.substring(0, qIdx);
        String query = withoutFragment.substring(qIdx + 1);
        String[] params = query.split("&");
        StringBuilder kept = new StringBuilder();
        for (String param : params) {
            if (param.isEmpty()) {
                continue;
            }
            String lower = param.toLowerCase();
            if (lower.equals("single")
                    || lower.startsWith("utm_")
                    || lower.startsWith("fbclid=")
                    || lower.startsWith("gclid=")
                    || lower.startsWith("si=")
                    || lower.startsWith("igshid=")) {
                continue;
            }
            if (kept.length() > 0) {
                kept.append('&');
            }
            kept.append(param);
        }
        return (kept.length() > 0 ? base + "?" + kept : base) + fragment;
    }

    public static String maskPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }
        LagramConfig.ensureLoaded();
        if (!LagramConfig.hidePhoneNumber) {
            return phone;
        }
        return "••••••••••";
    }

    public static int blendColor(int color1, int color2, float ratio) {
        float inverse = 1f - ratio;
        int a = Math.round(Color.alpha(color1) * inverse + Color.alpha(color2) * ratio);
        int r = Math.round(Color.red(color1) * inverse + Color.red(color2) * ratio);
        int g = Math.round(Color.green(color1) * inverse + Color.green(color2) * ratio);
        int b = Math.round(Color.blue(color1) * inverse + Color.blue(color2) * ratio);
        return Color.argb(a, r, g, b);
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((Math.max(0, Math.min(255, alpha))) << 24);
    }
}
