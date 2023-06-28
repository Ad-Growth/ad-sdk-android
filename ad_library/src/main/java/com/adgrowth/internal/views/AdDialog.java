package com.adgrowth.internal.views;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.adgrowth.adserver.R;

import java.util.Objects;

public class AdDialog extends Dialog {

    private final ProgressBar mProgressBar;
    private final ImageView mCloseBtn;

    private final TextView mCloseTextView;


    public AdDialog(@NonNull Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fit_content);
        setCancelable(false);

        Objects.requireNonNull(getWindow()).setGravity(Gravity.CENTER);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mCloseTextView = (TextView) findViewById(R.id.close_text_view);

        mCloseBtn = (ImageView) findViewById(R.id.close_btn);
        mCloseBtn.setEnabled(false);
        mProgressBar = (ProgressBar) findViewById(R.id.video_progress);
        mProgressBar.setProgress(0);
        mProgressBar.setMax(100);
    }

    public void setOnCloseListener(View.OnClickListener onCloseListener) {
        mCloseBtn.setOnClickListener(onCloseListener);
    }

    public void setVideoProgress(int progress) {
        if (mProgressBar.getVisibility() != View.VISIBLE)
            mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(progress);
    }

    public void enableCloseButton() {
        mCloseBtn.setEnabled(true);
        mCloseBtn.setAlpha(1f);
    }

    public boolean isCloseButtonEnabled() {
        return mCloseBtn.isEnabled();
    }

    public void showButtonText() {
        mCloseTextView.setVisibility(View.VISIBLE);
    }

    public void hideButtonText() {
        mCloseTextView.setVisibility(View.GONE);
    }


    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    public void setButtonLabelText(String text) {
        mCloseTextView.setText(text);
    }
}
