package com.surgicalboardmaster.app.domain.repository

import com.surgicalboardmaster.app.domain.model.Book
import com.surgicalboardmaster.app.domain.model.Chapter
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.StudyStatus
import com.surgicalboardmaster.app.domain.model.Topic
import kotlinx.coroutines.flow.Flow

data class LibrarySnapshot(
    val userId: String,
    val books: List<Book>,
    val chaptersByBookId: Map<String, List<Chapter>>,
    val topicsByChapterId: Map<String, List<Topic>>,
    val personalizedTopicSequence: List<Topic>
)

interface SurgicalLibraryRepository {
    fun observeLibrarySnapshot(userId: String): Flow<LibrarySnapshot>

    suspend fun ensureDefaultReferenceBooksSeeded(userId: String)

    suspend fun saveBook(
        userId: String,
        bookId: String? = null,
        title: String,
        edition: String,
        authors: List<String>,
        publicationYear: Int?
    ): Result<Book>

    suspend fun deleteBook(userId: String, bookId: String): Result<Unit>

    suspend fun saveChapter(
        userId: String,
        chapterId: String? = null,
        bookId: String,
        title: String,
        chapterNumber: Int?,
        description: String
    ): Result<Chapter>

    suspend fun reorderChapter(
        userId: String,
        bookId: String,
        chapterId: String,
        moveUp: Boolean
    ): Result<Unit>

    suspend fun deleteChapter(userId: String, chapterId: String): Result<Unit>

    suspend fun saveTopic(
        userId: String,
        topicId: String? = null,
        bookId: String,
        chapterId: String,
        title: String,
        difficulty: DifficultyLevel,
        studyStatus: StudyStatus,
        confidenceScore: Int,
        studyNotes: String
    ): Result<Topic>

    suspend fun updateTopicProgress(
        userId: String,
        topicId: String,
        studyStatus: StudyStatus,
        confidenceScore: Int,
        studyNotes: String,
        scheduleRevisionInDays: Int?
    ): Result<Topic>

    suspend fun reorderTopic(
        userId: String,
        chapterId: String,
        topicId: String,
        moveUp: Boolean
    ): Result<Unit>

    suspend fun deleteTopic(userId: String, topicId: String): Result<Unit>
}
