package com.surgicalboardmaster.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.surgicalboardmaster.app.data.local.entity.ExamAttemptEntity
import com.surgicalboardmaster.app.data.local.entity.FlashcardEntity
import com.surgicalboardmaster.app.data.local.entity.SavedLessonEntity
import com.surgicalboardmaster.app.data.local.entity.StudyAnnotationEntity
import com.surgicalboardmaster.app.data.local.entity.WrongAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TutorAndExamDao {

    @Query("SELECT * FROM saved_lessons WHERE userId = :userId ORDER BY updatedAtEpochMillis DESC")
    fun observeSavedLessonsForUser(userId: String): Flow<List<SavedLessonEntity>>

    @Query(
        """
        SELECT * FROM saved_lessons 
        WHERE userId = :userId 
          AND topicId = :topicId 
          AND lessonFormatName = :formatName 
          AND teachingDepthName = :depthName 
          AND languageName = :languageName 
        LIMIT 1
        """
    )
    suspend fun findCachedLesson(
        userId: String,
        topicId: String,
        formatName: String,
        depthName: String,
        languageName: String
    ): SavedLessonEntity?

    @Query("SELECT * FROM saved_lessons WHERE lessonId = :lessonId AND userId = :userId LIMIT 1")
    suspend fun getLessonById(lessonId: String, userId: String): SavedLessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLesson(lesson: SavedLessonEntity)

    @Query("SELECT * FROM study_annotations WHERE userId = :userId ORDER BY createdAtEpochMillis DESC")
    fun observeAnnotationsForUser(userId: String): Flow<List<StudyAnnotationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: StudyAnnotationEntity)

    @Query("DELETE FROM study_annotations WHERE annotationId = :annotationId AND userId = :userId")
    suspend fun deleteAnnotation(annotationId: String, userId: String)

    @Query("SELECT * FROM flashcards WHERE userId = :userId ORDER BY dueAtEpochMillis ASC")
    fun observeFlashcardsForUser(userId: String): Flow<List<FlashcardEntity>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE userId = :userId")
    suspend fun countFlashcardsForUser(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFlashcard(flashcard: FlashcardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFlashcards(flashcards: List<FlashcardEntity>)

    @Query("SELECT * FROM exam_attempts WHERE userId = :userId ORDER BY completedAtEpochMillis DESC")
    fun observeExamAttemptsForUser(userId: String): Flow<List<ExamAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamAttempt(attempt: ExamAttemptEntity)

    @Query("SELECT * FROM wrong_answers WHERE userId = :userId ORDER BY lastMissedEpochMillis DESC")
    fun observeWrongAnswersForUser(userId: String): Flow<List<WrongAnswerEntity>>

    @Query("SELECT * FROM wrong_answers WHERE userId = :userId AND questionId = :questionId LIMIT 1")
    suspend fun findWrongAnswer(userId: String, questionId: String): WrongAnswerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWrongAnswer(entry: WrongAnswerEntity)

    @Query("DELETE FROM wrong_answers WHERE userId = :userId AND questionId = :questionId")
    suspend fun deleteWrongAnswerByQuestionId(userId: String, questionId: String)
}
