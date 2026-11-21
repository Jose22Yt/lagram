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
    private static boolean loaded;

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

    // Appearance & UI
    public static boolean ios27BottomBar = true;
    public static boolean amoledSettingsBackdrop = true;
    public static int animationSpeed = ANIM_SPEED_FAST;
    public static boolean hideStories = false;
    public static boolean hideAllChatsFolder = false;
    public static boolean hidePhoneNumber = false;
    public static boolean hideTelegramBloat = false;
    public static boolean showSecondsInTime = false;
    public static boolean showIdAndDc = true;
    public static boolean idFormatBotApi = false;
    public static boolean useSystemFonts = false;

    // Chats & Messages
    public static int doubleTapAction = DOUBLE_TAP_REACTION;
    public static boolean hideKeyboardOnScroll = false;
    public static boolean disableNextChannelJump = false;
    public static boolean confirmBeforeCall = true;
    public static boolean confirmBeforeSendSticker = false;
    public static boolean disableVoiceVideoToggle = false;
    public static boolean disableProximitySensor = false;
    public static boolean forwardWithoutQuote = false;
    public static boolean hideShareButtonInChannels = false;
    public static int stickerSize = 14;

    // Media & Network
    public static boolean turboDownloads = true;
    public static int downloadSpeedBoost = DOWNLOAD_BOOST_FAST;
    public static boolean turboUploads = true;
    public static boolean sendPhotosInHd = false;
    public static boolean rearCameraForRoundVideo = false;
    public static boolean pauseVideoOnMinimize = false;

    // Performance & General
    public static boolean highRefreshRateScroll = true;
    public static boolean disableSponsoredMessages = true;
    public static boolean hideSimilarChannels = false;
    public static int forcePerformanceClass = -1; // -1 = Auto, 0 = Low, 1 = Average, 2 = High

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
        hideAllChatsFolder = prefs.getBoolean("hideAllChatsFolder", false);
        hidePhoneNumber = prefs.getBoolean("hidePhoneNumber", false);
        hideTelegramBloat = prefs.getBoolean("hideTelegramBloat", false);
        showSecondsInTime = prefs.getBoolean("showSecondsInTime", false);
        showIdAndDc = prefs.getBoolean("showIdAndDc", true);
        idFormatBotApi = prefs.getBoolean("idFormatBotApi", false);
        useSystemFonts = prefs.getBoolean("useSystemFonts", false);

        // Chats
        doubleTapAction = prefs.getInt("doubleTapAction", DOUBLE_TAP_REACTION);
        hideKeyboardOnScroll = prefs.getBoolean("hideKeyboardOnScroll", false);
        disableNextChannelJump = prefs.getBoolean("disableNextChannelJump", false);
        confirmBeforeCall = prefs.getBoolean("confirmBeforeCall", true);
        confirmBeforeSendSticker = prefs.getBoolean("confirmBeforeSendSticker", false);
        disableVoiceVideoToggle = prefs.getBoolean("disableVoiceVideoToggle", false);
        disableProximitySensor = prefs.getBoolean("disableProximitySensor", false);
        forwardWithoutQuote = prefs.getBoolean("forwardWithoutQuote", false);
        hideShareButtonInChannels = prefs.getBoolean("hideShareButtonInChannels", false);
        stickerSize = prefs.getInt("stickerSize", 14);

        // Media & Network
        turboDownloads = prefs.getBoolean("turboDownloads", true);
        downloadSpeedBoost = prefs.getInt("downloadSpeedBoost", turboDownloads ? DOWNLOAD_BOOST_FAST : DOWNLOAD_BOOST_OFF);
        turboUploads = prefs.getBoolean("turboUploads", true);
        sendPhotosInHd = prefs.getBoolean("sendPhotosInHd", false);
        rearCameraForRoundVideo = prefs.getBoolean("rearCameraForRoundVideo", false);
        pauseVideoOnMinimize = prefs.getBoolean("pauseVideoOnMinimize", false);

        // Performance & General
        highRefreshRateScroll = prefs.getBoolean("highRefreshRateScroll", true);
        disableSponsoredMessages = prefs.getBoolean("disableSponsoredMessages", true);
        hideSimilarChannels = prefs.getBoolean("hideSimilarChannels", false);
        forcePerformanceClass = prefs.getInt("forcePerformanceClass", -1);

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

    // --- Chats setters/toggles ---
    public static void setDoubleTapAction(int value) {
        ensureLoaded();
        doubleTapAction = value;
        saveInt("doubleTapAction", doubleTapAction);
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
            obj.put("hideAllChatsFolder", hideAllChatsFolder);
            obj.put("hidePhoneNumber", hidePhoneNumber);
            obj.put("hideTelegramBloat", hideTelegramBloat);
            obj.put("showSecondsInTime", showSecondsInTime);
            obj.put("showIdAndDc", showIdAndDc);
            obj.put("idFormatBotApi", idFormatBotApi);
            obj.put("useSystemFonts", useSystemFonts);
            obj.put("doubleTapAction", doubleTapAction);
            obj.put("hideKeyboardOnScroll", hideKeyboardOnScroll);
            obj.put("disableNextChannelJump", disableNextChannelJump);
            obj.put("confirmBeforeCall", confirmBeforeCall);
            obj.put("confirmBeforeSendSticker", confirmBeforeSendSticker);
            obj.put("disableVoiceVideoToggle", disableVoiceVideoToggle);
            obj.put("disableProximitySensor", disableProximitySensor);
            obj.put("forwardWithoutQuote", forwardWithoutQuote);
            obj.put("hideShareButtonInChannels", hideShareButtonInChannels);
            obj.put("stickerSize", stickerSize);
            obj.put("downloadSpeedBoost", downloadSpeedBoost);
            obj.put("turboUploads", turboUploads);
            obj.put("sendPhotosInHd", sendPhotosInHd);
            obj.put("rearCameraForRoundVideo", rearCameraForRoundVideo);
            obj.put("pauseVideoOnMinimize", pauseVideoOnMinimize);
            obj.put("highRefreshRateScroll", highRefreshRateScroll);
            obj.put("disableSponsoredMessages", disableSponsoredMessages);
            obj.put("hideSimilarChannels", hideSimilarChannels);
            obj.put("forcePerformanceClass", forcePerformanceClass);
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
            if (obj.has("hideAllChatsFolder")) editor.putBoolean("hideAllChatsFolder", obj.getBoolean("hideAllChatsFolder"));
            if (obj.has("hidePhoneNumber")) editor.putBoolean("hidePhoneNumber", obj.getBoolean("hidePhoneNumber"));
            if (obj.has("hideTelegramBloat")) editor.putBoolean("hideTelegramBloat", obj.getBoolean("hideTelegramBloat"));
            if (obj.has("showSecondsInTime")) editor.putBoolean("showSecondsInTime", obj.getBoolean("showSecondsInTime"));
            if (obj.has("showIdAndDc")) editor.putBoolean("showIdAndDc", obj.getBoolean("showIdAndDc"));
            if (obj.has("idFormatBotApi")) editor.putBoolean("idFormatBotApi", obj.getBoolean("idFormatBotApi"));
            if (obj.has("useSystemFonts")) editor.putBoolean("useSystemFonts", obj.getBoolean("useSystemFonts"));
            if (obj.has("doubleTapAction")) editor.putInt("doubleTapAction", obj.getInt("doubleTapAction"));
            if (obj.has("hideKeyboardOnScroll")) editor.putBoolean("hideKeyboardOnScroll", obj.getBoolean("hideKeyboardOnScroll"));
            if (obj.has("disableNextChannelJump")) editor.putBoolean("disableNextChannelJump", obj.getBoolean("disableNextChannelJump"));
            if (obj.has("confirmBeforeCall")) editor.putBoolean("confirmBeforeCall", obj.getBoolean("confirmBeforeCall"));
            if (obj.has("confirmBeforeSendSticker")) editor.putBoolean("confirmBeforeSendSticker", obj.getBoolean("confirmBeforeSendSticker"));
            if (obj.has("disableVoiceVideoToggle")) editor.putBoolean("disableVoiceVideoToggle", obj.getBoolean("disableVoiceVideoToggle"));
            if (obj.has("disableProximitySensor")) editor.putBoolean("disableProximitySensor", obj.getBoolean("disableProximitySensor"));
            if (obj.has("forwardWithoutQuote")) editor.putBoolean("forwardWithoutQuote", obj.getBoolean("forwardWithoutQuote"));
            if (obj.has("hideShareButtonInChannels")) editor.putBoolean("hideShareButtonInChannels", obj.getBoolean("hideShareButtonInChannels"));
            if (obj.has("stickerSize")) editor.putInt("stickerSize", obj.getInt("stickerSize"));
            if (obj.has("downloadSpeedBoost")) {
                int boost = obj.getInt("downloadSpeedBoost");
                editor.putInt("downloadSpeedBoost", boost);
                editor.putBoolean("turboDownloads", boost > DOWNLOAD_BOOST_OFF);
            }
            if (obj.has("turboUploads")) editor.putBoolean("turboUploads", obj.getBoolean("turboUploads"));
            if (obj.has("sendPhotosInHd")) editor.putBoolean("sendPhotosInHd", obj.getBoolean("sendPhotosInHd"));
            if (obj.has("rearCameraForRoundVideo")) editor.putBoolean("rearCameraForRoundVideo", obj.getBoolean("rearCameraForRoundVideo"));
            if (obj.has("pauseVideoOnMinimize")) editor.putBoolean("pauseVideoOnMinimize", obj.getBoolean("pauseVideoOnMinimize"));
            if (obj.has("highRefreshRateScroll")) editor.putBoolean("highRefreshRateScroll", obj.getBoolean("highRefreshRateScroll"));
            if (obj.has("disableSponsoredMessages")) editor.putBoolean("disableSponsoredMessages", obj.getBoolean("disableSponsoredMessages"));
            if (obj.has("hideSimilarChannels")) editor.putBoolean("hideSimilarChannels", obj.getBoolean("hideSimilarChannels"));
            if (obj.has("forcePerformanceClass")) editor.putInt("forcePerformanceClass", obj.getInt("forcePerformanceClass"));
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
