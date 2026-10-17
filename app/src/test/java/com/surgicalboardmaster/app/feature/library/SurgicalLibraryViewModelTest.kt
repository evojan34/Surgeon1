package com.surgicalboardmaster.app.feature.library

import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.AuthenticatedUser
import com.surgicalboardmaster.app.domain.model.Book
import com.surgicalboardmaster.app.domain.model.Chapter
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.StudyStatus
import com.surgicalboardmaster.app.domain.model.Topic
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.LibrarySnapshot
import com.surgicalboardmaster.app.domain.repository.SurgicalLibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SurgicalLibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeLibraryRepository: FakeSurgicalLibraryRepository
    private lateinit var viewModel: SurgicalLibraryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakeLibraryRepository = FakeSurgicalLibraryRepository()
        viewModel = SurgicalLibraryViewModel(fakeAuthRepository, fakeLibraryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `seeds default Bailey and Love and Schwartz books for authenticated user`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.first()
        assertEquals(2, state.books.size)
        assertTrue(state.books.any { it.title.contains("Bailey & Love") })
        assertTrue(state.books.any { it.title.contains("Schwartz") })
    }

    @Test
    fun `enforces strict user ownership isolation when switching users`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.saveBook(
            title = "User A Custom HPB Atlas",
            edition = "1st Ed",
            authorsCsv = "Author A",
            publicationYear = 2026
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.books.size)

        // Switch to User B
        fakeAuthRepository.switchActiveUser("user_b_uid")
        testDispatcher.scheduler.advanceUntilIdle()

        val userBState = viewModel.uiState.value
        assertEquals("user_b_uid", userBState.userId)
        assertEquals(2, userBState.books.size)
        assertTrue(userBState.books.none { it.title.contains("User A Custom HPB Atlas") })
    }

    @Test
    fun `reorders topics and updates topic study status and notes`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val initialChapter = viewModel.uiState.value.chaptersForSelectedBook.first()
        val initialTopics = viewModel.uiState.value.topicsByChapterId[initialChapter.id].orEmpty()
        assertEquals(2, initialTopics.size)
        val firstTopicId = initialTopics[0].id
        val secondTopicId = initialTopics[1].id

        // Move second topic up
        viewModel.moveTopic(initialChapter.id, secondTopicId, moveUp = true)
        testDispatcher.scheduler.advanceUntilIdle()

        val reordered = viewModel.uiState.value.topicsByChapterId[initialChapter.id].orEmpty()
        assertEquals(secondTopicId, reordered[0].id)
        assertEquals(firstTopicId, reordered[1].id)

        // Update topic status, confidence (clamped 1..5), and notes
        viewModel.updateTopicProgress(
            topicId = secondTopicId,
            studyStatus = StudyStatus.COMPLETED,
            confidenceScore = 5,
            studyNotes = "Mastered Strasberg Critical View of Safety criteria.",
            scheduleRevisionInDays = 14
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedTopic = viewModel.uiState.value.topicsByChapterId[initialChapter.id]
            .orEmpty()
            .first { it.id == secondTopicId }
        assertEquals(StudyStatus.COMPLETED, updatedTopic.studyStatus)
        assertEquals(5, updatedTopic.confidenceScore)
        assertEquals("Mastered Strasberg Critical View of Safety criteria.", updatedTopic.studyNotes)
    }
}

private class FakeAuthRepository : AuthRepository {
    private val state = MutableStateFlow<AuthState>(
        AuthState.Authenticated(
            AuthenticatedUser(
                uid = "user_a_uid",
                email = "usera@surgicalboard.org",
                displayName = "Resident A",
                isCloudSynced = true
            )
        )
    )

    fun switchActiveUser(newUid: String) {
        state.value = AuthState.Authenticated(
            AuthenticatedUser(
                uid = newUid,
                email = "$newUid@surgicalboard.org",
                displayName = "Resident $newUid",
                isCloudSynced = true
            )
        )
    }

    override fun observeAuthState(): Flow<AuthState> = state
    override fun requireCurrentUserId(): String =
        (state.value as AuthState.Authenticated).user.uid

    override suspend fun signInWithEmail(email: String, password: String) =
        Result.success((state.value as AuthState.Authenticated).user)

    override suspend fun registerWithEmail(email: String, password: String, displayName: String) =
        Result.success((state.value as AuthState.Authenticated).user)

    override suspend fun continueInLocalWorkstationMode(displayName: String) =
        Result.success((state.value as AuthState.Authenticated).user)

    override suspend fun signOut() = Unit
}

private class FakeSurgicalLibraryRepository : SurgicalLibraryRepository {
    private val booksByUser = MutableStateFlow<Map<String, List<Book>>>(emptyMap())
    private val chaptersByUser = MutableStateFlow<Map<String, List<Chapter>>>(emptyMap())
    private val topicsByUser = MutableStateFlow<Map<String, List<Topic>>>(emptyMap())

    override fun observeLibrarySnapshot(userId: String): Flow<LibrarySnapshot> {
        return booksByUser.map {
            val books = booksByUser.value[userId].orEmpty()
            val chapters = chaptersByUser.value[userId].orEmpty().groupBy { it.bookId }
            val topics = topicsByUser.value[userId].orEmpty()
                .sortedBy { it.sortOrder }
                .groupBy { it.chapterId }
            LibrarySnapshot(
                userId = userId,
                books = books,
                chaptersByBookId = chapters,
                topicsByChapterId = topics,
                personalizedTopicSequence = topicsByUser.value[userId].orEmpty().sortedBy { it.sortOrder }
            )
        }
    }

    override suspend fun ensureDefaultReferenceBooksSeeded(userId: String) {
        if (booksByUser.value[userId] != null) return
        val book1 = Book("b1_$userId", "Bailey & Love", "28th Ed", emptyList(), 2023, userId, 0, 2, "Ch 67", 1L, 1L)
        val book2 = Book("b2_$userId", "Schwartz", "11th Ed", emptyList(), 2019, userId, 0, 0, "Ch 33", 1L, 1L)
        val ch1 = Chapter("c1_$userId", book1.id, "Gallbladder", 67, 1, "HPB", StudyStatus.IN_PROGRESS, 1L, 1L)
        val t1 = Topic("t1_$userId", ch1.id, book1.title, "Calot Triangle", 1, DifficultyLevel.FOUNDATION, StudyStatus.IN_PROGRESS, 3, null, null)
        val t2 = Topic("t2_$userId", ch1.id, book1.title, "Mirizzi Syndrome", 2, DifficultyLevel.BOARD_ADVANCED, StudyStatus.NEEDS_REVISION, 2, null, null)

        booksByUser.value = booksByUser.value + (userId to listOf(book1, book2))
        chaptersByUser.value = chaptersByUser.value + (userId to listOf(ch1))
        topicsByUser.value = topicsByUser.value + (userId to listOf(t1, t2))
    }

    override suspend fun saveBook(
        userId: String,
        bookId: String?,
        title: String,
        edition: String,
        authors: List<String>,
        publicationYear: Int?
    ): Result<Book> {
        val book = Book(bookId ?: "b_new", title, edition, authors, publicationYear, userId, 0, 0, "", 1L, 1L)
        booksByUser.value = booksByUser.value + (userId to (booksByUser.value[userId].orEmpty() + book))
        return Result.success(book)
    }

    override suspend fun deleteBook(userId: String, bookId: String) = Result.success(Unit)

    override suspend fun saveChapter(
        userId: String,
        chapterId: String?,
        bookId: String,
        title: String,
        chapterNumber: Int?,
        description: String
    ): Result<Chapter> {
        val ch = Chapter(chapterId ?: "c_new", bookId, title, chapterNumber, 1, description, StudyStatus.NOT_STARTED, 1L, 1L)
        chaptersByUser.value = chaptersByUser.value + (userId to (chaptersByUser.value[userId].orEmpty() + ch))
        return Result.success(ch)
    }

    override suspend fun reorderChapter(userId: String, bookId: String, chapterId: String, moveUp: Boolean) =
        Result.success(Unit)

    override suspend fun deleteChapter(userId: String, chapterId: String) = Result.success(Unit)

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
        val topic = Topic(topicId ?: "t_new", chapterId, "Book", title, 1, difficulty, studyStatus, confidenceScore, null, null, studyNotes)
        topicsByUser.value = topicsByUser.value + (userId to (topicsByUser.value[userId].orEmpty() + topic))
        return Result.success(topic)
    }

    override suspend fun updateTopicProgress(
        userId: String,
        topicId: String,
        studyStatus: StudyStatus,
        confidenceScore: Int,
        studyNotes: String,
        scheduleRevisionInDays: Int?
    ): Result<Topic> {
        val list = topicsByUser.value[userId].orEmpty().map {
            if (it.id == topicId) it.copy(studyStatus = studyStatus, confidenceScore = confidenceScore, studyNotes = studyNotes)
            else it
        }
        topicsByUser.value = topicsByUser.value + (userId to list)
        booksByUser.value = booksByUser.value.toMap() // trigger emission
        return Result.success(list.first { it.id == topicId })
    }

    override suspend fun reorderTopic(userId: String, chapterId: String, topicId: String, moveUp: Boolean): Result<Unit> {
        val list = topicsByUser.value[userId].orEmpty().sortedBy { it.sortOrder }.toMutableList()
        val idx = list.indexOfFirst { it.id == topicId }
        val swapIdx = if (moveUp) idx - 1 else idx + 1
        if (idx != -1 && swapIdx in list.indices) {
            val temp = list[idx]
            list[idx] = list[swapIdx]
            list[swapIdx] = temp
            val reindexed = list.mapIndexed { i, t -> t.copy(sortOrder = i + 1) }
            topicsByUser.value = topicsByUser.value + (userId to reindexed)
            booksByUser.value = booksByUser.value.toMap()
        }
        return Result.success(Unit)
    }

    override suspend fun deleteTopic(userId: String, topicId: String) = Result.success(Unit)
}
