package com.surgicalboardmaster.app.domain.repository

import com.surgicalboardmaster.app.domain.model.LessonConfig
import com.surgicalboardmaster.app.domain.model.StructuredLesson
import kotlinx.coroutines.flow.Flow

interface LessonRepository {
    fun observeSavedLessons(userId: String): Flow<List<StructuredLesson>>
    suspend fun getLessonById(lessonId: String, userId: String): StructuredLesson?
    suspend fun generateOrFetchStructuredLesson(userId: String, topicId: String, config: LessonConfig): Result<StructuredLesson>
    suspend fun toggleBookmark(lessonId: String, userId: String, isBookmarked: Boolean): Result<Unit>
    suspend fun savePersonalNotes(lessonId: String, userId: String, notes: String): Result<Unit>
}
