package com.surgicalboardmaster.app.domain.model

enum class QuestionType(val displayLabel: String) {
    SINGLE_BEST_ANSWER_MCQ("Single Best Answer MCQ"),
    CASE_BASED_QUESTION("Clinical Case Vignette"),
    DIFFERENTIAL_DIAGNOSIS("Differential Diagnosis Priority"),
    INVESTIGATION_INTERPRETATION("Investigation Interpretation"),
    NEXT_BEST_STEP("Next Best Step in Management"),
    OPERATIVE_DECISION("Intraoperative Decision Making"),
    POSTOP_COMPLICATION("Postoperative Complication Management"),
    SHORT_ANSWER("Structured Short Answer"),
    VIVA_PRACTICE("Oral Examination / Viva Voce")
}

enum class ContentOrigin(val provenanceLabel: String) {
    ORIGINAL_EDUCATIONAL_SYNTHESIS("Original Educational Synthesis (Not a past exam reproduction)"),
    LESSON_DERIVED_PRACTICE("Generated from User's Saved Surgical Lesson"),
    USER_AUTHORED_ITEM("User-Authored Board Revision Item")
}

enum class ExamMode(val displayLabel: String) {
    PRACTICE_MODE("Practice Mode (Immediate Explanation)"),
    TIMED_EXAM_MODE("Timed Board Examination Mode (Secure Key Until Submit)")
}

data class ExamQuestion(
    val questionId: String,
    val questionText: String,
    val questionType: QuestionType,
    val topicId: String,
    val topicTitle: String,
    val bookId: String,
    val difficulty: DifficultyLevel,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val distractorExplanations: Map<String, String>,
    val sourceReferences: String,
    val contentOrigin: ContentOrigin,
    val createdAtEpochMillis: Long
)

data class TopicPerformanceMetric(
    val topicId: String,
    val topicTitle: String,
    val attemptedCount: Int,
    val correctCount: Int,
    val accuracyPercent: Int
)

data class ExamAttempt(
    val attemptId: String,
    val userId: String,
    val mode: ExamMode,
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercent: Int,
    val durationSeconds: Int,
    val topicBreakdown: List<TopicPerformanceMetric>,
    val wrongQuestionIds: List<String>,
    val createdAtEpochMillis: Long
)

enum class SpacedRepetitionGrade(val label: String, val qualityScore: Int) {
    AGAIN("Again (< 24h)", 1),
    HARD("Hard (2-3d)", 3),
    GOOD("Good (Optimal)", 4),
    EASY("Easy (Mastered)", 5)
}

data class Flashcard(
    val flashcardId: String,
    val userId: String,
    val topicId: String,
    val topicTitle: String,
    val frontPrompt: String,
    val backAnswer: String,
    val clinicalPearl: String,
    val intervalDays: Int,
    val easeFactor: Float,
    val repetitions: Int,
    val dueAtEpochMillis: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
