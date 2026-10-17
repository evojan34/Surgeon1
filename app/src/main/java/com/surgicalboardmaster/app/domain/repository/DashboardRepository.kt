package com.surgicalboardmaster.app.domain.repository

import com.surgicalboardmaster.app.domain.model.DashboardSummary
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeDashboardSummary(): Flow<DashboardSummary>
    suspend fun markFollowUpCompleted(taskId: String)
    suspend fun recordQuickTopicRevision(topicId: String)
}

