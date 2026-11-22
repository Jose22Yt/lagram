package org.telegram.messenger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LagramMessageHistoryDb extends SQLiteOpenHelper {

    private static final String DB_NAME = "lagram_history.db";
    private static final int DB_VERSION = 1;

    private static volatile LagramMessageHistoryDb instance;
    private static final Set<Long> deletedKeys = ConcurrentHashMap.newKeySet();
    private static final Set<Long> editedKeys = ConcurrentHashMap.newKeySet();
    private static volatile boolean hasAnyDeleted = false;
    private static volatile boolean hasAnyEdited = false;
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

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        try {
            db.enableWriteAheadLogging();
        } catch (Throwable ignore) {}
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

    private static long packKey(int account, long dialogId, int messageId) {
        long h = ((long) (account & 0xF) << 60) ^ (dialogId * 0x9E3779B97F4A7C15L);
        return h ^ ((long) messageId & 0xFFFFFFFFL);
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
                            deletedKeys.add(packKey(c.getInt(0), c.getLong(1), c.getInt(2)));
                            hasAnyDeleted = true;
                        }
                    }
                    try (Cursor c = db.rawQuery("SELECT DISTINCT account, dialog_id, msg_id FROM edited_messages", null)) {
                        while (c.moveToNext()) {
                            editedKeys.add(packKey(c.getInt(0), c.getLong(1), c.getInt(2)));
                            hasAnyEdited = true;
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
        deletedKeys.add(packKey(account, dialogId, messageId));
        hasAnyDeleted = true;
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

    public static final int PREVIEW_DELETED_MSG_ID = -777001;

    public static boolean isMessageDeleted(int account, long dialogId, int messageId) {
        if (messageId == PREVIEW_DELETED_MSG_ID) {
            return true;
        }
        if (!hasAnyDeleted) {
            if (LagramConfig.saveDeletedMessages && !cacheLoaded) {
                ensureCacheLoaded();
            }
            return false;
        }
        return deletedKeys.contains(packKey(account, dialogId, messageId))
                || (dialogId > 0 && deletedKeys.contains(packKey(account, 0, messageId)));
    }

    public static void saveEditVersion(int account, long dialogId, int messageId, String oldText, int editDate) {
        if (oldText == null || oldText.isEmpty()) return;
        editedKeys.add(packKey(account, dialogId, messageId));
        hasAnyEdited = true;
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
        if (!hasAnyEdited) {
            if (LagramConfig.saveEditedMessages && !cacheLoaded) {
                ensureCacheLoaded();
            }
            return false;
        }
        return editedKeys.contains(packKey(account, dialogId, messageId));
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
        hasAnyDeleted = false;
        hasAnyEdited = false;
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
