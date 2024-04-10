package com.adgrowth.internal.integrations.adserver.views

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdImage(context: Context?, private val url: String, private val listener: Listener) :
    ImageView(context),
    RequestListener<Drawable?> {
    private val mainScope = CoroutineScope(Dispatchers.Main)

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        translationZ = 2f

        Glide.with(context!!).load(url).listener(this).preload()
        setOnClickListener { listener.onClick() }
    }

    fun release() {
        mainScope.launch {
            try {
                Glide.with(context).clear(this@AdImage)
                setImageDrawable(null)
                if (parent != null) (parent as ViewGroup).removeView(this@AdImage)
            } catch (_: Exception) {
            }
        }
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Drawable?>,
        isFirstResource: Boolean
    ): Boolean {
        mainScope.launch { listener.onImageError() }
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
        mainScope.launch {
            listener.onImageReady()
            Glide.with(context).load(url).into(this@AdImage)
        }
        return false
    }

    interface Listener {
        fun onClick()
        fun onImageReady()
        fun onImageError()
    }
}
