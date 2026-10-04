package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trading_tasks")
data class TradingTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "Routine", // "Routine", "Risk Check", "Discipline", "Psychology", "Review"
    val createdDate: String, // "YYYY-MM-DD"
    val isCompleted: Boolean = false,
    val completedDate: String? = null // "YYYY-MM-DD" when it was completed
)
