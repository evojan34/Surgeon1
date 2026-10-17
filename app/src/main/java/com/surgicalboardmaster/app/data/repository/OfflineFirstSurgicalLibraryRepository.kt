package com.surgicalboardmaster.app.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.surgicalboardmaster.app.data.local.dao.SurgicalLibraryDao
import com.surgicalboardmaster.app.data.local.entity.BookEntity
import com.surgicalboardmaster.app.data.local.entity.ChapterEntity
import com.surgicalboardmaster.app.data.local.entity.TopicEntity
import com.surgicalboardmaster.app.data.local.entity.toDomainModel
import com.surgicalboardmaster.app.domain.model.Book
import com.surgicalboardmaster.app.domain.model.Chapter
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.StudyStatus
import com.surgicalboardmaster.app.domain.model.Topic
import com.surgicalboardmaster.app.domain.repository.LibrarySnapshot
import com.surgicalboardmaster.app.domain.repository.SurgicalLibraryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstSurgicalLibraryRepository @Inject constructor(
    private val libraryDao: SurgicalLibraryDao,
    @ApplicationContext private val context: Context
) : SurgicalLibraryRepository {

    override fun observeLibrarySnapshot(userId: String): Flow<LibrarySnapshot> {
        val cleanUserId = requireValidUserId(userId)
        return combine(
            libraryDao.observeBooksForUser(cleanUserId),
            libraryDao.observeAllChaptersForUser(cleanUserId),
            libraryDao.observeAllTopicsForUser(cleanUserId)
        ) { bookEntities, chapterEntities, topicEntities ->
            val chaptersByBook = chapterEntities
                .map { it.toDomainModel() }
                .groupBy { it.bookId }

            val topicsByChapter = topicEntities
                .map { it.toDomainModel() }
                .groupBy { it.chapterId }

            val books = bookEntities.map { bookEntity ->
                val bookChapters = chaptersByBook[bookEntity.id].orEmpty()
                val bookTopics = bookChapters.flatMap { ch -> topicsByChapter[ch.id].orEmpty() }
                val completedCount = bookTopics.count { it.studyStatus == StudyStatus.COMPLETED }
                val activeChapter = bookChapters.firstOrNull { ch ->
                    topicsByChapter[ch.id].orEmpty().any { it.studyStatus == StudyStatus.IN_PROGRESS }
                } ?: bookChapters.firstOrNull()

                bookEntity.toDomainModel(
                    completedTopicsCount = completedCount,
                    totalTopicsCount = bookTopics.size,
                    activeChapterTitle = activeChapter?.title ?: "No chapters configured yet"
                )
            }

            val personalizedSequence = topicEntities
                .sortedWith(
                    compareBy<TopicEntity> {
                        when (it.studyStatusName) {
                            StudyStatus.IN_PROGRESS.name -> 0
                            StudyStatus.NEEDS_REVISION.name -> 1
                            StudyStatus.NOT_STARTED.name -> 2
                            else -> 3
                        }
                    }.thenBy { it.sortOrder }
                )
                .map { it.toDomainModel() }

            LibrarySnapshot(
                userId = cleanUserId,
                books = books,
                chaptersByBookId = chaptersByBook,
                topicsByChapterId = topicsByChapter,
                personalizedTopicSequence = personalizedSequence
            )
        }
    }

    override suspend fun ensureDefaultReferenceBooksSeeded(userId: String) {
        val cleanUserId = requireValidUserId(userId)
        if (libraryDao.countBooksForUser(cleanUserId) > 0) return

        val now = System.currentTimeMillis()
        val hour = 3_600_000L

        val baileyBookId = "book_bailey_28_$cleanUserId"
        val schwartzBookId = "book_schwartz_11_$cleanUserId"

        val defaultBooks = listOf(
            BookEntity(
                id = baileyBookId,
                userId = cleanUserId,
                title = "Bailey & Love's Short Practice of Surgery",
                edition = "28th Edition",
                authorsCsv = "Norman S. Williams|P. Ronan O'Connell|Andrew W. McCaskie",
                publicationYear = 2023,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            ),
            BookEntity(
                id = schwartzBookId,
                userId = cleanUserId,
                title = "Schwartz's Principles of Surgery",
                edition = "11th Edition",
                authorsCsv = "F. Charles Brunicardi|Dana K. Andersen|Timothy R. Billiar",
                publicationYear = 2019,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )

        val chHpbId = "ch_bailey_67_$cleanUserId"
        val chAppendixId = "ch_bailey_72_$cleanUserId"
        val chHerniaId = "ch_bailey_60_$cleanUserId"
        val chPancreasId = "ch_schwartz_33_$cleanUserId"
        val chTraumaId = "ch_schwartz_07_$cleanUserId"

        val defaultChapters = listOf(
            ChapterEntity(
                id = chHpbId,
                userId = cleanUserId,
                bookId = baileyBookId,
                title = "The Gallbladder and Bile Ducts",
                chapterNumber = 67,
                sortOrder = 1,
                description = "Biliary anatomy, Calot's triangle, acute calculous cholecystitis, Mirizzi syndrome, and choledocholithiasis.",
                statusName = StudyStatus.IN_PROGRESS.name,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            ),
            ChapterEntity(
                id = chAppendixId,
                userId = cleanUserId,
                bookId = baileyBookId,
                title = "The Vermiform Appendix",
                chapterNumber = 72,
                sortOrder = 2,
                description = "Acute appendicitis, Alvarado score, appendix mass (Ochsner-Sherren regimen), and appendiceal neoplasms.",
                statusName = StudyStatus.NOT_STARTED.name,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            ),
            ChapterEntity(
                id = chHerniaId,
                userId = cleanUserId,
                bookId = baileyBookId,
                title = "Abdominal Wall, Hernias and Umbilicus",
                chapterNumber = 60,
                sortOrder = 3,
                description = "Inguinal canal anatomy, direct vs indirect hernias, femoral hernia repair, and ventral abdominal wall reconstruction.",
                statusName = StudyStatus.NOT_STARTED.name,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            ),
            ChapterEntity(
                id = chPancreasId,
                userId = cleanUserId,
                bookId = schwartzBookId,
                title = "The Pancreas",
                chapterNumber = 33,
                sortOrder = 1,
                description = "Revised Atlanta classification of acute pancreatitis, step-up necrosectomy approach, and pancreaticoduodenectomy.",
                statusName = StudyStatus.IN_PROGRESS.name,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            ),
            ChapterEntity(
                id = chTraumaId,
                userId = cleanUserId,
                bookId = schwartzBookId,
                title = "Trauma and Damage Control Surgery",
                chapterNumber = 7,
                sortOrder = 2,
                description = "ATLS primary survey, FAST examination, blunt splenic/hepatic injury grading, and damage control laparotomy.",
                statusName = StudyStatus.NOT_STARTED.name,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )

        val defaultTopics = listOf(
            TopicEntity(
                id = "top_calot_$cleanUserId",
                userId = cleanUserId,
                bookId = baileyBookId,
                chapterId = chHpbId,
                bookTitle = "Bailey & Love 28th Ed",
                title = "Surgical Anatomy of Hepatocystic Triangle & Critical View of Safety (Strasberg)",
                sortOrder = 1,
                difficultyName = DifficultyLevel.FOUNDATION.name,
                studyStatusName = StudyStatus.COMPLETED.name,
                confidenceScore = 5,
                studyNotes = "2 structures entering gallbladder only; clear hepatocystic triangle of fat/fibrous tissue; lower 1/3 of gallbladder dissected off cystic plate.",
                lastStudiedEpochMillis = now - 48 * hour,
                nextRevisionEpochMillis = now + 7 * 24 * hour,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            ),
            TopicEntity(
                id = "top_mirizzi_$cleanUserId",
                userId = cleanUserId,
                bookId = baileyBookId,
                chapterId = chHpbId,
                bookTitle = "Bailey & Love 28th Ed",
                title = "Mirizzi Syndrome: Csendes Classification (Types I–V) & Subtotal Cholecystectomy",
                sortOrder = 2,
                difficultyName = DifficultyLevel.BOARD_ADVANCED.name,
                studyStatusName = StudyStatus.NEEDS_REVISION.name,
                confidenceScore = 2,
                studyNotes = "Avoid complete dissection of Calot's triangle in Type II/III to prevent transection of CBD; use fundus-first subtotal cholecystectomy or Roux-en-Y hepaticojejunostomy.",
                lastStudiedEpochMillis = now - 72 * hour,
                nextRevisionEpochMillis = now - 4 * hour,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            ),
            TopicEntity(
                id = "top_cholangitis_$cleanUserId",
                userId = cleanUserId,
                bookId = baileyBookId,
                chapterId = chHpbId,
                bookTitle = "Bailey & Love 28th Ed",
                title = "Acute Calculous Cholangitis: Tokyo Guidelines & Urgent Biliary Decompression",
                sortOrder = 3,
                difficultyName = DifficultyLevel.BOARD_ADVANCED.name,
                studyStatusName = StudyStatus.IN_PROGRESS.name,
                confidenceScore = 4,
                studyNotes = "Charcot's triad + hypotension + mental status change = Reynolds' pentad. Resuscitate + IV antibiotics + Urgent ERCP sphincterotomy.",
                lastStudiedEpochMillis = now - 6 * hour,
                nextRevisionEpochMillis = now + 72 * hour,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            ),
            TopicEntity(
                id = "top_ochsner_$cleanUserId",
                userId = cleanUserId,
                bookId = baileyBookId,
                chapterId = chAppendixId,
                bookTitle = "Bailey & Love 28th Ed",
                title = "Appendix Mass & Abscess: Conservative Ochsner-Sherren Regimen vs Percutaneous Drainage",
                sortOrder = 1,
                difficultyName = DifficultyLevel.INTERMEDIATE.name,
                studyStatusName = StudyStatus.NOT_STARTED.name,
                confidenceScore = 3,
                studyNotes = "Rule out caecal carcinoma in patients > 40 years via CT and follow-up colonoscopy.",
                lastStudiedEpochMillis = null,
                nextRevisionEpochMillis = null,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            ),
            TopicEntity(
                id = "top_atlanta_$cleanUserId",
                userId = cleanUserId,
                bookId = schwartzBookId,
                chapterId = chPancreasId,
                bookTitle = "Schwartz 11th Ed",
                title = "Acute Pancreatitis: Revised Atlanta Severity & Step-Up Management of Infected Necrosis",
                sortOrder = 1,
                difficultyName = DifficultyLevel.FELLOWSHIP_VIVA.name,
                studyStatusName = StudyStatus.IN_PROGRESS.name,
                confidenceScore = 3,
                studyNotes = "Delay necrosectomy > 4 weeks where possible to allow walled-off necrosis (WON) maturation; percutaneous/endoscopic drainage before VARD.",
                lastStudiedEpochMillis = now - 12 * hour,
                nextRevisionEpochMillis = now + 48 * hour,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )

        libraryDao.upsertBooks(defaultBooks)
        libraryDao.upsertChapters(defaultChapters)
        libraryDao.upsertTopics(defaultTopics)
    }

    override suspend fun saveBook(
        userId: String,
        bookId: String?,
        title: String,
        edition: String,
        authors: List<String>,
        publicationYear: Int?
    ): Result<Book> {
        val cleanUserId = requireValidUserId(userId)
        val cleanTitle = title.trim()
        val cleanEdition = edition.trim()
        if (cleanTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Book title cannot be blank."))
        }
        if (cleanEdition.isBlank()) {
            return Result.failure(IllegalArgumentException("Book edition cannot be blank."))
        }

        val now = System.currentTimeMillis()
        val resolvedId = bookId ?: "book_${UUID.randomUUID()}"
        val entity = BookEntity(
            id = resolvedId,
            userId = cleanUserId,
            title = cleanTitle,
            edition = cleanEdition,
            authorsCsv = authors.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("|"),
            publicationYear = publicationYear,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
            isPendingCloudSync = true
        )
        libraryDao.upsertBook(entity)
        syncBookToFirestoreIfAuthenticated(entity)
        return Result.success(
            entity.toDomainModel(
                completedTopicsCount = 0,
                totalTopicsCount = 0,
                activeChapterTitle = "Newly added reference"
            )
        )
    }

    override suspend fun deleteBook(userId: String, bookId: String): Result<Unit> {
        val cleanUserId = requireValidUserId(userId)
        libraryDao.deleteTopicsByBook(bookId, cleanUserId)
        libraryDao.deleteChaptersByBook(bookId, cleanUserId)
        libraryDao.deleteBook(bookId, cleanUserId)
        return Result.success(Unit)
    }

    override suspend fun saveChapter(
        userId: String,
        chapterId: String?,
        bookId: String,
        title: String,
        chapterNumber: Int?,
        description: String
    ): Result<Chapter> {
        val cleanUserId = requireValidUserId(userId)
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Chapter title cannot be blank."))
        }

        val existingChapters = libraryDao.getChaptersForBook(cleanUserId, bookId)
        val existingTarget = existingChapters.find { it.id == chapterId }
        val nextSortOrder = existingTarget?.sortOrder
            ?: ((existingChapters.maxOfOrNull { it.sortOrder } ?: 0) + 1)

        val now = System.currentTimeMillis()
        val entity = ChapterEntity(
            id = chapterId ?: "ch_${UUID.randomUUID()}",
            userId = cleanUserId,
            bookId = bookId,
            title = cleanTitle,
            chapterNumber = chapterNumber,
            sortOrder = nextSortOrder,
            description = description.trim(),
            statusName = existingTarget?.statusName ?: StudyStatus.NOT_STARTED.name,
            createdAtEpochMillis = existingTarget?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now,
            isPendingCloudSync = true
        )
        libraryDao.upsertChapter(entity)
        syncChapterToFirestoreIfAuthenticated(entity)
        return Result.success(entity.toDomainModel())
    }

    override suspend fun reorderChapter(
        userId: String,
        bookId: String,
        chapterId: String,
        moveUp: Boolean
    ): Result<Unit> {
        val cleanUserId = requireValidUserId(userId)
        val chapters = libraryDao.getChaptersForBook(cleanUserId, bookId)
            .sortedBy { it.sortOrder }
            .toMutableList()
        val index = chapters.indexOfFirst { it.id == chapterId }
        if (index == -1) return Result.failure(IllegalArgumentException("Chapter not found."))

        val swapIndex = if (moveUp) index - 1 else index + 1
        if (swapIndex !in chapters.indices) return Result.success(Unit)

        val current = chapters[index]
        val neighbor = chapters[swapIndex]
        chapters[index] = neighbor
        chapters[swapIndex] = current

        val now = System.currentTimeMillis()
        val reindexed = chapters.mapIndexed { idx, ch ->
            ch.copy(sortOrder = idx + 1, updatedAtEpochMillis = now, isPendingCloudSync = true)
        }
        libraryDao.upsertChapters(reindexed)
        return Result.success(Unit)
    }

    override suspend fun deleteChapter(userId: String, chapterId: String): Result<Unit> {
        val cleanUserId = requireValidUserId(userId)
        libraryDao.deleteTopicsByChapter(chapterId, cleanUserId)
        libraryDao.deleteChapter(chapterId, cleanUserId)
        return Result.success(Unit)
    }

    override suspend fun saveTopic(
        userId: String,
        topicId: String?,
        bookId: String,
        chapterId: String,
        title: String,
        difficulty: DifficultyLevel,
        studyStatus: StudyStatus,
        confidenceScore: Int,
        studyNotes: String
    ): Result<Topic> {
        val cleanUserId = requireValidUserId(userId)
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Topic title cannot be blank."))
        }

        val existingTopics = libraryDao.getTopicsForChapter(cleanUserId, chapterId)
        val existingTarget = existingTopics.find { it.id == topicId }
        val nextSortOrder = existingTarget?.sortOrder
            ?: ((existingTopics.maxOfOrNull { it.sortOrder } ?: 0) + 1)

        val now = System.currentTimeMillis()
        val entity = TopicEntity(
            id = topicId ?: "top_${UUID.randomUUID()}",
            userId = cleanUserId,
            bookId = bookId,
            chapterId = chapterId,
            bookTitle = existingTarget?.bookTitle ?: "Surgical Reference",
            title = cleanTitle,
            sortOrder = nextSortOrder,
            difficultyName = difficulty.name,
            studyStatusName = studyStatus.name,
            confidenceScore = confidenceScore.coerceIn(1, 5),
            studyNotes = studyNotes.trim(),
            lastStudiedEpochMillis = if (studyStatus != StudyStatus.NOT_STARTED) now else existingTarget?.lastStudiedEpochMillis,
            nextRevisionEpochMillis = existingTarget?.nextRevisionEpochMillis,
            createdAtEpochMillis = existingTarget?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now,
            isPendingCloudSync = true
        )
        libraryDao.upsertTopic(entity)
        syncTopicToFirestoreIfAuthenticated(entity)
        return Result.success(entity.toDomainModel())
    }

    override suspend fun updateTopicProgress(
        userId: String,
        topicId: String,
        studyStatus: StudyStatus,
        confidenceScore: Int,
        studyNotes: String,
        scheduleRevisionInDays: Int?
    ): Result<Topic> {
        val cleanUserId = requireValidUserId(userId)
        val existing = libraryDao.getTopicById(topicId, cleanUserId)
            ?: return Result.failure(IllegalArgumentException("Topic not found for active user."))

        val now = System.currentTimeMillis()
        val dayMillis = 24 * 3_600_000L
        val nextRevision = scheduleRevisionInDays?.let { now + it * dayMillis }
            ?: existing.nextRevisionEpochMillis

        val updated = existing.copy(
            studyStatusName = studyStatus.name,
            confidenceScore = confidenceScore.coerceIn(1, 5),
            studyNotes = studyNotes.trim(),
            lastStudiedEpochMillis = now,
            nextRevisionEpochMillis = nextRevision,
            updatedAtEpochMillis = now,
            isPendingCloudSync = true
        )
        libraryDao.upsertTopic(updated)
        syncTopicToFirestoreIfAuthenticated(updated)
        return Result.success(updated.toDomainModel())
    }

    override suspend fun reorderTopic(
        userId: String,
        chapterId: String,
        topicId: String,
        moveUp: Boolean
    ): Result<Unit> {
        val cleanUserId = requireValidUserId(userId)
        val topics = libraryDao.getTopicsForChapter(cleanUserId, chapterId)
            .sortedBy { it.sortOrder }
            .toMutableList()
        val index = topics.indexOfFirst { it.id == topicId }
        if (index == -1) return Result.failure(IllegalArgumentException("Topic not found."))

        val swapIndex = if (moveUp) index - 1 else index + 1
        if (swapIndex !in topics.indices) return Result.success(Unit)

        val current = topics[index]
        val neighbor = topics[swapIndex]
        topics[index] = neighbor
        topics[swapIndex] = current

        val now = System.currentTimeMillis()
        val reindexed = topics.mapIndexed { idx, topic ->
            topic.copy(sortOrder = idx + 1, updatedAtEpochMillis = now, isPendingCloudSync = true)
        }
        libraryDao.upsertTopics(reindexed)
        return Result.success(Unit)
    }

    override suspend fun deleteTopic(userId: String, topicId: String): Result<Unit> {
        val cleanUserId = requireValidUserId(userId)
        libraryDao.deleteTopic(topicId, cleanUserId)
        return Result.success(Unit)
    }

    private fun requireValidUserId(userId: String): String {
        val trimmed = userId.trim()
        require(trimmed.isNotEmpty()) { "User ID is required for data ownership isolation." }
        return trimmed
    }

    private fun isCloudSyncAvailableForUser(userId: String): Boolean {
        return runCatching {
            FirebaseApp.getApps(context).isNotEmpty() &&
                FirebaseAuth.getInstance().currentUser?.uid == userId
        }.getOrDefault(false)
    }

    private fun syncBookToFirestoreIfAuthenticated(book: BookEntity) {
        if (!isCloudSyncAvailableForUser(book.userId)) return
        runCatching {
            val payload = mapOf(
                "id" to book.id,
                "title" to book.title,
                "edition" to book.edition,
                "authors" to book.authorsCsv.split("|").filter { it.isNotBlank() },
                "publicationYear" to book.publicationYear,
                "userId" to book.userId,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(book.userId)
                .collection("books")
                .document(book.id)
                .set(payload, SetOptions.merge())
        }
    }

    private fun syncChapterToFirestoreIfAuthenticated(chapter: ChapterEntity) {
        if (!isCloudSyncAvailableForUser(chapter.userId)) return
        runCatching {
            val payload = mapOf(
                "id" to chapter.id,
                "bookId" to chapter.bookId,
                "title" to chapter.title,
                "chapterNumber" to chapter.chapterNumber,
                "sortOrder" to chapter.sortOrder,
                "description" to chapter.description,
                "status" to chapter.statusName,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(chapter.userId)
                .collection("chapters")
                .document(chapter.id)
                .set(payload, SetOptions.merge())
        }
    }

    private fun syncTopicToFirestoreIfAuthenticated(topic: TopicEntity) {
        if (!isCloudSyncAvailableForUser(topic.userId)) return
        runCatching {
            val payload = mapOf(
                "id" to topic.id,
                "chapterId" to topic.chapterId,
                "bookId" to topic.bookId,
                "title" to topic.title,
                "sortOrder" to topic.sortOrder,
                "difficulty" to topic.difficultyName,
                "studyStatus" to topic.studyStatusName,
                "confidenceScore" to topic.confidenceScore,
                "studyNotes" to topic.studyNotes,
                "lastStudiedEpochMillis" to topic.lastStudiedEpochMillis,
                "nextRevisionEpochMillis" to topic.nextRevisionEpochMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(topic.userId)
                .collection("topics")
                .document(topic.id)
                .set(payload, SetOptions.merge())
        }
    }
}
