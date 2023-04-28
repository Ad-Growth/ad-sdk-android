package com.adgrowth.adserver.entities;

import java.util.ArrayList;


public class ClientProfile {
    private Gender gender = Gender.ALL;
    private int age = 13;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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
