package com.adgrowth.adserver;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.adgrowth.adserver.constants.BannerAspectRatio;
import com.adgrowth.adserver.helpers.ScreenHelpers;
import com.bumptech.glide.Glide;


public class AdBanner extends LinearLayout {
//    private final String unitId;
//    private final String size;


    ImageView image;
    private String image_uri;
    private int height;
    private int width;


//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        this.getLayoutParams().width = width;
//        this.getLayoutParams().height = height;
//
//    }

    void setupImage (Context context, @Nullable AttributeSet attrs){

        // size = attrs.getAttributeValue("ads", "banner_size");
        // TODO: calculate height and with with banner_size attribute
        width = ScreenHelpers.getScreenWidth();
        height = Math.round(width / BannerAspectRatio.BANNER);

        this.setBackgroundColor(Color.parseColor("#000000"));
        this.image = new ImageView(context);
        image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    void load() {
        // TODO: load from api
        image_uri = "https://resources.construx.com/wp-content/uploads/2016/08/gif-placeholder.gif";
        present();
    }

    void present() {
        Glide.with(this).load(image_uri).into(image);
        //
    }


    public AdBanner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupImage(context, attrs);

        // autoLoad = attrs.getAttributeBooleanValue("ads","auto_load");
        // unitId = attrs.getAttributeStringValue("ads","unit_id");


        this.addView(image);
//        if (autoLoad) {
            load();
//        }
    }

    public AdBanner(Context context) {
        super(context);
    }

    public AdBanner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdBanner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
