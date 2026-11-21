package org.telegram.ui.lagram;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LagramMessageHistoryDb;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.BulletinFactory;

public class LagramPrivacyActivity extends BaseLagramSettingsActivity {

    private int ghostHeaderRow;
    private int ghostMasterRow;
    private int ghostDontReadMessagesRow;
    private int ghostDontReadStoriesRow;
    private int ghostDontSendTypingRow;
    private int ghostDontSendOnlineRow;
    private int ghostOfflineAfterSendRow;
    private int ghostMarkReadOnSendRow;
    private int divider1Row;

    private int historyHeaderRow;
    private int saveDeletedMessagesRow;
    private int saveEditedMessagesRow;
    private int clearHistoryDbRow;
    private int divider2Row;

    private int antiRestrictHeaderRow;
    private int allowScreenshotsRow;
    private int bypassNoForwardsRow;
    private int infoRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.LagramCategoryPrivacy);
    }

    @Override
    protected void updateRows() {
        rowCount = 0;
        ghostHeaderRow = rowCount++;
        ghostMasterRow = rowCount++;
        ghostDontReadMessagesRow = rowCount++;
        ghostDontReadStoriesRow = rowCount++;
        ghostDontSendTypingRow = rowCount++;
        ghostDontSendOnlineRow = rowCount++;
        ghostOfflineAfterSendRow = rowCount++;
        ghostMarkReadOnSendRow = rowCount++;
        divider1Row = rowCount++;

        historyHeaderRow = rowCount++;
        saveDeletedMessagesRow = rowCount++;
        saveEditedMessagesRow = rowCount++;
        clearHistoryDbRow = rowCount++;
        divider2Row = rowCount++;

        antiRestrictHeaderRow = rowCount++;
        allowScreenshotsRow = rowCount++;
        bypassNoForwardsRow = rowCount++;
        infoRow = rowCount++;
    }

    @Override
    protected int getItemViewType(int position) {
        if (position == ghostHeaderRow || position == historyHeaderRow || position == antiRestrictHeaderRow) {
            return VIEW_TYPE_HEADER;
        } else if (position == clearHistoryDbRow) {
            return VIEW_TYPE_TEXT_ICON;
        } else if (position == infoRow) {
            return VIEW_TYPE_INFO;
        } else if (position == divider1Row || position == divider2Row) {
            return VIEW_TYPE_SHADOW;
        }
        return VIEW_TYPE_CHECK;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER: {
                HeaderCell cell = (HeaderCell) holder.itemView;
                if (position == ghostHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramGhostModeHeader));
                } else if (position == historyHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramHistoryHeader));
                } else if (position == antiRestrictHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramAntiRestrictHeader));
                }
                break;
            }
            case VIEW_TYPE_TEXT_ICON: {
                TextCell cell = (TextCell) holder.itemView;
                if (position == clearHistoryDbRow) {
                    cell.setTextAndIcon(LocaleController.getString(R.string.LagramClearHistoryDb), R.drawable.msg_delete, false);
                }
                break;
            }
            case VIEW_TYPE_CHECK: {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                if (position == ghostMasterRow) {
                    cell.setTextAndCheck(LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramGhostModeMaster)), LagramConfig.ghostModeEnabled, true);
                } else if (position == ghostDontReadMessagesRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramGhostDontReadMessages), LagramConfig.ghostDontReadMessages, true);
                } else if (position == ghostDontReadStoriesRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramGhostDontReadStories), LagramConfig.ghostDontReadStories, true);
                } else if (position == ghostDontSendTypingRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramGhostDontSendTyping), LagramConfig.ghostDontSendTyping, true);
                } else if (position == ghostDontSendOnlineRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramGhostDontSendOnline), LagramConfig.ghostDontSendOnline, true);
                } else if (position == ghostOfflineAfterSendRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramGhostOfflineAfterSend), LagramConfig.ghostSendOfflineAfterSend, true);
                } else if (position == ghostMarkReadOnSendRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramGhostMarkReadOnSend), LagramConfig.ghostMarkReadOnSend, false);
                } else if (position == saveDeletedMessagesRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramSaveDeletedMessages), LagramConfig.saveDeletedMessages, true);
                } else if (position == saveEditedMessagesRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramSaveEditedMessages), LagramConfig.saveEditedMessages, true);
                } else if (position == allowScreenshotsRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramAllowScreenshots), LagramConfig.allowScreenshots, true);
                } else if (position == bypassNoForwardsRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramBypassNoForwards), LagramConfig.bypassNoForwards, false);
                }
                break;
            }
            case VIEW_TYPE_INFO: {
                ((TextInfoPrivacyCell) holder.itemView).setText(LocaleController.getString(R.string.LagramPrivacyInfo));
                break;
            }
        }
    }

    @Override
    protected void onItemClick(View view, int position) {
        if (position == clearHistoryDbRow) {
            LagramMessageHistoryDb.clearAll();
            BulletinFactory.of(this).createSimpleBulletin(R.raw.ic_delete, LocaleController.getString(R.string.LagramHistoryClearedToast)).show();
            return;
        }
        if (!(view instanceof TextCheckCell)) {
            return;
        }
        TextCheckCell cell = (TextCheckCell) view;
        if (position == ghostMasterRow) {
            cell.setChecked(LagramConfig.toggleGhostMode());
        } else if (position == ghostDontReadMessagesRow) {
            cell.setChecked(LagramConfig.toggleGhostDontReadMessages());
        } else if (position == ghostDontReadStoriesRow) {
            cell.setChecked(LagramConfig.toggleGhostDontReadStories());
        } else if (position == ghostDontSendTypingRow) {
            cell.setChecked(LagramConfig.toggleGhostDontSendTyping());
        } else if (position == ghostDontSendOnlineRow) {
            cell.setChecked(LagramConfig.toggleGhostDontSendOnline());
        } else if (position == ghostOfflineAfterSendRow) {
            cell.setChecked(LagramConfig.toggleGhostSendOfflineAfterSend());
        } else if (position == ghostMarkReadOnSendRow) {
            cell.setChecked(LagramConfig.toggleGhostMarkReadOnSend());
        } else if (position == saveDeletedMessagesRow) {
            cell.setChecked(LagramConfig.toggleSaveDeletedMessages());
            if (LagramConfig.saveDeletedMessages) {
                LagramMessageHistoryDb.ensureCacheLoaded();
            }
        } else if (position == saveEditedMessagesRow) {
            cell.setChecked(LagramConfig.toggleSaveEditedMessages());
            if (LagramConfig.saveEditedMessages) {
                LagramMessageHistoryDb.ensureCacheLoaded();
            }
        } else if (position == allowScreenshotsRow) {
            cell.setChecked(LagramConfig.toggleAllowScreenshots());
        } else if (position == bypassNoForwardsRow) {
            cell.setChecked(LagramConfig.toggleBypassNoForwards());
        }
    }
}
