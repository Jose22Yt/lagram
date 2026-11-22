package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LagramMessageHistoryDb;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class LagramPrivacyActivity extends UniversalFragment {

    private static final int ID_GHOST_MASTER = 1;
    private static final int ID_GHOST_DONT_READ_MSG = 2;
    private static final int ID_GHOST_DONT_READ_STORIES = 3;
    private static final int ID_GHOST_DONT_SEND_TYPING = 4;
    private static final int ID_GHOST_DONT_SEND_ONLINE = 5;
    private static final int ID_GHOST_OFFLINE_AFTER_SEND = 6;
    private static final int ID_GHOST_MARK_READ_ON_SEND = 7;

    private static final int ID_SAVE_DELETED = 10;
    private static final int ID_SAVE_EDITED = 11;
    private static final int ID_CLEAR_HISTORY_DB = 12;

    private static final int ID_ALLOW_SCREENSHOTS = 20;
    private static final int ID_BYPASS_NO_FORWARDS = 21;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryPrivacy);
    }

    @Override
    public View createView(Context context) {
        LagramConfig.ensureLoaded();
        View view = super.createView(context);
        listView.setSections();
        if (LagramConfig.amoledSettingsBackdrop && Theme.isCurrentThemeDark()) {
            fragmentView.setBackgroundColor(0xFF000000);
            listView.setBackgroundColor(0xFF000000);
        }
        return view;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // Section 1: Ghost Mode
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramGhostModeHeader)));
        items.add(UItem.asCheck(ID_GHOST_MASTER, LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramGhostModeMaster)))
                .setChecked(LagramConfig.ghostModeEnabled));
        items.add(UItem.asCheck(ID_GHOST_DONT_READ_MSG, LocaleController.getString(R.string.LagramGhostDontReadMessages))
                .setChecked(LagramConfig.ghostDontReadMessages));
        items.add(UItem.asCheck(ID_GHOST_DONT_READ_STORIES, LocaleController.getString(R.string.LagramGhostDontReadStories))
                .setChecked(LagramConfig.ghostDontReadStories));
        items.add(UItem.asCheck(ID_GHOST_DONT_SEND_TYPING, LocaleController.getString(R.string.LagramGhostDontSendTyping))
                .setChecked(LagramConfig.ghostDontSendTyping));
        items.add(UItem.asCheck(ID_GHOST_DONT_SEND_ONLINE, LocaleController.getString(R.string.LagramGhostDontSendOnline))
                .setChecked(LagramConfig.ghostDontSendOnline));
        items.add(UItem.asCheck(ID_GHOST_OFFLINE_AFTER_SEND, LocaleController.getString(R.string.LagramGhostOfflineAfterSend))
                .setChecked(LagramConfig.ghostSendOfflineAfterSend));
        items.add(UItem.asCheck(ID_GHOST_MARK_READ_ON_SEND, LocaleController.getString(R.string.LagramGhostMarkReadOnSend))
                .setChecked(LagramConfig.ghostMarkReadOnSend));
        items.add(UItem.asShadow(null));

        // Section 2: Anti-Delete & Edit History
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramHistoryHeader)));
        items.add(UItem.asCheck(ID_SAVE_DELETED, LocaleController.getString(R.string.LagramSaveDeletedMessages))
                .setChecked(LagramConfig.saveDeletedMessages));
        items.add(UItem.asCheck(ID_SAVE_EDITED, LocaleController.getString(R.string.LagramSaveEditedMessages))
                .setChecked(LagramConfig.saveEditedMessages));
        items.add(UItem.asButton(ID_CLEAR_HISTORY_DB, R.drawable.msg_delete, LocaleController.getString(R.string.LagramClearHistoryDb)).red());
        items.add(UItem.asShadow(null));

        // Section 3: Anti-Restrictions
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAntiRestrictHeader)));
        items.add(UItem.asCheck(ID_ALLOW_SCREENSHOTS, LocaleController.getString(R.string.LagramAllowScreenshots))
                .setChecked(LagramConfig.allowScreenshots));
        items.add(UItem.asCheck(ID_BYPASS_NO_FORWARDS, LocaleController.getString(R.string.LagramBypassNoForwards))
                .setChecked(LagramConfig.bypassNoForwards));
        items.add(UItem.asShadow(LocaleController.getString(R.string.LagramPrivacyInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ID_CLEAR_HISTORY_DB) {
            LagramMessageHistoryDb.clearAll();
            BulletinFactory.of(this).createSimpleBulletin(R.raw.ic_delete, LocaleController.getString(R.string.LagramHistoryClearedToast)).show();
            return;
        }
        boolean checkChanged = false;
        boolean newVal = false;
        switch (item.id) {
            case ID_GHOST_MASTER:
                newVal = LagramConfig.toggleGhostMode();
                checkChanged = true;
                break;
            case ID_GHOST_DONT_READ_MSG:
                newVal = LagramConfig.toggleGhostDontReadMessages();
                checkChanged = true;
                break;
            case ID_GHOST_DONT_READ_STORIES:
                newVal = LagramConfig.toggleGhostDontReadStories();
                checkChanged = true;
                break;
            case ID_GHOST_DONT_SEND_TYPING:
                newVal = LagramConfig.toggleGhostDontSendTyping();
                checkChanged = true;
                break;
            case ID_GHOST_DONT_SEND_ONLINE:
                newVal = LagramConfig.toggleGhostDontSendOnline();
                checkChanged = true;
                break;
            case ID_GHOST_OFFLINE_AFTER_SEND:
                newVal = LagramConfig.toggleGhostSendOfflineAfterSend();
                checkChanged = true;
                break;
            case ID_GHOST_MARK_READ_ON_SEND:
                newVal = LagramConfig.toggleGhostMarkReadOnSend();
                checkChanged = true;
                break;
            case ID_SAVE_DELETED:
                newVal = LagramConfig.toggleSaveDeletedMessages();
                if (LagramConfig.saveDeletedMessages) {
                    LagramMessageHistoryDb.ensureCacheLoaded();
                }
                checkChanged = true;
                break;
            case ID_SAVE_EDITED:
                newVal = LagramConfig.toggleSaveEditedMessages();
                if (LagramConfig.saveEditedMessages) {
                    LagramMessageHistoryDb.ensureCacheLoaded();
                }
                checkChanged = true;
                break;
            case ID_ALLOW_SCREENSHOTS:
                newVal = LagramConfig.toggleAllowScreenshots();
                checkChanged = true;
                break;
            case ID_BYPASS_NO_FORWARDS:
                newVal = LagramConfig.toggleBypassNoForwards();
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
