package com.adgrowth.adserver.entities;

import android.util.Log;

import com.adgrowth.internal.helpers.JSONHelper;

import org.json.JSONObject;

public class ClientAddress {

    private String city;
    private String state;
    private String country;
    private double latitude = 0.0;
    private double longitude = 0.0;

    public ClientAddress(JSONObject json) {

        this.city = JSONHelper.safeGetString(json, "city");
        this.state = JSONHelper.safeGetString(json, "state");
        this.country = JSONHelper.safeGetString(json, "country");
        this.latitude = JSONHelper.safeGetDouble(json, "latitude");
        this.longitude = JSONHelper.safeGetDouble(json, "longitude");

    }


    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
