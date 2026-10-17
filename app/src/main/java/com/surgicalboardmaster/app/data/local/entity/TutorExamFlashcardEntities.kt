package com.surgicalboardmaster.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.surgicalboardmaster.app.domain.model.ContentOrigin
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.ExamAttempt
import com.surgicalboardmaster.app.domain.model.ExamMode
import com.surgicalboardmaster.app.domain.model.ExamQuestion
import com.surgicalboardmaster.app.domain.model.ExamTargetLevel
import com.surgicalboardmaster.app.domain.model.Flashcard
import com.surgicalboardmaster.app.domain.model.LessonFormat
import com.surgicalboardmaster.app.domain.model.LessonSection
import com.surgicalboardmaster.app.domain.model.LessonSectionType
import com.surgicalboardmaster.app.domain.model.QuestionType
import com.surgicalboardmaster.app.domain.model.StructuredSurgicalLesson
import com.surgicalboardmaster.app.domain.model.TeachingDepth
import com.surgicalboardmaster.app.domain.model.TeachingLanguage
import com.surgicalboardmaster.app.domain.model.TopicPerformanceMetric

@Entity(
    tableName = "structured_lessons",
    indices = [
        Index(value = ["userId", "topicId", "formatName"]),
        Index(value = ["userId", "lastOpenedEpochMillis"])
    ]
)
data class LessonEntity(
    @PrimaryKey val lessonId: String,
    val userId: String,
    val bookId: String,
    val bookTitle: String,
    val edition: String,
    val chapterId: String,
    val chapterTitle: String,
    val topicId: String,
    val topicTitle: String,
    val languageName: String,
    val depthName: String,
    val formatName: String,
    val examLevelName: String,
    val encodedSections: String,
    val personalNotes: String,
    val isCompleted: Boolean,
    val lastOpenedEpochMillis: Long,
    val nextRevisionEpochMillis: Long?,
    val provenanceBanner: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

@Entity(
    tableName = "exam_questions",
    indices = [Index(value = ["topicId"]), Index(value = ["bookId"])]
)
data class ExamQuestionEntity(
    @PrimaryKey val questionId: String,
    val questionText: String,
    val questionTypeName: String,
    val topicId: String,
    val topicTitle: String,
    val bookId: String,
    val difficultyName: String,
    val encodedOptions: String,
    val correctAnswer: String,
    val explanation: String,
    val encodedDistractors: String,
    val sourceReferences: String,
    val contentOriginName: String,
    val createdAtEpochMillis: Long
)

@Entity(
    tableName = "exam_attempts",
    indices = [Index(value = ["userId", "createdAtEpochMillis"])]
)
data class ExamAttemptEntity(
    @PrimaryKey val attemptId: String,
    val userId: String,
    val modeName: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercent: Int,
    val durationSeconds: Int,
    val encodedTopicBreakdown: String,
    val encodedWrongQuestionIds: String,
    val createdAtEpochMillis: Long
)

@Entity(
    tableName = "flashcards",
    indices = [Index(value = ["userId", "dueAtEpochMillis"]), Index(value = ["userId", "topicId"])]
)
data class FlashcardEntity(
    @PrimaryKey val flashcardId: String,
    val userId: String,
    val topicId: String,
    val topicTitle: String,
    val frontPrompt: String,
    val backAnswer: String,
    val clinicalPearl: String,
    val intervalDays: Int,
    val easeFactor: Float,
    val repetitions: Int,
    val dueAtEpochMillis: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

/**
 * Pure-Kotlin deterministic codec so Room entities serialize and deserialize identically
 * in both Android runtime and pure JVM unit tests without mocking android.jar JSON classes.
 */
object StructuredPayloadCodec {
    private const val RECORD_SEP = "\u001E"
    private const val FIELD_SEP = "\u001F"

    private fun escape(raw: String): String =
        raw.replace("\\", "\\\\")
            .replace(RECORD_SEP, "\\R")
            .replace(FIELD_SEP, "\\F")

    private fun unescape(encoded: String): String =
        encoded.replace("\\F", FIELD_SEP)
            .replace("\\R", RECORD_SEP)
            .replace("\\\\", "\\")

    fun encodeSections(sections: List<LessonSection>): String {
        return sections.joinToString(RECORD_SEP) { sec ->
            listOf(
                escape(sec.sectionId),
                escape(sec.type.name),
                escape(sec.bodyContent),
                sec.isVerifiedReferenceFact.toString(),
                sec.isBookmarked.toString(),
                escape(sec.highlightText)
            ).joinToString(FIELD_SEP)
        }
    }

    fun decodeSections(payload: String): List<LessonSection> {
        if (payload.isBlank()) return emptyList()
        return payload.split(RECORD_SEP).mapNotNull { row ->
            val parts = row.split(FIELD_SEP)
            if (parts.size < 6) return@mapNotNull null
            val type = runCatching { LessonSectionType.valueOf(unescape(parts[1])) }
                .getOrDefault(LessonSectionType.SUMMARY)
            LessonSection(
                sectionId = unescape(parts[0]),
                type = type,
                bodyContent = unescape(parts[2]),
                isVerifiedReferenceFact = parts[3].toBoolean(),
                isBookmarked = parts[4].toBoolean(),
                highlightText = unescape(parts[5])
            )
        }
    }

    fun encodeStringList(items: List<String>): String =
        items.joinToString(RECORD_SEP) { escape(it) }

    fun decodeStringList(payload: String): List<String> =
        if (payload.isBlank()) emptyList()
        else payload.split(RECORD_SEP).map { unescape(it) }

    fun encodeStringMap(map: Map<String, String>): String =
        map.entries.joinToString(RECORD_SEP) { (k, v) ->
            "${escape(k)}$FIELD_SEP${escape(v)}"
        }

    fun decodeStringMap(payload: String): Map<String, String> {
        if (payload.isBlank()) return emptyMap()
        return payload.split(RECORD_SEP).mapNotNull { entry ->
            val parts = entry.split(FIELD_SEP)
            if (parts.size < 2) null else unescape(parts[0]) to unescape(parts[1])
        }.toMap()
    }

    fun encodeTopicBreakdown(items: List<TopicPerformanceMetric>): String =
        items.joinToString(RECORD_SEP) { metric ->
            listOf(
                escape(metric.topicId),
                escape(metric.topicTitle),
                metric.attemptedCount.toString(),
                metric.correctCount.toString(),
                metric.accuracyPercent.toString()
            ).joinToString(FIELD_SEP)
        }

    fun decodeTopicBreakdown(payload: String): List<TopicPerformanceMetric> {
        if (payload.isBlank()) return emptyList()
        return payload.split(RECORD_SEP).mapNotNull { row ->
            val parts = row.split(FIELD_SEP)
            if (parts.size < 5) return@mapNotNull null
            TopicPerformanceMetric(
                topicId = unescape(parts[0]),
                topicTitle = unescape(parts[1]),
                attemptedCount = parts[2].toIntOrNull() ?: 0,
                correctCount = parts[3].toIntOrNull() ?: 0,
                accuracyPercent = parts[4].toIntOrNull() ?: 0
            )
        }
    }
}

fun LessonEntity.toDomainModel(): StructuredSurgicalLesson = StructuredSurgicalLesson(
    lessonId = lessonId,
    userId = userId,
    bookId = bookId,
    bookTitle = bookTitle,
    edition = edition,
    chapterId = chapterId,
    chapterTitle = chapterTitle,
    topicId = topicId,
    topicTitle = topicTitle,
    language = runCatching { TeachingLanguage.valueOf(languageName) }
        .getOrDefault(TeachingLanguage.ARABIC_WITH_ENGLISH_TERMS),
    depth = runCatching { TeachingDepth.valueOf(depthName) }
        .getOrDefault(TeachingDepth.BOARD_CANDIDATE),
    format = runCatching { LessonFormat.valueOf(formatName) }
        .getOrDefault(LessonFormat.BOARD_EXAM_PREP),
    examLevel = runCatching { ExamTargetLevel.valueOf(examLevelName) }
        .getOrDefault(ExamTargetLevel.ARAB_IRAQI_BOARD),
    sections = StructuredPayloadCodec.decodeSections(encodedSections),
    personalNotes = personalNotes,
    isCompleted = isCompleted,
    lastOpenedEpochMillis = lastOpenedEpochMillis,
    nextRevisionEpochMillis = nextRevisionEpochMillis,
    provenanceBanner = provenanceBanner,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun StructuredSurgicalLesson.toEntity(): LessonEntity = LessonEntity(
    lessonId = lessonId,
    userId = userId,
    bookId = bookId,
    bookTitle = bookTitle,
    edition = edition,
    chapterId = chapterId,
    chapterTitle = chapterTitle,
    topicId = topicId,
    topicTitle = topicTitle,
    languageName = language.name,
    depthName = depth.name,
    formatName = format.name,
    examLevelName = examLevel.name,
    encodedSections = StructuredPayloadCodec.encodeSections(sections),
    personalNotes = personalNotes,
    isCompleted = isCompleted,
    lastOpenedEpochMillis = lastOpenedEpochMillis,
    nextRevisionEpochMillis = nextRevisionEpochMillis,
    provenanceBanner = provenanceBanner,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun ExamQuestionEntity.toDomainModel(): ExamQuestion = ExamQuestion(
    questionId = questionId,
    questionText = questionText,
    questionType = runCatching { QuestionType.valueOf(questionTypeName) }
        .getOrDefault(QuestionType.SINGLE_BEST_ANSWER_MCQ),
    topicId = topicId,
    topicTitle = topicTitle,
    bookId = bookId,
    difficulty = runCatching { DifficultyLevel.valueOf(difficultyName) }
        .getOrDefault(DifficultyLevel.BOARD_ADVANCED),
    options = StructuredPayloadCodec.decodeStringList(encodedOptions),
    correctAnswer = correctAnswer,
    explanation = explanation,
    distractorExplanations = StructuredPayloadCodec.decodeStringMap(encodedDistractors),
    sourceReferences = sourceReferences,
    contentOrigin = runCatching { ContentOrigin.valueOf(contentOriginName) }
        .getOrDefault(ContentOrigin.ORIGINAL_EDUCATIONAL_SYNTHESIS),
    createdAtEpochMillis = createdAtEpochMillis
)

fun ExamQuestion.toEntity(): ExamQuestionEntity = ExamQuestionEntity(
    questionId = questionId,
    questionText = questionText,
    questionTypeName = questionType.name,
    topicId = topicId,
    topicTitle = topicTitle,
    bookId = bookId,
    difficultyName = difficulty.name,
    encodedOptions = StructuredPayloadCodec.encodeStringList(options),
    correctAnswer = correctAnswer,
    explanation = explanation,
    encodedDistractors = StructuredPayloadCodec.encodeStringMap(distractorExplanations),
    sourceReferences = sourceReferences,
    contentOriginName = contentOrigin.name,
    createdAtEpochMillis = createdAtEpochMillis
)

fun ExamAttemptEntity.toDomainModel(): ExamAttempt = ExamAttempt(
    attemptId = attemptId,
    userId = userId,
    mode = runCatching { ExamMode.valueOf(modeName) }.getOrDefault(ExamMode.PRACTICE_MODE),
    totalQuestions = totalQuestions,
    correctCount = correctCount,
    scorePercent = scorePercent,
    durationSeconds = durationSeconds,
    topicBreakdown = StructuredPayloadCodec.decodeTopicBreakdown(encodedTopicBreakdown),
    wrongQuestionIds = StructuredPayloadCodec.decodeStringList(encodedWrongQuestionIds),
    createdAtEpochMillis = createdAtEpochMillis
)

fun ExamAttempt.toEntity(): ExamAttemptEntity = ExamAttemptEntity(
    attemptId = attemptId,
    userId = userId,
    modeName = mode.name,
    totalQuestions = totalQuestions,
    correctCount = correctCount,
    scorePercent = scorePercent,
    durationSeconds = durationSeconds,
    encodedTopicBreakdown = StructuredPayloadCodec.encodeTopicBreakdown(topicBreakdown),
    encodedWrongQuestionIds = StructuredPayloadCodec.encodeStringList(wrongQuestionIds),
    createdAtEpochMillis = createdAtEpochMillis
)

fun FlashcardEntity.toDomainModel(): Flashcard = Flashcard(
    flashcardId = flashcardId,
    userId = userId,
    topicId = topicId,
    topicTitle = topicTitle,
    frontPrompt = frontPrompt,
    backAnswer = backAnswer,
    clinicalPearl = clinicalPearl,
    intervalDays = intervalDays,
    easeFactor = easeFactor,
    repetitions = repetitions,
    dueAtEpochMillis = dueAtEpochMillis,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun Flashcard.toEntity(): FlashcardEntity = FlashcardEntity(
    flashcardId = flashcardId,
    userId = userId,
    topicId = topicId,
    topicTitle = topicTitle,
    frontPrompt = frontPrompt,
    backAnswer = backAnswer,
    clinicalPearl = clinicalPearl,
    intervalDays = intervalDays,
    easeFactor = easeFactor,
    repetitions = repetitions,
    dueAtEpochMillis = dueAtEpochMillis,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)
