package com.adgrowth.adserver.views;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.adgrowth.adserver.R;

public class AdDialog extends Dialog {

    private final ProgressBar progressBar;
    private final ImageView closeBtn;

    public AdDialog(@NonNull Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fit_content);
        setCancelable(false);

        getWindow().setGravity(Gravity.CENTER);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        closeBtn = (ImageView) findViewById(R.id.close_btn);
        progressBar = (ProgressBar) findViewById(R.id.video_progress);
        progressBar.setProgress(0);
        progressBar.setMax(100);
    }

    public void setOnCloseListener(View.OnClickListener onCloseListener) {
        findViewById(R.id.close_btn).setOnClickListener(onCloseListener);
    }

    public void setVideoProgress(int progress) {
        this.progressBar.setVisibility(View.VISIBLE);
        this.progressBar.setProgress(progress);
    }

    public void showCloseButton() {
        closeBtn.setVisibility(View.VISIBLE);
    }


    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

}
