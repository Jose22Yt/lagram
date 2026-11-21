package org.telegram.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.lagram.BaseLagramSettingsActivity;
import org.telegram.ui.lagram.LagramAppearanceActivity;
import org.telegram.ui.lagram.LagramChatsActivity;
import org.telegram.ui.lagram.LagramGeneralActivity;
import org.telegram.ui.lagram.LagramMediaActivity;
import org.telegram.ui.lagram.LagramPrivacyActivity;

public class LagramSettingsActivity extends BaseLagramSettingsActivity {

    private int quickGhostHeaderRow;
    private int quickGhostToggleRow;
    private int divider0Row;

    private int categoriesHeaderRow;
    private int appearanceRow;
    private int chatsRow;
    private int privacyRow;
    private int mediaRow;
    private int generalRow;
    private int divider1Row;

    private int backupHeaderRow;
    private int exportRow;
    private int importRow;
    private int resetRow;
    private int footerInfoRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.LagramSettings);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBackdropColor();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void updateRows() {
        rowCount = 0;
        quickGhostHeaderRow = rowCount++;
        quickGhostToggleRow = rowCount++;
        divider0Row = rowCount++;

        categoriesHeaderRow = rowCount++;
        appearanceRow = rowCount++;
        chatsRow = rowCount++;
        privacyRow = rowCount++;
        mediaRow = rowCount++;
        generalRow = rowCount++;
        divider1Row = rowCount++;

        backupHeaderRow = rowCount++;
        exportRow = rowCount++;
        importRow = rowCount++;
        resetRow = rowCount++;
        footerInfoRow = rowCount++;
    }

    @Override
    protected int getItemViewType(int position) {
        if (position == quickGhostHeaderRow || position == categoriesHeaderRow || position == backupHeaderRow) {
            return VIEW_TYPE_HEADER;
        } else if (position == quickGhostToggleRow) {
            return VIEW_TYPE_CHECK;
        } else if (position == footerInfoRow) {
            return VIEW_TYPE_INFO;
        } else if (position == divider0Row || position == divider1Row) {
            return VIEW_TYPE_SHADOW;
        }
        return VIEW_TYPE_TEXT_ICON;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER: {
                HeaderCell cell = (HeaderCell) holder.itemView;
                if (position == quickGhostHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramQuickGhostHeader));
                } else if (position == categoriesHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramCategoriesHeader));
                } else if (position == backupHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramBackupHeader));
                }
                break;
            }
            case VIEW_TYPE_CHECK: {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                if (position == quickGhostToggleRow) {
                    cell.setTextAndCheck(LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramGhostModeMaster)), LagramConfig.ghostModeEnabled, false);
                }
                break;
            }
            case VIEW_TYPE_TEXT_ICON: {
                TextCell cell = (TextCell) holder.itemView;
                if (position == appearanceRow) {
                    cell.setTextAndColorfulIcon(LocaleController.getString(R.string.LagramCategoryAppearance), R.drawable.msg_theme, 0xFF3D8BEB, true);
                } else if (position == chatsRow) {
                    cell.setTextAndColorfulIcon(LocaleController.getString(R.string.LagramCategoryChats), R.drawable.msg_discussion, 0xFFF38B31, true);
                } else if (position == privacyRow) {
                    cell.setTextAndColorfulIcon(LocaleController.getString(R.string.LagramCategoryPrivacy), R.drawable.msg_secret, 0xFF55CA47, true);
                } else if (position == mediaRow) {
                    cell.setTextAndColorfulIcon(LocaleController.getString(R.string.LagramCategoryMedia), R.drawable.msg_photos, 0xFFB659FF, true);
                } else if (position == generalRow) {
                    cell.setTextAndColorfulIcon(LocaleController.getString(R.string.LagramCategoryGeneral), R.drawable.msg_speed, 0xFF00ACC1, false);
                } else if (position == exportRow) {
                    cell.setTextAndIcon(LocaleController.getString(R.string.LagramExportSettings), R.drawable.msg_copy, true);
                } else if (position == importRow) {
                    cell.setTextAndIcon(LocaleController.getString(R.string.LagramImportSettings), R.drawable.msg_download, true);
                } else if (position == resetRow) {
                    cell.setTextAndIcon(LocaleController.getString(R.string.LagramResetSettings), R.drawable.msg_reset, false);
                }
                break;
            }
            case VIEW_TYPE_INFO: {
                ((TextInfoPrivacyCell) holder.itemView).setText(LocaleController.getString(R.string.LagramAboutFooter));
                break;
            }
        }
    }

    @Override
    protected void onItemClick(View view, int position) {
        if (position == quickGhostToggleRow && view instanceof TextCheckCell) {
            ((TextCheckCell) view).setChecked(LagramConfig.toggleGhostMode());
        } else if (position == appearanceRow) {
            presentFragment(new LagramAppearanceActivity());
        } else if (position == chatsRow) {
            presentFragment(new LagramChatsActivity());
        } else if (position == privacyRow) {
            presentFragment(new LagramPrivacyActivity());
        } else if (position == mediaRow) {
            presentFragment(new LagramMediaActivity());
        } else if (position == generalRow) {
            presentFragment(new LagramGeneralActivity());
        } else if (position == exportRow) {
            String json = LagramConfig.exportToJson();
            AndroidUtilities.addToClipboard(json);
            BulletinFactory.of(this).createSimpleBulletin(R.raw.copy, LocaleController.getString(R.string.LagramExportedToast)).show();
        } else if (position == importRow) {
            if (getParentActivity() == null) return;
            ClipboardManager cm = (ClipboardManager) getParentActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            String text = null;
            if (cm != null && cm.hasPrimaryClip()) {
                ClipData clip = cm.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0 && clip.getItemAt(0).getText() != null) {
                    text = clip.getItemAt(0).getText().toString();
                }
            }
            if (LagramConfig.importFromJson(text)) {
                updateBackdropColor();
                if (listAdapter != null) listAdapter.notifyDataSetChanged();
                BulletinFactory.of(this).createSimpleBulletin(R.raw.contact_check, LocaleController.getString(R.string.LagramImportedToast)).show();
            } else {
                BulletinFactory.of(this).createSimpleBulletin(R.raw.error, LocaleController.getString(R.string.LagramImportFailedToast)).show();
            }
        } else if (position == resetRow) {
            if (getParentActivity() == null) return;
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString(R.string.LagramResetConfirmTitle));
            builder.setMessage(LocaleController.getString(R.string.LagramResetConfirmMessage));
            builder.setPositiveButton(LocaleController.getString(R.string.Reset), (dialog, which) -> {
                LagramConfig.resetToDefaults();
                updateBackdropColor();
                if (listAdapter != null) listAdapter.notifyDataSetChanged();
                BulletinFactory.of(this).createSimpleBulletin(R.raw.ic_delete, LocaleController.getString(R.string.LagramResetToast)).show();
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            showDialog(builder.create());
        }
    }
}
