package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Trade
import com.example.data.model.TradingTaskEntity
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Trade::class, TradingTaskEntity::class], version = 2, exportSchema = false)
abstract class TradeDatabase : RoomDatabase() {

    abstract fun tradeDao(): TradeDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TradeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TradeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TradeDatabase::class.java,
                    "trade_management_real_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
