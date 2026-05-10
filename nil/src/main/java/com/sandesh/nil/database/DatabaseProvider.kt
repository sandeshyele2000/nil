package com.sandesh.nil.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var database: NILDatabase? = null

    fun getDatabase(context: Context): NILDatabase {
        return database?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                NILDatabase::class.java,
                "nil_database"
            ).build()

            database = instance

            instance
        }
    }
}