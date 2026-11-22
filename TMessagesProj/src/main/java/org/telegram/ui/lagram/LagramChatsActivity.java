package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class LagramChatsActivity extends UniversalFragment {

    public static final int SUB_HOME = 0;
    public static final int SUB_FOCUS_AND_REACTIONS = 1;
    public static final int SUB_GESTURES = 2;
    public static final int SUB_DETAILS_AND_MEDIA = 3;
    public static final int SUB_SAFETY = 4;

    private final int subPage;

    private static final int ID_NAV_FOCUS = 100;
    private static final int ID_NAV_GESTURES = 101;
    private static final int ID_NAV_DETAILS = 102;
    private static final int ID_NAV_SAFETY = 103;

    private static final int ID_PREVIEW_CHAT = 1;
    private static final int ID_MESSAGE_FOCUS = 2;
    private static final int ID_MESSAGE_FOCUS_BLUR = 3;
    private static final int ID_MESSAGE_FOCUS_STYLE = 4;
    private static final int ID_BIG_REACTIONS = 5;
    private static final int ID_DELETE_EFFECT = 6;

    private static final int ID_DOUBLE_TAP_OUT = 20;
    private static final int ID_DOUBLE_TAP_IN = 21;
    private static final int ID_HIDE_KEYBOARD = 22;
    private static final int ID_PAGE_UP_BUTTON = 23;
    private static final int ID_DISABLE_NEXT_CHANNEL = 24;
    private static final int ID_UNLIMITED_SELECTION = 25;

    private static final int ID_STICKER_SIZE = 30;
    private static final int ID_GIF_SIZE = 31;
    private static final int ID_SHOW_WEEKDAY = 32;
    private static final int ID_SHOW_SECONDS = 33;
    private static final int ID_SHOW_FORWARD_TIME = 34;
    private static final int ID_SHOW_FORWARDS_COUNTER = 35;
    private static final int ID_STICKER_PACK_OWNER = 36;

    private static final int ID_CONFIRM_CALL = 40;
    private static final int ID_CONFIRM_STICKER = 41;
    private static final int ID_FORWARD_NO_QUOTE = 42;
    private static final int ID_HIDE_CHANNEL_SHARE = 43;
    private static final int ID_DISABLE_VOICE_VIDEO_TOGGLE = 44;
    private static final int ID_DISABLE_PROXIMITY = 45;

    public LagramChatsActivity() {
        this(SUB_HOME);
    }

    public LagramChatsActivity(int subPage) {
        super();
        this.subPage = subPage;
    }

    @Override
    protected CharSequence getTitle() {
        switch (subPage) {
            case SUB_FOCUS_AND_REACTIONS:
                return LocaleController.getString(R.string.LagramMessageFocusHeader);
            case SUB_GESTURES:
                return LocaleController.getString(R.string.LagramChatGesturesHeader);
            case SUB_DETAILS_AND_MEDIA:
                return LocaleController.getString(R.string.LagramChatDetailsHeader);
            case SUB_SAFETY:
                return LocaleController.getString(R.string.LagramChatConfirmHeader);
            case SUB_HOME:
            default:
                return LocaleController.getString(R.string.LagramCategoryChats);
        }
    }

    @Override
    public View createView(Context context) {
        LagramConfig.ensureLoaded();
        View view = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        updateBackdropColor();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    private void updateBackdropColor() {
        if (fragmentView == null || listView == null) {
            return;
        }
        if (LagramConfig.amoledSettingsBackdrop && Theme.isCurrentThemeDark()) {
            fragmentView.setBackgroundColor(0xFF000000);
            listView.setBackgroundColor(0xFF000000);
        } else {
            int bg = Theme.getColor(Theme.key_windowBackgroundGray);
            fragmentView.setBackgroundColor(bg);
            listView.setBackgroundColor(bg);
        }
    }

    private void refreshList() {
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();
        switch (subPage) {
            case SUB_FOCUS_AND_REACTIONS:
                fillFocusAndReactions(items);
                break;
            case SUB_GESTURES:
                fillGestures(items);
                break;
            case SUB_DETAILS_AND_MEDIA:
                fillDetailsAndMedia(items);
                break;
            case SUB_SAFETY:
                fillSafety(items);
                break;
            case SUB_HOME:
            default:
                fillHomeHub(items);
                break;
        }
    }

    private void fillHomeHub(ArrayList<UItem> items) {
        // Standalone Real Chat Preview Card
        items.add(LagramSettingsComponents.RealChatMessagesPreviewCell.Factory.of(
                ID_PREVIEW_CHAT,
                LagramSettingsComponents.RealChatMessagesPreviewCell.MODE_STICKER_AND_FOCUS));
        items.add(UItem.asShadow(-1, null));

        // Categorized Sub-Fragments Card
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramCategoryChats)));
        items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                ID_NAV_FOCUS,
                IconBackgroundColors.PURPLE,
                R.drawable.msg_msgbubble3,
                LocaleController.getString(R.string.LagramMessageFocusHeader),
                LocaleController.getString(R.string.LagramMessageFocus),
                getFocusStyleShort()));

        items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                ID_NAV_GESTURES,
                IconBackgroundColors.BLUE,
                R.drawable.msg_reactions2,
                LocaleController.getString(R.string.LagramChatGesturesHeader),
                LocaleController.getString(R.string.LagramDoubleTapOutgoing),
                getDoubleTapLabel(LagramConfig.doubleTapActionOutgoing)));

        items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                ID_NAV_DETAILS,
                IconBackgroundColors.GREEN,
                R.drawable.msg_sticker,
                LocaleController.getString(R.string.LagramChatDetailsHeader),
                LocaleController.getString(R.string.LagramStickerSize) + " • " + LocaleController.getString(R.string.LagramGifSize),
                LagramConfig.stickerSize + " / " + LagramConfig.gifSize));

        items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                ID_NAV_SAFETY,
                IconBackgroundColors.ORANGE,
                R.drawable.msg_secret,
                LocaleController.getString(R.string.LagramChatConfirmHeader),
                LocaleController.getString(R.string.LagramConfirmBeforeCall),
                LagramConfig.confirmBeforeCall ? "ON" : "OFF"));
        items.add(UItem.asShadow(-2, null));

        // Quick Toggles Card
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMessageFocusHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_MESSAGE_FOCUS,
                R.drawable.msg_msgbubble3,
                LocaleController.getString(R.string.LagramMessageFocus),
                LocaleController.getString(R.string.LagramMessageFocusInfo),
                LagramConfig.messageFocusEnabled));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_BIG_REACTIONS,
                R.drawable.msg_reactions2,
                LocaleController.getString(R.string.LagramBigReactions),
                null,
                LagramConfig.bigReactions));
        items.add(UItem.asShadow(-3, null));
    }

    private void fillFocusAndReactions(ArrayList<UItem> items) {
        items.add(LagramSettingsComponents.RealChatMessagesPreviewCell.Factory.of(
                ID_PREVIEW_CHAT,
                LagramSettingsComponents.RealChatMessagesPreviewCell.MODE_STICKER_AND_FOCUS));
        items.add(UItem.asShadow(-10, null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMessageFocusHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_MESSAGE_FOCUS,
                R.drawable.msg_msgbubble3,
                LocaleController.getString(R.string.LagramMessageFocus),
                LocaleController.getString(R.string.LagramMessageFocusInfo),
                LagramConfig.messageFocusEnabled));
        if (LagramConfig.messageFocusEnabled) {
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_MESSAGE_FOCUS_BLUR,
                    R.drawable.msg_photo_blur,
                    LocaleController.getString(R.string.LagramMessageFocusBlur),
                    null,
                    LagramConfig.messageFocusBlur));
            items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                    ID_MESSAGE_FOCUS_STYLE,
                    LocaleController.getString(R.string.LagramMessageFocusStyle),
                    new String[]{
                            LocaleController.getString(R.string.LagramFocusStyleList),
                            LocaleController.getString(R.string.LagramFocusStyleTiles),
                            LocaleController.getString(R.string.LagramFocusStyleCapsule)
                    },
                    LagramConfig.messageFocusStyle,
                    idx -> {
                        LagramConfig.setMessageFocusStyle(idx);
                        refreshList();
                    }));
        }
        items.add(UItem.asShadow(-11, null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramDeleteEffect)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_BIG_REACTIONS,
                R.drawable.msg_reactions2,
                LocaleController.getString(R.string.LagramBigReactions),
                null,
                LagramConfig.bigReactions));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                ID_DELETE_EFFECT,
                LocaleController.getString(R.string.LagramDeleteEffect),
                new String[]{
                        LocaleController.getString(R.string.LagramDeleteEffectDefault),
                        LocaleController.getString(R.string.LagramDeleteEffectThanos)
                },
                LagramConfig.deleteEffect == LagramConfig.DELETE_EFFECT_DEFAULT ? 0 : 1,
                idx -> {
                    LagramConfig.setDeleteEffect(idx == 0 ? LagramConfig.DELETE_EFFECT_DEFAULT : LagramConfig.DELETE_EFFECT_THANOS);
                    refreshList();
                }));
        items.add(UItem.asShadow(-12, null));
    }

    private void fillGestures(ArrayList<UItem> items) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatGesturesHeader)));
        items.add(UItem.asButton(ID_DOUBLE_TAP_OUT,
                LocaleController.getString(R.string.LagramDoubleTapOutgoing),
                getDoubleTapLabel(LagramConfig.doubleTapActionOutgoing)));
        items.add(UItem.asButton(ID_DOUBLE_TAP_IN,
                LocaleController.getString(R.string.LagramDoubleTapIncoming),
                getDoubleTapLabel(LagramConfig.doubleTapActionIncoming)));
        items.add(UItem.asShadow(-20, null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramCategoryChats)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_HIDE_KEYBOARD,
                R.drawable.input_keyboard,
                LocaleController.getString(R.string.LagramHideKeyboardOnScroll),
                null,
                LagramConfig.hideKeyboardOnScroll));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_PAGE_UP_BUTTON,
                R.drawable.msg_go_up,
                LocaleController.getString(R.string.LagramShowPageUpButton),
                null,
                LagramConfig.showPageUpButton));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_DISABLE_NEXT_CHANNEL,
                R.drawable.msg_channel,
                LocaleController.getString(R.string.LagramDisableNextChannelJump),
                null,
                LagramConfig.disableNextChannelJump));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_UNLIMITED_SELECTION,
                R.drawable.msg_select,
                LocaleController.getString(R.string.LagramUnlimitedSelection),
                null,
                LagramConfig.unlimitedSelection));
        items.add(UItem.asShadow(-21, null));
    }

    private void fillDetailsAndMedia(ArrayList<UItem> items) {
        items.add(LagramSettingsComponents.RealChatMessagesPreviewCell.Factory.of(
                ID_PREVIEW_CHAT,
                LagramSettingsComponents.RealChatMessagesPreviewCell.MODE_STICKER_AND_FOCUS));
        items.add(UItem.asShadow(-30, null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramStickerSize)));
        items.add(LagramSettingsComponents.InlineSliderCell.Factory.of(
                ID_STICKER_SIZE,
                LocaleController.getString(R.string.LagramStickerSize),
                4, 20, 14, LagramConfig.stickerSize, "",
                val -> {
                    LagramConfig.setStickerSize(val);
                    refreshList();
                }));
        items.add(LagramSettingsComponents.InlineSliderCell.Factory.of(
                ID_GIF_SIZE,
                LocaleController.getString(R.string.LagramGifSize),
                4, 20, 14, LagramConfig.gifSize, "",
                val -> {
                    LagramConfig.setGifSize(val);
                    refreshList();
                }));
        items.add(UItem.asShadow(-31, null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatDetailsHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SHOW_WEEKDAY,
                R.drawable.msg_calendar2,
                LocaleController.getString(R.string.LagramShowWeekdayInDate),
                null,
                LagramConfig.showWeekdayInDate));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SHOW_SECONDS,
                R.drawable.msg_recent,
                LocaleController.getString(R.string.LagramShowSeconds),
                null,
                LagramConfig.showSecondsInTime));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SHOW_FORWARD_TIME,
                R.drawable.msg_forward,
                LocaleController.getString(R.string.LagramShowForwardTime),
                null,
                LagramConfig.showForwardTime));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SHOW_FORWARDS_COUNTER,
                R.drawable.msg_forward,
                LocaleController.getString(R.string.LagramShowForwardsCounter),
                null,
                LagramConfig.showForwardsCounter));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_STICKER_PACK_OWNER,
                R.drawable.msg_sticker,
                LocaleController.getString(R.string.LagramShowStickerPackOwner),
                null,
                LagramConfig.showStickerPackOwner));
        items.add(UItem.asShadow(-32, null));
    }

    private void fillSafety(ArrayList<UItem> items) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatConfirmHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_CONFIRM_CALL,
                R.drawable.msg_calls,
                LocaleController.getString(R.string.LagramConfirmBeforeCall),
                null,
                LagramConfig.confirmBeforeCall));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_CONFIRM_STICKER,
                R.drawable.msg_sticker,
                LocaleController.getString(R.string.LagramConfirmBeforeSendSticker),
                null,
                LagramConfig.confirmBeforeSendSticker));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_FORWARD_NO_QUOTE,
                R.drawable.msg_forward,
                LocaleController.getString(R.string.LagramForwardWithoutQuote),
                null,
                LagramConfig.forwardWithoutQuote));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_HIDE_CHANNEL_SHARE,
                R.drawable.msg_shareout,
                LocaleController.getString(R.string.LagramHideShareInChannels),
                null,
                LagramConfig.hideShareButtonInChannels));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_DISABLE_VOICE_VIDEO_TOGGLE,
                R.drawable.msg_voicechat,
                LocaleController.getString(R.string.LagramDisableVoiceVideoToggle),
                null,
                LagramConfig.disableVoiceVideoToggle));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_DISABLE_PROXIMITY,
                R.drawable.msg_mute,
                LocaleController.getString(R.string.LagramDisableProximitySensor),
                null,
                LagramConfig.disableProximitySensor));
        items.add(UItem.asShadow(-40, null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_NAV_FOCUS:
                presentFragment(new LagramChatsActivity(SUB_FOCUS_AND_REACTIONS));
                return;
            case ID_NAV_GESTURES:
                presentFragment(new LagramChatsActivity(SUB_GESTURES));
                return;
            case ID_NAV_DETAILS:
                presentFragment(new LagramChatsActivity(SUB_DETAILS_AND_MEDIA));
                return;
            case ID_NAV_SAFETY:
                presentFragment(new LagramChatsActivity(SUB_SAFETY));
                return;
            case ID_MESSAGE_FOCUS:
                LagramConfig.toggleMessageFocusEnabled();
                refreshList();
                return;
            case ID_MESSAGE_FOCUS_BLUR:
                toggleSwitch(view, LagramConfig.toggleMessageFocusBlur());
                return;
            case ID_BIG_REACTIONS:
                toggleSwitch(view, LagramConfig.toggleBigReactions());
                return;
            case ID_DOUBLE_TAP_OUT:
                showDoubleTapDialog(true);
                return;
            case ID_DOUBLE_TAP_IN:
                showDoubleTapDialog(false);
                return;
            case ID_HIDE_KEYBOARD:
                toggleSwitch(view, LagramConfig.toggleHideKeyboardOnScroll());
                return;
            case ID_PAGE_UP_BUTTON:
                toggleSwitch(view, LagramConfig.toggleShowPageUpButton());
                return;
            case ID_DISABLE_NEXT_CHANNEL:
                toggleSwitch(view, LagramConfig.toggleDisableNextChannelJump());
                return;
            case ID_UNLIMITED_SELECTION:
                toggleSwitch(view, LagramConfig.toggleUnlimitedSelection());
                return;
            case ID_SHOW_WEEKDAY:
                toggleSwitch(view, LagramConfig.toggleShowWeekdayInDate());
                return;
            case ID_SHOW_SECONDS:
                toggleSwitch(view, LagramConfig.toggleShowSecondsInTime());
                return;
            case ID_SHOW_FORWARD_TIME:
                toggleSwitch(view, LagramConfig.toggleShowForwardTime());
                return;
            case ID_SHOW_FORWARDS_COUNTER:
                toggleSwitch(view, LagramConfig.toggleShowForwardsCounter());
                return;
            case ID_STICKER_PACK_OWNER:
                toggleSwitch(view, LagramConfig.toggleShowStickerPackOwner());
                return;
            case ID_CONFIRM_CALL:
                toggleSwitch(view, LagramConfig.toggleConfirmBeforeCall());
                return;
            case ID_CONFIRM_STICKER:
                toggleSwitch(view, LagramConfig.toggleConfirmBeforeSendSticker());
                return;
            case ID_FORWARD_NO_QUOTE:
                toggleSwitch(view, LagramConfig.toggleForwardWithoutQuote());
                return;
            case ID_HIDE_CHANNEL_SHARE:
                toggleSwitch(view, LagramConfig.toggleHideShareButtonInChannels());
                return;
            case ID_DISABLE_VOICE_VIDEO_TOGGLE:
                toggleSwitch(view, LagramConfig.toggleDisableVoiceVideoToggle());
                return;
            case ID_DISABLE_PROXIMITY:
                toggleSwitch(view, LagramConfig.toggleDisableProximitySensor());
                return;
        }
    }

    private void showDoubleTapDialog(boolean outgoing) {
        if (getParentActivity() == null) return;
        String[] labels = new String[]{
                LocaleController.getString(R.string.LagramDoubleTapReaction),
                LocaleController.getString(R.string.LagramDoubleTapReply),
                LocaleController.getString(R.string.LagramDoubleTapCopy),
                LocaleController.getString(R.string.LagramDoubleTapForward),
                LocaleController.getString(R.string.LagramDoubleTapEdit),
                LocaleController.getString(R.string.LagramDoubleTapSave),
                LocaleController.getString(R.string.LagramDoubleTapNone)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
        builder.setTitle(LocaleController.getString(outgoing ? R.string.LagramDoubleTapOutgoing : R.string.LagramDoubleTapIncoming));
        builder.setItems(labels, (d, which) -> {
            if (outgoing) {
                LagramConfig.setDoubleTapActionOutgoing(which);
            } else {
                LagramConfig.setDoubleTapActionIncoming(which);
            }
            refreshList();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void toggleSwitch(View view, boolean checked) {
        if (view instanceof LagramSettingsComponents.IconSwitchCell) {
            ((LagramSettingsComponents.IconSwitchCell) view).setChecked(checked, true);
        }
        refreshList();
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private String getFocusStyleShort() {
        if (!LagramConfig.messageFocusEnabled) {
            return LocaleController.getString(R.string.LagramHapticsOff);
        }
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
            case LagramConfig.DOUBLE_TAP_REPLY: return LocaleController.getString(R.string.LagramDoubleTapReply);
            case LagramConfig.DOUBLE_TAP_COPY: return LocaleController.getString(R.string.LagramDoubleTapCopy);
            case LagramConfig.DOUBLE_TAP_FORWARD: return LocaleController.getString(R.string.LagramDoubleTapForward);
            case LagramConfig.DOUBLE_TAP_EDIT: return LocaleController.getString(R.string.LagramDoubleTapEdit);
            case LagramConfig.DOUBLE_TAP_SAVE: return LocaleController.getString(R.string.LagramDoubleTapSave);
            case LagramConfig.DOUBLE_TAP_NONE: return LocaleController.getString(R.string.LagramDoubleTapNone);
            case LagramConfig.DOUBLE_TAP_REACTION:
            default: return LocaleController.getString(R.string.LagramDoubleTapReaction);
        }
    }
}
