package com.adgrowth.adserver.interfaces;

import android.media.MediaPlayer;

import com.adgrowth.adserver.exceptions.AdRequestException;

public abstract class InterstitialCallback {

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

    public void onDismissed() {
    }


}
