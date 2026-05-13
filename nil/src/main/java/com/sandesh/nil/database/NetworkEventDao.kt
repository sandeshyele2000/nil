package com.sandesh.nil.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sandesh.nil.model.NetworkEvent
import kotlinx.coroutines.flow.Flow


@Dao
interface NetworkEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(networkEvent: NetworkEvent)

    @Query("""
        SELECT * FROM network_events
        ORDER BY timestamp DESC
    """)
    fun observeAll(): Flow<List<NetworkEvent>>

    @Query(
        """
        SELECT * FROM network_events
        WHERE url LIKE '%' || :query || '%'
           OR method LIKE '%' || :query || '%'
           OR requestBody LIKE '%' || :query || '%'
           OR responseBody LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
        """
    )
    fun observeByQuery(query: String): Flow<List<NetworkEvent>>

    @Query("DELETE FROM network_events WHERE pinned = 0")
    suspend fun clear()

    @Query("UPDATE network_events SET pinned = :pinned WHERE id = :eventId")
    suspend fun setPinned(eventId: String, pinned: Boolean)
}
