package com.surgicalboardmaster.app.domain.repository

import com.surgicalboardmaster.app.domain.model.ExamTargetLevel
import com.surgicalboardmaster.app.domain.model.LessonFormat
import com.surgicalboardmaster.app.domain.model.StructuredSurgicalLesson
import com.surgicalboardmaster.app.domain.model.TeachingDepth
import com.surgicalboardmaster.app.domain.model.TeachingLanguage
import kotlinx.coroutines.flow.Flow

interface TutorRepository {
    fun observeSavedLessons(userId: String): Flow<List<StructuredSurgicalLesson>>

    /**
     * Returns an existing cached lesson if one already exists for (userId, topicId, format)
     * unless [forceRegenerate] is true, preventing redundant AI token usage.
     */
    suspend fun loadOrGenerateStructuredLesson(
        userId: String,
        bookId: String,
        bookTitle: String,
        edition: String,
        chapterId: String,
        chapterTitle: String,
        topicId: String,
        topicTitle: String,
        language: TeachingLanguage,
        depth: TeachingDepth,
        format: LessonFormat,
        examLevel: ExamTargetLevel,
        forceRegenerate: Boolean = false
    ): Result<StructuredSurgicalLesson>

    suspend fun toggleSectionBookmark(
        userId: String,
        lessonId: String,
        sectionId: String
    ): Result<StructuredSurgicalLesson>

    suspend fun updateSectionHighlight(
        userId: String,
        lessonId: String,
        sectionId: String,
        highlightText: String
    ): Result<StructuredSurgicalLesson>

    suspend fun savePersonalNotes(
        userId: String,
        lessonId: String,
        personalNotes: String
    ): Result<StructuredSurgicalLesson>

    suspend fun markLessonCompleteAndScheduleRevision(
        userId: String,
        lessonId: String,
        isCompleted: Boolean,
        revisionInDays: Int?
    ): Result<StructuredSurgicalLesson>
}
