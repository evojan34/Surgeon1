package com.surgicalboardmaster.app.domain.model

enum class StudyStatus(val displayLabel: String) {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    NEEDS_REVISION("Needs Revision")
}

enum class DifficultyLevel(val displayLabel: String) {
    FOUNDATION("Foundation"),
    INTERMEDIATE("Intermediate"),
    BOARD_ADVANCED("Board Advanced"),
    FELLOWSHIP_VIVA("Fellowship / Viva")
}

enum class LessonFormat(val displayLabel: String) {
    FOUNDATION_REVIEW("Foundation Review"),
    DETAILED_SURGICAL_TEACHING("Detailed Surgical Teaching"),
    BOARD_EXAM_PREP("Board Examination Preparation"),
    CLINICAL_APPROACH("Clinical Approach"),
    OPERATIVE_PRINCIPLES("Operative Principles"),
    HIGH_YIELD_REVISION("High-Yield Revision"),
    ORAL_EXAM_VIVA("Oral Examination & Viva"),
    RAPID_REVISION("Rapid Revision")
}

data class Book(
    val id: String,
    val title: String,
    val edition: String,
    val authors: List<String>,
    val publicationYear: Int?,
    val userId: String,
    val completedTopicsCount: Int,
    val totalTopicsCount: Int,
    val activeChapterTitle: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

data class Chapter(
    val id: String,
    val bookId: String,
    val title: String,
    val chapterNumber: Int?,
    val sortOrder: Int,
    val description: String,
    val status: StudyStatus,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

data class Topic(
    val id: String,
    val chapterId: String,
    val bookTitle: String,
    val title: String,
    val sortOrder: Int,
    val difficulty: DifficultyLevel,
    val studyStatus: StudyStatus,
    val confidenceScore: Int, // 1..5
    val lastStudiedEpochMillis: Long?,
    val nextRevisionEpochMillis: Long?,
    val studyNotes: String = ""
)

enum class CaseCategory {
    EDUCATIONAL_DEIDENTIFIED,
    PRIVATE_CLINICAL_RECORD
}

enum class CaseStatus(val displayLabel: String) {
    DRAFT("Draft"),
    ACTIVE("Active"),
    AWAITING_RESULTS("Awaiting Results"),
    POSTOPERATIVE("Postoperative"),
    FOLLOW_UP_DUE("Follow-up Due"),
    COMPLETED("Completed")
}

enum class CaseEventType {
    CLINICAL_ASSESSMENT,
    SYMPTOM_CHANGE,
    LAB_RESULT,
    IMAGING_FINDING,
    PROCEDURE,
    OPERATION,
    TREATMENT_CHANGE,
    POSTOP_PROGRESS,
    DISCHARGE,
    FOLLOW_UP_VISIT,
    AMENDMENT
}

/**
 * Append-only clinical timeline event.
 * If an event corrects a previous entry, [eventType] is [CaseEventType.AMENDMENT]
 * and [correctsEventId] references the original immutable event ID.
 */
data class CaseTimelineEvent(
    val eventId: String,
    val caseId: String,
    val eventType: CaseEventType,
    val summary: String,
    val clinicalDetails: String,
    val recordedAtEpochMillis: Long,
    val correctsEventId: String? = null
)

data class ClinicalCaseSummary(
    val caseId: String,
    val pseudonymousId: String,
    val category: CaseCategory,
    val ageRange: String,
    val chiefComplaint: String,
    val workingDiagnosis: String,
    val procedureName: String?,
    val status: CaseStatus,
    val latestClinicalUpdate: String,
    val nextAction: String,
    val updatedAtEpochMillis: Long
)

enum class TaskPriority(val displayLabel: String) {
    ROUTINE("Routine"),
    HIGH("High"),
    URGENT("Urgent")
}

enum class FollowUpStatus {
    PENDING,
    COMPLETED,
    OVERDUE,
    CANCELLED
}

data class FollowUpTask(
    val taskId: String,
    val caseId: String,
    val pseudonymousCaseId: String,
    val title: String,
    val description: String,
    val dueAtEpochMillis: Long,
    val status: FollowUpStatus,
    val priority: TaskPriority,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val completedAtEpochMillis: Long? = null
)

data class DashboardSummary(
    val candidateTitle: String,
    val books: List<Book>,
    val activeLessonTitle: String,
    val activeLessonBook: String,
    val completedTopicsTotal: Int,
    val totalTopicsCount: Int,
    val averageExamScorePercent: Int,
    val completedSimulationsCount: Int,
    val activeClinicalCasesCount: Int,
    val weakTopics: List<Topic>,
    val pendingFollowUps: List<FollowUpTask>,
    val recentCases: List<ClinicalCaseSummary>
)
