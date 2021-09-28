package com.tsato.mobile.inote.data.remote

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthInterceptor : Interceptor {

    val email: String? = null
    val password: String? = null

    // each interceptor modifies our request
    override fun intercept(chain: Interceptor.Chain): Response {
        val currRequest = chain.request()

        if (currRequest.url.encodedPath in IGNORE_AUTH_URLS) {
            return chain.proceed(currRequest)
        }

        // we add authentication header to the currRequest
        // authorized: the user is allowed to make this request
        // authenticated: the user is a valid user
        val authenticatedRequest = currRequest
            .newBuilder()
            .addHeader("Authorization", Credentials.basic(email ?: "", password ?: ""))
            .build()

        return chain.proceed(authenticatedRequest)
    }
}