package com.surgicalboardmaster.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.surgicalboardmaster.app.data.local.entity.FlashcardEntity
import com.surgicalboardmaster.app.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EducationalLessonDao {
    @Query("SELECT * FROM lessons WHERE topicId = :topicId")
    fun observeLessonsForTopic(topicId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
    suspend fun getLessonById(lessonId: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Query("UPDATE lessons SET isBookmarked = :bookmarked WHERE id = :lessonId")
    suspend fun setBookmark(lessonId: String, bookmarked: Boolean)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE topicId = :topicId")
    fun observeFlashcardsForTopic(topicId: String): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFlashcards(flashcards: List<FlashcardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFlashcard(flashcard: FlashcardEntity)
}
