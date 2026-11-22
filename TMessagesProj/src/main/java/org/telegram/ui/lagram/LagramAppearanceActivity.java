package org.telegram.ui.lagram;

import android.content.Context;
import android.os.Build;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class LagramAppearanceActivity extends UniversalFragment {

    public static final int SUB_HOME = 0;
    public static final int SUB_BUBBLES_AND_GLASS = 1;
    public static final int SUB_SCROLL_AND_MOTION = 2;
    public static final int SUB_FOLDERS = 3;
    public static final int SUB_DESIGN_AND_UI = 4;

    private final int subPage;

    // Hub navigation cards
    private static final int ID_NAV_BUBBLES = 100;
    private static final int ID_NAV_SCROLL = 101;
    private static final int ID_NAV_FOLDERS = 102;
    private static final int ID_NAV_DESIGN = 103;

    // Bubbles & Glass
    private static final int ID_PREVIEW_BUBBLES = 1;
    private static final int ID_GLASS_MESSAGES = 2;
    private static final int ID_GLASS_ALPHA = 3;
    private static final int ID_GLASS_BORDER = 4;
    private static final int ID_GLASS_HIGHLIGHTS = 5;
    private static final int ID_POPUP_GLASS = 6;
    private static final int ID_BUBBLE_TAILS = 7;
    private static final int ID_ROUNDING_PRESET = 8;
    private static final int ID_CHAT_EDGE_BLUR = 9;

    // Scroll & Motion
    private static final int ID_SCROLL_EDGE_SCALE = 20;
    private static final int ID_SCROLL_EDGE_MODE = 21;
    private static final int ID_ANIM_SPEED = 22;

    // Folders & Header
    private static final int ID_PREVIEW_FOLDERS = 30;
    private static final int ID_FOLDER_TABS_STYLE = 31;
    private static final int ID_FOLDER_COUNTERS = 32;
    private static final int ID_HIDE_ALL_CHATS = 33;
    private static final int ID_REMEMBER_LAST_FOLDER = 34;
    private static final int ID_CHATS_HEADER_CENTERED = 35;

    // Design, Avatars & UI Visibility
    private static final int ID_AVATAR_SHAPE = 40;
    private static final int ID_SECTION_GAP = 41;
    private static final int ID_IOS27_BOTTOM_BAR = 42;
    private static final int ID_AMOLED_BACKDROP = 43;
    private static final int ID_SYSTEM_FONTS = 44;
    private static final int ID_HIDE_STORIES = 45;
    private static final int ID_HIDE_STORIES_PROFILE = 46;
    private static final int ID_HIDE_STORY_POSTING = 47;
    private static final int ID_HIDE_TELEGRAM_BLOAT = 48;

    public LagramAppearanceActivity() {
        this(SUB_HOME);
    }

    public LagramAppearanceActivity(int subPage) {
        super();
        this.subPage = subPage;
    }

    @Override
    protected CharSequence getTitle() {
        switch (subPage) {
            case SUB_BUBBLES_AND_GLASS:
                return LocaleController.getString(R.string.LagramLiquidGlassHeader);
            case SUB_SCROLL_AND_MOTION:
                return LocaleController.getString(R.string.LagramScrollAndMotionHeader);
            case SUB_FOLDERS:
                return LocaleController.getString(R.string.LagramFoldersHeader);
            case SUB_DESIGN_AND_UI:
                return LocaleController.getString(R.string.LagramUiVisibilityHeader);
            case SUB_HOME:
            default:
                return LocaleController.getString(R.string.LagramCategoryAppearance);
        }
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
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
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

    private void updatePreviewAndAdapter() {
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();
        switch (subPage) {
            case SUB_BUBBLES_AND_GLASS:
                fillBubblesAndGlass(items);
                break;
            case SUB_SCROLL_AND_MOTION:
                fillScrollAndMotion(items);
                break;
            case SUB_FOLDERS:
                fillFoldersAndHeader(items);
                break;
            case SUB_DESIGN_AND_UI:
                fillDesignAndUi(items);
                break;
            case SUB_HOME:
            default:
                fillHomeHub(items);
                break;
        }
    }

    private void fillHomeHub(ArrayList<UItem> items) {
        // Card 1: Real Live Chat Bubbles Preview
        items.add(LagramSettingsComponents.RealChatMessagesPreviewCell.Factory.of(
                ID_PREVIEW_BUBBLES,
                LagramSettingsComponents.RealChatMessagesPreviewCell.MODE_BUBBLES_AND_GLASS));
        items.add(UItem.asShadow(-1, null));

        // Card 2: Categorized Sub-Fragments
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramCategoryAppearance)));
        items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                ID_NAV_BUBBLES,
                IconBackgroundColors.BLUE,
                R.drawable.msg_palette,
                LocaleController.getString(R.string.LagramLiquidGlassHeader),
                LocaleController.getString(R.string.LagramGlassMessages),
                LagramConfig.glassMessages ? ("Glass " + LagramConfig.glassMessagesAlpha + "%") : (LagramConfig.getEffectiveBubbleRadiusDp() + "dp")));

        items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                ID_NAV_SCROLL,
                IconBackgroundColors.PURPLE,
                R.drawable.msg_speed,
                LocaleController.getString(R.string.LagramScrollAndMotionHeader),
                LocaleController.getString(R.string.LagramScrollEdgeScale),
                getScrollBadge()));

        items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                ID_NAV_FOLDERS,
                IconBackgroundColors.CYAN,
                R.drawable.msg_folders,
                LocaleController.getString(R.string.LagramFoldersHeader),
                LocaleController.getString(R.string.LagramFolderTabsStyle),
                getFolderStyleShort()));

        items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                ID_NAV_DESIGN,
                IconBackgroundColors.ORANGE,
                R.drawable.msg_theme,
                LocaleController.getString(R.string.LagramUiVisibilityHeader),
                LocaleController.getString(R.string.LagramAvatarShape),
                getAvatarShapeShort()));
        items.add(UItem.asShadow(-2, null));

        // Card 3: Quick Toggles
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramLiquidGlassHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_GLASS_MESSAGES,
                R.drawable.msg_palette,
                LocaleController.getString(R.string.LagramGlassMessages),
                null,
                LagramConfig.glassMessages));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SCROLL_EDGE_SCALE,
                R.drawable.msg_speed,
                LocaleController.getString(R.string.LagramScrollEdgeScale),
                null,
                LagramConfig.scrollEdgeScale));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_AMOLED_BACKDROP,
                R.drawable.msg_theme,
                LocaleController.getString(R.string.LagramAmoledSettings),
                null,
                LagramConfig.amoledSettingsBackdrop));
        items.add(UItem.asShadow(-3, null));
    }

    private void fillBubblesAndGlass(ArrayList<UItem> items) {
        // Standalone Preview Card
        items.add(LagramSettingsComponents.RealChatMessagesPreviewCell.Factory.of(
                ID_PREVIEW_BUBBLES,
                LagramSettingsComponents.RealChatMessagesPreviewCell.MODE_BUBBLES_AND_GLASS));
        items.add(UItem.asShadow(-10, null));

        // Card 1: Liquid Glass Engine
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramLiquidGlassHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_GLASS_MESSAGES,
                R.drawable.msg_palette,
                LocaleController.getString(R.string.LagramGlassMessages),
                null,
                LagramConfig.glassMessages));

        if (LagramConfig.glassMessages) {
            items.add(LagramSettingsComponents.InlineSliderCell.Factory.of(
                    ID_GLASS_ALPHA,
                    LocaleController.getString(R.string.LagramGlassMessagesAlpha),
                    20, 95, 55, LagramConfig.glassMessagesAlpha, "%",
                    val -> {
                        LagramConfig.setGlassMessagesAlpha(val);
                        updatePreviewAndAdapter();
                    }));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_GLASS_BORDER,
                    R.drawable.msg_photo_settings,
                    LocaleController.getString(R.string.LagramGlassMessagesBorder),
                    null,
                    LagramConfig.glassMessagesBorder));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_GLASS_HIGHLIGHTS,
                    R.drawable.msg_photo_blur,
                    LocaleController.getString(R.string.LagramGlassHighlights),
                    null,
                    LagramConfig.glassHighlights));
        }
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_POPUP_GLASS,
                R.drawable.msg_list,
                LocaleController.getString(R.string.LagramPopupGlass),
                null,
                LagramConfig.popupGlass));
        items.add(UItem.asShadow(-11, null));

        // Card 2: Shape & Blur
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramRoundingPreset)));
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
                idx -> {
                    LagramConfig.setRoundingPreset(idx);
                    updatePreviewAndAdapter();
                }));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_BUBBLE_TAILS,
                R.drawable.msg_msgbubble3,
                LocaleController.getString(R.string.LagramShowBubbleTails),
                null,
                LagramConfig.showBubbleTails));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_CHAT_EDGE_BLUR,
                R.drawable.msg_photo_blur,
                LocaleController.getString(R.string.LagramChatEdgeBlur),
                null,
                LagramConfig.chatEdgeBlur));
        items.add(UItem.asShadow(-12, null));
    }

    private void fillScrollAndMotion(ArrayList<UItem> items) {
        // Card 1: Opexgram Scroll Edge Engine
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramScrollAndMotionHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SCROLL_EDGE_SCALE,
                R.drawable.msg_speed,
                LocaleController.getString(R.string.LagramScrollEdgeScale),
                null,
                LagramConfig.scrollEdgeScale));

        if (LagramConfig.scrollEdgeScale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                    ID_SCROLL_EDGE_MODE,
                    LocaleController.getString(R.string.LagramScrollEdgeMode),
                    new String[]{
                            LocaleController.getString(R.string.LagramScrollEdgeModeScale),
                            LocaleController.getString(R.string.LagramScrollEdgeModeDrum)
                    },
                    LagramConfig.scrollEdgeMode,
                    idx -> {
                        LagramConfig.setScrollEdgeMode(idx);
                        updatePreviewAndAdapter();
                    }));
        }
        items.add(UItem.asShadow(-20, null));

        // Card 2: Global Animation Speed
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAnimationSpeed)));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
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
                    updatePreviewAndAdapter();
                }));
        items.add(UItem.asShadow(-21, null));
    }

    private void fillFoldersAndHeader(ArrayList<UItem> items) {
        // Standalone Real FilterTabsView Preview Card
        items.add(LagramSettingsComponents.RealFolderTabsPreviewCell.Factory.of(ID_PREVIEW_FOLDERS));
        items.add(UItem.asShadow(-30, null));

        // Card 1: Folder Tabs
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramFoldersHeader)));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
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
                    updatePreviewAndAdapter();
                }));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_FOLDER_COUNTERS,
                R.drawable.msg_folders,
                LocaleController.getString(R.string.LagramShowFolderCounters),
                null,
                LagramConfig.showFolderCounters));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_HIDE_ALL_CHATS,
                R.drawable.msg_folders,
                LocaleController.getString(R.string.LagramHideAllChats),
                null,
                LagramConfig.hideAllChatsFolder));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_REMEMBER_LAST_FOLDER,
                R.drawable.msg_pin,
                LocaleController.getString(R.string.LagramRememberLastFolder),
                null,
                LagramConfig.rememberLastFolder));
        items.add(UItem.asShadow(-31, null));

        // Card 2: Chats Header
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramUiVisibilityHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_CHATS_HEADER_CENTERED,
                R.drawable.msg_list,
                LocaleController.getString(R.string.LagramChatsHeaderCentered),
                null,
                LagramConfig.chatsHeaderCentered));
        items.add(UItem.asShadow(-32, null));
    }

    private void fillDesignAndUi(ArrayList<UItem> items) {
        // Card 1: Avatars & Card Spacing
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAvatarShape)));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
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
                    updatePreviewAndAdapter();
                }));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                ID_SECTION_GAP,
                LocaleController.getString(R.string.LagramSectionGap),
                new String[]{
                        LocaleController.getString(R.string.LagramSectionGapCompact),
                        LocaleController.getString(R.string.LagramSectionGapDefault),
                        LocaleController.getString(R.string.LagramSectionGapWide)
                },
                LagramConfig.sectionGap,
                idx -> {
                    LagramConfig.setSectionGap(idx);
                    if (listView != null) {
                        listView.setSections();
                        listView.invalidateItemDecorations();
                    }
                    updatePreviewAndAdapter();
                }));
        items.add(UItem.asShadow(-40, null));

        // Card 2: Theme & Navigation
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramLiquidGlassHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_IOS27_BOTTOM_BAR,
                R.drawable.menu_add_tab_24,
                LocaleController.getString(R.string.LagramIos27BottomBar),
                null,
                LagramConfig.ios27BottomBar));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_AMOLED_BACKDROP,
                R.drawable.msg_theme,
                LocaleController.getString(R.string.LagramAmoledSettings),
                null,
                LagramConfig.amoledSettingsBackdrop));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SYSTEM_FONTS,
                R.drawable.msg_photo_text_regular,
                LocaleController.getString(R.string.LagramUseSystemFonts),
                null,
                LagramConfig.useSystemFonts));
        items.add(UItem.asShadow(-41, null));

        // Card 3: Stories & Clean UI
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramUiVisibilityHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_HIDE_STORIES,
                R.drawable.msg_stories_archive,
                LocaleController.getString(R.string.LagramHideStories),
                null,
                LagramConfig.hideStories));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_HIDE_STORIES_PROFILE,
                R.drawable.msg_openprofile,
                LocaleController.getString(R.string.LagramHideStoriesProfile),
                null,
                LagramConfig.hideStoriesInProfile));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_HIDE_STORY_POSTING,
                R.drawable.msg_camera,
                LocaleController.getString(R.string.LagramHideStoryPosting),
                null,
                LagramConfig.hideStoryPosting));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_HIDE_TELEGRAM_BLOAT,
                R.drawable.msg_clear,
                LocaleController.getString(R.string.LagramHideTelegramBloat),
                null,
                LagramConfig.hideTelegramBloat));
        items.add(UItem.asShadow(-42, null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_NAV_BUBBLES:
                presentFragment(new LagramAppearanceActivity(SUB_BUBBLES_AND_GLASS));
                return;
            case ID_NAV_SCROLL:
                presentFragment(new LagramAppearanceActivity(SUB_SCROLL_AND_MOTION));
                return;
            case ID_NAV_FOLDERS:
                presentFragment(new LagramAppearanceActivity(SUB_FOLDERS));
                return;
            case ID_NAV_DESIGN:
                presentFragment(new LagramAppearanceActivity(SUB_DESIGN_AND_UI));
                return;
            case ID_GLASS_MESSAGES:
                LagramConfig.toggleGlassMessages();
                LagramGlassBubbleEngine.invalidateCapture();
                updatePreviewAndAdapter();
                return;
            case ID_GLASS_BORDER:
                LagramConfig.toggleGlassMessagesBorder();
                updatePreviewAndAdapter();
                return;
            case ID_GLASS_HIGHLIGHTS:
                LagramConfig.toggleGlassHighlights();
                updatePreviewAndAdapter();
                return;
            case ID_POPUP_GLASS:
                toggleSwitch(view, LagramConfig.togglePopupGlass());
                return;
            case ID_BUBBLE_TAILS:
                LagramConfig.toggleShowBubbleTails();
                updatePreviewAndAdapter();
                return;
            case ID_CHAT_EDGE_BLUR:
                toggleSwitch(view, LagramConfig.toggleChatEdgeBlur());
                return;
            case ID_SCROLL_EDGE_SCALE:
                LagramConfig.toggleScrollEdgeScale();
                updatePreviewAndAdapter();
                return;
            case ID_IOS27_BOTTOM_BAR:
                toggleSwitch(view, LagramConfig.toggleIos27BottomBar());
                return;
            case ID_AMOLED_BACKDROP:
                toggleSwitch(view, LagramConfig.toggleAmoledSettingsBackdrop());
                updateBackdropColor();
                return;
            case ID_SYSTEM_FONTS:
                toggleSwitch(view, LagramConfig.toggleUseSystemFonts());
                return;
            case ID_FOLDER_COUNTERS:
                LagramConfig.toggleShowFolderCounters();
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogFiltersUpdated);
                updatePreviewAndAdapter();
                return;
            case ID_HIDE_ALL_CHATS:
                LagramConfig.toggleHideAllChatsFolder();
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogFiltersUpdated);
                updatePreviewAndAdapter();
                return;
            case ID_REMEMBER_LAST_FOLDER:
                toggleSwitch(view, LagramConfig.toggleRememberLastFolder());
                return;
            case ID_CHATS_HEADER_CENTERED:
                toggleSwitch(view, LagramConfig.toggleChatsHeaderCentered());
                return;
            case ID_HIDE_STORIES:
                toggleSwitch(view, LagramConfig.toggleHideStories());
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.storiesUpdated);
                return;
            case ID_HIDE_STORIES_PROFILE:
                toggleSwitch(view, LagramConfig.toggleHideStoriesInProfile());
                return;
            case ID_HIDE_STORY_POSTING:
                toggleSwitch(view, LagramConfig.toggleHideStoryPosting());
                return;
            case ID_HIDE_TELEGRAM_BLOAT:
                toggleSwitch(view, LagramConfig.toggleHideTelegramBloat());
                return;
        }
    }

    private void toggleSwitch(View view, boolean checked) {
        if (view instanceof LagramSettingsComponents.IconSwitchCell) {
            ((LagramSettingsComponents.IconSwitchCell) view).setChecked(checked, true);
        }
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private String getScrollBadge() {
        if (!LagramConfig.scrollEdgeScale) {
            return LocaleController.getString(R.string.LagramHapticsOff);
        }
        if (LagramConfig.scrollEdgeMode == LagramConfig.SCROLL_EDGE_DRUM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return LocaleController.getString(R.string.LagramScrollEdgeModeDrum);
        }
        return LocaleController.getString(R.string.LagramScrollEdgeModeScale);
    }

    private String getFolderStyleShort() {
        switch (LagramConfig.folderTabsStyle) {
            case LagramConfig.FOLDER_TABS_ICON_ONLY:
                return LocaleController.getString(R.string.LagramFolderTabsIconOnly);
            case LagramConfig.FOLDER_TABS_TEXT_ONLY:
                return LocaleController.getString(R.string.LagramFolderTabsTextOnly);
            case LagramConfig.FOLDER_TABS_ICON_AND_TEXT:
            default:
                return LocaleController.getString(R.string.LagramFolderTabsIconText);
        }
    }

    private String getAvatarShapeShort() {
        switch (LagramConfig.avatarShape) {
            case LagramConfig.AVATAR_SHAPE_ROUNDED:
                return LocaleController.getString(R.string.LagramAvatarShapeRounded);
            case LagramConfig.AVATAR_SHAPE_SQUIRCLE:
                return LocaleController.getString(R.string.LagramAvatarShapeSquircle);
            case LagramConfig.AVATAR_SHAPE_CIRCLE:
            default:
                return LocaleController.getString(R.string.LagramAvatarShapeCircle);
        }
    }
}
