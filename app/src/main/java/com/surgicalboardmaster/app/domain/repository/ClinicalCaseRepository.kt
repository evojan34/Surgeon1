package com.surgicalboardmaster.app.domain.repository

import com.surgicalboardmaster.app.domain.model.CaseEventType
import com.surgicalboardmaster.app.domain.model.CaseTimelineEvent
import com.surgicalboardmaster.app.domain.model.ClinicalCaseDetail
import com.surgicalboardmaster.app.domain.model.FollowUpTask
import com.surgicalboardmaster.app.domain.model.TaskPriority
import kotlinx.coroutines.flow.Flow

interface ClinicalCaseRepository {
    fun observeCases(userId: String): Flow<List<ClinicalCaseDetail>>
    fun observeCaseTimeline(userId: String, caseId: String): Flow<List<CaseTimelineEvent>>
    fun observeFollowUpTasks(userId: String): Flow<List<FollowUpTask>>

    suspend fun ensureDefaultEducationalCasesSeeded(userId: String)
    suspend fun getCaseById(userId: String, caseId: String): ClinicalCaseDetail?
    suspend fun saveClinicalCase(userId: String, caseDetail: ClinicalCaseDetail): Result<ClinicalCaseDetail>

    /**
     * Appends an immutable dated event to the clinical timeline.
     * To amend a prior entry without erasing clinical history, pass [eventType] = [CaseEventType.AMENDMENT]
     * and set [correctsEventId] to the original event's ID.
     */
    suspend fun appendTimelineEvent(
        userId: String,
        caseId: String,
        eventType: CaseEventType,
        summary: String,
        clinicalDetails: String,
        correctsEventId: String? = null
    ): Result<CaseTimelineEvent>

    suspend fun archiveCase(userId: String, caseId: String, archive: Boolean): Result<Unit>
    suspend fun deleteCaseExplicitly(userId: String, caseId: String): Result<Unit>
    suspend fun exportDeIdentifiedCaseSummary(userId: String, caseId: String): Result<String>

    suspend fun scheduleFollowUpTask(
        userId: String,
        caseId: String,
        pseudonymousCaseId: String,
        title: String,
        description: String,
        dueAtEpochMillis: Long,
        priority: TaskPriority
    ): Result<FollowUpTask>

    suspend fun markFollowUpTaskCompleted(userId: String, taskId: String): Result<FollowUpTask>
    suspend fun rescheduleFollowUpTask(userId: String, taskId: String, newDueAtEpochMillis: Long): Result<FollowUpTask>
}
