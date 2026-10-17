package com.surgicalboardmaster.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.surgicalboardmaster.app.domain.model.Book
import com.surgicalboardmaster.app.domain.model.Chapter
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.StudyStatus
import com.surgicalboardmaster.app.domain.model.Topic

@Entity(
    tableName = "books",
    indices = [Index(value = ["userId"])]
)
data class BookEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val edition: String,
    val authorsCsv: String,
    val publicationYear: Int?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isPendingCloudSync: Boolean = false
)

@Entity(
    tableName = "chapters",
    indices = [
        Index(value = ["userId", "bookId"]),
        Index(value = ["bookId", "sortOrder"])
    ]
)
data class ChapterEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val bookId: String,
    val title: String,
    val chapterNumber: Int?,
    val sortOrder: Int,
    val description: String,
    val statusName: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isPendingCloudSync: Boolean = false
)

@Entity(
    tableName = "topics",
    indices = [
        Index(value = ["userId", "chapterId"]),
        Index(value = ["chapterId", "sortOrder"])
    ]
)
data class TopicEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val bookId: String,
    val chapterId: String,
    val bookTitle: String,
    val title: String,
    val sortOrder: Int,
    val difficultyName: String,
    val studyStatusName: String,
    val confidenceScore: Int,
    val studyNotes: String,
    val lastStudiedEpochMillis: Long?,
    val nextRevisionEpochMillis: Long?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isPendingCloudSync: Boolean = false
)

fun BookEntity.toDomainModel(
    completedTopicsCount: Int,
    totalTopicsCount: Int,
    activeChapterTitle: String
): Book = Book(
    id = id,
    title = title,
    edition = edition,
    authors = authorsCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() },
    publicationYear = publicationYear,
    userId = userId,
    completedTopicsCount = completedTopicsCount,
    totalTopicsCount = totalTopicsCount,
    activeChapterTitle = activeChapterTitle,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun ChapterEntity.toDomainModel(): Chapter = Chapter(
    id = id,
    bookId = bookId,
    title = title,
    chapterNumber = chapterNumber,
    sortOrder = sortOrder,
    description = description,
    status = runCatching { StudyStatus.valueOf(statusName) }.getOrDefault(StudyStatus.NOT_STARTED),
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun TopicEntity.toDomainModel(): Topic = Topic(
    id = id,
    chapterId = chapterId,
    bookTitle = bookTitle,
    title = title,
    sortOrder = sortOrder,
    difficulty = runCatching { DifficultyLevel.valueOf(difficultyName) }.getOrDefault(DifficultyLevel.BOARD_ADVANCED),
    studyStatus = runCatching { StudyStatus.valueOf(studyStatusName) }.getOrDefault(StudyStatus.NOT_STARTED),
    confidenceScore = confidenceScore.coerceIn(1, 5),
    lastStudiedEpochMillis = lastStudiedEpochMillis,
    nextRevisionEpochMillis = nextRevisionEpochMillis,
    studyNotes = studyNotes
)
