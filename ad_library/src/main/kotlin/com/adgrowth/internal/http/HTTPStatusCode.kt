package com.adgrowth.internal.http

class HTTPStatusCode {
    companion object {
        const val NO_RESPONSE = 0
        const val OK = 200
        const val CREATED = 201
        const val NO_CONTENT = 204
        const val REDIRECT = 300
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val CONFLICT = 409
        const val UNPROCESSABLE_ENTITY = 422
        const val INTERNAL_ERROR = 500
        const val NOT_IMPLEMENTED = 501
        const val BAD_GATEWAY = 502
    }
}
