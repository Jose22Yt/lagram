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

public class LagramGeneralActivity extends BaseLagramSettingsActivity {

    private int performanceHeaderRow;
    private int highRefreshRateRow;
    private int forcePerfClassRow;
    private int divider1Row;

    private int cleanupHeaderRow;
    private int disableSponsoredRow;
    private int hideSimilarChannelsRow;
    private int infoRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.LagramCategoryGeneral);
    }

    @Override
    protected void updateRows() {
        rowCount = 0;
        performanceHeaderRow = rowCount++;
        highRefreshRateRow = rowCount++;
        forcePerfClassRow = rowCount++;
        divider1Row = rowCount++;

        cleanupHeaderRow = rowCount++;
        disableSponsoredRow = rowCount++;
        hideSimilarChannelsRow = rowCount++;
        infoRow = rowCount++;
    }

    @Override
    protected int getItemViewType(int position) {
        if (position == performanceHeaderRow || position == cleanupHeaderRow) {
            return VIEW_TYPE_HEADER;
        } else if (position == forcePerfClassRow) {
            return VIEW_TYPE_SETTINGS;
        } else if (position == infoRow) {
            return VIEW_TYPE_INFO;
        } else if (position == divider1Row) {
            return VIEW_TYPE_SHADOW;
        }
        return VIEW_TYPE_CHECK;
    }

    private String getPerfClassLabel() {
        switch (LagramConfig.forcePerformanceClass) {
            case 0:
                return LocaleController.getString(R.string.LagramPerfLow);
            case 1:
                return LocaleController.getString(R.string.LagramPerfAverage);
            case 2:
                return LocaleController.getString(R.string.LagramPerfHigh);
            default:
                return LocaleController.getString(R.string.LagramPerfAuto);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER: {
                HeaderCell cell = (HeaderCell) holder.itemView;
                if (position == performanceHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramPerformanceHeader));
                } else if (position == cleanupHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramAntiBloatHeader));
                }
                break;
            }
            case VIEW_TYPE_SETTINGS: {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                if (position == forcePerfClassRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramForcePerfClass), getPerfClassLabel(), false);
                }
                break;
            }
            case VIEW_TYPE_CHECK: {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                if (position == highRefreshRateRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHighRefreshRate), LagramConfig.highRefreshRateScroll, true);
                } else if (position == disableSponsoredRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramDisableSponsored), LagramConfig.disableSponsoredMessages, true);
                } else if (position == hideSimilarChannelsRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHideSimilarChannels), LagramConfig.hideSimilarChannels, false);
                }
                break;
            }
            case VIEW_TYPE_INFO: {
                ((TextInfoPrivacyCell) holder.itemView).setText(LocaleController.getString(R.string.LagramGeneralInfo));
                break;
            }
        }
    }

    @Override
    protected void onItemClick(View view, int position) {
        if (position == forcePerfClassRow) {
            String[] options = new String[]{
                    LocaleController.getString(R.string.LagramPerfAuto),
                    LocaleController.getString(R.string.LagramPerfLow),
                    LocaleController.getString(R.string.LagramPerfAverage),
                    LocaleController.getString(R.string.LagramPerfHigh)
            };
            int selected = LagramConfig.forcePerformanceClass + 1;
            showSelectorDialog(LocaleController.getString(R.string.LagramForcePerfClass), options, selected, which -> LagramConfig.setForcePerformanceClass(which - 1));
            return;
        }
        if (!(view instanceof TextCheckCell)) {
            return;
        }
        TextCheckCell cell = (TextCheckCell) view;
        if (position == highRefreshRateRow) {
            cell.setChecked(LagramConfig.toggleHighRefreshRateScroll());
        } else if (position == disableSponsoredRow) {
            cell.setChecked(LagramConfig.toggleDisableSponsoredMessages());
        } else if (position == hideSimilarChannelsRow) {
            cell.setChecked(LagramConfig.toggleHideSimilarChannels());
        }
    }
}
