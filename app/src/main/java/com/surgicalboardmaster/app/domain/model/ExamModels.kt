package com.surgicalboardmaster.app.domain.model

enum class QuestionType(val displayLabel: String) {
    SINGLE_BEST_ANSWER("Single Best Answer (SBA)"),
    CASE_BASED("Case-Based Clinical Vignette"),
    NEXT_BEST_STEP("Next Best Step in Management"),
    OPERATIVE_DECISION("Intraoperative Decision"),
    POSTOP_COMPLICATION("Postoperative Complication Management"),
    VIVA_PRACTICE("Viva & Oral Examination Practice")
}

data class ExamOption(
    val id: String, // "A", "B", "C", "D", "E"
    val text: String,
    val distractorRationale: String
)

data class ExamQuestion(
    val id: String,
    val topicId: String,
    val topicTitle: String,
    val bookReference: String, // "Bailey & Love 28th Ed, Ch 67"
    val questionType: QuestionType,
    val difficulty: DifficultyLevel,
    val clinicalVignette: String,
    val questionPrompt: String,
    val options: List<ExamOption>,
    val correctAnswerId: String, // "A", "B", "C", "D", "E"
    val comprehensiveExplanation: String,
    val boardPearls: String,
    val verifiedSourceProvenance: String
)

data class QuestionSubmission(
    val questionId: String,
    val selectedOptionId: String,
    val isCorrect: Boolean,
    val timeSpentSeconds: Int
)

data class ExamAttempt(
    val id: String,
    val userId: String,
    val examTitle: String,
    val isTimedMode: Boolean,
    val timeLimitMinutes: Int,
    val totalQuestionsCount: Int,
    val correctCount: Int,
    val scorePercentage: Int,
    val submissions: List<QuestionSubmission>,
    val completedAtEpochMillis: Long
)

data class WrongAnswerItem(
    val question: ExamQuestion,
    val selectedOptionId: String,
    val failureCount: Int,
    val lastAttemptEpochMillis: Long
)
