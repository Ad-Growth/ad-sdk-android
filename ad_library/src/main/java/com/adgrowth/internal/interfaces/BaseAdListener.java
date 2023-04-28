package com.adgrowth.internal.interfaces;

import com.adgrowth.adserver.exceptions.AdRequestException;

public abstract class BaseAdListener {
    public abstract void onLoad();

    public abstract void onFailedToLoad(AdRequestException exception);

    public void onImpression() {

    }


    public void onFailedToShow(String code) {

    }

    public void onClicked() {
    }
}
