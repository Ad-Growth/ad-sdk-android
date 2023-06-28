package com.adgrowth.internal.helpers;

import com.adgrowth.internal.exceptions.APIIOException;
import com.adgrowth.adserver.exceptions.AdRequestException;

public class IOErrorHandler {
    public static AdRequestException handle(Exception e) {
        if (e instanceof APIIOException) {
            switch (((APIIOException) e).getStatusCode()) {
                case 0:
                    return new AdRequestException(AdRequestException.NETWORK_ERROR);
                case 404:
                    if (e.getMessage().contains("No ads found"))
                        return new AdRequestException(AdRequestException.NO_AD_FOUND);
                    break;
                case 400:
                    if (e.getMessage().contains("Unit_id invalid"))
                        return new AdRequestException(AdRequestException.INVALID_UNIT_ID);

                    if (e.getMessage().contains("Client_key invalid"))
                        return new AdRequestException(AdRequestException.INVALID_CLIENT_KEY);

                    break;
                case 500:
                case 502:
                    return new AdRequestException(AdRequestException.INTERNAL_ERROR);
            }

        }
        return new AdRequestException(AdRequestException.UNKNOWN_ERROR);

    }
}
