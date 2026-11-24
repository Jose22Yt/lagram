package org.telegram.ui.lagram;

import android.content.Context;
import android.widget.LinearLayout;

import org.telegram.ui.Components.LayoutHelper;

/**
 * Chats subcategory fragment: live conversation preview + Graphite cards for
 * Message Bubbles, Bottom Input Bar, Chat Top Header, and Quick Message Actions.
 */
public class LagramChatsSettingsActivity extends LagramBaseSettingsFragment {

    private LagramPreviewViews.ChatsLivePreviewView previewView;

    @Override
    protected String getScreenTitle() {
        return "Chats";
    }

    private void refreshPreview() {
        if (previewView != null) {
            previewView.invalidate();
        }
    }

    @Override
    protected void buildContent(Context context, LinearLayout root) {
        // Live conversation preview inside Graphite card
        LinearLayout previewCard = LagramCardUi.createGraphiteCard(context);
        previewView = new LagramPreviewViews.ChatsLivePreviewView(context);
        previewCard.addView(previewView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addCardWithMargin(root, previewCard);

        // Card 1: Burbujas y Stickers
        root.addView(LagramCardUi.createSectionHeader(context, "Burbujas de mensajes y Stickers"));
        LinearLayout bubblesCard = LagramCardUi.createGraphiteCard(context);
        bubblesCard.addView(LagramCardUi.createSliderRow(
                context,
                "Radio de esquinas de burbujas",
                LagramConfig.MIN_BUBBLE_RADIUS_DP,
                LagramConfig.MAX_BUBBLE_RADIUS_DP,
                LagramConfig.bubbleCornerRadius,
                " dp",
                val -> {
                    LagramConfig.setBubbleCornerRadius(val);
                    refreshPreview();
                }
        ));
        bubblesCard.addView(LagramCardUi.createDivider(context));
        bubblesCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Estilo de cola de burbuja",
                null,
                new String[]{"Clásica", "Sin cola", "Corta"},
                LagramConfig.bubbleTailStyle,
                idx -> {
                    LagramConfig.setBubbleTailStyle(idx);
                    refreshPreview();
                }
        ));
        bubblesCard.addView(LagramCardUi.createDivider(context));
        bubblesCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Espaciado vertical entre mensajes",
                null,
                new String[]{"Compacto", "Normal", "Cómodo"},
                LagramConfig.messageVerticalSpacing,
                idx -> {
                    LagramConfig.setMessageVerticalSpacing(idx);
                    refreshPreview();
                }
        ));
        bubblesCard.addView(LagramCardUi.createDivider(context));
        bubblesCard.addView(LagramCardUi.createSliderRow(
                context,
                "Tamaño de stickers",
                LagramConfig.MIN_STICKER_SIZE_PERCENT,
                LagramConfig.MAX_STICKER_SIZE_PERCENT,
                LagramConfig.stickerSizePercent,
                "%",
                val -> {
                    LagramConfig.setStickerSizePercent(val);
                    refreshPreview();
                }
        ));
        addCardWithMargin(root, bubblesCard);

        // Card 2: Barra de entrada inferior
        root.addView(LagramCardUi.createSectionHeader(context, "Barra de entrada inferior"));
        LinearLayout inputCard = LagramCardUi.createGraphiteCard(context);
        inputCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Estilo de barra de entrada",
                "Vista previa en vivo arriba",
                new String[]{"iOS 3 píldoras", "Píldora M3", "Clásica TG"},
                LagramConfig.inputBarStyle,
                idx -> {
                    LagramConfig.setInputBarStyle(idx);
                    refreshPreview();
                }
        ));
        inputCard.addView(LagramCardUi.createDivider(context));
        inputCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Ocultar botón de regalos",
                "Deja una barra de texto más limpia",
                LagramConfig.hideInputGiftButton,
                v -> {
                    LagramConfig.setHideInputGiftButton(v);
                    refreshPreview();
                }
        ));
        inputCard.addView(LagramCardUi.createDivider(context));
        inputCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Ocultar botón de comandos de bot",
                null,
                LagramConfig.hideInputBotCommandButton,
                v -> {
                    LagramConfig.setHideInputBotCommandButton(v);
                    refreshPreview();
                }
        ));
        inputCard.addView(LagramCardUi.createDivider(context));
        inputCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Ocultar botón rápido audio/video",
                null,
                LagramConfig.hideInputQuickAudioVideoButton,
                v -> {
                    LagramConfig.setHideInputQuickAudioVideoButton(v);
                    refreshPreview();
                }
        ));
        addCardWithMargin(root, inputCard);

        // Card 3: Encabezado superior de chat
        root.addView(LagramCardUi.createSectionHeader(context, "Encabezado superior de chat"));
        LinearLayout headerCard = LagramCardUi.createGraphiteCard(context);
        headerCard.addView(LagramCardUi.createSliderRow(
                context,
                "Tamaño del avatar en cabecera",
                LagramConfig.MIN_CHAT_HEADER_AVATAR_DP,
                LagramConfig.MAX_CHAT_HEADER_AVATAR_DP,
                LagramConfig.chatHeaderAvatarSizeDp,
                " dp",
                val -> {
                    LagramConfig.setChatHeaderAvatarSizeDp(val);
                    refreshPreview();
                }
        ));
        headerCard.addView(LagramCardUi.createDivider(context));
        headerCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Mostrar subtítulo (\"últ. vez\" / estado)",
                "Si se desactiva, centra el nombre limpiamente",
                LagramConfig.chatHeaderShowSubtitle,
                v -> {
                    LagramConfig.setChatHeaderShowSubtitle(v);
                    refreshPreview();
                }
        ));
        headerCard.addView(LagramCardUi.createDivider(context));
        headerCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Botones rápidos limpios en cabecera",
                "Botones circulares tonales en la barra superior del chat",
                LagramConfig.chatHeaderCleanQuickButtons,
                v -> {
                    LagramConfig.setChatHeaderCleanQuickButtons(v);
                    refreshPreview();
                }
        ));
        addCardWithMargin(root, headerCard);

        // Card 4: Acciones rápidas en mensajes
        root.addView(LagramCardUi.createSectionHeader(context, "Acciones rápidas en mensajes"));
        LinearLayout actionsCard = LagramCardUi.createGraphiteCard(context);
        actionsCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Doble toque en mensaje (Fila 1)",
                "Elige qué acción ejecutar al hacer doble toque",
                new String[]{"Reaccionar", "Responder", "Copiar"},
                LagramConfig.doubleTapAction <= 2 ? LagramConfig.doubleTapAction : -1,
                idx -> {
                    LagramConfig.setDoubleTapAction(idx);
                    rebuildContent(context);
                }
        ));
        actionsCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Doble toque en mensaje (Fila 2)",
                null,
                new String[]{"Reenviar", "Traducir", "Guardados"},
                LagramConfig.doubleTapAction >= 3 ? LagramConfig.doubleTapAction - 3 : -1,
                idx -> {
                    LagramConfig.setDoubleTapAction(idx + 3);
                    rebuildContent(context);
                }
        ));
        actionsCard.addView(LagramCardUi.createDivider(context));
        actionsCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Reenviar sin autor por defecto",
                "Oculta el remitente original al reenviar rápidamente",
                LagramConfig.quickForwardNoAuthor,
                LagramConfig::setQuickForwardNoAuthor
        ));
        actionsCard.addView(LagramCardUi.createDivider(context));
        actionsCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Copiar enlace limpio",
                "Elimina parámetros de rastreo (?single, utm_*, etc.)",
                LagramConfig.copyCleanLink,
                LagramConfig::setCopyCleanLink
        ));
        addCardWithMargin(root, actionsCard);

        addResetCategoryButton(context, root, LagramConfig.CATEGORY_CHATS, "Restablecer Chats");
    }
}
