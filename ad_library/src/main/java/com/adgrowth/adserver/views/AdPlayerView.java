package com.adgrowth.adserver.views;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class AdPlayerView extends StyledPlayerView {
    public AdPlayerView(Context context, ExoPlayer player) {
        super(context);
        setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        setUseController(false);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setForegroundGravity(Gravity.CENTER);
        setTranslationZ(2);
        setPlayer(player);
        setShutterBackgroundColor(Color.BLACK);
        setKeepContentOnPlayerReset(true);
    }
}
