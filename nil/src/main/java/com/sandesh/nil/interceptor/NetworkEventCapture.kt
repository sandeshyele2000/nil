package com.sandesh.nil.interceptor

import com.sandesh.nil.core.NIL
import com.sandesh.nil.model.NetworkEvent
import com.sandesh.nil.storage.NILRepository
import java.util.UUID

internal data class CaptureResult(
    val responseHeaders: String?,
    val responseBody: String?,
    val statusCode: Int?
)

internal inline fun <T> captureNetworkEvent(
    url: String,
    method: String,
    requestHeaders: String?,
    requestBody: String?,
    execute: () -> T,
    onSuccess: (T) -> CaptureResult,
    onFailure: (Throwable) -> CaptureResult
): T {
    if (!NIL.shouldLogEvents()) {
        return execute()
    }

    val requestStartedAt = System.currentTimeMillis()
    val result = try {
        execute()
    } catch (throwable: Throwable) {
        val failure = onFailure(throwable)
        val failedEvent = NetworkEvent(
            id = UUID.randomUUID().toString(),
            url = url,
            method = method,
            requestHeaders = requestHeaders,
            requestBody = requestBody,
            responseHeaders = failure.responseHeaders,
            responseBody = failure.responseBody,
            statusCode = failure.statusCode,
            durationMs = System.currentTimeMillis() - requestStartedAt,
            timestamp = requestStartedAt
        )
        NILRepository.addEvent(failedEvent)
        throw throwable
    }

    val success = onSuccess(result)
    val completedEvent = NetworkEvent(
        id = UUID.randomUUID().toString(),
        url = url,
        method = method,
        requestHeaders = requestHeaders,
        requestBody = requestBody,
        responseHeaders = success.responseHeaders,
        responseBody = success.responseBody,
        statusCode = success.statusCode,
        durationMs = System.currentTimeMillis() - requestStartedAt,
        timestamp = requestStartedAt
    )
    NILRepository.addEvent(completedEvent)
    return result
}
