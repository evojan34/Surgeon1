package com.surgicalboardmaster.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.surgicalboardmaster.app.data.local.entity.ExamAttemptEntity
import com.surgicalboardmaster.app.data.local.entity.ExamQuestionEntity
import com.surgicalboardmaster.app.data.local.entity.FlashcardEntity
import com.surgicalboardmaster.app.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TutorExamFlashcardDao {

    // Structured Lessons
    @Query("SELECT * FROM structured_lessons WHERE userId = :userId ORDER BY lastOpenedEpochMillis DESC")
    fun observeLessonsForUser(userId: String): Flow<List<LessonEntity>>

    @Query(
        """
        SELECT * FROM structured_lessons
        WHERE userId = :userId AND topicId = :topicId AND formatName = :formatName
        LIMIT 1
        """
    )
    suspend fun findCachedLesson(
        userId: String,
        topicId: String,
        formatName: String
    ): LessonEntity?

    @Query("SELECT * FROM structured_lessons WHERE lessonId = :lessonId AND userId = :userId LIMIT 1")
    suspend fun getLessonById(lessonId: String, userId: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLesson(lesson: LessonEntity)

    // Exam Questions
    @Query("SELECT * FROM exam_questions ORDER BY createdAtEpochMillis ASC")
    fun observeAllQuestions(): Flow<List<ExamQuestionEntity>>

    @Query("SELECT COUNT(*) FROM exam_questions")
    suspend fun countQuestions(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestions(questions: List<ExamQuestionEntity>)

    // Exam Attempts
    @Query("SELECT * FROM exam_attempts WHERE userId = :userId ORDER BY createdAtEpochMillis DESC")
    fun observeExamAttemptsForUser(userId: String): Flow<List<ExamAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamAttempt(attempt: ExamAttemptEntity)

    // Spaced-Repetition Flashcards
    @Query("SELECT * FROM flashcards WHERE userId = :userId ORDER BY dueAtEpochMillis ASC")
    fun observeFlashcardsForUser(userId: String): Flow<List<FlashcardEntity>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE userId = :userId")
    suspend fun countFlashcardsForUser(userId: String): Int

    @Query("SELECT * FROM flashcards WHERE flashcardId = :flashcardId AND userId = :userId LIMIT 1")
    suspend fun getFlashcardById(flashcardId: String, userId: String): FlashcardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFlashcard(flashcard: FlashcardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFlashcards(flashcards: List<FlashcardEntity>)
}
