package org.telegram.ui.lagram;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Main Lagram Settings Center hub featuring Graphite category cards,
 * JSON export/import backup, and global reset.
 */
public class LagramSettingsActivity extends LagramBaseSettingsFragment {

    @Override
    protected String getScreenTitle() {
        return "Ajustes de Lagram";
    }

    @Override
    protected void buildContent(Context context, LinearLayout root) {
        LagramThemeEngine.AccentColors accent = LagramThemeEngine.getAccentColors();

        // Hero summary Graphite card
        LinearLayout heroCard = LagramCardUi.createGraphiteCard(context);
        heroCard.setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(16), AndroidUtilities.dp(18), AndroidUtilities.dp(16));

        TextView heroTitle = new TextView(context);
        heroTitle.setText("Lagram Studio");
        heroTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19);
        heroTitle.setTypeface(AndroidUtilities.bold());
        heroTitle.setTextColor(LagramThemeEngine.getGraphiteTextPrimary());
        heroCard.addView(heroTitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView heroSub = new TextView(context);
        heroSub.setText("Personalización modular Material 3 · 120 Hz · Liquid Glass apagado por defecto (solo desde Ahorro de batería).");
        heroSub.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        heroSub.setTextColor(LagramThemeEngine.getGraphiteTextSecondary());
        heroCard.addView(heroSub, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 0));

        addCardWithMargin(root, heroCard);

        // Category cards
        root.addView(LagramCardUi.createSectionHeader(context, "Categorías"));

        addCardWithMargin(root, LagramCardUi.createCategoryNavCard(
                context,
                "🎨",
                "Apariencia",
                "AMOLED puro, acento M3, icono, lista de chats, barra inferior, avatares, tipografía y animaciones",
                accent.primary,
                () -> presentFragment(new LagramAppearanceSettingsActivity())
        ));

        addCardWithMargin(root, LagramCardUi.createCategoryNavCard(
                context,
                "💬",
                "Chats",
                "Cabecera superior, burbujas y colas, barra de entrada iOS/M3, doble toque y acciones rápidas",
                0xFF22C58B,
                () -> presentFragment(new LagramChatsSettingsActivity())
        ));

        addCardWithMargin(root, LagramCardUi.createCategoryNavCard(
                context,
                "🎵",
                "Reproductor y Multimedia",
                "Onda animada, paleta dinámica de carátula, reproductor clásico, gestos y calidad foto/video",
                0xFF8B6CFF,
                () -> presentFragment(new LagramMediaSettingsActivity())
        ));

        addCardWithMargin(root, LagramCardUi.createCategoryNavCard(
                context,
                "🛡️",
                "Privacidad y Utilidades",
                "Traductor paralelo, confirmaciones antes de llamar o enviar stickers y ocultar teléfono",
                0xFFF5A623,
                () -> presentFragment(new LagramPrivacySettingsActivity())
        ));

        // Backup & Reset Graphite card
        root.addView(LagramCardUi.createSectionHeader(context, "Respaldo y Configuración"));

        LinearLayout backupCard = LagramCardUi.createGraphiteCard(context);
        backupCard.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(14), AndroidUtilities.dp(14), AndroidUtilities.dp(14));

        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView exportBtn = LagramCardUi.createActionButton(
                context,
                "Exportar JSON",
                accent.primary,
                accent.onPrimary,
                () -> exportConfig(context)
        );
        TextView importBtn = LagramCardUi.createActionButton(
                context,
                "Importar JSON",
                LagramThemeEngine.GRAPHITE_CARD_ELEVATED,
                LagramThemeEngine.getGraphiteTextPrimary(),
                () -> showImportDialog(context)
        );

        btnRow.addView(exportBtn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, 0, 0, 6, 0));
        btnRow.addView(importBtn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, 6, 0, 0, 0));
        backupCard.addView(btnRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 10));

        TextView resetAllBtn = LagramCardUi.createActionButton(
                context,
                "Restablecer todos los ajustes de Lagram",
                LagramThemeEngine.withAlpha(0xFFE54D66, 38),
                0xFFFF7A8F,
                () -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Restablecer todo");
                    builder.setMessage("Se restaurarán todas las categorías de Lagram a sus valores predeterminados.");
                    builder.setPositiveButton("Restablecer", (d, w) -> {
                        LagramConfig.resetAll();
                        rebuildContent(context);
                        Toast.makeText(context, "Ajustes de Lagram restablecidos", Toast.LENGTH_SHORT).show();
                    });
                    builder.setNegativeButton("Cancelar", null);
                    showDialog(builder.create());
                }
        );
        backupCard.addView(resetAllBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addCardWithMargin(root, backupCard);
    }

    private void exportConfig(Context context) {
        String json = LagramConfig.exportToJson();
        try {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("LagramConfig", json));
            }
            File outDir = context.getExternalFilesDir(null);
            if (outDir != null) {
                File outFile = new File(outDir, "lagram_config.json");
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(json.getBytes(StandardCharsets.UTF_8));
                }
            }
            Toast.makeText(context, "Configuración JSON copiada y guardada en lagram_config.json", Toast.LENGTH_LONG).show();
        } catch (Throwable e) {
            Toast.makeText(context, "JSON copiado al portapapeles", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImportDialog(Context context) {
        EditText input = new EditText(context);
        input.setHint("Pega aquí tu JSON de Lagram...");
        input.setMinLines(4);
        input.setMaxLines(10);
        input.setTypeface(Typeface.MONOSPACE);
        input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        input.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(10), AndroidUtilities.dp(14), AndroidUtilities.dp(10));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Importar configuración JSON");
        builder.setView(input);
        builder.setPositiveButton("Importar", (d, w) -> {
            String raw = input.getText() != null ? input.getText().toString() : "";
            if (LagramConfig.importFromJson(raw)) {
                rebuildContent(context);
                Toast.makeText(context, "Configuración importada correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "JSON inválido o vacío", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        showDialog(builder.create());
    }
}
