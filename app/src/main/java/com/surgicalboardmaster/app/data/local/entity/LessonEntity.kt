package com.surgicalboardmaster.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey
    val id: String,
    val topicId: String,
    val title: String,
    val contentMarkdown: String,
    val surgicalPearlsMarkdown: String,
    val boardPitfallsMarkdown: String,
    val baileyLoveReferenceSection: String,
    val schwartzReferenceSection: String,
    val estimatedMinutes: Int,
    val isBookmarked: Boolean = false
)
