package com.adgrowth.internal.helpers;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import java.util.HashMap;

public class SystemInfoHelpers {

    public static HashMap getSystemInfo(Context context) {
        HashMap deviceInfo = new HashMap();
        deviceInfo.put("brand", Build.BRAND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            deviceInfo.put("device_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        }
        deviceInfo.put("model", Build.MODEL);
        deviceInfo.put("build_id", Build.ID);
        deviceInfo.put("sdk", Build.VERSION.SDK_INT);
        deviceInfo.put("user", Build.USER);
        deviceInfo.put("type", Build.TYPE);
        deviceInfo.put("base", Build.VERSION_CODES.BASE);
        deviceInfo.put("incremental", Build.VERSION.INCREMENTAL);
        deviceInfo.put("board", Build.BOARD);
        deviceInfo.put("host", Build.HOST);
        deviceInfo.put("finger_print", Build.FINGERPRINT);
        deviceInfo.put("version_code", Build.VERSION.RELEASE);
        deviceInfo.put("OS", "android");


        return deviceInfo;
    }

}
