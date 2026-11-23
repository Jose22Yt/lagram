package org.telegram.ui;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.lagram.LagramGlassBubbleEngine;
import org.telegram.ui.lagram.LagramPrivacyActivity;
import org.telegram.ui.lagram.LagramRealPreviews;
import org.telegram.ui.lagram.LagramSettingsHeaderView;
import org.telegram.ui.lagram.OpexgramStyleWidgets;

import java.util.ArrayList;

/**
 * Ultra-clean, focused Lagram Settings containing ONLY the 4 requested core features:
 * 1. Liquid Glass message bubbles (with live real wallpaper preview)
 * 2. 3D Scroll animation (with live draggable preview)
 * 3. Message tap focus animation + 3 menu styles (with live tappable preview)
 * 4. Ghost Mode (quick toggle + full configuration screen)
 */
public class LagramSettingsActivity extends UniversalFragment {

    // 1. Liquid Glass
    private static final int ID_PREVIEW_GLASS = 100;
    private static final int ID_GLASS_MESSAGES = 101;
    private static final int ID_GLASS_TINT = 102;
    private static final int ID_GLASS_ALPHA = 103;
    private static final int ID_GLASS_BLUR_LEVEL = 104;
    private static final int ID_GLASS_BORDER = 105;
    private static final int ID_GLASS_BORDER_OPACITY = 106;
    private static final int ID_GLASS_HIGHLIGHTS = 107;

    // 2. 3D Scroll Animation
    private static final int ID_PREVIEW_SCROLL = 200;
    private static final int ID_SCROLL_EDGE = 201;
    private static final int ID_SCROLL_MODE = 202;

    // 3. Message Tap Animation + 3 Menu Styles
    private static final int ID_PREVIEW_MSG_MENU = 300;
    private static final int ID_FOCUS_ENABLED = 301;
    private static final int ID_FOCUS_BLUR = 302;
    private static final int ID_FOCUS_STYLE = 303;

    // 3b. Chat Header Styles (FlexyGram-inspired)
    private static final int ID_PREVIEW_HEADER = 350;
    private static final int ID_HEADER_STYLE = 351;

    // 4. Ghost Mode
    private static final int ID_GHOST_MASTER = 400;
    private static final int ID_OPEN_GHOST_CONFIG = 401;

    // Footer
    private static final int ID_RESET = 500;

    private LagramSettingsHeaderView headerView;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramSettings);
    }

    @Override
    public View createView(Context context) {
        LagramConfig.ensureLoaded();
        headerView = new LagramSettingsHeaderView(context, getResourceProvider());
        View view = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        return view;
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

        items.add(UItem.asCustomShadow(headerView, 253));

        // 1. Liquid Glass for Chat Bubbles
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramGlassBlurHeader)));
        items.add(LagramRealPreviews.RealBubblesGlassPreviewCell.Factory.of(ID_PREVIEW_GLASS));
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
                        LagramGlassBubbleEngine.invalidateCapture();
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
        items.add(UItem.asShadow(-1, null));

        // 2. 3D Scroll Animation
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
        items.add(UItem.asShadow(-2, null));

        // 3. Message Tap Focus Animation + 3 Menu Styles
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMessageFocusHeader)));
        items.add(LagramRealPreviews.RealMessageMenuPreviewCell.Factory.of(ID_PREVIEW_MSG_MENU));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_FOCUS_ENABLED, 0,
                LocaleController.getString(R.string.LagramMessageFocus),
                null,
                LagramConfig.messageFocusEnabled));
        if (LagramConfig.messageFocusEnabled) {
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_FOCUS_BLUR, 0,
                    LocaleController.getString(R.string.LagramMessageFocusBlur),
                    null,
                    LagramConfig.messageFocusBlur));
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_FOCUS_STYLE,
                    LocaleController.getString(R.string.LagramMessageFocusStyle),
                    new String[]{
                            LocaleController.getString(R.string.LagramFocusStyleList),
                            LocaleController.getString(R.string.LagramFocusStyleTiles),
                            LocaleController.getString(R.string.LagramFocusStyleCapsule)
                    },
                    LagramConfig.messageFocusStyle,
                    idx -> {
                        LagramConfig.setMessageFocusStyle(idx);
                        refreshList();
                    }));
        }
        items.add(UItem.asShadow(-3, null));

        // 4. Chat Top Bar Styles (FlexyGram-inspired: Default, Signature, Fusion, Portrait Matte, Floating)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatHeaderStyle)));
        items.add(LagramRealPreviews.RealChatHeaderPreviewCell.Factory.of(ID_PREVIEW_HEADER));
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                ID_HEADER_STYLE,
                null,
                new String[]{
                        LocaleController.getString(R.string.LagramChatHeaderDefault),
                        LocaleController.getString(R.string.LagramChatHeaderSignature),
                        LocaleController.getString(R.string.LagramChatHeaderFusion),
                        LocaleController.getString(R.string.LagramChatHeaderPortrait),
                        LocaleController.getString(R.string.LagramChatHeaderFloating)
                },
                LagramConfig.chatHeaderStyle,
                idx -> {
                    LagramConfig.setChatHeaderStyle(idx);
                    refreshList();
                }));
        items.add(UItem.asShadow(-4, null));

        // 5. Ghost Mode
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramGhostModeHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_GHOST_MASTER, R.drawable.msg_secret,
                LocaleController.getString(R.string.LagramGhostModeMaster),
                null,
                LagramConfig.ghostModeEnabled));
        items.add(SettingsActivity.SettingCell.Factory.of(
                ID_OPEN_GHOST_CONFIG,
                IconBackgroundColors.BLUE_DEEP.top,
                IconBackgroundColors.BLUE_DEEP.bottom,
                R.drawable.msg_secret,
                LocaleController.getString(R.string.LagramCategoryPrivacy)));
        items.add(UItem.asShadow(-5, null));

        // Reset & Version
        items.add(UItem.asButton(ID_RESET, R.drawable.msg_reset, LocaleController.getString(R.string.LagramResetSettings)).red());
        items.add(UItem.asCenterShadow(LagramSettingsHeaderView.getVersionString()));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_GLASS_MESSAGES:
                LagramConfig.toggleGlassMessages();
                LagramGlassBubbleEngine.invalidateCapture();
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewTheme, false);
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
            case ID_SCROLL_EDGE:
                LagramConfig.toggleScrollEdgeScale();
                refreshList();
                break;
            case ID_FOCUS_ENABLED:
                LagramConfig.toggleMessageFocusEnabled();
                refreshList();
                break;
            case ID_FOCUS_BLUR:
                LagramConfig.toggleMessageFocusBlur();
                refreshList();
                break;
            case ID_GHOST_MASTER:
                LagramConfig.toggleGhostMode();
                refreshList();
                break;
            case ID_OPEN_GHOST_CONFIG:
                presentFragment(new LagramPrivacyActivity());
                break;
            case ID_RESET:
                if (getParentActivity() == null) return;
                AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
                b.setTitle(LocaleController.getString(R.string.LagramResetSettings));
                b.setMessage(LocaleController.getString(R.string.LagramResetConfirmMessage));
                b.setPositiveButton(LocaleController.getString(R.string.Reset), (d, w) -> {
                    LagramConfig.resetToDefaults();
                    LagramGlassBubbleEngine.invalidateCapture();
                    refreshList();
                    BulletinFactory.of(this).createSimpleBulletin(R.raw.ic_delete, LocaleController.getString(R.string.LagramResetToast)).show();
                });
                b.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                showDialog(b.create());
                break;
        }
    }
}
