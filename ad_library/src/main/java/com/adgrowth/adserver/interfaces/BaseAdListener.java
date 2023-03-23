package com.adgrowth.adserver.interfaces;

import com.adgrowth.adserver.exceptions.AdRequestException;

public abstract class BaseAdListener {
    public void onLoad() {

    }

    public void onImpression() {

    }

    public void onFailedToLoad(AdRequestException exception) {
    }

    public void onFailedToShow(int code) {

    }

    public void onClicked() {
    }
}
