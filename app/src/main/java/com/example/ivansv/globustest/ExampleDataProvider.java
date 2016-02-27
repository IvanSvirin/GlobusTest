package com.example.ivansv.globustest;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

import com.example.ivansv.globustest.background.SqLiteRequestService;
import com.example.ivansv.globustest.sqliteprovider.MyDataContract;
import com.example.ivansv.globustest.sqliteprovider.MyDataProvider;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ivansv on 25.02.2016.
 */
public class ExampleDataProvider extends AbstractDataProvider {
    private Activity activity;
    private List<ConcreteData> mData;
    private ConcreteData mLastRemovedData;
    private int mLastRemovedPosition = -1;
    private MyDataProvider myDataProvider;

    public ExampleDataProvider(Activity activity) {
        this.activity = activity;
        myDataProvider = new MyDataProvider();
        Cursor cursor = myDataProvider.query(MyDataContract.Notes.CONTENT_URI, null, null, null, null);
        if (!cursor.isFirst()) {
            final String atoz = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            mData = new LinkedList<>();
            ArrayList<ContentValues> cv = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < atoz.length(); j++) {
                    final long id = mData.size();
                    final int viewType = 0;
                    final String text = Character.toString(atoz.charAt(j));
                    final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP |
                            RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;
                    mData.add(new ConcreteData(id, viewType, text, swipeReaction, String.valueOf(id + 1L)));
                    ContentValues cVal = new ContentValues();
                    cVal.put(MyDataContract.Notes.COLUMN_NAME_NOTE_TEXT, id + " - " + text);
                    cVal.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, String.valueOf(j + i * atoz.length()));
                    cv.add(cVal);
                }
            }
            cursor.close();
            backgroundInsert(cv);
        } else {
            // TODO: 26.02.2016
        }
    }

    private void backgroundInsert(ArrayList<ContentValues> cv) {
        Intent insertIntent = new Intent(activity.getApplicationContext(), SqLiteRequestService.class);
        insertIntent.setAction(SqLiteRequestService.ACTION_INSERT);
        insertIntent.putExtra(SqLiteRequestService.CONTENT_VALUES, cv);
        activity.getApplicationContext().startService(insertIntent);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return mData.get(index);
    }

    @Override
    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < mData.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = mData.size();
            }

            mData.add(insertedPosition, mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        String movedSqliteId = mData.get(fromPosition).getSqliteId();
        String wasAfterMovedSqliteId = fromPosition == mData.size() - 1 ? "0" : mData.get(fromPosition + 1).getSqliteId();
        String becameAfterMovedSqliteId = toPosition == mData.size() - 1 ? "0" : mData.get(toPosition + 1).getSqliteId();
        backgroundMove(movedSqliteId, wasAfterMovedSqliteId, becameAfterMovedSqliteId);

        final ConcreteData item = mData.remove(fromPosition);

        mData.add(toPosition, item);
        mLastRemovedPosition = -1;
    }

    private void backgroundMove(String movedSqliteId, String wasAfterMovedSqliteId, String becameAfterMovedSqliteId) {
        Intent moveIntent = new Intent(activity.getApplicationContext(), SqLiteRequestService.class);
        moveIntent.setAction(SqLiteRequestService.ACTION_MOVE);
        moveIntent.putExtra(SqLiteRequestService.MOVED_SQLITE_ID, movedSqliteId);
        moveIntent.putExtra(SqLiteRequestService.WAS_AFTER_MOVED_SQLITE_ID, wasAfterMovedSqliteId);
        moveIntent.putExtra(SqLiteRequestService.BECAME_AFTER_MOVED_SQLITE_ID, becameAfterMovedSqliteId);
        activity.getApplicationContext().startService(moveIntent);
    }

    @Override
    public void removeItem(int position) {
        //noinspection UnnecessaryLocalVariable
        final ConcreteData removedItem = mData.remove(position);

        mLastRemovedData = removedItem;
        mLastRemovedPosition = position;
    }

    public static final class ConcreteData extends Data {

        private final long mId;
        private final String mText;
        private final int mViewType;
        private boolean mPinned;
        private final String sqliteId;

        ConcreteData(long id, int viewType, String text, int swipeReaction, String sqliteId) {
            mId = id;
            mViewType = viewType;
            this.sqliteId = sqliteId;
            mText = makeText(id, text, swipeReaction);
        }

        private static String makeText(long id, String text, int swipeReaction) {
            final StringBuilder sb = new StringBuilder();

            sb.append(id);
            sb.append(" - ");
            sb.append(text);

            return sb.toString();
        }

        @Override
        public boolean isSectionHeader() {
            return false;
        }

        @Override
        public int getViewType() {
            return mViewType;
        }

        @Override
        public long getId() {
            return mId;
        }

        @Override
        public String toString() {
            return mText;
        }

        @Override
        public String getText() {
            return mText;
        }

        @Override
        public boolean isPinned() {
            return mPinned;
        }

        @Override
        public void setPinned(boolean pinned) {
            mPinned = pinned;
        }

        public String getSqliteId() {
            return sqliteId;
        }
    }
}
