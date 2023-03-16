package com.adgrowth.adserver.entities;

import com.adgrowth.adserver.helpers.JSONHelper;

import org.json.JSONObject;

public class ClientAddress {
    private final String neighborhood;
    private final String formattedAddress;
    private String city;
    private String state;
    private String country;
    private double latitude;
    private double longitude;

    public ClientAddress(JSONObject json) {
        this.neighborhood = JSONHelper.safeGetString(json, "neighborhood");
        this.city = JSONHelper.safeGetString(json, "city");
        this.state = JSONHelper.safeGetString(json, "state");
        this.country = JSONHelper.safeGetString(json, "country");
        this.formattedAddress = JSONHelper.safeGetString(json, "formatted_address");
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


    public String getFormattedAddress() {
        return formattedAddress;
    }

    public String getNeighborhood() {
        return neighborhood;
    }
}
