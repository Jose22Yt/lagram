package org.telegram.ui.lagram;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LagramThemeEngineTest {

    @Before
    public void setUp() {
        LagramConfig.setStorageForTesting(new LagramConfig.InMemoryStorage());
    }

    @Test
    public void graphiteCardColorsMatchRequestedPalette() {
        assertEquals(LagramThemeEngine.GRAPHITE_CARD_DARK, LagramThemeEngine.getGraphiteCardColor(true));
        assertEquals(LagramThemeEngine.GRAPHITE_CARD_LIGHT, LagramThemeEngine.getGraphiteCardColor(false));

        LagramConfig.setAmoledPureBlack(true);
        assertEquals(LagramThemeEngine.GRAPHITE_CARD_AMOLED, LagramThemeEngine.getGraphiteCardColor(true));
        assertEquals(LagramThemeEngine.GRAPHITE_BG_AMOLED, LagramThemeEngine.getSettingsBackgroundColor(true));
    }

    @Test
    public void amoledPureBlackOverridesBackgroundKeysToTrueBlack() {
        LagramConfig.setAmoledPureBlack(true);
        assertEquals(0xFF000000, LagramThemeEngine.resolveColorOverride(true, false, false, false, 0xFF1F242D, true));
        // In light mode, AMOLED pure black does not turn light theme black
        assertEquals(0xFFFFFFFF, LagramThemeEngine.resolveColorOverride(true, false, false, false, 0xFFFFFFFF, false));

        // Accent override check when Emerald palette is selected
        LagramConfig.setAccentPalette(LagramConfig.ACCENT_EMERALD);
        LagramThemeEngine.AccentColors emerald = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.ACCENT_EMERALD);
        assertEquals(emerald.primary, LagramThemeEngine.resolveColorOverride(false, true, false, false, 0xFF3B8BFF, true));
        assertEquals(emerald.primaryDark, LagramThemeEngine.resolveColorOverride(false, false, true, false, 0xFF256FE0, true));
        assertEquals(emerald.outgoingBubbleDark, LagramThemeEngine.resolveColorOverride(false, false, false, true, 0xFF1E447A, true));
        assertEquals(emerald.outgoingBubbleLight, LagramThemeEngine.resolveColorOverride(false, false, false, true, 0xFFD9EAFF, false));
    }

    @Test
    public void dynamicAccentPalettesAreDistinctAndValid() {
        LagramThemeEngine.AccentColors blue = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.ACCENT_LAGRAM_BLUE);
        LagramThemeEngine.AccentColors emerald = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.ACCENT_EMERALD);
        LagramThemeEngine.AccentColors violet = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.ACCENT_VIOLET);
        LagramThemeEngine.AccentColors amber = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.ACCENT_AMBER);
        LagramThemeEngine.AccentColors crimson = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.ACCENT_CRIMSON);
        LagramThemeEngine.AccentColors mono = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.ACCENT_AMOLED_MONO);

        assertNotEquals(blue.primary, emerald.primary);
        assertNotEquals(emerald.primary, violet.primary);
        assertNotEquals(violet.primary, amber.primary);
        assertNotEquals(amber.primary, crimson.primary);
        assertNotEquals(crimson.primary, mono.primary);
    }

    @Test
    public void avatarGeometryAndMotionScalingWorkCorrectly() {
        LagramConfig.setAvatarSizeDp(52);
        LagramConfig.setAvatarShape(LagramConfig.AVATAR_SHAPE_CIRCLE);
        assertEquals(26, LagramThemeEngine.computeAvatarRadiusDp());

        LagramConfig.setAvatarShape(LagramConfig.AVATAR_SHAPE_ROUNDED_SQUARE);
        assertEquals(10, LagramThemeEngine.computeAvatarRadiusDp());

        LagramConfig.setAnimationPreset(LagramConfig.ANIM_FLUID_120HZ_M3);
        assertEquals(300L, LagramThemeEngine.scaleDuration(300L));

        LagramConfig.setAnimationPreset(LagramConfig.ANIM_SNAPPY);
        assertEquals(180L, LagramThemeEngine.scaleDuration(300L));

        LagramConfig.setAnimationPreset(LagramConfig.ANIM_NONE);
        assertEquals(0L, LagramThemeEngine.scaleDuration(300L));
    }

    @Test
    public void cleanLinkAndPhoneMaskingUtilitiesWorkCorrectly() {
        LagramConfig.setCopyCleanLink(true);
        assertEquals("https://t.me/lagram/42", LagramThemeEngine.cleanTelegramLink("https://t.me/lagram/42?single"));
        assertEquals("https://example.com/post?id=7", LagramThemeEngine.cleanTelegramLink("https://example.com/post?id=7&utm_source=tg&fbclid=123"));

        LagramConfig.setCopyCleanLink(false);
        assertEquals("https://t.me/lagram/42?single", LagramThemeEngine.cleanTelegramLink("https://t.me/lagram/42?single"));

        LagramConfig.setHidePhoneNumber(false);
        assertEquals("+34 612 345 678", LagramThemeEngine.maskPhoneNumber("+34 612 345 678"));
        LagramConfig.setHidePhoneNumber(true);
        assertEquals("••••••••••", LagramThemeEngine.maskPhoneNumber("+34 612 345 678"));
    }

    @Test
    public void cachingAndVisualSafetyChecksPass() {
        // Palette caching avoids per-frame allocations on 120 Hz hot paths
        LagramThemeEngine.AccentColors first = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.ACCENT_VIOLET);
        LagramThemeEngine.AccentColors second = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.ACCENT_VIOLET);
        assertEquals(first, second);

        // Message vertical spacing is always >= 0 so bubbles are never clipped
        LagramConfig.setMessageVerticalSpacing(LagramConfig.SPACING_COMPACT);
        assertEquals(0, LagramThemeEngine.getMessageVerticalSpacingExtraDp());
        LagramConfig.setMessageVerticalSpacing(LagramConfig.SPACING_NORMAL);
        assertEquals(2, LagramThemeEngine.getMessageVerticalSpacingExtraDp());
        LagramConfig.setMessageVerticalSpacing(LagramConfig.SPACING_COMFORTABLE);
        assertEquals(6, LagramThemeEngine.getMessageVerticalSpacingExtraDp());

        // M3 chat row card colors differ properly across Dark, Light, and AMOLED modes
        LagramConfig.setAmoledPureBlack(false);
        assertNotEquals(LagramThemeEngine.getChatRowCardColor(true), LagramThemeEngine.getChatRowCardColor(false));
        LagramConfig.setAmoledPureBlack(true);
        assertEquals(0xFF14161B, LagramThemeEngine.getChatRowCardColor(true));
    }
}
