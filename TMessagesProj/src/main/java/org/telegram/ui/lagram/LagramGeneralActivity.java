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
    private int hapticsLevelRow;
    private int divider1Row;

    private int behaviorHeaderRow;
    private int showJoinDateRow;
    private int openArchiveOnPullRow;
    private int dividerBehaviorRow;

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
        hapticsLevelRow = rowCount++;
        divider1Row = rowCount++;

        behaviorHeaderRow = rowCount++;
        showJoinDateRow = rowCount++;
        openArchiveOnPullRow = rowCount++;
        dividerBehaviorRow = rowCount++;

        cleanupHeaderRow = rowCount++;
        disableSponsoredRow = rowCount++;
        hideSimilarChannelsRow = rowCount++;
        infoRow = rowCount++;
    }

    @Override
    protected int getItemViewType(int position) {
        if (position == performanceHeaderRow || position == behaviorHeaderRow || position == cleanupHeaderRow) {
            return VIEW_TYPE_HEADER;
        } else if (position == forcePerfClassRow || position == hapticsLevelRow) {
            return VIEW_TYPE_SETTINGS;
        } else if (position == infoRow) {
            return VIEW_TYPE_INFO;
        } else if (position == divider1Row || position == dividerBehaviorRow) {
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

    private String getHapticsLabel() {
        switch (LagramConfig.hapticsLevel) {
            case LagramConfig.HAPTICS_OFF:
                return LocaleController.getString(R.string.LagramHapticsOff);
            case LagramConfig.HAPTICS_SOFT:
                return LocaleController.getString(R.string.LagramHapticsSoft);
            case LagramConfig.HAPTICS_STRONG:
                return LocaleController.getString(R.string.LagramHapticsStrong);
            case LagramConfig.HAPTICS_NORMAL:
            default:
                return LocaleController.getString(R.string.LagramHapticsNormal);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER: {
                HeaderCell cell = (HeaderCell) holder.itemView;
                if (position == performanceHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramPerformanceHeader));
                } else if (position == behaviorHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramBehaviorHeader));
                } else if (position == cleanupHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramAntiBloatHeader));
                }
                break;
            }
            case VIEW_TYPE_SETTINGS: {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                if (position == forcePerfClassRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramForcePerfClass), getPerfClassLabel(), true);
                } else if (position == hapticsLevelRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramHapticsLevel), getHapticsLabel(), false);
                }
                break;
            }
            case VIEW_TYPE_CHECK: {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                if (position == highRefreshRateRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHighRefreshRate), LagramConfig.highRefreshRateScroll, true);
                } else if (position == showJoinDateRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramShowJoinDate), LagramConfig.showJoinDate, true);
                } else if (position == openArchiveOnPullRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramOpenArchiveOnPull), LagramConfig.openArchiveOnPull, false);
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
        if (position == hapticsLevelRow) {
            String[] options = new String[]{
                    LocaleController.getString(R.string.LagramHapticsOff),
                    LocaleController.getString(R.string.LagramHapticsSoft),
                    LocaleController.getString(R.string.LagramHapticsNormal),
                    LocaleController.getString(R.string.LagramHapticsStrong)
            };
            showSelectorDialog(LocaleController.getString(R.string.LagramHapticsLevel), options, LagramConfig.hapticsLevel, LagramConfig::setHapticsLevel);
            return;
        }
        if (!(view instanceof TextCheckCell)) {
            return;
        }
        TextCheckCell cell = (TextCheckCell) view;
        if (position == highRefreshRateRow) {
            cell.setChecked(LagramConfig.toggleHighRefreshRateScroll());
        } else if (position == showJoinDateRow) {
            cell.setChecked(LagramConfig.toggleShowJoinDate());
        } else if (position == openArchiveOnPullRow) {
            cell.setChecked(LagramConfig.toggleOpenArchiveOnPull());
        } else if (position == disableSponsoredRow) {
            cell.setChecked(LagramConfig.toggleDisableSponsoredMessages());
        } else if (position == hideSimilarChannelsRow) {
            cell.setChecked(LagramConfig.toggleHideSimilarChannels());
        }
    }
}
