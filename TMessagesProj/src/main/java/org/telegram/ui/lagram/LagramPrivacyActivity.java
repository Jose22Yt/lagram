package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LagramMessageHistoryDb;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public class LagramPrivacyActivity extends UniversalFragment {

    public static final int SUB_HOME = 0;
    public static final int SUB_GHOST_DETAILS = 1;

    private final int subPage;

    private static final int ID_NAV_GHOST_DETAILS = 100;

    private static final int ID_GHOST_MASTER = 1;
    private static final int ID_GHOST_DONT_READ_MSG = 2;
    private static final int ID_GHOST_DONT_READ_STORIES = 3;
    private static final int ID_GHOST_DONT_SEND_TYPING = 4;
    private static final int ID_GHOST_DONT_SEND_ONLINE = 5;
    private static final int ID_GHOST_OFFLINE_AFTER_SEND = 6;
    private static final int ID_GHOST_MARK_READ_ON_SEND = 7;

    private static final int ID_SAVE_DELETED = 10;
    private static final int ID_SAVE_EDITED = 11;
    private static final int ID_CLEAR_HISTORY_DB = 12;

    private static final int ID_ALLOW_SCREENSHOTS = 20;
    private static final int ID_BYPASS_NO_FORWARDS = 21;

    public LagramPrivacyActivity() {
        this(SUB_HOME);
    }

    public LagramPrivacyActivity(int subPage) {
        super();
        this.subPage = subPage;
    }

    @Override
    protected CharSequence getTitle() {
        if (subPage == SUB_GHOST_DETAILS) {
            return LocaleController.getString(R.string.LagramGhostModeHeader);
        }
        return LocaleController.getString(R.string.LagramCategoryPrivacy);
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
    public void onResume() {
        super.onResume();
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        if (subPage == SUB_GHOST_DETAILS) {
            items.add(UItem.asHeader(LocaleController.getString(R.string.LagramGhostModeHeader)));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_GHOST_MASTER,
                    R.drawable.msg_secret,
                    LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramGhostModeMaster)),
                    "Interruptor maestro que activa las reglas invisibles seleccionadas abajo",
                    LagramConfig.ghostModeEnabled
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_GHOST_DONT_READ_MSG,
                    R.drawable.msg_seen,
                    LocaleController.getString(R.string.LagramGhostDontReadMessages),
                    "Leer chats sin marcar el doble check azul al remitente",
                    LagramConfig.ghostDontReadMessages
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_GHOST_DONT_READ_STORIES,
                    R.drawable.msg_menu_stories,
                    LocaleController.getString(R.string.LagramGhostDontReadStories),
                    "Ver historias de forma anónima sin aparecer en la lista de vistos",
                    LagramConfig.ghostDontReadStories
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_GHOST_DONT_SEND_TYPING,
                    R.drawable.msg_edit,
                    LocaleController.getString(R.string.LagramGhostDontSendTyping),
                    "Ocultar indicadores de 'escribiendo...' o 'grabando audio...'",
                    LagramConfig.ghostDontSendTyping
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_GHOST_DONT_SEND_ONLINE,
                    R.drawable.msg_contacts,
                    LocaleController.getString(R.string.LagramGhostDontSendOnline),
                    "Mantener estado desconectado incluso con Lagram abierto",
                    LagramConfig.ghostDontSendOnline
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_GHOST_OFFLINE_AFTER_SEND,
                    R.drawable.msg_send,
                    LocaleController.getString(R.string.LagramGhostOfflineAfterSend),
                    "Enviar paquete de desconexión inmediatamente tras enviar un mensaje",
                    LagramConfig.ghostSendOfflineAfterSend
            ));
            items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                    ID_GHOST_MARK_READ_ON_SEND,
                    R.drawable.msg_check_s,
                    LocaleController.getString(R.string.LagramGhostMarkReadOnSend),
                    "Marcar los mensajes como leídos automáticamente cuando tú respondas",
                    LagramConfig.ghostMarkReadOnSend
            ));
            items.add(UItem.asShadow(LocaleController.getString(R.string.LagramPrivacyInfo)));
            return;
        }

        // SUB_HOME: Compact Hub with Ghost Master + Sub-fragment card + Anti-Delete + Anti-Restrictions
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramGhostModeHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_GHOST_MASTER,
                R.drawable.msg_secret,
                LagramConfig.withNewBadge(LocaleController.getString(R.string.LagramGhostModeMaster)),
                "Navega, lee mensajes y mira historias sin dejar rastro",
                LagramConfig.ghostModeEnabled
        ));
        items.add(LagramSettingsComponents.SubCategoryCardCell.Factory.of(
                ID_NAV_GHOST_DETAILS,
                IconBackgroundColors.BLUE_DEEP,
                R.drawable.msg_seen,
                "Reglas del Modo Fantasma",
                "Lectura de mensajes, historias, escribiendo y estado en línea",
                LagramConfig.ghostModeEnabled ? "Activo" : "Configurar"
        ));
        items.add(UItem.asShadow(null));

        // Section 2: Anti-Delete & Edit History
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramHistoryHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SAVE_DELETED,
                R.drawable.msg_delete,
                LocaleController.getString(R.string.LagramSaveDeletedMessages),
                "Conservar localmente los mensajes que otros usuarios eliminen",
                LagramConfig.saveDeletedMessages
        ));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_SAVE_EDITED,
                R.drawable.msg_edit,
                LocaleController.getString(R.string.LagramSaveEditedMessages),
                "Guardar todas las versiones anteriores cuando alguien edite un mensaje",
                LagramConfig.saveEditedMessages
        ));
        items.add(UItem.asButton(ID_CLEAR_HISTORY_DB, R.drawable.msg_clear, LocaleController.getString(R.string.LagramClearHistoryDb)).red());
        items.add(UItem.asShadow(null));

        // Section 3: Anti-Restrictions
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramAntiRestrictHeader)));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_ALLOW_SCREENSHOTS,
                R.drawable.msg_camera,
                LocaleController.getString(R.string.LagramAllowScreenshots),
                "Permitir capturas de pantalla en chats secretos y contenido restringido",
                LagramConfig.allowScreenshots
        ));
        items.add(LagramSettingsComponents.IconSwitchCell.Factory.of(
                ID_BYPASS_NO_FORWARDS,
                R.drawable.msg_copy,
                LocaleController.getString(R.string.LagramBypassNoForwards),
                "Permitir copiar texto y guardar multimedia en canales protegidos",
                LagramConfig.bypassNoForwards
        ));
        items.add(UItem.asShadow(LocaleController.getString(R.string.LagramPrivacyInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ID_NAV_GHOST_DETAILS) {
            presentFragment(new LagramPrivacyActivity(SUB_GHOST_DETAILS));
            return;
        }
        if (item.id == ID_CLEAR_HISTORY_DB) {
            LagramMessageHistoryDb.clearAll();
            BulletinFactory.of(this).createSimpleBulletin(R.raw.ic_delete, LocaleController.getString(R.string.LagramHistoryClearedToast)).show();
            return;
        }
        boolean checkChanged = false;
        boolean newVal = false;
        switch (item.id) {
            case ID_GHOST_MASTER:
                newVal = LagramConfig.toggleGhostMode();
                checkChanged = true;
                if (subPage == SUB_HOME && listView != null && listView.adapter != null) {
                    listView.adapter.update(true);
                }
                break;
            case ID_GHOST_DONT_READ_MSG:
                newVal = LagramConfig.toggleGhostDontReadMessages();
                checkChanged = true;
                break;
            case ID_GHOST_DONT_READ_STORIES:
                newVal = LagramConfig.toggleGhostDontReadStories();
                checkChanged = true;
                break;
            case ID_GHOST_DONT_SEND_TYPING:
                newVal = LagramConfig.toggleGhostDontSendTyping();
                checkChanged = true;
                break;
            case ID_GHOST_DONT_SEND_ONLINE:
                newVal = LagramConfig.toggleGhostDontSendOnline();
                checkChanged = true;
                break;
            case ID_GHOST_OFFLINE_AFTER_SEND:
                newVal = LagramConfig.toggleGhostSendOfflineAfterSend();
                checkChanged = true;
                break;
            case ID_GHOST_MARK_READ_ON_SEND:
                newVal = LagramConfig.toggleGhostMarkReadOnSend();
                checkChanged = true;
                break;
            case ID_SAVE_DELETED:
                newVal = LagramConfig.toggleSaveDeletedMessages();
                if (LagramConfig.saveDeletedMessages) {
                    LagramMessageHistoryDb.ensureCacheLoaded();
                }
                checkChanged = true;
                break;
            case ID_SAVE_EDITED:
                newVal = LagramConfig.toggleSaveEditedMessages();
                if (LagramConfig.saveEditedMessages) {
                    LagramMessageHistoryDb.ensureCacheLoaded();
                }
                checkChanged = true;
                break;
            case ID_ALLOW_SCREENSHOTS:
                newVal = LagramConfig.toggleAllowScreenshots();
                checkChanged = true;
                break;
            case ID_BYPASS_NO_FORWARDS:
                newVal = LagramConfig.toggleBypassNoForwards();
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
