package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class LagramChatsActivity extends UniversalFragment {

    private static final int ID_PREVIEW_CHAT_FOCUS = 1;
    private static final int ID_FOCUS_ENABLED = 2;
    private static final int ID_FOCUS_BLUR = 3;
    private static final int ID_FOCUS_STYLE = 4;

    private static final int ID_STICKER_SIZE = 10;
    private static final int ID_GIF_SIZE = 11;
    private static final int ID_HIDE_SHARE_CHANNELS = 12;
    private static final int ID_UNLIMITED_SELECTION = 13;
    private static final int ID_FAST_SELECTION = 14;

    private static final int ID_DOUBLE_TAP_OUT = 20;
    private static final int ID_DOUBLE_TAP_IN = 21;
    private static final int ID_DELETE_EFFECT = 22;
    private static final int ID_TEXT_APPEAR_ANIM = 23;
    private static final int ID_HIDE_KEYBOARD_SCROLL = 24;
    private static final int ID_DISABLE_NEXT_CHANNEL = 25;
    private static final int ID_FORWARD_NO_QUOTE = 26;
    private static final int ID_SHOW_PAGE_UP = 27;

    private static final int ID_CONFIRM_CALL = 30;
    private static final int ID_CONFIRM_STICKER = 31;
    private static final int ID_DISABLE_VOICE_VIDEO_TOGGLE = 32;
    private static final int ID_DISABLE_PROXIMITY = 33;

    private static final int ID_SHOW_WEEKDAY = 40;
    private static final int ID_SHOW_FORWARD_TIME = 41;
    private static final int ID_SHOW_POLL_PREVIEW = 42;
    private static final int ID_SHOW_STICKER_OWNER = 43;
    private static final int ID_SHOW_FORWARDS_COUNTER = 44;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryChats);
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

    private void refreshLivePreviews() {
        if (listView == null) {
            return;
        }
        for (int i = 0, count = listView.getChildCount(); i < count; i++) {
            View child = listView.getChildAt(i);
            if (child instanceof LagramSettingsComponents.ChatAppearancePreviewCell) {
                child.invalidate();
            }
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

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // Section 1: Opexgram Message Focus & Media Size Live Preview
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMessageFocusHeader)));
        items.add(LagramSettingsComponents.ChatAppearancePreviewCell.Factory.of(
                ID_PREVIEW_CHAT_FOCUS, LagramSettingsComponents.ChatAppearancePreviewCell.MODE_STICKER_AND_FOCUS));
        items.add(UItem.asCheck(ID_FOCUS_ENABLED, LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramMessageFocus)))
                .setChecked(LagramConfig.messageFocusEnabled));
        items.add(UItem.asCheck(ID_FOCUS_BLUR, LocaleController.getString(R.string.LagramMessageFocusBlur))
                .setChecked(LagramConfig.messageFocusBlur));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                ID_FOCUS_STYLE,
                LocaleController.getString(R.string.LagramMessageFocusStyle),
                new String[]{
                        LocaleController.getString(R.string.LagramFocusStyleList),
                        LocaleController.getString(R.string.LagramFocusStyleTiles),
                        LocaleController.getString(R.string.LagramFocusStyleCapsule)
                },
                LagramConfig.messageFocusStyle,
                which -> {
                    LagramConfig.setMessageFocusStyle(which);
                    refreshLivePreviews();
                }
        ));
        items.add(UItem.asShadow(LocaleController.getString(R.string.LagramMessageFocusInfo)));

        // Section 2: Inline Sticker & GIF Size Sliders + Layout
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatLayoutHeader)));
        items.add(LagramSettingsComponents.InlineSliderCell.Factory.of(
                ID_STICKER_SIZE,
                LocaleController.getString(R.string.LagramStickerSize),
                6, 20, LagramConfig.stickerSize, 14, "",
                val -> {
                    LagramConfig.setStickerSize(val);
                    refreshLivePreviews();
                }
        ));
        items.add(LagramSettingsComponents.InlineSliderCell.Factory.of(
                ID_GIF_SIZE,
                LocaleController.getString(R.string.LagramGifSize),
                6, 20, LagramConfig.gifSize, 14, "",
                val -> {
                    LagramConfig.setGifSize(val);
                    refreshLivePreviews();
                }
        ));
        items.add(UItem.asCheck(ID_HIDE_SHARE_CHANNELS, LocaleController.getString(R.string.LagramHideShareInChannels))
                .setChecked(LagramConfig.hideShareButtonInChannels));
        items.add(UItem.asCheck(ID_UNLIMITED_SELECTION, LocaleController.getString(R.string.LagramUnlimitedSelection))
                .setChecked(LagramConfig.unlimitedSelection));
        items.add(UItem.asCheck(ID_FAST_SELECTION, LocaleController.getString(R.string.LagramFastSelection))
                .setChecked(LagramConfig.fastSelection));
        items.add(UItem.asShadow(null));

        // Section 3: Gestures & Animations
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatGesturesHeader)));
        items.add(UItem.asButton(ID_DOUBLE_TAP_OUT, LocaleController.getString(R.string.LagramDoubleTapOutgoing),
                getDoubleTapLabel(LagramConfig.doubleTapActionOutgoing)));
        items.add(UItem.asButton(ID_DOUBLE_TAP_IN, LocaleController.getString(R.string.LagramDoubleTapIncoming),
                getDoubleTapLabel(LagramConfig.doubleTapActionIncoming)));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                ID_DELETE_EFFECT,
                LocaleController.getString(R.string.LagramDeleteEffect),
                new String[]{
                        LocaleController.getString(R.string.LagramDeleteEffectDefault),
                        LocaleController.getString(R.string.LagramDeleteEffectThanos),
                        LocaleController.getString(R.string.LagramDeleteEffectShards),
                        LocaleController.getString(R.string.LagramDeleteEffectPortal)
                },
                LagramConfig.deleteEffect,
                LagramConfig::setDeleteEffect
        ));
        items.add(UItem.asCheck(ID_TEXT_APPEAR_ANIM, LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramTextAppearAnimation)))
                .setChecked(LagramConfig.textAppearAnimation));
        items.add(UItem.asCheck(ID_HIDE_KEYBOARD_SCROLL, LocaleController.getString(R.string.LagramHideKeyboardOnScroll))
                .setChecked(LagramConfig.hideKeyboardOnScroll));
        items.add(UItem.asCheck(ID_DISABLE_NEXT_CHANNEL, LocaleController.getString(R.string.LagramDisableNextChannelJump))
                .setChecked(LagramConfig.disableNextChannelJump));
        items.add(UItem.asCheck(ID_FORWARD_NO_QUOTE, LocaleController.getString(R.string.LagramForwardWithoutQuote))
                .setChecked(LagramConfig.forwardWithoutQuote));
        items.add(UItem.asCheck(ID_SHOW_PAGE_UP, LocaleController.getString(R.string.LagramShowPageUpButton))
                .setChecked(LagramConfig.showPageUpButton));
        items.add(UItem.asShadow(null));

        // Section 4: Confirmations & Voice/Video
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatConfirmHeader)));
        items.add(UItem.asCheck(ID_CONFIRM_CALL, LocaleController.getString(R.string.LagramConfirmBeforeCall))
                .setChecked(LagramConfig.confirmBeforeCall));
        items.add(UItem.asCheck(ID_CONFIRM_STICKER, LocaleController.getString(R.string.LagramConfirmBeforeSendSticker))
                .setChecked(LagramConfig.confirmBeforeSendSticker));
        items.add(UItem.asCheck(ID_DISABLE_VOICE_VIDEO_TOGGLE, LocaleController.getString(R.string.LagramDisableVoiceVideoToggle))
                .setChecked(LagramConfig.disableVoiceVideoToggle));
        items.add(UItem.asCheck(ID_DISABLE_PROXIMITY, LocaleController.getString(R.string.LagramDisableProximitySensor))
                .setChecked(LagramConfig.disableProximitySensor));
        items.add(UItem.asShadow(null));

        // Section 5: Message Details
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatDetailsHeader)));
        items.add(UItem.asCheck(ID_SHOW_WEEKDAY, LocaleController.getString(R.string.LagramShowWeekdayInDate))
                .setChecked(LagramConfig.showWeekdayInDate));
        items.add(UItem.asCheck(ID_SHOW_FORWARD_TIME, LocaleController.getString(R.string.LagramShowForwardTime))
                .setChecked(LagramConfig.showForwardTime));
        items.add(UItem.asCheck(ID_SHOW_POLL_PREVIEW, LocaleController.getString(R.string.LagramShowPollPreview))
                .setChecked(LagramConfig.showPollPreview));
        items.add(UItem.asCheck(ID_SHOW_STICKER_OWNER, LocaleController.getString(R.string.LagramShowStickerPackOwner))
                .setChecked(LagramConfig.showStickerPackOwner));
        items.add(UItem.asCheck(ID_SHOW_FORWARDS_COUNTER, LocaleController.getString(R.string.LagramShowForwardsCounter))
                .setChecked(LagramConfig.showForwardsCounter));
        items.add(UItem.asShadow(null));
    }

    private void showDoubleTapDialog(boolean isOutgoing) {
        if (getParentActivity() == null) {
            return;
        }
        String[] options = new String[]{
                LocaleController.getString(R.string.LagramDoubleTapReaction),
                LocaleController.getString(R.string.LagramDoubleTapReply),
                LocaleController.getString(R.string.LagramDoubleTapCopy),
                LocaleController.getString(R.string.LagramDoubleTapForward),
                LocaleController.getString(R.string.LagramDoubleTapEdit),
                LocaleController.getString(R.string.LagramDoubleTapSave),
                LocaleController.getString(R.string.LagramDoubleTapNone)
        };
        int cur = isOutgoing ? LagramConfig.doubleTapActionOutgoing : LagramConfig.doubleTapActionIncoming;
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString(isOutgoing ? R.string.LagramDoubleTapOutgoing : R.string.LagramDoubleTapIncoming));
        builder.setSingleChoiceItems(options, cur, (dialog, which) -> {
            if (isOutgoing) {
                LagramConfig.setDoubleTapActionOutgoing(which);
            } else {
                LagramConfig.setDoubleTapActionIncoming(which);
            }
            dialog.dismiss();
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ID_DOUBLE_TAP_OUT) {
            showDoubleTapDialog(true);
            return;
        }
        if (item.id == ID_DOUBLE_TAP_IN) {
            showDoubleTapDialog(false);
            return;
        }
        boolean checkChanged = false;
        boolean newVal = false;
        switch (item.id) {
            case ID_FOCUS_ENABLED:
                newVal = LagramConfig.toggleMessageFocusEnabled();
                checkChanged = true;
                refreshLivePreviews();
                break;
            case ID_FOCUS_BLUR:
                newVal = LagramConfig.toggleMessageFocusBlur();
                checkChanged = true;
                refreshLivePreviews();
                break;
            case ID_HIDE_SHARE_CHANNELS:
                newVal = LagramConfig.toggleHideShareButtonInChannels();
                checkChanged = true;
                break;
            case ID_UNLIMITED_SELECTION:
                newVal = LagramConfig.toggleUnlimitedSelection();
                checkChanged = true;
                break;
            case ID_FAST_SELECTION:
                newVal = LagramConfig.toggleFastSelection();
                checkChanged = true;
                break;
            case ID_TEXT_APPEAR_ANIM:
                newVal = LagramConfig.toggleTextAppearAnimation();
                checkChanged = true;
                break;
            case ID_HIDE_KEYBOARD_SCROLL:
                newVal = LagramConfig.toggleHideKeyboardOnScroll();
                checkChanged = true;
                break;
            case ID_DISABLE_NEXT_CHANNEL:
                newVal = LagramConfig.toggleDisableNextChannelJump();
                checkChanged = true;
                break;
            case ID_FORWARD_NO_QUOTE:
                newVal = LagramConfig.toggleForwardWithoutQuote();
                checkChanged = true;
                break;
            case ID_SHOW_PAGE_UP:
                newVal = LagramConfig.toggleShowPageUpButton();
                checkChanged = true;
                break;
            case ID_CONFIRM_CALL:
                newVal = LagramConfig.toggleConfirmBeforeCall();
                checkChanged = true;
                break;
            case ID_CONFIRM_STICKER:
                newVal = LagramConfig.toggleConfirmBeforeSendSticker();
                checkChanged = true;
                break;
            case ID_DISABLE_VOICE_VIDEO_TOGGLE:
                newVal = LagramConfig.toggleDisableVoiceVideoToggle();
                checkChanged = true;
                break;
            case ID_DISABLE_PROXIMITY:
                newVal = LagramConfig.toggleDisableProximitySensor();
                checkChanged = true;
                break;
            case ID_SHOW_WEEKDAY:
                newVal = LagramConfig.toggleShowWeekdayInDate();
                checkChanged = true;
                break;
            case ID_SHOW_FORWARD_TIME:
                newVal = LagramConfig.toggleShowForwardTime();
                checkChanged = true;
                break;
            case ID_SHOW_POLL_PREVIEW:
                newVal = LagramConfig.toggleShowPollPreview();
                checkChanged = true;
                break;
            case ID_SHOW_STICKER_OWNER:
                newVal = LagramConfig.toggleShowStickerPackOwner();
                checkChanged = true;
                break;
            case ID_SHOW_FORWARDS_COUNTER:
                newVal = LagramConfig.toggleShowForwardsCounter();
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
