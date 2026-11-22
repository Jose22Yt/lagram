package org.telegram.ui.lagram;

import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

public class LagramPrivacyActivity extends BaseLagramSettingsActivity {

    private static final int ID_PREVIEW_DELETED = 300;

    private static final int ID_GHOST_MASTER = 301;
    private static final int ID_GHOST_READ_MSG = 302;
    private static final int ID_GHOST_READ_STORY = 303;
    private static final int ID_GHOST_TYPING = 304;
    private static final int ID_GHOST_ONLINE = 305;
    private static final int ID_GHOST_OFFLINE_AFTER = 306;
    private static final int ID_GHOST_MARK_ON_SEND = 307;

    private static final int ID_SAVE_DELETED = 310;
    private static final int ID_SAVE_EDITED = 311;
    private static final int ID_DELETED_BUBBLE_COLOR = 312;
    private static final int ID_DELETED_TEXT_COLOR = 313;
    private static final int ID_DELETED_ICON = 314;
    private static final int ID_DELETED_DIM = 315;
    private static final int ID_DELETED_STRIKE = 316;
    private static final int ID_DELETED_TIME = 317;

    private static final int ID_ALLOW_SCREENSHOTS = 320;
    private static final int ID_BYPASS_NO_FORWARDS = 321;

    private static final int[] BUBBLE_TINT_PALETTE = {
            0,          // Default / none
            0xFFE53935, // Crimson
            0xFFFB8C00, // Amber
            0xFF8E24AA, // Violet
            0xFF00ACC1, // Cyan
            0xFF43A047, // Emerald
            0xFFD81B60, // Rose
            0xFF546E7A  // Slate
    };

    private static final int[] TEXT_COLOR_PALETTE = {
            0,          // Default theme time color
            0xFFFF5252, // Coral Red
            0xFFFFAB40, // Warm Orange
            0xFFCE93D8, // Soft Violet
            0xFF4DD0E1, // Bright Cyan
            0xFF69F0AE, // Mint Green
            0xFFFF80AB, // Pink
            0xFFFFFFFF  // Pure White
    };

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryPrivacy);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // 1. REAL Deleted Message & Ghost Preview over REAL Lagram wallpaper!
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramGhostModeHeader)));
        items.add(LagramRealPreviews.RealDeletedMessagePreviewCell.Factory.of(ID_PREVIEW_DELETED));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_GHOST_MASTER, R.drawable.msg_secret,
                LocaleController.getString(R.string.LagramGhostModeMaster),
                null,
                LagramConfig.ghostModeEnabled));
        if (LagramConfig.ghostModeEnabled) {
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GHOST_READ_MSG, 0,
                    LocaleController.getString(R.string.LagramGhostDontReadMessages),
                    null,
                    LagramConfig.ghostDontReadMessages));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GHOST_READ_STORY, 0,
                    LocaleController.getString(R.string.LagramGhostDontReadStories),
                    null,
                    LagramConfig.ghostDontReadStories));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GHOST_TYPING, 0,
                    LocaleController.getString(R.string.LagramGhostDontSendTyping),
                    null,
                    LagramConfig.ghostDontSendTyping));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GHOST_ONLINE, 0,
                    LocaleController.getString(R.string.LagramGhostDontSendOnline),
                    null,
                    LagramConfig.ghostDontSendOnline));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GHOST_OFFLINE_AFTER, 0,
                    LocaleController.getString(R.string.LagramGhostOfflineAfterSend),
                    null,
                    LagramConfig.ghostSendOfflineAfterSend));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GHOST_MARK_ON_SEND, 0,
                    LocaleController.getString(R.string.LagramGhostMarkReadOnSend),
                    null,
                    LagramConfig.ghostMarkReadOnSend));
        }
        items.add(UItem.asShadow(null));

        // 2. Deleted & Edited Message History + Full Visual Customization!
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramDeletedStyleHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SAVE_DELETED, R.drawable.msg_delete,
                LocaleController.getString(R.string.LagramSaveDeletedMessages),
                null,
                LagramConfig.saveDeletedMessages));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SAVE_EDITED, R.drawable.msg_edit,
                LocaleController.getString(R.string.LagramSaveEditedMessages),
                null,
                LagramConfig.saveEditedMessages));
        items.add(OpexgramStyleWidgets.OpexColorSwatchesCell.Factory.of(
                ID_DELETED_BUBBLE_COLOR,
                LocaleController.getString(R.string.LagramDeletedBubbleColor),
                BUBBLE_TINT_PALETTE,
                LagramConfig.deletedBubbleTintColor,
                color -> {
                    LagramConfig.setDeletedBubbleTintColor(color);
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexColorSwatchesCell.Factory.of(
                ID_DELETED_TEXT_COLOR,
                LocaleController.getString(R.string.LagramDeletedTextColor),
                TEXT_COLOR_PALETTE,
                LagramConfig.deletedTextColor,
                color -> {
                    LagramConfig.setDeletedTextColor(color);
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                ID_DELETED_ICON,
                LocaleController.getString(R.string.LagramDeletedIconStyle),
                new String[]{
                        "\uD83D\uDDD1 " + LocaleController.getString(R.string.LagramDeletedIconTrash),
                        "\uD83D\uDC7B " + LocaleController.getString(R.string.LagramDeletedIconGhost),
                        "\uD83D\uDC41 " + LocaleController.getString(R.string.LagramDeletedIconEye),
                        "\u2715 " + LocaleController.getString(R.string.LagramDeletedIconCross),
                        "\uD83D\uDD25 " + LocaleController.getString(R.string.LagramDeletedIconFlame),
                        LocaleController.getString(R.string.LagramDeletedIconNone)
                },
                LagramConfig.deletedIconStyle,
                idx -> {
                    LagramConfig.setDeletedIconStyle(idx);
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_DELETED_DIM, 0,
                LocaleController.getString(R.string.LagramDeletedMessageDim),
                LocaleController.getString(R.string.LagramDeletedMessageDimDesc),
                LagramConfig.deletedMessageDim));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_DELETED_STRIKE, 0,
                LocaleController.getString(R.string.LagramDeletedMessageStrike),
                LocaleController.getString(R.string.LagramDeletedMessageStrikeDesc),
                LagramConfig.deletedMessageStrike));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_DELETED_TIME, 0,
                LocaleController.getString(R.string.LagramShowDeletedTime),
                LocaleController.getString(R.string.LagramShowDeletedTimeDesc),
                LagramConfig.showDeletedTime));
        items.add(UItem.asShadow(null));

        // 3. Anti-Restrictions
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAntiRestrictHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_ALLOW_SCREENSHOTS, 0,
                LocaleController.getString(R.string.LagramAllowScreenshots),
                null,
                LagramConfig.allowScreenshots));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_BYPASS_NO_FORWARDS, 0,
                LocaleController.getString(R.string.LagramBypassNoForwards),
                null,
                LagramConfig.bypassNoForwards));
        items.add(UItem.asShadow(LocaleController.getString(R.string.LagramPrivacyInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_GHOST_MASTER:
                LagramConfig.toggleGhostMode();
                refreshList();
                break;
            case ID_GHOST_READ_MSG:
                LagramConfig.toggleGhostDontReadMessages();
                refreshList();
                break;
            case ID_GHOST_READ_STORY:
                LagramConfig.toggleGhostDontReadStories();
                refreshList();
                break;
            case ID_GHOST_TYPING:
                LagramConfig.toggleGhostDontSendTyping();
                refreshList();
                break;
            case ID_GHOST_ONLINE:
                LagramConfig.toggleGhostDontSendOnline();
                refreshList();
                break;
            case ID_GHOST_OFFLINE_AFTER:
                LagramConfig.toggleGhostSendOfflineAfterSend();
                refreshList();
                break;
            case ID_GHOST_MARK_ON_SEND:
                LagramConfig.toggleGhostMarkReadOnSend();
                refreshList();
                break;
            case ID_SAVE_DELETED:
                LagramConfig.toggleSaveDeletedMessages();
                refreshList();
                break;
            case ID_SAVE_EDITED:
                LagramConfig.toggleSaveEditedMessages();
                refreshList();
                break;
            case ID_DELETED_DIM:
                LagramConfig.toggleDeletedMessageDim();
                refreshList();
                break;
            case ID_DELETED_STRIKE:
                LagramConfig.toggleDeletedMessageStrike();
                refreshList();
                break;
            case ID_DELETED_TIME:
                LagramConfig.toggleShowDeletedTime();
                refreshList();
                break;
            case ID_ALLOW_SCREENSHOTS:
                LagramConfig.toggleAllowScreenshots();
                refreshList();
                break;
            case ID_BYPASS_NO_FORWARDS:
                LagramConfig.toggleBypassNoForwards();
                refreshList();
                break;
        }
    }
}
