package com.adgrowth.adserver.entities;

import com.adgrowth.internal.helpers.JSONHelper;

import org.json.JSONObject;

public class ClientAddress {

    private String mCity;
    private String mState;
    private String mCountry;
    private double mLatitude;
    private double mLongitude;

    public ClientAddress(JSONObject json) {

        this.mCity = JSONHelper.safeGetString(json, "city");
        this.mState = JSONHelper.safeGetString(json, "state");
        this.mCountry = JSONHelper.safeGetString(json, "country");
        this.mLatitude = JSONHelper.safeGetDouble(json, "latitude");
        this.mLongitude = JSONHelper.safeGetDouble(json, "longitude");

    }


    public String getCity() {
        return mCity;
    }

    public String getState() {
        return mState;
    }

    public String getCountry() {
        return mCountry;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setCity(String city) {
        this.mCity = city;
    }

    public void setState(String state) {
        this.mState = state;
    }

    public void setCountry(String country) {
        this.mCountry = country;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

}
