package com.example.ivansv.globustest.model;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.ivansv.globustest.background.SqLiteRequestService;
import com.example.ivansv.globustest.sqliteprovider.MyDataContract;
import com.example.ivansv.globustest.sqliteprovider.MyDataProvider;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExampleDataProvider extends AbstractDataProvider implements MyResultReceiver.Receiver {
    private Activity activity;
    private List<ConcreteData> mData;
    private MyResultReceiver myResultReceiver;

    public ExampleDataProvider(Activity activity) {
        this.activity = activity;
        MyDataProvider myDataProvider = new MyDataProvider();
        Cursor cursor = myDataProvider.query(MyDataContract.Notes.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() == 0) {
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
                        mData.add(new ConcreteData(id, viewType, text, swipeReaction, String.valueOf(id + 1L),
                                String.valueOf(j + i * atoz.length())));
                        ContentValues cVal = new ContentValues();
                        cVal.put(MyDataContract.Notes.COLUMN_NAME_NOTE_TEXT, id + " - " + text);
                        cVal.put(MyDataContract.Notes.COLUMN_NAME_PREVIOUS_NOTE_ID, String.valueOf(j + i * atoz.length()));
                        cv.add(cVal);
                    }
                }
                cursor.close();
                backgroundInsert(cv);
            } else {
                mData = new LinkedList<>();
                backgroundLoad();
            }
        }
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
        return 0;
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        String movedSqlId = mData.get(fromPosition).getSqlId();
        String wasAfterMovedSqlId;
        String becameAfterMovedSqlId;
        String movedSqlPrevIdNew;
        String wasAfterMovedSqlPrevIdNew;
        String becameAfterMovedSqlPrevIdNew;
        if (fromPosition > toPosition) {
            becameAfterMovedSqlId = mData.get(toPosition).getSqlId();
            movedSqlPrevIdNew = mData.get(toPosition).getPrevSqlId();
            becameAfterMovedSqlPrevIdNew = movedSqlId;
            if (fromPosition == (mData.size() - 1)) {
                wasAfterMovedSqlId = "Empty";
                wasAfterMovedSqlPrevIdNew = "Empty";
            } else {
                wasAfterMovedSqlId = mData.get(fromPosition + 1).getSqlId();
                wasAfterMovedSqlPrevIdNew = mData.get(fromPosition).getPrevSqlId();
                mData.get(fromPosition + 1).setPrevSqlId(wasAfterMovedSqlPrevIdNew);
            }
            mData.get(toPosition).setPrevSqlId(becameAfterMovedSqlPrevIdNew);
        } else {
            wasAfterMovedSqlId = mData.get(fromPosition + 1).getSqlId();
            movedSqlPrevIdNew = mData.get(toPosition + 1).getPrevSqlId();
            wasAfterMovedSqlPrevIdNew = mData.get(fromPosition).getPrevSqlId();
            if (toPosition == (mData.size() - 1)) {
                becameAfterMovedSqlId = "Empty";
                becameAfterMovedSqlPrevIdNew = "Empty";
            } else {
                becameAfterMovedSqlId = mData.get(toPosition + 1).getSqlId();
                becameAfterMovedSqlPrevIdNew = movedSqlId;
                mData.get(toPosition + 1).setPrevSqlId(becameAfterMovedSqlPrevIdNew);
            }
            mData.get(fromPosition + 1).setPrevSqlId(wasAfterMovedSqlPrevIdNew);
        }
        mData.get(fromPosition).setPrevSqlId(movedSqlPrevIdNew);
        backgroundMove(movedSqlId, wasAfterMovedSqlId, becameAfterMovedSqlId,
                movedSqlPrevIdNew, wasAfterMovedSqlPrevIdNew, becameAfterMovedSqlPrevIdNew);

        final ConcreteData item = mData.remove(fromPosition);
        mData.add(toPosition, item);
    }

    private void backgroundLoad() {
        myResultReceiver = new MyResultReceiver(new Handler());
        myResultReceiver.setReceiver(this);
        Intent loadIntent = new Intent(activity.getApplicationContext(), SqLiteRequestService.class);
        loadIntent.setAction(SqLiteRequestService.ACTION_LOAD);
        loadIntent.putExtra(MyResultReceiver.RECEIVER, myResultReceiver);
        activity.getApplicationContext().startService(loadIntent);
    }

    private void backgroundInsert(ArrayList<ContentValues> cv) {
        Intent insertIntent = new Intent(activity.getApplicationContext(), SqLiteRequestService.class);
        insertIntent.setAction(SqLiteRequestService.ACTION_INSERT);
        insertIntent.putExtra(SqLiteRequestService.CONTENT_VALUES, cv);
        activity.getApplicationContext().startService(insertIntent);
    }


    private void backgroundMove(String movedSqlId, String wasAfterMovedSqlId, String becameAfterMovedSqlId,
                                String movedSqlPrevIdNew, String wasAfterMovedSqlPrevIdNew, String becameAfterMovedSqlPrevIdNew) {
        Intent moveIntent = new Intent(activity.getApplicationContext(), SqLiteRequestService.class);
        moveIntent.setAction(SqLiteRequestService.ACTION_MOVE);
        moveIntent.putExtra(SqLiteRequestService.MOVED_SQL_ID, movedSqlId);
        moveIntent.putExtra(SqLiteRequestService.WAS_AFTER_MOVED_SQL_ID, wasAfterMovedSqlId);
        moveIntent.putExtra(SqLiteRequestService.BECAME_AFTER_MOVED_SQL_ID, becameAfterMovedSqlId);
        moveIntent.putExtra(SqLiteRequestService.MOVED_SQL_PREV_ID_NEW, movedSqlPrevIdNew);
        moveIntent.putExtra(SqLiteRequestService.WAS_AFTER_MOVED_SQL_PREV_ID_NEW, wasAfterMovedSqlPrevIdNew);
        moveIntent.putExtra(SqLiteRequestService.BECAME_AFTER_MOVED_SQL_PREV_ID_NEW, becameAfterMovedSqlPrevIdNew);
        activity.getApplicationContext().startService(moveIntent);
    }

    @Override
    public void removeItem(int position) {
    }

    public static final class ConcreteData extends Data implements Parcelable{

        private long id;
        private String text;
        private int viewType;
        private boolean pinned;
        private String sqlId;
        private String prevSqlId;

        public ConcreteData() {
        }

        ConcreteData(long id, int viewType, String text, int swipeReaction, String sqliteId, String prevSqlId) {
            this.id = id;
            this.viewType = viewType;
            this.sqlId = sqliteId;
            this.prevSqlId = prevSqlId;
            this.text = makeText(id, text, swipeReaction);
        }

        protected ConcreteData(Parcel in) {
            id = in.readLong();
            text = in.readString();
            viewType = in.readInt();
            pinned = in.readByte() != 0;
            sqlId = in.readString();
            prevSqlId = in.readString();
        }

        public static final Creator<ConcreteData> CREATOR = new Creator<ConcreteData>() {
            @Override
            public ConcreteData createFromParcel(Parcel in) {
                return new ConcreteData(in);
            }

            @Override
            public ConcreteData[] newArray(int size) {
                return new ConcreteData[size];
            }
        };

        private static String makeText(long id, String text, int swipeReaction) {
            final StringBuilder sb = new StringBuilder();
            sb.append(id);
            sb.append(" - ");
            sb.append(text);

            return String.valueOf(sb);
        }

        @Override
        public boolean isSectionHeader() {
            return false;
        }

        @Override
        public int getViewType() {
            return viewType;
        }

        @Override
        public long getId() {
            return id;
        }

        @Override
        public String toString() {
            return text;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public boolean isPinned() {
            return pinned;
        }

        @Override
        public void setPinned(boolean pinned) {
            this.pinned = pinned;
        }

        public String getSqlId() {
            return sqlId;
        }

        public String getPrevSqlId() {
            return prevSqlId;
        }

        public void setPrevSqlId(String prevSqlId) {
            this.prevSqlId = prevSqlId;
        }

        public void setSqlId(String sqlId) {
            this.sqlId = sqlId;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeString(text);
            dest.writeInt(viewType);
            dest.writeByte((byte) (pinned ? 1 : 0));
            dest.writeString(sqlId);
            dest.writeString(prevSqlId);
        }
    }

        @Override
        public void onReceiveResult(int resultCode, Bundle data) {
            ArrayList<ConcreteData> receivedList = data.getParcelableArrayList(SqLiteRequestService.DATA_LIST);
            if (receivedList != null) {
                int link = 0;
                while (receivedList.size() > 0) {
                    for (int i = 0; i < receivedList.size(); i++) {
                        if (Integer.parseInt(receivedList.get(i).getPrevSqlId()) == link) {
                            mData.add(receivedList.get(i));
                            link = Integer.parseInt(receivedList.get(i).getSqlId());
                            receivedList.remove(i);
                            break;
                        }
                    }
                }
            }
            myResultReceiver.setReceiver(null);
        }


}
