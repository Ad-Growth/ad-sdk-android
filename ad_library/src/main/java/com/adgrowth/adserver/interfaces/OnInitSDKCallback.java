package com.adgrowth.adserver.interfaces;

import com.adgrowth.adserver.exceptions.SDKInitException;

public interface OnInitSDKCallback {
     void onInit();
    void onFailed(SDKInitException e);
}
