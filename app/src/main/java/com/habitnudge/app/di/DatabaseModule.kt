package com.habitnudge.app.di

import android.content.Context
import com.habitnudge.app.data.local.HabitDatabase
import com.habitnudge.app.data.local.dao.CheckInDao
import com.habitnudge.app.data.local.dao.HabitDao
import com.habitnudge.app.data.local.dao.JournalDao
import com.habitnudge.app.data.local.dao.ReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): HabitDatabase {
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return HabitDatabase.create(context, applicationScope)
    }

    @Provides
    fun provideHabitDao(database: HabitDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideCheckInDao(database: HabitDatabase): CheckInDao = database.checkInDao()

    @Provides
    fun provideReminderDao(database: HabitDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideJournalDao(database: HabitDatabase): JournalDao = database.journalDao()
}
