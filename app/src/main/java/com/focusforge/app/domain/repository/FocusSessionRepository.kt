package com.focusforge.app.domain.repository

import com.focusforge.app.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusSessionRepository {
    suspend fun recordFocusSession(session: FocusSession): Long
    fun getAllSessions(): Flow<List<FocusSession>>
    fun getTodayFocusMinutes(todayString: String): Flow<Int>
    fun getTotalFocusMinutes(): Flow<Int>
    fun getTotalSessionsCount(): Flow<Int>
}
