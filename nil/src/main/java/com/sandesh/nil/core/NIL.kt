package com.sandesh.nil.core

import android.content.Context
import com.sandesh.nil.database.DatabaseProvider
import com.sandesh.nil.interceptor.NILInterceptor
import com.sandesh.nil.model.NetworkEvent
import com.sandesh.nil.overlay.NILFloatingButtonController
import com.sandesh.nil.storage.NILRepository
import kotlinx.coroutines.flow.StateFlow

object NIL {
    private val interceptor = NILInterceptor()

    val events: StateFlow<List<NetworkEvent>> = NILRepository.events

    fun initialize(
        context: Context,
        enableFloatingButton: Boolean = false
    ) {
        val database = DatabaseProvider.getDatabase(context.applicationContext)
        NILRepository.initialize(database)
        if (enableFloatingButton) {
            val application = context.applicationContext as? android.app.Application
            if (application != null) {
                NILFloatingButtonController.initialize(application)
            }
        }
    }

    fun interceptor(): NILInterceptor = interceptor

    fun setSearchQuery(query: String) {
        NILRepository.observeEvents(query)
    }

    suspend fun clearEvents() {
        NILRepository.clear()
    }
}
