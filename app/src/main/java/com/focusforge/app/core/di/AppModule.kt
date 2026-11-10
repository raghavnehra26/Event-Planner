package com.focusforge.app.core.di

import android.content.Context
import androidx.room.Room
import com.focusforge.app.data.local.AppDatabase
import com.focusforge.app.data.local.dao.FocusSessionDao
import com.focusforge.app.data.local.dao.HabitDao
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.focusforge.app.data.repository.FocusSessionRepositoryImpl
import com.focusforge.app.data.repository.HabitRepositoryImpl
import com.focusforge.app.domain.repository.FocusSessionRepository
import com.focusforge.app.domain.repository.HabitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "focusforge_database.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideHabitDao(database: AppDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideFocusSessionDao(database: AppDatabase): FocusSessionDao {
        return database.focusSessionDao()
    }

    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: HabitDao,
        focusSessionDao: FocusSessionDao,
        preferencesRepository: UserPreferencesRepository
    ): HabitRepository {
        return HabitRepositoryImpl(habitDao, focusSessionDao, preferencesRepository)
    }

    @Provides
    @Singleton
    fun provideFocusSessionRepository(
        focusSessionDao: FocusSessionDao,
        preferencesRepository: UserPreferencesRepository
    ): FocusSessionRepository {
        return FocusSessionRepositoryImpl(focusSessionDao, preferencesRepository)
    }
}
