package com.adgrowth.adserver.views;

import static com.bumptech.glide.load.resource.gif.GifDrawable.LOOP_FOREVER;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class AdImage extends ImageView implements RequestListener<Drawable> {

    private final Listener listener;
    private final String url;

    public AdImage(Context context, String url, @Nullable Listener imageListener) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setTranslationZ(2);
        this.listener = imageListener;
        this.url = url;

        Glide.with(context).load(url).listener(this).preload();
    }

    void runOnUiThread(Runnable runnable) {
        ((Activity) getContext()).runOnUiThread(runnable);
    }

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

        runOnUiThread(listener::onLoadFailed);
        return false;
    }

    @Override
    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
        if (resource instanceof GifDrawable)
            ((GifDrawable) resource).setLoopCount(LOOP_FOREVER);

        setImageDrawable(resource);
        runOnUiThread(() -> {
            listener.onLoad();
            Glide.with(getContext()).load(url).into(AdImage.this);
        });
        return false;
    }

    public static class Listener {
        public void onLoad() {
        }

        public void onLoadFailed() {
        }
    }
}
