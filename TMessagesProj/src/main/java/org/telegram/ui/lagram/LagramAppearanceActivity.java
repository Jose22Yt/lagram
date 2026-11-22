package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class LagramAppearanceActivity extends UniversalFragment {

    private static final int ID_PREVIEW_LIST_FOLDERS = 100;
    private static final int ID_PREVIEW_BUBBLES_GLASS = 101;
    private static final int ID_PREVIEW_SCROLL = 102;

    private static final int ID_MAT_CONTAINERS = 110;
    private static final int ID_SECTION_SPACING = 111;
    private static final int ID_MAT_CONTROLS = 112;
    private static final int ID_MAT_CHAT_LIST = 113;
    private static final int ID_MAT_APP_BAR = 114;
    private static final int ID_APP_BAR_BG = 115;
    private static final int ID_MAT_PROFILE_TABS = 116;

    private static final int ID_FOLDER_BAR_STYLE = 120;
    private static final int ID_FOLDER_TABS_STYLE = 121;
    private static final int ID_SHOW_FOLDER_COUNTERS = 122;
    private static final int ID_REMEMBER_LAST_FOLDER = 123;

    private static final int ID_ROUNDING_PRESET = 130;
    private static final int ID_AVATAR_SHAPE = 131;
    private static final int ID_SHOW_BUBBLE_TAILS = 132;

    private static final int ID_GLASS_MESSAGES = 140;
    private static final int ID_GLASS_ALPHA = 141;
    private static final int ID_GLASS_BORDER = 142;
    private static final int ID_GLASS_HIGHLIGHTS = 143;
    private static final int ID_POPUP_GLASS = 144;
    private static final int ID_CHAT_EDGE_BLUR = 145;

    private static final int ID_SCROLL_EDGE = 150;
    private static final int ID_SCROLL_MODE = 151;
    private static final int ID_HIGH_REFRESH = 152;
    private static final int ID_ANIM_SPEED = 153;

    private static final int ID_AMOLED_BACKDROP = 160;
    private static final int ID_SYSTEM_FONTS = 161;
    private static final int ID_SHOW_SECONDS = 162;
    private static final int ID_CHATS_HEADER_CENTERED = 163;
    private static final int ID_HIDE_STORIES = 164;
    private static final int ID_HIDE_STORIES_PROFILE = 165;
    private static final int ID_HIDE_STORY_POSTING = 166;
    private static final int ID_HIDE_ALL_CHATS = 167;
    private static final int ID_HIDE_PHONE = 168;
    private static final int ID_HIDE_BLOAT = 169;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryAppearance);
    }

    @Override
    public View createView(Context context) {
        LagramConfig.ensureLoaded();
        View view = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        updateBackdropColor();
        return view;
    }

    private void updateBackdropColor() {
        if (fragmentView == null || listView == null) return;
        if (LagramConfig.amoledSettingsBackdrop && Theme.isCurrentThemeDark()) {
            fragmentView.setBackgroundColor(0xFF000000);
            listView.setBackgroundColor(0xFF000000);
        } else {
            int bg = Theme.getColor(Theme.key_windowBackgroundGray);
            fragmentView.setBackgroundColor(bg);
            listView.setBackgroundColor(bg);
        }
    }

    private void refreshList() {
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // 1. REAL Interactive Chat List & Folders Preview + Material Design 3
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramM3Header)));
        items.add(LagramRealPreviews.RealChatListFoldersPreviewCell.Factory.of(ID_PREVIEW_LIST_FOLDERS));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_MAT_CONTAINERS, 0,
                LocaleController.getString(R.string.LagramM3Containers),
                LocaleController.getString(R.string.LagramM3ContainersInfo),
                LagramConfig.materialContainers));
        if (LagramConfig.materialContainers) {
            items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                    ID_SECTION_SPACING,
                    LocaleController.getString(R.string.LagramM3SectionSpacing),
                    0, 16, 7, LagramConfig.sectionSpacingDp, "dp",
                    val -> {
                        LagramConfig.setSectionSpacingDp(val);
                        if (listView != null) {
                            listView.invalidateItemDecorations();
                            listView.invalidate();
                        }
                        refreshList();
                    }));
        }
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_MAT_CONTROLS, 0,
                LocaleController.getString(R.string.LagramM3Controls),
                LocaleController.getString(R.string.LagramM3ControlsInfo),
                LagramConfig.materialControls));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_MAT_CHAT_LIST, 0,
                LocaleController.getString(R.string.LagramM3ChatList),
                LocaleController.getString(R.string.LagramM3ChatListInfo),
                LagramConfig.materialChatList));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_MAT_APP_BAR, 0,
                LocaleController.getString(R.string.LagramM3AppBar),
                LocaleController.getString(R.string.LagramM3AppBarInfo),
                LagramConfig.materialAppBar));
        if (LagramConfig.materialAppBar) {
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_APP_BAR_BG,
                    LocaleController.getString(R.string.LagramM3AppBarBg),
                    new String[]{
                            LocaleController.getString(R.string.LagramM3AppBarTone),
                            LocaleController.getString(R.string.LagramM3AppBarBlur)
                    },
                    LagramConfig.appBarBackground,
                    idx -> {
                        LagramConfig.setAppBarBackground(idx);
                        refreshList();
                    }));
        }
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_MAT_PROFILE_TABS, 0,
                LocaleController.getString(R.string.LagramM3ProfileTabs),
                LocaleController.getString(R.string.LagramM3ProfileTabsInfo),
                LagramConfig.materialProfileTabs));
        items.add(UItem.asShadow(null));

        // 2. Folder Tabs Bar Customization (live in RealChatListFoldersPreviewCell above!)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramFoldersHeader)));
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                ID_FOLDER_BAR_STYLE,
                LocaleController.getString(R.string.LagramFolderBarStyle),
                new String[]{
                        LocaleController.getString(R.string.LagramFolderBarTelegram),
                        LocaleController.getString(R.string.LagramFolderBarTabs),
                        LocaleController.getString(R.string.LagramFolderBarButtons)
                },
                LagramConfig.folderBarStyle,
                idx -> {
                    LagramConfig.setFolderBarStyle(idx);
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                ID_FOLDER_TABS_STYLE,
                LocaleController.getString(R.string.LagramFolderTabsStyle),
                new String[]{
                        LocaleController.getString(R.string.LagramFolderTabsIconText),
                        LocaleController.getString(R.string.LagramFolderTabsIconOnly),
                        LocaleController.getString(R.string.LagramFolderTabsTextOnly)
                },
                LagramConfig.folderTabsStyle,
                idx -> {
                    LagramConfig.setFolderTabsStyle(idx);
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_FOLDER_COUNTERS, 0,
                LocaleController.getString(R.string.LagramShowFolderCounters),
                null,
                LagramConfig.showFolderCounters));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_REMEMBER_LAST_FOLDER, 0,
                LocaleController.getString(R.string.LagramRememberLastFolder),
                null,
                LagramConfig.rememberLastFolder));
        items.add(UItem.asShadow(null));

        // 3. REAL Chat Bubbles, Rounding, Avatars & Liquid Glass (REAL ChatMessageCell over REAL wallpaper!)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramGlassBlurHeader)));
        items.add(LagramRealPreviews.RealBubblesGlassPreviewCell.Factory.of(ID_PREVIEW_BUBBLES_GLASS));
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                ID_ROUNDING_PRESET,
                LocaleController.getString(R.string.LagramRoundingPreset),
                new String[]{
                        LocaleController.getString(R.string.LagramRoundingDefault),
                        LocaleController.getString(R.string.LagramRoundingRound),
                        LocaleController.getString(R.string.LagramRoundingSquircle),
                        LocaleController.getString(R.string.LagramRoundingSquare)
                },
                LagramConfig.roundingPreset,
                idx -> {
                    LagramConfig.setRoundingPreset(idx);
                    if (listView != null) {
                        listView.invalidateItemDecorations();
                        listView.invalidate();
                    }
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                ID_AVATAR_SHAPE,
                LocaleController.getString(R.string.LagramAvatarShape),
                new String[]{
                        LocaleController.getString(R.string.LagramAvatarShapeCircle),
                        LocaleController.getString(R.string.LagramAvatarShapeRounded),
                        LocaleController.getString(R.string.LagramAvatarShapeSquircle)
                },
                LagramConfig.avatarShape,
                idx -> {
                    LagramConfig.setAvatarShape(idx);
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_BUBBLE_TAILS, 0,
                LocaleController.getString(R.string.LagramShowBubbleTails),
                null,
                LagramConfig.showBubbleTails));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_GLASS_MESSAGES, 0,
                LocaleController.getString(R.string.LagramGlassMessages),
                null,
                LagramConfig.glassMessages));
        if (LagramConfig.glassMessages) {
            items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                    ID_GLASS_ALPHA,
                    LocaleController.getString(R.string.LagramGlassMessagesAlpha),
                    20, 90, 55, LagramConfig.glassMessagesAlpha, "%",
                    val -> {
                        LagramConfig.setGlassMessagesAlpha(val);
                        refreshList();
                    }));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GLASS_BORDER, 0,
                    LocaleController.getString(R.string.LagramGlassMessagesBorder),
                    null,
                    LagramConfig.glassMessagesBorder));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GLASS_HIGHLIGHTS, 0,
                    LocaleController.getString(R.string.LagramGlassHighlights),
                    null,
                    LagramConfig.glassHighlights));
        }
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_POPUP_GLASS, 0,
                LocaleController.getString(R.string.LagramPopupGlass),
                null,
                LagramConfig.popupGlass));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_CHAT_EDGE_BLUR, 0,
                LocaleController.getString(R.string.LagramChatEdgeBlur),
                null,
                LagramConfig.chatEdgeBlur));
        items.add(UItem.asShadow(null));

        // 4. REAL Interactive Scroll Effect Preview (drag up & down inside preview!)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramScrollAndMotionHeader)));
        items.add(LagramRealPreviews.RealScrollEffectPreviewCell.Factory.of(ID_PREVIEW_SCROLL));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SCROLL_EDGE, 0,
                LocaleController.getString(R.string.LagramScrollEdgeScale),
                null,
                LagramConfig.scrollEdgeScale));
        if (LagramConfig.scrollEdgeScale) {
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_SCROLL_MODE,
                    LocaleController.getString(R.string.LagramScrollEdgeMode),
                    new String[]{
                            LocaleController.getString(R.string.LagramScrollEdgeModeScale),
                            LocaleController.getString(R.string.LagramScrollEdgeModeDrum)
                    },
                    LagramConfig.scrollEdgeMode,
                    idx -> {
                        LagramConfig.setScrollEdgeMode(idx);
                        refreshList();
                    }));
        }
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIGH_REFRESH, 0,
                LocaleController.getString(R.string.LagramHighRefreshRate),
                null,
                LagramConfig.highRefreshRateScroll));
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                ID_ANIM_SPEED,
                LocaleController.getString(R.string.LagramAnimationSpeed),
                new String[]{
                        LocaleController.getString(R.string.LagramAnimNormal),
                        LocaleController.getString(R.string.LagramAnimFast),
                        LocaleController.getString(R.string.LagramAnimInstant)
                },
                LagramConfig.animationSpeed,
                idx -> {
                    LagramConfig.setAnimationSpeed(idx);
                    refreshList();
                }));
        items.add(UItem.asShadow(null));

        // 5. Display & Interface Cleanup
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramScreensHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_AMOLED_BACKDROP, 0,
                LocaleController.getString(R.string.LagramAmoledSettings),
                null,
                LagramConfig.amoledSettingsBackdrop));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SYSTEM_FONTS, 0,
                LocaleController.getString(R.string.LagramUseSystemFonts),
                null,
                LagramConfig.useSystemFonts));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_SECONDS, 0,
                LocaleController.getString(R.string.LagramShowSeconds),
                null,
                LagramConfig.showSecondsInTime));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_CHATS_HEADER_CENTERED, 0,
                LocaleController.getString(R.string.LagramChatsHeaderCentered),
                null,
                LagramConfig.chatsHeaderCentered));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_STORIES, 0,
                LocaleController.getString(R.string.LagramHideStories),
                null,
                LagramConfig.hideStories));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_STORIES_PROFILE, 0,
                LocaleController.getString(R.string.LagramHideStoriesProfile),
                null,
                LagramConfig.hideStoriesInProfile));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_STORY_POSTING, 0,
                LocaleController.getString(R.string.LagramHideStoryPosting),
                null,
                LagramConfig.hideStoryPosting));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_ALL_CHATS, 0,
                LocaleController.getString(R.string.LagramHideAllChats),
                null,
                LagramConfig.hideAllChatsFolder));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_PHONE, 0,
                LocaleController.getString(R.string.LagramHidePhoneNumber),
                null,
                LagramConfig.hidePhoneNumber));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_BLOAT, 0,
                LocaleController.getString(R.string.LagramHideTelegramBloat),
                null,
                LagramConfig.hideTelegramBloat));
        items.add(UItem.asShadow(LocaleController.getString(R.string.LagramAppearanceInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_MAT_CONTAINERS:
                LagramConfig.toggleMaterialContainers();
                if (listView != null) {
                    listView.invalidateItemDecorations();
                    listView.invalidate();
                }
                refreshList();
                break;
            case ID_MAT_CONTROLS:
                LagramConfig.toggleMaterialControls();
                refreshList();
                break;
            case ID_MAT_CHAT_LIST:
                LagramConfig.toggleMaterialChatList();
                refreshList();
                break;
            case ID_MAT_APP_BAR:
                LagramConfig.toggleMaterialAppBar();
                refreshList();
                break;
            case ID_MAT_PROFILE_TABS:
                LagramConfig.toggleMaterialProfileTabs();
                refreshList();
                break;
            case ID_SHOW_FOLDER_COUNTERS:
                LagramConfig.toggleShowFolderCounters();
                refreshList();
                break;
            case ID_REMEMBER_LAST_FOLDER:
                LagramConfig.toggleRememberLastFolder();
                refreshList();
                break;
            case ID_SHOW_BUBBLE_TAILS:
                LagramConfig.toggleShowBubbleTails();
                refreshList();
                break;
            case ID_GLASS_MESSAGES:
                LagramConfig.toggleGlassMessages();
                refreshList();
                break;
            case ID_GLASS_BORDER:
                LagramConfig.toggleGlassMessagesBorder();
                refreshList();
                break;
            case ID_GLASS_HIGHLIGHTS:
                LagramConfig.toggleGlassHighlights();
                refreshList();
                break;
            case ID_POPUP_GLASS:
                LagramConfig.togglePopupGlass();
                refreshList();
                break;
            case ID_CHAT_EDGE_BLUR:
                LagramConfig.toggleChatEdgeBlur();
                refreshList();
                break;
            case ID_SCROLL_EDGE:
                LagramConfig.toggleScrollEdgeScale();
                refreshList();
                break;
            case ID_HIGH_REFRESH:
                LagramConfig.toggleHighRefreshRateScroll();
                refreshList();
                break;
            case ID_AMOLED_BACKDROP:
                LagramConfig.toggleAmoledSettingsBackdrop();
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewTheme, false, false);
                refreshList();
                break;
            case ID_SYSTEM_FONTS:
                LagramConfig.toggleUseSystemFonts();
                refreshList();
                break;
            case ID_SHOW_SECONDS:
                LagramConfig.toggleShowSecondsInTime();
                refreshList();
                break;
            case ID_CHATS_HEADER_CENTERED:
                LagramConfig.toggleChatsHeaderCentered();
                refreshList();
                break;
            case ID_HIDE_STORIES:
                LagramConfig.toggleHideStories();
                refreshList();
                break;
            case ID_HIDE_STORIES_PROFILE:
                LagramConfig.toggleHideStoriesInProfile();
                refreshList();
                break;
            case ID_HIDE_STORY_POSTING:
                LagramConfig.toggleHideStoryPosting();
                refreshList();
                break;
            case ID_HIDE_ALL_CHATS:
                LagramConfig.toggleHideAllChatsFolder();
                refreshList();
                break;
            case ID_HIDE_PHONE:
                LagramConfig.toggleHidePhoneNumber();
                refreshList();
                break;
            case ID_HIDE_BLOAT:
                LagramConfig.toggleHideTelegramBloat();
                refreshList();
                break;
        }
    }
}
