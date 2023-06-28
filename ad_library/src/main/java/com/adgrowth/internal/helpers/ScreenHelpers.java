package com.adgrowth.internal.helpers;

import android.content.Context;
import android.content.res.Configuration;

public class ScreenHelpers {
    public static String getOrientation(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;

        return orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "PORTRAIT";
    }
}
