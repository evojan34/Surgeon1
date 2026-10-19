package com.surgicalboardmaster.app.di

import com.surgicalboardmaster.app.data.repository.OfflineFirstClinicalCaseRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstDashboardRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstExamRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstFlashcardRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstLessonRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstSimulatorRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstSurgicalLibraryRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstTutorRepository
import com.surgicalboardmaster.app.domain.repository.ClinicalCaseRepository
import com.surgicalboardmaster.app.domain.repository.DashboardRepository
import com.surgicalboardmaster.app.domain.repository.ExamRepository
import com.surgicalboardmaster.app.domain.repository.FlashcardRepository
import com.surgicalboardmaster.app.domain.repository.LessonRepository
import com.surgicalboardmaster.app.domain.repository.SimulatorRepository
import com.surgicalboardmaster.app.domain.repository.SurgicalLibraryRepository
import com.surgicalboardmaster.app.domain.repository.TutorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindSurgicalLibraryRepository(
        impl: OfflineFirstSurgicalLibraryRepository
    ): SurgicalLibraryRepository

    @Binds
    abstract fun bindLessonRepository(
        impl: OfflineFirstLessonRepository
    ): LessonRepository

    @Binds
    abstract fun bindExamRepository(
        impl: OfflineFirstExamRepository
    ): ExamRepository

    @Binds
    abstract fun bindFlashcardRepository(
        impl: OfflineFirstFlashcardRepository
    ): FlashcardRepository

    @Binds
    abstract fun bindClinicalCaseRepository(
        impl: OfflineFirstClinicalCaseRepository
    ): ClinicalCaseRepository

    @Binds
    abstract fun bindSimulatorRepository(
        impl: OfflineFirstSimulatorRepository
    ): SimulatorRepository

    @Binds
    abstract fun bindDashboardRepository(
        impl: OfflineFirstDashboardRepository
    ): DashboardRepository

    @Binds
    abstract fun bindTutorRepository(
        impl: OfflineFirstTutorRepository
    ): TutorRepository
}
