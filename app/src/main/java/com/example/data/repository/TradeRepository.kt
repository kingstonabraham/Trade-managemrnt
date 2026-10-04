package com.example.data.repository

import com.example.data.local.TaskDao
import com.example.data.local.TradeDao
import com.example.data.model.Trade
import com.example.data.model.TradingTaskEntity
import kotlinx.coroutines.flow.Flow

class TradeRepository(
    private val tradeDao: TradeDao,
    private val taskDao: TaskDao
) {

    val allTrades: Flow<List<Trade>> = tradeDao.getAllTrades()

    fun getTradesByDate(date: String): Flow<List<Trade>> = tradeDao.getTradesByDate(date)

    fun getTradesBetweenDates(startDate: String, endDate: String): Flow<List<Trade>> =
        tradeDao.getTradesBetweenDates(startDate, endDate)

    suspend fun getTradeById(id: Long): Trade? = tradeDao.getTradeById(id)

    suspend fun insertTrade(trade: Trade): Long = tradeDao.insertTrade(trade)

    suspend fun insertTrades(trades: List<Trade>) = tradeDao.insertTrades(trades)

    suspend fun updateTrade(trade: Trade) = tradeDao.updateTrade(trade)

    suspend fun deleteTrade(trade: Trade) = tradeDao.deleteTrade(trade)

    suspend fun deleteTradeById(id: Long) = tradeDao.deleteTradeById(id)

    suspend fun getTradeCount(): Int = tradeDao.getTradeCount()

    suspend fun clearAllTrades() = tradeDao.clearAllTrades()

    // Tasks operations
    val allTasks: Flow<List<TradingTaskEntity>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TradingTaskEntity): Long = taskDao.insertTask(task)

    suspend fun insertTasks(tasks: List<TradingTaskEntity>) = taskDao.insertTasks(tasks)

    suspend fun updateTask(task: TradingTaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TradingTaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)
}
