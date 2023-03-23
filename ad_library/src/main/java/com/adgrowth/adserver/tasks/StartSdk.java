package com.adgrowth.adserver.tasks;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import com.adgrowth.adserver.entities.ClientAddress;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.adserver.exceptions.SDKInitException;
import com.adgrowth.adserver.http.AdRequest;


@SuppressLint("NewApi")
public class StartSDK extends AsyncTask<String, String, ClientAddress> {

    private final OnStartCallback callback;
    private final Location location;

    private SDKInitException exception;

    public StartSDK(@Nullable Location location, OnStartCallback callback) {
        this.location = location;
        this.callback = callback;
    }


    @Override
    protected ClientAddress doInBackground(String... strings) {

        try {
            ClientAddress clientAddress = AdRequest.getAddress(location);

            return clientAddress;
        } catch (AdRequestException e) {

            exception = new SDKInitException(e);
            return null;
        }

    }

    @Override
    protected void onPostExecute(ClientAddress clientAddress) {

        if (exception != null) {
            callback.onFailed(exception);
            return;
        }

        callback.onInit(clientAddress);
    }

    public interface OnStartCallback {
        void onInit(ClientAddress clientAddress);
        void onFailed(SDKInitException e);
    }
}
