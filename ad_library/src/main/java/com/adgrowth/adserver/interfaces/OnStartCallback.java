package com.adgrowth.adserver.interfaces;

import com.adgrowth.adserver.entities.ClientAddress;
import com.adgrowth.adserver.exceptions.SDKInitException;

public interface OnStartCallback {
    public abstract void onInit(ClientAddress clientAddress);
    public abstract void onFailed(SDKInitException e);
}
