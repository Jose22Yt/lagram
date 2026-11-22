package org.telegram.ui.lagram;

import android.content.Context;
import android.os.Build;
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

    // Material Design 3 section
    private static final int ID_M3_HERO_PREVIEW = 1;
    private static final int ID_M3_APP_BAR = 2;
    private static final int ID_M3_APP_BAR_BG = 3;
    private static final int ID_M3_CONTAINERS = 4;
    private static final int ID_M3_SECTION_SPACING = 5;
    private static final int ID_M3_CONTROLS = 6;
    private static final int ID_M3_CHAT_LIST = 7;
    private static final int ID_M3_PROFILE_TABS = 8;
    private static final int ID_SCROLL_EDGE_SCALE = 9;
    private static final int ID_SCROLL_EDGE_MODE = 10;
    private static final int ID_ANIM_SPEED = 11;
    private static final int ID_SYSTEM_FONTS = 12;

    // Shape section
    private static final int ID_ROUNDING_PREVIEW = 20;
    private static final int ID_ROUNDING_PRESET = 21;
    private static final int ID_AVATAR_SHAPE = 22;
    private static final int ID_BUBBLE_TAILS = 23;

    // Glass & blur section
    private static final int ID_GLASS_PREVIEW = 30;
    private static final int ID_POPUP_GLASS = 31;
    private static final int ID_GLASS_HIGHLIGHTS = 32;
    private static final int ID_GLASS_MESSAGES = 33;
    private static final int ID_GLASS_TRANSPARENCY = 34;
    private static final int ID_GLASS_BORDER = 35;
    private static final int ID_CHAT_EDGE_BLUR = 36;

    // Screens section
    private static final int ID_IOS27_BOTTOM_BAR = 40;
    private static final int ID_CHATS_HEADER_CENTERED = 41;
    private static final int ID_AMOLED_BACKDROP = 42;
    private static final int ID_HIDE_TELEGRAM_BLOAT = 43;

    // Folders section
    private static final int ID_FOLDER_BAR_PICKER = 50;
    private static final int ID_FOLDER_TABS_PREVIEW = 51;
    private static final int ID_FOLDER_TABS_STYLE = 52;
    private static final int ID_FOLDER_COUNTERS = 53;
    private static final int ID_HIDE_ALL_CHATS = 54;
    private static final int ID_REMEMBER_LAST_FOLDER = 55;

    public LagramAppearanceActivity() {
        super();
    }

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

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
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
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // =========================================================================
        // 1. Material Design 3 (Exact Opexgram Appearance top section)
        // =========================================================================
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramM3Header)));
        items.add(OpexgramStyleWidgets.OpexMaterialHeroPreviewCell.Factory.of(ID_M3_HERO_PREVIEW));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_M3_APP_BAR, 0,
                LocaleController.getString(R.string.LagramM3AppBar),
                LocaleController.getString(R.string.LagramM3AppBarInfo),
                LagramConfig.materialAppBar));
        if (LagramConfig.materialAppBar) {
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_M3_APP_BAR_BG,
                    LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramM3AppBarBg)),
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
                ID_M3_CONTAINERS, 0,
                LocaleController.getString(R.string.LagramM3Containers),
                LocaleController.getString(R.string.LagramM3ContainersInfo),
                LagramConfig.materialContainers));
        if (LagramConfig.materialContainers) {
            items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                    ID_M3_SECTION_SPACING,
                    LocaleController.getString(R.string.LagramM3SectionSpacing),
                    0, 24, 7, LagramConfig.sectionSpacingDp, " dp",
                    val -> {
                        LagramConfig.setSectionSpacingDp(val);
                        if (listView != null) {
                            listView.invalidateItemDecorations();
                        }
                        refreshList();
                    }));
        }
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_M3_CONTROLS, 0,
                LocaleController.getString(R.string.LagramM3Controls),
                LocaleController.getString(R.string.LagramM3ControlsInfo),
                LagramConfig.materialControls));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_M3_CHAT_LIST, 0,
                LocaleController.getString(R.string.LagramM3ChatList),
                LocaleController.getString(R.string.LagramM3ChatListInfo),
                LagramConfig.materialChatList));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_M3_PROFILE_TABS, 0,
                LocaleController.getString(R.string.LagramM3ProfileTabs),
                LocaleController.getString(R.string.LagramM3ProfileTabsInfo),
                LagramConfig.materialProfileTabs));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SCROLL_EDGE_SCALE, 0,
                LocaleController.getString(R.string.LagramScrollEdgeScale),
                null,
                LagramConfig.scrollEdgeScale));
        if (LagramConfig.scrollEdgeScale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_SCROLL_EDGE_MODE,
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
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SYSTEM_FONTS, 0,
                LocaleController.getString(R.string.LagramUseSystemFonts),
                null,
                LagramConfig.useSystemFonts));
        items.add(UItem.asShadow(-1, null));

        // =========================================================================
        // 2. Shape (Unified Rounding & Avatar Shape with live preview)
        // =========================================================================
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramShapeHeader)));
        items.add(OpexgramStyleWidgets.OpexRoundingPreviewCell.Factory.of(ID_ROUNDING_PREVIEW));
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
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload);
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_BUBBLE_TAILS, 0,
                LocaleController.getString(R.string.LagramShowBubbleTails),
                null,
                LagramConfig.showBubbleTails));
        items.add(UItem.asShadow(-2, null));

        // =========================================================================
        // 3. Glass and blur (Real Chat Wallpaper + Blurred Liquid Glass Preview!)
        // =========================================================================
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramGlassBlurHeader)));
        items.add(LagramSettingsComponents.RealChatMessagesPreviewCell.Factory.of(
                ID_GLASS_PREVIEW,
                LagramSettingsComponents.RealChatMessagesPreviewCell.MODE_BUBBLES_AND_GLASS));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_POPUP_GLASS, 0,
                LocaleController.getString(R.string.LagramPopupGlass),
                null,
                LagramConfig.popupGlass));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_GLASS_HIGHLIGHTS, 0,
                LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramGlassHighlights)),
                null,
                LagramConfig.glassHighlights));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_GLASS_MESSAGES, 0,
                LocaleController.getString(R.string.LagramGlassMessages),
                null,
                LagramConfig.glassMessages));
        if (LagramConfig.glassMessages) {
            items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                    ID_GLASS_TRANSPARENCY,
                    LocaleController.getString(R.string.LagramGlassMessagesAlpha),
                    20, 95, 55, LagramConfig.glassMessagesAlpha, "%",
                    val -> {
                        LagramConfig.setGlassMessagesAlpha(val);
                        refreshList();
                    }));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GLASS_BORDER, 0,
                    LocaleController.getString(R.string.LagramGlassMessagesBorder),
                    null,
                    LagramConfig.glassMessagesBorder));
        }
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_CHAT_EDGE_BLUR, 0,
                LocaleController.getString(R.string.LagramChatEdgeBlur),
                null,
                LagramConfig.chatEdgeBlur));
        items.add(UItem.asShadow(-3, LocaleController.getString(R.string.LagramAppearanceInfo)));

        // =========================================================================
        // 4. Screens
        // =========================================================================
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramScreensHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_IOS27_BOTTOM_BAR, R.drawable.menu_add_tab_24,
                LocaleController.getString(R.string.LagramIos27BottomBar),
                null,
                LagramConfig.ios27BottomBar));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_CHATS_HEADER_CENTERED, R.drawable.msg_list,
                LocaleController.getString(R.string.LagramChatsHeaderCentered),
                null,
                LagramConfig.chatsHeaderCentered));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_AMOLED_BACKDROP, R.drawable.msg_theme,
                LocaleController.getString(R.string.LagramAmoledSettings),
                null,
                LagramConfig.amoledSettingsBackdrop));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_TELEGRAM_BLOAT, R.drawable.msg_clear,
                LocaleController.getString(R.string.LagramHideTelegramBloat),
                null,
                LagramConfig.hideTelegramBloat));
        items.add(UItem.asShadow(-4, null));

        // =========================================================================
        // 5. Folders (3-Card Visual Picker + Real FilterTabsView Preview!)
        // =========================================================================
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramFoldersHeader)));
        items.add(OpexgramStyleWidgets.OpexVisualPickerCell.Factory.of(
                ID_FOLDER_BAR_PICKER,
                OpexgramStyleWidgets.OpexVisualPickerCell.MODE_FOLDERS,
                new String[]{
                        LocaleController.getString(R.string.LagramFolderBarTelegram),
                        LocaleController.getString(R.string.LagramFolderBarTabs),
                        LocaleController.getString(R.string.LagramFolderBarButtons)
                },
                LagramConfig.folderBarStyle,
                idx -> {
                    LagramConfig.setFolderBarStyle(idx);
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogFiltersUpdated);
                    refreshList();
                }));
        items.add(LagramSettingsComponents.RealFolderTabsPreviewCell.Factory.of(ID_FOLDER_TABS_PREVIEW));
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
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogFiltersUpdated);
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_FOLDER_COUNTERS, 0,
                LocaleController.getString(R.string.LagramShowFolderCounters),
                null,
                LagramConfig.showFolderCounters));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_ALL_CHATS, 0,
                LocaleController.getString(R.string.LagramHideAllChats),
                null,
                LagramConfig.hideAllChatsFolder));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_REMEMBER_LAST_FOLDER, 0,
                LocaleController.getString(R.string.LagramRememberLastFolder),
                null,
                LagramConfig.rememberLastFolder));
        items.add(UItem.asShadow(-5, null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_M3_APP_BAR:
                toggle(view, LagramConfig.toggleMaterialAppBar());
                if (actionBar != null && LagramConfig.materialAppBar) {
                    actionBar.centerTitle();
                    actionBar.setCastShadows(false);
                }
                return;
            case ID_M3_CONTAINERS:
                toggle(view, LagramConfig.toggleMaterialContainers());
                if (listView != null) {
                    listView.invalidateItemDecorations();
                }
                return;
            case ID_M3_CONTROLS:
                toggle(view, LagramConfig.toggleMaterialControls());
                return;
            case ID_M3_CHAT_LIST:
                toggle(view, LagramConfig.toggleMaterialChatList());
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload);
                return;
            case ID_M3_PROFILE_TABS:
                toggle(view, LagramConfig.toggleMaterialProfileTabs());
                return;
            case ID_SCROLL_EDGE_SCALE:
                toggle(view, LagramConfig.toggleScrollEdgeScale());
                return;
            case ID_SYSTEM_FONTS:
                toggle(view, LagramConfig.toggleUseSystemFonts());
                return;
            case ID_BUBBLE_TAILS:
                toggle(view, LagramConfig.toggleShowBubbleTails());
                return;
            case ID_POPUP_GLASS:
                toggle(view, LagramConfig.togglePopupGlass());
                return;
            case ID_GLASS_HIGHLIGHTS:
                toggle(view, LagramConfig.toggleGlassHighlights());
                return;
            case ID_GLASS_MESSAGES:
                LagramGlassBubbleEngine.invalidateCapture();
                toggle(view, LagramConfig.toggleGlassMessages());
                return;
            case ID_GLASS_BORDER:
                toggle(view, LagramConfig.toggleGlassMessagesBorder());
                return;
            case ID_CHAT_EDGE_BLUR:
                toggle(view, LagramConfig.toggleChatEdgeBlur());
                return;
            case ID_IOS27_BOTTOM_BAR:
                toggle(view, LagramConfig.toggleIos27BottomBar());
                return;
            case ID_CHATS_HEADER_CENTERED:
                toggle(view, LagramConfig.toggleChatsHeaderCentered());
                return;
            case ID_AMOLED_BACKDROP:
                toggle(view, LagramConfig.toggleAmoledSettingsBackdrop());
                updateBackdropColor();
                return;
            case ID_HIDE_TELEGRAM_BLOAT:
                toggle(view, LagramConfig.toggleHideTelegramBloat());
                return;
            case ID_FOLDER_COUNTERS:
                toggle(view, LagramConfig.toggleShowFolderCounters());
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogFiltersUpdated);
                return;
            case ID_HIDE_ALL_CHATS:
                toggle(view, LagramConfig.toggleHideAllChatsFolder());
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogFiltersUpdated);
                return;
            case ID_REMEMBER_LAST_FOLDER:
                toggle(view, LagramConfig.toggleRememberLastFolder());
                return;
        }
    }

    private void toggle(View view, boolean checked) {
        if (view instanceof OpexgramStyleWidgets.OpexSwitchCell) {
            ((OpexgramStyleWidgets.OpexSwitchCell) view).setChecked(checked);
        }
        refreshList();
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
