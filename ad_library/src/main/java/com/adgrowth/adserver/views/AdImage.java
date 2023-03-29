package com.adgrowth.adserver.views;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.adgrowth.adserver.helpers.ImageLoader;

public class AdImage extends ImageView {
    private final ImageLoader loader;

    public AdImage(Activity context, String url, @Nullable Listener imageListener) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setTranslationZ(2);
        this.loader = new ImageLoader(context);
        loader.setListener(imageListener);
        this.loader.loadImage(url, this);

    }



    public static class Listener {
        public void onLoad() {
        }

        public void onLoadFailed(int code) {
        }
    }
}
