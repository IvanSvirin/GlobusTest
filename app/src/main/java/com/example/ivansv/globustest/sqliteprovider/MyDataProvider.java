package com.example.ivansv.globustest.sqliteprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.HashMap;

/**
 * Created by ivansv on 26.02.2016.
 */
public class MyDataProvider extends ContentProvider {
    private static final int DATABASE_VERSION = 1;
    private static HashMap<String, String> sNotesProjectionMap;
    private static final int NOTES = 1;
    private static final int NOTES_ID = 2;
    private static final UriMatcher sUriMatcher;
    private static DatabaseHelper dbHelper;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(MyDataContract.AUTHORITY, "notes", NOTES);
        sUriMatcher.addURI(MyDataContract.AUTHORITY, "notes/#", NOTES_ID);
        sNotesProjectionMap = new HashMap<String, String>();
        for (int i = 0; i < MyDataContract.Notes.DEFAULT_PROJECTION.length; i++) {
            sNotesProjectionMap.put(
                    MyDataContract.Notes.DEFAULT_PROJECTION[i],
                    MyDataContract.Notes.DEFAULT_PROJECTION[i]);
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "mynotes";
        public static final String DATABASE_TABLE_NOTES = MyDataContract.Notes.TABLE_NAME;
        public static final String KEY_ROWID = "_id";
        public static final String KEY_NOTE_TEXT = "note_text";
        public static final String KEY_PREVIOUS_NOTE_ID = "previous_note_id";
        private static final String DATABASE_CREATE_TABLE_NOTES =
                "create table " + DATABASE_TABLE_NOTES + " ("
                        + KEY_ROWID + " integer primary key autoincrement, "
                        + KEY_NOTE_TEXT + " string , "
                        + KEY_PREVIOUS_NOTE_ID + " string );";

        private Context ctx;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            ctx = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_TABLE_NOTES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_NOTES);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy = null;
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                qb.setTables(MyDataContract.Notes.TABLE_NAME);
                qb.setProjectionMap(sNotesProjectionMap);
                orderBy = sortOrder == null ? MyDataContract.Notes.DEFAULT_SORT_ORDER : sortOrder;
                break;
            case NOTES_ID:
                qb.setTables(MyDataContract.Notes.TABLE_NAME);
                qb.setProjectionMap(sNotesProjectionMap);
                qb.appendWhere(MyDataContract.Notes._ID + "=" + uri.getPathSegments()
                        .get(MyDataContract.Notes.NOTES_ID_PATH_POSITION));
                orderBy = MyDataContract.Notes.DEFAULT_SORT_ORDER;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        Cursor c = db.query(DatabaseHelper.DATABASE_TABLE_NOTES, null, selection, selectionArgs, null, null, null);
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
//        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                return MyDataContract.Notes.CONTENT_TYPE;
            case NOTES_ID:
                return MyDataContract.Notes.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        long rowId = -1;
        Uri rowUri = Uri.EMPTY;
        if (!values.containsKey(MyDataContract.Notes.COLUMN_NAME_NOTE_TEXT)) {
            values.put(MyDataContract.Notes.COLUMN_NAME_NOTE_TEXT, "");
        }
        if (!values.containsKey(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID)) {
            values.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, "");
        }
        rowId = db.insert(MyDataContract.Notes.TABLE_NAME,
                MyDataContract.Notes.COLUMN_NAME_NOTE_TEXT,
                values);
        if (rowId > 0) {
            rowUri = ContentUris.withAppendedId(MyDataContract.Notes.CONTENT_ID_URI_BASE, rowId);
//            getContext().getContentResolver().notifyChange(rowUri, null);
        }
        return rowUri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String finalWhere;
        int count;
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                count = db.delete(MyDataContract.Notes.TABLE_NAME, where, whereArgs);
                break;
            case NOTES_ID:
                finalWhere = MyDataContract.Notes._ID + " = " + uri.getPathSegments().get(MyDataContract.Notes.NOTES_ID_PATH_POSITION);
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.delete(MyDataContract.Notes.TABLE_NAME, finalWhere, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
//        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        String finalWhere;
        String id;
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                count = db.update(MyDataContract.Notes.TABLE_NAME, values, where, whereArgs);
                break;
            case NOTES_ID:
                id = uri.getPathSegments().get(MyDataContract.Notes.NOTES_ID_PATH_POSITION);
                finalWhere = MyDataContract.Notes._ID + " = " + id;
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                count = db.update(MyDataContract.Notes.TABLE_NAME, values, finalWhere, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
//        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
