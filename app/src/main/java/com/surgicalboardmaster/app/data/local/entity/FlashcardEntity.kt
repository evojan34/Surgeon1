package com.surgicalboardmaster.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    indices = [
        Index(value = ["topicId"]),
        Index(value = ["nextReviewEpochMillis"])
    ]
)
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val frontPrompt: String,
    val backAnswer: String,
    val surgicalPearls: String,
    val sourceReference: String,
    val repetitionStage: Int = 0,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 1,
    val nextReviewEpochMillis: Long = System.currentTimeMillis(),
    val lastReviewedEpochMillis: Long? = null
)
