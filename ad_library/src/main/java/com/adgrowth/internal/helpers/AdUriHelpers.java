package com.adgrowth.internal.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.adgrowth.adserver.AdServer;

import java.util.UUID;

public class AdUriHelpers {
    public static void openUrl(Activity context, String uri, String ipAddress) {

        uri = replaceAdCallbackParams(context, uri, ipAddress);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        context.startActivity(intent);

    }

    public static String replaceAdCallbackParams(Activity context, String uri, String ipAddress) {
        String clickId = UUID.randomUUID().toString();
        String adId = AdServer.getAdId();
        String siteId = context.getPackageName();

        return uri
                .replaceAll("\\{advertising_id\\}", adId)
                .replaceAll("\\{click_id\\}", clickId)
                .replaceAll("\\{site_id\\}", siteId)
                .replaceAll("\\{ip\\}", ipAddress);
    }
}
