package com.focusforge.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_logs",
    indices = [
        Index(value = ["habitId", "dateString"], unique = true),
        Index(value = ["dateString"])
    ]
)
data class DailyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateString: String,
    val completedAt: Long = System.currentTimeMillis(),
    val notes: String? = null
)
