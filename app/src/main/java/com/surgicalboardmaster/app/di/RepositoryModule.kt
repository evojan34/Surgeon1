package com.surgicalboardmaster.app.di

import com.surgicalboardmaster.app.data.repository.OfflineFirstDashboardRepository
import com.surgicalboardmaster.app.domain.repository.DashboardRepository
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
}

