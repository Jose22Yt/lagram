package org.telegram.ui.lagram;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextSettingsCell;

public class LagramChatsActivity extends BaseLagramSettingsActivity {

    private int gesturesHeaderRow;
    private int doubleTapActionRow;
    private int hideKeyboardOnScrollRow;
    private int disableNextChannelJumpRow;
    private int forwardWithoutQuoteRow;
    private int divider1Row;

    private int confirmHeaderRow;
    private int confirmBeforeCallRow;
    private int confirmBeforeSendStickerRow;
    private int disableVoiceVideoToggleRow;
    private int disableProximitySensorRow;
    private int divider2Row;

    private int layoutHeaderRow;
    private int hideShareInChannelsRow;
    private int stickerSizeRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.LagramCategoryChats);
    }

    @Override
    protected void updateRows() {
        rowCount = 0;
        gesturesHeaderRow = rowCount++;
        doubleTapActionRow = rowCount++;
        hideKeyboardOnScrollRow = rowCount++;
        disableNextChannelJumpRow = rowCount++;
        forwardWithoutQuoteRow = rowCount++;
        divider1Row = rowCount++;

        confirmHeaderRow = rowCount++;
        confirmBeforeCallRow = rowCount++;
        confirmBeforeSendStickerRow = rowCount++;
        disableVoiceVideoToggleRow = rowCount++;
        disableProximitySensorRow = rowCount++;
        divider2Row = rowCount++;

        layoutHeaderRow = rowCount++;
        hideShareInChannelsRow = rowCount++;
        stickerSizeRow = rowCount++;
    }

    @Override
    protected int getItemViewType(int position) {
        if (position == gesturesHeaderRow || position == confirmHeaderRow || position == layoutHeaderRow) {
            return VIEW_TYPE_HEADER;
        } else if (position == doubleTapActionRow || position == stickerSizeRow) {
            return VIEW_TYPE_SETTINGS;
        } else if (position == divider1Row || position == divider2Row) {
            return VIEW_TYPE_SHADOW;
        }
        return VIEW_TYPE_CHECK;
    }

    private String getDoubleTapLabel() {
        switch (LagramConfig.doubleTapAction) {
            case LagramConfig.DOUBLE_TAP_REPLY:
                return LocaleController.getString(R.string.LagramDoubleTapReply);
            case LagramConfig.DOUBLE_TAP_COPY:
                return LocaleController.getString(R.string.LagramDoubleTapCopy);
            case LagramConfig.DOUBLE_TAP_FORWARD:
                return LocaleController.getString(R.string.LagramDoubleTapForward);
            case LagramConfig.DOUBLE_TAP_EDIT:
                return LocaleController.getString(R.string.LagramDoubleTapEdit);
            case LagramConfig.DOUBLE_TAP_SAVE:
                return LocaleController.getString(R.string.LagramDoubleTapSave);
            case LagramConfig.DOUBLE_TAP_NONE:
                return LocaleController.getString(R.string.LagramDoubleTapNone);
            case LagramConfig.DOUBLE_TAP_REACTION:
            default:
                return LocaleController.getString(R.string.LagramDoubleTapReaction);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER: {
                HeaderCell cell = (HeaderCell) holder.itemView;
                if (position == gesturesHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramChatGesturesHeader));
                } else if (position == confirmHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramChatConfirmHeader));
                } else if (position == layoutHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramChatLayoutHeader));
                }
                break;
            }
            case VIEW_TYPE_SETTINGS: {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                if (position == doubleTapActionRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramDoubleTapAction), getDoubleTapLabel(), true);
                } else if (position == stickerSizeRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramStickerSize), LagramConfig.stickerSize + " / 20", false);
                }
                break;
            }
            case VIEW_TYPE_CHECK: {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                if (position == hideKeyboardOnScrollRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHideKeyboardOnScroll), LagramConfig.hideKeyboardOnScroll, true);
                } else if (position == disableNextChannelJumpRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramDisableNextChannelJump), LagramConfig.disableNextChannelJump, true);
                } else if (position == forwardWithoutQuoteRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramForwardWithoutQuote), LagramConfig.forwardWithoutQuote, false);
                } else if (position == confirmBeforeCallRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramConfirmBeforeCall), LagramConfig.confirmBeforeCall, true);
                } else if (position == confirmBeforeSendStickerRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramConfirmBeforeSendSticker), LagramConfig.confirmBeforeSendSticker, true);
                } else if (position == disableVoiceVideoToggleRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramDisableVoiceVideoToggle), LagramConfig.disableVoiceVideoToggle, true);
                } else if (position == disableProximitySensorRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramDisableProximitySensor), LagramConfig.disableProximitySensor, false);
                } else if (position == hideShareInChannelsRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHideShareInChannels), LagramConfig.hideShareButtonInChannels, true);
                }
                break;
            }
        }
    }

    @Override
    protected void onItemClick(View view, int position) {
        if (position == doubleTapActionRow) {
            String[] options = new String[]{
                    LocaleController.getString(R.string.LagramDoubleTapReaction),
                    LocaleController.getString(R.string.LagramDoubleTapReply),
                    LocaleController.getString(R.string.LagramDoubleTapCopy),
                    LocaleController.getString(R.string.LagramDoubleTapForward),
                    LocaleController.getString(R.string.LagramDoubleTapEdit),
                    LocaleController.getString(R.string.LagramDoubleTapSave),
                    LocaleController.getString(R.string.LagramDoubleTapNone)
            };
            showSelectorDialog(LocaleController.getString(R.string.LagramDoubleTapAction), options, LagramConfig.doubleTapAction, LagramConfig::setDoubleTapAction);
            return;
        }
        if (position == stickerSizeRow) {
            final int[] sizes = new int[]{6, 8, 10, 12, 14, 16, 18, 20};
            String[] labels = new String[]{"6 (Mini)", "8", "10", "12", "14 (Default)", "16", "18", "20 (Max)"};
            int selected = 4;
            for (int i = 0; i < sizes.length; i++) {
                if (sizes[i] == LagramConfig.stickerSize) {
                    selected = i;
                    break;
                }
            }
            showSelectorDialog(LocaleController.getString(R.string.LagramStickerSize), labels, selected, which -> LagramConfig.setStickerSize(sizes[which]));
            return;
        }
        if (!(view instanceof TextCheckCell)) {
            return;
        }
        TextCheckCell cell = (TextCheckCell) view;
        if (position == hideKeyboardOnScrollRow) {
            cell.setChecked(LagramConfig.toggleHideKeyboardOnScroll());
        } else if (position == disableNextChannelJumpRow) {
            cell.setChecked(LagramConfig.toggleDisableNextChannelJump());
        } else if (position == forwardWithoutQuoteRow) {
            cell.setChecked(LagramConfig.toggleForwardWithoutQuote());
        } else if (position == confirmBeforeCallRow) {
            cell.setChecked(LagramConfig.toggleConfirmBeforeCall());
        } else if (position == confirmBeforeSendStickerRow) {
            cell.setChecked(LagramConfig.toggleConfirmBeforeSendSticker());
        } else if (position == disableVoiceVideoToggleRow) {
            cell.setChecked(LagramConfig.toggleDisableVoiceVideoToggle());
        } else if (position == disableProximitySensorRow) {
            cell.setChecked(LagramConfig.toggleDisableProximitySensor());
        } else if (position == hideShareInChannelsRow) {
            cell.setChecked(LagramConfig.toggleHideShareButtonInChannels());
        }
    }
}
