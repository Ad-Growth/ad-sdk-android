package com.adgrowth.adserver.exceptions

class SDKInitException : Exception {
    var code: String? = null
        private set
    override var message: String? = null
        private set(code) {
            when (code) {
                INVALID_CLIENT_KEY -> field = "Your app key does not match any app in our database."
                APP_NOT_FOUND -> field = "Your app key doesn't match with any app on our database."
                ALREADY_INITIALIZED -> field ="You already initialized this with this key, just use it."
                UNAUTHORIZED_CLIENT_KEY -> field = "The provided client key is not valid."
                UNKNOWN_ERROR, INTERNAL_ERROR -> field = "Sorry, this was an unexpected error, please try again."
                NETWORK_ERROR -> field = "It is possible that your internet connection is unstable, please try again."
                else -> {}
            }
        }

    constructor(e: AdRequestException) {
        code = e.code
        message = e.code
    }

    constructor(errorCode: String?) {
        this.code = errorCode
        message = this.code
    }


    companion object {
        const val UNAUTHORIZED_CLIENT_KEY = "unauthorized_client_key"
        const val ALREADY_INITIALIZED = "already_initialized"
        const val APP_NOT_FOUND = "app_not_found"
        const val INVALID_CLIENT_KEY = "invalid_client_key"
        const val INTERNAL_ERROR = "internal_error"
        const val NETWORK_ERROR = "network_error"
        const val UNKNOWN_ERROR = "unknown_error"
    }
}
