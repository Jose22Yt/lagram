package org.telegram.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.SettingsSearchCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.lagram.LagramAppearanceActivity;
import org.telegram.ui.lagram.LagramChatsActivity;
import org.telegram.ui.lagram.LagramGeneralActivity;
import org.telegram.ui.lagram.LagramMediaActivity;
import org.telegram.ui.lagram.LagramPrivacyActivity;
import org.telegram.ui.lagram.LagramSettingsHeaderView;

import java.util.ArrayList;
import java.util.Locale;

public class LagramSettingsActivity extends UniversalFragment {

    private static final int ID_CAT_GENERAL = 1;
    private static final int ID_CAT_APPEARANCE = 2;
    private static final int ID_CAT_CHATS = 3;
    private static final int ID_CAT_MEDIA = 4;
    private static final int ID_CAT_PRIVACY = 5;

    private static final int ID_GHOST_MASTER = 10;
    private static final int ID_BACKUP_EXPORT = 20;
    private static final int ID_BACKUP_IMPORT = 21;
    private static final int ID_BACKUP_RESET = 22;

    private static final int ID_LINK_GITHUB = 30;
    private static final int ID_LINK_DEVELOPER = 31;

    private LagramSettingsHeaderView headerView;
    private ActionBarMenuItem searchItem;
    private String searchQuery;
    private ArrayList<SearchEntry> cachedCatalog;
    private final ArrayList<SearchEntry> searchResults = new ArrayList<>();

    private static final class SearchEntry {
        final int id;
        final String title;
        final String subtitle;
        final String[] breadcrumbs;
        final int iconResId;
        final int targetCategory;

        SearchEntry(int id, int titleRes, int subtitleRes, String categoryName, int iconResId, int targetCategory) {
            this.id = id;
            this.title = LocaleController.getString(titleRes);
            this.subtitle = subtitleRes != 0 ? LocaleController.getString(subtitleRes) : null;
            this.breadcrumbs = new String[]{LocaleController.getString(R.string.LagramSettings), categoryName};
            this.iconResId = iconResId;
            this.targetCategory = targetCategory;
        }
    }

    private ArrayList<SearchEntry> getCatalog() {
        if (cachedCatalog != null) {
            return cachedCatalog;
        }
        ArrayList<SearchEntry> list = new ArrayList<>(64);
        String catGeneral = LocaleController.getString(R.string.LagramCategoryGeneral);
        String catAppearance = LocaleController.getString(R.string.LagramCategoryAppearance);
        String catChats = LocaleController.getString(R.string.LagramCategoryChats);
        String catMedia = LocaleController.getString(R.string.LagramCategoryMedia);
        String catPrivacy = LocaleController.getString(R.string.LagramCategoryPrivacy);

        // General
        list.add(new SearchEntry(101, R.string.LagramHighRefreshRate, 0, catGeneral, R.drawable.msg_speed, ID_CAT_GENERAL));
        list.add(new SearchEntry(102, R.string.LagramForcePerfClass, 0, catGeneral, R.drawable.msg_speed, ID_CAT_GENERAL));
        list.add(new SearchEntry(103, R.string.LagramDisableSponsored, 0, catGeneral, R.drawable.msg_speed, ID_CAT_GENERAL));
        list.add(new SearchEntry(104, R.string.LagramHideSimilarChannels, 0, catGeneral, R.drawable.msg_speed, ID_CAT_GENERAL));
        list.add(new SearchEntry(105, R.string.LagramShowJoinDate, 0, catGeneral, R.drawable.msg_speed, ID_CAT_GENERAL));
        list.add(new SearchEntry(106, R.string.LagramOpenArchiveOnPull, 0, catGeneral, R.drawable.msg_speed, ID_CAT_GENERAL));
        list.add(new SearchEntry(107, R.string.LagramHapticsLevel, 0, catGeneral, R.drawable.msg_speed, ID_CAT_GENERAL));
        list.add(new SearchEntry(108, R.string.LagramProfileNameSwitch, 0, catGeneral, R.drawable.msg_copy, ID_CAT_GENERAL));
        list.add(new SearchEntry(109, R.string.LagramShowIdAndDc, 0, catGeneral, R.drawable.msg_info, ID_CAT_GENERAL));
        list.add(new SearchEntry(110, R.string.LagramHidePhoneNumber, 0, catGeneral, R.drawable.msg_info, ID_CAT_GENERAL));
        list.add(new SearchEntry(111, R.string.LagramHideStories, 0, catGeneral, R.drawable.msg_info, ID_CAT_GENERAL));

        // Appearance (Liquid Glass, 3D Scroll, Shape & Cards)
        list.add(new SearchEntry(200, R.string.LagramAppIconTitle, R.string.LagramAppIconHint, catAppearance, R.drawable.msg_palette, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(201, R.string.LagramGlassMessages, 0, catAppearance, R.drawable.msg_palette, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(202, R.string.LagramGlassTintMode, 0, catAppearance, R.drawable.msg_palette, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(203, R.string.LagramGlassBlurLevel, 0, catAppearance, R.drawable.msg_palette, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(204, R.string.LagramGlassBorderOpacity, 0, catAppearance, R.drawable.msg_palette, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(205, R.string.LagramGlassHighlights, 0, catAppearance, R.drawable.msg_palette, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(206, R.string.LagramPopupGlass, 0, catAppearance, R.drawable.msg_palette, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(207, R.string.LagramChatEdgeBlur, 0, catAppearance, R.drawable.msg_theme, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(208, R.string.LagramShowBubbleTails, 0, catAppearance, R.drawable.msg_theme, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(209, R.string.LagramRoundingPreset, 0, catAppearance, R.drawable.msg_theme, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(210, R.string.LagramAvatarShape, 0, catAppearance, R.drawable.msg_theme, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(211, R.string.LagramScrollEdgeScale, 0, catAppearance, R.drawable.msg_speed, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(212, R.string.LagramScrollEdgeMode, 0, catAppearance, R.drawable.msg_speed, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(213, R.string.LagramAnimationSpeed, 0, catAppearance, R.drawable.msg_speed, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(214, R.string.LagramM3Containers, R.string.LagramM3ContainersInfo, catAppearance, R.drawable.msg_theme, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(215, R.string.LagramM3SectionSpacing, 0, catAppearance, R.drawable.msg_theme, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(216, R.string.LagramFolderTabsStyle, 0, catAppearance, R.drawable.msg_folders, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(217, R.string.LagramShowFolderCounters, 0, catAppearance, R.drawable.msg_folders, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(218, R.string.LagramAmoledSettings, 0, catAppearance, R.drawable.msg_theme, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(219, R.string.LagramUseSystemFonts, 0, catAppearance, R.drawable.msg_theme, ID_CAT_APPEARANCE));
        list.add(new SearchEntry(220, R.string.LagramShowSeconds, 0, catAppearance, R.drawable.msg_theme, ID_CAT_APPEARANCE));

        // Chats & Message Focus
        list.add(new SearchEntry(300, R.string.LagramNagramTranslate, R.string.LagramNagramTranslateDesc, catChats, R.drawable.msg_translate, ID_CAT_CHATS));
        list.add(new SearchEntry(299, R.string.LagramTranslateProvider, 0, catChats, R.drawable.msg_translate, ID_CAT_CHATS));
        list.add(new SearchEntry(298, R.string.LagramTranslateKeepOriginal, R.string.LagramTranslateKeepOriginalDesc, catChats, R.drawable.msg_translate, ID_CAT_CHATS));
        list.add(new SearchEntry(297, R.string.LagramAutoTranslateChats, R.string.LagramAutoTranslateChatsDesc, catChats, R.drawable.msg_translate, ID_CAT_CHATS));
        list.add(new SearchEntry(301, R.string.LagramMessageFocus, R.string.LagramMessageFocusInfo, catChats, R.drawable.msg_discussion, ID_CAT_CHATS));
        list.add(new SearchEntry(302, R.string.LagramMessageFocusBlur, 0, catChats, R.drawable.msg_discussion, ID_CAT_CHATS));
        list.add(new SearchEntry(303, R.string.LagramMessageFocusStyle, 0, catChats, R.drawable.msg_discussion, ID_CAT_CHATS));
        list.add(new SearchEntry(304, R.string.LagramChatHeaderStyle, 0, catChats, R.drawable.msg_discussion, ID_CAT_CHATS));
        list.add(new SearchEntry(305, R.string.LagramIos27BottomBar, 0, catChats, R.drawable.msg_discussion, ID_CAT_CHATS));
        list.add(new SearchEntry(306, R.string.LagramTextAnimHeader, R.string.LagramTextAnimInfo, catChats, R.drawable.msg_edit, ID_CAT_CHATS));
        list.add(new SearchEntry(307, R.string.LagramDeleteEffect, 0, catChats, R.drawable.msg_delete, ID_CAT_CHATS));
        list.add(new SearchEntry(308, R.string.LagramDoubleTapOutgoing, 0, catChats, R.drawable.msg_discussion, ID_CAT_CHATS));
        list.add(new SearchEntry(309, R.string.LagramDoubleTapIncoming, 0, catChats, R.drawable.msg_discussion, ID_CAT_CHATS));
        list.add(new SearchEntry(310, R.string.LagramStickerSize, 0, catChats, R.drawable.msg_sticker, ID_CAT_CHATS));
        list.add(new SearchEntry(311, R.string.LagramGifSize, 0, catChats, R.drawable.msg_discussion, ID_CAT_CHATS));
        list.add(new SearchEntry(312, R.string.LagramBigReactions, 0, catChats, R.drawable.msg_reactions2, ID_CAT_CHATS));
        list.add(new SearchEntry(313, R.string.LagramForwardWithoutQuote, 0, catChats, R.drawable.msg_discussion, ID_CAT_CHATS));
        list.add(new SearchEntry(314, R.string.LagramConfirmBeforeCall, 0, catChats, R.drawable.msg_discussion, ID_CAT_CHATS));
        list.add(new SearchEntry(315, R.string.LagramUnlimitedSelection, 0, catChats, R.drawable.msg_discussion, ID_CAT_CHATS));

        // Media
        list.add(new SearchEntry(401, R.string.LagramDownloadBoost, 0, catMedia, R.drawable.msg_photos, ID_CAT_MEDIA));
        list.add(new SearchEntry(402, R.string.LagramTurboUploads, 0, catMedia, R.drawable.msg_photos, ID_CAT_MEDIA));
        list.add(new SearchEntry(403, R.string.LagramSendPhotosHd, 0, catMedia, R.drawable.msg_photos, ID_CAT_MEDIA));
        list.add(new SearchEntry(404, R.string.LagramRearCameraRound, 0, catMedia, R.drawable.msg_camera, ID_CAT_MEDIA));
        list.add(new SearchEntry(405, R.string.LagramAutoplayVoice, 0, catMedia, R.drawable.msg_photos, ID_CAT_MEDIA));
        list.add(new SearchEntry(406, R.string.LagramMediaGlow, 0, catMedia, R.drawable.msg_photos, ID_CAT_MEDIA));
        list.add(new SearchEntry(407, R.string.LagramCompactRoundCamera, 0, catMedia, R.drawable.msg_camera, ID_CAT_MEDIA));

        // Privacy & Ghost Mode
        list.add(new SearchEntry(501, R.string.LagramGhostModeMaster, 0, catPrivacy, R.drawable.msg_secret, ID_CAT_PRIVACY));
        list.add(new SearchEntry(502, R.string.LagramGhostDontReadMessages, 0, catPrivacy, R.drawable.msg_secret, ID_CAT_PRIVACY));
        list.add(new SearchEntry(503, R.string.LagramGhostDontReadStories, 0, catPrivacy, R.drawable.msg_secret, ID_CAT_PRIVACY));
        list.add(new SearchEntry(504, R.string.LagramGhostDontSendTyping, 0, catPrivacy, R.drawable.msg_secret, ID_CAT_PRIVACY));
        list.add(new SearchEntry(505, R.string.LagramGhostDontSendOnline, 0, catPrivacy, R.drawable.msg_secret, ID_CAT_PRIVACY));
        list.add(new SearchEntry(506, R.string.LagramSaveDeletedMessages, 0, catPrivacy, R.drawable.msg_delete, ID_CAT_PRIVACY));
        list.add(new SearchEntry(507, R.string.LagramDeletedStyleHeader, 0, catPrivacy, R.drawable.msg_palette, ID_CAT_PRIVACY));
        list.add(new SearchEntry(508, R.string.LagramSaveEditedMessages, 0, catPrivacy, R.drawable.msg_edit, ID_CAT_PRIVACY));
        list.add(new SearchEntry(509, R.string.LagramAllowScreenshots, 0, catPrivacy, R.drawable.msg_secret, ID_CAT_PRIVACY));
        list.add(new SearchEntry(510, R.string.LagramBypassNoForwards, 0, catPrivacy, R.drawable.msg_secret, ID_CAT_PRIVACY));

        cachedCatalog = list;
        return cachedCatalog;
    }

    @Override
    public View createView(Context context) {
        LagramConfig.ensureLoaded();
        headerView = new LagramSettingsHeaderView(context, getResourceProvider());
        View view = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        if (LagramConfig.amoledSettingsBackdrop && Theme.isCurrentThemeDark()) {
            fragmentView.setBackgroundColor(0xFF000000);
            listView.setBackgroundColor(0xFF000000);
        }

        searchItem = actionBar.createMenu()
                .addItem(1, R.drawable.outline_header_search)
                .setIsSearchField(true)
                .setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
                    @Override
                    public void onSearchCollapse() {
                        updateSearchQuery(null);
                    }

                    @Override
                    public void onTextChanged(EditText editText) {
                        updateSearchQuery(editText.getText().toString());
                    }
                });
        searchItem.setSearchFieldHint(LocaleController.getString(R.string.Search));
        searchItem.setContentDescription(LocaleController.getString(R.string.Search));
        return view;
    }

    private void updateSearchQuery(String query) {
        String trimmed = (query == null || TextUtils.isEmpty(query.trim())) ? null : query.trim();
        if (TextUtils.equals(trimmed, searchQuery)) {
            return;
        }
        searchQuery = trimmed;
        searchResults.clear();
        if (searchQuery != null) {
            String[] tokens = searchQuery.toLowerCase(Locale.ROOT).split("\\s+");
            ArrayList<SearchEntry> catalog = getCatalog();
            for (int i = 0, n = catalog.size(); i < n; i++) {
                SearchEntry entry = catalog.get(i);
                String lowerTitle = entry.title.toLowerCase(Locale.ROOT);
                String lowerSub = entry.subtitle != null ? entry.subtitle.toLowerCase(Locale.ROOT) : null;
                boolean matchesAll = true;
                for (String token : tokens) {
                    if (!token.isEmpty() && !lowerTitle.contains(token) && (lowerSub == null || !lowerSub.contains(token))) {
                        matchesAll = false;
                        break;
                    }
                }
                if (matchesAll) {
                    searchResults.add(entry);
                }
            }
        }
        if (listView != null) {
            listView.adapter.update(true);
            listView.scrollToPosition(0);
        }
    }

    private static UItem createCategoryItem(int id, IconBackgroundColors colors, int iconResId, int titleResId) {
        return SettingsActivity.SettingCell.Factory.of(id, colors.top, colors.bottom, iconResId, LocaleController.getString(titleResId));
    }

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramSettings);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();
        if (searchQuery == null) {
            items.add(UItem.asCustomShadow(headerView, 253));

            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramQuickGhostHeader)));
            items.add(org.telegram.ui.lagram.OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_GHOST_MASTER, 0,
                    LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramGhostModeMaster)),
                    null,
                    LagramConfig.ghostModeEnabled));
            items.add(UItem.asShadow(-1, null));

            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramCategoriesHeader)));
            items.add(createCategoryItem(ID_CAT_GENERAL, IconBackgroundColors.BLUE, R.drawable.filled_profile_settings, R.string.LagramCategoryGeneral));
            items.add(createCategoryItem(ID_CAT_APPEARANCE, IconBackgroundColors.PURPLE, R.drawable.msg_theme, R.string.LagramCategoryAppearance));
            items.add(createCategoryItem(ID_CAT_CHATS, IconBackgroundColors.GREEN, R.drawable.settings_chat, R.string.LagramCategoryChats));
            items.add(createCategoryItem(ID_CAT_MEDIA, IconBackgroundColors.ORANGE, R.drawable.filled_widget_music, R.string.LagramCategoryMedia));
            items.add(createCategoryItem(ID_CAT_PRIVACY, IconBackgroundColors.BLUE_DEEP, R.drawable.msg_secret, R.string.LagramCategoryPrivacy));
            items.add(UItem.asShadow(-2, null));

            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramBackupHeader)));
            items.add(UItem.asButton(ID_BACKUP_EXPORT, R.drawable.msg_copy, LocaleController.getString(R.string.LagramExportSettings)));
            items.add(UItem.asButton(ID_BACKUP_IMPORT, R.drawable.msg_download, LocaleController.getString(R.string.LagramImportSettings)));
            items.add(UItem.asButton(ID_BACKUP_RESET, R.drawable.msg_reset, LocaleController.getString(R.string.LagramResetSettings)).red());
            items.add(UItem.asShadow(-3, null));

            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramLinksHeader)));
            items.add(UItem.asSettingsCell(ID_LINK_GITHUB, R.drawable.msg_channel, LocaleController.getString(R.string.LagramRepoTitle), "GitHub"));
            items.add(UItem.asSettingsCell(ID_LINK_DEVELOPER, R.drawable.msg_openprofile, LocaleController.getString(R.string.LagramDeveloperTitle), "@Jose22Yt"));
            items.add(UItem.asCenterShadow(LagramSettingsHeaderView.getVersionString()));
            return;
        }

        if (searchResults.isEmpty()) {
            items.add(UItem.asCenterShadow(LocaleController.getString(R.string.NoResult)));
            return;
        }

        int highlightColor = getThemedColor(Theme.key_windowBackgroundWhiteBlueText4);
        String[] tokens = searchQuery.toLowerCase(Locale.ROOT).split("\\s+");
        for (int i = 0, n = searchResults.size(); i < n; i++) {
            SearchEntry entry = searchResults.get(i);
            SpannableStringBuilder ssb = new SpannableStringBuilder(entry.title);
            String lower = entry.title.toLowerCase(Locale.ROOT);
            for (String token : tokens) {
                if (!token.isEmpty()) {
                    int idx = lower.indexOf(token);
                    if (idx >= 0) {
                        ssb.setSpan(new ForegroundColorSpan(highlightColor), idx, Math.min(ssb.length(), idx + token.length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            UItem item = SettingsSearchCell.Factory.of(ssb, (CharSequence[]) entry.breadcrumbs);
            item.id = entry.targetCategory;
            item.iconResId = entry.iconResId;
            items.add(item);
        }
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_GHOST_MASTER: {
                boolean enabled = LagramConfig.toggleGhostMode();
                if (view instanceof org.telegram.ui.lagram.OpexgramStyleWidgets.OpexSwitchCell) {
                    ((org.telegram.ui.lagram.OpexgramStyleWidgets.OpexSwitchCell) view).setChecked(enabled);
                }
                if (listView != null) {
                    listView.adapter.update(true);
                }
                break;
            }
            case ID_CAT_GENERAL:
                presentFragment(new LagramGeneralActivity());
                break;
            case ID_CAT_APPEARANCE:
                presentFragment(new LagramAppearanceActivity());
                break;
            case ID_CAT_CHATS:
                presentFragment(new LagramChatsActivity());
                break;
            case ID_CAT_MEDIA:
                presentFragment(new LagramMediaActivity());
                break;
            case ID_CAT_PRIVACY:
                presentFragment(new LagramPrivacyActivity());
                break;
            case ID_BACKUP_EXPORT: {
                String json = LagramConfig.exportToJson();
                AndroidUtilities.addToClipboard(json);
                BulletinFactory.of(this).createSimpleBulletin(R.raw.copy, LocaleController.getString(R.string.LagramExportedToast)).show();
                break;
            }
            case ID_BACKUP_IMPORT: {
                if (getParentActivity() == null) return;
                ClipboardManager cm = (ClipboardManager) getParentActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                String text = null;
                if (cm != null && cm.hasPrimaryClip()) {
                    ClipData clip = cm.getPrimaryClip();
                    if (clip != null && clip.getItemCount() > 0 && clip.getItemAt(0).getText() != null) {
                        text = clip.getItemAt(0).getText().toString();
                    }
                }
                if (LagramConfig.importFromJson(text)) {
                    if (listView != null) listView.adapter.update(true);
                    BulletinFactory.of(this).createSimpleBulletin(R.raw.contact_check, LocaleController.getString(R.string.LagramImportedToast)).show();
                } else {
                    BulletinFactory.of(this).createSimpleBulletin(R.raw.error, LocaleController.getString(R.string.LagramImportFailedToast)).show();
                }
                break;
            }
            case ID_BACKUP_RESET: {
                if (getParentActivity() == null) return;
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.LagramResetConfirmTitle));
                builder.setMessage(LocaleController.getString(R.string.LagramResetConfirmMessage));
                builder.setPositiveButton(LocaleController.getString(R.string.Reset), (dialog, which) -> {
                    LagramConfig.resetToDefaults();
                    if (listView != null) listView.adapter.update(true);
                    BulletinFactory.of(this).createSimpleBulletin(R.raw.ic_delete, LocaleController.getString(R.string.LagramResetToast)).show();
                });
                builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                showDialog(builder.create());
                break;
            }
            case ID_LINK_GITHUB:
                Browser.openUrl(getContext(), "https://github.com/Jose22Yt/lagram");
                break;
            case ID_LINK_DEVELOPER:
                Browser.openUrl(getContext(), "https://github.com/Jose22Yt");
                break;
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    @Override
    public boolean onBackPressed(boolean invoked) {
        if (actionBar != null && actionBar.isSearchFieldVisible()) {
            if (invoked) {
                actionBar.closeSearchField();
            }
            return false;
        }
        return super.onBackPressed(invoked);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (headerView != null) {
            headerView.setPaused(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listView != null) {
            listView.adapter.update(true);
        }
        if (headerView != null) {
            headerView.setPaused(false);
        }
    }
}
