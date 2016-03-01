package com.example.ivansv.globustest.background;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.example.ivansv.globustest.model.ExampleDataProvider;
import com.example.ivansv.globustest.model.MyResultReceiver;
import com.example.ivansv.globustest.sqliteprovider.MyDataContract;
import com.example.ivansv.globustest.sqliteprovider.MyDataProvider;

import java.util.ArrayList;

public class SqLiteRequestService extends IntentService {
    public static final String ACTION_INSERT = "action_insert";
    public static final String CONTENT_URI = "content_uri";
    public static final String CONTENT_VALUES = "content_values";
    public static final String ACTION_MOVE = "action_move";
    public static final String MOVED_SQL_ID = "movedSqlId";
    public static final String WAS_AFTER_MOVED_SQL_ID = "wasAfterMovedSqlId";
    public static final String BECAME_AFTER_MOVED_SQL_ID = "becameAfterMovedSqlId";
    public static final String MOVED_SQL_PREV_ID_NEW = "movedSqlPrevIdNew";
    public static final String WAS_AFTER_MOVED_SQL_PREV_ID_NEW = "wasAfterMovedSqlPrevIdNew";
    public static final String BECAME_AFTER_MOVED_SQL_PREV_ID_NEW = "becameAfterMovedSqlprevIdNew";
    public static final String ACTION_LOAD = "action_load";
    public static final String DATA_LIST = "data_list";
    private Uri uri = MyDataContract.Notes.CONTENT_URI;

    public SqLiteRequestService() {
        super("SqLiteRequestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MyDataProvider myDataProvider = new MyDataProvider();
        switch (intent.getAction()) {
            case ACTION_INSERT:
                ArrayList<ContentValues> contentValues = intent.getParcelableArrayListExtra(CONTENT_VALUES);
                for (ContentValues cv : contentValues) {
                    myDataProvider.insert(uri, cv);
                }
                break;
            case ACTION_MOVE:
                String movedSqlId = intent.getStringExtra(MOVED_SQL_ID);
                String wasAfterMovedSqlId = intent.getStringExtra(WAS_AFTER_MOVED_SQL_ID);
                String becameAfterMovedSqlId = intent.getStringExtra(BECAME_AFTER_MOVED_SQL_ID);
                String movedSqlPrevIdNew = intent.getStringExtra(MOVED_SQL_PREV_ID_NEW);
                String wasAfterMovedSqlPrevIdNew = intent.getStringExtra(WAS_AFTER_MOVED_SQL_PREV_ID_NEW);
                String becameAfterMovedSqlPrevIdNew = intent.getStringExtra(BECAME_AFTER_MOVED_SQL_PREV_ID_NEW);
                ContentValues movedCv = new ContentValues();
                movedCv.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, movedSqlPrevIdNew);
                ContentValues wasAfterMovedCv = new ContentValues();
                wasAfterMovedCv.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, wasAfterMovedSqlPrevIdNew);
                ContentValues becameAfterMovedCv = new ContentValues();
                becameAfterMovedCv.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, becameAfterMovedSqlPrevIdNew);
                myDataProvider.update(uri, movedCv, MyDataContract.Notes._ID + "=?",
                        new String[]{movedSqlId});
                if (!wasAfterMovedSqlId.equals("Empty")) {
                    myDataProvider.update(uri, wasAfterMovedCv, MyDataContract.Notes._ID + "=?",
                            new String[]{wasAfterMovedSqlId});
                }
                if (!becameAfterMovedSqlId.equals("Empty")) {
                    myDataProvider.update(uri, becameAfterMovedCv, MyDataContract.Notes._ID + "=?",
                            new String[]{becameAfterMovedSqlId});
                }
                break;
            case ACTION_LOAD:
                ArrayList<ExampleDataProvider.ConcreteData> dataList = new ArrayList<>();
                Cursor cursor = myDataProvider.query(uri, null, null, null, MyDataContract.Notes.SORT_ORDER_BY_PREVIOUS_NOTE_ID);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int sqlIdIndex = cursor.getColumnIndex(MyDataContract.Notes._ID);
                        int noteTextIndex = cursor.getColumnIndex(MyDataContract.Notes.COLUMN_NAME_NOTE_TEXT);
                        int prevNoteSqlIdIndex = cursor.getColumnIndex(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID);
                        do {
                            ExampleDataProvider.ConcreteData note = new ExampleDataProvider.ConcreteData();
                            note.setSqlId(cursor.getString(sqlIdIndex));
                            note.setText(cursor.getString(noteTextIndex));
                            note.setPrevSqlId(cursor.getString(prevNoteSqlIdIndex));
                            dataList.add(note);
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
                ResultReceiver resultReceiver = intent.getParcelableExtra(MyResultReceiver.RECEIVER);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(DATA_LIST, dataList);
                resultReceiver.send(MyResultReceiver.RESULT, bundle);
                break;
        }
    }
}
