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

import com.adgrowth.adserver.AdRequest;
import com.adgrowth.adserver.R;
import com.adgrowth.adserver.exceptions.AdRequestException;
import com.adgrowth.internal.entities.Ad;
import com.adgrowth.internal.enums.AdEventType;
import com.adgrowth.internal.enums.AdSizeType;
import com.adgrowth.internal.helpers.OnClickHelpers;
import com.adgrowth.internal.helpers.ScreenHelpers;
import com.adgrowth.internal.interfaces.BaseAdListener;
import com.adgrowth.internal.views.AdImage;

import java.util.HashMap;


public class AdView extends ViewGroup implements AdImage.Listener {
    private Activity context;

    private String unitId;
    private AdRequest adRequest;

    AdImage image;
    private AdSizeType size;
    private Listener listener;
    private Ad ad;
    private View.OnClickListener onAdClickListener = view -> {
        if (ad == null) {
            context.runOnUiThread(() -> Toast.makeText(context, "AD NOT LOADED YET", Toast.LENGTH_SHORT).show());
            return;
        }

        OnClickHelpers.openUrl(context, ad.getActionUrl());

        if (listener != null) context.runOnUiThread(() -> listener.onClicked());

        adRequest.sendEvent(ad, AdEventType.CLICKED);
    };


    public AdView(Context context, String unitId, AdSizeType size) {
        super(context);

        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, ScreenHelpers.dpToPx(72, context)));
        this.context = (Activity) context;
        this.unitId = unitId;
        this.size = size;

        init();
    }

    public AdView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = (Activity) context;

        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AdView, 0, 0);

        size = AdSizeType.values()[(attributes.getInteger(R.styleable.AdView_size, 0))];
        unitId = attributes.getString(R.styleable.AdView_unit_id);

        init();
    }

    public String getUnitId() {
        return unitId;
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
        this.listener = listener;
    }


    void init() {

        if (unitId == null || unitId.equals(""))
            throw new RuntimeException("You must provide an unit_id for AdView");


        this.setOnClickListener(onAdClickListener);
        adRequest = new AdRequest(context);

        loadAd();
    }


    private void loadAd() {
        new Thread(() -> {
            try {
                HashMap<String, Object> options = new HashMap<>();
                options.put("orientation", "LANDSCAPE");
                options.put("dimension", size.toString());

                ad = adRequest.getAd(unitId, options);

                presentAd(ad);

            } catch (AdRequestException e) {
                if (this.listener != null) context.runOnUiThread(() -> listener.onFailedToLoad(e));
            }

        }).start();
    }

    void presentAd(Ad ad) {

        context.runOnUiThread(() -> {
            this.image = new AdImage(context, ad.getMediaUrl(), this);
            image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            super.addView(image);
        });
    }

    public void reload() {
        if (ad != null) ad = null;
        removeView(image);
        loadAd();
    }

    @Override
    public void onLoad() {
        if (listener != null) {
            context.runOnUiThread(() -> {
                listener.onLoad();
                listener.onImpression();
            });
        }
    }

    @Override
    public void onLoadFailed() {
        if (listener != null)
            context.runOnUiThread(() -> listener.onFailedToLoad(new AdRequestException(AdRequestException.NETWORK_ERROR)));
    }


    public abstract static class Listener extends BaseAdListener {
    }
}
