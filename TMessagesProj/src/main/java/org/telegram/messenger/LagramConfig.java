// Lagram Turbo configuration & settings
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

import org.json.JSONObject;

public final class LagramConfig {
    private static final String PREFS_NAME = "lagram_config";
    private static volatile boolean loaded;

    // Animation speed modes
    public static final int ANIM_SPEED_NORMAL = 0;
    public static final int ANIM_SPEED_FAST = 1;
    public static final int ANIM_SPEED_INSTANT = 2;

    // Download speed boost levels
    public static final int DOWNLOAD_BOOST_OFF = 0;
    public static final int DOWNLOAD_BOOST_FAST = 1;
    public static final int DOWNLOAD_BOOST_EXTREME = 2;

    // Double tap actions
    public static final int DOUBLE_TAP_REACTION = 0;
    public static final int DOUBLE_TAP_REPLY = 1;
    public static final int DOUBLE_TAP_COPY = 2;
    public static final int DOUBLE_TAP_FORWARD = 3;
    public static final int DOUBLE_TAP_EDIT = 4;
    public static final int DOUBLE_TAP_SAVE = 5;
    public static final int DOUBLE_TAP_NONE = 6;

    // Opexgram Message Focus menu styles
    public static final int FOCUS_STYLE_LIST = 0;
    public static final int FOCUS_STYLE_TILES = 1;
    public static final int FOCUS_STYLE_CAPSULE = 2;

    // Folder tab styles
    public static final int FOLDER_TABS_ICON_AND_TEXT = 0;
    public static final int FOLDER_TABS_ICON_ONLY = 1;
    public static final int FOLDER_TABS_TEXT_ONLY = 2;

    // Corner rounding presets
    public static final int ROUNDING_DEFAULT = 0;
    public static final int ROUNDING_ROUND = 1;
    public static final int ROUNDING_SQUIRCLE = 2;
    public static final int ROUNDING_SQUARE = 3;

    // Delete message effects
    public static final int DELETE_EFFECT_DEFAULT = 0;
    public static final int DELETE_EFFECT_THANOS = 1;
    public static final int DELETE_EFFECT_SHARDS = 2;
    public static final int DELETE_EFFECT_PORTAL = 3;

    // Haptics levels
    public static final int HAPTICS_OFF = 0;
    public static final int HAPTICS_SOFT = 1;
    public static final int HAPTICS_NORMAL = 2;
    public static final int HAPTICS_STRONG = 3;

    // Appearance & UI
    public static boolean ios27BottomBar = true;
    public static boolean amoledSettingsBackdrop = true;
    public static int animationSpeed = ANIM_SPEED_FAST;
    public static boolean hideStories = false;
    public static boolean hideStoriesInProfile = false;
    public static boolean hideStoryPosting = false;
    public static boolean hideAllChatsFolder = false;
    public static boolean hidePhoneNumber = false;
    public static boolean hideTelegramBloat = false;
    public static boolean showSecondsInTime = false;
    public static boolean showIdAndDc = true;
    public static boolean idFormatBotApi = false;
    public static boolean useSystemFonts = false;
    public static boolean showBubbleTails = true;
    public static boolean glassMessages = false;
    public static int glassMessagesAlpha = 75;
    public static boolean glassMessagesBorder = true;
    public static boolean chatEdgeBlur = true;
    public static int folderTabsStyle = FOLDER_TABS_ICON_AND_TEXT;
    public static boolean showFolderCounters = true;
    public static boolean rememberLastFolder = false;
    public static int roundingPreset = ROUNDING_DEFAULT;
    public static boolean scrollEdgeScale = true;

    // Chats & Messages (including Opexgram Message Focus)
    public static boolean messageFocusEnabled = true;
    public static boolean messageFocusBlur = true;
    public static int messageFocusStyle = FOCUS_STYLE_CAPSULE;
    public static int doubleTapAction = DOUBLE_TAP_REACTION;
    public static int doubleTapActionOutgoing = DOUBLE_TAP_REACTION;
    public static int doubleTapActionIncoming = DOUBLE_TAP_REACTION;
    public static boolean hideKeyboardOnScroll = false;
    public static boolean disableNextChannelJump = false;
    public static boolean confirmBeforeCall = true;
    public static boolean confirmBeforeSendSticker = false;
    public static boolean disableVoiceVideoToggle = false;
    public static boolean disableProximitySensor = false;
    public static boolean forwardWithoutQuote = false;
    public static boolean hideShareButtonInChannels = false;
    public static int stickerSize = 14;
    public static int gifSize = 14;
    public static int deleteEffect = DELETE_EFFECT_THANOS;
    public static boolean showPageUpButton = true;
    public static boolean unlimitedSelection = true;
    public static boolean fastSelection = true;
    public static boolean showWeekdayInDate = true;
    public static boolean showForwardTime = true;
    public static boolean showPollPreview = true;
    public static boolean showStickerPackOwner = true;
    public static boolean showForwardsCounter = true;
    public static boolean textAppearAnimation = true;

    // Media & Network
    public static boolean turboDownloads = true;
    public static int downloadSpeedBoost = DOWNLOAD_BOOST_FAST;
    public static boolean turboUploads = true;
    public static boolean sendPhotosInHd = false;
    public static boolean rearCameraForRoundVideo = false;
    public static boolean pauseVideoOnMinimize = false;
    public static boolean autoplayVoiceInRow = true;
    public static boolean autoplayRoundInRow = true;
    public static boolean autoplayMusicInRow = true;
    public static boolean mediaGlow = true;
    public static boolean compactRoundCamera = false;

    // Performance & General
    public static boolean highRefreshRateScroll = true;
    public static boolean disableSponsoredMessages = true;
    public static boolean hideSimilarChannels = false;
    public static int forcePerformanceClass = -1; // -1 = Auto, 0 = Low, 1 = Average, 2 = High
    public static boolean showJoinDate = true;
    public static boolean openArchiveOnPull = true;
    public static int hapticsLevel = HAPTICS_NORMAL;

    // Privacy & Ghost Mode (AyuGram-style)
    public static boolean ghostModeEnabled = false;
    public static boolean ghostDontReadMessages = true;
    public static boolean ghostDontReadStories = true;
    public static boolean ghostDontSendTyping = true;
    public static boolean ghostDontSendOnline = true;
    public static boolean ghostSendOfflineAfterSend = true;
    public static boolean ghostMarkReadOnSend = false;

    // Local History & Anti-Restrictions
    public static boolean saveDeletedMessages = false;
    public static boolean saveEditedMessages = false;
    public static boolean allowScreenshots = false;
    public static boolean bypassNoForwards = false;

    private LagramConfig() {}

    public static void ensureLoaded() {
        if (loaded || ApplicationLoader.applicationContext == null) {
            return;
        }
        loaded = true;
        final SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Appearance
        ios27BottomBar = prefs.getBoolean("ios27BottomBar", true);
        amoledSettingsBackdrop = prefs.getBoolean("amoledSettingsBackdrop", true);
        animationSpeed = prefs.getInt("animationSpeed", ANIM_SPEED_FAST);
        hideStories = prefs.getBoolean("hideStories", false);
        hideStoriesInProfile = prefs.getBoolean("hideStoriesInProfile", false);
        hideStoryPosting = prefs.getBoolean("hideStoryPosting", false);
        hideAllChatsFolder = prefs.getBoolean("hideAllChatsFolder", false);
        hidePhoneNumber = prefs.getBoolean("hidePhoneNumber", false);
        hideTelegramBloat = prefs.getBoolean("hideTelegramBloat", false);
        showSecondsInTime = prefs.getBoolean("showSecondsInTime", false);
        showIdAndDc = prefs.getBoolean("showIdAndDc", true);
        idFormatBotApi = prefs.getBoolean("idFormatBotApi", false);
        useSystemFonts = prefs.getBoolean("useSystemFonts", false);
        showBubbleTails = prefs.getBoolean("showBubbleTails", true);
        glassMessages = prefs.getBoolean("glassMessages", false);
        glassMessagesAlpha = prefs.getInt("glassMessagesAlpha", 75);
        glassMessagesBorder = prefs.getBoolean("glassMessagesBorder", true);
        chatEdgeBlur = prefs.getBoolean("chatEdgeBlur", true);
        folderTabsStyle = prefs.getInt("folderTabsStyle", FOLDER_TABS_ICON_AND_TEXT);
        showFolderCounters = prefs.getBoolean("showFolderCounters", true);
        rememberLastFolder = prefs.getBoolean("rememberLastFolder", false);
        roundingPreset = prefs.getInt("roundingPreset", ROUNDING_DEFAULT);
        scrollEdgeScale = prefs.getBoolean("scrollEdgeScale", true);

        // Chats & Message Focus
        messageFocusEnabled = prefs.getBoolean("messageFocusEnabled", true);
        messageFocusBlur = prefs.getBoolean("messageFocusBlur", true);
        messageFocusStyle = prefs.getInt("messageFocusStyle", FOCUS_STYLE_CAPSULE);
        doubleTapAction = prefs.getInt("doubleTapAction", DOUBLE_TAP_REACTION);
        doubleTapActionOutgoing = prefs.getInt("doubleTapActionOutgoing", doubleTapAction);
        doubleTapActionIncoming = prefs.getInt("doubleTapActionIncoming", doubleTapAction);
        hideKeyboardOnScroll = prefs.getBoolean("hideKeyboardOnScroll", false);
        disableNextChannelJump = prefs.getBoolean("disableNextChannelJump", false);
        confirmBeforeCall = prefs.getBoolean("confirmBeforeCall", true);
        confirmBeforeSendSticker = prefs.getBoolean("confirmBeforeSendSticker", false);
        disableVoiceVideoToggle = prefs.getBoolean("disableVoiceVideoToggle", false);
        disableProximitySensor = prefs.getBoolean("disableProximitySensor", false);
        forwardWithoutQuote = prefs.getBoolean("forwardWithoutQuote", false);
        hideShareButtonInChannels = prefs.getBoolean("hideShareButtonInChannels", false);
        stickerSize = prefs.getInt("stickerSize", 14);
        gifSize = prefs.getInt("gifSize", 14);
        deleteEffect = prefs.getInt("deleteEffect", DELETE_EFFECT_THANOS);
        showPageUpButton = prefs.getBoolean("showPageUpButton", true);
        unlimitedSelection = prefs.getBoolean("unlimitedSelection", true);
        fastSelection = prefs.getBoolean("fastSelection", true);
        showWeekdayInDate = prefs.getBoolean("showWeekdayInDate", true);
        showForwardTime = prefs.getBoolean("showForwardTime", true);
        showPollPreview = prefs.getBoolean("showPollPreview", true);
        showStickerPackOwner = prefs.getBoolean("showStickerPackOwner", true);
        showForwardsCounter = prefs.getBoolean("showForwardsCounter", true);
        textAppearAnimation = prefs.getBoolean("textAppearAnimation", true);

        // Media & Network
        turboDownloads = prefs.getBoolean("turboDownloads", true);
        downloadSpeedBoost = prefs.getInt("downloadSpeedBoost", turboDownloads ? DOWNLOAD_BOOST_FAST : DOWNLOAD_BOOST_OFF);
        turboUploads = prefs.getBoolean("turboUploads", true);
        sendPhotosInHd = prefs.getBoolean("sendPhotosInHd", false);
        rearCameraForRoundVideo = prefs.getBoolean("rearCameraForRoundVideo", false);
        pauseVideoOnMinimize = prefs.getBoolean("pauseVideoOnMinimize", false);
        autoplayVoiceInRow = prefs.getBoolean("autoplayVoiceInRow", true);
        autoplayRoundInRow = prefs.getBoolean("autoplayRoundInRow", true);
        autoplayMusicInRow = prefs.getBoolean("autoplayMusicInRow", true);
        mediaGlow = prefs.getBoolean("mediaGlow", true);
        compactRoundCamera = prefs.getBoolean("compactRoundCamera", false);

        // Performance & General
        highRefreshRateScroll = prefs.getBoolean("highRefreshRateScroll", true);
        disableSponsoredMessages = prefs.getBoolean("disableSponsoredMessages", true);
        hideSimilarChannels = prefs.getBoolean("hideSimilarChannels", false);
        forcePerformanceClass = prefs.getInt("forcePerformanceClass", -1);
        showJoinDate = prefs.getBoolean("showJoinDate", true);
        openArchiveOnPull = prefs.getBoolean("openArchiveOnPull", true);
        hapticsLevel = prefs.getInt("hapticsLevel", HAPTICS_NORMAL);

        // Privacy & Ghost Mode
        ghostModeEnabled = prefs.getBoolean("ghostModeEnabled", false);
        ghostDontReadMessages = prefs.getBoolean("ghostDontReadMessages", true);
        ghostDontReadStories = prefs.getBoolean("ghostDontReadStories", true);
        ghostDontSendTyping = prefs.getBoolean("ghostDontSendTyping", true);
        ghostDontSendOnline = prefs.getBoolean("ghostDontSendOnline", true);
        ghostSendOfflineAfterSend = prefs.getBoolean("ghostSendOfflineAfterSend", true);
        ghostMarkReadOnSend = prefs.getBoolean("ghostMarkReadOnSend", false);

        // History & Anti-Restrictions
        saveDeletedMessages = prefs.getBoolean("saveDeletedMessages", false);
        saveEditedMessages = prefs.getBoolean("saveEditedMessages", false);
        allowScreenshots = prefs.getBoolean("allowScreenshots", false);
        bypassNoForwards = prefs.getBoolean("bypassNoForwards", false);

        if (saveDeletedMessages || saveEditedMessages) {
            LagramMessageHistoryDb.ensureCacheLoaded();
        }
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

    private static void saveInt(String key, int value) {
        if (ApplicationLoader.applicationContext == null) {
            return;
        }
        ApplicationLoader.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(key, value)
                .apply();
    }

    // --- Appearance setters/toggles ---
    public static boolean toggleIos27BottomBar() {
        ensureLoaded();
        ios27BottomBar = !ios27BottomBar;
        saveBoolean("ios27BottomBar", ios27BottomBar);
        return ios27BottomBar;
    }

    public static boolean toggleAmoledSettingsBackdrop() {
        ensureLoaded();
        amoledSettingsBackdrop = !amoledSettingsBackdrop;
        saveBoolean("amoledSettingsBackdrop", amoledSettingsBackdrop);
        return amoledSettingsBackdrop;
    }

    public static void setAnimationSpeed(int value) {
        ensureLoaded();
        animationSpeed = value;
        saveInt("animationSpeed", animationSpeed);
    }

    public static boolean toggleHideStories() {
        ensureLoaded();
        hideStories = !hideStories;
        saveBoolean("hideStories", hideStories);
        return hideStories;
    }

    public static boolean toggleHideStoriesInProfile() {
        ensureLoaded();
        hideStoriesInProfile = !hideStoriesInProfile;
        saveBoolean("hideStoriesInProfile", hideStoriesInProfile);
        return hideStoriesInProfile;
    }

    public static boolean toggleHideStoryPosting() {
        ensureLoaded();
        hideStoryPosting = !hideStoryPosting;
        saveBoolean("hideStoryPosting", hideStoryPosting);
        return hideStoryPosting;
    }

    public static boolean toggleHideAllChatsFolder() {
        ensureLoaded();
        hideAllChatsFolder = !hideAllChatsFolder;
        saveBoolean("hideAllChatsFolder", hideAllChatsFolder);
        return hideAllChatsFolder;
    }

    public static boolean toggleHidePhoneNumber() {
        ensureLoaded();
        hidePhoneNumber = !hidePhoneNumber;
        saveBoolean("hidePhoneNumber", hidePhoneNumber);
        return hidePhoneNumber;
    }

    public static boolean toggleHideTelegramBloat() {
        ensureLoaded();
        hideTelegramBloat = !hideTelegramBloat;
        saveBoolean("hideTelegramBloat", hideTelegramBloat);
        return hideTelegramBloat;
    }

    public static boolean toggleShowSecondsInTime() {
        ensureLoaded();
        showSecondsInTime = !showSecondsInTime;
        saveBoolean("showSecondsInTime", showSecondsInTime);
        return showSecondsInTime;
    }

    public static boolean toggleShowIdAndDc() {
        ensureLoaded();
        showIdAndDc = !showIdAndDc;
        saveBoolean("showIdAndDc", showIdAndDc);
        return showIdAndDc;
    }

    public static boolean toggleIdFormatBotApi() {
        ensureLoaded();
        idFormatBotApi = !idFormatBotApi;
        saveBoolean("idFormatBotApi", idFormatBotApi);
        return idFormatBotApi;
    }

    public static boolean toggleUseSystemFonts() {
        ensureLoaded();
        useSystemFonts = !useSystemFonts;
        saveBoolean("useSystemFonts", useSystemFonts);
        return useSystemFonts;
    }

    public static boolean toggleShowBubbleTails() {
        ensureLoaded();
        showBubbleTails = !showBubbleTails;
        saveBoolean("showBubbleTails", showBubbleTails);
        return showBubbleTails;
    }

    public static boolean toggleGlassMessages() {
        ensureLoaded();
        glassMessages = !glassMessages;
        saveBoolean("glassMessages", glassMessages);
        return glassMessages;
    }

    public static void setGlassMessagesAlpha(int alpha) {
        ensureLoaded();
        glassMessagesAlpha = Math.max(20, Math.min(100, alpha));
        saveInt("glassMessagesAlpha", glassMessagesAlpha);
    }

    public static boolean toggleGlassMessagesBorder() {
        ensureLoaded();
        glassMessagesBorder = !glassMessagesBorder;
        saveBoolean("glassMessagesBorder", glassMessagesBorder);
        return glassMessagesBorder;
    }

    public static boolean toggleChatEdgeBlur() {
        ensureLoaded();
        chatEdgeBlur = !chatEdgeBlur;
        saveBoolean("chatEdgeBlur", chatEdgeBlur);
        return chatEdgeBlur;
    }

    public static void setFolderTabsStyle(int style) {
        ensureLoaded();
        folderTabsStyle = style;
        saveInt("folderTabsStyle", folderTabsStyle);
    }

    public static boolean toggleShowFolderCounters() {
        ensureLoaded();
        showFolderCounters = !showFolderCounters;
        saveBoolean("showFolderCounters", showFolderCounters);
        return showFolderCounters;
    }

    public static boolean toggleRememberLastFolder() {
        ensureLoaded();
        rememberLastFolder = !rememberLastFolder;
        saveBoolean("rememberLastFolder", rememberLastFolder);
        return rememberLastFolder;
    }

    public static void setRoundingPreset(int preset) {
        ensureLoaded();
        roundingPreset = preset;
        saveInt("roundingPreset", roundingPreset);
    }

    public static int getEffectiveBubbleRadiusDp() {
        ensureLoaded();
        switch (roundingPreset) {
            case ROUNDING_ROUND:
                return 24;
            case ROUNDING_SQUIRCLE:
                return 20;
            case ROUNDING_SQUARE:
                return 8;
            case ROUNDING_DEFAULT:
            default:
                return SharedConfig.bubbleRadius > 0 ? SharedConfig.bubbleRadius : 17;
        }
    }

    public static boolean toggleScrollEdgeScale() {
        ensureLoaded();
        scrollEdgeScale = !scrollEdgeScale;
        saveBoolean("scrollEdgeScale", scrollEdgeScale);
        return scrollEdgeScale;
    }

    // --- Chats & Message Focus setters/toggles ---
    public static boolean toggleMessageFocusEnabled() {
        ensureLoaded();
        messageFocusEnabled = !messageFocusEnabled;
        saveBoolean("messageFocusEnabled", messageFocusEnabled);
        return messageFocusEnabled;
    }

    public static boolean toggleMessageFocusBlur() {
        ensureLoaded();
        messageFocusBlur = !messageFocusBlur;
        saveBoolean("messageFocusBlur", messageFocusBlur);
        return messageFocusBlur;
    }

    public static void setMessageFocusStyle(int style) {
        ensureLoaded();
        messageFocusStyle = style;
        saveInt("messageFocusStyle", messageFocusStyle);
    }

    public static void setDoubleTapAction(int value) {
        ensureLoaded();
        doubleTapAction = value;
        doubleTapActionOutgoing = value;
        doubleTapActionIncoming = value;
        saveInt("doubleTapAction", doubleTapAction);
        saveInt("doubleTapActionOutgoing", doubleTapActionOutgoing);
        saveInt("doubleTapActionIncoming", doubleTapActionIncoming);
    }

    public static void setDoubleTapActionOutgoing(int value) {
        ensureLoaded();
        doubleTapActionOutgoing = value;
        saveInt("doubleTapActionOutgoing", doubleTapActionOutgoing);
    }

    public static void setDoubleTapActionIncoming(int value) {
        ensureLoaded();
        doubleTapActionIncoming = value;
        saveInt("doubleTapActionIncoming", doubleTapActionIncoming);
    }

    public static boolean toggleHideKeyboardOnScroll() {
        ensureLoaded();
        hideKeyboardOnScroll = !hideKeyboardOnScroll;
        saveBoolean("hideKeyboardOnScroll", hideKeyboardOnScroll);
        return hideKeyboardOnScroll;
    }

    public static boolean toggleDisableNextChannelJump() {
        ensureLoaded();
        disableNextChannelJump = !disableNextChannelJump;
        saveBoolean("disableNextChannelJump", disableNextChannelJump);
        return disableNextChannelJump;
    }

    public static boolean toggleConfirmBeforeCall() {
        ensureLoaded();
        confirmBeforeCall = !confirmBeforeCall;
        saveBoolean("confirmBeforeCall", confirmBeforeCall);
        return confirmBeforeCall;
    }

    public static boolean toggleConfirmBeforeSendSticker() {
        ensureLoaded();
        confirmBeforeSendSticker = !confirmBeforeSendSticker;
        saveBoolean("confirmBeforeSendSticker", confirmBeforeSendSticker);
        return confirmBeforeSendSticker;
    }

    public static boolean toggleDisableVoiceVideoToggle() {
        ensureLoaded();
        disableVoiceVideoToggle = !disableVoiceVideoToggle;
        saveBoolean("disableVoiceVideoToggle", disableVoiceVideoToggle);
        return disableVoiceVideoToggle;
    }

    public static boolean toggleDisableProximitySensor() {
        ensureLoaded();
        disableProximitySensor = !disableProximitySensor;
        saveBoolean("disableProximitySensor", disableProximitySensor);
        return disableProximitySensor;
    }

    public static boolean toggleForwardWithoutQuote() {
        ensureLoaded();
        forwardWithoutQuote = !forwardWithoutQuote;
        saveBoolean("forwardWithoutQuote", forwardWithoutQuote);
        return forwardWithoutQuote;
    }

    public static boolean toggleHideShareButtonInChannels() {
        ensureLoaded();
        hideShareButtonInChannels = !hideShareButtonInChannels;
        saveBoolean("hideShareButtonInChannels", hideShareButtonInChannels);
        return hideShareButtonInChannels;
    }

    public static void setStickerSize(int size) {
        ensureLoaded();
        stickerSize = Math.max(4, Math.min(20, size));
        saveInt("stickerSize", stickerSize);
    }

    public static void setGifSize(int size) {
        ensureLoaded();
        gifSize = Math.max(4, Math.min(20, size));
        saveInt("gifSize", gifSize);
    }

    public static void setDeleteEffect(int effect) {
        ensureLoaded();
        deleteEffect = effect;
        saveInt("deleteEffect", deleteEffect);
    }

    public static boolean toggleShowPageUpButton() {
        ensureLoaded();
        showPageUpButton = !showPageUpButton;
        saveBoolean("showPageUpButton", showPageUpButton);
        return showPageUpButton;
    }

    public static boolean toggleUnlimitedSelection() {
        ensureLoaded();
        unlimitedSelection = !unlimitedSelection;
        saveBoolean("unlimitedSelection", unlimitedSelection);
        return unlimitedSelection;
    }

    public static boolean toggleFastSelection() {
        ensureLoaded();
        fastSelection = !fastSelection;
        saveBoolean("fastSelection", fastSelection);
        return fastSelection;
    }

    public static boolean toggleShowWeekdayInDate() {
        ensureLoaded();
        showWeekdayInDate = !showWeekdayInDate;
        saveBoolean("showWeekdayInDate", showWeekdayInDate);
        return showWeekdayInDate;
    }

    public static boolean toggleShowForwardTime() {
        ensureLoaded();
        showForwardTime = !showForwardTime;
        saveBoolean("showForwardTime", showForwardTime);
        return showForwardTime;
    }

    public static boolean toggleShowPollPreview() {
        ensureLoaded();
        showPollPreview = !showPollPreview;
        saveBoolean("showPollPreview", showPollPreview);
        return showPollPreview;
    }

    public static boolean toggleShowStickerPackOwner() {
        ensureLoaded();
        showStickerPackOwner = !showStickerPackOwner;
        saveBoolean("showStickerPackOwner", showStickerPackOwner);
        return showStickerPackOwner;
    }

    public static boolean toggleShowForwardsCounter() {
        ensureLoaded();
        showForwardsCounter = !showForwardsCounter;
        saveBoolean("showForwardsCounter", showForwardsCounter);
        return showForwardsCounter;
    }

    public static boolean toggleTextAppearAnimation() {
        ensureLoaded();
        textAppearAnimation = !textAppearAnimation;
        saveBoolean("textAppearAnimation", textAppearAnimation);
        return textAppearAnimation;
    }

    // --- Media & Network setters/toggles ---
    public static boolean toggleTurboDownloads() {
        ensureLoaded();
        turboDownloads = !turboDownloads;
        downloadSpeedBoost = turboDownloads ? DOWNLOAD_BOOST_FAST : DOWNLOAD_BOOST_OFF;
        saveBoolean("turboDownloads", turboDownloads);
        saveInt("downloadSpeedBoost", downloadSpeedBoost);
        return turboDownloads;
    }

    public static void setDownloadSpeedBoost(int level) {
        ensureLoaded();
        downloadSpeedBoost = level;
        turboDownloads = level > DOWNLOAD_BOOST_OFF;
        saveInt("downloadSpeedBoost", downloadSpeedBoost);
        saveBoolean("turboDownloads", turboDownloads);
    }

    public static boolean toggleTurboUploads() {
        ensureLoaded();
        turboUploads = !turboUploads;
        saveBoolean("turboUploads", turboUploads);
        return turboUploads;
    }

    public static boolean toggleSendPhotosInHd() {
        ensureLoaded();
        sendPhotosInHd = !sendPhotosInHd;
        saveBoolean("sendPhotosInHd", sendPhotosInHd);
        return sendPhotosInHd;
    }

    public static boolean toggleRearCameraForRoundVideo() {
        ensureLoaded();
        rearCameraForRoundVideo = !rearCameraForRoundVideo;
        saveBoolean("rearCameraForRoundVideo", rearCameraForRoundVideo);
        return rearCameraForRoundVideo;
    }

    public static boolean togglePauseVideoOnMinimize() {
        ensureLoaded();
        pauseVideoOnMinimize = !pauseVideoOnMinimize;
        saveBoolean("pauseVideoOnMinimize", pauseVideoOnMinimize);
        return pauseVideoOnMinimize;
    }

    public static boolean toggleAutoplayVoiceInRow() {
        ensureLoaded();
        autoplayVoiceInRow = !autoplayVoiceInRow;
        saveBoolean("autoplayVoiceInRow", autoplayVoiceInRow);
        return autoplayVoiceInRow;
    }

    public static boolean toggleAutoplayRoundInRow() {
        ensureLoaded();
        autoplayRoundInRow = !autoplayRoundInRow;
        saveBoolean("autoplayRoundInRow", autoplayRoundInRow);
        return autoplayRoundInRow;
    }

    public static boolean toggleAutoplayMusicInRow() {
        ensureLoaded();
        autoplayMusicInRow = !autoplayMusicInRow;
        saveBoolean("autoplayMusicInRow", autoplayMusicInRow);
        return autoplayMusicInRow;
    }

    public static boolean toggleMediaGlow() {
        ensureLoaded();
        mediaGlow = !mediaGlow;
        saveBoolean("mediaGlow", mediaGlow);
        return mediaGlow;
    }

    public static boolean toggleCompactRoundCamera() {
        ensureLoaded();
        compactRoundCamera = !compactRoundCamera;
        saveBoolean("compactRoundCamera", compactRoundCamera);
        return compactRoundCamera;
    }

    // --- Performance & General setters/toggles ---
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

    public static boolean toggleHideSimilarChannels() {
        ensureLoaded();
        hideSimilarChannels = !hideSimilarChannels;
        saveBoolean("hideSimilarChannels", hideSimilarChannels);
        return hideSimilarChannels;
    }

    public static void setForcePerformanceClass(int value) {
        ensureLoaded();
        forcePerformanceClass = value;
        saveInt("forcePerformanceClass", forcePerformanceClass);
    }

    public static boolean toggleShowJoinDate() {
        ensureLoaded();
        showJoinDate = !showJoinDate;
        saveBoolean("showJoinDate", showJoinDate);
        return showJoinDate;
    }

    public static boolean toggleOpenArchiveOnPull() {
        ensureLoaded();
        openArchiveOnPull = !openArchiveOnPull;
        saveBoolean("openArchiveOnPull", openArchiveOnPull);
        return openArchiveOnPull;
    }

    public static void setHapticsLevel(int level) {
        ensureLoaded();
        hapticsLevel = level;
        saveInt("hapticsLevel", hapticsLevel);
    }

    // --- Privacy & Ghost Mode setters/toggles ---
    public static boolean toggleGhostMode() {
        ensureLoaded();
        ghostModeEnabled = !ghostModeEnabled;
        saveBoolean("ghostModeEnabled", ghostModeEnabled);
        return ghostModeEnabled;
    }

    public static boolean toggleGhostDontReadMessages() {
        ensureLoaded();
        ghostDontReadMessages = !ghostDontReadMessages;
        saveBoolean("ghostDontReadMessages", ghostDontReadMessages);
        return ghostDontReadMessages;
    }

    public static boolean toggleGhostDontReadStories() {
        ensureLoaded();
        ghostDontReadStories = !ghostDontReadStories;
        saveBoolean("ghostDontReadStories", ghostDontReadStories);
        return ghostDontReadStories;
    }

    public static boolean toggleGhostDontSendTyping() {
        ensureLoaded();
        ghostDontSendTyping = !ghostDontSendTyping;
        saveBoolean("ghostDontSendTyping", ghostDontSendTyping);
        return ghostDontSendTyping;
    }

    public static boolean toggleGhostDontSendOnline() {
        ensureLoaded();
        ghostDontSendOnline = !ghostDontSendOnline;
        saveBoolean("ghostDontSendOnline", ghostDontSendOnline);
        return ghostDontSendOnline;
    }

    public static boolean toggleGhostSendOfflineAfterSend() {
        ensureLoaded();
        ghostSendOfflineAfterSend = !ghostSendOfflineAfterSend;
        saveBoolean("ghostSendOfflineAfterSend", ghostSendOfflineAfterSend);
        return ghostSendOfflineAfterSend;
    }

    public static boolean toggleGhostMarkReadOnSend() {
        ensureLoaded();
        ghostMarkReadOnSend = !ghostMarkReadOnSend;
        saveBoolean("ghostMarkReadOnSend", ghostMarkReadOnSend);
        return ghostMarkReadOnSend;
    }

    public static boolean isGhostReadMessagesActive() {
        ensureLoaded();
        return ghostModeEnabled && ghostDontReadMessages;
    }

    public static boolean isGhostReadStoriesActive() {
        ensureLoaded();
        return ghostModeEnabled && ghostDontReadStories;
    }

    public static boolean isGhostTypingActive() {
        ensureLoaded();
        return ghostModeEnabled && ghostDontSendTyping;
    }

    public static boolean isGhostOnlineActive() {
        ensureLoaded();
        return ghostModeEnabled && ghostDontSendOnline;
    }

    // --- History & Anti-Restrictions setters/toggles ---
    public static boolean toggleSaveDeletedMessages() {
        ensureLoaded();
        saveDeletedMessages = !saveDeletedMessages;
        saveBoolean("saveDeletedMessages", saveDeletedMessages);
        return saveDeletedMessages;
    }

    public static boolean toggleSaveEditedMessages() {
        ensureLoaded();
        saveEditedMessages = !saveEditedMessages;
        saveBoolean("saveEditedMessages", saveEditedMessages);
        return saveEditedMessages;
    }

    public static boolean toggleAllowScreenshots() {
        ensureLoaded();
        allowScreenshots = !allowScreenshots;
        saveBoolean("allowScreenshots", allowScreenshots);
        return allowScreenshots;
    }

    public static boolean toggleBypassNoForwards() {
        ensureLoaded();
        bypassNoForwards = !bypassNoForwards;
        saveBoolean("bypassNoForwards", bypassNoForwards);
        return bypassNoForwards;
    }

    // --- Export / Import / Reset ---
    public static String exportToJson() {
        ensureLoaded();
        try {
            JSONObject obj = new JSONObject();
            obj.put("ios27BottomBar", ios27BottomBar);
            obj.put("amoledSettingsBackdrop", amoledSettingsBackdrop);
            obj.put("animationSpeed", animationSpeed);
            obj.put("hideStories", hideStories);
            obj.put("hideStoriesInProfile", hideStoriesInProfile);
            obj.put("hideStoryPosting", hideStoryPosting);
            obj.put("hideAllChatsFolder", hideAllChatsFolder);
            obj.put("hidePhoneNumber", hidePhoneNumber);
            obj.put("hideTelegramBloat", hideTelegramBloat);
            obj.put("showSecondsInTime", showSecondsInTime);
            obj.put("showIdAndDc", showIdAndDc);
            obj.put("idFormatBotApi", idFormatBotApi);
            obj.put("useSystemFonts", useSystemFonts);
            obj.put("showBubbleTails", showBubbleTails);
            obj.put("glassMessages", glassMessages);
            obj.put("glassMessagesAlpha", glassMessagesAlpha);
            obj.put("glassMessagesBorder", glassMessagesBorder);
            obj.put("chatEdgeBlur", chatEdgeBlur);
            obj.put("folderTabsStyle", folderTabsStyle);
            obj.put("showFolderCounters", showFolderCounters);
            obj.put("rememberLastFolder", rememberLastFolder);
            obj.put("roundingPreset", roundingPreset);
            obj.put("scrollEdgeScale", scrollEdgeScale);
            obj.put("messageFocusEnabled", messageFocusEnabled);
            obj.put("messageFocusBlur", messageFocusBlur);
            obj.put("messageFocusStyle", messageFocusStyle);
            obj.put("doubleTapAction", doubleTapAction);
            obj.put("doubleTapActionOutgoing", doubleTapActionOutgoing);
            obj.put("doubleTapActionIncoming", doubleTapActionIncoming);
            obj.put("hideKeyboardOnScroll", hideKeyboardOnScroll);
            obj.put("disableNextChannelJump", disableNextChannelJump);
            obj.put("confirmBeforeCall", confirmBeforeCall);
            obj.put("confirmBeforeSendSticker", confirmBeforeSendSticker);
            obj.put("disableVoiceVideoToggle", disableVoiceVideoToggle);
            obj.put("disableProximitySensor", disableProximitySensor);
            obj.put("forwardWithoutQuote", forwardWithoutQuote);
            obj.put("hideShareButtonInChannels", hideShareButtonInChannels);
            obj.put("stickerSize", stickerSize);
            obj.put("gifSize", gifSize);
            obj.put("deleteEffect", deleteEffect);
            obj.put("showPageUpButton", showPageUpButton);
            obj.put("unlimitedSelection", unlimitedSelection);
            obj.put("fastSelection", fastSelection);
            obj.put("showWeekdayInDate", showWeekdayInDate);
            obj.put("showForwardTime", showForwardTime);
            obj.put("showPollPreview", showPollPreview);
            obj.put("showStickerPackOwner", showStickerPackOwner);
            obj.put("showForwardsCounter", showForwardsCounter);
            obj.put("textAppearAnimation", textAppearAnimation);
            obj.put("downloadSpeedBoost", downloadSpeedBoost);
            obj.put("turboUploads", turboUploads);
            obj.put("sendPhotosInHd", sendPhotosInHd);
            obj.put("rearCameraForRoundVideo", rearCameraForRoundVideo);
            obj.put("pauseVideoOnMinimize", pauseVideoOnMinimize);
            obj.put("autoplayVoiceInRow", autoplayVoiceInRow);
            obj.put("autoplayRoundInRow", autoplayRoundInRow);
            obj.put("autoplayMusicInRow", autoplayMusicInRow);
            obj.put("mediaGlow", mediaGlow);
            obj.put("compactRoundCamera", compactRoundCamera);
            obj.put("highRefreshRateScroll", highRefreshRateScroll);
            obj.put("disableSponsoredMessages", disableSponsoredMessages);
            obj.put("hideSimilarChannels", hideSimilarChannels);
            obj.put("forcePerformanceClass", forcePerformanceClass);
            obj.put("showJoinDate", showJoinDate);
            obj.put("openArchiveOnPull", openArchiveOnPull);
            obj.put("hapticsLevel", hapticsLevel);
            obj.put("ghostModeEnabled", ghostModeEnabled);
            obj.put("ghostDontReadMessages", ghostDontReadMessages);
            obj.put("ghostDontReadStories", ghostDontReadStories);
            obj.put("ghostDontSendTyping", ghostDontSendTyping);
            obj.put("ghostDontSendOnline", ghostDontSendOnline);
            obj.put("ghostSendOfflineAfterSend", ghostSendOfflineAfterSend);
            obj.put("ghostMarkReadOnSend", ghostMarkReadOnSend);
            obj.put("saveDeletedMessages", saveDeletedMessages);
            obj.put("saveEditedMessages", saveEditedMessages);
            obj.put("allowScreenshots", allowScreenshots);
            obj.put("bypassNoForwards", bypassNoForwards);
            return obj.toString(2);
        } catch (Exception e) {
            FileLog.e(e);
            return "{}";
        }
    }

    public static boolean importFromJson(String json) {
        if (json == null || json.trim().isEmpty() || ApplicationLoader.applicationContext == null) {
            return false;
        }
        try {
            JSONObject obj = new JSONObject(json.trim());
            SharedPreferences.Editor editor = ApplicationLoader.applicationContext
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit();
            if (obj.has("ios27BottomBar")) editor.putBoolean("ios27BottomBar", obj.getBoolean("ios27BottomBar"));
            if (obj.has("amoledSettingsBackdrop")) editor.putBoolean("amoledSettingsBackdrop", obj.getBoolean("amoledSettingsBackdrop"));
            if (obj.has("animationSpeed")) editor.putInt("animationSpeed", obj.getInt("animationSpeed"));
            if (obj.has("hideStories")) editor.putBoolean("hideStories", obj.getBoolean("hideStories"));
            if (obj.has("hideStoriesInProfile")) editor.putBoolean("hideStoriesInProfile", obj.getBoolean("hideStoriesInProfile"));
            if (obj.has("hideStoryPosting")) editor.putBoolean("hideStoryPosting", obj.getBoolean("hideStoryPosting"));
            if (obj.has("hideAllChatsFolder")) editor.putBoolean("hideAllChatsFolder", obj.getBoolean("hideAllChatsFolder"));
            if (obj.has("hidePhoneNumber")) editor.putBoolean("hidePhoneNumber", obj.getBoolean("hidePhoneNumber"));
            if (obj.has("hideTelegramBloat")) editor.putBoolean("hideTelegramBloat", obj.getBoolean("hideTelegramBloat"));
            if (obj.has("showSecondsInTime")) editor.putBoolean("showSecondsInTime", obj.getBoolean("showSecondsInTime"));
            if (obj.has("showIdAndDc")) editor.putBoolean("showIdAndDc", obj.getBoolean("showIdAndDc"));
            if (obj.has("idFormatBotApi")) editor.putBoolean("idFormatBotApi", obj.getBoolean("idFormatBotApi"));
            if (obj.has("useSystemFonts")) editor.putBoolean("useSystemFonts", obj.getBoolean("useSystemFonts"));
            if (obj.has("showBubbleTails")) editor.putBoolean("showBubbleTails", obj.getBoolean("showBubbleTails"));
            if (obj.has("glassMessages")) editor.putBoolean("glassMessages", obj.getBoolean("glassMessages"));
            if (obj.has("glassMessagesAlpha")) editor.putInt("glassMessagesAlpha", obj.getInt("glassMessagesAlpha"));
            if (obj.has("glassMessagesBorder")) editor.putBoolean("glassMessagesBorder", obj.getBoolean("glassMessagesBorder"));
            if (obj.has("chatEdgeBlur")) editor.putBoolean("chatEdgeBlur", obj.getBoolean("chatEdgeBlur"));
            if (obj.has("folderTabsStyle")) editor.putInt("folderTabsStyle", obj.getInt("folderTabsStyle"));
            if (obj.has("showFolderCounters")) editor.putBoolean("showFolderCounters", obj.getBoolean("showFolderCounters"));
            if (obj.has("rememberLastFolder")) editor.putBoolean("rememberLastFolder", obj.getBoolean("rememberLastFolder"));
            if (obj.has("roundingPreset")) editor.putInt("roundingPreset", obj.getInt("roundingPreset"));
            if (obj.has("scrollEdgeScale")) editor.putBoolean("scrollEdgeScale", obj.getBoolean("scrollEdgeScale"));
            if (obj.has("messageFocusEnabled")) editor.putBoolean("messageFocusEnabled", obj.getBoolean("messageFocusEnabled"));
            if (obj.has("messageFocusBlur")) editor.putBoolean("messageFocusBlur", obj.getBoolean("messageFocusBlur"));
            if (obj.has("messageFocusStyle")) editor.putInt("messageFocusStyle", obj.getInt("messageFocusStyle"));
            if (obj.has("doubleTapAction")) editor.putInt("doubleTapAction", obj.getInt("doubleTapAction"));
            if (obj.has("doubleTapActionOutgoing")) editor.putInt("doubleTapActionOutgoing", obj.getInt("doubleTapActionOutgoing"));
            if (obj.has("doubleTapActionIncoming")) editor.putInt("doubleTapActionIncoming", obj.getInt("doubleTapActionIncoming"));
            if (obj.has("hideKeyboardOnScroll")) editor.putBoolean("hideKeyboardOnScroll", obj.getBoolean("hideKeyboardOnScroll"));
            if (obj.has("disableNextChannelJump")) editor.putBoolean("disableNextChannelJump", obj.getBoolean("disableNextChannelJump"));
            if (obj.has("confirmBeforeCall")) editor.putBoolean("confirmBeforeCall", obj.getBoolean("confirmBeforeCall"));
            if (obj.has("confirmBeforeSendSticker")) editor.putBoolean("confirmBeforeSendSticker", obj.getBoolean("confirmBeforeSendSticker"));
            if (obj.has("disableVoiceVideoToggle")) editor.putBoolean("disableVoiceVideoToggle", obj.getBoolean("disableVoiceVideoToggle"));
            if (obj.has("disableProximitySensor")) editor.putBoolean("disableProximitySensor", obj.getBoolean("disableProximitySensor"));
            if (obj.has("forwardWithoutQuote")) editor.putBoolean("forwardWithoutQuote", obj.getBoolean("forwardWithoutQuote"));
            if (obj.has("hideShareButtonInChannels")) editor.putBoolean("hideShareButtonInChannels", obj.getBoolean("hideShareButtonInChannels"));
            if (obj.has("stickerSize")) editor.putInt("stickerSize", obj.getInt("stickerSize"));
            if (obj.has("gifSize")) editor.putInt("gifSize", obj.getInt("gifSize"));
            if (obj.has("deleteEffect")) editor.putInt("deleteEffect", obj.getInt("deleteEffect"));
            if (obj.has("showPageUpButton")) editor.putBoolean("showPageUpButton", obj.getBoolean("showPageUpButton"));
            if (obj.has("unlimitedSelection")) editor.putBoolean("unlimitedSelection", obj.getBoolean("unlimitedSelection"));
            if (obj.has("fastSelection")) editor.putBoolean("fastSelection", obj.getBoolean("fastSelection"));
            if (obj.has("showWeekdayInDate")) editor.putBoolean("showWeekdayInDate", obj.getBoolean("showWeekdayInDate"));
            if (obj.has("showForwardTime")) editor.putBoolean("showForwardTime", obj.getBoolean("showForwardTime"));
            if (obj.has("showPollPreview")) editor.putBoolean("showPollPreview", obj.getBoolean("showPollPreview"));
            if (obj.has("showStickerPackOwner")) editor.putBoolean("showStickerPackOwner", obj.getBoolean("showStickerPackOwner"));
            if (obj.has("showForwardsCounter")) editor.putBoolean("showForwardsCounter", obj.getBoolean("showForwardsCounter"));
            if (obj.has("textAppearAnimation")) editor.putBoolean("textAppearAnimation", obj.getBoolean("textAppearAnimation"));
            if (obj.has("downloadSpeedBoost")) {
                int boost = obj.getInt("downloadSpeedBoost");
                editor.putInt("downloadSpeedBoost", boost);
                editor.putBoolean("turboDownloads", boost > DOWNLOAD_BOOST_OFF);
            }
            if (obj.has("turboUploads")) editor.putBoolean("turboUploads", obj.getBoolean("turboUploads"));
            if (obj.has("sendPhotosInHd")) editor.putBoolean("sendPhotosInHd", obj.getBoolean("sendPhotosInHd"));
            if (obj.has("rearCameraForRoundVideo")) editor.putBoolean("rearCameraForRoundVideo", obj.getBoolean("rearCameraForRoundVideo"));
            if (obj.has("pauseVideoOnMinimize")) editor.putBoolean("pauseVideoOnMinimize", obj.getBoolean("pauseVideoOnMinimize"));
            if (obj.has("autoplayVoiceInRow")) editor.putBoolean("autoplayVoiceInRow", obj.getBoolean("autoplayVoiceInRow"));
            if (obj.has("autoplayRoundInRow")) editor.putBoolean("autoplayRoundInRow", obj.getBoolean("autoplayRoundInRow"));
            if (obj.has("autoplayMusicInRow")) editor.putBoolean("autoplayMusicInRow", obj.getBoolean("autoplayMusicInRow"));
            if (obj.has("mediaGlow")) editor.putBoolean("mediaGlow", obj.getBoolean("mediaGlow"));
            if (obj.has("compactRoundCamera")) editor.putBoolean("compactRoundCamera", obj.getBoolean("compactRoundCamera"));
            if (obj.has("highRefreshRateScroll")) editor.putBoolean("highRefreshRateScroll", obj.getBoolean("highRefreshRateScroll"));
            if (obj.has("disableSponsoredMessages")) editor.putBoolean("disableSponsoredMessages", obj.getBoolean("disableSponsoredMessages"));
            if (obj.has("hideSimilarChannels")) editor.putBoolean("hideSimilarChannels", obj.getBoolean("hideSimilarChannels"));
            if (obj.has("forcePerformanceClass")) editor.putInt("forcePerformanceClass", obj.getInt("forcePerformanceClass"));
            if (obj.has("showJoinDate")) editor.putBoolean("showJoinDate", obj.getBoolean("showJoinDate"));
            if (obj.has("openArchiveOnPull")) editor.putBoolean("openArchiveOnPull", obj.getBoolean("openArchiveOnPull"));
            if (obj.has("hapticsLevel")) editor.putInt("hapticsLevel", obj.getInt("hapticsLevel"));
            if (obj.has("ghostModeEnabled")) editor.putBoolean("ghostModeEnabled", obj.getBoolean("ghostModeEnabled"));
            if (obj.has("ghostDontReadMessages")) editor.putBoolean("ghostDontReadMessages", obj.getBoolean("ghostDontReadMessages"));
            if (obj.has("ghostDontReadStories")) editor.putBoolean("ghostDontReadStories", obj.getBoolean("ghostDontReadStories"));
            if (obj.has("ghostDontSendTyping")) editor.putBoolean("ghostDontSendTyping", obj.getBoolean("ghostDontSendTyping"));
            if (obj.has("ghostDontSendOnline")) editor.putBoolean("ghostDontSendOnline", obj.getBoolean("ghostDontSendOnline"));
            if (obj.has("ghostSendOfflineAfterSend")) editor.putBoolean("ghostSendOfflineAfterSend", obj.getBoolean("ghostSendOfflineAfterSend"));
            if (obj.has("ghostMarkReadOnSend")) editor.putBoolean("ghostMarkReadOnSend", obj.getBoolean("ghostMarkReadOnSend"));
            if (obj.has("saveDeletedMessages")) editor.putBoolean("saveDeletedMessages", obj.getBoolean("saveDeletedMessages"));
            if (obj.has("saveEditedMessages")) editor.putBoolean("saveEditedMessages", obj.getBoolean("saveEditedMessages"));
            if (obj.has("allowScreenshots")) editor.putBoolean("allowScreenshots", obj.getBoolean("allowScreenshots"));
            if (obj.has("bypassNoForwards")) editor.putBoolean("bypassNoForwards", obj.getBoolean("bypassNoForwards"));
            editor.apply();
            loaded = false;
            ensureLoaded();
            return true;
        } catch (Exception e) {
            FileLog.e(e);
            return false;
        }
    }

    public static void resetToDefaults() {
        if (ApplicationLoader.applicationContext != null) {
            ApplicationLoader.applicationContext
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
        }
        loaded = false;
        ensureLoaded();
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
