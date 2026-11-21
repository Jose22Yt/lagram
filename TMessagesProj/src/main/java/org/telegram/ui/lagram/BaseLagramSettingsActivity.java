package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public abstract class BaseLagramSettingsActivity extends BaseFragment {

    protected static final int VIEW_TYPE_HEADER = 0;
    protected static final int VIEW_TYPE_CHECK = 1;
    protected static final int VIEW_TYPE_INFO = 2;
    protected static final int VIEW_TYPE_SHADOW = 3;
    protected static final int VIEW_TYPE_SETTINGS = 4;
    protected static final int VIEW_TYPE_TEXT_ICON = 5;

    protected RecyclerListView listView;
    protected ListAdapter listAdapter;
    protected int rowCount;

    protected abstract String getActionBarTitle();
    protected abstract void updateRows();
    protected abstract int getItemViewType(int position);
    protected abstract void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position);
    protected abstract void onItemClick(View view, int position);

    @Override
    public boolean onFragmentCreate() {
        LagramConfig.ensureLoaded();
        updateRows();
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getActionBarTitle());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        updateBackdropColor();

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setCaptureSectionsDecoratorAllowed(true);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        listView.setAdapter(listAdapter = new ListAdapter(context));
        listView.setOnItemClickListener(this::onItemClick);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    protected void updateBackdropColor() {
        if (fragmentView == null) return;
        LagramConfig.ensureLoaded();
        if (LagramConfig.amoledSettingsBackdrop) {
            fragmentView.setBackgroundColor(Theme.isCurrentThemeDark() ? 0xFF000000 : 0xFFFFFFFF);
        } else {
            fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        }
    }

    protected void showSelectorDialog(String title, String[] items, int selectedIndex, OnSelectCallback callback) {
        if (getParentActivity() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);
        builder.setItems(items, (dialog, which) -> {
            callback.onSelect(which);
            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    protected interface OnSelectCallback {
        void onSelect(int which);
    }

    protected class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == VIEW_TYPE_CHECK || type == VIEW_TYPE_SETTINGS || type == VIEW_TYPE_TEXT_ICON;
        }

        @Override
        public int getItemViewType(int position) {
            return BaseLagramSettingsActivity.this.getItemViewType(position);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    view = new HeaderCell(context);
                    break;
                case VIEW_TYPE_CHECK:
                    view = new TextCheckCell(context);
                    break;
                case VIEW_TYPE_INFO:
                    view = new TextInfoPrivacyCell(context);
                    break;
                case VIEW_TYPE_SETTINGS:
                    view = new TextSettingsCell(context);
                    break;
                case VIEW_TYPE_TEXT_ICON:
                    view = new TextCell(context);
                    break;
                default:
                    view = new ShadowSectionCell(context);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            LagramConfig.ensureLoaded();
            BaseLagramSettingsActivity.this.onBindViewHolder(holder, position);
        }
    }
}
