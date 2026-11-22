package org.telegram.ui.lagram;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;

public class LagramAppearanceActivity extends BaseLagramSettingsActivity {

    private int liquidGlassHeaderRow;
    private int ios27BottomBarRow;
    private int amoledSettingsRow;
    private int glassMessagesRow;
    private int glassMessagesBorderRow;
    private int chatEdgeBlurRow;
    private int showBubbleTailsRow;
    private int roundingPresetRow;
    private int scrollEdgeScaleRow;
    private int animationSpeedRow;
    private int useSystemFontsRow;
    private int divider1Row;

    private int foldersHeaderRow;
    private int folderTabsStyleRow;
    private int showFolderCountersRow;
    private int hideAllChatsRow;
    private int rememberLastFolderRow;
    private int dividerFoldersRow;

    private int uiVisibilityHeaderRow;
    private int hideStoriesRow;
    private int hideStoriesInProfileRow;
    private int hideStoryPostingRow;
    private int hidePhoneNumberRow;
    private int hideTelegramBloatRow;
    private int divider2Row;

    private int detailsHeaderRow;
    private int showSecondsRow;
    private int showIdAndDcRow;
    private int idFormatBotApiRow;
    private int infoRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.LagramCategoryAppearance);
    }

    @Override
    protected void updateRows() {
        rowCount = 0;
        liquidGlassHeaderRow = rowCount++;
        ios27BottomBarRow = rowCount++;
        amoledSettingsRow = rowCount++;
        glassMessagesRow = rowCount++;
        glassMessagesBorderRow = rowCount++;
        chatEdgeBlurRow = rowCount++;
        showBubbleTailsRow = rowCount++;
        roundingPresetRow = rowCount++;
        scrollEdgeScaleRow = rowCount++;
        animationSpeedRow = rowCount++;
        useSystemFontsRow = rowCount++;
        divider1Row = rowCount++;

        foldersHeaderRow = rowCount++;
        folderTabsStyleRow = rowCount++;
        showFolderCountersRow = rowCount++;
        hideAllChatsRow = rowCount++;
        rememberLastFolderRow = rowCount++;
        dividerFoldersRow = rowCount++;

        uiVisibilityHeaderRow = rowCount++;
        hideStoriesRow = rowCount++;
        hideStoriesInProfileRow = rowCount++;
        hideStoryPostingRow = rowCount++;
        hidePhoneNumberRow = rowCount++;
        hideTelegramBloatRow = rowCount++;
        divider2Row = rowCount++;

        detailsHeaderRow = rowCount++;
        showSecondsRow = rowCount++;
        showIdAndDcRow = rowCount++;
        idFormatBotApiRow = rowCount++;
        infoRow = rowCount++;
    }

    @Override
    protected int getItemViewType(int position) {
        if (position == liquidGlassHeaderRow || position == foldersHeaderRow || position == uiVisibilityHeaderRow || position == detailsHeaderRow) {
            return VIEW_TYPE_HEADER;
        } else if (position == animationSpeedRow || position == folderTabsStyleRow || position == roundingPresetRow) {
            return VIEW_TYPE_SETTINGS;
        } else if (position == infoRow) {
            return VIEW_TYPE_INFO;
        } else if (position == divider1Row || position == dividerFoldersRow || position == divider2Row) {
            return VIEW_TYPE_SHADOW;
        }
        return VIEW_TYPE_CHECK;
    }

    private String getAnimationSpeedLabel() {
        switch (LagramConfig.animationSpeed) {
            case LagramConfig.ANIM_SPEED_NORMAL:
                return LocaleController.getString(R.string.LagramAnimNormal);
            case LagramConfig.ANIM_SPEED_INSTANT:
                return LocaleController.getString(R.string.LagramAnimInstant);
            case LagramConfig.ANIM_SPEED_FAST:
            default:
                return LocaleController.getString(R.string.LagramAnimFast);
        }
    }

    private String getFolderTabsStyleLabel() {
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

    private String getRoundingPresetLabel() {
        switch (LagramConfig.roundingPreset) {
            case LagramConfig.ROUNDING_ROUND:
                return LocaleController.getString(R.string.LagramRoundingRound);
            case LagramConfig.ROUNDING_SQUIRCLE:
                return LocaleController.getString(R.string.LagramRoundingSquircle);
            case LagramConfig.ROUNDING_SQUARE:
                return LocaleController.getString(R.string.LagramRoundingSquare);
            case LagramConfig.ROUNDING_DEFAULT:
            default:
                return LocaleController.getString(R.string.LagramRoundingDefault);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER: {
                HeaderCell cell = (HeaderCell) holder.itemView;
                if (position == liquidGlassHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramLiquidGlassHeader));
                } else if (position == foldersHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramFoldersHeader));
                } else if (position == uiVisibilityHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramUiVisibilityHeader));
                } else if (position == detailsHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramDetailsHeader));
                }
                break;
            }
            case VIEW_TYPE_SETTINGS: {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                if (position == animationSpeedRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramAnimationSpeed), getAnimationSpeedLabel(), true);
                } else if (position == folderTabsStyleRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramFolderTabsStyle), getFolderTabsStyleLabel(), true);
                } else if (position == roundingPresetRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramRoundingPreset), getRoundingPresetLabel(), true);
                }
                break;
            }
            case VIEW_TYPE_CHECK: {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                if (position == ios27BottomBarRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramIos27BottomBar), LagramConfig.ios27BottomBar, true);
                } else if (position == amoledSettingsRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramAmoledSettings), LagramConfig.amoledSettingsBackdrop, true);
                } else if (position == glassMessagesRow) {
                    cell.setTextAndCheck(LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramGlassMessages)), LagramConfig.glassMessages, true);
                } else if (position == glassMessagesBorderRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramGlassMessagesBorder), LagramConfig.glassMessagesBorder, true);
                } else if (position == chatEdgeBlurRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramChatEdgeBlur), LagramConfig.chatEdgeBlur, true);
                } else if (position == showBubbleTailsRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramShowBubbleTails), LagramConfig.showBubbleTails, true);
                } else if (position == scrollEdgeScaleRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramScrollEdgeScale), LagramConfig.scrollEdgeScale, true);
                } else if (position == useSystemFontsRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramUseSystemFonts), LagramConfig.useSystemFonts, false);
                } else if (position == showFolderCountersRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramShowFolderCounters), LagramConfig.showFolderCounters, true);
                } else if (position == hideAllChatsRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHideAllChats), LagramConfig.hideAllChatsFolder, true);
                } else if (position == rememberLastFolderRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramRememberLastFolder), LagramConfig.rememberLastFolder, false);
                } else if (position == hideStoriesRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHideStories), LagramConfig.hideStories, true);
                } else if (position == hideStoriesInProfileRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHideStoriesProfile), LagramConfig.hideStoriesInProfile, true);
                } else if (position == hideStoryPostingRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHideStoryPosting), LagramConfig.hideStoryPosting, true);
                } else if (position == hidePhoneNumberRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHidePhoneNumber), LagramConfig.hidePhoneNumber, true);
                } else if (position == hideTelegramBloatRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHideTelegramBloat), LagramConfig.hideTelegramBloat, false);
                } else if (position == showSecondsRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramShowSeconds), LagramConfig.showSecondsInTime, true);
                } else if (position == showIdAndDcRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramShowIdAndDc), LagramConfig.showIdAndDc, true);
                } else if (position == idFormatBotApiRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramIdFormatBotApi), LagramConfig.idFormatBotApi, false);
                }
                break;
            }
            case VIEW_TYPE_INFO: {
                ((TextInfoPrivacyCell) holder.itemView).setText(LocaleController.getString(R.string.LagramAppearanceInfo));
                break;
            }
        }
    }

    @Override
    protected void onItemClick(View view, int position) {
        if (position == animationSpeedRow) {
            String[] options = new String[]{
                    LocaleController.getString(R.string.LagramAnimNormal),
                    LocaleController.getString(R.string.LagramAnimFast),
                    LocaleController.getString(R.string.LagramAnimInstant)
            };
            showSelectorDialog(LocaleController.getString(R.string.LagramAnimationSpeed), options, LagramConfig.animationSpeed, LagramConfig::setAnimationSpeed);
            return;
        }
        if (position == folderTabsStyleRow) {
            String[] options = new String[]{
                    LocaleController.getString(R.string.LagramFolderTabsIconText),
                    LocaleController.getString(R.string.LagramFolderTabsIconOnly),
                    LocaleController.getString(R.string.LagramFolderTabsTextOnly)
            };
            showSelectorDialog(LocaleController.getString(R.string.LagramFolderTabsStyle), options, LagramConfig.folderTabsStyle, which -> {
                LagramConfig.setFolderTabsStyle(which);
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            });
            return;
        }
        if (position == roundingPresetRow) {
            String[] options = new String[]{
                    LocaleController.getString(R.string.LagramRoundingDefault),
                    LocaleController.getString(R.string.LagramRoundingRound),
                    LocaleController.getString(R.string.LagramRoundingSquircle),
                    LocaleController.getString(R.string.LagramRoundingSquare)
            };
            showSelectorDialog(LocaleController.getString(R.string.LagramRoundingPreset), options, LagramConfig.roundingPreset, LagramConfig::setRoundingPreset);
            return;
        }
        if (!(view instanceof TextCheckCell)) {
            return;
        }
        TextCheckCell cell = (TextCheckCell) view;
        if (position == ios27BottomBarRow) {
            cell.setChecked(LagramConfig.toggleIos27BottomBar());
        } else if (position == amoledSettingsRow) {
            cell.setChecked(LagramConfig.toggleAmoledSettingsBackdrop());
            updateBackdropColor();
            if (listView != null) listView.invalidate();
        } else if (position == glassMessagesRow) {
            cell.setChecked(LagramConfig.toggleGlassMessages());
        } else if (position == glassMessagesBorderRow) {
            cell.setChecked(LagramConfig.toggleGlassMessagesBorder());
        } else if (position == chatEdgeBlurRow) {
            cell.setChecked(LagramConfig.toggleChatEdgeBlur());
        } else if (position == showBubbleTailsRow) {
            cell.setChecked(LagramConfig.toggleShowBubbleTails());
        } else if (position == scrollEdgeScaleRow) {
            cell.setChecked(LagramConfig.toggleScrollEdgeScale());
        } else if (position == useSystemFontsRow) {
            cell.setChecked(LagramConfig.toggleUseSystemFonts());
        } else if (position == showFolderCountersRow) {
            cell.setChecked(LagramConfig.toggleShowFolderCounters());
            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
        } else if (position == hideAllChatsRow) {
            cell.setChecked(LagramConfig.toggleHideAllChatsFolder());
            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
        } else if (position == rememberLastFolderRow) {
            cell.setChecked(LagramConfig.toggleRememberLastFolder());
        } else if (position == hideStoriesRow) {
            cell.setChecked(LagramConfig.toggleHideStories());
            getNotificationCenter().postNotificationName(NotificationCenter.storiesEnabledUpdate);
        } else if (position == hideStoriesInProfileRow) {
            cell.setChecked(LagramConfig.toggleHideStoriesInProfile());
        } else if (position == hideStoryPostingRow) {
            cell.setChecked(LagramConfig.toggleHideStoryPosting());
        } else if (position == hidePhoneNumberRow) {
            cell.setChecked(LagramConfig.toggleHidePhoneNumber());
            getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, 0);
        } else if (position == hideTelegramBloatRow) {
            cell.setChecked(LagramConfig.toggleHideTelegramBloat());
        } else if (position == showSecondsRow) {
            cell.setChecked(LagramConfig.toggleShowSecondsInTime());
            getNotificationCenter().postNotificationName(NotificationCenter.reloadInterface);
        } else if (position == showIdAndDcRow) {
            cell.setChecked(LagramConfig.toggleShowIdAndDc());
        } else if (position == idFormatBotApiRow) {
            cell.setChecked(LagramConfig.toggleIdFormatBotApi());
        }
    }
}
