package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class LagramChatsActivity extends UniversalFragment {

    public static final int SUB_HOME = 0;
    public static final int SUB_MESSAGE_MENU = 1;
    public static final int SUB_TEXT_ANIMATION = 2;
    public static final int SUB_SAFETY = 3;

    private final int subPage;

    // Home top navigation rows (Exact Opexgram Chats top card group)
    private static final int ID_NAV_MESSAGE_MENU = 100;
    private static final int ID_NAV_TEXT_ANIM = 101;
    private static final int ID_DELETE_EFFECT = 102;
    private static final int ID_NAV_SAFETY = 103;

    // Sticker & GIF size
    private static final int ID_PREVIEW_CHAT = 1;
    private static final int ID_STICKER_SIZE = 2;
    private static final int ID_GIF_SIZE = 3;

    // Header & message field
    private static final int ID_CHAT_HEADER_EXPRESSIVE = 10;
    private static final int ID_CHAT_BOX_PICKER = 11;
    private static final int ID_CHAT_BOX_CONTRAST = 12;

    // Gestures
    private static final int ID_HIDE_KEYBOARD = 20;
    private static final int ID_DOUBLE_TAP_OUT = 21;
    private static final int ID_DOUBLE_TAP_IN = 22;
    private static final int ID_PAGE_UP_BUTTON = 23;
    private static final int ID_BIG_REACTIONS = 24;
    private static final int ID_DISABLE_NEXT_CHANNEL = 25;

    // Message selection
    private static final int ID_UNLIMITED_SELECTION = 30;
    private static final int ID_FAST_SELECTION = 31;
    private static final int ID_SELECTION_SPEED = 32;

    // Message details
    private static final int ID_SHOW_WEEKDAY = 40;
    private static final int ID_SHOW_SECONDS = 41;
    private static final int ID_SHOW_FORWARD_TIME = 42;
    private static final int ID_STICKER_PACK_OWNER = 43;
    private static final int ID_SHOW_FORWARDS_COUNTER = 44;

    // Sub: Message Menu
    private static final int ID_MENU_PREVIEW = 50;
    private static final int ID_MESSAGE_FOCUS = 51;
    private static final int ID_MESSAGE_FOCUS_BLUR = 52;
    private static final int ID_MESSAGE_FOCUS_STYLE = 53;

    // Sub: Text Animation
    private static final int ID_TEXT_ANIM_PREVIEW = 60;
    private static final int ID_TEXT_ANIM_ENABLE = 61;
    private static final int ID_TEXT_ANIM_BLUR = 62;
    private static final int ID_TEXT_ANIM_SLIDE = 63;
    private static final int ID_TEXT_ANIM_SCALE = 64;
    private static final int ID_TEXT_ANIM_ROTATE = 65;

    // Sub: Confirmations & Safety
    private static final int ID_CONFIRM_CALL = 70;
    private static final int ID_CONFIRM_STICKER = 71;
    private static final int ID_FORWARD_NO_QUOTE = 72;
    private static final int ID_HIDE_CHANNEL_SHARE = 73;
    private static final int ID_DISABLE_VOICE_VIDEO_TOGGLE = 74;
    private static final int ID_DISABLE_PROXIMITY = 75;

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
            case SUB_MESSAGE_MENU:
                return LocaleController.getString(R.string.LagramMessageFocusHeader);
            case SUB_TEXT_ANIMATION:
                return LocaleController.getString(R.string.LagramTextAnimHeader);
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
        refreshList();
    }

    private void updateBackdropColor() {
        if (fragmentView == null || listView == null) return;
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
            case SUB_MESSAGE_MENU:
                fillMessageMenu(items);
                break;
            case SUB_TEXT_ANIMATION:
                fillTextAnimation(items);
                break;
            case SUB_SAFETY:
                fillSafety(items);
                break;
            case SUB_HOME:
            default:
                fillHome(items);
                break;
        }
    }

    private void fillHome(ArrayList<UItem> items) {
        // =========================================================================
        // 1. Top Navigation Sub-Screens (Exact Opexgram Chats top list)
        // =========================================================================
        items.add(UItem.asButton(ID_NAV_MESSAGE_MENU, R.drawable.msg_list,
                LocaleController.getString(R.string.LagramMessageFocusHeader),
                getFocusStyleShort()));
        items.add(UItem.asButton(ID_NAV_TEXT_ANIM, R.drawable.msg_edit,
                LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramTextAnimHeader)),
                LagramConfig.textAppearAnimation ? "ON" : "OFF"));
        items.add(UItem.asButton(ID_DELETE_EFFECT, R.drawable.msg_delete,
                LocaleController.getString(R.string.LagramDeleteEffect),
                getDeleteEffectLabel()));
        items.add(UItem.asButton(ID_NAV_SAFETY, R.drawable.msg_secret,
                LocaleController.getString(R.string.LagramChatConfirmHeader),
                LagramConfig.confirmBeforeCall ? "ON" : "OFF"));
        items.add(UItem.asShadow(-1, null));

        // =========================================================================
        // 2. Sticker and GIF size (Real Chat Preview + Opexgram M3 Pill Sliders)
        // =========================================================================
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramStickerSize) + " / " + LocaleController.getString(R.string.LagramGifSize)));
        items.add(LagramSettingsComponents.RealChatMessagesPreviewCell.Factory.of(
                ID_PREVIEW_CHAT,
                LagramSettingsComponents.RealChatMessagesPreviewCell.MODE_STICKER_AND_FOCUS));
        items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                ID_STICKER_SIZE,
                LocaleController.getString(R.string.LagramStickerSize),
                4, 20, 14, LagramConfig.stickerSize, "",
                val -> {
                    LagramConfig.setStickerSize(val);
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                ID_GIF_SIZE,
                LocaleController.getString(R.string.LagramGifSize),
                4, 20, 14, LagramConfig.gifSize, "",
                val -> {
                    LagramConfig.setGifSize(val);
                    refreshList();
                }));
        items.add(UItem.asShadow(-2, null));

        // =========================================================================
        // 3. Header and message field (Expressive Header + 3-Card Chat Box Picker + Contrast)
        // =========================================================================
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatBoxHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_CHAT_HEADER_EXPRESSIVE, 0,
                LocaleController.getString(R.string.LagramChatHeaderExpressive),
                LocaleController.getString(R.string.LagramChatHeaderExpressiveInfo),
                LagramConfig.chatHeaderExpressive));
        items.add(OpexgramStyleWidgets.OpexVisualPickerCell.Factory.of(
                ID_CHAT_BOX_PICKER,
                OpexgramStyleWidgets.OpexVisualPickerCell.MODE_CHAT_BOX,
                new String[]{
                        LocaleController.getString(R.string.LagramChatBoxTelegram),
                        LocaleController.getString(R.string.LagramChatBoxExpressive),
                        LocaleController.getString(R.string.LagramChatBoxButtons)
                },
                LagramConfig.chatBoxStyle,
                idx -> {
                    LagramConfig.setChatBoxStyle(idx);
                    refreshList();
                }));
        if (LagramConfig.chatBoxStyle != LagramConfig.CHAT_BOX_TELEGRAM) {
            items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                    ID_CHAT_BOX_CONTRAST,
                    LocaleController.getString(R.string.LagramChatBoxContrast),
                    5, 100, 55, LagramConfig.chatBoxContrast, "%",
                    val -> {
                        LagramConfig.setChatBoxContrast(val);
                        refreshList();
                    }));
        }
        items.add(UItem.asShadow(-3, LocaleController.getString(R.string.LagramChatBoxInfo)));

        // =========================================================================
        // 4. Gestures
        // =========================================================================
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatGesturesHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_KEYBOARD, 0,
                LocaleController.getString(R.string.LagramHideKeyboardOnScroll),
                null,
                LagramConfig.hideKeyboardOnScroll));
        items.add(UItem.asButton(ID_DOUBLE_TAP_OUT,
                LocaleController.getString(R.string.LagramDoubleTapOutgoing),
                getDoubleTapLabel(LagramConfig.doubleTapActionOutgoing)));
        items.add(UItem.asButton(ID_DOUBLE_TAP_IN,
                LocaleController.getString(R.string.LagramDoubleTapIncoming),
                getDoubleTapLabel(LagramConfig.doubleTapActionIncoming)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_PAGE_UP_BUTTON, 0,
                LocaleController.getString(R.string.LagramShowPageUpButton),
                null,
                LagramConfig.showPageUpButton));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_BIG_REACTIONS, 0,
                LocaleController.getString(R.string.LagramBigReactions),
                null,
                LagramConfig.bigReactions));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_DISABLE_NEXT_CHANNEL, 0,
                LocaleController.getString(R.string.LagramDisableNextChannelJump),
                null,
                LagramConfig.disableNextChannelJump));
        items.add(UItem.asShadow(-4, null));

        // =========================================================================
        // 5. Message selection
        // =========================================================================
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramSelectionHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_UNLIMITED_SELECTION, 0,
                LocaleController.getString(R.string.LagramUnlimitedSelection),
                null,
                LagramConfig.unlimitedSelection));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_FAST_SELECTION, 0,
                LocaleController.getString(R.string.LagramFastSelection),
                null,
                LagramConfig.fastSelection));
        if (LagramConfig.fastSelection) {
            items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                    ID_SELECTION_SPEED,
                    LocaleController.getString(R.string.LagramSelectionSpeed),
                    2, 8, 5, LagramConfig.selectionScrollSpeed, "×", "",
                    val -> {
                        LagramConfig.setSelectionScrollSpeed(val);
                        refreshList();
                    }));
        }
        items.add(UItem.asShadow(-5, null));

        // =========================================================================
        // 6. Message details
        // =========================================================================
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatDetailsHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_WEEKDAY, 0,
                LocaleController.getString(R.string.LagramShowWeekdayInDate),
                null,
                LagramConfig.showWeekdayInDate));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_SECONDS, 0,
                LocaleController.getString(R.string.LagramShowSeconds),
                null,
                LagramConfig.showSecondsInTime));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_FORWARD_TIME, 0,
                LocaleController.getString(R.string.LagramShowForwardTime),
                null,
                LagramConfig.showForwardTime));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_STICKER_PACK_OWNER, 0,
                LocaleController.getString(R.string.LagramShowStickerPackOwner),
                null,
                LagramConfig.showStickerPackOwner));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_FORWARDS_COUNTER, 0,
                LocaleController.getString(R.string.LagramShowForwardsCounter),
                null,
                LagramConfig.showForwardsCounter));
        items.add(UItem.asShadow(-6, null));
    }

    private void fillMessageMenu(ArrayList<UItem> items) {
        items.add(OpexgramStyleWidgets.OpexMessageMenuPreviewCell.Factory.of(ID_MENU_PREVIEW, LagramConfig.messageFocusStyle));
        items.add(UItem.asShadow(-10, null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMessageFocusHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_MESSAGE_FOCUS, 0,
                LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramMessageFocus)),
                LocaleController.getString(R.string.LagramMessageFocusInfo),
                LagramConfig.messageFocusEnabled));
        if (LagramConfig.messageFocusEnabled) {
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_MESSAGE_FOCUS_BLUR, 0,
                    LocaleController.getString(R.string.LagramMessageFocusBlur),
                    null,
                    LagramConfig.messageFocusBlur));
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
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
        items.add(UItem.asShadow(-11, LocaleController.getString(R.string.LagramMessageFocusInfo)));
    }

    private void fillTextAnimation(ArrayList<UItem> items) {
        items.add(OpexgramStyleWidgets.OpexTextAnimPreviewCell.Factory.of(ID_TEXT_ANIM_PREVIEW));
        items.add(UItem.asShadow(-20, null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramTextAnimHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_TEXT_ANIM_ENABLE, 0,
                LocaleController.getString(R.string.LagramTextAnimEnable),
                LocaleController.getString(R.string.LagramTextAnimInfo),
                LagramConfig.textAppearAnimation));
        if (LagramConfig.textAppearAnimation) {
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_TEXT_ANIM_BLUR, 0,
                    LocaleController.getString(R.string.LagramTextAnimBlur),
                    null,
                    LagramConfig.textAnimBlur));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_TEXT_ANIM_SLIDE, 0,
                    LocaleController.getString(R.string.LagramTextAnimSlide),
                    null,
                    LagramConfig.textAnimSlide));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_TEXT_ANIM_SCALE, 0,
                    LocaleController.getString(R.string.LagramTextAnimScale),
                    null,
                    LagramConfig.textAnimScale));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_TEXT_ANIM_ROTATE, 0,
                    LocaleController.getString(R.string.LagramTextAnimRotate),
                    null,
                    LagramConfig.textAnimRotate));
        }
        items.add(UItem.asShadow(-21, null));
    }

    private void fillSafety(ArrayList<UItem> items) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatConfirmHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_CONFIRM_CALL, R.drawable.msg_calls,
                LocaleController.getString(R.string.LagramConfirmBeforeCall),
                null,
                LagramConfig.confirmBeforeCall));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_CONFIRM_STICKER, R.drawable.msg_sticker,
                LocaleController.getString(R.string.LagramConfirmBeforeSendSticker),
                null,
                LagramConfig.confirmBeforeSendSticker));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_FORWARD_NO_QUOTE, R.drawable.msg_forward,
                LocaleController.getString(R.string.LagramForwardWithoutQuote),
                null,
                LagramConfig.forwardWithoutQuote));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_CHANNEL_SHARE, R.drawable.msg_shareout,
                LocaleController.getString(R.string.LagramHideShareInChannels),
                null,
                LagramConfig.hideShareButtonInChannels));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_DISABLE_VOICE_VIDEO_TOGGLE, R.drawable.msg_voicechat,
                LocaleController.getString(R.string.LagramDisableVoiceVideoToggle),
                null,
                LagramConfig.disableVoiceVideoToggle));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_DISABLE_PROXIMITY, R.drawable.msg_mute,
                LocaleController.getString(R.string.LagramDisableProximitySensor),
                null,
                LagramConfig.disableProximitySensor));
        items.add(UItem.asShadow(-30, null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_NAV_MESSAGE_MENU:
                presentFragment(new LagramChatsActivity(SUB_MESSAGE_MENU));
                return;
            case ID_NAV_TEXT_ANIM:
                presentFragment(new LagramChatsActivity(SUB_TEXT_ANIMATION));
                return;
            case ID_NAV_SAFETY:
                presentFragment(new LagramChatsActivity(SUB_SAFETY));
                return;
            case ID_DELETE_EFFECT:
                showDeleteEffectDialog();
                return;
            case ID_CHAT_HEADER_EXPRESSIVE:
                toggle(view, LagramConfig.toggleChatHeaderExpressive());
                return;
            case ID_HIDE_KEYBOARD:
                toggle(view, LagramConfig.toggleHideKeyboardOnScroll());
                return;
            case ID_DOUBLE_TAP_OUT:
                showDoubleTapDialog(true);
                return;
            case ID_DOUBLE_TAP_IN:
                showDoubleTapDialog(false);
                return;
            case ID_PAGE_UP_BUTTON:
                toggle(view, LagramConfig.toggleShowPageUpButton());
                return;
            case ID_BIG_REACTIONS:
                toggle(view, LagramConfig.toggleBigReactions());
                return;
            case ID_DISABLE_NEXT_CHANNEL:
                toggle(view, LagramConfig.toggleDisableNextChannelJump());
                return;
            case ID_UNLIMITED_SELECTION:
                toggle(view, LagramConfig.toggleUnlimitedSelection());
                return;
            case ID_FAST_SELECTION:
                toggle(view, LagramConfig.toggleFastSelection());
                return;
            case ID_SHOW_WEEKDAY:
                toggle(view, LagramConfig.toggleShowWeekdayInDate());
                return;
            case ID_SHOW_SECONDS:
                toggle(view, LagramConfig.toggleShowSecondsInTime());
                return;
            case ID_SHOW_FORWARD_TIME:
                toggle(view, LagramConfig.toggleShowForwardTime());
                return;
            case ID_STICKER_PACK_OWNER:
                toggle(view, LagramConfig.toggleShowStickerPackOwner());
                return;
            case ID_SHOW_FORWARDS_COUNTER:
                toggle(view, LagramConfig.toggleShowForwardsCounter());
                return;
            case ID_MESSAGE_FOCUS:
                toggle(view, LagramConfig.toggleMessageFocusEnabled());
                return;
            case ID_MESSAGE_FOCUS_BLUR:
                toggle(view, LagramConfig.toggleMessageFocusBlur());
                return;
            case ID_TEXT_ANIM_ENABLE:
                toggle(view, LagramConfig.toggleTextAppearAnimation());
                return;
            case ID_TEXT_ANIM_BLUR:
                toggle(view, LagramConfig.toggleTextAnimBlur());
                return;
            case ID_TEXT_ANIM_SLIDE:
                toggle(view, LagramConfig.toggleTextAnimSlide());
                return;
            case ID_TEXT_ANIM_SCALE:
                toggle(view, LagramConfig.toggleTextAnimScale());
                return;
            case ID_TEXT_ANIM_ROTATE:
                toggle(view, LagramConfig.toggleTextAnimRotate());
                return;
            case ID_CONFIRM_CALL:
                toggle(view, LagramConfig.toggleConfirmBeforeCall());
                return;
            case ID_CONFIRM_STICKER:
                toggle(view, LagramConfig.toggleConfirmBeforeSendSticker());
                return;
            case ID_FORWARD_NO_QUOTE:
                toggle(view, LagramConfig.toggleForwardWithoutQuote());
                return;
            case ID_HIDE_CHANNEL_SHARE:
                toggle(view, LagramConfig.toggleHideShareButtonInChannels());
                return;
            case ID_DISABLE_VOICE_VIDEO_TOGGLE:
                toggle(view, LagramConfig.toggleDisableVoiceVideoToggle());
                return;
            case ID_DISABLE_PROXIMITY:
                toggle(view, LagramConfig.toggleDisableProximitySensor());
                return;
        }
    }

    private void showDeleteEffectDialog() {
        if (getParentActivity() == null) return;
        String[] labels = new String[]{
                LocaleController.getString(R.string.LagramDeleteEffectDefault),
                LocaleController.getString(R.string.LagramDeleteEffectThanos)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
        builder.setTitle(LocaleController.getString(R.string.LagramDeleteEffect));
        builder.setItems(labels, (d, which) -> {
            LagramConfig.setDeleteEffect(which == 0 ? LagramConfig.DELETE_EFFECT_DEFAULT : LagramConfig.DELETE_EFFECT_THANOS);
            refreshList();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
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

    private void toggle(View view, boolean checked) {
        if (view instanceof OpexgramStyleWidgets.OpexSwitchCell) {
            ((OpexgramStyleWidgets.OpexSwitchCell) view).setChecked(checked);
        }
        refreshList();
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private String getFocusStyleShort() {
        if (!LagramConfig.messageFocusEnabled) return LocaleController.getString(R.string.LagramHapticsOff);
        switch (LagramConfig.messageFocusStyle) {
            case LagramConfig.FOCUS_STYLE_LIST: return LocaleController.getString(R.string.LagramFocusStyleList);
            case LagramConfig.FOCUS_STYLE_TILES: return LocaleController.getString(R.string.LagramFocusStyleTiles);
            default: return LocaleController.getString(R.string.LagramFocusStyleCapsule);
        }
    }

    private String getDeleteEffectLabel() {
        return LagramConfig.deleteEffect == LagramConfig.DELETE_EFFECT_DEFAULT
                ? LocaleController.getString(R.string.LagramDeleteEffectDefault)
                : LocaleController.getString(R.string.LagramDeleteEffectThanos);
    }

    private String getDoubleTapLabel(int action) {
        switch (action) {
            case LagramConfig.DOUBLE_TAP_REPLY: return LocaleController.getString(R.string.LagramDoubleTapReply);
            case LagramConfig.DOUBLE_TAP_COPY: return LocaleController.getString(R.string.LagramDoubleTapCopy);
            case LagramConfig.DOUBLE_TAP_FORWARD: return LocaleController.getString(R.string.LagramDoubleTapForward);
            case LagramConfig.DOUBLE_TAP_EDIT: return LocaleController.getString(R.string.LagramDoubleTapEdit);
            case LagramConfig.DOUBLE_TAP_SAVE: return LocaleController.getString(R.string.LagramDoubleTapSave);
            case LagramConfig.DOUBLE_TAP_NONE: return LocaleController.getString(R.string.LagramDoubleTapNone);
            default: return LocaleController.getString(R.string.LagramDoubleTapReaction);
        }
    }
}
