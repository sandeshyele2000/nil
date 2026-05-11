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

    private val interceptor = NILInterceptor()

    @Volatile
    private var initialized = false
    @Volatile
    private var jsonTreeMaxChars: Int = DEFAULT_JSON_TREE_MAX_CHARS
    private val _isLoggingPaused = MutableStateFlow(false)
    val isLoggingPaused: StateFlow<Boolean> get() = _isLoggingPaused

    val events: StateFlow<List<NetworkEvent>> get() = NILRepository.events

    /**
     * SDK initialization
     */
    fun initialize(
        context: Context,
        enableFloatingButton: Boolean = false,
        jsonTreeMaxChars: Int = DEFAULT_JSON_TREE_MAX_CHARS
    ) {
        this.jsonTreeMaxChars = jsonTreeMaxChars.coerceAtLeast(1_000)
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

    suspend fun clearEvents() {
        NILRepository.clear()
    }

}
