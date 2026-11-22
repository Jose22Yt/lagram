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

    // 1. Message Tap Focus & Menu (Opexgram Focus + 3 styles)
    private static final int ID_PREVIEW_MSG_MENU = 200;
    private static final int ID_FOCUS_ENABLED = 201;
    private static final int ID_FOCUS_BLUR = 202;
    private static final int ID_FOCUS_STYLE = 203;

    // 2. Chat Top Bar Styles (4 clean, collision-free styles)
    private static final int ID_PREVIEW_HEADER = 210;
    private static final int ID_HEADER_STYLE = 211;

    // 3. Bottom Bar & Live Typing Effects
    private static final int ID_PREVIEW_CHAT_BOX_TYPING = 220;
    private static final int ID_IOS27_BAR = 221;
    private static final int ID_TEXT_ANIM_ENABLE = 222;
    private static final int ID_TEXT_ANIM_EFFECTS = 223;
    private static final int ID_TEXT_ANIM_SPEED = 224;

    // 4. Delete Message Animation
    private static final int ID_PREVIEW_DELETE = 230;
    private static final int ID_DELETE_EFFECT = 231;

    // 4b. Fast Chat Translation (Nagram Engine)
    private static final int ID_NAGRAM_TRANSLATE = 232;
    private static final int ID_TRANSLATE_PROVIDER = 233;
    private static final int ID_TRANSLATE_KEEP_ORIG = 234;
    private static final int ID_AUTO_TRANSLATE = 235;
    private static final int ID_CLEAR_TRANSLATE_CACHE = 236;

    // 5. Gestures, Selection & Sizes
    private static final int ID_DOUBLE_TAP_IN = 240;
    private static final int ID_DOUBLE_TAP_OUT = 241;
    private static final int ID_UNLIMITED_SEL = 242;
    private static final int ID_STICKER_SIZE = 243;
    private static final int ID_GIF_SIZE = 244;
    private static final int ID_BIG_REACTIONS = 245;

    // 6. Controls & Confirmations
    private static final int ID_PAGE_UP = 250;
    private static final int ID_HIDE_KB_SCROLL = 251;
    private static final int ID_DISABLE_NEXT_CHANNEL = 252;
    private static final int ID_CONFIRM_CALL = 253;
    private static final int ID_CONFIRM_STICKER = 254;
    private static final int ID_DISABLE_VOICE_TOGGLE = 255;
    private static final int ID_DISABLE_PROXIMITY = 256;

    // 7. Quotes, Forwarding & Details
    private static final int ID_FORWARD_NO_QUOTE = 260;
    private static final int ID_HIDE_SHARE_CHANNELS = 261;
    private static final int ID_SHOW_WEEKDAY = 262;
    private static final int ID_SHOW_FWD_TIME = 263;
    private static final int ID_SHOW_FWD_COUNT = 264;
    private static final int ID_SHOW_STICKER_OWNER = 265;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryChats);
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
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // 1. REAL Interactive Message Tap Focus & Menu (List / Tiles / Capsule)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMessageFocusHeader)));
        items.add(LagramRealPreviews.RealMessageMenuPreviewCell.Factory.of(ID_PREVIEW_MSG_MENU));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_FOCUS_ENABLED, 0,
                LocaleController.getString(R.string.LagramMessageFocus),
                null,
                LagramConfig.messageFocusEnabled));
        if (LagramConfig.messageFocusEnabled) {
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_FOCUS_BLUR, 0,
                    LocaleController.getString(R.string.LagramMessageFocusBlur),
                    null,
                    LagramConfig.messageFocusBlur));
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_FOCUS_STYLE,
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
        items.add(UItem.asShadow(LocaleController.getString(R.string.LagramMessageFocusInfo)));

        // 2. REAL Interactive Chat Top Bar Styles (4 clean, collision-free styles)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatHeaderStyle)));
        items.add(LagramRealPreviews.RealChatHeaderPreviewCell.Factory.of(ID_PREVIEW_HEADER));
        int currentHeaderIdx = Math.min(LagramConfig.CHAT_HEADER_ADAPTIVE, LagramConfig.chatHeaderStyle);
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                ID_HEADER_STYLE,
                null,
                new String[]{
                        LocaleController.getString(R.string.LagramChatHeaderDefault),
                        LocaleController.getString(R.string.LagramChatHeaderIos),
                        LocaleController.getString(R.string.LagramChatHeaderCompact),
                        LocaleController.getString(R.string.LagramChatHeaderAdaptive)
                },
                currentHeaderIdx,
                idx -> {
                    LagramConfig.setChatHeaderStyle(idx);
                    refreshList();
                }));
        items.add(UItem.asShadow(null));

        // 3. REAL Interactive Bottom Bar & Live Per-Character Typing Effects
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramTextAnimHeader)));
        items.add(LagramRealPreviews.RealInteractiveChatBoxPreviewCell.Factory.of(ID_PREVIEW_CHAT_BOX_TYPING));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_IOS27_BAR, 0,
                LocaleController.getString(R.string.LagramIos27BottomBar),
                null,
                LagramConfig.ios27BottomBar));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_TEXT_ANIM_ENABLE, 0,
                LocaleController.getString(R.string.LagramTextAnimEnable),
                LocaleController.getString(R.string.LagramTextAnimInfo),
                LagramConfig.textAppearAnimation));
        if (LagramConfig.textAppearAnimation) {
            int mask = (LagramConfig.textAnimBlur ? 1 : 0)
                    | (LagramConfig.textAnimSlide ? 2 : 0)
                    | (LagramConfig.textAnimScale ? 4 : 0)
                    | (LagramConfig.textAnimRotate ? 8 : 0);
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.multi(
                    ID_TEXT_ANIM_EFFECTS,
                    null,
                    new String[]{
                            LocaleController.getString(R.string.LagramTextAnimBlur),
                            LocaleController.getString(R.string.LagramTextAnimSlide),
                            LocaleController.getString(R.string.LagramTextAnimScale),
                            LocaleController.getString(R.string.LagramTextAnimRotate)
                    },
                    mask,
                    idx -> {
                        if (idx == 0) LagramConfig.toggleTextAnimBlur();
                        else if (idx == 1) LagramConfig.toggleTextAnimSlide();
                        else if (idx == 2) LagramConfig.toggleTextAnimScale();
                        else if (idx == 3) LagramConfig.toggleTextAnimRotate();
                        refreshList();
                    }));
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_TEXT_ANIM_SPEED,
                    LocaleController.getString(R.string.LagramTextAnimSpeed),
                    new String[]{
                            LocaleController.getString(R.string.LagramTextAnimSpeedFast),
                            LocaleController.getString(R.string.LagramTextAnimSpeedNormal),
                            LocaleController.getString(R.string.LagramTextAnimSpeedSmooth)
                    },
                    LagramConfig.textAnimSpeed,
                    idx -> {
                        LagramConfig.setTextAnimSpeed(idx);
                        refreshList();
                    }));
        }
        items.add(UItem.asShadow(null));

        // 4. REAL Interactive Delete Message Animation Preview
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramDeleteEffect)));
        items.add(LagramRealPreviews.RealDeleteAnimPreviewCell.Factory.of(ID_PREVIEW_DELETE));
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                ID_DELETE_EFFECT,
                null,
                new String[]{
                        LocaleController.getString(R.string.LagramDeleteEffectDefault),
                        LocaleController.getString(R.string.LagramDeleteEffectThanos),
                        LocaleController.getString(R.string.LagramDeleteEffectShards),
                        LocaleController.getString(R.string.LagramDeleteEffectPortal),
                        LocaleController.getString(R.string.LagramDeleteEffectSlide)
                },
                LagramConfig.deleteEffect,
                idx -> {
                    LagramConfig.setDeleteEffect(idx);
                    refreshList();
                }));
        items.add(UItem.asShadow(null));

        // 4b. Fast Chat Translation (Nagram Engine)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramTranslateHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_NAGRAM_TRANSLATE, 0,
                LocaleController.getString(R.string.LagramNagramTranslate),
                LocaleController.getString(R.string.LagramNagramTranslateDesc),
                LagramConfig.nagramTranslateEnabled));
        if (LagramConfig.nagramTranslateEnabled) {
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_TRANSLATE_PROVIDER,
                    LocaleController.getString(R.string.LagramTranslateProvider),
                    new String[]{
                            LocaleController.getString(R.string.LagramTranslateProviderGoogle),
                            LocaleController.getString(R.string.LagramTranslateProviderMicrosoft),
                            LocaleController.getString(R.string.LagramTranslateProviderGoogleCloud),
                            LocaleController.getString(R.string.LagramTranslateProviderTelegram)
                    },
                    LagramConfig.translateProvider,
                    idx -> {
                        LagramConfig.setTranslateProvider(idx);
                        refreshList();
                    }));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_TRANSLATE_KEEP_ORIG, 0,
                    LocaleController.getString(R.string.LagramTranslateKeepOriginal),
                    LocaleController.getString(R.string.LagramTranslateKeepOriginalDesc),
                    LagramConfig.translateKeepOriginal));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_AUTO_TRANSLATE, 0,
                    LocaleController.getString(R.string.LagramAutoTranslateChats),
                    LocaleController.getString(R.string.LagramAutoTranslateChatsDesc),
                    LagramConfig.autoTranslateChats));
            items.add(UItem.asButton(
                    ID_CLEAR_TRANSLATE_CACHE,
                    R.drawable.msg_delete,
                    LocaleController.getString(R.string.LagramClearTranslateCache)));
        }
        items.add(UItem.asShadow(null));

        // 5. Gestures & Sizes
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatGesturesHeader)));
        items.add(UItem.asButton(ID_DOUBLE_TAP_IN, 0,
                LocaleController.getString(R.string.LagramDoubleTapIncoming),
                doubleTapLabel(LagramConfig.doubleTapActionIncoming)));
        items.add(UItem.asButton(ID_DOUBLE_TAP_OUT, 0,
                LocaleController.getString(R.string.LagramDoubleTapOutgoing),
                doubleTapLabel(LagramConfig.doubleTapActionOutgoing)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_UNLIMITED_SEL, 0,
                LocaleController.getString(R.string.LagramUnlimitedSelection),
                null,
                LagramConfig.unlimitedSelection));
        items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                ID_STICKER_SIZE,
                LocaleController.getString(R.string.LagramStickerSize),
                8, 20, 14, LagramConfig.stickerSize, "",
                val -> {
                    LagramConfig.setStickerSize(val);
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                ID_GIF_SIZE,
                LocaleController.getString(R.string.LagramGifSize),
                8, 20, 14, LagramConfig.gifSize, "",
                val -> {
                    LagramConfig.setGifSize(val);
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_BIG_REACTIONS, 0,
                LocaleController.getString(R.string.LagramBigReactions),
                null,
                LagramConfig.bigReactions));
        items.add(UItem.asShadow(null));

        // 6. Controls & Confirmations
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatConfirmHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_PAGE_UP, 0,
                LocaleController.getString(R.string.LagramShowPageUpButton),
                null,
                LagramConfig.showPageUpButton));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_KB_SCROLL, 0,
                LocaleController.getString(R.string.LagramHideKeyboardOnScroll),
                null,
                LagramConfig.hideKeyboardOnScroll));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_DISABLE_NEXT_CHANNEL, 0,
                LocaleController.getString(R.string.LagramDisableNextChannelJump),
                null,
                LagramConfig.disableNextChannelJump));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_CONFIRM_CALL, 0,
                LocaleController.getString(R.string.LagramConfirmBeforeCall),
                null,
                LagramConfig.confirmBeforeCall));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_CONFIRM_STICKER, 0,
                LocaleController.getString(R.string.LagramConfirmBeforeSendSticker),
                null,
                LagramConfig.confirmBeforeSendSticker));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_DISABLE_VOICE_TOGGLE, 0,
                LocaleController.getString(R.string.LagramDisableVoiceVideoToggle),
                null,
                LagramConfig.disableVoiceVideoToggle));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_DISABLE_PROXIMITY, 0,
                LocaleController.getString(R.string.LagramDisableProximitySensor),
                null,
                LagramConfig.disableProximitySensor));
        items.add(UItem.asShadow(null));

        // 7. Quotes, Forwarding & Details
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramQuotesTitle)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_FORWARD_NO_QUOTE, 0,
                LocaleController.getString(R.string.LagramForwardWithoutQuote),
                null,
                LagramConfig.forwardWithoutQuote));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_SHARE_CHANNELS, 0,
                LocaleController.getString(R.string.LagramHideShareInChannels),
                null,
                LagramConfig.hideShareButtonInChannels));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_WEEKDAY, 0,
                LocaleController.getString(R.string.LagramShowWeekdayInDate),
                null,
                LagramConfig.showWeekdayInDate));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_FWD_TIME, 0,
                LocaleController.getString(R.string.LagramShowForwardTime),
                null,
                LagramConfig.showForwardTime));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_FWD_COUNT, 0,
                LocaleController.getString(R.string.LagramShowForwardsCounter),
                null,
                LagramConfig.showForwardsCounter));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_STICKER_OWNER, 0,
                LocaleController.getString(R.string.LagramShowStickerPackOwner),
                null,
                LagramConfig.showStickerPackOwner));
        items.add(UItem.asShadow(null));
    }

    private String doubleTapLabel(int action) {
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

    private void showDoubleTapPicker(boolean outgoing) {
        if (getParentActivity() == null) return;
        String[] opts = new String[]{
                LocaleController.getString(R.string.LagramDoubleTapReaction),
                LocaleController.getString(R.string.LagramDoubleTapReply),
                LocaleController.getString(R.string.LagramDoubleTapCopy),
                LocaleController.getString(R.string.LagramDoubleTapForward),
                LocaleController.getString(R.string.LagramDoubleTapEdit),
                LocaleController.getString(R.string.LagramDoubleTapSave),
                LocaleController.getString(R.string.LagramDoubleTapNone)
        };
        AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
        b.setTitle(LocaleController.getString(outgoing ? R.string.LagramDoubleTapOutgoing : R.string.LagramDoubleTapIncoming));
        b.setItems(opts, (d, which) -> {
            if (outgoing) {
                LagramConfig.setDoubleTapActionOutgoing(which);
            } else {
                LagramConfig.setDoubleTapActionIncoming(which);
            }
            refreshList();
        });
        showDialog(b.create());
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_FOCUS_ENABLED:
                LagramConfig.toggleMessageFocusEnabled();
                refreshList();
                break;
            case ID_FOCUS_BLUR:
                LagramConfig.toggleMessageFocusBlur();
                refreshList();
                break;
            case ID_IOS27_BAR:
                LagramConfig.toggleIos27BottomBar();
                refreshList();
                break;
            case ID_TEXT_ANIM_ENABLE:
                LagramConfig.toggleTextAppearAnimation();
                refreshList();
                break;
            case ID_NAGRAM_TRANSLATE:
                LagramConfig.toggleNagramTranslateEnabled();
                refreshList();
                break;
            case ID_TRANSLATE_KEEP_ORIG:
                LagramConfig.toggleTranslateKeepOriginal();
                refreshList();
                break;
            case ID_AUTO_TRANSLATE:
                LagramConfig.toggleAutoTranslateChats();
                refreshList();
                break;
            case ID_CLEAR_TRANSLATE_CACHE:
                org.telegram.messenger.LagramTranslateDb.clearAll(() -> {
                    org.telegram.ui.Components.BulletinFactory.of(this)
                            .createSimpleBulletin(R.raw.ic_delete, LocaleController.getString(R.string.LagramTranslateCacheCleared))
                            .show();
                });
                break;
            case ID_DOUBLE_TAP_IN:
                showDoubleTapPicker(false);
                break;
            case ID_DOUBLE_TAP_OUT:
                showDoubleTapPicker(true);
                break;
            case ID_UNLIMITED_SEL:
                LagramConfig.toggleUnlimitedSelection();
                refreshList();
                break;
            case ID_BIG_REACTIONS:
                LagramConfig.toggleBigReactions();
                refreshList();
                break;
            case ID_PAGE_UP:
                LagramConfig.toggleShowPageUpButton();
                refreshList();
                break;
            case ID_HIDE_KB_SCROLL:
                LagramConfig.toggleHideKeyboardOnScroll();
                refreshList();
                break;
            case ID_DISABLE_NEXT_CHANNEL:
                LagramConfig.toggleDisableNextChannelJump();
                refreshList();
                break;
            case ID_CONFIRM_CALL:
                LagramConfig.toggleConfirmBeforeCall();
                refreshList();
                break;
            case ID_CONFIRM_STICKER:
                LagramConfig.toggleConfirmBeforeSendSticker();
                refreshList();
                break;
            case ID_DISABLE_VOICE_TOGGLE:
                LagramConfig.toggleDisableVoiceVideoToggle();
                refreshList();
                break;
            case ID_DISABLE_PROXIMITY:
                LagramConfig.toggleDisableProximitySensor();
                refreshList();
                break;
            case ID_FORWARD_NO_QUOTE:
                LagramConfig.toggleForwardWithoutQuote();
                refreshList();
                break;
            case ID_HIDE_SHARE_CHANNELS:
                LagramConfig.toggleHideShareButtonInChannels();
                refreshList();
                break;
            case ID_SHOW_WEEKDAY:
                LagramConfig.toggleShowWeekdayInDate();
                refreshList();
                break;
            case ID_SHOW_FWD_TIME:
                LagramConfig.toggleShowForwardTime();
                refreshList();
                break;
            case ID_SHOW_FWD_COUNT:
                LagramConfig.toggleShowForwardsCounter();
                refreshList();
                break;
            case ID_SHOW_STICKER_OWNER:
                LagramConfig.toggleShowStickerPackOwner();
                refreshList();
                break;
        }
    }
}
