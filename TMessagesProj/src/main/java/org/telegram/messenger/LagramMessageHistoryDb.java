package org.telegram.messenger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class LagramMessageHistoryDb extends SQLiteOpenHelper {

    private static final String DB_NAME = "lagram_history.db";
    private static final int DB_VERSION = 1;

    private static volatile LagramMessageHistoryDb instance;
    private static final Set<String> deletedKeys = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> editedKeys = Collections.synchronizedSet(new HashSet<>());
    private static volatile boolean cacheLoaded = false;

    public static final class EditEntry {
        public final String text;
        public final int date;

        public EditEntry(String text, int date) {
            this.text = text;
            this.date = date;
        }
    }

    private LagramMessageHistoryDb(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static LagramMessageHistoryDb getInstance() {
        if (instance == null && ApplicationLoader.applicationContext != null) {
            synchronized (LagramMessageHistoryDb.class) {
                if (instance == null) {
                    instance = new LagramMessageHistoryDb(ApplicationLoader.applicationContext);
                }
            }
        }
        return instance;
    }

    private static String makeKey(int account, long dialogId, int messageId) {
        return account + ":" + dialogId + ":" + messageId;
    }

    public static void ensureCacheLoaded() {
        if (cacheLoaded) return;
        final LagramMessageHistoryDb dbHelper = getInstance();
        if (dbHelper == null) return;
        synchronized (LagramMessageHistoryDb.class) {
            if (cacheLoaded) return;
            cacheLoaded = true;
            Utilities.globalQueue.postRunnable(() -> {
                try {
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    try (Cursor c = db.rawQuery("SELECT account, dialog_id, msg_id FROM deleted_messages", null)) {
                        while (c.moveToNext()) {
                            deletedKeys.add(makeKey(c.getInt(0), c.getLong(1), c.getInt(2)));
                        }
                    }
                    try (Cursor c = db.rawQuery("SELECT DISTINCT account, dialog_id, msg_id FROM edited_messages", null)) {
                        while (c.moveToNext()) {
                            editedKeys.add(makeKey(c.getInt(0), c.getLong(1), c.getInt(2)));
                        }
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            });
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS deleted_messages (" +
                "account INTEGER NOT NULL, " +
                "dialog_id INTEGER NOT NULL, " +
                "msg_id INTEGER NOT NULL, " +
                "deleted_at INTEGER NOT NULL, " +
                "PRIMARY KEY (account, dialog_id, msg_id))");
        db.execSQL("CREATE TABLE IF NOT EXISTS edited_messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "account INTEGER NOT NULL, " +
                "dialog_id INTEGER NOT NULL, " +
                "msg_id INTEGER NOT NULL, " +
                "old_text TEXT NOT NULL, " +
                "edit_date INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_edited_lookup ON edited_messages(account, dialog_id, msg_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static void markMessageDeleted(int account, long dialogId, int messageId) {
        deletedKeys.add(makeKey(account, dialogId, messageId));
        final LagramMessageHistoryDb dbHelper = getInstance();
        if (dbHelper == null) return;
        Utilities.globalQueue.postRunnable(() -> {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("account", account);
                cv.put("dialog_id", dialogId);
                cv.put("msg_id", messageId);
                cv.put("deleted_at", (int) (System.currentTimeMillis() / 1000L));
                db.insertWithOnConflict("deleted_messages", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }

    public static boolean isMessageDeleted(int account, long dialogId, int messageId) {
        if (!LagramConfig.saveDeletedMessages && deletedKeys.isEmpty()) {
            return false;
        }
        ensureCacheLoaded();
        return deletedKeys.contains(makeKey(account, dialogId, messageId))
                || (dialogId > 0 && deletedKeys.contains(makeKey(account, 0, messageId)));
    }

    public static void saveEditVersion(int account, long dialogId, int messageId, String oldText, int editDate) {
        if (oldText == null || oldText.isEmpty()) return;
        editedKeys.add(makeKey(account, dialogId, messageId));
        final LagramMessageHistoryDb dbHelper = getInstance();
        if (dbHelper == null) return;
        Utilities.globalQueue.postRunnable(() -> {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("account", account);
                cv.put("dialog_id", dialogId);
                cv.put("msg_id", messageId);
                cv.put("old_text", oldText);
                cv.put("edit_date", editDate > 0 ? editDate : (int) (System.currentTimeMillis() / 1000L));
                db.insert("edited_messages", null, cv);
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }

    public static boolean hasEditHistory(int account, long dialogId, int messageId) {
        if (!LagramConfig.saveEditedMessages && editedKeys.isEmpty()) {
            return false;
        }
        ensureCacheLoaded();
        return editedKeys.contains(makeKey(account, dialogId, messageId));
    }

    public static ArrayList<EditEntry> getEditHistory(int account, long dialogId, int messageId) {
        ArrayList<EditEntry> result = new ArrayList<>();
        final LagramMessageHistoryDb dbHelper = getInstance();
        if (dbHelper == null) return result;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try (Cursor c = db.rawQuery(
                    "SELECT old_text, edit_date FROM edited_messages WHERE account=? AND dialog_id=? AND msg_id=? ORDER BY id DESC",
                    new String[]{String.valueOf(account), String.valueOf(dialogId), String.valueOf(messageId)})) {
                while (c.moveToNext()) {
                    result.add(new EditEntry(c.getString(0), c.getInt(1)));
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return result;
    }

    public static void clearAll() {
        deletedKeys.clear();
        editedKeys.clear();
        final LagramMessageHistoryDb dbHelper = getInstance();
        if (dbHelper == null) return;
        Utilities.globalQueue.postRunnable(() -> {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete("deleted_messages", null, null);
                db.delete("edited_messages", null, null);
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }
}
