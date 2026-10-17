package com.surgicalboardmaster.app.feature.exams

import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.AuthenticatedUser
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.ExamAttempt
import com.surgicalboardmaster.app.domain.model.ExamOption
import com.surgicalboardmaster.app.domain.model.ExamQuestion
import com.surgicalboardmaster.app.domain.model.QuestionSubmission
import com.surgicalboardmaster.app.domain.model.QuestionType
import com.surgicalboardmaster.app.domain.model.WrongAnswerItem
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.ExamRepository
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
class ExamCenterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuth: FakeAuthRepo2
    private lateinit var fakeExam: FakeExamRepo
    private lateinit var viewModel: ExamCenterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuth = FakeAuthRepo2()
        fakeExam = FakeExamRepo()
        viewModel = ExamCenterViewModel(fakeAuth, fakeExam)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starting exam transitions to InProgress state with loaded questions`() = runTest {
        viewModel.startExam(questionCount = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.flowState.value
        assertTrue(state is ExamFlowState.InProgress)
        val inProgress = state as ExamFlowState.InProgress
        assertEquals(1, inProgress.questions.size)
    }

    @Test
    fun `submitting exam records attempt and transitions to Review state`() = runTest {
        viewModel.startExam(questionCount = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectAnswer("q1", "C")
        viewModel.submitExam()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.flowState.value
        assertTrue(state is ExamFlowState.Review)
        val review = state as ExamFlowState.Review
        assertEquals(100, review.attempt.scorePercentage)
    }
}

private class FakeAuthRepo2 : AuthRepository {
    private val st = MutableStateFlow<AuthState>(AuthState.Authenticated(AuthenticatedUser("uid1", "a@b.com", "Dr", true)))
    override fun observeAuthState(): Flow<AuthState> = st
    override fun requireCurrentUserId(): String = "uid1"
    override suspend fun signInWithEmail(e: String, p: String) = Result.success((st.value as AuthState.Authenticated).user)
    override suspend fun registerWithEmail(e: String, p: String, d: String) = Result.success((st.value as AuthState.Authenticated).user)
    override suspend fun continueInLocalWorkstationMode(d: String) = Result.success((st.value as AuthState.Authenticated).user)
    override suspend fun signOut() = Unit
}

private class FakeExamRepo : ExamRepository {
    override fun observeAllQuestions(): Flow<List<ExamQuestion>> = MutableStateFlow(emptyList())
    override fun observeExamAttempts(userId: String): Flow<List<ExamAttempt>> = MutableStateFlow(emptyList())
    override suspend fun getQuestionsForExam(topicId: String?, limit: Int): List<ExamQuestion> {
        return listOf(
            ExamQuestion(
                id = "q1", topicId = "t1", topicTitle = "Mirizzi", bookReference = "Bailey 28",
                questionType = QuestionType.SINGLE_BEST_ANSWER, difficulty = DifficultyLevel.BOARD_ADVANCED,
                clinicalVignette = "Vignette", questionPrompt = "Prompt",
                options = listOf(ExamOption("C", "Hepaticojejunostomy", "Correct rationale")),
                correctAnswerId = "C", comprehensiveExplanation = "Explanation", boardPearls = "Pearl", verifiedSourceProvenance = "Prov"
            )
        )
    }
    override suspend fun submitExamAttempt(
        userId: String,
        examTitle: String,
        isTimedMode: Boolean,
        timeLimitMinutes: Int,
        submissions: List<QuestionSubmission>
    ): Result<ExamAttempt> {
        val att = ExamAttempt("att1", userId, examTitle, isTimedMode, timeLimitMinutes, 1, 1, 100, submissions, 1L)
        return Result.success(att)
    }
    override suspend fun observeWrongAnswerNotebook(userId: String): List<WrongAnswerItem> = emptyList()
    override suspend fun ensureDefaultQuestionsSeeded() = Unit
}
