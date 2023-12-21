package com.adgrowth.internal.integrations.adserver.views

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class AdImage(context: Context?, url: String, imageListener: Listener?) : ImageView(context),
    RequestListener<Drawable?> {
    private val mListener: Listener?
    private val mUrl: String

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        translationZ = 2f
        mListener = imageListener
        this.mUrl = url
        Glide.with(context!!).load(url).listener(this).preload()
    }

    private fun runOnUiThread(runnable: Runnable?) {
        (context as Activity).runOnUiThread(runnable)
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Drawable?>,
        isFirstResource: Boolean
    ): Boolean {
        runOnUiThread { mListener!!.onImageError() }
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any,
        target: Target<Drawable?>,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        if (resource is GifDrawable) resource.setLoopCount(GifDrawable.LOOP_FOREVER)
        setImageDrawable(resource)
        runOnUiThread {
            mListener!!.onImageReady()
            Glide.with(context).load(mUrl).into(this@AdImage)
        }
        return false
    }

    interface Listener {
        fun onImageReady()
        fun onImageError()
    }
}
