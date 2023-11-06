package com.adgrowth.adserver.exceptions

class SDKInitException : Exception {
    var code: String? = null
        private set
    override var message: String? = null
        private set(code) {
            when (code) {
                ALREADY_INITIALIZED -> field =
                    "You already initialized this with this key, just use it."
                UNAUTHORIZED_CLIENT_KEY -> field = "The provided client key is not valid."
                UNKNOWN_ERROR, INTERNAL_ERROR -> field =
                    "Sorry, this was an unexpected error, please try again."
                else -> {}
            }
        }

    constructor(e: AdRequestException) {
        handleError(e.code)
    }

    constructor(errorCode: String?) {
        this.code = errorCode
        message = this.code
    }

    private fun handleError(errorCode: String) {
        when (errorCode) {
            AdRequestException.NETWORK_ERROR -> this.code = NETWORK_ERROR
            AdRequestException.INTERNAL_ERROR -> this.code = INTERNAL_ERROR
            AdRequestException.UNKNOWN_ERROR -> this.code = UNKNOWN_ERROR
            else -> this.code = UNKNOWN_ERROR
        }
        message = this.code
    }

    companion object {
        const val UNAUTHORIZED_CLIENT_KEY = "unauthorized_client_key"
        const val ALREADY_INITIALIZED = "already_initialized"
        const val INTERNAL_ERROR = "internal_error"
        const val NETWORK_ERROR = "network_error"
        const val UNKNOWN_ERROR = "unknown_error"
    }
}