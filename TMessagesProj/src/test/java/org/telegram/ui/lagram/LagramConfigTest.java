package org.telegram.ui.lagram;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LagramConfigTest {

    private LagramConfig.InMemoryStorage storage;

    @Before
    public void setUp() {
        storage = new LagramConfig.InMemoryStorage();
        LagramConfig.setStorageForTesting(storage);
    }

    @Test
    public void defaultsAreValidAndLiquidGlassIsOffByDefault() {
        assertFalse(LagramConfig.isLiquidGlassEnabled());
        assertEquals(LagramConfig.CHAT_LIST_M3_CARDS, LagramConfig.chatListStyle);
        assertEquals(LagramConfig.ANIM_FLUID_120HZ_M3, LagramConfig.animationPreset);
        assertEquals(LagramConfig.INPUT_BAR_IOS_3_PILLS, LagramConfig.inputBarStyle);
        assertEquals(17, LagramConfig.bubbleCornerRadius);
        assertTrue(LagramConfig.hapticFeedbackEnabled);
        assertTrue(LagramConfig.bottomNavigationEnabled);
        assertFalse(LagramConfig.amoledPureBlack);
    }

    @Test
    public void sliderValuesAreClampedToSafeBounds() {
        LagramConfig.setAvatarCornerRadius(-10);
        assertEquals(LagramConfig.MIN_AVATAR_RADIUS_DP, LagramConfig.avatarCornerRadius);
        LagramConfig.setAvatarCornerRadius(999);
        assertEquals(LagramConfig.MAX_AVATAR_RADIUS_DP, LagramConfig.avatarCornerRadius);

        LagramConfig.setAvatarSizeDp(10);
        assertEquals(LagramConfig.MIN_AVATAR_SIZE_DP, LagramConfig.avatarSizeDp);
        LagramConfig.setAvatarSizeDp(120);
        assertEquals(LagramConfig.MAX_AVATAR_SIZE_DP, LagramConfig.avatarSizeDp);

        LagramConfig.setBubbleCornerRadius(0);
        assertEquals(LagramConfig.MIN_BUBBLE_RADIUS_DP, LagramConfig.bubbleCornerRadius);
        LagramConfig.setBubbleCornerRadius(80);
        assertEquals(LagramConfig.MAX_BUBBLE_RADIUS_DP, LagramConfig.bubbleCornerRadius);

        LagramConfig.setStickerSizePercent(10);
        assertEquals(LagramConfig.MIN_STICKER_SIZE_PERCENT, LagramConfig.stickerSizePercent);
        LagramConfig.setStickerSizePercent(300);
        assertEquals(LagramConfig.MAX_STICKER_SIZE_PERCENT, LagramConfig.stickerSizePercent);

        LagramConfig.setTextScalePercent(20);
        assertEquals(LagramConfig.MIN_TEXT_SCALE, LagramConfig.textScalePercent);
        LagramConfig.setTextScalePercent(250);
        assertEquals(LagramConfig.MAX_TEXT_SCALE, LagramConfig.textScalePercent);
    }

    @Test
    public void jsonExportAndImportRoundTripAllCategories() {
        LagramConfig.setChatListStyle(LagramConfig.CHAT_LIST_MINIMAL_SPACIOUS);
        LagramConfig.setAmoledPureBlack(true);
        LagramConfig.setAccentPalette(LagramConfig.ACCENT_EMERALD);
        LagramConfig.setAvatarShape(LagramConfig.AVATAR_SHAPE_SQUIRCLE);
        LagramConfig.setAvatarCornerRadius(14);
        LagramConfig.setBubbleCornerRadius(22);
        LagramConfig.setBubbleTailStyle(LagramConfig.TAIL_MODERN_NONE);
        LagramConfig.setInputBarStyle(LagramConfig.INPUT_BAR_M3_UNIFIED);
        LagramConfig.setDoubleTapAction(LagramConfig.DOUBLE_TAP_REPLY);
        LagramConfig.setPlayerClassicMode(true);
        LagramConfig.setParallelTranslatorEnabled(true);
        LagramConfig.setHidePhoneNumber(true);

        String exported = LagramConfig.exportToJson();
        assertTrue(exported.contains("\"amoledPureBlack\": true"));
        assertTrue(exported.contains("\"bubbleCornerRadius\": 22"));

        // Reset into fresh storage and import
        LagramConfig.setStorageForTesting(new LagramConfig.InMemoryStorage());
        assertFalse(LagramConfig.amoledPureBlack);
        assertEquals(17, LagramConfig.bubbleCornerRadius);

        assertTrue(LagramConfig.importFromJson(exported));
        assertEquals(LagramConfig.CHAT_LIST_MINIMAL_SPACIOUS, LagramConfig.chatListStyle);
        assertTrue(LagramConfig.amoledPureBlack);
        assertEquals(LagramConfig.ACCENT_EMERALD, LagramConfig.accentPalette);
        assertEquals(LagramConfig.AVATAR_SHAPE_SQUIRCLE, LagramConfig.avatarShape);
        assertEquals(14, LagramConfig.avatarCornerRadius);
        assertEquals(22, LagramConfig.bubbleCornerRadius);
        assertEquals(LagramConfig.TAIL_MODERN_NONE, LagramConfig.bubbleTailStyle);
        assertEquals(LagramConfig.INPUT_BAR_M3_UNIFIED, LagramConfig.inputBarStyle);
        assertEquals(LagramConfig.DOUBLE_TAP_REPLY, LagramConfig.doubleTapAction);
        assertTrue(LagramConfig.playerClassicMode);
        assertTrue(LagramConfig.parallelTranslatorEnabled);
        assertTrue(LagramConfig.hidePhoneNumber);
    }

    @Test
    public void invalidJsonIsRejectedSafely() {
        assertFalse(LagramConfig.importFromJson(null));
        assertFalse(LagramConfig.importFromJson(""));
        assertFalse(LagramConfig.importFromJson("{}"));
        assertFalse(LagramConfig.importFromJson("not-json"));
    }

    @Test
    public void resetCategoryOnlyResetsSelectedCategory() {
        LagramConfig.setAmoledPureBlack(true);
        LagramConfig.setBubbleCornerRadius(23);
        LagramConfig.setHidePhoneNumber(true);

        LagramConfig.resetCategory(LagramConfig.CATEGORY_CHATS);

        assertEquals(17, LagramConfig.bubbleCornerRadius);
        assertTrue(LagramConfig.amoledPureBlack);
        assertTrue(LagramConfig.hidePhoneNumber);
    }

    @Test
    public void configListenerReceivesUpdates() {
        List<String> events = new ArrayList<>();
        LagramConfig.ConfigListener listener = events::add;
        LagramConfig.addListener(listener);
        try {
            LagramConfig.setAmoledPureBlack(true);
            LagramConfig.setDoubleTapAction(LagramConfig.DOUBLE_TAP_COPY);
            assertTrue(events.contains("amoledPureBlack"));
            assertTrue(events.contains("doubleTapAction"));
        } finally {
            LagramConfig.removeListener(listener);
        }
    }

    @Test
    public void uploadQualityHelpersResolveExpectedTargets() {
        LagramConfig.setPhotoUploadQuality(LagramConfig.PHOTO_QUALITY_STANDARD);
        assertEquals(1280, LagramConfig.getPhotoUploadMaxDimension(false));
        assertEquals(2560, LagramConfig.getPhotoUploadMaxDimension(true));

        LagramConfig.setPhotoUploadQuality(LagramConfig.PHOTO_QUALITY_HIGH);
        assertEquals(1920, LagramConfig.getPhotoUploadMaxDimension(false));

        LagramConfig.setPhotoUploadQuality(LagramConfig.PHOTO_QUALITY_ULTRA);
        assertEquals(2560, LagramConfig.getPhotoUploadMaxDimension(false));

        LagramConfig.setVideoUploadQuality(LagramConfig.VIDEO_QUALITY_STANDARD);
        assertEquals(1, LagramConfig.getVideoUploadCompressionTarget(4));

        LagramConfig.setVideoUploadQuality(LagramConfig.VIDEO_QUALITY_HD);
        assertEquals(2, LagramConfig.getVideoUploadCompressionTarget(4));

        LagramConfig.setVideoUploadQuality(LagramConfig.VIDEO_QUALITY_FHD);
        assertEquals(3, LagramConfig.getVideoUploadCompressionTarget(4));
        assertEquals(0, LagramConfig.getVideoUploadCompressionTarget(1));
    }

    @Test
    public void resetAndImportBatchListenerNotifications() {
        List<String> events = new ArrayList<>();
        LagramConfig.ConfigListener listener = events::add;
        LagramConfig.addListener(listener);
        try {
            LagramConfig.resetCategory(LagramConfig.CATEGORY_APPEARANCE);
            assertEquals(1, events.size());
            assertEquals("reset:" + LagramConfig.CATEGORY_APPEARANCE, events.get(0));

            events.clear();
            LagramConfig.resetAll();
            assertEquals(1, events.size());
            assertEquals("reset:all", events.get(0));

            events.clear();
            String json = LagramConfig.exportToJson();
            assertTrue(LagramConfig.importFromJson(json));
            assertEquals(1, events.size());
            assertEquals("import", events.get(0));
        } finally {
            LagramConfig.removeListener(listener);
        }
    }

    @Test
    public void drawerAndNavigationOptionsPersistAcrossJsonRoundTrip() {
        LagramConfig.setDrawerHeaderStyle(LagramConfig.DRAWER_HEADER_MINIMAL);
        LagramConfig.setDrawerShowNewGroup(false);
        LagramConfig.setDrawerShowContacts(false);
        LagramConfig.setDrawerShowCalls(false);
        LagramConfig.setDrawerShowSavedMessages(false);
        LagramConfig.setDrawerShowSettings(false);
        LagramConfig.setBottomNavigationEnabled(false);
        LagramConfig.setProfileBannerM3Enabled(false);

        String exported = LagramConfig.exportToJson();
        LagramConfig.setStorageForTesting(new LagramConfig.InMemoryStorage());
        assertTrue(LagramConfig.importFromJson(exported));

        assertEquals(LagramConfig.DRAWER_HEADER_MINIMAL, LagramConfig.drawerHeaderStyle);
        assertFalse(LagramConfig.drawerShowNewGroup);
        assertFalse(LagramConfig.drawerShowContacts);
        assertFalse(LagramConfig.drawerShowCalls);
        assertFalse(LagramConfig.drawerShowSavedMessages);
        assertFalse(LagramConfig.drawerShowSettings);
        assertFalse(LagramConfig.bottomNavigationEnabled);
        assertFalse(LagramConfig.profileBannerM3Enabled);
    }
}
