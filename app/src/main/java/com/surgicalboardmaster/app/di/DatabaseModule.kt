package com.surgicalboardmaster.app.di

import android.content.Context
import androidx.room.Room
import com.surgicalboardmaster.app.data.local.SbmDatabase
import com.surgicalboardmaster.app.data.local.dao.ClinicalCasesDao
import com.surgicalboardmaster.app.data.local.dao.EducationalLessonDao
import com.surgicalboardmaster.app.data.local.dao.ExamDao
import com.surgicalboardmaster.app.data.local.dao.FlashcardDao
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
            "sbm_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideSurgicalLibraryDao(db: SbmDatabase): SurgicalLibraryDao = db.surgicalLibraryDao()

    @Provides
    fun provideEducationalLessonDao(db: SbmDatabase): EducationalLessonDao = db.educationalLessonDao()

    @Provides
    fun provideExamDao(db: SbmDatabase): ExamDao = db.examDao()

    @Provides
    fun provideFlashcardDao(db: SbmDatabase): FlashcardDao = db.flashcardDao()

    @Provides
    fun provideClinicalCasesDao(db: SbmDatabase): ClinicalCasesDao = db.clinicalCasesDao()
}
