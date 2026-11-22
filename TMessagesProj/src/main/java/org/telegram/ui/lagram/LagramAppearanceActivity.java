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

    private static final int ID_PREVIEW_BUBBLES_GLASS = 100;
    private static final int ID_PREVIEW_SCROLL = 101;
    private static final int ID_APP_ICON_SELECTOR = 102;

    // Liquid Glass & Bubble Shape
    private static final int ID_GLASS_MESSAGES = 110;
    private static final int ID_GLASS_TINT = 111;
    private static final int ID_GLASS_ALPHA = 112;
    private static final int ID_GLASS_BLUR_LEVEL = 113;
    private static final int ID_GLASS_BORDER = 114;
    private static final int ID_GLASS_BORDER_OPACITY = 115;
    private static final int ID_GLASS_HIGHLIGHTS = 116;
    private static final int ID_POPUP_GLASS = 117;
    private static final int ID_CHAT_EDGE_BLUR = 118;
    private static final int ID_SHOW_BUBBLE_TAILS = 119;
    private static final int ID_ROUNDING_PRESET = 120;
    private static final int ID_AVATAR_SHAPE = 121;

    // 3D Scroll & Motion
    private static final int ID_SCROLL_EDGE = 130;
    private static final int ID_SCROLL_MODE = 131;
    private static final int ID_ANIM_SPEED = 132;

    // Folders & Clean Interface
    private static final int ID_MAT_CONTAINERS = 140;
    private static final int ID_SECTION_SPACING = 141;
    private static final int ID_FOLDER_TABS_STYLE = 142;
    private static final int ID_SHOW_FOLDER_COUNTERS = 143;
    private static final int ID_REMEMBER_LAST_FOLDER = 144;
    private static final int ID_HIDE_ALL_CHATS = 145;
    private static final int ID_AMOLED_BACKDROP = 146;
    private static final int ID_SYSTEM_FONTS = 147;
    private static final int ID_SHOW_SECONDS = 148;
    private static final int ID_CHATS_HEADER_CENTERED = 149;
    private static final int ID_HIDE_BLOAT = 150;

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

        // 0. App Icon Selector (6 redesigned Lagram icons with live preview)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAppIconHeader)));
        items.add(LagramRealPreviews.RealAppIconSelectorCell.Factory.of(ID_APP_ICON_SELECTOR));
        items.add(UItem.asShadow(null));

        // 1. Liquid Glass & Bubbles (Real ChatMessageCell over real wallpaper!)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramGlassBlurHeader)));
        items.add(LagramRealPreviews.RealBubblesGlassPreviewCell.Factory.of(ID_PREVIEW_BUBBLES_GLASS));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_GLASS_MESSAGES, 0,
                LocaleController.getString(R.string.LagramGlassMessages),
                null,
                LagramConfig.glassMessages));
        if (LagramConfig.glassMessages) {
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_GLASS_TINT,
                    LocaleController.getString(R.string.LagramGlassTintMode),
                    new String[]{
                            LocaleController.getString(R.string.LagramGlassTintTheme),
                            LocaleController.getString(R.string.LagramGlassTintCrystal),
                            LocaleController.getString(R.string.LagramGlassTintFrost),
                            LocaleController.getString(R.string.LagramGlassTintSmoked)
                    },
                    LagramConfig.glassTintMode,
                    idx -> {
                        LagramConfig.setGlassTintMode(idx);
                        refreshList();
                    }));
            items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                    ID_GLASS_ALPHA,
                    LocaleController.getString(R.string.LagramGlassMessagesAlpha),
                    15, 95, 52, LagramConfig.glassMessagesAlpha, "%",
                    val -> {
                        LagramConfig.setGlassMessagesAlpha(val);
                        refreshList();
                    }));
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_GLASS_BLUR_LEVEL,
                    LocaleController.getString(R.string.LagramGlassBlurLevel),
                    new String[]{
                            LocaleController.getString(R.string.LagramGlassBlurSoft),
                            LocaleController.getString(R.string.LagramGlassBlurMedium),
                            LocaleController.getString(R.string.LagramGlassBlurStrong)
                    },
                    LagramConfig.glassBlurLevel,
                    idx -> {
                        LagramConfig.setGlassBlurLevel(idx);
                        refreshList();
                    }));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GLASS_BORDER, 0,
                    LocaleController.getString(R.string.LagramGlassMessagesBorder),
                    null,
                    LagramConfig.glassMessagesBorder));
            if (LagramConfig.glassMessagesBorder) {
                items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                        ID_GLASS_BORDER_OPACITY,
                        LocaleController.getString(R.string.LagramGlassBorderOpacity),
                        10, 100, 60, LagramConfig.glassBorderOpacity, "%",
                        val -> {
                            LagramConfig.setGlassBorderOpacity(val);
                            refreshList();
                        }));
            }
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

        // 2. Shape & Rounding
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramShapeHeader)));
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
        items.add(UItem.asShadow(null));

        // 3. 3D Scroll Animation & Motion (Real interactive scrollable preview!)
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

        // 4. Settings Fragment Cards & Folders
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramFoldersHeader)));
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
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_ALL_CHATS, 0,
                LocaleController.getString(R.string.LagramHideAllChats),
                null,
                LagramConfig.hideAllChatsFolder));
        items.add(UItem.asShadow(null));

        // 5. Display & Clean Interface (Zero duplicates with General!)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramUiVisibilityHeader)));
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
                ID_HIDE_BLOAT, 0,
                LocaleController.getString(R.string.LagramHideTelegramBloat),
                null,
                LagramConfig.hideTelegramBloat));
        items.add(UItem.asShadow(LocaleController.getString(R.string.LagramAppearanceInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
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
            case ID_SHOW_BUBBLE_TAILS:
                LagramConfig.toggleShowBubbleTails();
                refreshList();
                break;
            case ID_SCROLL_EDGE:
                LagramConfig.toggleScrollEdgeScale();
                refreshList();
                break;
            case ID_MAT_CONTAINERS:
                LagramConfig.toggleMaterialContainers();
                if (listView != null) {
                    listView.invalidateItemDecorations();
                    listView.invalidate();
                }
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
            case ID_HIDE_ALL_CHATS:
                LagramConfig.toggleHideAllChatsFolder();
                refreshList();
                break;
            case ID_AMOLED_BACKDROP:
                LagramConfig.toggleAmoledSettingsBackdrop();
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewTheme, false, false);
                updateBackdropColor();
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
            case ID_HIDE_BLOAT:
                LagramConfig.toggleHideTelegramBloat();
                refreshList();
                break;
        }
    }
}
