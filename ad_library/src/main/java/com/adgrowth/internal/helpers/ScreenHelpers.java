package com.adgrowth.internal.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import com.adgrowth.adserver.enums.AdOrientation;

public class ScreenHelpers {
    public static AdOrientation getOrientation(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;

        return orientation == Configuration.ORIENTATION_LANDSCAPE ? AdOrientation.LANDSCAPE : AdOrientation.PORTRAIT;
    }

    public static void setOrientation(Activity context, AdOrientation orientation) {
        context.setRequestedOrientation(orientation == AdOrientation.LANDSCAPE ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }
}
