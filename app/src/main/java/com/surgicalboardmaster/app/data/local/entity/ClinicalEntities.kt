package com.surgicalboardmaster.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.surgicalboardmaster.app.domain.model.CaseCategory
import com.surgicalboardmaster.app.domain.model.CaseEventType
import com.surgicalboardmaster.app.domain.model.CaseStatus
import com.surgicalboardmaster.app.domain.model.CaseTimelineEvent
import com.surgicalboardmaster.app.domain.model.ClinicalCaseDetail
import com.surgicalboardmaster.app.domain.model.FollowUpStatus
import com.surgicalboardmaster.app.domain.model.FollowUpTask
import com.surgicalboardmaster.app.domain.model.TaskPriority

@Entity(
    tableName = "clinical_cases",
    indices = [
        Index(value = ["userId", "statusName", "updatedAtEpochMillis"]),
        Index(value = ["userId", "categoryName"])
    ]
)
data class ClinicalCaseEntity(
    @PrimaryKey val caseId: String,
    val userId: String,
    val pseudonymousId: String,
    val categoryName: String,
    val ageRange: String,
    val chiefComplaint: String,
    val historyOfPresentIllness: String,
    val relevantMedicalSurgicalHistory: String,
    val medicationsAndAllergies: String,
    val examinationFindings: String,
    val laboratoryResults: String,
    val imagingReports: String,
    val differentialDiagnosis: String,
    val finalDiagnosis: String,
    val admissionInformation: String,
    val operationDetails: String,
    val operativeNotes: String,
    val postoperativeProgress: String,
    val complications: String,
    val currentTreatmentPlan: String,
    val pendingResults: String,
    val nextAction: String,
    val dischargeSummary: String,
    val followUpPlan: String,
    val learningNotes: String,
    val relatedEducationalTopicsCsv: String,
    val statusName: String,
    val isArchived: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isPendingCloudSync: Boolean = false
)

/**
 * Append-only clinical timeline event entity.
 * Corrections must be inserted as a new row with [eventTypeName] = "AMENDMENT"
 * and [correctsEventId] referencing the original immutable [eventId].
 */
@Entity(
    tableName = "case_timeline_events",
    indices = [
        Index(value = ["userId", "caseId", "recordedAtEpochMillis"])
    ]
)
data class CaseTimelineEventEntity(
    @PrimaryKey val eventId: String,
    val userId: String,
    val caseId: String,
    val eventTypeName: String,
    val summary: String,
    val clinicalDetails: String,
    val recordedAtEpochMillis: Long,
    val correctsEventId: String? = null,
    val isPendingCloudSync: Boolean = false
)

@Entity(
    tableName = "follow_up_tasks",
    indices = [
        Index(value = ["userId", "statusName", "dueAtEpochMillis"]),
        Index(value = ["userId", "caseId"])
    ]
)
data class FollowUpTaskEntity(
    @PrimaryKey val taskId: String,
    val userId: String,
    val caseId: String,
    val pseudonymousCaseId: String,
    val title: String,
    val description: String,
    val dueAtEpochMillis: Long,
    val statusName: String,
    val priorityName: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val completedAtEpochMillis: Long? = null,
    val isPendingCloudSync: Boolean = false
)

fun ClinicalCaseEntity.toDomainModel(): ClinicalCaseDetail = ClinicalCaseDetail(
    caseId = caseId,
    userId = userId,
    pseudonymousId = pseudonymousId,
    category = runCatching { CaseCategory.valueOf(categoryName) }.getOrDefault(CaseCategory.PRIVATE_CLINICAL_RECORD),
    ageRange = ageRange,
    chiefComplaint = chiefComplaint,
    historyOfPresentIllness = historyOfPresentIllness,
    relevantMedicalSurgicalHistory = relevantMedicalSurgicalHistory,
    medicationsAndAllergies = medicationsAndAllergies,
    examinationFindings = examinationFindings,
    laboratoryResults = laboratoryResults,
    imagingReports = imagingReports,
    differentialDiagnosis = differentialDiagnosis,
    finalDiagnosis = finalDiagnosis,
    admissionInformation = admissionInformation,
    operationDetails = operationDetails,
    operativeNotes = operativeNotes,
    postoperativeProgress = postoperativeProgress,
    complications = complications,
    currentTreatmentPlan = currentTreatmentPlan,
    pendingResults = pendingResults,
    nextAction = nextAction,
    dischargeSummary = dischargeSummary,
    followUpPlan = followUpPlan,
    learningNotes = learningNotes,
    relatedEducationalTopics = relatedEducationalTopicsCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() },
    status = runCatching { CaseStatus.valueOf(statusName) }.getOrDefault(CaseStatus.DRAFT),
    isArchived = isArchived,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun CaseTimelineEventEntity.toDomainModel(): CaseTimelineEvent = CaseTimelineEvent(
    eventId = eventId,
    caseId = caseId,
    eventType = runCatching { CaseEventType.valueOf(eventTypeName) }.getOrDefault(CaseEventType.CLINICAL_ASSESSMENT),
    summary = summary,
    clinicalDetails = clinicalDetails,
    recordedAtEpochMillis = recordedAtEpochMillis,
    correctsEventId = correctsEventId
)

fun FollowUpTaskEntity.toDomainModel(): FollowUpTask = FollowUpTask(
    taskId = taskId,
    caseId = caseId,
    pseudonymousCaseId = pseudonymousCaseId,
    title = title,
    description = description,
    dueAtEpochMillis = dueAtEpochMillis,
    status = runCatching { FollowUpStatus.valueOf(statusName) }.getOrDefault(FollowUpStatus.PENDING),
    priority = runCatching { TaskPriority.valueOf(priorityName) }.getOrDefault(TaskPriority.ROUTINE),
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    completedAtEpochMillis = completedAtEpochMillis
)
