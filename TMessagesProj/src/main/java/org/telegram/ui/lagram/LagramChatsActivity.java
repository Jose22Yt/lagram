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

public class LagramChatsActivity extends BaseLagramSettingsActivity {

    private int focusHeaderRow;
    private int messageFocusEnabledRow;
    private int messageFocusBlurRow;
    private int messageFocusStyleRow;
    private int focusInfoRow;

    private int gesturesHeaderRow;
    private int doubleTapOutgoingRow;
    private int doubleTapIncomingRow;
    private int hideKeyboardOnScrollRow;
    private int disableNextChannelJumpRow;
    private int forwardWithoutQuoteRow;
    private int showPageUpButtonRow;
    private int textAppearAnimationRow;
    private int deleteEffectRow;
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
    private int gifSizeRow;
    private int unlimitedSelectionRow;
    private int fastSelectionRow;
    private int divider3Row;

    private int detailsHeaderRow;
    private int showWeekdayInDateRow;
    private int showForwardTimeRow;
    private int showPollPreviewRow;
    private int showStickerPackOwnerRow;
    private int showForwardsCounterRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.LagramCategoryChats);
    }

    @Override
    protected void updateRows() {
        rowCount = 0;
        focusHeaderRow = rowCount++;
        messageFocusEnabledRow = rowCount++;
        messageFocusBlurRow = rowCount++;
        messageFocusStyleRow = rowCount++;
        focusInfoRow = rowCount++;

        gesturesHeaderRow = rowCount++;
        doubleTapOutgoingRow = rowCount++;
        doubleTapIncomingRow = rowCount++;
        hideKeyboardOnScrollRow = rowCount++;
        disableNextChannelJumpRow = rowCount++;
        forwardWithoutQuoteRow = rowCount++;
        showPageUpButtonRow = rowCount++;
        textAppearAnimationRow = rowCount++;
        deleteEffectRow = rowCount++;
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
        gifSizeRow = rowCount++;
        unlimitedSelectionRow = rowCount++;
        fastSelectionRow = rowCount++;
        divider3Row = rowCount++;

        detailsHeaderRow = rowCount++;
        showWeekdayInDateRow = rowCount++;
        showForwardTimeRow = rowCount++;
        showPollPreviewRow = rowCount++;
        showStickerPackOwnerRow = rowCount++;
        showForwardsCounterRow = rowCount++;
    }

    @Override
    protected int getItemViewType(int position) {
        if (position == focusHeaderRow || position == gesturesHeaderRow || position == confirmHeaderRow || position == layoutHeaderRow || position == detailsHeaderRow) {
            return VIEW_TYPE_HEADER;
        } else if (position == messageFocusStyleRow || position == doubleTapOutgoingRow || position == doubleTapIncomingRow
                || position == stickerSizeRow || position == gifSizeRow || position == deleteEffectRow) {
            return VIEW_TYPE_SETTINGS;
        } else if (position == focusInfoRow) {
            return VIEW_TYPE_INFO;
        } else if (position == divider1Row || position == divider2Row || position == divider3Row) {
            return VIEW_TYPE_SHADOW;
        }
        return VIEW_TYPE_CHECK;
    }

    private String getFocusStyleLabel() {
        switch (LagramConfig.messageFocusStyle) {
            case LagramConfig.FOCUS_STYLE_LIST:
                return LocaleController.getString(R.string.LagramFocusStyleList);
            case LagramConfig.FOCUS_STYLE_TILES:
                return LocaleController.getString(R.string.LagramFocusStyleTiles);
            case LagramConfig.FOCUS_STYLE_CAPSULE:
            default:
                return LocaleController.getString(R.string.LagramFocusStyleCapsule);
        }
    }

    private String getDoubleTapLabel(int action) {
        switch (action) {
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

    private String getDeleteEffectLabel() {
        switch (LagramConfig.deleteEffect) {
            case LagramConfig.DELETE_EFFECT_DEFAULT:
                return LocaleController.getString(R.string.LagramDeleteEffectDefault);
            case LagramConfig.DELETE_EFFECT_SHARDS:
                return LocaleController.getString(R.string.LagramDeleteEffectShards);
            case LagramConfig.DELETE_EFFECT_PORTAL:
                return LocaleController.getString(R.string.LagramDeleteEffectPortal);
            case LagramConfig.DELETE_EFFECT_THANOS:
            default:
                return LocaleController.getString(R.string.LagramDeleteEffectThanos);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER: {
                HeaderCell cell = (HeaderCell) holder.itemView;
                if (position == focusHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramMessageFocusHeader));
                } else if (position == gesturesHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramChatGesturesHeader));
                } else if (position == confirmHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramChatConfirmHeader));
                } else if (position == layoutHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramChatLayoutHeader));
                } else if (position == detailsHeaderRow) {
                    cell.setText(LocaleController.getString(R.string.LagramChatDetailsHeader));
                }
                break;
            }
            case VIEW_TYPE_SETTINGS: {
                TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                if (position == messageFocusStyleRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramMessageFocusStyle), getFocusStyleLabel(), false);
                } else if (position == doubleTapOutgoingRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramDoubleTapOutgoing), getDoubleTapLabel(LagramConfig.doubleTapActionOutgoing), true);
                } else if (position == doubleTapIncomingRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramDoubleTapIncoming), getDoubleTapLabel(LagramConfig.doubleTapActionIncoming), true);
                } else if (position == deleteEffectRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramDeleteEffect), getDeleteEffectLabel(), false);
                } else if (position == stickerSizeRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramStickerSize), LagramConfig.stickerSize + " / 20", true);
                } else if (position == gifSizeRow) {
                    cell.setTextAndValue(LocaleController.getString(R.string.LagramGifSize), LagramConfig.gifSize + " / 20", true);
                }
                break;
            }
            case VIEW_TYPE_CHECK: {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                if (position == messageFocusEnabledRow) {
                    cell.setTextAndCheck(LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramMessageFocus)), LagramConfig.messageFocusEnabled, true);
                } else if (position == messageFocusBlurRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramMessageFocusBlur), LagramConfig.messageFocusBlur, true);
                } else if (position == hideKeyboardOnScrollRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramHideKeyboardOnScroll), LagramConfig.hideKeyboardOnScroll, true);
                } else if (position == disableNextChannelJumpRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramDisableNextChannelJump), LagramConfig.disableNextChannelJump, true);
                } else if (position == forwardWithoutQuoteRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramForwardWithoutQuote), LagramConfig.forwardWithoutQuote, true);
                } else if (position == showPageUpButtonRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramShowPageUpButton), LagramConfig.showPageUpButton, true);
                } else if (position == textAppearAnimationRow) {
                    cell.setTextAndCheck(LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramTextAppearAnimation)), LagramConfig.textAppearAnimation, true);
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
                } else if (position == unlimitedSelectionRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramUnlimitedSelection), LagramConfig.unlimitedSelection, true);
                } else if (position == fastSelectionRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramFastSelection), LagramConfig.fastSelection, false);
                } else if (position == showWeekdayInDateRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramShowWeekdayInDate), LagramConfig.showWeekdayInDate, true);
                } else if (position == showForwardTimeRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramShowForwardTime), LagramConfig.showForwardTime, true);
                } else if (position == showPollPreviewRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramShowPollPreview), LagramConfig.showPollPreview, true);
                } else if (position == showStickerPackOwnerRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramShowStickerPackOwner), LagramConfig.showStickerPackOwner, true);
                } else if (position == showForwardsCounterRow) {
                    cell.setTextAndCheck(LocaleController.getString(R.string.LagramShowForwardsCounter), LagramConfig.showForwardsCounter, false);
                }
                break;
            }
            case VIEW_TYPE_INFO: {
                ((TextInfoPrivacyCell) holder.itemView).setText(LocaleController.getString(R.string.LagramMessageFocusInfo));
                break;
            }
        }
    }

    @Override
    protected void onItemClick(View view, int position) {
        if (position == messageFocusStyleRow) {
            String[] options = new String[]{
                    LocaleController.getString(R.string.LagramFocusStyleList),
                    LocaleController.getString(R.string.LagramFocusStyleTiles),
                    LocaleController.getString(R.string.LagramFocusStyleCapsule)
            };
            showSelectorDialog(LocaleController.getString(R.string.LagramMessageFocusStyle), options, LagramConfig.messageFocusStyle, LagramConfig::setMessageFocusStyle);
            return;
        }
        if (position == doubleTapOutgoingRow || position == doubleTapIncomingRow) {
            final boolean isOut = position == doubleTapOutgoingRow;
            String[] options = new String[]{
                    LocaleController.getString(R.string.LagramDoubleTapReaction),
                    LocaleController.getString(R.string.LagramDoubleTapReply),
                    LocaleController.getString(R.string.LagramDoubleTapCopy),
                    LocaleController.getString(R.string.LagramDoubleTapForward),
                    LocaleController.getString(R.string.LagramDoubleTapEdit),
                    LocaleController.getString(R.string.LagramDoubleTapSave),
                    LocaleController.getString(R.string.LagramDoubleTapNone)
            };
            int cur = isOut ? LagramConfig.doubleTapActionOutgoing : LagramConfig.doubleTapActionIncoming;
            String title = LocaleController.getString(isOut ? R.string.LagramDoubleTapOutgoing : R.string.LagramDoubleTapIncoming);
            showSelectorDialog(title, options, cur, which -> {
                if (isOut) {
                    LagramConfig.setDoubleTapActionOutgoing(which);
                } else {
                    LagramConfig.setDoubleTapActionIncoming(which);
                }
            });
            return;
        }
        if (position == deleteEffectRow) {
            String[] options = new String[]{
                    LocaleController.getString(R.string.LagramDeleteEffectDefault),
                    LocaleController.getString(R.string.LagramDeleteEffectThanos),
                    LocaleController.getString(R.string.LagramDeleteEffectShards),
                    LocaleController.getString(R.string.LagramDeleteEffectPortal)
            };
            showSelectorDialog(LocaleController.getString(R.string.LagramDeleteEffect), options, LagramConfig.deleteEffect, LagramConfig::setDeleteEffect);
            return;
        }
        if (position == stickerSizeRow || position == gifSizeRow) {
            final boolean isSticker = position == stickerSizeRow;
            final int[] sizes = new int[]{6, 8, 10, 12, 14, 16, 18, 20};
            String[] labels = new String[]{"6 (Mini)", "8", "10", "12", "14 (Default)", "16", "18", "20 (Max)"};
            int current = isSticker ? LagramConfig.stickerSize : LagramConfig.gifSize;
            int selected = 4;
            for (int i = 0; i < sizes.length; i++) {
                if (sizes[i] == current) {
                    selected = i;
                    break;
                }
            }
            String title = LocaleController.getString(isSticker ? R.string.LagramStickerSize : R.string.LagramGifSize);
            showSelectorDialog(title, labels, selected, which -> {
                if (isSticker) {
                    LagramConfig.setStickerSize(sizes[which]);
                } else {
                    LagramConfig.setGifSize(sizes[which]);
                }
            });
            return;
        }
        if (!(view instanceof TextCheckCell)) {
            return;
        }
        TextCheckCell cell = (TextCheckCell) view;
        if (position == messageFocusEnabledRow) {
            cell.setChecked(LagramConfig.toggleMessageFocusEnabled());
        } else if (position == messageFocusBlurRow) {
            cell.setChecked(LagramConfig.toggleMessageFocusBlur());
        } else if (position == hideKeyboardOnScrollRow) {
            cell.setChecked(LagramConfig.toggleHideKeyboardOnScroll());
        } else if (position == disableNextChannelJumpRow) {
            cell.setChecked(LagramConfig.toggleDisableNextChannelJump());
        } else if (position == forwardWithoutQuoteRow) {
            cell.setChecked(LagramConfig.toggleForwardWithoutQuote());
        } else if (position == showPageUpButtonRow) {
            cell.setChecked(LagramConfig.toggleShowPageUpButton());
        } else if (position == textAppearAnimationRow) {
            cell.setChecked(LagramConfig.toggleTextAppearAnimation());
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
        } else if (position == unlimitedSelectionRow) {
            cell.setChecked(LagramConfig.toggleUnlimitedSelection());
        } else if (position == fastSelectionRow) {
            cell.setChecked(LagramConfig.toggleFastSelection());
        } else if (position == showWeekdayInDateRow) {
            cell.setChecked(LagramConfig.toggleShowWeekdayInDate());
        } else if (position == showForwardTimeRow) {
            cell.setChecked(LagramConfig.toggleShowForwardTime());
        } else if (position == showPollPreviewRow) {
            cell.setChecked(LagramConfig.toggleShowPollPreview());
        } else if (position == showStickerPackOwnerRow) {
            cell.setChecked(LagramConfig.toggleShowStickerPackOwner());
        } else if (position == showForwardsCounterRow) {
            cell.setChecked(LagramConfig.toggleShowForwardsCounter());
        }
    }
}
