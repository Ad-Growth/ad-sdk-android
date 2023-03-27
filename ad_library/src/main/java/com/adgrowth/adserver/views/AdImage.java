package com.adgrowth.adserver.views;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adgrowth.adserver.helpers.ImageLoader;

public class AdImage extends ImageView {
    private final ImageLoader loader;
    private String url;

    public AdImage(Activity context, String url) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setTranslationZ(2);
        this.loader = new ImageLoader(context);
        this.url = url;
    }
    
    public void prepare (){
        this.loader.loadImage(url, this);
    }

    public void addListener(Listener imageListener) {
        loader.setListener(imageListener);
    }

    public static class Listener {
        public void onLoad() {
        }

        public void onLoadFailed(int code) {
        }
    }
}
