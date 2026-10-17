package com.surgicalboardmaster.app.feature.tutor

import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.AuthenticatedUser
import com.surgicalboardmaster.app.domain.model.Flashcard
import com.surgicalboardmaster.app.domain.model.LessonConfig
import com.surgicalboardmaster.app.domain.model.LessonFormat
import com.surgicalboardmaster.app.domain.model.ReviewRating
import com.surgicalboardmaster.app.domain.model.StructuredLesson
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.FlashcardRepository
import com.surgicalboardmaster.app.domain.repository.LessonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class AiTutorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuth: FakeAuthRepo
    private lateinit var fakeLesson: FakeLessonRepo
    private lateinit var fakeFlashcard: FakeFlashcardRepo
    private lateinit var viewModel: AiTutorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuth = FakeAuthRepo()
        fakeLesson = FakeLessonRepo()
        fakeFlashcard = FakeFlashcardRepo()
        viewModel = AiTutorViewModel(fakeAuth, fakeLesson, fakeFlashcard)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `generating lesson sets state to LessonReady with structured Bailey content`() = runTest {
        viewModel.startLesson(topicId = "topic_mirizzi")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is TutorUiState.LessonReady)
        val lesson = (state as TutorUiState.LessonReady).lesson
        assertEquals("topic_mirizzi", lesson.topicId)
        assertTrue(lesson.boardPearls.isNotEmpty())
    }

    @Test
    fun `generating flashcards from active lesson updates repository`() = runTest {
        viewModel.startLesson(topicId = "topic_mirizzi")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.generateFlashcardsFromActiveLesson()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, fakeFlashcard.cardsGenerated)
    }
}

private class FakeAuthRepo : AuthRepository {
    private val st = MutableStateFlow<AuthState>(AuthState.Authenticated(AuthenticatedUser("uid1", "a@b.com", "Dr", true)))
    override fun observeAuthState(): Flow<AuthState> = st
    override fun requireCurrentUserId(): String = "uid1"
    override suspend fun signInWithEmail(e: String, p: String) = Result.success((st.value as AuthState.Authenticated).user)
    override suspend fun registerWithEmail(e: String, p: String, d: String) = Result.success((st.value as AuthState.Authenticated).user)
    override suspend fun continueInLocalWorkstationMode(d: String) = Result.success((st.value as AuthState.Authenticated).user)
    override suspend fun signOut() = Unit
}

private class FakeLessonRepo : LessonRepository {
    override fun observeSavedLessons(userId: String): Flow<List<StructuredLesson>> = MutableStateFlow(emptyList())
    override suspend fun getLessonById(lessonId: String, userId: String): StructuredLesson? = null
    override suspend fun generateOrFetchStructuredLesson(userId: String, topicId: String, config: LessonConfig): Result<StructuredLesson> {
        val dummy = StructuredLesson(
            id = "l1", userId = userId, bookId = "b1", bookTitle = "Bailey", chapterId = "c1", chapterTitle = "HPB",
            topicId = topicId, topicTitle = "Mirizzi", format = LessonFormat.BOARD_EXAM_PREP, language = "English",
            teachingDepth = "Board", learningObjectives = listOf("Obj 1"), relevantAnatomy = "Calot", pathophysiology = "Stone",
            clinicalPresentation = "Pain", investigationsAndInterpretation = "US", managementAlgorithms = "Surg",
            operativePrinciples = "CVS", complications = "BDI", boardPearls = listOf("P1", "P2"), commonMistakes = listOf("M1"),
            summary = "Sum", references = listOf("Ref"), createdAtEpochMillis = 1L, updatedAtEpochMillis = 1L
        )
        return Result.success(dummy)
    }
    override suspend fun toggleBookmark(lessonId: String, userId: String, isBookmarked: Boolean) = Result.success(Unit)
    override suspend fun savePersonalNotes(lessonId: String, userId: String, notes: String) = Result.success(Unit)
}

private class FakeFlashcardRepo : FlashcardRepository {
    var cardsGenerated = 0
    override fun observeAllFlashcards(userId: String): Flow<List<Flashcard>> = MutableStateFlow(emptyList())
    override suspend fun getDueFlashcards(userId: String): List<Flashcard> = emptyList()
    override suspend fun reviewFlashcard(userId: String, cardId: String, rating: ReviewRating): Result<Flashcard> = Result.failure(Exception())
    override suspend fun createFlashcard(userId: String, topicId: String, topicTitle: String, front: String, back: String, pearl: String): Result<Flashcard> = Result.failure(Exception())
    override suspend fun generateFlashcardsFromLesson(userId: String, topicId: String, topicTitle: String, pearls: List<String>): Result<Int> {
        cardsGenerated = pearls.size
        return Result.success(pearls.size)
    }
    override suspend fun ensureDefaultFlashcardsSeeded(userId: String) = Unit
}
