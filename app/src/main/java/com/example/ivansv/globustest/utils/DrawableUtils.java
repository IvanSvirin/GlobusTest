package com.example.ivansv.globustest.utils;

import android.graphics.drawable.Drawable;

/**
 * Created by ivansv on 25.02.2016.
 */
public class DrawableUtils {
    private static final int[] EMPTY_STATE = new int[] {};

    public static void clearState(Drawable drawable) {
        if (drawable != null) {
            drawable.setState(EMPTY_STATE);
        }
    }
}
