package com.adgrowth.adserver.entities;

import org.json.JSONObject;

import java.util.ArrayList;


public class ClientProfile {
    private Gender gender = Gender.ALL;
    private ClientAddress clientAddress = new ClientAddress(new JSONObject());
    private int age = 0;
    private int minAge = 0;
    private int maxAge = 0;
    private ArrayList<String> interests = new ArrayList<>();


    public ArrayList<String> getInterests() {
        return interests;
    }

    public void setInterests(ArrayList<String> interests) {
        this.interests = interests;
    }

    public void addInterest(String interest) {
        this.interests.add(interest);
    }


    public void removeInterest(String interest) {
        this.interests.removeIf(s -> s.equals(interest));

    }

    public  ClientAddress getClientAddress() {
        return clientAddress;
    }

    public  void setClientAddress(ClientAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    public int getAge() {
        return age;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
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
