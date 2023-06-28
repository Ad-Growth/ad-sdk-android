package com.adgrowth.adserver.entities;

import org.json.JSONObject;

import java.util.ArrayList;


public class ClientProfile {
    private Gender mGender = Gender.ALL;
    private ClientAddress mClientAddress = new ClientAddress(new JSONObject());
    private int mAge = 0;
    private int mMinAge = 0;
    private int mMaxAge = 0;
    private ArrayList<String> mInterests = new ArrayList<>();


    public ArrayList<String> getInterests() {
        return mInterests;
    }

    public void setInterests(ArrayList<String> interests) {
        this.mInterests = interests;
    }

    public void addInterest(String interest) {
        this.mInterests.add(interest);
    }


    public void removeInterest(String interest) {
        this.mInterests.removeIf(s -> s.equals(interest));

    }

    public  ClientAddress getClientAddress() {
        return mClientAddress;
    }

    public  void setClientAddress(ClientAddress clientAddress) {
        this.mClientAddress = clientAddress;
    }

    public int getAge() {
        return mAge;
    }

    public int getMinAge() {
        return mMinAge;
    }

    public int getMaxAge() {
        return mMaxAge;
    }

    public void setAge(int age) {
        this.mAge = age;
    }

    public void setMinAge(int minAge) {
        this.mMinAge = minAge;
    }

    public void setMaxAge(int maxAge) {
        this.mMaxAge = maxAge;
    }

    public Gender getGender() {
        return mGender;
    }

    public void setGender(Gender gender) {
        this.mGender = gender;
    }

    public enum Gender {
        ALL("ALL"), MALE("MALE"), FEMALE("FEMALE");

        private final String name;

        Gender(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }


    }

}
