package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TradingTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM trading_tasks ORDER BY isCompleted ASC, id DESC")
    fun getAllTasks(): Flow<List<TradingTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TradingTaskEntity): Long

    @Update
    suspend fun updateTask(task: TradingTaskEntity)

    @Delete
    suspend fun deleteTask(task: TradingTaskEntity)

    @Query("DELETE FROM trading_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("SELECT COUNT(*) FROM trading_tasks")
    suspend fun getTaskCount(): Int
}
