package com.lw.top.lib_core.data.network.interceptor

import com.blankj.utilcode.util.LogUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

const val TAG = "Cyber OkHttp"

class CustomLoggingInterceptor() : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        logRequest(request)

        val startTime = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            LogUtils.eTag(TAG, "<-- HTTP FAILED: $e")
            throw e
        }
        val tookMs = (System.nanoTime() - startTime) / 1_000_000L

        logResponse(response, tookMs)

        return response
    }

    private fun logRequest(request: Request) {
        val url = request.url.toString()
        val method = request.method
        var requestBodyString = ""

        request.body?.let { requestBody ->
            val buffer = okio.Buffer()
            requestBody.writeTo(buffer)
            var charset: Charset = StandardCharsets.UTF_8
            requestBody.contentType()?.let {
                charset = it.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            }
            requestBodyString = buffer.readString(charset)
        }

        val logBuilder = StringBuilder()
        logBuilder.appendLine("--> $method $url")

//         打印请求头中的特定参数（如果需要，例如 Authorization, Content-Type）
//         request.headers.forEach { header ->
//             if (header.first.equals("Authorization", ignoreCase = true) ||
//                 header.first.equals("Content-Type", ignoreCase = true)) {
//                 logBuilder.appendLine("    ${header.first}: ${header.second}")
//             }
//         }

        if (requestBodyString.isNotEmpty()) {
            logBuilder.appendLine("Request Params: $requestBodyString")
        }
        logBuilder.appendLine("--> END $method")

        LogUtils.dTag(TAG, logBuilder.toString())
    }

    private fun logResponse(response: Response, tookMs: Long) {
        val requestUrl = response.request.url.toString()
        val responseBody = response.body
        val contentLength = responseBody.contentLength()
        var responseBodyString = "No response body or body too large to log."

        if (responseBody.contentType() != null && contentLength < 2 * 1024 * 1024) { // 限制日志大小，例如2MB
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer
            var charset: Charset = StandardCharsets.UTF_8
            responseBody.contentType()?.let {
                charset = it.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            }

            if (contentLength != 0L) {
                responseBodyString = buffer.clone().readString(charset)
            }
        }

        val logBuilder = StringBuilder()
        logBuilder.appendLine("<-- ${response.code} ${response.message} $requestUrl (${tookMs}ms)")

        // 打印响应头中的特定参数（如果需要）
        // response.headers().forEach { header ->
        //    if (header.first.equals("Content-Type", ignoreCase = true)) {
        //        logBuilder.appendLine("    ${header.first}: ${header.second}")
        //    }
        // }

        logBuilder.appendLine("Response Params: $responseBodyString")
        logBuilder.appendLine("<-- END HTTP")

        LogUtils.dTag(TAG, logBuilder.toString())
    }
}