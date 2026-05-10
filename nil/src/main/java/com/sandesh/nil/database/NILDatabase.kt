package com.sandesh.nil.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sandesh.nil.model.NetworkEvent

@Database(
    entities = [
        NetworkEvent::class
    ],
    version = 1,
    exportSchema = true
)
abstract class NILDatabase: RoomDatabase() {
    abstract fun networkEventDao(): NetworkEventDao
}