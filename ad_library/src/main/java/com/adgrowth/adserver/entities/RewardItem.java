package com.adgrowth.adserver.entities;

public class RewardItem {
    private final int mRewardValue;
    private final String mRewardItem;

    public RewardItem(int value, String item) {
        this.mRewardValue = value;
        this.mRewardItem = item;
    }

    public String getItem() {
        return mRewardItem;
    }

    public int getValue() {
        return mRewardValue;
    }
}
