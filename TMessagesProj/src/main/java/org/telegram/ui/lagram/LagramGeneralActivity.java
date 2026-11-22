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

public class LagramGeneralActivity extends UniversalFragment {

    private static final int ID_SHOW_ID_DC = 1;
    private static final int ID_ID_BOT_API = 2;
    private static final int ID_SHOW_JOIN_DATE = 3;
    private static final int ID_PROFILE_NAME_SWITCH = 4;
    private static final int ID_HIDE_PHONE_NUMBER = 5;

    private static final int ID_HIGH_REFRESH = 10;
    private static final int ID_PERF_CLASS = 11;
    private static final int ID_HAPTICS_LEVEL = 12;

    private static final int ID_OPEN_ARCHIVE_PULL = 20;
    private static final int ID_DISABLE_SPONSORED = 21;
    private static final int ID_HIDE_SIMILAR = 22;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryGeneral);
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

        // Card 1: Profile & Peer Identifiers
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramDetailsHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SHOW_ID_DC,
                R.drawable.msg_info,
                LocaleController.getString(R.string.LagramShowIdAndDc),
                null,
                LagramConfig.showIdAndDc));
        if (LagramConfig.showIdAndDc) {
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_ID_BOT_API,
                    R.drawable.msg_bot,
                    LocaleController.getString(R.string.LagramIdFormatBotApi),
                    null,
                    LagramConfig.idFormatBotApi));
        }
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SHOW_JOIN_DATE,
                R.drawable.msg_calendar2,
                LocaleController.getString(R.string.LagramShowJoinDate),
                null,
                LagramConfig.showJoinDate));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_PROFILE_NAME_SWITCH,
                R.drawable.msg_copy,
                LocaleController.getString(R.string.LagramProfileNameSwitch),
                LocaleController.getString(R.string.LagramProfileNameSwitchInfo),
                LagramConfig.profileNameSwitch));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_HIDE_PHONE_NUMBER,
                R.drawable.msg_calls,
                LocaleController.getString(R.string.LagramHidePhoneNumber),
                null,
                LagramConfig.hidePhoneNumber));
        items.add(UItem.asShadow(-1, null));

        // Card 2: Performance & Haptics
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramPerformanceHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_HIGH_REFRESH,
                R.drawable.msg_speed,
                LocaleController.getString(R.string.LagramHighRefreshRate),
                null,
                LagramConfig.highRefreshRateScroll));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                ID_PERF_CLASS,
                LocaleController.getString(R.string.LagramForcePerfClass),
                new String[]{
                        LocaleController.getString(R.string.LagramPerfAuto),
                        LocaleController.getString(R.string.LagramPerfLow),
                        LocaleController.getString(R.string.LagramPerfAverage),
                        LocaleController.getString(R.string.LagramPerfHigh)
                },
                LagramConfig.forcePerformanceClass + 1,
                idx -> {
                    LagramConfig.setForcePerformanceClass(idx - 1);
                    refreshList();
                }));
        items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                ID_HAPTICS_LEVEL,
                LocaleController.getString(R.string.LagramHapticsLevel),
                new String[]{
                        LocaleController.getString(R.string.LagramHapticsOff),
                        LocaleController.getString(R.string.LagramHapticsSoft),
                        LocaleController.getString(R.string.LagramHapticsNormal),
                        LocaleController.getString(R.string.LagramHapticsStrong)
                },
                LagramConfig.hapticsLevel,
                idx -> {
                    LagramConfig.setHapticsLevel(idx);
                    refreshList();
                }));
        items.add(UItem.asShadow(-2, null));

        // Card 3: Behavior & Clean Feed
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramBehaviorHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_OPEN_ARCHIVE_PULL,
                R.drawable.msg_archive,
                LocaleController.getString(R.string.LagramOpenArchiveOnPull),
                null,
                LagramConfig.openArchiveOnPull));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_DISABLE_SPONSORED,
                R.drawable.msg_block,
                LocaleController.getString(R.string.LagramDisableSponsored),
                null,
                LagramConfig.disableSponsoredMessages));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_HIDE_SIMILAR,
                R.drawable.msg_channel,
                LocaleController.getString(R.string.LagramHideSimilarChannels),
                null,
                LagramConfig.hideSimilarChannels));
        items.add(UItem.asShadow(-3, null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_SHOW_ID_DC:
                LagramConfig.toggleShowIdAndDc();
                refreshList();
                return;
            case ID_ID_BOT_API:
                toggleSwitch(view, LagramConfig.toggleIdFormatBotApi());
                return;
            case ID_SHOW_JOIN_DATE:
                toggleSwitch(view, LagramConfig.toggleShowJoinDate());
                return;
            case ID_PROFILE_NAME_SWITCH:
                toggleSwitch(view, LagramConfig.toggleProfileNameSwitch());
                return;
            case ID_HIDE_PHONE_NUMBER:
                toggleSwitch(view, LagramConfig.toggleHidePhoneNumber());
                return;
            case ID_HIGH_REFRESH:
                toggleSwitch(view, LagramConfig.toggleHighRefreshRateScroll());
                return;
            case ID_OPEN_ARCHIVE_PULL:
                toggleSwitch(view, LagramConfig.toggleOpenArchiveOnPull());
                return;
            case ID_DISABLE_SPONSORED:
                toggleSwitch(view, LagramConfig.toggleDisableSponsoredMessages());
                return;
            case ID_HIDE_SIMILAR:
                toggleSwitch(view, LagramConfig.toggleHideSimilarChannels());
                return;
        }
    }

    private void toggleSwitch(View view, boolean checked) {
        if (view instanceof LagramSettingsComponents.IconSwitchCell) {
            ((LagramSettingsComponents.IconSwitchCell) view).setChecked(checked, true);
        }
        refreshList();
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
}
