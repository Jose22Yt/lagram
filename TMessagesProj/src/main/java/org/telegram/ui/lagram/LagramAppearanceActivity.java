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

/**
 * Clean Appearance & Motion category screen segmented into 3 rounded cards:
 * 1. Lagram App Icon (6 redesigned icons)
 * 2. Liquid Glass for chat bubbles (real wallpaper preview + full customization)
 * 3. 3D Scroll Animation (live draggable preview + mode selector)
 */
public class LagramAppearanceActivity extends UniversalFragment {

    private static final int ID_APP_ICON_SELECTOR = 100;

    private static final int ID_PREVIEW_GLASS = 200;
    private static final int ID_GLASS_MESSAGES = 201;
    private static final int ID_GLASS_TINT = 202;
    private static final int ID_GLASS_ALPHA = 203;
    private static final int ID_GLASS_BLUR_LEVEL = 204;
    private static final int ID_GLASS_BORDER = 205;
    private static final int ID_GLASS_BORDER_OPACITY = 206;
    private static final int ID_GLASS_HIGHLIGHTS = 207;

    private static final int ID_PREVIEW_SCROLL = 300;
    private static final int ID_SCROLL_EDGE = 301;
    private static final int ID_SCROLL_MODE = 302;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryAppearance);
    }

    @Override
    public View createView(Context context) {
        LagramConfig.ensureLoaded();
        View view = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        int bg = Theme.getColor(Theme.key_windowBackgroundGray);
        view.setBackgroundColor(bg);
        listView.setBackgroundColor(bg);
        return view;
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

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // Card 1: Lagram App Icon (6 redesigned icons)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAppIconHeader)));
        items.add(LagramRealPreviews.RealAppIconSelectorCell.Factory.of(ID_APP_ICON_SELECTOR));
        items.add(UItem.asShadow(-1, null));

        // Card 2: Liquid Glass for Chat Bubbles
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramGlassBlurHeader)));
        items.add(LagramRealPreviews.RealBubblesGlassPreviewCell.Factory.of(ID_PREVIEW_GLASS));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_GLASS_MESSAGES, 0,
                LocaleController.getString(R.string.LagramGlassMessages),
                null,
                LagramConfig.glassMessages));
        if (LagramConfig.glassMessages) {
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_GLASS_TINT,
                    LocaleController.getString(R.string.LagramGlassTintMode),
                    new String[]{
                            LocaleController.getString(R.string.LagramGlassTintTheme),
                            LocaleController.getString(R.string.LagramGlassTintCrystal),
                            LocaleController.getString(R.string.LagramGlassTintFrost),
                            LocaleController.getString(R.string.LagramGlassTintSmoked)
                    },
                    LagramConfig.glassTintMode,
                    idx -> {
                        LagramConfig.setGlassTintMode(idx);
                        refreshList();
                    }));
            items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                    ID_GLASS_ALPHA,
                    LocaleController.getString(R.string.LagramGlassMessagesAlpha),
                    15, 95, 52, LagramConfig.glassMessagesAlpha, "%",
                    val -> {
                        LagramConfig.setGlassMessagesAlpha(val);
                        refreshList();
                    }));
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_GLASS_BLUR_LEVEL,
                    LocaleController.getString(R.string.LagramGlassBlurLevel),
                    new String[]{
                            LocaleController.getString(R.string.LagramGlassBlurSoft),
                            LocaleController.getString(R.string.LagramGlassBlurMedium),
                            LocaleController.getString(R.string.LagramGlassBlurStrong)
                    },
                    LagramConfig.glassBlurLevel,
                    idx -> {
                        LagramConfig.setGlassBlurLevel(idx);
                        LagramGlassBubbleEngine.invalidateCapture();
                        refreshList();
                    }));
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GLASS_BORDER, 0,
                    LocaleController.getString(R.string.LagramGlassMessagesBorder),
                    null,
                    LagramConfig.glassMessagesBorder));
            if (LagramConfig.glassMessagesBorder) {
                items.add(OpexgramStyleWidgets.OpexSliderCell.Factory.of(
                        ID_GLASS_BORDER_OPACITY,
                        LocaleController.getString(R.string.LagramGlassBorderOpacity),
                        10, 100, 60, LagramConfig.glassBorderOpacity, "%",
                        val -> {
                            LagramConfig.setGlassBorderOpacity(val);
                            refreshList();
                        }));
            }
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GLASS_HIGHLIGHTS, 0,
                    LocaleController.getString(R.string.LagramGlassHighlights),
                    null,
                    LagramConfig.glassHighlights));
        }
        items.add(UItem.asShadow(-2, null));

        // Card 3: 3D Scroll Animation
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramScrollAndMotionHeader)));
        items.add(LagramRealPreviews.RealScrollEffectPreviewCell.Factory.of(ID_PREVIEW_SCROLL));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_SCROLL_EDGE, 0,
                LocaleController.getString(R.string.LagramScrollEdgeScale),
                null,
                LagramConfig.scrollEdgeScale));
        if (LagramConfig.scrollEdgeScale) {
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_SCROLL_MODE,
                    LocaleController.getString(R.string.LagramScrollEdgeMode),
                    new String[]{
                            LocaleController.getString(R.string.LagramScrollEdgeModeScale),
                            LocaleController.getString(R.string.LagramScrollEdgeModeDrum)
                    },
                    LagramConfig.scrollEdgeMode,
                    idx -> {
                        LagramConfig.setScrollEdgeMode(idx);
                        refreshList();
                    }));
        }
        items.add(UItem.asShadow(-3, null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_GLASS_MESSAGES:
                LagramConfig.toggleGlassMessages();
                LagramGlassBubbleEngine.invalidateCapture();
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewTheme, false);
                refreshList();
                break;
            case ID_GLASS_BORDER:
                LagramConfig.toggleGlassMessagesBorder();
                refreshList();
                break;
            case ID_GLASS_HIGHLIGHTS:
                LagramConfig.toggleGlassHighlights();
                refreshList();
                break;
            case ID_SCROLL_EDGE:
                LagramConfig.toggleScrollEdgeScale();
                refreshList();
                break;
        }
    }
}
