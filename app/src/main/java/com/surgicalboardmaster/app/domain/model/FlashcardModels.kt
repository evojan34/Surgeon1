package com.surgicalboardmaster.app.domain.model

enum class ReviewRating {
    AGAIN, // Fail / Reset interval
    HARD,  // Minimal increment
    GOOD,  // Standard spaced interval
    EASY   // Long spaced interval
}

data class Flashcard(
    val id: String,
    val userId: String,
    val topicId: String,
    val topicTitle: String,
    val frontPrompt: String,
    val backAnswer: String,
    val clinicalPearl: String,
    val repetitionNumber: Int = 0,
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val nextReviewEpochMillis: Long,
    val lastReviewedEpochMillis: Long? = null,
    val createdAtEpochMillis: Long
)
