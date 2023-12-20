package com.adgrowth.adserver.views

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import com.adgrowth.adserver.enums.AdOrientation
import com.adgrowth.adserver.enums.AdSize
import com.adgrowth.adserver.exceptions.AdRequestException
import com.adgrowth.internal.integrations.AdViewManager
import com.adgrowth.internal.integrations.InitializationManager
import com.adgrowth.internal.integrations.adserver.helpers.AdServerEventManager
import com.adgrowth.internal.interfaces.integrations.AdViewIntegration
import com.adgrowth.internal.views.PreviewHandlerView

class AdView : PreviewHandlerView, AdViewIntegration.Listener,
    AdServerEventManager.SdkInitializedListener {
    var isLoaded = false
    var isFailed = false
    private var mAdManager: AdViewManager? = null
    private var mListener: Listener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context, unitId: String, size: AdSize, orientation: AdOrientation
    ) : super(context) {
        this.context = context as Activity
        this.unitId = unitId
        this.size = size
        this.orientation = orientation

        if (InitializationManager.isInitialized) {
            load()
        }
    }

    init {
        AdServerEventManager.registerSDKInitializedListener(this)
    }

    override fun load() {
        mAdManager = AdViewManager(unitId)
        mAdManager!!.listener = this
        mAdManager!!.load(context, size, orientation)
    }

    override fun onSDKInit() {
        load()
    }


    fun setListener(listener: Listener) {
        mListener = listener
    }


    fun reload() {
        if (mAdManager == null) {
            context.runOnUiThread {
                mListener?.onFailedToLoad(AdRequestException(AdRequestException.NOT_READY))
                isFailed = true
            }
            return
        }

        mAdManager!!.reload(this)
    }

    override fun onFinished() {
        reload()
    }

    override fun onDismissed() {
        context.runOnUiThread { mListener?.onDismissed() }
    }

    override fun onLoad(ad: AdViewIntegration) {
        context.runOnUiThread {
            isLoaded = true
            mAdManager?.show(this)
            mListener?.onLoad(this)
        }
    }

    override fun onFailedToLoad(exception: AdRequestException?) {
        context.runOnUiThread {
            mListener?.onFailedToLoad(exception)
            isFailed = true
        }
    }

    override fun onClicked() {
        context.runOnUiThread { mListener?.onClicked() }
    }

    override fun onFailedToShow(code: String?) {
        context.runOnUiThread { mListener?.onFailedToShow(code) }
    }

    override fun onImpression() {
        context.runOnUiThread { mListener?.onImpression() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAdManager?.release()
        mAdManager = null
        AdServerEventManager.unregisterSDKInitializedListener(this)
    }


    interface Listener {
        fun onLoad(ad: AdView)
        fun onFailedToLoad(exception: AdRequestException?)

        @JvmDefault
        fun onDismissed() {
        }

        @JvmDefault
        fun onClicked() {
        }

        @JvmDefault
        fun onFailedToShow(code: String?) {
        }

        @JvmDefault
        fun onImpression() {
        }
    }
}
