package com.surgicalboardmaster.app.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.surgicalboardmaster.app.data.local.dao.ClinicalCasesDao
import com.surgicalboardmaster.app.data.local.entity.CaseTimelineEventEntity
import com.surgicalboardmaster.app.data.local.entity.ClinicalCaseEntity
import com.surgicalboardmaster.app.data.local.entity.FollowUpTaskEntity
import com.surgicalboardmaster.app.data.local.entity.toDomainModel
import com.surgicalboardmaster.app.domain.model.CaseCategory
import com.surgicalboardmaster.app.domain.model.CaseEventType
import com.surgicalboardmaster.app.domain.model.CaseStatus
import com.surgicalboardmaster.app.domain.model.CaseTimelineEvent
import com.surgicalboardmaster.app.domain.model.ClinicalCaseDetail
import com.surgicalboardmaster.app.domain.model.ClinicalPrivacyValidator
import com.surgicalboardmaster.app.domain.model.FollowUpStatus
import com.surgicalboardmaster.app.domain.model.FollowUpTask
import com.surgicalboardmaster.app.domain.model.TaskPriority
import com.surgicalboardmaster.app.domain.repository.ClinicalCaseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstClinicalCaseRepository @Inject constructor(
    private val clinicalCasesDao: ClinicalCasesDao,
    @ApplicationContext private val context: Context
) : ClinicalCaseRepository {

    override fun observeCases(userId: String): Flow<List<ClinicalCaseDetail>> {
        return clinicalCasesDao.observeCasesForUser(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun observeCaseTimeline(
        userId: String,
        caseId: String
    ): Flow<List<CaseTimelineEvent>> {
        return clinicalCasesDao.observeTimelineForCase(userId, caseId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun observeFollowUpTasks(userId: String): Flow<List<FollowUpTask>> {
        val now = System.currentTimeMillis()
        return clinicalCasesDao.observeFollowUpTasksForUser(userId).map { list ->
            list.map { entity ->
                val domain = entity.toDomainModel()
                if (domain.status == FollowUpStatus.PENDING && domain.dueAtEpochMillis < now) {
                    domain.copy(status = FollowUpStatus.OVERDUE)
                } else {
                    domain
                }
            }
        }
    }

    override suspend fun ensureDefaultEducationalCasesSeeded(userId: String) {
        if (clinicalCasesDao.countCasesForUser(userId) > 0) return

        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val seedCaseId = "case_edu_hpb_$userId"

        // Note: per Part 9 Privacy Policy, only a de-identified EDUCATIONAL teaching case is seeded.
        val educationalSeed = ClinicalCaseEntity(
            caseId = seedCaseId,
            userId = userId,
            pseudonymousId = "EDU-HPB-2026-01",
            categoryName = CaseCategory.EDUCATIONAL_DEIDENTIFIED.name,
            ageRange = "40-49y",
            chiefComplaint = "Right upper quadrant pain, fever (38.8°C), and jaundice",
            historyOfPresentIllness = "48-hour history of biliary colic progressing to persistent RUQ pain, rigors, and dark urine.",
            relevantMedicalSurgicalHistory = "Known symptomatic cholelithiasis; no prior abdominal surgery.",
            medicationsAndAllergies = "NKDA.",
            examinationFindings = "Icteric sclerae, tender RUQ, HR 110 bpm, BP 114/72 mmHg.",
            laboratoryResults = "WBC 16.4 x10^9/L, Total Bilirubin 98 umol/L, Direct Bilirubin 74 umol/L, ALP 410 U/L, Lipase normal.",
            imagingReports = "US Abdomen: Multiple gallbladder calculi, CBD dilated to 11 mm with distal acoustic shadow.",
            differentialDiagnosis = "Acute Calculous Cholangitis vs Mirizzi Syndrome vs Acute Cholecystitis",
            finalDiagnosis = "Acute Calculous Cholangitis (Tokyo Grade II)",
            admissionInformation = "Admitted to Surgical High-Dependency Unit.",
            operationDetails = "Urgent ERCP + Endoscopic Sphincterotomy + Stone Extraction, followed by index admission Laparoscopic Cholecystectomy",
            operativeNotes = "Strasberg Critical View of Safety achieved; cystic duct and artery clipped and divided safely.",
            postoperativeProgress = "POD 1: Afebrile, bilirubin trending down to 34 umol/L, tolerating oral diet.",
            complications = "None",
            currentTreatmentPlan = "Complete 5-day antibiotic course and verify LFT normalization.",
            pendingResults = "Gallbladder histopathology report",
            nextAction = "Outpatient surgical clinic review with histopathology",
            dischargeSummary = "Discharged POD 2 in stable condition.",
            followUpPlan = "Clinic review in 2 weeks",
            learningNotes = "Charcot's triad requires prompt IV resuscitation, broad-spectrum antibiotics, and biliary drainage via ERCP prior to cholecystectomy.",
            relatedEducationalTopicsCsv = "topic_mirizzi|top_cholangitis",
            statusName = CaseStatus.FOLLOW_UP_DUE.name,
            isArchived = false,
            createdAtEpochMillis = now - 48 * hour,
            updatedAtEpochMillis = now - 4 * hour
        )

        val initialEvent = CaseTimelineEventEntity(
            eventId = "evt_seed_1_$userId",
            userId = userId,
            caseId = seedCaseId,
            eventTypeName = CaseEventType.PROCEDURE.name,
            summary = "Urgent ERCP & Biliary Decompression Completed",
            clinicalDetails = "Purulent bile drained following sphincterotomy; 9-mm distal CBD calculus extracted via balloon sweep.",
            recordedAtEpochMillis = now - 24 * hour
        )

        val followUpTask = FollowUpTaskEntity(
            taskId = "task_seed_1_$userId",
            userId = userId,
            caseId = seedCaseId,
            pseudonymousCaseId = "EDU-HPB-2026-01",
            title = "Review Gallbladder Histopathology & Repeat LFTs",
            description = "Verify absence of incidental gallbladder dysplasia and confirm normal alkaline phosphatase.",
            dueAtEpochMillis = now + 24 * hour,
            statusName = FollowUpStatus.PENDING.name,
            priorityName = TaskPriority.HIGH.name,
            createdAtEpochMillis = now - 12 * hour,
            updatedAtEpochMillis = now - 12 * hour
        )

        clinicalCasesDao.upsertCase(educationalSeed)
        clinicalCasesDao.insertAppendOnlyTimelineEvent(initialEvent)
        clinicalCasesDao.upsertFollowUpTask(followUpTask)
    }

    override suspend fun getCaseById(userId: String, caseId: String): ClinicalCaseDetail? {
        return clinicalCasesDao.getCaseById(caseId, userId)?.toDomainModel()
    }

    override suspend fun saveClinicalCase(
        userId: String,
        caseDetail: ClinicalCaseDetail
    ): Result<ClinicalCaseDetail> {
        ClinicalPrivacyValidator.validatePseudonymousId(caseDetail.pseudonymousId)
            .onFailure { return Result.failure(it) }
        ClinicalPrivacyValidator.validateNoRawIdentifiersInText("Chief Complaint", caseDetail.chiefComplaint)
            .onFailure { return Result.failure(it) }
        ClinicalPrivacyValidator.validateNoRawIdentifiersInText("HPI / Notes", caseDetail.historyOfPresentIllness)
            .onFailure { return Result.failure(it) }

        val now = System.currentTimeMillis()
        val resolvedId = caseDetail.caseId.ifBlank { "case_${UUID.randomUUID()}" }
        val updatedDomain = caseDetail.copy(
            caseId = resolvedId,
            userId = userId,
            updatedAtEpochMillis = now
        )

        val entity = ClinicalCaseEntity(
            caseId = updatedDomain.caseId,
            userId = userId,
            pseudonymousId = updatedDomain.pseudonymousId.trim(),
            categoryName = updatedDomain.category.name,
            ageRange = updatedDomain.ageRange.trim(),
            chiefComplaint = updatedDomain.chiefComplaint.trim(),
            historyOfPresentIllness = updatedDomain.historyOfPresentIllness.trim(),
            relevantMedicalSurgicalHistory = updatedDomain.relevantMedicalSurgicalHistory.trim(),
            medicationsAndAllergies = updatedDomain.medicationsAndAllergies.trim(),
            examinationFindings = updatedDomain.examinationFindings.trim(),
            laboratoryResults = updatedDomain.laboratoryResults.trim(),
            imagingReports = updatedDomain.imagingReports.trim(),
            differentialDiagnosis = updatedDomain.differentialDiagnosis.trim(),
            finalDiagnosis = updatedDomain.finalDiagnosis.trim(),
            admissionInformation = updatedDomain.admissionInformation.trim(),
            operationDetails = updatedDomain.operationDetails.trim(),
            operativeNotes = updatedDomain.operativeNotes.trim(),
            postoperativeProgress = updatedDomain.postoperativeProgress.trim(),
            complications = updatedDomain.complications.trim(),
            currentTreatmentPlan = updatedDomain.currentTreatmentPlan.trim(),
            pendingResults = updatedDomain.pendingResults.trim(),
            nextAction = updatedDomain.nextAction.trim(),
            dischargeSummary = updatedDomain.dischargeSummary.trim(),
            followUpPlan = updatedDomain.followUpPlan.trim(),
            learningNotes = updatedDomain.learningNotes.trim(),
            relatedEducationalTopicsCsv = updatedDomain.relatedEducationalTopics.joinToString("|"),
            statusName = updatedDomain.status.name,
            isArchived = updatedDomain.isArchived,
            createdAtEpochMillis = updatedDomain.createdAtEpochMillis,
            updatedAtEpochMillis = now,
            isPendingCloudSync = true
        )

        clinicalCasesDao.upsertCase(entity)
        syncCaseToFirestoreIfAuthenticated(entity)
        return Result.success(updatedDomain)
    }

    override suspend fun appendTimelineEvent(
        userId: String,
        caseId: String,
        eventType: CaseEventType,
        summary: String,
        clinicalDetails: String,
        correctsEventId: String?
    ): Result<CaseTimelineEvent> {
        if (summary.isBlank()) {
            return Result.failure(IllegalArgumentException("Timeline event summary cannot be empty."))
        }
        ClinicalPrivacyValidator.validateNoRawIdentifiersInText("Event Details", clinicalDetails)
            .onFailure { return Result.failure(it) }

        val now = System.currentTimeMillis()
        val resolvedType = if (!correctsEventId.isNullOrBlank()) {
            CaseEventType.AMENDMENT
        } else {
            eventType
        }

        val entity = CaseTimelineEventEntity(
            eventId = "evt_${UUID.randomUUID()}",
            userId = userId,
            caseId = caseId,
            eventTypeName = resolvedType.name,
            summary = summary.trim(),
            clinicalDetails = clinicalDetails.trim(),
            recordedAtEpochMillis = now,
            correctsEventId = correctsEventId,
            isPendingCloudSync = true
        )

        clinicalCasesDao.insertAppendOnlyTimelineEvent(entity)
        syncTimelineEventToFirestoreIfAuthenticated(entity)
        return Result.success(entity.toDomainModel())
    }

    override suspend fun archiveCase(
        userId: String,
        caseId: String,
        archive: Boolean
    ): Result<Unit> {
        clinicalCasesDao.setCaseArchived(caseId, userId, archive, System.currentTimeMillis())
        return Result.success(Unit)
    }

    override suspend fun deleteCaseExplicitly(userId: String, caseId: String): Result<Unit> {
        clinicalCasesDao.deleteCase(caseId, userId)
        return Result.success(Unit)
    }

    override suspend fun exportDeIdentifiedCaseSummary(
        userId: String,
        caseId: String
    ): Result<String> {
        val detail = clinicalCasesDao.getCaseById(caseId, userId)?.toDomainModel()
            ?: return Result.failure(IllegalArgumentException("Case not found"))
        val timeline = clinicalCasesDao.getTimelineForCaseOnce(userId, caseId)

        val builder = StringBuilder()
        builder.appendLine("=== DE-IDENTIFIED SURGICAL CASE LOG ===")
        builder.appendLine("Notice: De-identification reduces risk but cannot guarantee zero re-identification risk.")
        builder.appendLine("Pseudonymous ID: ${detail.pseudonymousId}")
        builder.appendLine("Category: ${detail.category.name} | Status: ${detail.status.displayLabel}")
        builder.appendLine("Age Range: ${detail.ageRange}")
        builder.appendLine("Chief Complaint: ${detail.chiefComplaint}")
        builder.appendLine("Final / Working Diagnosis: ${detail.finalDiagnosis.ifBlank { detail.differentialDiagnosis }}")
        builder.appendLine("Procedure: ${detail.operationDetails}")
        builder.appendLine("Current Plan: ${detail.currentTreatmentPlan}")
        builder.appendLine("Next Action: ${detail.nextAction}")
        builder.appendLine("--- APPEND-ONLY CLINICAL TIMELINE (${timeline.size} events) ---")
        timeline.forEach { evt ->
            val amendNote = evt.correctsEventId?.let { " [AMENDS EVENT $it]" } ?: ""
            builder.appendLine("• [${evt.eventTypeName}]$amendNote ${evt.summary}: ${evt.clinicalDetails}")
        }
        return Result.success(builder.toString())
    }

    override suspend fun scheduleFollowUpTask(
        userId: String,
        caseId: String,
        pseudonymousCaseId: String,
        title: String,
        description: String,
        dueAtEpochMillis: Long,
        priority: TaskPriority
    ): Result<FollowUpTask> {
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Follow-up title is required."))
        }
        val now = System.currentTimeMillis()
        val entity = FollowUpTaskEntity(
            taskId = "task_${UUID.randomUUID()}",
            userId = userId,
            caseId = caseId,
            pseudonymousCaseId = pseudonymousCaseId.trim(),
            title = title.trim(),
            description = description.trim(),
            dueAtEpochMillis = dueAtEpochMillis,
            statusName = FollowUpStatus.PENDING.name,
            priorityName = priority.name,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
            isPendingCloudSync = true
        )
        clinicalCasesDao.upsertFollowUpTask(entity)
        syncFollowUpTaskToFirestoreIfAuthenticated(entity)
        return Result.success(entity.toDomainModel())
    }

    override suspend fun markFollowUpTaskCompleted(
        userId: String,
        taskId: String
    ): Result<FollowUpTask> {
        val existing = clinicalCasesDao.getFollowUpTaskById(taskId, userId)
            ?: return Result.failure(IllegalArgumentException("Follow-up task not found"))
        val now = System.currentTimeMillis()
        val updated = existing.copy(
            statusName = FollowUpStatus.COMPLETED.name,
            completedAtEpochMillis = now,
            updatedAtEpochMillis = now,
            isPendingCloudSync = true
        )
        clinicalCasesDao.upsertFollowUpTask(updated)
        syncFollowUpTaskToFirestoreIfAuthenticated(updated)
        return Result.success(updated.toDomainModel())
    }

    override suspend fun rescheduleFollowUpTask(
        userId: String,
        taskId: String,
        newDueAtEpochMillis: Long
    ): Result<FollowUpTask> {
        val existing = clinicalCasesDao.getFollowUpTaskById(taskId, userId)
            ?: return Result.failure(IllegalArgumentException("Follow-up task not found"))
        val now = System.currentTimeMillis()
        val updated = existing.copy(
            dueAtEpochMillis = newDueAtEpochMillis,
            statusName = FollowUpStatus.PENDING.name,
            updatedAtEpochMillis = now,
            isPendingCloudSync = true
        )
        clinicalCasesDao.upsertFollowUpTask(updated)
        syncFollowUpTaskToFirestoreIfAuthenticated(updated)
        return Result.success(updated.toDomainModel())
    }

    private fun isCloudSyncActive(userId: String): Boolean {
        return runCatching {
            FirebaseApp.getApps(context).isNotEmpty() &&
                FirebaseAuth.getInstance().currentUser?.uid == userId
        }.getOrDefault(false)
    }

    private fun syncCaseToFirestoreIfAuthenticated(entity: ClinicalCaseEntity) {
        if (!isCloudSyncActive(entity.userId)) return
        runCatching {
            val payload = mapOf(
                "caseId" to entity.caseId,
                "pseudonymousId" to entity.pseudonymousId,
                "category" to entity.categoryName,
                "ageRange" to entity.ageRange,
                "chiefComplaint" to entity.chiefComplaint,
                "finalDiagnosis" to entity.finalDiagnosis,
                "operationDetails" to entity.operationDetails,
                "status" to entity.statusName,
                "currentTreatmentPlan" to entity.currentTreatmentPlan,
                "nextAction" to entity.nextAction,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(entity.userId)
                .collection("clinicalCases")
                .document(entity.caseId)
                .set(payload, SetOptions.merge())
        }
    }

    private fun syncTimelineEventToFirestoreIfAuthenticated(entity: CaseTimelineEventEntity) {
        if (!isCloudSyncActive(entity.userId)) return
        runCatching {
            val payload = mapOf(
                "eventId" to entity.eventId,
                "caseId" to entity.caseId,
                "eventType" to entity.eventTypeName,
                "summary" to entity.summary,
                "clinicalDetails" to entity.clinicalDetails,
                "correctsEventId" to entity.correctsEventId,
                "recordedAt" to FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(entity.userId)
                .collection("clinicalCases")
                .document(entity.caseId)
                .collection("caseEvents")
                .document(entity.eventId)
                .set(payload)
        }
    }

    private fun syncFollowUpTaskToFirestoreIfAuthenticated(entity: FollowUpTaskEntity) {
        if (!isCloudSyncActive(entity.userId)) return
        runCatching {
            val payload = mapOf(
                "taskId" to entity.taskId,
                "caseId" to entity.caseId,
                "pseudonymousCaseId" to entity.pseudonymousCaseId,
                "title" to entity.title,
                "description" to entity.description,
                "dueAtEpochMillis" to entity.dueAtEpochMillis,
                "status" to entity.statusName,
                "priority" to entity.priorityName,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(entity.userId)
                .collection("followUpTasks")
                .document(entity.taskId)
                .set(payload, SetOptions.merge())
        }
    }
}
