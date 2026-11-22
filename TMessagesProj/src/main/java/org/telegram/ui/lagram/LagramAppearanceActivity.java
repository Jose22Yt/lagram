package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class LagramAppearanceActivity extends UniversalFragment {

    public static final int SUB_HOME = 0;
    public static final int SUB_BUBBLES_AND_GLASS = 1;
    public static final int SUB_FOLDERS = 2;
    public static final int SUB_CLEAN_UI = 3;
    public static final int SUB_INTERFACE_DETAILS = 4;

    private final int subPage;

    private static final int ID_NAV_BUBBLES = 100;
    private static final int ID_NAV_FOLDERS = 101;
    private static final int ID_NAV_CLEAN_UI = 102;
    private static final int ID_NAV_DETAILS = 103;

    private static final int ID_PREVIEW_BUBBLES = 1;
    private static final int ID_GLASS_MESSAGES = 2;
    private static final int ID_GLASS_ALPHA = 3;
    private static final int ID_GLASS_BORDER = 4;
    private static final int ID_BUBBLE_TAILS = 5;
    private static final int ID_ROUNDING_PRESET = 6;
    private static final int ID_CHAT_EDGE_BLUR = 7;
    private static final int ID_SCROLL_EDGE_SCALE = 8;
    private static final int ID_IOS27_BOTTOM_BAR = 9;
    private static final int ID_AMOLED_BACKDROP = 10;
    private static final int ID_ANIM_SPEED = 11;
    private static final int ID_SYSTEM_FONTS = 12;

    private static final int ID_PREVIEW_FOLDERS = 20;
    private static final int ID_FOLDER_TABS_STYLE = 21;
    private static final int ID_FOLDER_COUNTERS = 22;
    private static final int ID_HIDE_ALL_CHATS = 23;
    private static final int ID_REMEMBER_LAST_FOLDER = 24;

    private static final int ID_HIDE_STORIES = 30;
    private static final int ID_HIDE_STORIES_PROFILE = 31;
    private static final int ID_HIDE_STORY_POSTING = 32;
    private static final int ID_HIDE_PHONE_NUMBER = 33;
    private static final int ID_HIDE_TELEGRAM_BLOAT = 34;

    private static final int ID_SHOW_SECONDS = 40;
    private static final int ID_SHOW_ID_DC = 41;
    private static final int ID_ID_BOT_API = 42;

    public LagramAppearanceActivity() {
        this(SUB_HOME);
    }

    public LagramAppearanceActivity(int subPage) {
        super();
        this.subPage = subPage;
    }

    @Override
    protected CharSequence getTitle() {
        switch (subPage) {
            case SUB_BUBBLES_AND_GLASS:
                return LocaleController.getString(R.string.LagramLiquidGlassHeader);
            case SUB_FOLDERS:
                return LocaleController.getString(R.string.LagramFoldersHeader);
            case SUB_CLEAN_UI:
                return LocaleController.getString(R.string.LagramUiVisibilityHeader);
            case SUB_INTERFACE_DETAILS:
                return LocaleController.getString(R.string.LagramDetailsHeader);
            case SUB_HOME:
            default:
                return LocaleController.getString(R.string.LagramCategoryAppearance);
        }
    }

    @Override
    public View createView(Context context) {
        LagramConfig.ensureLoaded();
        View view = super.createView(context);
        listView.setSections();
        updateBackdropColor();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
        refreshLivePreviews();
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

    private void refreshLivePreviews() {
        if (listView == null) {
            return;
        }
        for (int i = 0, count = listView.getChildCount(); i < count; i++) {
            View child = listView.getChildAt(i);
            if (child instanceof LagramSettingsComponents.RealChatMessagesPreviewCell) {
                ((LagramSettingsComponents.RealChatMessagesPreviewCell) child).refreshPreview();
            } else if (child instanceof LagramSettingsComponents.RealFolderTabsPreviewCell) {
                ((LagramSettingsComponents.RealFolderTabsPreviewCell) child).rebuildTabs();
            }
        }
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        if (subPage == SUB_HOME) {
            // Compact Hub with Real Live Chat Preview + Grouped Sub-Fragment Cards
            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramLiquidGlassHeader)));
            items.add(LagramSettingsComponents.RealChatMessagesPreviewCell.Factory.of(
                    ID_PREVIEW_BUBBLES, LagramSettingsComponents.RealChatMessagesPreviewCell.MODE_BUBBLES_AND_GLASS));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_GLASS_MESSAGES,
                    R.drawable.msg_theme,
                    LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramGlassMessages)),
                    LocaleController.getString(R.string.LagramGlassMessagesBorder),
                    LagramConfig.glassMessages
            ));
            items.add(UItem.asShadow(null));

            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramCategoriesHeader)));
            items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                    ID_NAV_BUBBLES,
                    IconBackgroundColors.PURPLE,
                    R.drawable.msg_theme,
                    "Burbujas, Liquid Glass y Bordes",
                    "Opacidad cristalina, borde especular, colas y redondeo",
                    LagramConfig.glassMessages ? (LagramConfig.glassMessagesAlpha + "%") : (LagramConfig.getEffectiveBubbleRadiusDp() + "dp")
            ));
            items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                    ID_NAV_FOLDERS,
                    IconBackgroundColors.BLUE,
                    R.drawable.msg_folders,
                    "Pestañas de Carpetas",
                    "Iconos, contadores y vista previa real de FilterTabsView",
                    LagramConfig.folderTabsStyle == LagramConfig.FOLDER_TABS_ICON_ONLY ? "Iconos"
                            : (LagramConfig.folderTabsStyle == LagramConfig.FOLDER_TABS_TEXT_ONLY ? "Texto" : "Ambos")
            ));
            items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                    ID_NAV_CLEAN_UI,
                    IconBackgroundColors.ORANGE,
                    R.drawable.msg_menu_stories,
                    "Visibilidad e Interfaz Limpia",
                    "Ocultar historias, botón de cámara, teléfono y publicidad",
                    null
            ));
            items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                    ID_NAV_DETAILS,
                    IconBackgroundColors.CYAN,
                    R.drawable.msg_info,
                    "Barra iOS 27, Animaciones y Detalles",
                    "Fondo AMOLED, segundos en hora, ID/DC y velocidad",
                    null
            ));
            items.add(UItem.asShadow(LocaleController.getString(R.string.LagramAppearanceInfo)));
            return;
        }

        if (subPage == SUB_BUBBLES_AND_GLASS) {
            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramLiquidGlassHeader)));
            items.add(LagramSettingsComponents.RealChatMessagesPreviewCell.Factory.of(
                    ID_PREVIEW_BUBBLES, LagramSettingsComponents.RealChatMessagesPreviewCell.MODE_BUBBLES_AND_GLASS));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_GLASS_MESSAGES,
                    R.drawable.msg_theme,
                    LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramGlassMessages)),
                    "Burbujas traslúcidas reales sobre el fondo del chat",
                    LagramConfig.glassMessages
            ));
            if (LagramConfig.glassMessages) {
                items.add(LagramSettingsComponents.InlineSliderCell.Factory.of(
                        ID_GLASS_ALPHA,
                        LocaleController.getString(R.string.LagramGlassMessagesAlpha),
                        25, 100, LagramConfig.glassMessagesAlpha, 75, "%",
                        val -> {
                            LagramConfig.setGlassMessagesAlpha(val);
                            refreshLivePreviews();
                        }
                ));
                items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                        ID_GLASS_BORDER,
                        R.drawable.msg_photos,
                        LocaleController.getString(R.string.LagramGlassMessagesBorder),
                        "Reflejo cristalino degradado en el contorno de cada burbuja",
                        LagramConfig.glassMessagesBorder
                ));
            }
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_BUBBLE_TAILS,
                    R.drawable.msg_discussion,
                    LocaleController.getString(R.string.LagramShowBubbleTails),
                    "Mostrar la esquina con cola en el último mensaje del grupo",
                    LagramConfig.showBubbleTails
            ));
            items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                    ID_ROUNDING_PRESET,
                    LocaleController.getString(R.string.LagramRoundingPreset),
                    new String[]{
                            LocaleController.getString(R.string.LagramRoundingDefault),
                            LocaleController.getString(R.string.LagramRoundingRound),
                            LocaleController.getString(R.string.LagramRoundingSquircle),
                            LocaleController.getString(R.string.LagramRoundingSquare)
                    },
                    LagramConfig.roundingPreset,
                    which -> {
                        LagramConfig.setRoundingPreset(which);
                        refreshLivePreviews();
                    }
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_CHAT_EDGE_BLUR,
                    R.drawable.msg_photo_blur,
                    LocaleController.getString(R.string.LagramChatEdgeBlur),
                    "Desenfoque en tiempo real detrás de la barra superior e inferior",
                    LagramConfig.chatEdgeBlur
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_SCROLL_EDGE_SCALE,
                    R.drawable.msg_speed,
                    LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramScrollEdgeScale)),
                    "Compresión elástica suave en los bordes al deslizar el chat",
                    LagramConfig.scrollEdgeScale
            ));
            items.add(UItem.asShadow(null));
            return;
        }

        if (subPage == SUB_FOLDERS) {
            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramFoldersHeader)));
            items.add(LagramSettingsComponents.RealFolderTabsPreviewCell.Factory.of(ID_PREVIEW_FOLDERS));
            items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                    ID_FOLDER_TABS_STYLE,
                    LocaleController.getString(R.string.LagramFolderTabsStyle),
                    new String[]{
                            LocaleController.getString(R.string.LagramFolderTabsIconText),
                            LocaleController.getString(R.string.LagramFolderTabsIconOnly),
                            LocaleController.getString(R.string.LagramFolderTabsTextOnly)
                    },
                    LagramConfig.folderTabsStyle,
                    which -> {
                        LagramConfig.setFolderTabsStyle(which);
                        refreshLivePreviews();
                        getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                    }
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_FOLDER_COUNTERS,
                    R.drawable.msg_notifications,
                    LocaleController.getString(R.string.LagramShowFolderCounters),
                    "Mostrar burbujas con el número de chats sin leer en cada carpeta",
                    LagramConfig.showFolderCounters
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_HIDE_ALL_CHATS,
                    R.drawable.msg_folders,
                    LocaleController.getString(R.string.LagramHideAllChats),
                    "Ocultar la pestaña 'Todos' cuando tienes carpetas personalizadas",
                    LagramConfig.hideAllChatsFolder
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_REMEMBER_LAST_FOLDER,
                    R.drawable.msg_pin,
                    LocaleController.getString(R.string.LagramRememberLastFolder),
                    "Abrir Lagram en la última carpeta usada",
                    LagramConfig.rememberLastFolder
            ));
            items.add(UItem.asShadow(null));
            return;
        }

        if (subPage == SUB_CLEAN_UI) {
            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramUiVisibilityHeader)));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_HIDE_STORIES,
                    R.drawable.msg_menu_stories,
                    LocaleController.getString(R.string.LagramHideStories),
                    "Ocultar la barra superior de historias en la lista de chats",
                    LagramConfig.hideStories
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_HIDE_STORIES_PROFILE,
                    R.drawable.msg_openprofile,
                    LocaleController.getString(R.string.LagramHideStoriesProfile),
                    "Desactivar el aro de historias en avatares de perfiles",
                    LagramConfig.hideStoriesInProfile
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_HIDE_STORY_POSTING,
                    R.drawable.msg_camera,
                    LocaleController.getString(R.string.LagramHideStoryPosting),
                    "Ocultar el botón flotante de cámara para publicar historias",
                    LagramConfig.hideStoryPosting
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_HIDE_PHONE_NUMBER,
                    R.drawable.msg_calls,
                    LocaleController.getString(R.string.LagramHidePhoneNumber),
                    "Ocultar tu número telefónico en ajustes y menú lateral",
                    LagramConfig.hidePhoneNumber
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_HIDE_TELEGRAM_BLOAT,
                    R.drawable.msg_clear,
                    LocaleController.getString(R.string.LagramHideTelegramBloat),
                    "Ocultar secciones promocionales de Estrellas, Regalos y Negocios",
                    LagramConfig.hideTelegramBloat
            ));
            items.add(UItem.asShadow(null));
            return;
        }

        if (subPage == SUB_INTERFACE_DETAILS) {
            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramDetailsHeader)));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_IOS27_BOTTOM_BAR,
                    R.drawable.msg_theme,
                    LocaleController.getString(R.string.LagramIos27BottomBar),
                    "Barra inferior flotante estilo cápsula Liquid Glass",
                    LagramConfig.ios27BottomBar
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_AMOLED_BACKDROP,
                    R.drawable.msg_theme,
                    LocaleController.getString(R.string.LagramAmoledSettings),
                    "Usar negro puro (#000000) detrás de las tarjetas en modo oscuro",
                    LagramConfig.amoledSettingsBackdrop
            ));
            items.add(LagramSettingsComponents.SegmentedPillCell.Factory.of(
                    ID_ANIM_SPEED,
                    LocaleController.getString(R.string.LagramAnimationSpeed),
                    new String[]{
                            LocaleController.getString(R.string.LagramAnimNormal),
                            LocaleController.getString(R.string.LagramAnimFast),
                            LocaleController.getString(R.string.LagramAnimInstant)
                    },
                    LagramConfig.animationSpeed,
                    LagramConfig::setAnimationSpeed
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_SYSTEM_FONTS,
                    R.drawable.msg_photo_text2,
                    LocaleController.getString(R.string.LagramUseSystemFonts),
                    "Usar la tipografía nativa del sistema Android en toda la app",
                    LagramConfig.useSystemFonts
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_SHOW_SECONDS,
                    R.drawable.msg_recent,
                    LocaleController.getString(R.string.LagramShowSeconds),
                    "Formato HH:mm:ss en las marcas de hora de los mensajes",
                    LagramConfig.showSecondsInTime
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_SHOW_ID_DC,
                    R.drawable.msg_info,
                    LocaleController.getString(R.string.LagramShowIdAndDc),
                    "Mostrar ID de usuario/canal y Datacenter (DC) en perfiles",
                    LagramConfig.showIdAndDc
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_ID_BOT_API,
                    R.drawable.msg_bot,
                    LocaleController.getString(R.string.LagramIdFormatBotApi),
                    "Añadir prefijo -100 a los IDs de supergrupos y canales",
                    LagramConfig.idFormatBotApi
            ));
            items.add(UItem.asShadow(LocaleController.getString(R.string.LagramAppearanceInfo)));
        }
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_NAV_BUBBLES:
                presentFragment(new LagramAppearanceActivity(SUB_BUBBLES_AND_GLASS));
                return;
            case ID_NAV_FOLDERS:
                presentFragment(new LagramAppearanceActivity(SUB_FOLDERS));
                return;
            case ID_NAV_CLEAN_UI:
                presentFragment(new LagramAppearanceActivity(SUB_CLEAN_UI));
                return;
            case ID_NAV_DETAILS:
                presentFragment(new LagramAppearanceActivity(SUB_INTERFACE_DETAILS));
                return;
        }

        boolean checkChanged = false;
        boolean newVal = false;
        switch (item.id) {
            case ID_GLASS_MESSAGES:
                newVal = LagramConfig.toggleGlassMessages();
                checkChanged = true;
                if (listView != null && listView.adapter != null) {
                    listView.adapter.update(true);
                }
                refreshLivePreviews();
                break;
            case ID_GLASS_BORDER:
                newVal = LagramConfig.toggleGlassMessagesBorder();
                checkChanged = true;
                refreshLivePreviews();
                break;
            case ID_BUBBLE_TAILS:
                newVal = LagramConfig.toggleShowBubbleTails();
                checkChanged = true;
                refreshLivePreviews();
                break;
            case ID_CHAT_EDGE_BLUR:
                newVal = LagramConfig.toggleChatEdgeBlur();
                checkChanged = true;
                break;
            case ID_SCROLL_EDGE_SCALE:
                newVal = LagramConfig.toggleScrollEdgeScale();
                checkChanged = true;
                break;
            case ID_IOS27_BOTTOM_BAR:
                newVal = LagramConfig.toggleIos27BottomBar();
                checkChanged = true;
                break;
            case ID_AMOLED_BACKDROP:
                newVal = LagramConfig.toggleAmoledSettingsBackdrop();
                checkChanged = true;
                updateBackdropColor();
                if (listView != null) listView.invalidate();
                break;
            case ID_SYSTEM_FONTS:
                newVal = LagramConfig.toggleUseSystemFonts();
                checkChanged = true;
                break;
            case ID_FOLDER_COUNTERS:
                newVal = LagramConfig.toggleShowFolderCounters();
                checkChanged = true;
                refreshLivePreviews();
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                break;
            case ID_HIDE_ALL_CHATS:
                newVal = LagramConfig.toggleHideAllChatsFolder();
                checkChanged = true;
                refreshLivePreviews();
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                break;
            case ID_REMEMBER_LAST_FOLDER:
                newVal = LagramConfig.toggleRememberLastFolder();
                checkChanged = true;
                break;
            case ID_HIDE_STORIES:
                newVal = LagramConfig.toggleHideStories();
                checkChanged = true;
                getNotificationCenter().postNotificationName(NotificationCenter.storiesEnabledUpdate);
                break;
            case ID_HIDE_STORIES_PROFILE:
                newVal = LagramConfig.toggleHideStoriesInProfile();
                checkChanged = true;
                break;
            case ID_HIDE_STORY_POSTING:
                newVal = LagramConfig.toggleHideStoryPosting();
                checkChanged = true;
                getNotificationCenter().postNotificationName(NotificationCenter.storiesEnabledUpdate);
                break;
            case ID_HIDE_PHONE_NUMBER:
                newVal = LagramConfig.toggleHidePhoneNumber();
                checkChanged = true;
                getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, 0);
                break;
            case ID_HIDE_TELEGRAM_BLOAT:
                newVal = LagramConfig.toggleHideTelegramBloat();
                checkChanged = true;
                break;
            case ID_SHOW_SECONDS:
                newVal = LagramConfig.toggleShowSecondsInTime();
                checkChanged = true;
                refreshLivePreviews();
                break;
            case ID_SHOW_ID_DC:
                newVal = LagramConfig.toggleShowIdAndDc();
                checkChanged = true;
                break;
            case ID_ID_BOT_API:
                newVal = LagramConfig.toggleIdFormatBotApi();
                checkChanged = true;
                break;
        }
        if (checkChanged && view instanceof LagramSettingsComponents.IconSwitchCell) {
            ((LagramSettingsComponents.IconSwitchCell) view).setChecked(newVal);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
