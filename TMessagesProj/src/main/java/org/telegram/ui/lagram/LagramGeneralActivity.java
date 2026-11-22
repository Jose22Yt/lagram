package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class LagramGeneralActivity extends UniversalFragment {

    // Profile
    private static final int ID_HIDE_PHONE_NUMBER = 1;
    private static final int ID_SHOW_ID_DC = 2;
    private static final int ID_ID_BOT_API = 3;
    private static final int ID_SHOW_JOIN_DATE = 4;
    private static final int ID_PROFILE_NAME_SWITCH = 5;

    // Stories & Archive
    private static final int ID_HIDE_STORIES = 10;
    private static final int ID_HIDE_STORIES_PROFILE = 11;
    private static final int ID_HIDE_STORY_POSTING = 12;
    private static final int ID_OPEN_ARCHIVE_PULL = 13;

    // Performance & Haptics
    private static final int ID_HIGH_REFRESH = 30;
    private static final int ID_PERF_CLASS = 31;
    private static final int ID_HAPTICS_LEVEL = 32;

    // Cleanup
    private static final int ID_DISABLE_SPONSORED = 40;
    private static final int ID_HIDE_SIMILAR = 41;

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

        // 1. Profile
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramDetailsHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_PHONE_NUMBER, 0,
                LocaleController.getString(R.string.LagramHidePhoneNumber),
                null,
                LagramConfig.hidePhoneNumber));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_ID_DC, 0,
                LocaleController.getString(R.string.LagramShowIdAndDc),
                null,
                LagramConfig.showIdAndDc));
        if (LagramConfig.showIdAndDc) {
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_ID_BOT_API, 0,
                    LocaleController.getString(R.string.LagramIdFormatBotApi),
                    null,
                    LagramConfig.idFormatBotApi));
        }
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SHOW_JOIN_DATE, 0,
                LocaleController.getString(R.string.LagramShowJoinDate),
                null,
                LagramConfig.showJoinDate));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_PROFILE_NAME_SWITCH, 0,
                LocaleController.getString(R.string.LagramProfileNameSwitch),
                LocaleController.getString(R.string.LagramProfileNameSwitchInfo),
                LagramConfig.profileNameSwitch));
        items.add(UItem.asShadow(-1, null));

        // 2. Stories & Archive
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramStoriesArchiveHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_STORIES, 0,
                LocaleController.getString(R.string.LagramHideStories),
                null,
                LagramConfig.hideStories));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_STORIES_PROFILE, 0,
                LocaleController.getString(R.string.LagramHideStoriesProfile),
                null,
                LagramConfig.hideStoriesInProfile));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_STORY_POSTING, 0,
                LocaleController.getString(R.string.LagramHideStoryPosting),
                null,
                LagramConfig.hideStoryPosting));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_OPEN_ARCHIVE_PULL, 0,
                LocaleController.getString(R.string.LagramOpenArchiveOnPull),
                null,
                LagramConfig.openArchiveOnPull));
        items.add(UItem.asShadow(-2, null));

        // 3. Performance & Haptics
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramPerformanceHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIGH_REFRESH, 0,
                LocaleController.getString(R.string.LagramHighRefreshRate),
                null,
                LagramConfig.highRefreshRateScroll));
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
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
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
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
        items.add(UItem.asShadow(-4, LocaleController.getString(R.string.LagramGeneralInfo)));

        // 4. Cleanup
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAntiBloatHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_DISABLE_SPONSORED, 0,
                LocaleController.getString(R.string.LagramDisableSponsored),
                null,
                LagramConfig.disableSponsoredMessages));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_HIDE_SIMILAR, 0,
                LocaleController.getString(R.string.LagramHideSimilarChannels),
                null,
                LagramConfig.hideSimilarChannels));
        items.add(UItem.asShadow(-5, null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_HIDE_PHONE_NUMBER:
                toggle(view, LagramConfig.toggleHidePhoneNumber());
                return;
            case ID_SHOW_ID_DC:
                toggle(view, LagramConfig.toggleShowIdAndDc());
                return;
            case ID_ID_BOT_API:
                toggle(view, LagramConfig.toggleIdFormatBotApi());
                return;
            case ID_SHOW_JOIN_DATE:
                toggle(view, LagramConfig.toggleShowJoinDate());
                return;
            case ID_PROFILE_NAME_SWITCH:
                toggle(view, LagramConfig.toggleProfileNameSwitch());
                return;
            case ID_HIDE_STORIES:
                toggle(view, LagramConfig.toggleHideStories());
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.storiesUpdated);
                return;
            case ID_HIDE_STORIES_PROFILE:
                toggle(view, LagramConfig.toggleHideStoriesInProfile());
                return;
            case ID_HIDE_STORY_POSTING:
                toggle(view, LagramConfig.toggleHideStoryPosting());
                return;
            case ID_OPEN_ARCHIVE_PULL:
                toggle(view, LagramConfig.toggleOpenArchiveOnPull());
                return;
            case ID_HIGH_REFRESH:
                toggle(view, LagramConfig.toggleHighRefreshRateScroll());
                return;
            case ID_DISABLE_SPONSORED:
                toggle(view, LagramConfig.toggleDisableSponsoredMessages());
                return;
            case ID_HIDE_SIMILAR:
                toggle(view, LagramConfig.toggleHideSimilarChannels());
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
