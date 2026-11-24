package org.telegram.ui.lagram;

import android.content.Context;
import android.widget.LinearLayout;

import org.telegram.ui.Components.LayoutHelper;

/**
 * Privacidad y Utilidades subcategory fragment: live privacy preview + Graphite cards
 * for parallel translator, call/sticker confirmations, and phone number masking.
 */
public class LagramPrivacySettingsActivity extends LagramBaseSettingsFragment {

    private LagramPreviewViews.PrivacyLivePreviewView previewView;

    @Override
    protected String getScreenTitle() {
        return "Privacidad y Utilidades";
    }

    private void refreshPreview() {
        if (previewView != null) {
            previewView.invalidate();
        }
    }

    @Override
    protected void buildContent(Context context, LinearLayout root) {
        LinearLayout previewCard = LagramCardUi.createGraphiteCard(context);
        previewView = new LagramPreviewViews.PrivacyLivePreviewView(context);
        previewCard.addView(previewView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addCardWithMargin(root, previewCard);

        root.addView(LagramCardUi.createSectionHeader(context, "Privacidad y Protección"));
        LinearLayout privacyCard = LagramCardUi.createGraphiteCard(context);
        privacyCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Ocultar número de teléfono",
                "Enmascara tu número en perfil, menú lateral y ajustes",
                LagramConfig.hidePhoneNumber,
                v -> {
                    LagramConfig.setHidePhoneNumber(v);
                    refreshPreview();
                }
        ));
        privacyCard.addView(LagramCardUi.createDivider(context));
        privacyCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Confirmar antes de llamar",
                "Evita llamadas accidentales con un aviso previo",
                LagramConfig.confirmBeforeCall,
                v -> {
                    LagramConfig.setConfirmBeforeCall(v);
                    refreshPreview();
                }
        ));
        privacyCard.addView(LagramCardUi.createDivider(context));
        privacyCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Confirmar antes de enviar stickers",
                "Pide confirmación antes de enviar un sticker al tocarlo",
                LagramConfig.confirmBeforeSendSticker,
                v -> {
                    LagramConfig.setConfirmBeforeSendSticker(v);
                    refreshPreview();
                }
        ));
        addCardWithMargin(root, privacyCard);

        root.addView(LagramCardUi.createSectionHeader(context, "Utilidades de chat"));
        LinearLayout utilCard = LagramCardUi.createGraphiteCard(context);
        utilCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Traductor paralelo",
                "Muestra el mensaje original junto con su traducción debajo",
                LagramConfig.parallelTranslatorEnabled,
                v -> {
                    LagramConfig.setParallelTranslatorEnabled(v);
                    refreshPreview();
                }
        ));
        addCardWithMargin(root, utilCard);

        addResetCategoryButton(context, root, LagramConfig.CATEGORY_PRIVACY, "Restablecer Privacidad");
    }
}
