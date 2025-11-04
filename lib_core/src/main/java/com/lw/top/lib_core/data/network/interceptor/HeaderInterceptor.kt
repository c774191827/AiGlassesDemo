package com.lw.top.lib_core.data.network.interceptor

import com.lw.top.lib_core.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val authToken = tokenManager.getAccessToken()
        authToken?.takeIf { it.isNotBlank() }?.let { token ->
            requestBuilder.header("Authorization", token)
        }
        requestBuilder.header("X-Api-Version", "1.1")
        requestBuilder.header("X-Device-Type", "Android")
        requestBuilder.header("Accept-Language", "en")
        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }

}