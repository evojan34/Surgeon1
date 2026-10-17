package com.surgicalboardmaster.app.domain.repository

import com.surgicalboardmaster.app.domain.model.AnnotationKind
import com.surgicalboardmaster.app.domain.model.ExaminationLevel
import com.surgicalboardmaster.app.domain.model.LessonFormat
import com.surgicalboardmaster.app.domain.model.LessonSectionKey
import com.surgicalboardmaster.app.domain.model.StructuredSurgicalLesson
import com.surgicalboardmaster.app.domain.model.StudyAnnotation
import com.surgicalboardmaster.app.domain.model.TeachingDepth
import com.surgicalboardmaster.app.domain.model.TeachingLanguage
import kotlinx.coroutines.flow.Flow

data class LessonGenerationResult(
    val lesson: StructuredSurgicalLesson,
    val wasLoadedFromCache: Boolean
)

interface TutorRepository {
    fun observeSavedLessons(userId: String): Flow<List<StructuredSurgicalLesson>>
    fun observeAnnotations(userId: String): Flow<List<StudyAnnotation>>

    suspend fun getOrCreateStructuredLesson(
        userId: String,
        bookId: String,
        bookTitle: String,
        edition: String,
        chapterId: String,
        chapterTitle: String,
        topicId: String,
        topicTitle: String,
        language: TeachingLanguage,
        teachingDepth: TeachingDepth,
        lessonFormat: LessonFormat,
        examinationLevel: ExaminationLevel,
        forceRegenerate: Boolean = false
    ): Result<LessonGenerationResult>

    suspend fun saveReadingPosition(
        userId: String,
        lessonId: String,
        sectionOrder: Int
    ): Result<StructuredSurgicalLesson>

    suspend fun updatePersonalNotesAndHighlights(
        userId: String,
        lessonId: String,
        personalNotes: String,
        newHighlightQuote: String? = null
    ): Result<StructuredSurgicalLesson>

    suspend fun markLessonCompleteAndScheduleRevision(
        userId: String,
        lessonId: String,
        revisionInDays: Int
    ): Result<StructuredSurgicalLesson>

    suspend fun addAnnotation(
        userId: String,
        lessonId: String,
        topicId: String,
        topicTitle: String,
        sectionKey: LessonSectionKey,
        kind: AnnotationKind,
        textContent: String
    ): Result<StudyAnnotation>

    suspend fun deleteAnnotation(userId: String, annotationId: String): Result<Unit>
}
