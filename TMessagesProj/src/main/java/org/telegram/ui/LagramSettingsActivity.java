package org.telegram.ui;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.lagram.LagramAppearanceActivity;
import org.telegram.ui.lagram.LagramChatsActivity;
import org.telegram.ui.lagram.LagramGlassBubbleEngine;
import org.telegram.ui.lagram.LagramPrivacyActivity;
import org.telegram.ui.lagram.LagramSettingsHeaderView;
import org.telegram.ui.lagram.OpexgramStyleWidgets;

import java.util.ArrayList;

/**
 * Categorized Material 3 card-hub for Lagram Settings.
 * Cleanly segmented into rounded category cards, each opening its focused sub-screen
 * with real interactive previews and zero extra bloat.
 */
public class LagramSettingsActivity extends UniversalFragment {

    private static final int ID_CAT_APPEARANCE = 100;
    private static final int ID_CAT_CHATS = 200;
    private static final int ID_GHOST_MASTER = 300;
    private static final int ID_CAT_PRIVACY = 301;
    private static final int ID_RESET = 500;

    private LagramSettingsHeaderView headerView;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramSettings);
    }

    @Override
    public View createView(Context context) {
        LagramConfig.ensureLoaded();
        headerView = new LagramSettingsHeaderView(context, getResourceProvider());
        View view = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        int bg = Theme.getColor(Theme.key_windowBackgroundGray);
        view.setBackgroundColor(bg);
        listView.setBackgroundColor(bg);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (headerView != null) {
            headerView.setPaused(false);
        }
        refreshList();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (headerView != null) {
            headerView.setPaused(true);
        }
    }

    private void refreshList() {
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    private String getChatHeaderLabel() {
        switch (LagramConfig.chatHeaderStyle) {
            case LagramConfig.CHAT_HEADER_SIGNATURE:
                return LocaleController.getString(R.string.LagramChatHeaderSignature);
            case LagramConfig.CHAT_HEADER_FUSION:
                return LocaleController.getString(R.string.LagramChatHeaderFusion);
            case LagramConfig.CHAT_HEADER_PORTRAIT:
                return LocaleController.getString(R.string.LagramChatHeaderPortrait);
            case LagramConfig.CHAT_HEADER_FLOATING:
                return LocaleController.getString(R.string.LagramChatHeaderFloating);
            default:
                return LocaleController.getString(R.string.LagramChatHeaderDefault);
        }
    }

    private String getFocusStyleLabel() {
        switch (LagramConfig.messageFocusStyle) {
            case LagramConfig.FOCUS_STYLE_LIST:
                return LocaleController.getString(R.string.LagramFocusStyleList);
            case LagramConfig.FOCUS_STYLE_TILES:
                return LocaleController.getString(R.string.LagramFocusStyleTiles);
            default:
                return LocaleController.getString(R.string.LagramFocusStyleCapsule);
        }
    }

    private String buildAppearanceSubtitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(LocaleController.getString(R.string.LagramGlassMessages));
        sb.append(": ");
        sb.append(LagramConfig.glassMessages ? (LagramConfig.glassMessagesAlpha + "%") : "OFF");
        sb.append(" • ");
        sb.append(LocaleController.getString(R.string.LagramScrollEdgeScale));
        if (LagramConfig.scrollEdgeScale) {
            sb.append(" (");
            sb.append(LagramConfig.scrollEdgeMode == LagramConfig.SCROLL_EDGE_DRUM
                    ? LocaleController.getString(R.string.LagramScrollEdgeModeDrum)
                    : LocaleController.getString(R.string.LagramScrollEdgeModeScale));
            sb.append(")");
        }
        return sb.toString();
    }

    private String buildChatsSubtitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(getChatHeaderLabel());
        if (LagramConfig.ios27BottomBar) {
            sb.append(" • iOS");
        }
        if (LagramConfig.messageFocusEnabled) {
            sb.append(" • ").append(getFocusStyleLabel());
        }
        return sb.toString();
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // Hero Banner
        items.add(UItem.asCustomShadow(headerView, 253));

        // Card 1: Personalization & Chat Interface Categories
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramSettings)));
        items.add(SettingsActivity.SettingCell.Factory.of(
                ID_CAT_APPEARANCE,
                IconBackgroundColors.PURPLE.top,
                IconBackgroundColors.PURPLE.bottom,
                R.drawable.msg_theme,
                LocaleController.getString(R.string.LagramCategoryAppearance),
                buildAppearanceSubtitle()));
        items.add(SettingsActivity.SettingCell.Factory.of(
                ID_CAT_CHATS,
                IconBackgroundColors.BLUE.top,
                IconBackgroundColors.BLUE.bottom,
                R.drawable.msg_discussion,
                LocaleController.getString(R.string.LagramCategoryChats),
                buildChatsSubtitle()));
        items.add(UItem.asShadow(-1, null));

        // Card 2: Privacy & Ghost Mode
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramGhostModeHeader)));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_GHOST_MASTER, R.drawable.msg_secret,
                LocaleController.getString(R.string.LagramGhostModeMaster),
                null,
                LagramConfig.ghostModeEnabled));
        items.add(SettingsActivity.SettingCell.Factory.of(
                ID_CAT_PRIVACY,
                IconBackgroundColors.BLUE_DEEP.top,
                IconBackgroundColors.BLUE_DEEP.bottom,
                R.drawable.msg_secret,
                LocaleController.getString(R.string.LagramCategoryPrivacy),
                LocaleController.getString(R.string.LagramGhostModeHeader)));
        items.add(UItem.asShadow(-2, null));

        // Card 3: Reset & Version
        items.add(UItem.asButton(ID_RESET, R.drawable.msg_reset, LocaleController.getString(R.string.LagramResetSettings)).red());
        items.add(UItem.asCenterShadow(LagramSettingsHeaderView.getVersionString()));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_CAT_APPEARANCE:
                presentFragment(new LagramAppearanceActivity());
                break;
            case ID_CAT_CHATS:
                presentFragment(new LagramChatsActivity());
                break;
            case ID_GHOST_MASTER:
                LagramConfig.toggleGhostMode();
                refreshList();
                break;
            case ID_CAT_PRIVACY:
                presentFragment(new LagramPrivacyActivity());
                break;
            case ID_RESET:
                if (getParentActivity() == null) return;
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
                builder.setTitle(LocaleController.getString(R.string.LagramResetSettings));
                builder.setMessage(LocaleController.getString(R.string.LagramResetConfirmMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Reset), (dialog, which) -> {
                    LagramConfig.resetToDefaults();
                    LagramGlassBubbleEngine.invalidateCapture();
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewTheme, false);
                    refreshList();
                    BulletinFactory.of(this).createSimpleBulletin(R.raw.info, LocaleController.getString(R.string.LagramResetToast)).show();
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                showDialog(builder.create());
                break;
        }
    }
}
