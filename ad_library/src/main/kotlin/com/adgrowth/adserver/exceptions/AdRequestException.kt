package com.adgrowth.adserver.exceptions

class AdRequestException(val code: String) : Exception() {

    companion object {
        const val PLAYBACK_ERROR = "playback_error"
        const val NETWORK_ERROR = "network_error"
        const val UNKNOWN_ERROR = "unknown_error"
        const val UNIT_ID_MISMATCHED_AD_TYPE = "unit_id_mismatched_ad_type"
        const val INTERNAL_ERROR = "internal_error"
        const val INVALID_CLIENT_KEY = "invalid_client_key"
        const val INVALID_UNIT_ID = "invalid_unit_id"
        const val SDK_NOT_INITIALIZED = "sdk_not_initialized"
        const val NO_AD_FOUND = "no_ad_found"
        const val APP_NOT_FOUND = "app_not_found"
        const val ALREADY_LOADED = "already_loaded"
        const val ALREADY_CONSUMED = "already_consumed"
        const val NOT_READY = "not_ready"
        const val ALREADY_SHOWING_FULL_SCREEN_AD = "already_showing_full_screen_ad"
    }
}
