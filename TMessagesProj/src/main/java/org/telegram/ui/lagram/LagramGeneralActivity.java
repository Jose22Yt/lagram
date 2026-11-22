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

public class LagramGeneralActivity extends UniversalFragment {

    private static final int ID_HIGH_REFRESH = 1;
    private static final int ID_PERF_CLASS = 2;
    private static final int ID_HAPTICS_LEVEL = 3;

    private static final int ID_SHOW_JOIN_DATE = 10;
    private static final int ID_OPEN_ARCHIVE_PULL = 11;

    private static final int ID_DISABLE_SPONSORED = 20;
    private static final int ID_HIDE_SIMILAR_CHANNELS = 21;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryGeneral);
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

        // Section 1: Performance & Haptics with Segmented Pill Selectors
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramPerformanceHeader)));
        items.add(UItem.asCheck(ID_HIGH_REFRESH, LocaleController.getString(R.string.LagramHighRefreshRate))
                .setChecked(LagramConfig.highRefreshRateScroll));
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
                which -> LagramConfig.setForcePerformanceClass(which - 1)
        ));
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
                LagramConfig::setHapticsLevel
        ));
        items.add(UItem.asShadow(null));

        // Section 2: Profile & Chat List Behavior
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramBehaviorHeader)));
        items.add(UItem.asCheck(ID_SHOW_JOIN_DATE, LocaleController.getString(R.string.LagramShowJoinDate))
                .setChecked(LagramConfig.showJoinDate));
        items.add(UItem.asCheck(ID_OPEN_ARCHIVE_PULL, LocaleController.getString(R.string.LagramOpenArchiveOnPull))
                .setChecked(LagramConfig.openArchiveOnPull));
        items.add(UItem.asShadow(null));

        // Section 3: Anti-Bloat & Sponsored Cleanup
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAntiBloatHeader)));
        items.add(UItem.asCheck(ID_DISABLE_SPONSORED, LocaleController.getString(R.string.LagramDisableSponsored))
                .setChecked(LagramConfig.disableSponsoredMessages));
        items.add(UItem.asCheck(ID_HIDE_SIMILAR_CHANNELS, LocaleController.getString(R.string.LagramHideSimilarChannels))
                .setChecked(LagramConfig.hideSimilarChannels));
        items.add(UItem.asShadow(LocaleController.getString(R.string.LagramGeneralInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        boolean checkChanged = false;
        boolean newVal = false;
        switch (item.id) {
            case ID_HIGH_REFRESH:
                newVal = LagramConfig.toggleHighRefreshRateScroll();
                checkChanged = true;
                break;
            case ID_SHOW_JOIN_DATE:
                newVal = LagramConfig.toggleShowJoinDate();
                checkChanged = true;
                break;
            case ID_OPEN_ARCHIVE_PULL:
                newVal = LagramConfig.toggleOpenArchiveOnPull();
                checkChanged = true;
                break;
            case ID_DISABLE_SPONSORED:
                newVal = LagramConfig.toggleDisableSponsoredMessages();
                checkChanged = true;
                break;
            case ID_HIDE_SIMILAR_CHANNELS:
                newVal = LagramConfig.toggleHideSimilarChannels();
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
