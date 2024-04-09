package com.adgrowth.internal.integrations.adserver.views

import android.content.Context
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
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

class AdImage(private val context: Context, imageUrl: String, private val listener: Listener) :
    RequestListener<Drawable> {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val imageView = ImageView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        translationZ = 2f
    }

    init {
        try {
            Glide.with(context)
                .load(imageUrl)
                .listener(this)
                .preload()
        } catch (e: Exception) {
            listener.onImageError()
        }

        imageView.setOnClickListener {
            listener.onClick()
        }

    }

    fun addInto(parent: ViewGroup) {
        mainScope.launch {
            if (imageView.parent != null) (imageView.parent as ViewGroup).removeView(imageView)
            parent.addView(imageView)

            imageView.drawable?.let {
                try {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && it is AnimatedImageDrawable) {
                        it.repeatCount = AnimatedImageDrawable.REPEAT_INFINITE
                        it.start()
                    }

                    if (it is GifDrawable) {
                        it.setLoopCount(GifDrawable.LOOP_FOREVER)
                        it.start()
                    }
                } catch (e: Exception) {
                    listener.onImageError()
                    println("Error while playing gif: ${e.message}")
                }
            }
        }
    }

    fun release() {
        mainScope.launch {
            try {
                Glide.with(context).clear(imageView)
                imageView.setImageDrawable(null)
                if (imageView.parent != null) (imageView.parent as ViewGroup).removeView(imageView)
            } catch (_: Exception) {
            }
        }
    }


    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        mainScope.launch {
            listener.onImageError()
        }
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        mainScope.launch {
            resource?.let {
                imageView.setImageDrawable(it)
            }
            listener.onImageReady()
        }
        return false
    }

    interface Listener {
        fun onClick()
        fun onImageReady()
        fun onImageError()
    }
}
