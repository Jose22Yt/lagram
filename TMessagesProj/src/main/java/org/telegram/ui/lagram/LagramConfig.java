package org.telegram.ui.lagram;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LiteMode;
import org.telegram.messenger.SharedConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central persistent configuration for all Lagram UI & customization features.
 * Organized into four categories with JSON export/import and per-category reset.
 */
public final class LagramConfig {

    private static final String PREFS_NAME = "lagram_config_v1";

    // Categories
    public static final String CATEGORY_APPEARANCE = "appearance";
    public static final String CATEGORY_CHATS = "chats";
    public static final String CATEGORY_MEDIA = "media";
    public static final String CATEGORY_PRIVACY = "privacy";

    // 1. Chat List Styles
    public static final int CHAT_LIST_CLASSIC = 0;
    public static final int CHAT_LIST_M3_CARDS = 1;
    public static final int CHAT_LIST_MINIMAL_SPACIOUS = 2;

    // Avatar Shapes
    public static final int AVATAR_SHAPE_CIRCLE = 0;
    public static final int AVATAR_SHAPE_SQUIRCLE = 1;
    public static final int AVATAR_SHAPE_ROUNDED_SQUARE = 2;

    // Folder Tabs Styles
    public static final int FOLDER_TABS_PILL_M3 = 0;
    public static final int FOLDER_TABS_UNDERLINE = 1;
    public static final int FOLDER_TABS_COMPACT_ICONS = 2;

    // Unread Badge Styles
    public static final int UNREAD_BADGE_TONAL_PILL = 0;
    public static final int UNREAD_BADGE_MINIMAL_DOT = 1;
    public static final int UNREAD_BADGE_CLASSIC = 2;

    // Online Status Position
    public static final int ONLINE_POS_BOTTOM_RIGHT = 0;
    public static final int ONLINE_POS_TOP_RIGHT = 1;
    public static final int ONLINE_POS_HIDDEN = 2;

    // Home Header Title Mode
    public static final int TITLE_LAGRAM = 0;
    public static final int TITLE_USERNAME = 1;
    public static final int TITLE_ACTIVE_FOLDER = 2;

    // 2. Bubble Tail Styles
    public static final int TAIL_CLASSIC = 0;
    public static final int TAIL_MODERN_NONE = 1;
    public static final int TAIL_MINIMAL_SHORT = 2;

    // Message Vertical Spacing
    public static final int SPACING_COMPACT = 0;
    public static final int SPACING_NORMAL = 1;
    public static final int SPACING_COMFORTABLE = 2;

    // Bottom Input Bar Styles
    public static final int INPUT_BAR_IOS_3_PILLS = 0;
    public static final int INPUT_BAR_M3_UNIFIED = 1;
    public static final int INPUT_BAR_CLASSIC_TG = 2;

    // Double Tap Actions
    public static final int DOUBLE_TAP_REACT = 0;
    public static final int DOUBLE_TAP_REPLY = 1;
    public static final int DOUBLE_TAP_COPY = 2;
    public static final int DOUBLE_TAP_FORWARD = 3;
    public static final int DOUBLE_TAP_TRANSLATE = 4;
    public static final int DOUBLE_TAP_SAVED = 5;

    // 3. Drawer Header Styles
    public static final int DRAWER_HEADER_DYNAMIC = 0;
    public static final int DRAWER_HEADER_COMPACT = 1;
    public static final int DRAWER_HEADER_MINIMAL = 2;

    // 4. Accent Palettes (Material 3)
    public static final int ACCENT_SYSTEM_MONET = 0;
    public static final int ACCENT_LAGRAM_BLUE = 1;
    public static final int ACCENT_EMERALD = 2;
    public static final int ACCENT_VIOLET = 3;
    public static final int ACCENT_AMBER = 4;
    public static final int ACCENT_CRIMSON = 5;
    public static final int ACCENT_AMOLED_MONO = 6;

    // Fonts
    public static final int FONT_SYSTEM = 0;
    public static final int FONT_ROUNDED = 1;
    public static final int FONT_SANS_MODERN = 2;
    public static final int FONT_SERIF = 3;
    public static final int FONT_MONO = 4;

    // Animation Presets
    public static final int ANIM_FLUID_120HZ_M3 = 0;
    public static final int ANIM_SNAPPY = 1;
    public static final int ANIM_NONE = 2;

    // App Icon Styles
    public static final int ICON_LAGRAM_DEFAULT = 0;
    public static final int ICON_LAGRAM_DARK = 1;
    public static final int ICON_LAGRAM_AMOLED = 2;
    public static final int ICON_LAGRAM_MONET = 3;

    // Media Upload Quality
    public static final int PHOTO_QUALITY_STANDARD = 0;
    public static final int PHOTO_QUALITY_HIGH = 1;
    public static final int PHOTO_QUALITY_ULTRA = 2;

    public static final int VIDEO_QUALITY_STANDARD = 0;
    public static final int VIDEO_QUALITY_HD = 1;
    public static final int VIDEO_QUALITY_FHD = 2;

    // Slider bounds
    public static final int MIN_AVATAR_RADIUS_DP = 0;
    public static final int MAX_AVATAR_RADIUS_DP = 28;
    public static final int MIN_AVATAR_SIZE_DP = 36;
    public static final int MAX_AVATAR_SIZE_DP = 64;
    public static final int MIN_TEXT_SCALE = 85;
    public static final int MAX_TEXT_SCALE = 130;
    public static final int MIN_BUBBLE_RADIUS_DP = 4;
    public static final int MAX_BUBBLE_RADIUS_DP = 24;
    public static final int MIN_STICKER_SIZE_PERCENT = 50;
    public static final int MAX_STICKER_SIZE_PERCENT = 120;
    public static final int MIN_CHAT_HEADER_AVATAR_DP = 32;
    public static final int MAX_CHAT_HEADER_AVATAR_DP = 48;

    public interface Storage {
        int getInt(String key, int defValue);
        boolean getBoolean(String key, boolean defValue);
        void putInt(String key, int value);
        void putBoolean(String key, boolean value);
        void remove(String key);
    }

    public interface ConfigListener {
        void onLagramConfigChanged(String key);
    }

    public static final class InMemoryStorage implements Storage {
        private final Map<String, Object> map = new LinkedHashMap<>();

        @Override
        public int getInt(String key, int defValue) {
            Object v = map.get(key);
            return v instanceof Integer ? (Integer) v : defValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            Object v = map.get(key);
            return v instanceof Boolean ? (Boolean) v : defValue;
        }

        @Override
        public void putInt(String key, int value) {
            map.put(key, value);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            map.put(key, value);
        }

        @Override
        public void remove(String key) {
            map.remove(key);
        }
    }

    private static final class SharedPrefsStorage implements Storage {
        private final SharedPreferences prefs;

        SharedPrefsStorage(SharedPreferences prefs) {
            this.prefs = prefs;
        }

        @Override
        public int getInt(String key, int defValue) {
            return prefs.getInt(key, defValue);
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return prefs.getBoolean(key, defValue);
        }

        @Override
        public void putInt(String key, int value) {
            prefs.edit().putInt(key, value).apply();
        }

        @Override
        public void putBoolean(String key, boolean value) {
            prefs.edit().putBoolean(key, value).apply();
        }

        @Override
        public void remove(String key) {
            prefs.edit().remove(key).apply();
        }
    }

    private static volatile Storage storage;
    private static volatile boolean loaded;
    private static volatile int batchDepth;
    private static final CopyOnWriteArrayList<ConfigListener> listeners = new CopyOnWriteArrayList<>();

    // --- Appearance ---
    public static volatile int chatListStyle = CHAT_LIST_M3_CARDS;
    public static volatile int avatarShape = AVATAR_SHAPE_CIRCLE;
    public static volatile int avatarCornerRadius = 26;
    public static volatile int avatarSizeDp = 52;
    public static volatile int folderTabsStyle = FOLDER_TABS_PILL_M3;
    public static volatile boolean hideAllChatsTab = false;
    public static volatile int unreadBadgeStyle = UNREAD_BADGE_TONAL_PILL;
    public static volatile int onlineStatusPosition = ONLINE_POS_BOTTOM_RIGHT;
    public static volatile int homeHeaderTitleMode = TITLE_LAGRAM;
    public static volatile int accentPalette = ACCENT_LAGRAM_BLUE;
    public static volatile boolean amoledPureBlack = false;
    public static volatile int fontFamily = FONT_SYSTEM;
    public static volatile int textScalePercent = 100;
    public static volatile int animationPreset = ANIM_FLUID_120HZ_M3;
    public static volatile boolean hapticFeedbackEnabled = true;
    public static volatile boolean bottomNavigationEnabled = true;
    public static volatile boolean profileBannerM3Enabled = true;
    public static volatile int drawerHeaderStyle = DRAWER_HEADER_DYNAMIC;
    public static volatile boolean drawerShowNewGroup = true;
    public static volatile boolean drawerShowContacts = true;
    public static volatile boolean drawerShowCalls = true;
    public static volatile boolean drawerShowSavedMessages = true;
    public static volatile boolean drawerShowSettings = true;
    public static volatile int appIconStyle = ICON_LAGRAM_DEFAULT;

    // --- Chats ---
    public static volatile int bubbleCornerRadius = 17;
    public static volatile int bubbleTailStyle = TAIL_CLASSIC;
    public static volatile int messageVerticalSpacing = SPACING_NORMAL;
    public static volatile int stickerSizePercent = 100;
    public static volatile int inputBarStyle = INPUT_BAR_IOS_3_PILLS;
    public static volatile boolean hideInputGiftButton = false;
    public static volatile boolean hideInputBotCommandButton = false;
    public static volatile boolean hideInputQuickAudioVideoButton = false;
    public static volatile int chatHeaderAvatarSizeDp = 42;
    public static volatile boolean chatHeaderShowSubtitle = true;
    public static volatile boolean chatHeaderCleanQuickButtons = true;
    public static volatile int doubleTapAction = DOUBLE_TAP_REACT;
    public static volatile boolean quickForwardNoAuthor = true;
    public static volatile boolean copyCleanLink = true;

    // --- Media & Player ---
    public static volatile boolean playerWaveAnimation = true;
    public static volatile boolean playerDynamicCoverPalette = true;
    public static volatile boolean playerClassicMode = false;
    public static volatile boolean videoGesturesEnabled = true;
    public static volatile int photoUploadQuality = PHOTO_QUALITY_HIGH;
    public static volatile int videoUploadQuality = VIDEO_QUALITY_HD;

    // --- Privacy & Utilities ---
    public static volatile boolean parallelTranslatorEnabled = false;
    public static volatile boolean confirmBeforeCall = true;
    public static volatile boolean confirmBeforeSendSticker = false;
    public static volatile boolean hidePhoneNumber = false;

    private LagramConfig() {
    }

    public static void setStorageForTesting(Storage customStorage) {
        storage = customStorage;
        loaded = false;
        ensureLoaded();
    }

    private static Storage getStorage() {
        if (storage == null) {
            synchronized (LagramConfig.class) {
                if (storage == null) {
                    Context ctx = ApplicationLoader.applicationContext;
                    if (ctx != null) {
                        storage = new SharedPrefsStorage(ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE));
                    } else {
                        storage = new InMemoryStorage();
                    }
                }
            }
        }
        return storage;
    }

    public static void preloadAsync() {
        if (loaded) {
            return;
        }
        Thread t = new Thread(LagramConfig::ensureLoaded, "LagramConfigPreload");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (LagramConfig.class) {
            if (loaded) {
                return;
            }
            Storage s = getStorage();

            chatListStyle = clamp(s.getInt("chatListStyle", CHAT_LIST_M3_CARDS), CHAT_LIST_CLASSIC, CHAT_LIST_MINIMAL_SPACIOUS);
            avatarShape = clamp(s.getInt("avatarShape", AVATAR_SHAPE_CIRCLE), AVATAR_SHAPE_CIRCLE, AVATAR_SHAPE_ROUNDED_SQUARE);
            avatarCornerRadius = clamp(s.getInt("avatarCornerRadius", 26), MIN_AVATAR_RADIUS_DP, MAX_AVATAR_RADIUS_DP);
            avatarSizeDp = clamp(s.getInt("avatarSizeDp", 52), MIN_AVATAR_SIZE_DP, MAX_AVATAR_SIZE_DP);
            folderTabsStyle = clamp(s.getInt("folderTabsStyle", FOLDER_TABS_PILL_M3), FOLDER_TABS_PILL_M3, FOLDER_TABS_COMPACT_ICONS);
            hideAllChatsTab = s.getBoolean("hideAllChatsTab", false);
            unreadBadgeStyle = clamp(s.getInt("unreadBadgeStyle", UNREAD_BADGE_TONAL_PILL), UNREAD_BADGE_TONAL_PILL, UNREAD_BADGE_CLASSIC);
            onlineStatusPosition = clamp(s.getInt("onlineStatusPosition", ONLINE_POS_BOTTOM_RIGHT), ONLINE_POS_BOTTOM_RIGHT, ONLINE_POS_HIDDEN);
            homeHeaderTitleMode = clamp(s.getInt("homeHeaderTitleMode", TITLE_LAGRAM), TITLE_LAGRAM, TITLE_ACTIVE_FOLDER);
            accentPalette = clamp(s.getInt("accentPalette", ACCENT_LAGRAM_BLUE), ACCENT_SYSTEM_MONET, ACCENT_AMOLED_MONO);
            amoledPureBlack = s.getBoolean("amoledPureBlack", false);
            fontFamily = clamp(s.getInt("fontFamily", FONT_SYSTEM), FONT_SYSTEM, FONT_MONO);
            textScalePercent = clamp(s.getInt("textScalePercent", 100), MIN_TEXT_SCALE, MAX_TEXT_SCALE);
            animationPreset = clamp(s.getInt("animationPreset", ANIM_FLUID_120HZ_M3), ANIM_FLUID_120HZ_M3, ANIM_NONE);
            hapticFeedbackEnabled = s.getBoolean("hapticFeedbackEnabled", true);
            bottomNavigationEnabled = s.getBoolean("bottomNavigationEnabled", true);
            profileBannerM3Enabled = s.getBoolean("profileBannerM3Enabled", true);
            drawerHeaderStyle = clamp(s.getInt("drawerHeaderStyle", DRAWER_HEADER_DYNAMIC), DRAWER_HEADER_DYNAMIC, DRAWER_HEADER_MINIMAL);
            drawerShowNewGroup = s.getBoolean("drawerShowNewGroup", true);
            drawerShowContacts = s.getBoolean("drawerShowContacts", true);
            drawerShowCalls = s.getBoolean("drawerShowCalls", true);
            drawerShowSavedMessages = s.getBoolean("drawerShowSavedMessages", true);
            drawerShowSettings = s.getBoolean("drawerShowSettings", true);
            appIconStyle = clamp(s.getInt("appIconStyle", ICON_LAGRAM_DEFAULT), ICON_LAGRAM_DEFAULT, ICON_LAGRAM_MONET);

            bubbleCornerRadius = clamp(s.getInt("bubbleCornerRadius", 17), MIN_BUBBLE_RADIUS_DP, MAX_BUBBLE_RADIUS_DP);
            bubbleTailStyle = clamp(s.getInt("bubbleTailStyle", TAIL_CLASSIC), TAIL_CLASSIC, TAIL_MINIMAL_SHORT);
            messageVerticalSpacing = clamp(s.getInt("messageVerticalSpacing", SPACING_NORMAL), SPACING_COMPACT, SPACING_COMFORTABLE);
            stickerSizePercent = clamp(s.getInt("stickerSizePercent", 100), MIN_STICKER_SIZE_PERCENT, MAX_STICKER_SIZE_PERCENT);
            inputBarStyle = clamp(s.getInt("inputBarStyle", INPUT_BAR_IOS_3_PILLS), INPUT_BAR_IOS_3_PILLS, INPUT_BAR_CLASSIC_TG);
            hideInputGiftButton = s.getBoolean("hideInputGiftButton", false);
            hideInputBotCommandButton = s.getBoolean("hideInputBotCommandButton", false);
            hideInputQuickAudioVideoButton = s.getBoolean("hideInputQuickAudioVideoButton", false);
            chatHeaderAvatarSizeDp = clamp(s.getInt("chatHeaderAvatarSizeDp", 42), MIN_CHAT_HEADER_AVATAR_DP, MAX_CHAT_HEADER_AVATAR_DP);
            chatHeaderShowSubtitle = s.getBoolean("chatHeaderShowSubtitle", true);
            chatHeaderCleanQuickButtons = s.getBoolean("chatHeaderCleanQuickButtons", true);
            doubleTapAction = clamp(s.getInt("doubleTapAction", DOUBLE_TAP_REACT), DOUBLE_TAP_REACT, DOUBLE_TAP_SAVED);
            quickForwardNoAuthor = s.getBoolean("quickForwardNoAuthor", true);
            copyCleanLink = s.getBoolean("copyCleanLink", true);

            playerWaveAnimation = s.getBoolean("playerWaveAnimation", true);
            playerDynamicCoverPalette = s.getBoolean("playerDynamicCoverPalette", true);
            playerClassicMode = s.getBoolean("playerClassicMode", false);
            videoGesturesEnabled = s.getBoolean("videoGesturesEnabled", true);
            photoUploadQuality = clamp(s.getInt("photoUploadQuality", PHOTO_QUALITY_HIGH), PHOTO_QUALITY_STANDARD, PHOTO_QUALITY_ULTRA);
            videoUploadQuality = clamp(s.getInt("videoUploadQuality", VIDEO_QUALITY_HD), VIDEO_QUALITY_STANDARD, VIDEO_QUALITY_FHD);

            parallelTranslatorEnabled = s.getBoolean("parallelTranslatorEnabled", false);
            confirmBeforeCall = s.getBoolean("confirmBeforeCall", true);
            confirmBeforeSendSticker = s.getBoolean("confirmBeforeSendSticker", false);
            hidePhoneNumber = s.getBoolean("hidePhoneNumber", false);

            try {
                SharedConfig.bubbleRadius = bubbleCornerRadius;
                SharedConfig.fontSize = Math.max(12, Math.min(30, Math.round(16f * textScalePercent / 100f)));
            } catch (Throwable ignored) {
            }

            loaded = true;
        }
    }

    /**
     * Liquid Glass is strictly disabled by default and only active when explicitly
     * turned on by the user in Power Saving (LiteMode).
     */
    public static boolean isLiquidGlassEnabled() {
        try {
            return LiteMode.isEnabled(LiteMode.FLAG_LIQUID_GLASS);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void addListener(ConfigListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(ConfigListener listener) {
        listeners.remove(listener);
    }

    private static void notifyChanged(String key) {
        if (batchDepth > 0) {
            return;
        }
        for (ConfigListener listener : listeners) {
            listener.onLagramConfigChanged(key);
        }
    }

    /**
     * Resolves target photo upload dimension in pixels based on Lagram photoUploadQuality.
     */
    public static int getPhotoUploadMaxDimension(boolean highQualityRequested) {
        ensureLoaded();
        if (highQualityRequested || photoUploadQuality == PHOTO_QUALITY_ULTRA) {
            return 2560;
        }
        if (photoUploadQuality == PHOTO_QUALITY_HIGH) {
            return 1920;
        }
        return 1280;
    }

    /**
     * Resolves default video compression tier index for PhotoViewer based on Lagram videoUploadQuality.
     */
    public static int getVideoUploadCompressionTarget(int compressionsCount) {
        ensureLoaded();
        if (compressionsCount <= 1) {
            return 0;
        }
        if (videoUploadQuality == VIDEO_QUALITY_FHD) {
            return compressionsCount - 1;
        }
        if (videoUploadQuality == VIDEO_QUALITY_HD) {
            return Math.min(2, compressionsCount - 1);
        }
        return Math.min(1, compressionsCount - 1);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    // --- Setters: Appearance ---
    public static void setChatListStyle(int v) {
        ensureLoaded();
        chatListStyle = clamp(v, CHAT_LIST_CLASSIC, CHAT_LIST_MINIMAL_SPACIOUS);
        getStorage().putInt("chatListStyle", chatListStyle);
        notifyChanged("chatListStyle");
    }

    public static void setAvatarShape(int v) {
        ensureLoaded();
        avatarShape = clamp(v, AVATAR_SHAPE_CIRCLE, AVATAR_SHAPE_ROUNDED_SQUARE);
        getStorage().putInt("avatarShape", avatarShape);
        if (avatarShape == AVATAR_SHAPE_CIRCLE) {
            setAvatarCornerRadius(26);
        } else if (avatarShape == AVATAR_SHAPE_SQUIRCLE) {
            setAvatarCornerRadius(16);
        } else {
            setAvatarCornerRadius(10);
        }
        notifyChanged("avatarShape");
    }

    public static void setAvatarCornerRadius(int dp) {
        ensureLoaded();
        avatarCornerRadius = clamp(dp, MIN_AVATAR_RADIUS_DP, MAX_AVATAR_RADIUS_DP);
        getStorage().putInt("avatarCornerRadius", avatarCornerRadius);
        notifyChanged("avatarCornerRadius");
    }

    public static void setAvatarSizeDp(int dp) {
        ensureLoaded();
        avatarSizeDp = clamp(dp, MIN_AVATAR_SIZE_DP, MAX_AVATAR_SIZE_DP);
        getStorage().putInt("avatarSizeDp", avatarSizeDp);
        notifyChanged("avatarSizeDp");
    }

    public static void setFolderTabsStyle(int v) {
        ensureLoaded();
        folderTabsStyle = clamp(v, FOLDER_TABS_PILL_M3, FOLDER_TABS_COMPACT_ICONS);
        getStorage().putInt("folderTabsStyle", folderTabsStyle);
        notifyChanged("folderTabsStyle");
    }

    public static void setHideAllChatsTab(boolean v) {
        ensureLoaded();
        hideAllChatsTab = v;
        getStorage().putBoolean("hideAllChatsTab", v);
        notifyChanged("hideAllChatsTab");
    }

    public static void setUnreadBadgeStyle(int v) {
        ensureLoaded();
        unreadBadgeStyle = clamp(v, UNREAD_BADGE_TONAL_PILL, UNREAD_BADGE_CLASSIC);
        getStorage().putInt("unreadBadgeStyle", unreadBadgeStyle);
        notifyChanged("unreadBadgeStyle");
    }

    public static void setOnlineStatusPosition(int v) {
        ensureLoaded();
        onlineStatusPosition = clamp(v, ONLINE_POS_BOTTOM_RIGHT, ONLINE_POS_HIDDEN);
        getStorage().putInt("onlineStatusPosition", onlineStatusPosition);
        notifyChanged("onlineStatusPosition");
    }

    public static void setHomeHeaderTitleMode(int v) {
        ensureLoaded();
        homeHeaderTitleMode = clamp(v, TITLE_LAGRAM, TITLE_ACTIVE_FOLDER);
        getStorage().putInt("homeHeaderTitleMode", homeHeaderTitleMode);
        notifyChanged("homeHeaderTitleMode");
    }

    public static void setAccentPalette(int v) {
        ensureLoaded();
        accentPalette = clamp(v, ACCENT_SYSTEM_MONET, ACCENT_AMOLED_MONO);
        if (accentPalette == ACCENT_SYSTEM_MONET) {
            LagramThemeEngine.invalidateMonetCache();
        }
        getStorage().putInt("accentPalette", accentPalette);
        notifyChanged("accentPalette");
    }

    public static void setAmoledPureBlack(boolean v) {
        ensureLoaded();
        amoledPureBlack = v;
        getStorage().putBoolean("amoledPureBlack", v);
        notifyChanged("amoledPureBlack");
    }

    public static void setFontFamily(int v) {
        ensureLoaded();
        fontFamily = clamp(v, FONT_SYSTEM, FONT_MONO);
        getStorage().putInt("fontFamily", fontFamily);
        notifyChanged("fontFamily");
    }

    public static void setTextScalePercent(int v) {
        ensureLoaded();
        textScalePercent = clamp(v, MIN_TEXT_SCALE, MAX_TEXT_SCALE);
        getStorage().putInt("textScalePercent", textScalePercent);
        try {
            SharedConfig.fontSize = Math.max(12, Math.min(30, Math.round(16f * textScalePercent / 100f)));
        } catch (Throwable ignored) {
        }
        notifyChanged("textScalePercent");
    }

    public static void setAnimationPreset(int v) {
        ensureLoaded();
        animationPreset = clamp(v, ANIM_FLUID_120HZ_M3, ANIM_NONE);
        getStorage().putInt("animationPreset", animationPreset);
        notifyChanged("animationPreset");
    }

    public static void setHapticFeedbackEnabled(boolean v) {
        ensureLoaded();
        hapticFeedbackEnabled = v;
        getStorage().putBoolean("hapticFeedbackEnabled", v);
        notifyChanged("hapticFeedbackEnabled");
    }

    public static void setBottomNavigationEnabled(boolean v) {
        ensureLoaded();
        bottomNavigationEnabled = v;
        getStorage().putBoolean("bottomNavigationEnabled", v);
        notifyChanged("bottomNavigationEnabled");
    }

    public static void setProfileBannerM3Enabled(boolean v) {
        ensureLoaded();
        profileBannerM3Enabled = v;
        getStorage().putBoolean("profileBannerM3Enabled", v);
        notifyChanged("profileBannerM3Enabled");
    }

    public static void setDrawerHeaderStyle(int v) {
        ensureLoaded();
        drawerHeaderStyle = clamp(v, DRAWER_HEADER_DYNAMIC, DRAWER_HEADER_MINIMAL);
        getStorage().putInt("drawerHeaderStyle", drawerHeaderStyle);
        notifyChanged("drawerHeaderStyle");
    }

    public static void setDrawerShowNewGroup(boolean v) {
        ensureLoaded();
        drawerShowNewGroup = v;
        getStorage().putBoolean("drawerShowNewGroup", v);
        notifyChanged("drawerShowNewGroup");
    }

    public static void setDrawerShowContacts(boolean v) {
        ensureLoaded();
        drawerShowContacts = v;
        getStorage().putBoolean("drawerShowContacts", v);
        notifyChanged("drawerShowContacts");
    }

    public static void setDrawerShowCalls(boolean v) {
        ensureLoaded();
        drawerShowCalls = v;
        getStorage().putBoolean("drawerShowCalls", v);
        notifyChanged("drawerShowCalls");
    }

    public static void setDrawerShowSavedMessages(boolean v) {
        ensureLoaded();
        drawerShowSavedMessages = v;
        getStorage().putBoolean("drawerShowSavedMessages", v);
        notifyChanged("drawerShowSavedMessages");
    }

    public static void setDrawerShowSettings(boolean v) {
        ensureLoaded();
        drawerShowSettings = v;
        getStorage().putBoolean("drawerShowSettings", v);
        notifyChanged("drawerShowSettings");
    }

    public static void setAppIconStyle(int v) {
        ensureLoaded();
        int prev = appIconStyle;
        appIconStyle = clamp(v, ICON_LAGRAM_DEFAULT, ICON_LAGRAM_MONET);
        getStorage().putInt("appIconStyle", appIconStyle);
        if (appIconStyle != prev) {
            try {
                if (ApplicationLoader.applicationContext != null) {
                    org.telegram.ui.LauncherIconController.LauncherIcon target;
                    switch (appIconStyle) {
                        case ICON_LAGRAM_DARK:
                            target = org.telegram.ui.LauncherIconController.LauncherIcon.NOX;
                            break;
                        case ICON_LAGRAM_AMOLED:
                            target = org.telegram.ui.LauncherIconController.LauncherIcon.TURBO;
                            break;
                        case ICON_LAGRAM_MONET:
                            target = org.telegram.ui.LauncherIconController.LauncherIcon.AQUA;
                            break;
                        case ICON_LAGRAM_DEFAULT:
                        default:
                            target = org.telegram.ui.LauncherIconController.LauncherIcon.DEFAULT;
                            break;
                    }
                    org.telegram.ui.LauncherIconController.setIcon(target);
                }
            } catch (Throwable ignored) {
            }
        }
        notifyChanged("appIconStyle");
    }

    // --- Setters: Chats ---
    public static void setBubbleCornerRadius(int dp) {
        ensureLoaded();
        bubbleCornerRadius = clamp(dp, MIN_BUBBLE_RADIUS_DP, MAX_BUBBLE_RADIUS_DP);
        getStorage().putInt("bubbleCornerRadius", bubbleCornerRadius);
        try {
            SharedConfig.bubbleRadius = bubbleCornerRadius;
        } catch (Throwable ignored) {
        }
        notifyChanged("bubbleCornerRadius");
    }

    public static void setBubbleTailStyle(int v) {
        ensureLoaded();
        bubbleTailStyle = clamp(v, TAIL_CLASSIC, TAIL_MINIMAL_SHORT);
        getStorage().putInt("bubbleTailStyle", bubbleTailStyle);
        notifyChanged("bubbleTailStyle");
    }

    public static void setMessageVerticalSpacing(int v) {
        ensureLoaded();
        messageVerticalSpacing = clamp(v, SPACING_COMPACT, SPACING_COMFORTABLE);
        getStorage().putInt("messageVerticalSpacing", messageVerticalSpacing);
        notifyChanged("messageVerticalSpacing");
    }

    public static void setStickerSizePercent(int v) {
        ensureLoaded();
        stickerSizePercent = clamp(v, MIN_STICKER_SIZE_PERCENT, MAX_STICKER_SIZE_PERCENT);
        getStorage().putInt("stickerSizePercent", stickerSizePercent);
        notifyChanged("stickerSizePercent");
    }

    public static void setInputBarStyle(int v) {
        ensureLoaded();
        inputBarStyle = clamp(v, INPUT_BAR_IOS_3_PILLS, INPUT_BAR_CLASSIC_TG);
        getStorage().putInt("inputBarStyle", inputBarStyle);
        notifyChanged("inputBarStyle");
    }

    public static void setHideInputGiftButton(boolean v) {
        ensureLoaded();
        hideInputGiftButton = v;
        getStorage().putBoolean("hideInputGiftButton", v);
        notifyChanged("hideInputGiftButton");
    }

    public static void setHideInputBotCommandButton(boolean v) {
        ensureLoaded();
        hideInputBotCommandButton = v;
        getStorage().putBoolean("hideInputBotCommandButton", v);
        notifyChanged("hideInputBotCommandButton");
    }

    public static void setHideInputQuickAudioVideoButton(boolean v) {
        ensureLoaded();
        hideInputQuickAudioVideoButton = v;
        getStorage().putBoolean("hideInputQuickAudioVideoButton", v);
        notifyChanged("hideInputQuickAudioVideoButton");
    }

    public static void setChatHeaderAvatarSizeDp(int dp) {
        ensureLoaded();
        chatHeaderAvatarSizeDp = clamp(dp, MIN_CHAT_HEADER_AVATAR_DP, MAX_CHAT_HEADER_AVATAR_DP);
        getStorage().putInt("chatHeaderAvatarSizeDp", chatHeaderAvatarSizeDp);
        notifyChanged("chatHeaderAvatarSizeDp");
    }

    public static void setChatHeaderShowSubtitle(boolean v) {
        ensureLoaded();
        chatHeaderShowSubtitle = v;
        getStorage().putBoolean("chatHeaderShowSubtitle", v);
        notifyChanged("chatHeaderShowSubtitle");
    }

    public static void setChatHeaderCleanQuickButtons(boolean v) {
        ensureLoaded();
        chatHeaderCleanQuickButtons = v;
        getStorage().putBoolean("chatHeaderCleanQuickButtons", v);
        notifyChanged("chatHeaderCleanQuickButtons");
    }

    public static void setDoubleTapAction(int v) {
        ensureLoaded();
        doubleTapAction = clamp(v, DOUBLE_TAP_REACT, DOUBLE_TAP_SAVED);
        getStorage().putInt("doubleTapAction", doubleTapAction);
        notifyChanged("doubleTapAction");
    }

    public static void setQuickForwardNoAuthor(boolean v) {
        ensureLoaded();
        quickForwardNoAuthor = v;
        getStorage().putBoolean("quickForwardNoAuthor", v);
        notifyChanged("quickForwardNoAuthor");
    }

    public static void setCopyCleanLink(boolean v) {
        ensureLoaded();
        copyCleanLink = v;
        getStorage().putBoolean("copyCleanLink", v);
        notifyChanged("copyCleanLink");
    }

    // --- Setters: Media ---
    public static void setPlayerWaveAnimation(boolean v) {
        ensureLoaded();
        playerWaveAnimation = v;
        getStorage().putBoolean("playerWaveAnimation", v);
        notifyChanged("playerWaveAnimation");
    }

    public static void setPlayerDynamicCoverPalette(boolean v) {
        ensureLoaded();
        playerDynamicCoverPalette = v;
        getStorage().putBoolean("playerDynamicCoverPalette", v);
        notifyChanged("playerDynamicCoverPalette");
    }

    public static void setPlayerClassicMode(boolean v) {
        ensureLoaded();
        playerClassicMode = v;
        getStorage().putBoolean("playerClassicMode", v);
        notifyChanged("playerClassicMode");
    }

    public static void setVideoGesturesEnabled(boolean v) {
        ensureLoaded();
        videoGesturesEnabled = v;
        getStorage().putBoolean("videoGesturesEnabled", v);
        notifyChanged("videoGesturesEnabled");
    }

    public static void setPhotoUploadQuality(int v) {
        ensureLoaded();
        photoUploadQuality = clamp(v, PHOTO_QUALITY_STANDARD, PHOTO_QUALITY_ULTRA);
        getStorage().putInt("photoUploadQuality", photoUploadQuality);
        notifyChanged("photoUploadQuality");
    }

    public static void setVideoUploadQuality(int v) {
        ensureLoaded();
        videoUploadQuality = clamp(v, VIDEO_QUALITY_STANDARD, VIDEO_QUALITY_FHD);
        getStorage().putInt("videoUploadQuality", videoUploadQuality);
        notifyChanged("videoUploadQuality");
    }

    // --- Setters: Privacy ---
    public static void setParallelTranslatorEnabled(boolean v) {
        ensureLoaded();
        parallelTranslatorEnabled = v;
        getStorage().putBoolean("parallelTranslatorEnabled", v);
        notifyChanged("parallelTranslatorEnabled");
    }

    public static void setConfirmBeforeCall(boolean v) {
        ensureLoaded();
        confirmBeforeCall = v;
        getStorage().putBoolean("confirmBeforeCall", v);
        notifyChanged("confirmBeforeCall");
    }

    public static void setConfirmBeforeSendSticker(boolean v) {
        ensureLoaded();
        confirmBeforeSendSticker = v;
        getStorage().putBoolean("confirmBeforeSendSticker", v);
        notifyChanged("confirmBeforeSendSticker");
    }

    public static void setHidePhoneNumber(boolean v) {
        ensureLoaded();
        hidePhoneNumber = v;
        getStorage().putBoolean("hidePhoneNumber", v);
        notifyChanged("hidePhoneNumber");
    }

    // --- Category Reset ---
    public static void resetCategory(String category) {
        ensureLoaded();
        batchDepth++;
        try {
            if (CATEGORY_APPEARANCE.equals(category)) {
                setChatListStyle(CHAT_LIST_M3_CARDS);
                setAvatarShape(AVATAR_SHAPE_CIRCLE);
                setAvatarCornerRadius(26);
                setAvatarSizeDp(52);
                setFolderTabsStyle(FOLDER_TABS_PILL_M3);
                setHideAllChatsTab(false);
                setUnreadBadgeStyle(UNREAD_BADGE_TONAL_PILL);
                setOnlineStatusPosition(ONLINE_POS_BOTTOM_RIGHT);
                setHomeHeaderTitleMode(TITLE_LAGRAM);
                setAccentPalette(ACCENT_LAGRAM_BLUE);
                setAmoledPureBlack(false);
                setFontFamily(FONT_SYSTEM);
                setTextScalePercent(100);
                setAnimationPreset(ANIM_FLUID_120HZ_M3);
                setHapticFeedbackEnabled(true);
                setBottomNavigationEnabled(true);
                setProfileBannerM3Enabled(true);
                setDrawerHeaderStyle(DRAWER_HEADER_DYNAMIC);
                setDrawerShowNewGroup(true);
                setDrawerShowContacts(true);
                setDrawerShowCalls(true);
                setDrawerShowSavedMessages(true);
                setDrawerShowSettings(true);
                setAppIconStyle(ICON_LAGRAM_DEFAULT);
            } else if (CATEGORY_CHATS.equals(category)) {
                setBubbleCornerRadius(17);
                setBubbleTailStyle(TAIL_CLASSIC);
                setMessageVerticalSpacing(SPACING_NORMAL);
                setStickerSizePercent(100);
                setInputBarStyle(INPUT_BAR_IOS_3_PILLS);
                setHideInputGiftButton(false);
                setHideInputBotCommandButton(false);
                setHideInputQuickAudioVideoButton(false);
                setChatHeaderAvatarSizeDp(42);
                setChatHeaderShowSubtitle(true);
                setChatHeaderCleanQuickButtons(true);
                setDoubleTapAction(DOUBLE_TAP_REACT);
                setQuickForwardNoAuthor(true);
                setCopyCleanLink(true);
            } else if (CATEGORY_MEDIA.equals(category)) {
                setPlayerWaveAnimation(true);
                setPlayerDynamicCoverPalette(true);
                setPlayerClassicMode(false);
                setVideoGesturesEnabled(true);
                setPhotoUploadQuality(PHOTO_QUALITY_HIGH);
                setVideoUploadQuality(VIDEO_QUALITY_HD);
            } else if (CATEGORY_PRIVACY.equals(category)) {
                setParallelTranslatorEnabled(false);
                setConfirmBeforeCall(true);
                setConfirmBeforeSendSticker(false);
                setHidePhoneNumber(false);
            }
        } finally {
            batchDepth--;
        }
        notifyChanged("reset:" + category);
    }

    public static void resetAll() {
        batchDepth++;
        try {
            resetCategory(CATEGORY_APPEARANCE);
            resetCategory(CATEGORY_CHATS);
            resetCategory(CATEGORY_MEDIA);
            resetCategory(CATEGORY_PRIVACY);
        } finally {
            batchDepth--;
        }
        notifyChanged("reset:all");
    }

    // --- Pure-Java JSON Export / Import ---
    public static String exportToJson() {
        ensureLoaded();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("version", 1);
        map.put("chatListStyle", chatListStyle);
        map.put("avatarShape", avatarShape);
        map.put("avatarCornerRadius", avatarCornerRadius);
        map.put("avatarSizeDp", avatarSizeDp);
        map.put("folderTabsStyle", folderTabsStyle);
        map.put("hideAllChatsTab", hideAllChatsTab);
        map.put("unreadBadgeStyle", unreadBadgeStyle);
        map.put("onlineStatusPosition", onlineStatusPosition);
        map.put("homeHeaderTitleMode", homeHeaderTitleMode);
        map.put("accentPalette", accentPalette);
        map.put("amoledPureBlack", amoledPureBlack);
        map.put("fontFamily", fontFamily);
        map.put("textScalePercent", textScalePercent);
        map.put("animationPreset", animationPreset);
        map.put("hapticFeedbackEnabled", hapticFeedbackEnabled);
        map.put("bottomNavigationEnabled", bottomNavigationEnabled);
        map.put("profileBannerM3Enabled", profileBannerM3Enabled);
        map.put("drawerHeaderStyle", drawerHeaderStyle);
        map.put("drawerShowNewGroup", drawerShowNewGroup);
        map.put("drawerShowContacts", drawerShowContacts);
        map.put("drawerShowCalls", drawerShowCalls);
        map.put("drawerShowSavedMessages", drawerShowSavedMessages);
        map.put("drawerShowSettings", drawerShowSettings);
        map.put("appIconStyle", appIconStyle);

        map.put("bubbleCornerRadius", bubbleCornerRadius);
        map.put("bubbleTailStyle", bubbleTailStyle);
        map.put("messageVerticalSpacing", messageVerticalSpacing);
        map.put("stickerSizePercent", stickerSizePercent);
        map.put("inputBarStyle", inputBarStyle);
        map.put("hideInputGiftButton", hideInputGiftButton);
        map.put("hideInputBotCommandButton", hideInputBotCommandButton);
        map.put("hideInputQuickAudioVideoButton", hideInputQuickAudioVideoButton);
        map.put("chatHeaderAvatarSizeDp", chatHeaderAvatarSizeDp);
        map.put("chatHeaderShowSubtitle", chatHeaderShowSubtitle);
        map.put("chatHeaderCleanQuickButtons", chatHeaderCleanQuickButtons);
        map.put("doubleTapAction", doubleTapAction);
        map.put("quickForwardNoAuthor", quickForwardNoAuthor);
        map.put("copyCleanLink", copyCleanLink);

        map.put("playerWaveAnimation", playerWaveAnimation);
        map.put("playerDynamicCoverPalette", playerDynamicCoverPalette);
        map.put("playerClassicMode", playerClassicMode);
        map.put("videoGesturesEnabled", videoGesturesEnabled);
        map.put("photoUploadQuality", photoUploadQuality);
        map.put("videoUploadQuality", videoUploadQuality);

        map.put("parallelTranslatorEnabled", parallelTranslatorEnabled);
        map.put("confirmBeforeCall", confirmBeforeCall);
        map.put("confirmBeforeSendSticker", confirmBeforeSendSticker);
        map.put("hidePhoneNumber", hidePhoneNumber);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        List<Map.Entry<String, Object>> entries = new ArrayList<>(map.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Object> e = entries.get(i);
            sb.append("  \"").append(e.getKey()).append("\": ").append(e.getValue());
            if (i < entries.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public static boolean importFromJson(String json) {
        if (json == null) {
            return false;
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return false;
        }
        String body = trimmed.substring(1, trimmed.length() - 1).trim();
        if (body.isEmpty()) {
            return false;
        }
        Map<String, String> parsed = new LinkedHashMap<>();
        String[] parts = body.split(",");
        for (String part : parts) {
            int colon = part.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String rawKey = part.substring(0, colon).trim();
            String rawVal = part.substring(colon + 1).trim();
            if (rawKey.startsWith("\"") && rawKey.endsWith("\"") && rawKey.length() >= 2) {
                rawKey = rawKey.substring(1, rawKey.length() - 1);
            }
            parsed.put(rawKey, rawVal);
        }
        if (parsed.isEmpty()) {
            return false;
        }

        ensureLoaded();
        boolean appliedAny = false;
        batchDepth++;
        try {
            if (parsed.containsKey("chatListStyle")) { setChatListStyle(parseInt(parsed.get("chatListStyle"), chatListStyle)); appliedAny = true; }
            if (parsed.containsKey("avatarShape")) {
                avatarShape = clamp(parseInt(parsed.get("avatarShape"), avatarShape), AVATAR_SHAPE_CIRCLE, AVATAR_SHAPE_ROUNDED_SQUARE);
                getStorage().putInt("avatarShape", avatarShape);
                appliedAny = true;
            }
            if (parsed.containsKey("avatarCornerRadius")) { setAvatarCornerRadius(parseInt(parsed.get("avatarCornerRadius"), avatarCornerRadius)); appliedAny = true; }
            if (parsed.containsKey("avatarSizeDp")) { setAvatarSizeDp(parseInt(parsed.get("avatarSizeDp"), avatarSizeDp)); appliedAny = true; }
            if (parsed.containsKey("folderTabsStyle")) { setFolderTabsStyle(parseInt(parsed.get("folderTabsStyle"), folderTabsStyle)); appliedAny = true; }
            if (parsed.containsKey("hideAllChatsTab")) { setHideAllChatsTab(parseBool(parsed.get("hideAllChatsTab"), hideAllChatsTab)); appliedAny = true; }
            if (parsed.containsKey("unreadBadgeStyle")) { setUnreadBadgeStyle(parseInt(parsed.get("unreadBadgeStyle"), unreadBadgeStyle)); appliedAny = true; }
            if (parsed.containsKey("onlineStatusPosition")) { setOnlineStatusPosition(parseInt(parsed.get("onlineStatusPosition"), onlineStatusPosition)); appliedAny = true; }
            if (parsed.containsKey("homeHeaderTitleMode")) { setHomeHeaderTitleMode(parseInt(parsed.get("homeHeaderTitleMode"), homeHeaderTitleMode)); appliedAny = true; }
            if (parsed.containsKey("accentPalette")) { setAccentPalette(parseInt(parsed.get("accentPalette"), accentPalette)); appliedAny = true; }
            if (parsed.containsKey("amoledPureBlack")) { setAmoledPureBlack(parseBool(parsed.get("amoledPureBlack"), amoledPureBlack)); appliedAny = true; }
            if (parsed.containsKey("fontFamily")) { setFontFamily(parseInt(parsed.get("fontFamily"), fontFamily)); appliedAny = true; }
            if (parsed.containsKey("textScalePercent")) { setTextScalePercent(parseInt(parsed.get("textScalePercent"), textScalePercent)); appliedAny = true; }
            if (parsed.containsKey("animationPreset")) { setAnimationPreset(parseInt(parsed.get("animationPreset"), animationPreset)); appliedAny = true; }
            if (parsed.containsKey("hapticFeedbackEnabled")) { setHapticFeedbackEnabled(parseBool(parsed.get("hapticFeedbackEnabled"), hapticFeedbackEnabled)); appliedAny = true; }
            if (parsed.containsKey("bottomNavigationEnabled")) { setBottomNavigationEnabled(parseBool(parsed.get("bottomNavigationEnabled"), bottomNavigationEnabled)); appliedAny = true; }
            if (parsed.containsKey("profileBannerM3Enabled")) { setProfileBannerM3Enabled(parseBool(parsed.get("profileBannerM3Enabled"), profileBannerM3Enabled)); appliedAny = true; }
            if (parsed.containsKey("drawerHeaderStyle")) { setDrawerHeaderStyle(parseInt(parsed.get("drawerHeaderStyle"), drawerHeaderStyle)); appliedAny = true; }
            if (parsed.containsKey("drawerShowNewGroup")) { setDrawerShowNewGroup(parseBool(parsed.get("drawerShowNewGroup"), drawerShowNewGroup)); appliedAny = true; }
            if (parsed.containsKey("drawerShowContacts")) { setDrawerShowContacts(parseBool(parsed.get("drawerShowContacts"), drawerShowContacts)); appliedAny = true; }
            if (parsed.containsKey("drawerShowCalls")) { setDrawerShowCalls(parseBool(parsed.get("drawerShowCalls"), drawerShowCalls)); appliedAny = true; }
            if (parsed.containsKey("drawerShowSavedMessages")) { setDrawerShowSavedMessages(parseBool(parsed.get("drawerShowSavedMessages"), drawerShowSavedMessages)); appliedAny = true; }
            if (parsed.containsKey("drawerShowSettings")) { setDrawerShowSettings(parseBool(parsed.get("drawerShowSettings"), drawerShowSettings)); appliedAny = true; }
            if (parsed.containsKey("appIconStyle")) { setAppIconStyle(parseInt(parsed.get("appIconStyle"), appIconStyle)); appliedAny = true; }

            if (parsed.containsKey("bubbleCornerRadius")) { setBubbleCornerRadius(parseInt(parsed.get("bubbleCornerRadius"), bubbleCornerRadius)); appliedAny = true; }
            if (parsed.containsKey("bubbleTailStyle")) { setBubbleTailStyle(parseInt(parsed.get("bubbleTailStyle"), bubbleTailStyle)); appliedAny = true; }
            if (parsed.containsKey("messageVerticalSpacing")) { setMessageVerticalSpacing(parseInt(parsed.get("messageVerticalSpacing"), messageVerticalSpacing)); appliedAny = true; }
            if (parsed.containsKey("stickerSizePercent")) { setStickerSizePercent(parseInt(parsed.get("stickerSizePercent"), stickerSizePercent)); appliedAny = true; }
            if (parsed.containsKey("inputBarStyle")) { setInputBarStyle(parseInt(parsed.get("inputBarStyle"), inputBarStyle)); appliedAny = true; }
            if (parsed.containsKey("hideInputGiftButton")) { setHideInputGiftButton(parseBool(parsed.get("hideInputGiftButton"), hideInputGiftButton)); appliedAny = true; }
            if (parsed.containsKey("hideInputBotCommandButton")) { setHideInputBotCommandButton(parseBool(parsed.get("hideInputBotCommandButton"), hideInputBotCommandButton)); appliedAny = true; }
            if (parsed.containsKey("hideInputQuickAudioVideoButton")) { setHideInputQuickAudioVideoButton(parseBool(parsed.get("hideInputQuickAudioVideoButton"), hideInputQuickAudioVideoButton)); appliedAny = true; }
            if (parsed.containsKey("chatHeaderAvatarSizeDp")) { setChatHeaderAvatarSizeDp(parseInt(parsed.get("chatHeaderAvatarSizeDp"), chatHeaderAvatarSizeDp)); appliedAny = true; }
            if (parsed.containsKey("chatHeaderShowSubtitle")) { setChatHeaderShowSubtitle(parseBool(parsed.get("chatHeaderShowSubtitle"), chatHeaderShowSubtitle)); appliedAny = true; }
            if (parsed.containsKey("chatHeaderCleanQuickButtons")) { setChatHeaderCleanQuickButtons(parseBool(parsed.get("chatHeaderCleanQuickButtons"), chatHeaderCleanQuickButtons)); appliedAny = true; }
            if (parsed.containsKey("doubleTapAction")) { setDoubleTapAction(parseInt(parsed.get("doubleTapAction"), doubleTapAction)); appliedAny = true; }
            if (parsed.containsKey("quickForwardNoAuthor")) { setQuickForwardNoAuthor(parseBool(parsed.get("quickForwardNoAuthor"), quickForwardNoAuthor)); appliedAny = true; }
            if (parsed.containsKey("copyCleanLink")) { setCopyCleanLink(parseBool(parsed.get("copyCleanLink"), copyCleanLink)); appliedAny = true; }

            if (parsed.containsKey("playerWaveAnimation")) { setPlayerWaveAnimation(parseBool(parsed.get("playerWaveAnimation"), playerWaveAnimation)); appliedAny = true; }
            if (parsed.containsKey("playerDynamicCoverPalette")) { setPlayerDynamicCoverPalette(parseBool(parsed.get("playerDynamicCoverPalette"), playerDynamicCoverPalette)); appliedAny = true; }
            if (parsed.containsKey("playerClassicMode")) { setPlayerClassicMode(parseBool(parsed.get("playerClassicMode"), playerClassicMode)); appliedAny = true; }
            if (parsed.containsKey("videoGesturesEnabled")) { setVideoGesturesEnabled(parseBool(parsed.get("videoGesturesEnabled"), videoGesturesEnabled)); appliedAny = true; }
            if (parsed.containsKey("photoUploadQuality")) { setPhotoUploadQuality(parseInt(parsed.get("photoUploadQuality"), photoUploadQuality)); appliedAny = true; }
            if (parsed.containsKey("videoUploadQuality")) { setVideoUploadQuality(parseInt(parsed.get("videoUploadQuality"), videoUploadQuality)); appliedAny = true; }

            if (parsed.containsKey("parallelTranslatorEnabled")) { setParallelTranslatorEnabled(parseBool(parsed.get("parallelTranslatorEnabled"), parallelTranslatorEnabled)); appliedAny = true; }
            if (parsed.containsKey("confirmBeforeCall")) { setConfirmBeforeCall(parseBool(parsed.get("confirmBeforeCall"), confirmBeforeCall)); appliedAny = true; }
            if (parsed.containsKey("confirmBeforeSendSticker")) { setConfirmBeforeSendSticker(parseBool(parsed.get("confirmBeforeSendSticker"), confirmBeforeSendSticker)); appliedAny = true; }
            if (parsed.containsKey("hidePhoneNumber")) { setHidePhoneNumber(parseBool(parsed.get("hidePhoneNumber"), hidePhoneNumber)); appliedAny = true; }
        } finally {
            batchDepth--;
        }

        if (appliedAny) {
            notifyChanged("import");
        }
        return appliedAny;
    }

    private static int parseInt(String val, int fallback) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static boolean parseBool(String val, boolean fallback) {
        if ("true".equalsIgnoreCase(val.trim())) {
            return true;
        }
        if ("false".equalsIgnoreCase(val.trim())) {
            return false;
        }
        return fallback;
    }
}
