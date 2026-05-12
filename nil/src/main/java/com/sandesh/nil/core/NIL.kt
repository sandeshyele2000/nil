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

object NIL {
    private const val DEFAULT_JSON_TREE_MAX_CHARS = 200_000
    private const val DEFAULT_ANALYSE_LAZY_TEXT_THRESHOLD_CHARS = 200_000

    private val interceptor = NILInterceptor()

    @Volatile
    private var initialized = false
    @Volatile
    private var jsonTreeMaxChars: Int = DEFAULT_JSON_TREE_MAX_CHARS
    @Volatile
    private var analyseLazyTextThresholdChars: Int = DEFAULT_ANALYSE_LAZY_TEXT_THRESHOLD_CHARS
    private val _isLoggingPaused = MutableStateFlow(false)
    val isLoggingPaused: StateFlow<Boolean> get() = _isLoggingPaused

    val events: StateFlow<List<NetworkEvent>> get() = NILRepository.events

    /**
     * SDK initialization
     */
    fun initialize(
        context: Context,
        enableFloatingButton: Boolean = false,
        jsonTreeMaxChars: Int = DEFAULT_JSON_TREE_MAX_CHARS,
        analyseLazyTextThresholdChars: Int = DEFAULT_ANALYSE_LAZY_TEXT_THRESHOLD_CHARS
    ) {
        this.jsonTreeMaxChars = jsonTreeMaxChars.coerceAtLeast(1_000)
        this.analyseLazyTextThresholdChars = analyseLazyTextThresholdChars.coerceAtLeast(10_000)
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
     * Interceptor entry point. Defaults to OkHttp when no type is provided.
     */
    fun interceptor(): NILInterceptor = interceptor

    /**
     * Unified interceptor entry point with explicit type selection.
     * Supported values: "okhttp", "httpurl", "httpurlconnection"
     */
    fun interceptor(type: String): NILInterceptor {
        val normalizedType = type.trim().lowercase()
        require(normalizedType in setOf("okhttp", "httpurl", "httpurlconnection")) {
            "Unsupported interceptor type: $type"
        }
        return interceptor
    }

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

    fun jsonTreeMaxChars(): Int = jsonTreeMaxChars
    fun analyseLazyTextThresholdChars(): Int = analyseLazyTextThresholdChars

    suspend fun clearEvents() {
        NILRepository.clear()
    }

}
