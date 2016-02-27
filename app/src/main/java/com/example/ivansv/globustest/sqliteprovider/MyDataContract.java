package com.example.ivansv.globustest.sqliteprovider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ivansv on 26.02.2016.
 */
public final class MyDataContract {
    public static final String AUTHORITY = "com.example.ivansv.globustest.sqliteprovider.MyDataContract";
    private MyDataContract() {}
    public static final class Notes implements BaseColumns {
        private Notes() {}
        public static final String TABLE_NAME ="notes";
        private static final String SCHEME = "content://";
        private static final String PATH_NOTES = "/notes";
        private static final String PATH_NOTES_ID = "/notes/";
        public static final int NOTES_ID_PATH_POSITION = 1;
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES_ID);
        //public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_CLASSES_ID + "/#");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.example.notes";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.org.example.notes";
        public static final String DEFAULT_SORT_ORDER = "_id ASC";
        public static final String COLUMN_NAME_NOTE_TEXT = "note_text";
        public static final String COLUMN_NAME_PREVIOUS_NOTE_ID = "previous_note_id";
        public static final String[] DEFAULT_PROJECTION = new String[] {
                MyDataContract.Notes._ID,
                MyDataContract.Notes.COLUMN_NAME_NOTE_TEXT,
                MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID
        };
    }
}
