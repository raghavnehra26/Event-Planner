package com.focusforge.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.focusforge.app.domain.model.FocusSession
import com.focusforge.app.domain.model.SessionMode

@Entity(
    tableName = "focus_sessions",
    indices = [
        Index(value = ["dateString"]),
        Index(value = ["habitId"])
    ]
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long? = null,
    val habitName: String? = null,
    val durationMinutes: Int,
    val mode: String,
    val completedTimestamp: Long = System.currentTimeMillis(),
    val dateString: String
) {
    fun toDomain(): FocusSession {
        val sessionMode = try {
            SessionMode.valueOf(mode)
        } catch (e: Exception) {
            SessionMode.POMODORO
        }
        return FocusSession(
            id = id,
            habitId = habitId,
            habitName = habitName,
            durationMinutes = durationMinutes,
            mode = sessionMode,
            completedTimestamp = completedTimestamp,
            dateString = dateString
        )
    }

    companion object {
        fun fromDomain(session: FocusSession): FocusSessionEntity {
            return FocusSessionEntity(
                id = session.id,
                habitId = session.habitId,
                habitName = session.habitName,
                durationMinutes = session.durationMinutes,
                mode = session.mode.name,
                completedTimestamp = session.completedTimestamp,
                dateString = session.dateString
            )
        }
    }
}
