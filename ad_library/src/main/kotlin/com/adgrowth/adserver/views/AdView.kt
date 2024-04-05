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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdView : PreviewHandlerView, AdViewIntegration.Listener,
    AdServerEventManager.SdkInitializedListener {
    private val mainScope = CoroutineScope(Dispatchers.Main)

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
            mainScope.launch {
                mListener?.onFailedToLoad(AdRequestException(AdRequestException.NOT_READY))

            }
            return
        }

        mAdManager!!.reload(this)
    }

    fun isLoaded(): Boolean {
        return mAdManager?.isLoaded == true
    }

    fun isFailed(): Boolean {
        return mAdManager?.isFailed == true
    }

    override fun onFinished() {
        mAdManager?.reload(this)
    }

    override fun onDismissed() {
        mainScope.launch { mListener?.onDismissed() }
    }

    override fun onLoad(ad: AdViewIntegration) {
        mainScope.launch {
            mAdManager?.show(this@AdView)
            mListener?.onLoad(this@AdView)
        }
    }

    override fun onFailedToLoad(exception: AdRequestException?) {
        mainScope.launch {
            mListener?.onFailedToLoad(exception)
        }
    }

    override fun onClicked() {
        mainScope.launch { mListener?.onClicked() }
    }

    override fun onFailedToShow(code: String?) {
        mainScope.launch { mListener?.onFailedToShow(code) }
    }

    override fun onImpression() {
        mainScope.launch { mListener?.onImpression() }
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
