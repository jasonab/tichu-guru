package com.tichuguru.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlayerEntity::class, GameEntity::class, HandEntity::class], version = 1, exportSchema = false)
abstract class TichuDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun handDao(): HandDao

    companion object {
        @Volatile private var instance: TichuDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): TichuDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, TichuDatabase::class.java, "tichu.db")
                    .allowMainThreadQueries()
                    .build()
                    .also { instance = it }
            }
    }
}
