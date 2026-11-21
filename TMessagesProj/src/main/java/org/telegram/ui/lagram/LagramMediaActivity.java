package org.telegram.ui.lagram;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;

public class LagramMediaActivity extends BaseLagramSettingsActivity {

    private int networkHeaderRow;
    private int downloadBoostRow;
    private int turboUploadsRow;
    private int divider1Row;

    private int mediaHeaderRow;
    private int sendPhotosHdRow;
    private int rearCameraRoundRow;
    private int pauseVideoMinimizeRow;
    private int infoRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.LagramCategoryMedia);
    }

    @Override
    protected void updateRows() {
        rowCount = 0;
        networkHeaderRow = rowCount++;
        downloadBoostRow = rowCount++;
        turboUploadsRow = rowCount++;
        divider1Row = rowCount++;

        mediaHeaderRow = rowCount++;
        sendPhotosHdRow = rowCount++;
        rearCameraRoundRow = rowCount++;
        pauseVideoMinimizeRow = rowCount++;
        infoRow = rowCount++;
    }

    @Override
    protected int getItemViewType(int position) {
        if (position == networkHeaderRow || position == mediaHeaderRow) {
            return VIEW_TYPE_HEADER;
        } else if (position == downloadBoostRow) {
            return VIEW_TYPE_SETTINGS;
        } else if (position == infoRow) {
            return VIEW_TYPE_INFO;
        } else if (position == divider1Row) {
            return VIEW_TYPE_SHADOW;
        }
        return VIEW_TYPE_CHECK;
    }

    private String getDownloadBoostLabel() {
        switch (LagramConfig.downloadSpeedBoost) {
            case LagramConfig.DOWNLOAD_BOOST_OFF:
                return LocaleController.getString(R.string.LagramBoostOff);
            case LagramConfig.DOWNLOAD_BOOST_EXTREME:
                return LocaleController.getString(R.string.LagramBoostExtreme);
            case LagramConfig.DOWNLOAD_BOOST_FAST:
            default:
                return LocaleController.getString(R.string.LagramBoostFast);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER: {
                HeaderCell cell = (HeaderCell) holder.itemView;
                if (position == networkHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramNetworkHeader));
                } else if (position == mediaHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramMediaHeader));
                }
                break;
            }
            case VIEW_TYPE_SETTINGS: {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                if (position == downloadBoostRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramDownloadBoost), getDownloadBoostLabel(), true);
                }
                break;
            }
            case VIEW_TYPE_CHECK: {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                if (position == turboUploadsRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramTurboUploads), LagramConfig.turboUploads, false);
                } else if (position == sendPhotosHdRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramSendPhotosHd), LagramConfig.sendPhotosInHd, true);
                } else if (position == rearCameraRoundRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramRearCameraRound), LagramConfig.rearCameraForRoundVideo, true);
                } else if (position == pauseVideoMinimizeRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramPauseVideoMinimize), LagramConfig.pauseVideoOnMinimize, false);
                }
                break;
            }
            case VIEW_TYPE_INFO: {
                ((TextInfoPrivacyCell) holder.itemView).setText(LocaleController.getString(R.string.LagramMediaInfo));
                break;
            }
        }
    }

    @Override
    protected void onItemClick(View view, int position) {
        if (position == downloadBoostRow) {
            String[] options = new String[]{
                    LocaleController.getString(R.string.LagramBoostOff),
                    LocaleController.getString(R.string.LagramBoostFast),
                    LocaleController.getString(R.string.LagramBoostExtreme)
            };
            showSelectorDialog(LocaleController.getString(R.string.LagramDownloadBoost), options, LagramConfig.downloadSpeedBoost, LagramConfig::setDownloadSpeedBoost);
            return;
        }
        if (!(view instanceof TextCheckCell)) {
            return;
        }
        TextCheckCell cell = (TextCheckCell) view;
        if (position == turboUploadsRow) {
            cell.setChecked(LagramConfig.toggleTurboUploads());
        } else if (position == sendPhotosHdRow) {
            cell.setChecked(LagramConfig.toggleSendPhotosInHd());
        } else if (position == rearCameraRoundRow) {
            cell.setChecked(LagramConfig.toggleRearCameraForRoundVideo());
        } else if (position == pauseVideoMinimizeRow) {
            cell.setChecked(LagramConfig.togglePauseVideoOnMinimize());
        }
    }
}
