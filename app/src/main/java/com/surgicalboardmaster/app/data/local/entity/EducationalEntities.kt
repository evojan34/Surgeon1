package com.surgicalboardmaster.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.ExamOption
import com.surgicalboardmaster.app.domain.model.ExamQuestion
import com.surgicalboardmaster.app.domain.model.Flashcard
import com.surgicalboardmaster.app.domain.model.LessonFormat
import com.surgicalboardmaster.app.domain.model.LessonSection
import com.surgicalboardmaster.app.domain.model.QuestionType
import com.surgicalboardmaster.app.domain.model.StructuredLesson

@Entity(
    tableName = "lessons",
    indices = [
        Index(value = ["userId", "topicId"]),
        Index(value = ["userId", "updatedAtEpochMillis"])
    ]
)
data class LessonEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val bookId: String,
    val bookTitle: String,
    val chapterId: String,
    val chapterTitle: String,
    val topicId: String,
    val topicTitle: String,
    val formatName: String,
    val language: String,
    val teachingDepth: String,
    val learningObjectivesCsv: String,
    val relevantAnatomy: String,
    val pathophysiology: String,
    val clinicalPresentation: String,
    val investigationsAndInterpretation: String,
    val managementAlgorithms: String,
    val operativePrinciples: String,
    val complications: String,
    val boardPearlsCsv: String,
    val commonMistakesCsv: String,
    val summary: String,
    val referencesCsv: String,
    val isBookmarked: Boolean,
    val userPersonalNotes: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isPendingCloudSync: Boolean = false
)

@Entity(
    tableName = "exam_questions",
    indices = [
        Index(value = ["topicId"]),
        Index(value = ["difficultyName"])
    ]
)
data class ExamQuestionEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val topicTitle: String,
    val bookReference: String,
    val questionTypeName: String,
    val difficultyName: String,
    val clinicalVignette: String,
    val questionPrompt: String,
    val optionsJson: String,
    val correctAnswerId: String,
    val comprehensiveExplanation: String,
    val boardPearls: String,
    val verifiedSourceProvenance: String
)

@Entity(
    tableName = "exam_attempts",
    indices = [
        Index(value = ["userId", "completedAtEpochMillis"])
    ]
)
data class ExamAttemptEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val examTitle: String,
    val isTimedMode: Boolean,
    val timeLimitMinutes: Int,
    val totalQuestionsCount: Int,
    val correctCount: Int,
    val scorePercentage: Int,
    val submissionsJson: String,
    val completedAtEpochMillis: Long,
    val isPendingCloudSync: Boolean = false
)

@Entity(
    tableName = "flashcards",
    indices = [
        Index(value = ["userId", "nextReviewEpochMillis"]),
        Index(value = ["userId", "topicId"])
    ]
)
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val topicId: String,
    val topicTitle: String,
    val frontPrompt: String,
    val backAnswer: String,
    val clinicalPearl: String,
    val repetitionNumber: Int,
    val intervalDays: Int,
    val easeFactor: Float,
    val nextReviewEpochMillis: Long,
    val lastReviewedEpochMillis: Long?,
    val createdAtEpochMillis: Long,
    val isPendingCloudSync: Boolean = false
)

fun LessonEntity.toDomainModel(): StructuredLesson = StructuredLesson(
    id = id,
    userId = userId,
    bookId = bookId,
    bookTitle = bookTitle,
    chapterId = chapterId,
    chapterTitle = chapterTitle,
    topicId = topicId,
    topicTitle = topicTitle,
    format = runCatching { LessonFormat.valueOf(formatName) }.getOrDefault(LessonFormat.BOARD_EXAM_PREP),
    language = language,
    teachingDepth = teachingDepth,
    learningObjectives = learningObjectivesCsv.split(";;;").filter { it.isNotBlank() },
    relevantAnatomy = relevantAnatomy,
    pathophysiology = pathophysiology,
    clinicalPresentation = clinicalPresentation,
    investigationsAndInterpretation = investigationsAndInterpretation,
    managementAlgorithms = managementAlgorithms,
    operativePrinciples = operativePrinciples,
    complications = complications,
    boardPearls = boardPearlsCsv.split(";;;").filter { it.isNotBlank() },
    commonMistakes = commonMistakesCsv.split(";;;").filter { it.isNotBlank() },
    summary = summary,
    references = referencesCsv.split(";;;").filter { it.isNotBlank() },
    isBookmarked = isBookmarked,
    userPersonalNotes = userPersonalNotes,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun FlashcardEntity.toDomainModel(): Flashcard = Flashcard(
    id = id,
    userId = userId,
    topicId = topicId,
    topicTitle = topicTitle,
    frontPrompt = frontPrompt,
    backAnswer = backAnswer,
    clinicalPearl = clinicalPearl,
    repetitionNumber = repetitionNumber,
    intervalDays = intervalDays,
    easeFactor = easeFactor,
    nextReviewEpochMillis = nextReviewEpochMillis,
    lastReviewedEpochMillis = lastReviewedEpochMillis,
    createdAtEpochMillis = createdAtEpochMillis
)
