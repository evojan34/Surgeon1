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
interface EducationalLessonDao {

    @Query("SELECT * FROM lessons WHERE userId = :userId ORDER BY updatedAtEpochMillis DESC")
    fun observeSavedLessons(userId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId AND userId = :userId LIMIT 1")
    suspend fun getLessonById(lessonId: String, userId: String): LessonEntity?

    @Query("SELECT * FROM lessons WHERE topicId = :topicId AND userId = :userId LIMIT 1")
    suspend fun getLessonByTopicId(topicId: String, userId: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLesson(lesson: LessonEntity)

    @Query("UPDATE lessons SET isBookmarked = :isBookmarked, updatedAtEpochMillis = :updatedAt WHERE id = :lessonId AND userId = :userId")
    suspend fun updateBookmark(lessonId: String, userId: String, isBookmarked: Boolean, updatedAt: Long)

    @Query("UPDATE lessons SET userPersonalNotes = :notes, updatedAtEpochMillis = :updatedAt WHERE id = :lessonId AND userId = :userId")
    suspend fun updatePersonalNotes(lessonId: String, userId: String, notes: String, updatedAt: Long)

    @Query("DELETE FROM lessons WHERE id = :lessonId AND userId = :userId")
    suspend fun deleteLesson(lessonId: String, userId: String)
}

@Dao
interface ExamDao {

    @Query("SELECT * FROM exam_questions")
    fun observeAllQuestions(): Flow<List<ExamQuestionEntity>>

    @Query("SELECT * FROM exam_questions WHERE topicId = :topicId")
    suspend fun getQuestionsForTopic(topicId: String): List<ExamQuestionEntity>

    @Query("SELECT COUNT(*) FROM exam_questions")
    suspend fun countQuestions(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<ExamQuestionEntity>)

    @Query("SELECT * FROM exam_attempts WHERE userId = :userId ORDER BY completedAtEpochMillis DESC")
    fun observeExamAttempts(userId: String): Flow<List<ExamAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamAttempt(attempt: ExamAttemptEntity)
}

@Dao
interface FlashcardDao {

    @Query("SELECT * FROM flashcards WHERE userId = :userId ORDER BY nextReviewEpochMillis ASC")
    fun observeFlashcards(userId: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE userId = :userId AND nextReviewEpochMillis <= :nowMillis ORDER BY nextReviewEpochMillis ASC")
    suspend fun getDueFlashcards(userId: String, nowMillis: Long): List<FlashcardEntity>

    @Query("SELECT COUNT(*) FROM flashcards WHERE userId = :userId")
    suspend fun countFlashcards(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFlashcards(cards: List<FlashcardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFlashcard(card: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :cardId AND userId = :userId")
    suspend fun deleteFlashcard(cardId: String, userId: String)
}
