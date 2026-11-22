package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class LagramAppearanceActivity extends UniversalFragment {

    private static final int ID_PREVIEW_BUBBLES = 1;
    private static final int ID_GLASS_MESSAGES = 2;
    private static final int ID_GLASS_ALPHA = 3;
    private static final int ID_GLASS_BORDER = 4;
    private static final int ID_BUBBLE_TAILS = 5;
    private static final int ID_ROUNDING_PRESET = 6;
    private static final int ID_CHAT_EDGE_BLUR = 7;
    private static final int ID_SCROLL_EDGE_SCALE = 8;
    private static final int ID_IOS27_BOTTOM_BAR = 9;
    private static final int ID_AMOLED_BACKDROP = 10;
    private static final int ID_ANIM_SPEED = 11;
    private static final int ID_SYSTEM_FONTS = 12;

    private static final int ID_PREVIEW_FOLDERS = 20;
    private static final int ID_FOLDER_TABS_STYLE = 21;
    private static final int ID_FOLDER_COUNTERS = 22;
    private static final int ID_HIDE_ALL_CHATS = 23;
    private static final int ID_REMEMBER_LAST_FOLDER = 24;

    private static final int ID_HIDE_STORIES = 30;
    private static final int ID_HIDE_STORIES_PROFILE = 31;
    private static final int ID_HIDE_STORY_POSTING = 32;
    private static final int ID_HIDE_PHONE_NUMBER = 33;
    private static final int ID_HIDE_TELEGRAM_BLOAT = 34;

    private static final int ID_SHOW_SECONDS = 40;
    private static final int ID_SHOW_ID_DC = 41;
    private static final int ID_ID_BOT_API = 42;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryAppearance);
    }

    @Override
    public View createView(Context context) {
        LagramConfig.ensureLoaded();
        View view = super.createView(context);
        listView.setSections();
        updateBackdropColor();
        return view;
    }

    private void updateBackdropColor() {
        if (fragmentView == null || listView == null) {
            return;
        }
        if (LagramConfig.amoledSettingsBackdrop && Theme.isCurrentThemeDark()) {
            fragmentView.setBackgroundColor(0xFF000000);
            listView.setBackgroundColor(0xFF000000);
        } else {
            int bg = Theme.getColor(Theme.key_windowBackgroundGray);
            fragmentView.setBackgroundColor(bg);
            listView.setBackgroundColor(bg);
        }
    }

    private void refreshLivePreviews() {
        if (listView == null) {
            return;
        }
        for (int i = 0, count = listView.getChildCount(); i < count; i++) {
            View child = listView.getChildAt(i);
            if (child instanceof LagramSettingsComponents.ChatAppearancePreviewCell
                    || child instanceof LagramSettingsComponents.FolderTabsPreviewCell) {
                child.invalidate();
            }
        }
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // Section 1: Liquid Glass & Bubbles with Interactive Live Preview Card
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramLiquidGlassHeader)));
        items.add(LagramSettingsComponents.ChatAppearancePreviewCell.Factory.of(
                ID_PREVIEW_BUBBLES, LagramSettingsComponents.ChatAppearancePreviewCell.MODE_BUBBLES_AND_GLASS));
        items.add(UItem.asCheck(ID_GLASS_MESSAGES, LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramGlassMessages)))
                .setChecked(LagramConfig.glassMessages));
        if (LagramConfig.glassMessages) {
            items.add(LagramSettingsComponents.InlineSliderCell.Factory.of(
                    ID_GLASS_ALPHA,
                    LocaleController.getString(R.string.LagramGlassMessagesAlpha),
                    25, 100, LagramConfig.glassMessagesAlpha, 75, "%",
                    val -> {
                        LagramConfig.setGlassMessagesAlpha(val);
                        refreshLivePreviews();
                    }
            ));
            items.add(UItem.asCheck(ID_GLASS_BORDER, LocaleController.getString(R.string.LagramGlassMessagesBorder))
                    .setChecked(LagramConfig.glassMessagesBorder));
        }
        items.add(UItem.asCheck(ID_BUBBLE_TAILS, LocaleController.getString(R.string.LagramShowBubbleTails))
                .setChecked(LagramConfig.showBubbleTails));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                ID_ROUNDING_PRESET,
                LocaleController.getString(R.string.LagramRoundingPreset),
                new String[]{
                        LocaleController.getString(R.string.LagramRoundingDefault),
                        LocaleController.getString(R.string.LagramRoundingRound),
                        LocaleController.getString(R.string.LagramRoundingSquircle),
                        LocaleController.getString(R.string.LagramRoundingSquare)
                },
                LagramConfig.roundingPreset,
                which -> {
                    LagramConfig.setRoundingPreset(which);
                    refreshLivePreviews();
                }
        ));
        items.add(UItem.asCheck(ID_CHAT_EDGE_BLUR, LocaleController.getString(R.string.LagramChatEdgeBlur))
                .setChecked(LagramConfig.chatEdgeBlur));
        items.add(UItem.asCheck(ID_SCROLL_EDGE_SCALE, LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramScrollEdgeScale)))
                .setChecked(LagramConfig.scrollEdgeScale));
        items.add(UItem.asCheck(ID_IOS27_BOTTOM_BAR, LocaleController.getString(R.string.LagramIos27BottomBar))
                .setChecked(LagramConfig.ios27BottomBar));
        items.add(UItem.asCheck(ID_AMOLED_BACKDROP, LocaleController.getString(R.string.LagramAmoledSettings))
                .setChecked(LagramConfig.amoledSettingsBackdrop));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                ID_ANIM_SPEED,
                LocaleController.getString(R.string.LagramAnimationSpeed),
                new String[]{
                        LocaleController.getString(R.string.LagramAnimNormal),
                        LocaleController.getString(R.string.LagramAnimFast),
                        LocaleController.getString(R.string.LagramAnimInstant)
                },
                LagramConfig.animationSpeed,
                LagramConfig::setAnimationSpeed
        ));
        items.add(UItem.asCheck(ID_SYSTEM_FONTS, LocaleController.getString(R.string.LagramUseSystemFonts))
                .setChecked(LagramConfig.useSystemFonts));
        items.add(UItem.asShadow(null));

        // Section 2: Folders with Live Folder Tabs Preview
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramFoldersHeader)));
        items.add(LagramSettingsComponents.FolderTabsPreviewCell.Factory.of(ID_PREVIEW_FOLDERS));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                ID_FOLDER_TABS_STYLE,
                LocaleController.getString(R.string.LagramFolderTabsStyle),
                new String[]{
                        LocaleController.getString(R.string.LagramFolderTabsIconText),
                        LocaleController.getString(R.string.LagramFolderTabsIconOnly),
                        LocaleController.getString(R.string.LagramFolderTabsTextOnly)
                },
                LagramConfig.folderTabsStyle,
                which -> {
                    LagramConfig.setFolderTabsStyle(which);
                    refreshLivePreviews();
                    getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                }
        ));
        items.add(UItem.asCheck(ID_FOLDER_COUNTERS, LocaleController.getString(R.string.LagramShowFolderCounters))
                .setChecked(LagramConfig.showFolderCounters));
        items.add(UItem.asCheck(ID_HIDE_ALL_CHATS, LocaleController.getString(R.string.LagramHideAllChats))
                .setChecked(LagramConfig.hideAllChatsFolder));
        items.add(UItem.asCheck(ID_REMEMBER_LAST_FOLDER, LocaleController.getString(R.string.LagramRememberLastFolder))
                .setChecked(LagramConfig.rememberLastFolder));
        items.add(UItem.asShadow(null));

        // Section 3: Clean UI & Visibility
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramUiVisibilityHeader)));
        items.add(UItem.asCheck(ID_HIDE_STORIES, LocaleController.getString(R.string.LagramHideStories))
                .setChecked(LagramConfig.hideStories));
        items.add(UItem.asCheck(ID_HIDE_STORIES_PROFILE, LocaleController.getString(R.string.LagramHideStoriesProfile))
                .setChecked(LagramConfig.hideStoriesInProfile));
        items.add(UItem.asCheck(ID_HIDE_STORY_POSTING, LocaleController.getString(R.string.LagramHideStoryPosting))
                .setChecked(LagramConfig.hideStoryPosting));
        items.add(UItem.asCheck(ID_HIDE_PHONE_NUMBER, LocaleController.getString(R.string.LagramHidePhoneNumber))
                .setChecked(LagramConfig.hidePhoneNumber));
        items.add(UItem.asCheck(ID_HIDE_TELEGRAM_BLOAT, LocaleController.getString(R.string.LagramHideTelegramBloat))
                .setChecked(LagramConfig.hideTelegramBloat));
        items.add(UItem.asShadow(null));

        // Section 4: Extra Details
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramDetailsHeader)));
        items.add(UItem.asCheck(ID_SHOW_SECONDS, LocaleController.getString(R.string.LagramShowSeconds))
                .setChecked(LagramConfig.showSecondsInTime));
        items.add(UItem.asCheck(ID_SHOW_ID_DC, LocaleController.getString(R.string.LagramShowIdAndDc))
                .setChecked(LagramConfig.showIdAndDc));
        items.add(UItem.asCheck(ID_ID_BOT_API, LocaleController.getString(R.string.LagramIdFormatBotApi))
                .setChecked(LagramConfig.idFormatBotApi));
        items.add(UItem.asShadow(LocaleController.getString(R.string.LagramAppearanceInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        boolean checkChanged = false;
        boolean newVal = false;
        switch (item.id) {
            case ID_GLASS_MESSAGES:
                newVal = LagramConfig.toggleGlassMessages();
                checkChanged = true;
                if (listView != null && listView.adapter != null) {
                    listView.adapter.update(true);
                }
                refreshLivePreviews();
                break;
            case ID_GLASS_BORDER:
                newVal = LagramConfig.toggleGlassMessagesBorder();
                checkChanged = true;
                refreshLivePreviews();
                break;
            case ID_BUBBLE_TAILS:
                newVal = LagramConfig.toggleShowBubbleTails();
                checkChanged = true;
                refreshLivePreviews();
                break;
            case ID_CHAT_EDGE_BLUR:
                newVal = LagramConfig.toggleChatEdgeBlur();
                checkChanged = true;
                break;
            case ID_SCROLL_EDGE_SCALE:
                newVal = LagramConfig.toggleScrollEdgeScale();
                checkChanged = true;
                break;
            case ID_IOS27_BOTTOM_BAR:
                newVal = LagramConfig.toggleIos27BottomBar();
                checkChanged = true;
                break;
            case ID_AMOLED_BACKDROP:
                newVal = LagramConfig.toggleAmoledSettingsBackdrop();
                checkChanged = true;
                updateBackdropColor();
                if (listView != null) listView.invalidate();
                break;
            case ID_SYSTEM_FONTS:
                newVal = LagramConfig.toggleUseSystemFonts();
                checkChanged = true;
                break;
            case ID_FOLDER_COUNTERS:
                newVal = LagramConfig.toggleShowFolderCounters();
                checkChanged = true;
                refreshLivePreviews();
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                break;
            case ID_HIDE_ALL_CHATS:
                newVal = LagramConfig.toggleHideAllChatsFolder();
                checkChanged = true;
                refreshLivePreviews();
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                break;
            case ID_REMEMBER_LAST_FOLDER:
                newVal = LagramConfig.toggleRememberLastFolder();
                checkChanged = true;
                break;
            case ID_HIDE_STORIES:
                newVal = LagramConfig.toggleHideStories();
                checkChanged = true;
                getNotificationCenter().postNotificationName(NotificationCenter.storiesEnabledUpdate);
                break;
            case ID_HIDE_STORIES_PROFILE:
                newVal = LagramConfig.toggleHideStoriesInProfile();
                checkChanged = true;
                break;
            case ID_HIDE_STORY_POSTING:
                newVal = LagramConfig.toggleHideStoryPosting();
                checkChanged = true;
                break;
            case ID_HIDE_PHONE_NUMBER:
                newVal = LagramConfig.toggleHidePhoneNumber();
                checkChanged = true;
                getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, 0);
                break;
            case ID_HIDE_TELEGRAM_BLOAT:
                newVal = LagramConfig.toggleHideTelegramBloat();
                checkChanged = true;
                break;
            case ID_SHOW_SECONDS:
                newVal = LagramConfig.toggleShowSecondsInTime();
                checkChanged = true;
                refreshLivePreviews();
                break;
            case ID_SHOW_ID_DC:
                newVal = LagramConfig.toggleShowIdAndDc();
                checkChanged = true;
                break;
            case ID_ID_BOT_API:
                newVal = LagramConfig.toggleIdFormatBotApi();
                checkChanged = true;
                break;
        }
        if (checkChanged && view instanceof TextCheckCell) {
            ((TextCheckCell) view).setChecked(newVal);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
