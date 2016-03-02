package com.example.ivansv.globustest.model;

public abstract class AbstractDataProvider {

    public static abstract class Data {
        public abstract long getId();

        public abstract int getViewType();

        public abstract String getText();
    }

    public abstract int getCount();

    public abstract Data getItem(int index);

    public abstract void moveItem(int fromPosition, int toPosition);
}
