package com.sandesh.nil.core

import android.app.Application
import android.content.Context
import com.sandesh.nil.database.DatabaseProvider
import com.sandesh.nil.interceptor.NILInterceptor
import com.sandesh.nil.model.NetworkEvent
import com.sandesh.nil.overlay.NILFloatingButtonController
import com.sandesh.nil.storage.NILRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

object NIL {

    private val interceptor = NILInterceptor()

    @Volatile
    private var initialized = false
    private val _isLoggingPaused = MutableStateFlow(false)
    val isLoggingPaused: StateFlow<Boolean> get() = _isLoggingPaused

    /**
     * Public immutable stream
     * (safe abstraction boundary)
     */
    val events: StateFlow<List<NetworkEvent>> get() = NILRepository.events

    /**
     * SDK initialization
     */
    fun initialize(
        context: Context,
        enableFloatingButton: Boolean = false
    ) {
        if (initialized) return

        val appContext = context.applicationContext

        val database = DatabaseProvider.getDatabase(appContext)
        NILRepository.initialize(database)

        if (enableFloatingButton && appContext is Application) {
            NILFloatingButtonController.initialize(appContext)
        }

        initialized = true
    }

    /**
     * OkHttp interceptor entry point
     */
    fun interceptor(): NILInterceptor = interceptor

    /**
     * Search filter (rename for clarity)
     */
    fun setFilter(query: String) {
        NILRepository.observeEvents(query)
    }

    fun pauseLogging() {
        _isLoggingPaused.value = true
    }

    fun resumeLogging() {
        _isLoggingPaused.value = false
    }

    fun shouldLogEvents(): Boolean = !_isLoggingPaused.value

    /**
     * Safe clear API
     */
    suspend fun clearEvents() {
        NILRepository.clear()
    }

    fun addMockEvent() {
        val now = System.currentTimeMillis()
        val mockRequest = """
            {
              "userId": 123,
              "location": "Bengaluru",
              "query": {
                "category": "food",
                "sort": "rating"
              }
            }
        """.trimIndent()
        val mockResponse = """
            {
              "restaurants": [
                {
                  "id": 1,
                  "name": "Spice Route",
                  "rating": 4.6,
                  "deliveryTimeMin": 28
                },
                {
                  "id": 2,
                  "name": "Urban Bowl",
                  "rating": 4.4,
                  "deliveryTimeMin": 22
                }
              ],
              "pagination": {
                "page": 1,
                "size": 20,
                "hasNext": true
              }
            }
        """.trimIndent()

        NILRepository.addEvent(
            NetworkEvent(
                id = UUID.randomUUID().toString(),
                url = "https://api.nil.dev/mock/restaurants",
                method = "POST",
                requestHeaders = "Content-Type: application/json\nAuthorization: Bearer mock-token",
                requestBody = mockRequest,
                responseHeaders = "Content-Type: application/json\nX-Mock: true",
                responseBody = mockResponse,
                statusCode = 200,
                durationMs = 143,
                timestamp = now
            )
        )
    }
}
