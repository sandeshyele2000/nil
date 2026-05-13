package com.sandesh.nil.storage

import com.sandesh.nil.database.NILDatabase
import com.sandesh.nil.model.NetworkEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Single source of truth for NIL events.
 * Owns persistence subscription and exposes reactive state for UI.
 */
object NILRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var database: NILDatabase
    private var observeJob: Job? = null
    private val _events = MutableStateFlow<List<NetworkEvent>>(emptyList())
    val events: StateFlow<List<NetworkEvent>> = _events.asStateFlow()

    @Volatile
    private var initialized: Boolean = false

    @Synchronized
    fun initialize(db: NILDatabase) {
        if (initialized) return
        database = db
        initialized = true
        observeEvents("")
    }

    fun observeEvents(query: String) {
        ensureInitialized()
        observeJob?.cancel()
        observeJob = scope.launch {
            val source = if (query.isBlank()) {
                database.networkEventDao().observeAll()
            } else {
                database.networkEventDao().observeByQuery(query.trim())
            }

            source.collectLatest { list ->
                _events.value = list
            }
        }
    }

    fun addEvent(event: NetworkEvent) {
        ensureInitialized()
        scope.launch {
            database.networkEventDao().insert(event)
        }
    }

    suspend fun clear() {
        ensureInitialized()
        database.networkEventDao().clear()
    }

    fun clearAsync() {
        ensureInitialized()
        scope.launch {
            database.networkEventDao().clear()
        }
    }

    fun setPinned(eventId: String, pinned: Boolean) {
        ensureInitialized()
        scope.launch {
            database.networkEventDao().setPinned(eventId, pinned)
        }
    }

    private fun ensureInitialized() {
        check(initialized) { "NILRepository is not initialized. Call NIL.initialize(context)." }
    }
}
