package com.surgicalboardmaster.app.data.repository

import com.surgicalboardmaster.app.data.local.dao.FlashcardDao
import com.surgicalboardmaster.app.data.local.entity.FlashcardEntity
import com.surgicalboardmaster.app.data.local.entity.toDomainModel
import com.surgicalboardmaster.app.domain.model.Flashcard
import com.surgicalboardmaster.app.domain.model.ReviewRating
import com.surgicalboardmaster.app.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstFlashcardRepository @Inject constructor(
    private val flashcardDao: FlashcardDao
) : FlashcardRepository {

    override fun observeAllFlashcards(userId: String): Flow<List<Flashcard>> {
        return flashcardDao.observeFlashcards(userId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun getDueFlashcards(userId: String): List<Flashcard> {
        val now = System.currentTimeMillis()
        return flashcardDao.getDueFlashcards(userId, now).map { it.toDomainModel() }
    }

    override suspend fun reviewFlashcard(
        userId: String,
        cardId: String,
        rating: ReviewRating
    ): Result<Flashcard> {
        val now = System.currentTimeMillis()
        val cards = flashcardDao.getDueFlashcards(userId, Long.MAX_VALUE)
        val entity = cards.find { it.id == cardId }
            ?: return Result.failure(IllegalArgumentException("Flashcard not found"))

        // Standard SuperMemo SM-2 Interval Calculation
        var ease = entity.easeFactor
        var reps = entity.repetitionNumber
        val nextIntervalDays: Int

        when (rating) {
            ReviewRating.AGAIN -> {
                reps = 0
                nextIntervalDays = 1
                ease = (ease - 0.2f).coerceAtLeast(1.3f)
            }
            ReviewRating.HARD -> {
                reps += 1
                nextIntervalDays = (entity.intervalDays * 1.2f).toInt().coerceAtLeast(2)
                ease = (ease - 0.15f).coerceAtLeast(1.3f)
            }
            ReviewRating.GOOD -> {
                reps += 1
                nextIntervalDays = if (reps == 1) 1 else if (reps == 2) 6 else (entity.intervalDays * ease).toInt()
            }
            ReviewRating.EASY -> {
                reps += 1
                ease = (ease + 0.15f).coerceAtMost(2.8f)
                nextIntervalDays = if (reps == 1) 4 else (entity.intervalDays * ease * 1.3f).toInt()
            }
        }

        val dayMillis = 86_400_000L
        val updated = entity.copy(
            repetitionNumber = reps,
            intervalDays = nextIntervalDays,
            easeFactor = ease,
            nextReviewEpochMillis = now + nextIntervalDays * dayMillis,
            lastReviewedEpochMillis = now,
            isPendingCloudSync = true
        )

        flashcardDao.upsertFlashcard(updated)
        return Result.success(updated.toDomainModel())
    }

    override suspend fun createFlashcard(
        userId: String,
        topicId: String,
        topicTitle: String,
        front: String,
        back: String,
        pearl: String
    ): Result<Flashcard> {
        val now = System.currentTimeMillis()
        val card = FlashcardEntity(
            id = "fc_${UUID.randomUUID()}",
            userId = userId,
            topicId = topicId,
            topicTitle = topicTitle,
            frontPrompt = front.trim(),
            backAnswer = back.trim(),
            clinicalPearl = pearl.trim(),
            repetitionNumber = 0,
            intervalDays = 1,
            easeFactor = 2.5f,
            nextReviewEpochMillis = now,
            lastReviewedEpochMillis = null,
            createdAtEpochMillis = now,
            isPendingCloudSync = true
        )
        flashcardDao.upsertFlashcard(card)
        return Result.success(card.toDomainModel())
    }

    override suspend fun generateFlashcardsFromLesson(
        userId: String,
        topicId: String,
        topicTitle: String,
        pearls: List<String>
    ): Result<Int> {
        val now = System.currentTimeMillis()
        val cards = pearls.mapIndexed { idx, pearl ->
            FlashcardEntity(
                id = "fc_${UUID.randomUUID()}",
                userId = userId,
                topicId = topicId,
                topicTitle = topicTitle,
                frontPrompt = "Board Recall #${idx + 1}: $topicTitle High-Yield Principle",
                backAnswer = pearl,
                clinicalPearl = "Bailey & Love 28th Ed / Schwartz 11th Ed Core Knowledge",
                repetitionNumber = 0,
                intervalDays = 1,
                easeFactor = 2.5f,
                nextReviewEpochMillis = now,
                lastReviewedEpochMillis = null,
                createdAtEpochMillis = now,
                isPendingCloudSync = true
            )
        }
        flashcardDao.upsertFlashcards(cards)
        return Result.success(cards.size)
    }

    override suspend fun ensureDefaultFlashcardsSeeded(userId: String) {
        if (flashcardDao.countFlashcards(userId) > 0) return
        val now = System.currentTimeMillis()
        val defaultCards = listOf(
            FlashcardEntity(
                id = "fc_seed_01_$userId",
                userId = userId,
                topicId = "topic_mirizzi",
                topicTitle = "Mirizzi Syndrome",
                frontPrompt = "What is the defining anatomical difference between Mirizzi Syndrome Csendes Type I and Type II?",
                backAnswer = "Type I is purely extrinsic compression of the Common Hepatic Duct without fistula. Type II features a cholecystobiliary fistula involving up to 33% (1/3) of the duct circumference.",
                clinicalPearl = "Always dissect fundus-first in severe Mirizzi to avoid mistaking the CHD for the cystic duct.",
                repetitionNumber = 0,
                intervalDays = 1,
                easeFactor = 2.5f,
                nextReviewEpochMillis = now,
                lastReviewedEpochMillis = null,
                createdAtEpochMillis = now
            ),
            FlashcardEntity(
                id = "fc_seed_02_$userId",
                userId = userId,
                topicId = "top_calot",
                topicTitle = "Critical View of Safety (Strasberg)",
                frontPrompt = "Name the THREE mandatory criteria required to confirm the Strasberg Critical View of Safety during Laparoscopic Cholecystectomy.",
                backAnswer = "1. Clear the hepatocystic triangle of all fat and fibrous tissue.\n2. Separate the lower third of the gallbladder from the cystic plate.\n3. Verify exactly two structures (and only two) entering the gallbladder.",
                clinicalPearl = "CVS must be confirmed from both anterior AND posterior (sulcus of Rouviere) views before applying any clips.",
                repetitionNumber = 0,
                intervalDays = 1,
                easeFactor = 2.5f,
                nextReviewEpochMillis = now,
                lastReviewedEpochMillis = null,
                createdAtEpochMillis = now
            )
        )
        flashcardDao.upsertFlashcards(defaultCards)
    }
}
