package com.surgicalboardmaster.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.surgicalboardmaster.app.data.local.entity.CaseTimelineEventEntity
import com.surgicalboardmaster.app.data.local.entity.ClinicalCaseEntity
import com.surgicalboardmaster.app.data.local.entity.FollowUpTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClinicalCasesDao {

    @Query("SELECT * FROM clinical_cases WHERE userId = :userId ORDER BY updatedAtEpochMillis DESC")
    fun observeCasesForUser(userId: String): Flow<List<ClinicalCaseEntity>>

    @Query("SELECT * FROM clinical_cases WHERE caseId = :caseId AND userId = :userId LIMIT 1")
    suspend fun getCaseById(caseId: String, userId: String): ClinicalCaseEntity?

    @Query("SELECT COUNT(*) FROM clinical_cases WHERE userId = :userId")
    suspend fun countCasesForUser(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCase(clinicalCase: ClinicalCaseEntity)

    @Query("UPDATE clinical_cases SET isArchived = :archived, updatedAtEpochMillis = :updatedAt WHERE caseId = :caseId AND userId = :userId")
    suspend fun setCaseArchived(caseId: String, userId: String, archived: Boolean, updatedAt: Long)

    @Query("DELETE FROM clinical_cases WHERE caseId = :caseId AND userId = :userId")
    suspend fun deleteCase(caseId: String, userId: String)

    // APPEND-ONLY CLINICAL TIMELINE DAO:
    // Notice there is NO update or individual event delete method to preserve audit integrity.
    @Query("SELECT * FROM case_timeline_events WHERE userId = :userId AND caseId = :caseId ORDER BY recordedAtEpochMillis DESC")
    fun observeTimelineForCase(userId: String, caseId: String): Flow<List<CaseTimelineEventEntity>>

    @Query("SELECT * FROM case_timeline_events WHERE userId = :userId AND caseId = :caseId ORDER BY recordedAtEpochMillis DESC")
    suspend fun getTimelineForCaseOnce(userId: String, caseId: String): List<CaseTimelineEventEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAppendOnlyTimelineEvent(event: CaseTimelineEventEntity)

    // Follow-Up Tasks
    @Query("SELECT * FROM follow_up_tasks WHERE userId = :userId ORDER BY dueAtEpochMillis ASC")
    fun observeFollowUpTasksForUser(userId: String): Flow<List<FollowUpTaskEntity>>

    @Query("SELECT * FROM follow_up_tasks WHERE taskId = :taskId AND userId = :userId LIMIT 1")
    suspend fun getFollowUpTaskById(taskId: String, userId: String): FollowUpTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFollowUpTask(task: FollowUpTaskEntity)

    @Query("DELETE FROM follow_up_tasks WHERE taskId = :taskId AND userId = :userId")
    suspend fun deleteFollowUpTask(taskId: String, userId: String)
}
