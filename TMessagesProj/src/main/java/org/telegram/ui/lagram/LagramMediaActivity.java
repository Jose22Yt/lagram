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
    private static final int ID_SEND_HD = 3;

    private static final int ID_MEDIA_GLOW = 10;
    private static final int ID_AUTOPLAY_VOICE = 11;
    private static final int ID_AUTOPLAY_ROUND = 12;
    private static final int ID_AUTOPLAY_MUSIC = 13;
    private static final int ID_PAUSE_ON_MINIMIZE = 14;

    private static final int ID_REAR_CAMERA_ROUND = 20;
    private static final int ID_COMPACT_ROUND_CAMERA = 21;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryMedia);
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

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // Card 1: Network & Turbo Transfers
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
                idx -> {
                    LagramConfig.setDownloadSpeedBoost(idx);
                    if (listView != null && listView.adapter != null) {
                        listView.adapter.update(true);
                    }
                }));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_TURBO_UPLOADS,
                R.drawable.msg_speed,
                LocaleController.getString(R.string.LagramTurboUploads),
                null,
                LagramConfig.turboUploads));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SEND_HD,
                R.drawable.msg_photos,
                LocaleController.getString(R.string.LagramSendPhotosHd),
                null,
                LagramConfig.sendPhotosInHd));
        items.add(UItem.asShadow(-1, null));

        // Card 2: Playback & Ambient Glow
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAutoplayHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_MEDIA_GLOW,
                R.drawable.msg_photo_blur,
                LocaleController.getString(R.string.LagramMediaGlow),
                null,
                LagramConfig.mediaGlow));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_AUTOPLAY_VOICE,
                R.drawable.msg_voicechat,
                LocaleController.getString(R.string.LagramAutoplayVoice),
                null,
                LagramConfig.autoplayVoiceInRow));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_AUTOPLAY_ROUND,
                R.drawable.msg_videocall,
                LocaleController.getString(R.string.LagramAutoplayRound),
                null,
                LagramConfig.autoplayRoundInRow));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_AUTOPLAY_MUSIC,
                R.drawable.msg_played,
                LocaleController.getString(R.string.LagramAutoplayMusic),
                null,
                LagramConfig.autoplayMusicInRow));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_PAUSE_ON_MINIMIZE,
                R.drawable.msg_media,
                LocaleController.getString(R.string.LagramPauseVideoMinimize),
                null,
                LagramConfig.pauseVideoOnMinimize));
        items.add(UItem.asShadow(-2, null));

        // Card 3: Round Video Camera
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMediaHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_REAR_CAMERA_ROUND,
                R.drawable.msg_camera,
                LocaleController.getString(R.string.LagramRearCameraRound),
                null,
                LagramConfig.rearCameraForRoundVideo));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_COMPACT_ROUND_CAMERA,
                R.drawable.msg_camera,
                LocaleController.getString(R.string.LagramCompactRoundCamera),
                null,
                LagramConfig.compactRoundCamera));
        items.add(UItem.asShadow(-3, null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_TURBO_UPLOADS:
                toggleSwitch(view, LagramConfig.toggleTurboUploads());
                return;
            case ID_SEND_HD:
                toggleSwitch(view, LagramConfig.toggleSendPhotosInHd());
                return;
            case ID_MEDIA_GLOW:
                toggleSwitch(view, LagramConfig.toggleMediaGlow());
                return;
            case ID_AUTOPLAY_VOICE:
                toggleSwitch(view, LagramConfig.toggleAutoplayVoiceInRow());
                return;
            case ID_AUTOPLAY_ROUND:
                toggleSwitch(view, LagramConfig.toggleAutoplayRoundInRow());
                return;
            case ID_AUTOPLAY_MUSIC:
                toggleSwitch(view, LagramConfig.toggleAutoplayMusicInRow());
                return;
            case ID_PAUSE_ON_MINIMIZE:
                toggleSwitch(view, LagramConfig.togglePauseVideoOnMinimize());
                return;
            case ID_REAR_CAMERA_ROUND:
                toggleSwitch(view, LagramConfig.toggleRearCameraForRoundVideo());
                return;
            case ID_COMPACT_ROUND_CAMERA:
                toggleSwitch(view, LagramConfig.toggleCompactRoundCamera());
                return;
        }
    }

    private void toggleSwitch(View view, boolean checked) {
        if (view instanceof LagramSettingsComponents.IconSwitchCell) {
            ((LagramSettingsComponents.IconSwitchCell) view).setChecked(checked, true);
        }
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
