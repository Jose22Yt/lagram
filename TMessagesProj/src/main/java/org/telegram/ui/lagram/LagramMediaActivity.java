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

    private static final int ID_PLAY_IN_ROW = 1;
    private static final int ID_PAUSE_ON_MINIMIZE = 2;

    private static final int ID_MEDIA_GLOW_PHOTOS = 10;
    private static final int ID_MEDIA_GLOW_VIDEOS = 11;
    private static final int ID_MEDIA_GLOW_BRIGHTNESS = 12;

    private static final int ID_REAR_CAMERA_ROUND = 20;
    private static final int ID_COMPACT_ROUND_CAMERA = 21;

    private static final int ID_DOWNLOAD_BOOST = 30;
    private static final int ID_TURBO_UPLOADS = 31;
    private static final int ID_SEND_HD = 32;

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

        // 1. Playback (Exact Opexgram multi-select [✓ Voice] [✓ Round] [✓ Music] pills!)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAutoplayHeader)));
        int playMask = (LagramConfig.autoplayVoiceInRow ? 1 : 0)
                | (LagramConfig.autoplayRoundInRow ? 2 : 0)
                | (LagramConfig.autoplayMusicInRow ? 4 : 0);
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.multi(
                ID_PLAY_IN_ROW,
                LocaleController.getString(R.string.LagramPlayInARow),
                new String[]{
                        LocaleController.getString(R.string.LagramPlayVoiceShort),
                        LocaleController.getString(R.string.LagramPlayRoundShort),
                        LocaleController.getString(R.string.LagramPlayMusicShort)
                },
                playMask,
                idx -> {
                    if (idx == 0) LagramConfig.toggleAutoplayVoiceInRow();
                    else if (idx == 1) LagramConfig.toggleAutoplayRoundInRow();
                    else if (idx == 2) LagramConfig.toggleAutoplayMusicInRow();
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_PAUSE_ON_MINIMIZE, 0,
                LocaleController.getString(R.string.LagramPauseVideoMinimize),
                null,
                LagramConfig.pauseVideoOnMinimize));
        items.add(UItem.asShadow(-1, null));

        // 2. Media Glow (Exact Opexgram Sheet 2 Frame 6: Photos / Videos / Brightness)
        items.add(UItem.asHeader(LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramMediaGlow))));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_MEDIA_GLOW_PHOTOS, 0,
                LocaleController.getString(R.string.LagramMediaGlowPhotos),
                null,
                LagramConfig.mediaGlow));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_MEDIA_GLOW_VIDEOS, 0,
                LocaleController.getString(R.string.LagramMediaGlowVideos),
                null,
                LagramConfig.mediaGlowVideos));
        if (LagramConfig.mediaGlow || LagramConfig.mediaGlowVideos) {
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_MEDIA_GLOW_BRIGHTNESS,
                    LocaleController.getString(R.string.LagramMediaGlowBrightness),
                    new String[]{
                            LocaleController.getString(R.string.LagramMediaGlowSoft),
                            LocaleController.getString(R.string.LagramMediaGlowMedium),
                            LocaleController.getString(R.string.LagramMediaGlowStrong)
                    },
                    LagramConfig.mediaGlowLevel,
                    idx -> {
                        LagramConfig.setMediaGlowLevel(idx);
                        refreshList();
                    }));
        }
        items.add(UItem.asShadow(-2, null));

        // 3. Camera
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMediaHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_REAR_CAMERA_ROUND, R.drawable.msg_camera,
                LocaleController.getString(R.string.LagramRearCameraRound),
                null,
                LagramConfig.rearCameraForRoundVideo));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_COMPACT_ROUND_CAMERA, R.drawable.msg_camera,
                LocaleController.getString(R.string.LagramCompactRoundCamera),
                null,
                LagramConfig.compactRoundCamera));
        items.add(UItem.asShadow(-3, null));

        // 4. Lagram Turbo Network Acceleration
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramNetworkHeader)));
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
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
                    refreshList();
                }));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_TURBO_UPLOADS, R.drawable.msg_speed,
                LocaleController.getString(R.string.LagramTurboUploads),
                null,
                LagramConfig.turboUploads));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SEND_HD, R.drawable.msg_photos,
                LocaleController.getString(R.string.LagramSendPhotosHd),
                null,
                LagramConfig.sendPhotosInHd));
        items.add(UItem.asShadow(-4, LocaleController.getString(R.string.LagramMediaInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_PAUSE_ON_MINIMIZE:
                toggle(view, LagramConfig.togglePauseVideoOnMinimize());
                return;
            case ID_MEDIA_GLOW_PHOTOS:
                toggle(view, LagramConfig.toggleMediaGlow());
                return;
            case ID_MEDIA_GLOW_VIDEOS:
                toggle(view, LagramConfig.toggleMediaGlowVideos());
                return;
            case ID_REAR_CAMERA_ROUND:
                toggle(view, LagramConfig.toggleRearCameraForRoundVideo());
                return;
            case ID_COMPACT_ROUND_CAMERA:
                toggle(view, LagramConfig.toggleCompactRoundCamera());
                return;
            case ID_TURBO_UPLOADS:
                toggle(view, LagramConfig.toggleTurboUploads());
                return;
            case ID_SEND_HD:
                toggle(view, LagramConfig.toggleSendPhotosInHd());
                return;
        }
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
}
