package com.adgrowth.adserver.helpers;

import android.content.res.Resources;

public class ScreenHelpers {
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }
    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

}
