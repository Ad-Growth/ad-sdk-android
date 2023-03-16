package com.adgrowth.adserver.entities;

import java.util.ArrayList;


public class ClientProfile {
    public static String MALE = "male";
    public static String FEMALE = "female";
    public static String OTHER = "other";
    private String gender;
    private int age;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
