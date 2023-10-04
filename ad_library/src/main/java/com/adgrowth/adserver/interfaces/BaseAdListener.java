package com.adgrowth.adserver.interfaces;

import com.adgrowth.adserver.exceptions.AdRequestException;

public interface BaseAdListener<T> {
    void onLoad(T ad);

    void onFailedToLoad(AdRequestException exception);

    default void onImpression() {
    }


    default void onFailedToShow(String code) {
    }

    default void onClicked() {
    }

}
