package com.surgicalboardmaster.app.di

import android.content.Context
import androidx.room.Room
import com.surgicalboardmaster.app.data.local.SbmDatabase
import com.surgicalboardmaster.app.data.local.dao.SurgicalLibraryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSbmDatabase(
        @ApplicationContext context: Context
    ): SbmDatabase {
        return Room.databaseBuilder(
            context,
            SbmDatabase::class.java,
            "surgical_board_master_educational.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSurgicalLibraryDao(database: SbmDatabase): SurgicalLibraryDao {
        return database.surgicalLibraryDao()
    }
}
