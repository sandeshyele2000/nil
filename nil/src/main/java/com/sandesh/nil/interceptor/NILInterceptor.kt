package com.sandesh.nil.interceptor

import com.sandesh.nil.model.NetworkEvent
import com.sandesh.nil.storage.NILRepository
import com.sandesh.nil.utils.RequestBodyReader
import com.sandesh.nil.utils.ResponseBodyReader
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

class NILInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestStartedAt = System.currentTimeMillis()
        val requestBody = RequestBodyReader.read(request)
        val requestHeaders = request.headers.toFlatString()

        val response = try {
            chain.proceed(request)
        } catch (throwable: Throwable) {
            val failedEvent = NetworkEvent(
                id = UUID.randomUUID().toString(),
                url = request.url.toString(),
                method = request.method,
                requestHeaders = requestHeaders,
                requestBody = requestBody,
                responseHeaders = null,
                responseBody = throwable.message,
                statusCode = null,
                durationMs = System.currentTimeMillis() - requestStartedAt,
                timestamp = requestStartedAt
            )
            NILRepository.addEvent(failedEvent)
            throw throwable
        }

        val completedEvent = NetworkEvent(
            id = UUID.randomUUID().toString(),
            url = request.url.toString(),
            method = request.method,
            requestHeaders = requestHeaders,
            requestBody = requestBody,
            responseHeaders = response.headers.toFlatString(),
            responseBody = ResponseBodyReader.read(response),
            statusCode = response.code,
            durationMs = System.currentTimeMillis() - requestStartedAt,
            timestamp = requestStartedAt
        )
        NILRepository.addEvent(completedEvent)
        return response
    }
}

private fun okhttp3.Headers.toFlatString(): String = buildString {
    this@toFlatString.forEach { header ->
        append(header.first)
        append(": ")
        append(header.second)
        append('\n')
    }
}.trimEnd()
