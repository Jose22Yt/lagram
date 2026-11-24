package org.telegram.ui.lagram;

import android.content.Context;
import android.widget.LinearLayout;

import org.telegram.ui.Components.LayoutHelper;

/**
 * Reproductor y Multimedia subcategory fragment: live player preview + Graphite cards
 * for exteraless-style audio player, video gestures, and photo/video upload quality.
 */
public class LagramMediaSettingsActivity extends LagramBaseSettingsFragment {

    private LagramPreviewViews.MediaLivePreviewView previewView;

    @Override
    protected String getScreenTitle() {
        return "Reproductor y Multimedia";
    }

    private void refreshPreview() {
        if (previewView != null) {
            previewView.invalidate();
        }
    }

    @Override
    protected void buildContent(Context context, LinearLayout root) {
        LinearLayout previewCard = LagramCardUi.createGraphiteCard(context);
        previewView = new LagramPreviewViews.MediaLivePreviewView(context);
        previewCard.addView(previewView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addCardWithMargin(root, previewCard);

        root.addView(LagramCardUi.createSectionHeader(context, "Reproductor de música"));
        LinearLayout playerCard = LagramCardUi.createGraphiteCard(context);
        playerCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Animación de onda de audio",
                "Barra de progreso animada estilo exteraless",
                LagramConfig.playerWaveAnimation,
                v -> {
                    LagramConfig.setPlayerWaveAnimation(v);
                    refreshPreview();
                }
        ));
        playerCard.addView(LagramCardUi.createDivider(context));
        playerCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Paleta dinámica de carátula",
                "Adapta el color de la tarjeta del reproductor al álbum",
                LagramConfig.playerDynamicCoverPalette,
                v -> {
                    LagramConfig.setPlayerDynamicCoverPalette(v);
                    refreshPreview();
                }
        ));
        playerCard.addView(LagramCardUi.createDivider(context));
        playerCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Abrir reproductor clásico",
                "Usa la disposición clásica en lugar del reproductor M3",
                LagramConfig.playerClassicMode,
                v -> {
                    LagramConfig.setPlayerClassicMode(v);
                    refreshPreview();
                }
        ));
        addCardWithMargin(root, playerCard);

        root.addView(LagramCardUi.createSectionHeader(context, "Video y Calidad de envío"));
        LinearLayout mediaCard = LagramCardUi.createGraphiteCard(context);
        mediaCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Gestos en reproductor de video",
                "Deslizar para brillo, volumen y avance rápido",
                LagramConfig.videoGesturesEnabled,
                v -> {
                    LagramConfig.setVideoGesturesEnabled(v);
                    refreshPreview();
                }
        ));
        mediaCard.addView(LagramCardUi.createDivider(context));
        mediaCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Calidad de envío de fotos",
                null,
                new String[]{"Estándar", "Alta", "Ultra HD"},
                LagramConfig.photoUploadQuality,
                LagramConfig::setPhotoUploadQuality
        ));
        mediaCard.addView(LagramCardUi.createDivider(context));
        mediaCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Calidad de envío de video",
                null,
                new String[]{"Estándar", "HD 720p", "Full HD"},
                LagramConfig.videoUploadQuality,
                LagramConfig::setVideoUploadQuality
        ));
        addCardWithMargin(root, mediaCard);

        addResetCategoryButton(context, root, LagramConfig.CATEGORY_MEDIA, "Restablecer Multimedia");
    }
}
