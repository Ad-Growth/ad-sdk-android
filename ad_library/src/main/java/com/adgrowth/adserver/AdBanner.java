package com.adgrowth.adserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.adgrowth.adserver.constants.AdBannerSize;
import com.adgrowth.adserver.constants.AdDimensionType;
import com.adgrowth.adserver.entities.Ad;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.adserver.helpers.ImageLoader;
import com.adgrowth.adserver.helpers.ScreenHelpers;
import com.adgrowth.adserver.http.AdRequest;
import com.adgrowth.adserver.interfaces.BaseAdListener;


public class AdBanner extends LinearLayout {
    protected String unitId;
    protected AdRequest adRequest;
    protected Ad ad;
    ImageView image;
    private long height;
    private long width;
    private AdDimensionType type = AdDimensionType.BANNER;
    private BaseAdListener callback;


    public AdBanner(Context context) {
        super(context);
        setupImage(context, null);
    }

    public AdBanner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupImage(context, attrs);

    }


    @SuppressLint("NewApi")
    public AdBanner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupImage(context, attrs);
    }


    @SuppressLint("NewApi")
    public AdBanner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setupImage(context, attrs);
    }


    public void setCallback(BaseAdListener callback) {
        this.callback = callback;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.getLayoutParams().width = (int) width;
        this.getLayoutParams().height = (int) height;

    }

    void setupImage(Context context, @Nullable AttributeSet attrs) {

        adRequest = new AdRequest((Activity) context);

        if (attrs != null) {
            if (attrs.getAttributeValue("ad", "type") != null)
                type = AdDimensionType.valueOf(attrs.getAttributeValue("ad", "type"));
            if (attrs.getAttributeValue("ad", "unit_id") != null)
                unitId = attrs.getAttributeValue("ad", "unit_id");
        }

        width = ScreenHelpers.getScreenWidth();

        switch (type) {
            case FULL_BANNER:
                height = Math.round(width / AdBannerSize.FULL_BANNER);
                break;
            case LEADERBOARD:
                height = Math.round(width / AdBannerSize.LEADERBOARD);
                break;
            case LARGE_BANNER:
                height = Math.round(width / AdBannerSize.LARGE_BANNER);
                break;
            case MEDIUM_RECTANGLE:
                height = Math.round(width / AdBannerSize.MEDIUM_RECTANGLE);
                break;
            case BANNER:
            default:
                height = Math.round(width / AdBannerSize.BANNER);
        }

        this.setBackgroundColor(Color.parseColor("#000000"));
        this.image = new ImageView(context);
        image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        this.addView(image);
        loadAd();
    }

    private void loadAd() {
        new Thread(() -> {
            try {
                ad = adRequest.getAd(unitId);
                if (this.callback != null)
                    this.callback.onLoad();
                presentAd();

            } catch (AdRequestException e) {
                if (this.callback != null)
                    callback.onFailedToLoad(e);
            }

        }).start();
    }

    void presentAd() {
        new ImageLoader(this.getContext()).loadImage(ad.getMediaUrl(), image);
        assert callback != null;
        callback.onImpression();
    }

}
