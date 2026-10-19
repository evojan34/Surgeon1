package com.surgicalboardmaster.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    indices = [
        Index(value = ["topicId"]),
        Index(value = ["isBookmarked"])
    ]
)
data class LessonEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String,
    val contentMarkdown: String,
    val surgicalPearlsMarkdown: String,
    val boardPitfallsMarkdown: String,
    val baileyLoveReferenceSection: String,
    val schwartzReferenceSection: String,
    val estimatedMinutes: Int,
    val isBookmarked: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAtEpochMillis: Long? = null,
    val nextReviewEpochMillis: Long? = null,
    val repetitionIntervalDays: Int = 1,
    val easeFactor: Float = 2.5f
)
