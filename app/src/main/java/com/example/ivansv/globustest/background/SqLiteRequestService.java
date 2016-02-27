package com.example.ivansv.globustest.background;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.example.ivansv.globustest.sqliteprovider.MyDataContract;
import com.example.ivansv.globustest.sqliteprovider.MyDataProvider;

import java.util.ArrayList;

/**
 * Created by ivansv on 27.02.2016.
 */
public class SqLiteRequestService extends IntentService {
    public static final String ACTION_INSERT = "action_insert";
    public static final String CONTENT_URI = "content_uri";
    public static final String CONTENT_VALUES = "content_values";
    public static final String ACTION_MOVE = "action_move";
    public static final String MOVED_SQLITE_ID = "movedSqliteId";
    public static final String WAS_AFTER_MOVED_SQLITE_ID = "wasAfterMovedSqliteId";
    public static final String BECAME_AFTER_MOVED_SQLITE_ID = "becameAfterMovedSqliteId";
    private MyDataProvider myDataProvider;

    public SqLiteRequestService() {
        super("SqLiteRequestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        myDataProvider = new MyDataProvider();
        switch (intent.getAction()) {
            case ACTION_INSERT:
                Uri uriInsert = MyDataContract.Notes.CONTENT_URI;
                ArrayList<ContentValues> contentValues = new ArrayList<>();
                contentValues = intent.getParcelableArrayListExtra(CONTENT_VALUES);
                for (ContentValues cv : contentValues) {
                    myDataProvider.insert(uriInsert, cv);
                }
                break;
            case ACTION_MOVE:
                Uri uriUpdate = MyDataContract.Notes.CONTENT_ID_URI_BASE;
                String movedSqliteId = intent.getStringExtra(MOVED_SQLITE_ID);
                String wasAfterMovedSqliteId = intent.getStringExtra(WAS_AFTER_MOVED_SQLITE_ID);
                String becameAfterMovedSqliteId = intent.getStringExtra(BECAME_AFTER_MOVED_SQLITE_ID);
                ContentValues movedCv = new ContentValues();
                ContentValues wasAfterMovedCv = new ContentValues();
                ContentValues becameAfterMovedCv = new ContentValues();
                if (wasAfterMovedSqliteId.equals("0")) {
                    movedCv.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, );
                    becameAfterMovedCv.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, );
                    myDataProvider.update(uriUpdate, );
                    myDataProvider.update(uriUpdate, );
                } else if (becameAfterMovedSqliteId.equals("0")) {
                    movedCv.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, );
                    wasAfterMovedCv.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, );
                    myDataProvider.update(uriUpdate, );
                    myDataProvider.update(uriUpdate, );
                } else {
                    Cursor movedCursor = myDataProvider.query();
                    String movedPrevId = movedCursor.getString(movedCursor
                            .getColumnIndex(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID));
                    movedCv.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, );
                    wasAfterMovedCv.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, movedPrevId);
                    becameAfterMovedCv.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, );
                    myDataProvider.update(uriUpdate, );
                    myDataProvider.update(uriUpdate, );
                    myDataProvider.update(uriUpdate, );
                }
                break;
        }
    }
}
