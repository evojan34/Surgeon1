package com.surgicalboardmaster.app.data.repository

import com.surgicalboardmaster.app.domain.model.Book
import com.surgicalboardmaster.app.domain.model.CaseCategory
import com.surgicalboardmaster.app.domain.model.CaseStatus
import com.surgicalboardmaster.app.domain.model.ClinicalCaseSummary
import com.surgicalboardmaster.app.domain.model.DashboardSummary
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.FollowUpStatus
import com.surgicalboardmaster.app.domain.model.FollowUpTask
import com.surgicalboardmaster.app.domain.model.StudyStatus
import com.surgicalboardmaster.app.domain.model.TaskPriority
import com.surgicalboardmaster.app.domain.model.Topic
import com.surgicalboardmaster.app.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstDashboardRepository @Inject constructor() : DashboardRepository {

    private val nowMillis = System.currentTimeMillis()
    private val hourMillis = 3_600_000L

    private val dashboardState = MutableStateFlow(
        DashboardSummary(
            candidateTitle = "General Surgery Board Candidate",
            books = listOf(
                Book(
                    id = "book_bailey_28",
                    title = "Bailey & Love's Short Practice of Surgery",
                    edition = "28th Edition",
                    authors = listOf("Norman S. Williams", "P. Ronan O'Connell", "Andrew W. McCaskie"),
                    publicationYear = 2023,
                    userId = "local_user",
                    completedTopicsCount = 18,
                    totalTopicsCount = 42,
                    activeChapterTitle = "Ch 67: The Gallbladder and Bile Ducts",
                    createdAtEpochMillis = nowMillis - 30 * 24 * hourMillis,
                    updatedAtEpochMillis = nowMillis - 2 * hourMillis
                ),
                Book(
                    id = "book_schwartz_11",
                    title = "Schwartz's Principles of Surgery",
                    edition = "11th Edition",
                    authors = listOf("F. Charles Brunicardi", "Dana K. Andersen", "Timothy R. Billiar"),
                    publicationYear = 2019,
                    userId = "local_user",
                    completedTopicsCount = 11,
                    totalTopicsCount = 36,
                    activeChapterTitle = "Ch 33: The Pancreas — Acute Pancreatitis & Atlanta Classification",
                    createdAtEpochMillis = nowMillis - 30 * 24 * hourMillis,
                    updatedAtEpochMillis = nowMillis - 6 * hourMillis
                )
            ),
            activeLessonTitle = "Management of Acute Cholangitis & Tokyo Guidelines (Choledocholithiasis)",
            activeLessonBook = "Bailey & Love 28th Ed • HPB Surgery",
            completedTopicsTotal = 29,
            totalTopicsCount = 78,
            averageExamScorePercent = 84,
            completedSimulationsCount = 12,
            activeClinicalCasesCount = 4,
            weakTopics = listOf(
                Topic(
                    id = "topic_mirizzi",
                    chapterId = "ch_hpb_67",
                    bookTitle = "Bailey & Love 28th Ed",
                    title = "Mirizzi Syndrome (Csendes Classification & Operative Strategy)",
                    sortOrder = 4,
                    difficulty = DifficultyLevel.BOARD_ADVANCED,
                    studyStatus = StudyStatus.NEEDS_REVISION,
                    confidenceScore = 2,
                    lastStudiedEpochMillis = nowMillis - 72 * hourMillis,
                    nextRevisionEpochMillis = nowMillis - 4 * hourMillis
                ),
                Topic(
                    id = "topic_hinchey",
                    chapterId = "ch_colorectal_73",
                    bookTitle = "Bailey & Love 28th Ed",
                    title = "Complicated Diverticulitis: Modified Hinchey Stages & Hartmann vs Primary Anastomosis",
                    sortOrder = 8,
                    difficulty = DifficultyLevel.BOARD_ADVANCED,
                    studyStatus = StudyStatus.NEEDS_REVISION,
                    confidenceScore = 2,
                    lastStudiedEpochMillis = nowMillis - 96 * hourMillis,
                    nextRevisionEpochMillis = nowMillis + 12 * hourMillis
                ),
                Topic(
                    id = "topic_phaeo",
                    chapterId = "ch_endocrine_56",
                    bookTitle = "Schwartz 11th Ed",
                    title = "Phaeochromocytoma Perioperative Alpha/Beta Blockade Protocol",
                    sortOrder = 3,
                    difficulty = DifficultyLevel.FELLOWSHIP_VIVA,
                    studyStatus = StudyStatus.NEEDS_REVISION,
                    confidenceScore = 3,
                    lastStudiedEpochMillis = nowMillis - 120 * hourMillis,
                    nextRevisionEpochMillis = nowMillis + 24 * hourMillis
                )
            ),
            pendingFollowUps = listOf(
                FollowUpTask(
                    taskId = "task_101",
                    caseId = "case_hpb_01",
                    pseudonymousCaseId = "SBM-2026-HPB-014",
                    title = "Review Post-ERCP Serum Bilirubin & Alkaline Phosphatase",
                    description = "Confirm biliary decompression prior to index admission laparoscopic cholecystectomy.",
                    dueAtEpochMillis = nowMillis - 2 * hourMillis,
                    status = FollowUpStatus.OVERDUE,
                    priority = TaskPriority.URGENT,
                    createdAtEpochMillis = nowMillis - 24 * hourMillis,
                    updatedAtEpochMillis = nowMillis - 2 * hourMillis
                ),
                FollowUpTask(
                    taskId = "task_102",
                    caseId = "case_crc_04",
                    pseudonymousCaseId = "SBM-2026-CRC-009",
                    title = "Check Histopathology Margin & Lymph Node Yield (pT3N1)",
                    description = "Prepare case summary for Colorectal MDT discussion.",
                    dueAtEpochMillis = nowMillis + 18 * hourMillis,
                    status = FollowUpStatus.PENDING,
                    priority = TaskPriority.HIGH,
                    createdAtEpochMillis = nowMillis - 48 * hourMillis,
                    updatedAtEpochMillis = nowMillis - 12 * hourMillis
                )
            ),
            recentCases = listOf(
                ClinicalCaseSummary(
                    caseId = "case_hpb_01",
                    pseudonymousId = "SBM-2026-HPB-014",
                    category = CaseCategory.PRIVATE_CLINICAL_RECORD,
                    ageRange = "40-49y",
                    chiefComplaint = "RUQ pain, fever, and obstructive jaundice (Charcot's triad)",
                    workingDiagnosis = "Acute Calculous Cholangitis secondary to distal CBD stone",
                    procedureName = "Urgent ERCP + Sphincterotomy & Stenting",
                    status = CaseStatus.AWAITING_RESULTS,
                    latestClinicalUpdate = "POD 1 post-ERCP: afebrile, WBC trending down (14.2 -> 9.8).",
                    nextAction = "Verify LFT normalization and schedule Interval Laparoscopic Cholecystectomy",
                    updatedAtEpochMillis = nowMillis - 2 * hourMillis
                ),
                ClinicalCaseSummary(
                    caseId = "case_edu_02",
                    pseudonymousId = "EDU-TRAUMA-003",
                    category = CaseCategory.EDUCATIONAL_DEIDENTIFIED,
                    ageRange = "20-29y",
                    chiefComplaint = "Blunt abdominal trauma following high-speed MVC",
                    workingDiagnosis = "AAST Grade IV Splenic Laceration with active blush",
                    procedureName = "Angioembolization vs Exploratory Laparotomy teaching case",
                    status = CaseStatus.ACTIVE,
                    latestClinicalUpdate = "Responder to initial 1L crystalloid + 2 units pRBCs.",
                    nextAction = "Complete Trauma Simulation debriefing notes",
                    updatedAtEpochMillis = nowMillis - 8 * hourMillis
                )
            )
        )
    )

    override fun observeDashboardSummary(): Flow<DashboardSummary> = dashboardState.asStateFlow()

    override suspend fun markFollowUpCompleted(taskId: String) {
        val completedTime = System.currentTimeMillis()
        dashboardState.update { current ->
            current.copy(
                pendingFollowUps = current.pendingFollowUps.map { task ->
                    if (task.taskId == taskId) {
                        task.copy(
                            status = FollowUpStatus.COMPLETED,
                            completedAtEpochMillis = completedTime,
                            updatedAtEpochMillis = completedTime
                        )
                    } else {
                        task
                    }
                }.filter { it.status != FollowUpStatus.COMPLETED }
            )
        }
    }

    override suspend fun recordQuickTopicRevision(topicId: String) {
        val updatedTime = System.currentTimeMillis()
        dashboardState.update { current ->
            val updatedWeakTopics = current.weakTopics.map { topic ->
                if (topic.id == topicId) {
                    topic.copy(
                        studyStatus = StudyStatus.COMPLETED,
                        confidenceScore = (topic.confidenceScore + 1).coerceAtMost(5),
                        lastStudiedEpochMillis = updatedTime
                    )
                } else {
                    topic
                }
            }.filter { it.studyStatus == StudyStatus.NEEDS_REVISION }

            current.copy(
                completedTopicsTotal = current.completedTopicsTotal + 1,
                weakTopics = updatedWeakTopics
            )
        }
    }
}

