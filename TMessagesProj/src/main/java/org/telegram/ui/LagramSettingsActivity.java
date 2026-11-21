package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public class LagramSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int rowCount;
    private int interfaceHeaderRow;
    private int ios27BottomBarRow;
    private int disableSponsoredRow;
    private int interfaceInfoRow;
    private int sectionDividerRow;
    private int performanceHeaderRow;
    private int highRefreshRateScrollRow;
    private int turboDownloadsRow;
    private int performanceInfoRow;

    @Override
    public boolean onFragmentCreate() {
        LagramConfig.ensureLoaded();
        rowCount = 0;
        interfaceHeaderRow = rowCount++;
        ios27BottomBarRow = rowCount++;
        disableSponsoredRow = rowCount++;
        interfaceInfoRow = rowCount++;
        sectionDividerRow = rowCount++;
        performanceHeaderRow = rowCount++;
        highRefreshRateScrollRow = rowCount++;
        turboDownloadsRow = rowCount++;
        performanceInfoRow = rowCount++;
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Ajustes de Lagram");
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
        frameLayout.setBackgroundColor(Theme.isCurrentThemeDark() ? 0xFF000000 : 0xFFFFFFFF);

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setCaptureSectionsDecoratorAllowed(true);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        listView.setAdapter(listAdapter = new ListAdapter(context));
        listView.setOnItemClickListener((view, position) -> {
            if (!(view instanceof TextCheckCell)) {
                return;
            }
            LagramConfig.ensureLoaded();
            if (position == ios27BottomBarRow) {
                LagramConfig.toggleIos27BottomBar();
                ((TextCheckCell) view).setChecked(LagramConfig.ios27BottomBar);
            } else if (position == disableSponsoredRow) {
                LagramConfig.toggleDisableSponsoredMessages();
                ((TextCheckCell) view).setChecked(LagramConfig.disableSponsoredMessages);
            } else if (position == highRefreshRateScrollRow) {
                LagramConfig.toggleHighRefreshRateScroll();
                ((TextCheckCell) view).setChecked(LagramConfig.highRefreshRateScroll);
            } else if (position == turboDownloadsRow) {
                LagramConfig.toggleTurboDownloads();
                ((TextCheckCell) view).setChecked(LagramConfig.turboDownloads);
            }
        });
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

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
            return holder.getItemViewType() == 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == interfaceHeaderRow || position == performanceHeaderRow) {
                return 0;
            } else if (position == ios27BottomBarRow || position == disableSponsoredRow
                    || position == highRefreshRateScrollRow || position == turboDownloadsRow) {
                return 1;
            } else if (position == interfaceInfoRow || position == performanceInfoRow) {
                return 2;
            }
            return 3;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new HeaderCell(context);
                    break;
                case 1:
                    view = new TextCheckCell(context);
                    break;
                case 2:
                    view = new TextInfoPrivacyCell(context);
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
            switch (holder.getItemViewType()) {
                case 0: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == interfaceHeaderRow) {
                        headerCell.setText("INTERFAZ Y CHATS");
                    } else if (position == performanceHeaderRow) {
                        headerCell.setText("RENDIMIENTO Y RED");
                    }
                    break;
                }
                case 1: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    if (position == ios27BottomBarRow) {
                        checkCell.setTextAndCheck(LagramConfig.withNewBadge("Barra inferior iOS 27 (3 p\u00edldoras)"), LagramConfig.ios27BottomBar, true);
                    } else if (position == disableSponsoredRow) {
                        checkCell.setTextAndCheck(LagramConfig.withNewBadge("Ocultar mensajes patrocinados"), LagramConfig.disableSponsoredMessages, false);
                    } else if (position == highRefreshRateScrollRow) {
                        checkCell.setTextAndCheck(LagramConfig.withNewBadge("Hints de 120 Hz en scroll"), LagramConfig.highRefreshRateScroll, true);
                    } else if (position == turboDownloadsRow) {
                        checkCell.setTextAndCheck(LagramConfig.withNewBadge("Descargas paralelas Turbo"), LagramConfig.turboDownloads, false);
                    }
                    break;
                }
                case 2: {
                    TextInfoPrivacyCell infoCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == interfaceInfoRow) {
                        infoCell.setText("Al desactivar la barra inferior iOS 27 se restaura el estilo original de Telegram al volver a abrir un chat.");
                    } else if (position == performanceInfoRow) {
                        infoCell.setText("Las descargas Turbo aceleran archivos grandes usando conexiones paralelas.");
                    }
                    break;
                }
            }
        }
    }
}
