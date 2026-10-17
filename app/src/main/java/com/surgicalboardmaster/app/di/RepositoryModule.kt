package com.surgicalboardmaster.app.di

import com.surgicalboardmaster.app.data.repository.FirebaseAuthRepositoryImpl
import com.surgicalboardmaster.app.data.repository.OfflineFirstDashboardRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstExamRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstFlashcardRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstLessonRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstSurgicalLibraryRepository
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.DashboardRepository
import com.surgicalboardmaster.app.domain.repository.ExamRepository
import com.surgicalboardmaster.app.domain.repository.FlashcardRepository
import com.surgicalboardmaster.app.domain.repository.LessonRepository
import com.surgicalboardmaster.app.domain.repository.SurgicalLibraryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        impl: OfflineFirstDashboardRepository
    ): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSurgicalLibraryRepository(
        impl: OfflineFirstSurgicalLibraryRepository
    ): SurgicalLibraryRepository

    @Binds
    @Singleton
    abstract fun bindLessonRepository(
        impl: OfflineFirstLessonRepository
    ): LessonRepository

    @Binds
    @Singleton
    abstract fun bindExamRepository(
        impl: OfflineFirstExamRepository
    ): ExamRepository

    @Binds
    @Singleton
    abstract fun bindFlashcardRepository(
        impl: OfflineFirstFlashcardRepository
    ): FlashcardRepository
}
