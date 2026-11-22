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
    public static final int SUB_FOCUS_AND_SIZE = 1;
    public static final int SUB_GESTURES_AND_EFFECTS = 2;
    public static final int SUB_CONFIRM_AND_DETAILS = 3;

    private final int subPage;

    private static final int ID_NAV_FOCUS_AND_SIZE = 100;
    private static final int ID_NAV_GESTURES = 101;
    private static final int ID_NAV_CONFIRM_DETAILS = 102;

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
            case SUB_FOCUS_AND_SIZE:
                return LocaleController.getString(R.string.LagramMessageFocusHeader);
            case SUB_GESTURES_AND_EFFECTS:
                return LocaleController.getString(R.string.LagramChatGesturesHeader);
            case SUB_CONFIRM_AND_DETAILS:
                return LocaleController.getString(R.string.LagramChatDetailsHeader);
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
        if (LagramConfig.amoledSettingsBackdrop && Theme.isCurrentThemeDark()) {
            fragmentView.setBackgroundColor(0xFF000000);
            listView.setBackgroundColor(0xFF000000);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
        refreshLivePreviews();
    }

    private void refreshLivePreviews() {
        if (listView == null) {
            return;
        }
        for (int i = 0, count = listView.getChildCount(); i < count; i++) {
            View child = listView.getChildAt(i);
            if (child instanceof LagramSettingsComponents.RealChatMessagesPreviewCell) {
                ((LagramSettingsComponents.RealChatMessagesPreviewCell) child).refreshPreview();
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

    private String getFocusStyleShort() {
        switch (LagramConfig.messageFocusStyle) {
            case LagramConfig.FOCUS_STYLE_LIST:
                return "Lista";
            case LagramConfig.FOCUS_STYLE_TILES:
                return "Cuadrícula";
            case LagramConfig.FOCUS_STYLE_CAPSULE:
            default:
                return "Cápsula";
        }
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        if (subPage == SUB_HOME) {
            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMessageFocusHeader)));
            items.add(LagramSettingsComponents.RealChatMessagesPreviewCell.Factory.of(
                    ID_PREVIEW_CHAT_FOCUS, LagramSettingsComponents.RealChatMessagesPreviewCell.MODE_STICKER_AND_FOCUS));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_FOCUS_ENABLED,
                    R.drawable.msg_discussion,
                    LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramMessageFocus)),
                    "Eleva la burbuja al tocarla con desenfoque y menú estilo Opexgram",
                    LagramConfig.messageFocusEnabled
            ));
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
            items.add(UItem.asShadow(null));

            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramCategoriesHeader)));
            items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                    ID_NAV_FOCUS_AND_SIZE,
                    IconBackgroundColors.GREEN,
                    R.drawable.msg_sticker,
                    "Enfoque, Stickers, GIFs y Selección",
                    "Tamaño en vivo de Stickers/GIFs, desenfoque Focus y selección ilimitada",
                    getFocusStyleShort() + " • " + LagramConfig.stickerSize
            ));
            items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                    ID_NAV_GESTURES,
                    IconBackgroundColors.BLUE,
                    R.drawable.msg_reactions,
                    "Gestos, Doble Toque y Efectos",
                    "Acciones de doble toque, efecto al eliminar, reenvío sin cita y teclado",
                    getDoubleTapLabel(LagramConfig.doubleTapActionOutgoing)
            ));
            items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                    ID_NAV_CONFIRM_DETAILS,
                    IconBackgroundColors.ORANGE,
                    R.drawable.msg_calls,
                    "Confirmaciones, Fechas y Reenvíos",
                    "Confirmar llamadas/stickers, hora original de reenvío y día de semana",
                    null
            ));
            items.add(UItem.asShadow(LocaleController.getString(R.string.LagramMessageFocusInfo)));
            return;
        }

        if (subPage == SUB_FOCUS_AND_SIZE) {
            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMessageFocusHeader)));
            items.add(LagramSettingsComponents.RealChatMessagesPreviewCell.Factory.of(
                    ID_PREVIEW_CHAT_FOCUS, LagramSettingsComponents.RealChatMessagesPreviewCell.MODE_STICKER_AND_FOCUS));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_FOCUS_ENABLED,
                    R.drawable.msg_discussion,
                    LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramMessageFocus)),
                    "Eleva la burbuja con animación spring al tocar un mensaje",
                    LagramConfig.messageFocusEnabled
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_FOCUS_BLUR,
                    R.drawable.msg_photo_blur,
                    LocaleController.getString(R.string.LagramMessageFocusBlur),
                    "Desenfoque acelerado por hardware detrás del mensaje enfocado",
                    LagramConfig.messageFocusBlur
            ));
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
            items.add(UItem.asShadow(null));

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
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_HIDE_SHARE_CHANNELS,
                    R.drawable.msg_share,
                    LocaleController.getString(R.string.LagramHideShareInChannels),
                    "Ocultar el botón lateral de compartir en publicaciones de canales",
                    LagramConfig.hideShareButtonInChannels
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_UNLIMITED_SELECTION,
                    R.drawable.msg_check_s,
                    LocaleController.getString(R.string.LagramUnlimitedSelection),
                    "Permite seleccionar más de 100 mensajes simultáneamente",
                    LagramConfig.unlimitedSelection
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_FAST_SELECTION,
                    R.drawable.msg_list,
                    LocaleController.getString(R.string.LagramFastSelection),
                    "Deslizar para seleccionar múltiples mensajes rápidamente",
                    LagramConfig.fastSelection
            ));
            items.add(UItem.asShadow(null));
            return;
        }

        if (subPage == SUB_GESTURES_AND_EFFECTS) {
            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatGesturesHeader)));
            items.add(UItem.asButton(ID_DOUBLE_TAP_OUT, R.drawable.msg_reactions,
                    LocaleController.getString(R.string.LagramDoubleTapOutgoing),
                    getDoubleTapLabel(LagramConfig.doubleTapActionOutgoing)));
            items.add(UItem.asButton(ID_DOUBLE_TAP_IN, R.drawable.msg_reactions,
                    LocaleController.getString(R.string.LagramDoubleTapIncoming),
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
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_TEXT_APPEAR_ANIM,
                    R.drawable.msg_photo_text2,
                    LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramTextAppearAnimation)),
                    "Animación fluida al escribir y enviar mensajes",
                    LagramConfig.textAppearAnimation
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_HIDE_KEYBOARD_SCROLL,
                    R.drawable.msg_edit,
                    LocaleController.getString(R.string.LagramHideKeyboardOnScroll),
                    "Ocultar automáticamente el teclado al deslizar la conversación",
                    LagramConfig.hideKeyboardOnScroll
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_DISABLE_NEXT_CHANNEL,
                    R.drawable.msg_channel,
                    LocaleController.getString(R.string.LagramDisableNextChannelJump),
                    "Evitar saltar al siguiente canal no leído al llegar al final",
                    LagramConfig.disableNextChannelJump
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_FORWARD_NO_QUOTE,
                    R.drawable.msg_forward,
                    LocaleController.getString(R.string.LagramForwardWithoutQuote),
                    "Ocultar el remitente original por defecto al reenviar mensajes",
                    LagramConfig.forwardWithoutQuote
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_SHOW_PAGE_UP,
                    R.drawable.msg_inputarrow,
                    LocaleController.getString(R.string.LagramShowPageUpButton),
                    "Mostrar botón rápido para subir al inicio del chat",
                    LagramConfig.showPageUpButton
            ));
            items.add(UItem.asShadow(null));
            return;
        }

        if (subPage == SUB_CONFIRM_AND_DETAILS) {
            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatConfirmHeader)));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_CONFIRM_CALL,
                    R.drawable.msg_calls,
                    LocaleController.getString(R.string.LagramConfirmBeforeCall),
                    "Preguntar antes de iniciar una llamada de voz o video",
                    LagramConfig.confirmBeforeCall
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_CONFIRM_STICKER,
                    R.drawable.msg_sticker,
                    LocaleController.getString(R.string.LagramConfirmBeforeSendSticker),
                    "Mostrar confirmación antes de enviar un Sticker o GIF",
                    LagramConfig.confirmBeforeSendSticker
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_DISABLE_VOICE_VIDEO_TOGGLE,
                    R.drawable.msg_filled_data_voice,
                    LocaleController.getString(R.string.LagramDisableVoiceVideoToggle),
                    "Evitar cambiar accidentalmente entre nota de voz y videomensaje",
                    LagramConfig.disableVoiceVideoToggle
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_DISABLE_PROXIMITY,
                    R.drawable.msg_calls,
                    LocaleController.getString(R.string.LagramDisableProximitySensor),
                    "Ignorar el sensor de proximidad al escuchar notas de voz",
                    LagramConfig.disableProximitySensor
            ));
            items.add(UItem.asShadow(null));

            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatDetailsHeader)));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_SHOW_WEEKDAY,
                    R.drawable.msg_calendar2,
                    LocaleController.getString(R.string.LagramShowWeekdayInDate),
                    "Incluir el día de la semana en las cabeceras de fecha del chat",
                    LagramConfig.showWeekdayInDate
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_SHOW_FORWARD_TIME,
                    R.drawable.msg_recent,
                    LocaleController.getString(R.string.LagramShowForwardTime),
                    "Mostrar la fecha y hora original en el encabezado de reenviados",
                    LagramConfig.showForwardTime
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_SHOW_FORWARDS_COUNTER,
                    R.drawable.msg_forward,
                    LocaleController.getString(R.string.LagramShowForwardsCounter),
                    "Mostrar cuántas veces fue reenviado un mensaje (↪ N)",
                    LagramConfig.showForwardsCounter
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_SHOW_POLL_PREVIEW,
                    R.drawable.filled_chatlist_poll,
                    LocaleController.getString(R.string.LagramShowPollPreview),
                    "Ver resultados de encuestas públicas sin necesidad de votar",
                    LagramConfig.showPollPreview
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_SHOW_STICKER_OWNER,
                    R.drawable.msg_openprofile,
                    LocaleController.getString(R.string.LagramShowStickerPackOwner),
                    "Mostrar información del creador del pack de stickers",
                    LagramConfig.showStickerPackOwner
            ));
            items.add(UItem.asShadow(null));
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString(isOutgoing ? R.string.LagramDoubleTapOutgoing : R.string.LagramDoubleTapIncoming));
        builder.setItems(options, (dialog, which) -> {
            if (isOutgoing) {
                LagramConfig.setDoubleTapActionOutgoing(which);
            } else {
                LagramConfig.setDoubleTapActionIncoming(which);
            }
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_NAV_FOCUS_AND_SIZE:
                presentFragment(new LagramChatsActivity(SUB_FOCUS_AND_SIZE));
                return;
            case ID_NAV_GESTURES:
                presentFragment(new LagramChatsActivity(SUB_GESTURES_AND_EFFECTS));
                return;
            case ID_NAV_CONFIRM_DETAILS:
                presentFragment(new LagramChatsActivity(SUB_CONFIRM_AND_DETAILS));
                return;
            case ID_DOUBLE_TAP_OUT:
                showDoubleTapDialog(true);
                return;
            case ID_DOUBLE_TAP_IN:
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
                refreshLivePreviews();
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
                refreshLivePreviews();
                break;
        }
        if (checkChanged && view instanceof LagramSettingsComponents.IconSwitchCell) {
            ((LagramSettingsComponents.IconSwitchCell) view).setChecked(newVal);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
