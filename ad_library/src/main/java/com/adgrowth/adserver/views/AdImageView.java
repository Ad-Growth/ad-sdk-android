package com.adgrowth.adserver.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class AdImageView extends ImageView {
    public AdImageView(Context context, String url, View.OnClickListener onAdClickListener) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setTranslationZ(2);
        setOnClickListener(onAdClickListener);
        Glide.with((Context) context).load(url).into(this);
    }
}
