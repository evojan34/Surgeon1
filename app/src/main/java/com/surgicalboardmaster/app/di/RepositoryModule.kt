package com.surgicalboardmaster.app.di

import android.content.Context
import com.surgicalboardmaster.app.data.local.dao.SurgicalLibraryDao
import com.surgicalboardmaster.app.data.repository.FirebaseAuthRepositoryImpl
import com.surgicalboardmaster.app.data.repository.OfflineFirstDashboardRepository
import com.surgicalboardmaster.app.data.repository.OfflineFirstSurgicalLibraryRepository
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.DashboardRepository
import com.surgicalboardmaster.app.domain.repository.SurgicalLibraryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDashboardRepository(): DashboardRepository {
        return OfflineFirstDashboardRepository()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context
    ): AuthRepository {
        return FirebaseAuthRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideSurgicalLibraryRepository(
        libraryDao: SurgicalLibraryDao,
        @ApplicationContext context: Context
    ): SurgicalLibraryRepository {
        return OfflineFirstSurgicalLibraryRepository(libraryDao, context)
    }
}
