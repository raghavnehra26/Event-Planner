package com.focusforge.app.data.repository

import com.focusforge.app.data.local.dao.FocusSessionDao
import com.focusforge.app.data.local.entity.FocusSessionEntity
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.focusforge.app.domain.model.FocusSession
import com.focusforge.app.domain.repository.FocusSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusSessionRepositoryImpl @Inject constructor(
    private val focusSessionDao: FocusSessionDao,
    private val preferencesRepository: UserPreferencesRepository
) : FocusSessionRepository {

    override suspend fun recordFocusSession(session: FocusSession): Long = withContext(Dispatchers.IO) {
        val id = focusSessionDao.insertSession(FocusSessionEntity.fromDomain(session))
        // Increment completed sessions counter
        preferencesRepository.incrementCompletedSessions()
        id
    }

    override fun getAllSessions(): Flow<List<FocusSession>> {
        return focusSessionDao.getAllSessions()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getTodayFocusMinutes(todayString: String): Flow<Int> {
        return focusSessionDao.getTodayFocusMinutes(todayString)
            .map { it ?: 0 }
            .flowOn(Dispatchers.IO)
    }

    override fun getTotalFocusMinutes(): Flow<Int> {
        return focusSessionDao.getTotalFocusMinutes()
            .map { it ?: 0 }
            .flowOn(Dispatchers.IO)
    }

    override fun getTotalSessionsCount(): Flow<Int> {
        return focusSessionDao.getTotalSessionsCount()
            .flowOn(Dispatchers.IO)
    }
}
