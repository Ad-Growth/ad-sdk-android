package com.adgrowth.adserver.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.adgrowth.internal.http.AdRequest;
import com.adgrowth.adserver.R;
import com.adgrowth.adserver.enums.AdOrientation;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.internal.entities.Ad;
import com.adgrowth.adserver.enums.AdSizeType;
import com.adgrowth.internal.enums.AdType;
import com.adgrowth.internal.helpers.AdUriHelpers;
import com.adgrowth.internal.interfaces.BaseAdListener;
import com.adgrowth.internal.views.AdImage;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class AdView extends ViewGroup implements AdImage.Listener {
    private static final int AFTER_ERROR_REFRESH_RATE = 10;
    private AdRequest mAdRequest;
    private Activity mContext;
    AdImage mAdImage;
    private Listener mListener;
    private Timer mRefreshTimer;
    private Ad mAd;
    private Boolean mFailedToLoad = false;
    private Boolean mAdIsReady = false;
    boolean mRefreshCountStarted = false;
    private final AdOrientation mOrientation;
    private final String mUnitId;
    private final AdSizeType mSize;

    private final View.OnClickListener onAdClickListener = view -> {
        if (mAd == null) {
            mContext.runOnUiThread(() -> Toast.makeText(mContext, "AD NOT LOADED YET", Toast.LENGTH_SHORT).show());
            return;
        }

        AdUriHelpers.openUrl(mContext, mAd.getActionUrl(), mAd.getIpAddress());

        if (mListener != null) mContext.runOnUiThread(() -> mListener.onClicked());
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        stopRefreshCountdown();
    }

    public AdView(Context context, String unitId, AdSizeType size, AdOrientation orientation) {
        super(context);
        this.mContext = (Activity) context;
        this.mUnitId = unitId;
        this.mSize = size;
        this.mOrientation = orientation;
        init();
    }

    public AdView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = (Activity) context;

        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AdView, 0, 0);

        this.mOrientation = attributes.getInteger(R.styleable.AdView_orientation, 0) == 1 ? AdOrientation.PORTRAIT : AdOrientation.LANDSCAPE;
        this.mSize = AdSizeType.values()[(attributes.getInteger(R.styleable.AdView_size, 0))];
        this.mUnitId = attributes.getString(R.styleable.AdView_unit_id);

        init();
    }

    public AdSizeType getSize() {
        return mSize;
    }

    public String getUnitId() {
        return mUnitId;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int childCount = getChildCount();
        int x = 0;
        int y = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.layout(x, y, x + child.getMeasuredWidth(), y + child.getMeasuredHeight());
            x += child.getMeasuredWidth();
            if (i % 2 != 0) {
                x = 0;
                y += child.getMeasuredHeight();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);


        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }


    void init() {

        if (mUnitId == null || mUnitId.equals(""))
            throw new RuntimeException("You must provide an unit_id for AdView");


        this.setOnClickListener(onAdClickListener);
        mAdRequest = new AdRequest();

        loadAd();
    }


    private void loadAd() {
        new Thread(() -> {
            mFailedToLoad = false;
            mAdIsReady = false;

            try {
                HashMap<String, Object> options = new HashMap<>();
                options.put("orientation", mOrientation.toString());
                options.put("dimension", mSize.toString());

                mAd = mAdRequest.getAd(mUnitId, options);

                if (mAd.getType() != AdType.BANNER) {
                    throw new AdRequestException(AdRequestException.UNIT_ID_MISMATCHED_AD_TYPE);
                }
                presentAd(mAd);

            } catch (AdRequestException e) {
                mFailedToLoad = true;
                startRefreshCountdown(AFTER_ERROR_REFRESH_RATE);
                if (mListener != null)
                    mContext.runOnUiThread(() -> mListener.onFailedToLoad(e));
            }

        }).start();
    }

    void presentAd(Ad ad) {

        mContext.runOnUiThread(() -> {
            this.mAdImage = new AdImage(mContext, ad.getMediaUrl(), this);
            mAdImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mAdImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            super.addView(mAdImage);
        });
    }

    public void reload() {

        refreshAd();
    }

    private void refreshAd() {
        if (mAd != null) mAd = null;
        stopRefreshCountdown();
        removeView(mAdImage);
        loadAd();
    }

    private void stopRefreshCountdown() {
        if (mRefreshTimer != null) {
            mRefreshCountStarted = false;
            mRefreshTimer.cancel();
            mRefreshTimer = null;
        }

    }

    private void startRefreshCountdown(Integer refreshRate) {
        mRefreshTimer = new Timer();

        // 0 = disabled
        if (refreshRate == 0)
            return;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mContext.runOnUiThread(() -> refreshAd());
            }
        };

        mRefreshTimer.scheduleAtFixedRate(task, refreshRate * 1000, refreshRate * 1000);
        mRefreshCountStarted = true;
    }

    @Override
    public void onImageReady() {
        mAdIsReady = true;
        if (mListener != null) {
            mContext.runOnUiThread(() -> {
                mListener.onLoad(this);
                mListener.onImpression();
            });

            if (!mRefreshCountStarted)
                startRefreshCountdown(mAd.getRefreshRate());

        }
            mAdRequest.sendImpression(mContext, mAd);
    }

    @Override
    public void onImageError() {
        mFailedToLoad = true;
        startRefreshCountdown(AFTER_ERROR_REFRESH_RATE);
        if (mListener != null)
            mContext.runOnUiThread(() -> mListener.onFailedToLoad(new AdRequestException(AdRequestException.NETWORK_ERROR)));
    }

    public boolean isLoaded() {
        return this.mAdIsReady;
    }

    public boolean isFailed() {
        return this.mFailedToLoad;
    }

    public AdOrientation getOrientation() {
        return mOrientation;
    }

    public interface Listener extends BaseAdListener<AdView> {
    }
}
