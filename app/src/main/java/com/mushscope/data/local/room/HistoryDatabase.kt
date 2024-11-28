package com.mushscope.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mushscope.data.local.entity.HistoryEntity

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class HistoryDatabase: RoomDatabase(){
    abstract fun HistoryDao(): HistoryDao

    companion object {
        @Volatile
        private var instance: HistoryDatabase? = null
        fun getInstance(context: Context): HistoryDatabase =
            instance?: synchronized(this) {
                instance?: Room.databaseBuilder(
                    context.applicationContext,
                    HistoryDatabase::class.java,
                    "History.db"
                ).build()
            }
    }
}