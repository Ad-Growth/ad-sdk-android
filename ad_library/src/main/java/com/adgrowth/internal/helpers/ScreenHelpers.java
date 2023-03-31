package com.adgrowth.internal.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

public class ScreenHelpers {
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static String getOrientation(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;

        return orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "PORTRAIT";
    }

}
