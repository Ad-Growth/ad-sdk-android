package com.adgrowth.adserver;

import android.app.Activity;
import android.content.DialogInterface;

import com.adgrowth.adserver.entities.RewardItem;
import com.adgrowth.internal.enums.AdType;
import com.adgrowth.internal.http.AdRequest;
import com.adgrowth.internal.interfaces.BaseAdListener;
import com.adgrowth.internal.views.BaseFullScreenAd;

public class RewardedAd extends BaseFullScreenAd<RewardedAd.Listener> {

    private static final int TIME_TO_REWARD = 30;
    private static final int TIME_TO_SHOW_TAP_TO_CLOSE = -3;

    private boolean mRewarded = false;

    public RewardedAd(String unitId) {
        this.mUnitId = unitId;
        this.mAdRequest = new AdRequest();
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        super.onShow(dialogInterface);
        mDialog.showButtonText();
    }

    public void load(Activity context) {
        mRewarded = false;
        super.requestAd(context, AdType.REWARDED);
    }

    @Override
    protected void dismiss() {
        super.dismiss();
        mContext.runOnUiThread(() -> mListener.onDismissed());
    }

    @Override
    public void onDisplayTimeChanged(int currentTime) {
        super.onDisplayTimeChanged(currentTime);

        int remainingTime = (int) Math.ceil(TIME_TO_REWARD - currentTime);


        if (remainingTime < 0) {
            if (remainingTime <= TIME_TO_SHOW_TAP_TO_CLOSE) {
                mDialog.setButtonLabelText(mContext.getString(R.string.tap_to_close));
                stopAdStartedTimer();
            }
            return;
        }

        String buttonLabel = mContext.getResources().getQuantityString(R.plurals.remaining_seconds, remainingTime, remainingTime);

        if (remainingTime == 0) buttonLabel = mContext.getString(R.string.prize_received);

        mDialog.setButtonLabelText(buttonLabel);

        if ((currentTime >= TIME_TO_REWARD) && !mRewarded) {
            mRewarded = true;
            mListener.onEarnedReward(mAd.getReward());
        }

    }

    public interface Listener extends BaseAdListener<RewardedAd> {
        void onEarnedReward(RewardItem rewardItem);

        default void onDismissed() {
        }

    }
}