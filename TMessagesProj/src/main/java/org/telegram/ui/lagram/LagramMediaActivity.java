package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class LagramMediaActivity extends UniversalFragment {

    private static final int ID_DOWNLOAD_BOOST = 1;
    private static final int ID_TURBO_UPLOADS = 2;

    private static final int ID_AUTOPLAY_VOICE = 10;
    private static final int ID_AUTOPLAY_ROUND = 11;
    private static final int ID_AUTOPLAY_MUSIC = 12;

    private static final int ID_SEND_PHOTOS_HD = 20;
    private static final int ID_REAR_CAMERA_ROUND = 21;
    private static final int ID_COMPACT_ROUND_CAMERA = 22;
    private static final int ID_MEDIA_GLOW = 23;
    private static final int ID_PAUSE_VIDEO_MINIMIZE = 24;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryMedia);
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
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // Section 1: Network & Turbo Boost
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramNetworkHeader)));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                ID_DOWNLOAD_BOOST,
                LocaleController.getString(R.string.LagramDownloadBoost),
                new String[]{
                        LocaleController.getString(R.string.LagramBoostOff),
                        LocaleController.getString(R.string.LagramBoostFast),
                        LocaleController.getString(R.string.LagramBoostExtreme)
                },
                LagramConfig.downloadSpeedBoost,
                LagramConfig::setDownloadSpeedBoost
        ));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_TURBO_UPLOADS,
                R.drawable.msg_send,
                LocaleController.getString(R.string.LagramTurboUploads),
                "Aumentar fragmentos paralelos al subir archivos y videos",
                LagramConfig.turboUploads
        ));
        items.add(UItem.asShadow(null));

        // Section 2: Sequential Autoplay
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAutoplayHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_AUTOPLAY_VOICE,
                R.drawable.msg_filled_data_voice,
                LocaleController.getString(R.string.LagramAutoplayVoice),
                "Continuar automáticamente con la siguiente nota de voz del chat",
                LagramConfig.autoplayVoiceInRow
        ));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_AUTOPLAY_ROUND,
                R.drawable.msg_video,
                LocaleController.getString(R.string.LagramAutoplayRound),
                "Reproducir el siguiente videomensaje circular al terminar",
                LagramConfig.autoplayRoundInRow
        ));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_AUTOPLAY_MUSIC,
                R.drawable.msg_filled_data_music,
                LocaleController.getString(R.string.LagramAutoplayMusic),
                "Continuar con la siguiente pista de audio en la lista",
                LagramConfig.autoplayMusicInRow
        ));
        items.add(UItem.asShadow(null));

        // Section 3: Camera, HD Photos & Ambient Glow
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMediaHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SEND_PHOTOS_HD,
                R.drawable.msg_photos,
                LocaleController.getString(R.string.LagramSendPhotosHd),
                "Enviar imágenes en resolución HD (2560px) por defecto",
                LagramConfig.sendPhotosInHd
        ));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_REAR_CAMERA_ROUND,
                R.drawable.msg_camera,
                LocaleController.getString(R.string.LagramRearCameraRound),
                "Iniciar grabación de videomensajes con la cámara trasera",
                LagramConfig.rearCameraForRoundVideo
        ));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_COMPACT_ROUND_CAMERA,
                R.drawable.msg_video,
                LocaleController.getString(R.string.LagramCompactRoundCamera),
                "Vista previa compacta al grabar videomensajes circulares",
                LagramConfig.compactRoundCamera
        ));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_MEDIA_GLOW,
                R.drawable.msg_theme,
                LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramMediaGlow)),
                "Iluminación ambiental dinámica en el visor de fotos y videos",
                LagramConfig.mediaGlow
        ));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_PAUSE_VIDEO_MINIMIZE,
                R.drawable.msg_played,
                LocaleController.getString(R.string.LagramPauseVideoMinimize),
                "Pausar reproducción de video al salir de Lagram",
                LagramConfig.pauseVideoOnMinimize
        ));
        items.add(UItem.asShadow(LocaleController.getString(R.string.LagramMediaInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        boolean checkChanged = false;
        boolean newVal = false;
        switch (item.id) {
            case ID_TURBO_UPLOADS:
                newVal = LagramConfig.toggleTurboUploads();
                checkChanged = true;
                break;
            case ID_AUTOPLAY_VOICE:
                newVal = LagramConfig.toggleAutoplayVoiceInRow();
                checkChanged = true;
                break;
            case ID_AUTOPLAY_ROUND:
                newVal = LagramConfig.toggleAutoplayRoundInRow();
                checkChanged = true;
                break;
            case ID_AUTOPLAY_MUSIC:
                newVal = LagramConfig.toggleAutoplayMusicInRow();
                checkChanged = true;
                break;
            case ID_SEND_PHOTOS_HD:
                newVal = LagramConfig.toggleSendPhotosInHd();
                checkChanged = true;
                break;
            case ID_REAR_CAMERA_ROUND:
                newVal = LagramConfig.toggleRearCameraForRoundVideo();
                checkChanged = true;
                break;
            case ID_COMPACT_ROUND_CAMERA:
                newVal = LagramConfig.toggleCompactRoundCamera();
                checkChanged = true;
                break;
            case ID_MEDIA_GLOW:
                newVal = LagramConfig.toggleMediaGlow();
                checkChanged = true;
                break;
            case ID_PAUSE_VIDEO_MINIMIZE:
                newVal = LagramConfig.togglePauseVideoOnMinimize();
                checkChanged = true;
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
