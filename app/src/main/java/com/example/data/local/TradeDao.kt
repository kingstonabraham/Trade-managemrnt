package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Trade
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades ORDER BY entryDate DESC, id DESC")
    fun getAllTrades(): Flow<List<Trade>>

    @Query("SELECT * FROM trades WHERE entryDate = :date ORDER BY id DESC")
    fun getTradesByDate(date: String): Flow<List<Trade>>

    @Query("SELECT * FROM trades WHERE entryDate >= :startDate AND entryDate <= :endDate ORDER BY entryDate DESC, id DESC")
    fun getTradesBetweenDates(startDate: String, endDate: String): Flow<List<Trade>>

    @Query("SELECT * FROM trades WHERE id = :id LIMIT 1")
    suspend fun getTradeById(id: Long): Trade?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: Trade): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<Trade>)

    @Update
    suspend fun updateTrade(trade: Trade)

    @Delete
    suspend fun deleteTrade(trade: Trade)

    @Query("DELETE FROM trades WHERE id = :id")
    suspend fun deleteTradeById(id: Long)

    @Query("SELECT COUNT(*) FROM trades")
    suspend fun getTradeCount(): Int

    @Query("DELETE FROM trades")
    suspend fun clearAllTrades()
}
