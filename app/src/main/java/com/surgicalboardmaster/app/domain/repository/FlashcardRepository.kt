package com.surgicalboardmaster.app.domain.repository

import com.surgicalboardmaster.app.domain.model.Flashcard
import com.surgicalboardmaster.app.domain.model.ReviewRating
import kotlinx.coroutines.flow.Flow

interface FlashcardRepository {
    fun observeAllFlashcards(userId: String): Flow<List<Flashcard>>
    suspend fun getDueFlashcards(userId: String): List<Flashcard>
    suspend fun reviewFlashcard(userId: String, cardId: String, rating: ReviewRating): Result<Flashcard>
    suspend fun createFlashcard(userId: String, topicId: String, topicTitle: String, front: String, back: String, pearl: String): Result<Flashcard>
    suspend fun generateFlashcardsFromLesson(userId: String, topicId: String, topicTitle: String, pearls: List<String>): Result<Int>
    suspend fun ensureDefaultFlashcardsSeeded(userId: String)
}
