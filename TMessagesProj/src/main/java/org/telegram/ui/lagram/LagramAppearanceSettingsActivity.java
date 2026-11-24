package org.telegram.ui.lagram;

import android.content.Context;
import android.widget.LinearLayout;

import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Apariencia subcategory fragment: live preview + Graphite cards for
 * Chat List Styles, Avatars, Folder Tabs, Unread & Online, Home Title,
 * Profiles/Drawer/Bottom Nav, M3 Dynamic Accent, AMOLED Pure Black,
 * Typography, and Animations/Haptics.
 */
public class LagramAppearanceSettingsActivity extends LagramBaseSettingsFragment {

    private LagramPreviewViews.AppearanceLivePreviewView previewView;

    @Override
    protected String getScreenTitle() {
        return "Apariencia";
    }

    private void refreshPreviewAndUi() {
        if (previewView != null) {
            previewView.requestLayout();
            previewView.invalidate();
        }
        try {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewTheme, false);
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected void buildContent(Context context, LinearLayout root) {
        // Live preview inside Graphite card
        LinearLayout previewCard = LagramCardUi.createGraphiteCard(context);
        previewView = new LagramPreviewViews.AppearanceLivePreviewView(context);
        previewCard.addView(previewView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addCardWithMargin(root, previewCard);

        // Card 1: Tema, Acento M3 e Icono
        root.addView(LagramCardUi.createSectionHeader(context, "Colores, AMOLED e Icono"));
        LinearLayout themeCard = LagramCardUi.createGraphiteCard(context);
        themeCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Modo Negro Puro (AMOLED)",
                "Fondo #000000 real para chats, lista de chats y ajustes",
                LagramConfig.amoledPureBlack,
                v -> {
                    LagramConfig.setAmoledPureBlack(v);
                    rebuildContent(context);
                    refreshPreviewAndUi();
                }
        ));
        themeCard.addView(LagramCardUi.createDivider(context));
        themeCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Acento Material 3 (Fila 1)",
                "Paleta dinámica para burbujas, contadores y switches",
                new String[]{"Monet", "Azul", "Esmeralda", "Violeta"},
                LagramConfig.accentPalette <= 3 ? LagramConfig.accentPalette : -1,
                idx -> {
                    LagramConfig.setAccentPalette(idx);
                    rebuildContent(context);
                    refreshPreviewAndUi();
                }
        ));
        themeCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Acento Material 3 (Fila 2)",
                null,
                new String[]{"Ámbar", "Carmesí", "AMOLED Mono"},
                LagramConfig.accentPalette >= 4 ? LagramConfig.accentPalette - 4 : -1,
                idx -> {
                    LagramConfig.setAccentPalette(idx + 4);
                    rebuildContent(context);
                    refreshPreviewAndUi();
                }
        ));
        themeCard.addView(LagramCardUi.createDivider(context));
        themeCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Icono de aplicación",
                "Estilo visual del icono de Lagram",
                new String[]{"Lagram", "Oscuro", "AMOLED", "Monet"},
                LagramConfig.appIconStyle,
                idx -> {
                    LagramConfig.setAppIconStyle(idx);
                    refreshPreviewAndUi();
                }
        ));
        addCardWithMargin(root, themeCard);

        // Card 2: Lista de chats y pantalla principal
        root.addView(LagramCardUi.createSectionHeader(context, "Lista de chats e Inicio"));
        LinearLayout listCard = LagramCardUi.createGraphiteCard(context);
        listCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Estilo de fila de chat",
                "Diseño visual de cada conversación en Inicio",
                new String[]{"Clásico", "Tarjetas M3", "Minimalista"},
                LagramConfig.chatListStyle,
                idx -> {
                    LagramConfig.setChatListStyle(idx);
                    refreshPreviewAndUi();
                }
        ));
        listCard.addView(LagramCardUi.createDivider(context));
        listCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Barra de carpetas / pestañas",
                "Estilo de navegación entre carpetas de chats",
                new String[]{"Píldora M3", "Subrayado", "Solo iconos"},
                LagramConfig.folderTabsStyle,
                idx -> {
                    LagramConfig.setFolderTabsStyle(idx);
                    refreshPreviewAndUi();
                }
        ));
        listCard.addView(LagramCardUi.createDivider(context));
        listCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Ocultar pestaña \"Todos los chats\"",
                "Muestra directamente tus carpetas personalizadas",
                LagramConfig.hideAllChatsTab,
                v -> {
                    LagramConfig.setHideAllChatsTab(v);
                    refreshPreviewAndUi();
                }
        ));
        listCard.addView(LagramCardUi.createDivider(context));
        listCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Contador de no leídos",
                "Estilo de la insignia en filas de chat",
                new String[]{"Píldora tonal", "Punto", "Clásico"},
                LagramConfig.unreadBadgeStyle,
                idx -> {
                    LagramConfig.setUnreadBadgeStyle(idx);
                    refreshPreviewAndUi();
                }
        ));
        listCard.addView(LagramCardUi.createDivider(context));
        listCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Posición estado \"en línea\"",
                null,
                new String[]{"Abajo der.", "Arriba der.", "Oculto"},
                LagramConfig.onlineStatusPosition,
                idx -> {
                    LagramConfig.setOnlineStatusPosition(idx);
                    refreshPreviewAndUi();
                }
        ));
        listCard.addView(LagramCardUi.createDivider(context));
        listCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Título superior principal",
                "Texto mostrado en la cabecera de Inicio",
                new String[]{"Lagram", "Usuario", "Carpeta"},
                LagramConfig.homeHeaderTitleMode,
                idx -> {
                    LagramConfig.setHomeHeaderTitleMode(idx);
                    refreshPreviewAndUi();
                }
        ));
        addCardWithMargin(root, listCard);

        // Card 3: Avatares globales
        root.addView(LagramCardUi.createSectionHeader(context, "Avatares globales"));
        LinearLayout avatarCard = LagramCardUi.createGraphiteCard(context);
        avatarCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Forma de avatares",
                "Aplica a la lista de chats y perfiles",
                new String[]{"Círculo", "Squircle iOS", "Cuadrado"},
                LagramConfig.avatarShape,
                idx -> {
                    LagramConfig.setAvatarShape(idx);
                    rebuildContent(context);
                    refreshPreviewAndUi();
                }
        ));
        avatarCard.addView(LagramCardUi.createDivider(context));
        avatarCard.addView(LagramCardUi.createSliderRow(
                context,
                "Radio de esquina del avatar",
                LagramConfig.MIN_AVATAR_RADIUS_DP,
                LagramConfig.MAX_AVATAR_RADIUS_DP,
                LagramConfig.avatarCornerRadius,
                " dp",
                val -> {
                    LagramConfig.setAvatarCornerRadius(val);
                    if (previewView != null) {
                        previewView.invalidate();
                    }
                },
                this::refreshPreviewAndUi
        ));
        avatarCard.addView(LagramCardUi.createDivider(context));
        avatarCard.addView(LagramCardUi.createSliderRow(
                context,
                "Tamaño global de avatares",
                LagramConfig.MIN_AVATAR_SIZE_DP,
                LagramConfig.MAX_AVATAR_SIZE_DP,
                LagramConfig.avatarSizeDp,
                " dp",
                val -> {
                    LagramConfig.setAvatarSizeDp(val);
                    if (previewView != null) {
                        previewView.invalidate();
                    }
                },
                this::refreshPreviewAndUi
        ));
        addCardWithMargin(root, avatarCard);

        // Card 4: Navegación, Menú lateral y Perfiles
        root.addView(LagramCardUi.createSectionHeader(context, "Navegación, Menú y Perfiles"));
        LinearLayout navCard = LagramCardUi.createGraphiteCard(context);
        navCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Barra inferior de navegación",
                "Muestra u oculta la barra inferior M3 Expressive con resorte",
                LagramConfig.bottomNavigationEnabled,
                v -> {
                    LagramConfig.setBottomNavigationEnabled(v);
                    refreshPreviewAndUi();
                }
        ));
        navCard.addView(LagramCardUi.createDivider(context));
        navCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Cabecera de perfiles Banner M3",
                "Píldoras segmentadas (Llamar, Silenciar, Buscar, Más) y tarjeta limpia",
                LagramConfig.profileBannerM3Enabled,
                v -> {
                    LagramConfig.setProfileBannerM3Enabled(v);
                    refreshPreviewAndUi();
                }
        ));
        navCard.addView(LagramCardUi.createDivider(context));
        navCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Estilo de cabecera del menú",
                "Diseño visual del encabezado en el menú principal",
                new String[]{"Dinámico", "Compacto", "Minimalista"},
                LagramConfig.drawerHeaderStyle,
                LagramConfig::setDrawerHeaderStyle
        ));
        navCard.addView(LagramCardUi.createDivider(context));
        navCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Acceso: Nuevo grupo",
                "Mostrar acceso directo en el menú principal",
                LagramConfig.drawerShowNewGroup,
                LagramConfig::setDrawerShowNewGroup
        ));
        navCard.addView(LagramCardUi.createDivider(context));
        navCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Acceso: Contactos",
                null,
                LagramConfig.drawerShowContacts,
                LagramConfig::setDrawerShowContacts
        ));
        navCard.addView(LagramCardUi.createDivider(context));
        navCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Acceso: Llamadas",
                null,
                LagramConfig.drawerShowCalls,
                LagramConfig::setDrawerShowCalls
        ));
        navCard.addView(LagramCardUi.createDivider(context));
        navCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Acceso: Mensajes guardados",
                null,
                LagramConfig.drawerShowSavedMessages,
                LagramConfig::setDrawerShowSavedMessages
        ));
        navCard.addView(LagramCardUi.createDivider(context));
        navCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Acceso: Ajustes",
                null,
                LagramConfig.drawerShowSettings,
                LagramConfig::setDrawerShowSettings
        ));
        addCardWithMargin(root, navCard);

        // Card 5: Tipografía, Animaciones y Háptica
        root.addView(LagramCardUi.createSectionHeader(context, "Tipografía, Animaciones y Háptica"));
        LinearLayout motionCard = LagramCardUi.createGraphiteCard(context);
        motionCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Fuente / Tipografía",
                null,
                new String[]{"Sistema", "Rounded", "Moderna", "Serif", "Mono"},
                LagramConfig.fontFamily,
                idx -> {
                    LagramConfig.setFontFamily(idx);
                    refreshPreviewAndUi();
                }
        ));
        motionCard.addView(LagramCardUi.createDivider(context));
        motionCard.addView(LagramCardUi.createSliderRow(
                context,
                "Escala fina de texto",
                LagramConfig.MIN_TEXT_SCALE,
                LagramConfig.MAX_TEXT_SCALE,
                LagramConfig.textScalePercent,
                "%",
                val -> {
                    LagramConfig.setTextScalePercent(val);
                    if (previewView != null) {
                        previewView.invalidate();
                    }
                },
                this::refreshPreviewAndUi
        ));
        motionCard.addView(LagramCardUi.createDivider(context));
        motionCard.addView(LagramCardUi.createSegmentedSelectorRow(
                context,
                "Preset de animaciones",
                "Optimizado para pantallas de 120 Hz",
                new String[]{"Fluido 120 Hz", "Rápido", "Sin anim."},
                LagramConfig.animationPreset,
                LagramConfig::setAnimationPreset
        ));
        motionCard.addView(LagramCardUi.createDivider(context));
        motionCard.addView(LagramCardUi.createSwitchRow(
                context,
                "Respuesta háptica fina",
                "Vibración táctil suave en botones, barras y gestos",
                LagramConfig.hapticFeedbackEnabled,
                LagramConfig::setHapticFeedbackEnabled
        ));
        addCardWithMargin(root, motionCard);

        addResetCategoryButton(context, root, LagramConfig.CATEGORY_APPEARANCE, "Restablecer Apariencia");
    }
}
