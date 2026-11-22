package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
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
        items.add(UItem.asCheck(ID_TURBO_UPLOADS, LocaleController.getString(R.string.LagramTurboUploads))
                .setChecked(LagramConfig.turboUploads));
        items.add(UItem.asShadow(null));

        // Section 2: Sequential Autoplay
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAutoplayHeader)));
        items.add(UItem.asCheck(ID_AUTOPLAY_VOICE, LocaleController.getString(R.string.LagramAutoplayVoice))
                .setChecked(LagramConfig.autoplayVoiceInRow));
        items.add(UItem.asCheck(ID_AUTOPLAY_ROUND, LocaleController.getString(R.string.LagramAutoplayRound))
                .setChecked(LagramConfig.autoplayRoundInRow));
        items.add(UItem.asCheck(ID_AUTOPLAY_MUSIC, LocaleController.getString(R.string.LagramAutoplayMusic))
                .setChecked(LagramConfig.autoplayMusicInRow));
        items.add(UItem.asShadow(null));

        // Section 3: Camera, HD Photos & Ambient Glow
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMediaHeader)));
        items.add(UItem.asCheck(ID_SEND_PHOTOS_HD, LocaleController.getString(R.string.LagramSendPhotosHd))
                .setChecked(LagramConfig.sendPhotosInHd));
        items.add(UItem.asCheck(ID_REAR_CAMERA_ROUND, LocaleController.getString(R.string.LagramRearCameraRound))
                .setChecked(LagramConfig.rearCameraForRoundVideo));
        items.add(UItem.asCheck(ID_COMPACT_ROUND_CAMERA, LocaleController.getString(R.string.LagramCompactRoundCamera))
                .setChecked(LagramConfig.compactRoundCamera));
        items.add(UItem.asCheck(ID_MEDIA_GLOW, LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramMediaGlow)))
                .setChecked(LagramConfig.mediaGlow));
        items.add(UItem.asCheck(ID_PAUSE_VIDEO_MINIMIZE, LocaleController.getString(R.string.LagramPauseVideoMinimize))
                .setChecked(LagramConfig.pauseVideoOnMinimize));
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
        if (checkChanged && view instanceof TextCheckCell) {
            ((TextCheckCell) view).setChecked(newVal);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
