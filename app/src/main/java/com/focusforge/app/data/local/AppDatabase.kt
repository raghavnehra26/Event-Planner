package com.focusforge.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.focusforge.app.data.local.dao.FocusSessionDao
import com.focusforge.app.data.local.dao.HabitDao
import com.focusforge.app.data.local.entity.DailyLogEntity
import com.focusforge.app.data.local.entity.FocusSessionEntity
import com.focusforge.app.data.local.entity.HabitEntity

@Database(
    entities = [
        HabitEntity::class,
        DailyLogEntity::class,
        FocusSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun focusSessionDao(): FocusSessionDao
}
