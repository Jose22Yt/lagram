package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Base fragment for Lagram Settings screens using Graphite cards and smooth scrolling.
 */
public abstract class LagramBaseSettingsFragment extends BaseFragment {

    protected LinearLayout contentLayout;
    protected ScrollView scrollView;

    protected abstract String getScreenTitle();

    protected abstract void buildContent(Context context, LinearLayout root);

    @Override
    public View createView(Context context) {
        LagramConfig.ensureLoaded();
        boolean isDark = Theme.isCurrentThemeDark();
        int bgColor = LagramThemeEngine.getSettingsBackgroundColor(isDark);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getScreenTitle());
        actionBar.setBackgroundColor(bgColor);
        actionBar.setTitleColor(isDark ? LagramThemeEngine.GRAPHITE_TEXT_PRIMARY : 0xFF1A1D24);
        actionBar.setItemsColor(isDark ? LagramThemeEngine.GRAPHITE_TEXT_PRIMARY : 0xFF1A1D24, false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(bgColor);
        scrollView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(10), AndroidUtilities.dp(14), AndroidUtilities.dp(28));
        scrollView.addView(contentLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0));

        buildContent(context, contentLayout);

        fragmentView = scrollView;
        return fragmentView;
    }

    protected void rebuildContent(Context context) {
        if (contentLayout == null) {
            return;
        }
        boolean isDark = Theme.isCurrentThemeDark();
        int bgColor = LagramThemeEngine.getSettingsBackgroundColor(isDark);
        if (scrollView != null) {
            scrollView.setBackgroundColor(bgColor);
        }
        if (actionBar != null) {
            actionBar.setBackgroundColor(bgColor);
            actionBar.setTitleColor(isDark ? LagramThemeEngine.GRAPHITE_TEXT_PRIMARY : 0xFF1A1D24);
            actionBar.setItemsColor(isDark ? LagramThemeEngine.GRAPHITE_TEXT_PRIMARY : 0xFF1A1D24, false);
        }
        contentLayout.removeAllViews();
        buildContent(context, contentLayout);
    }

    protected void addCardWithMargin(LinearLayout root, View card) {
        root.addView(card, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));
    }

    protected void addResetCategoryButton(Context context, LinearLayout root, String category, String label) {
        LinearLayout card = LagramCardUi.createGraphiteCard(context);
        card.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(12), AndroidUtilities.dp(12), AndroidUtilities.dp(12));
        View resetBtn = LagramCardUi.createActionButton(
                context,
                label,
                LagramThemeEngine.withAlpha(0xFFE54D66, 42),
                0xFFFF7A8F,
                () -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Restablecer categoría");
                    builder.setMessage("¿Deseas restaurar los valores predeterminados de esta sección?");
                    builder.setPositiveButton("Restablecer", (d, w) -> {
                        LagramConfig.resetCategory(category);
                        rebuildContent(context);
                    });
                    builder.setNegativeButton("Cancelar", null);
                    showDialog(builder.create());
                }
        );
        card.addView(resetBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addCardWithMargin(root, card);
    }
}
