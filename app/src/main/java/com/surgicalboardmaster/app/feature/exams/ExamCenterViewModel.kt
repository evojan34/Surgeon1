package com.surgicalboardmaster.app.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.ExamAttempt
import com.surgicalboardmaster.app.domain.model.ExamQuestion
import com.surgicalboardmaster.app.domain.model.QuestionSubmission
import com.surgicalboardmaster.app.domain.model.WrongAnswerItem
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ExamFlowState {
    data object Configuration : ExamFlowState
    data class InProgress(
        val questions: List<ExamQuestion>,
        val currentIndex: Int,
        val userAnswers: Map<String, String>, // questionId -> optionId
        val isTimed: Boolean,
        val remainingSeconds: Int
    ) : ExamFlowState
    data class Review(
        val attempt: ExamAttempt,
        val questions: List<ExamQuestion>
    ) : ExamFlowState
}

@HiltViewModel
class ExamCenterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _flowState = MutableStateFlow<ExamFlowState>(ExamFlowState.Configuration)
    val flowState: StateFlow<ExamFlowState> = _flowState.asStateFlow()

    private val _wrongAnswers = MutableStateFlow<List<WrongAnswerItem>>(emptyList())
    val wrongAnswers: StateFlow<List<WrongAnswerItem>> = _wrongAnswers.asStateFlow()

    val pastAttempts: StateFlow<List<ExamAttempt>> = authRepository.observeAuthState()
        .map { state -> if (state is AuthState.Authenticated) state.user.uid else authRepository.requireCurrentUserId() }
        .flatMapLatest { uid -> examRepository.observeExamAttempts(uid) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        refreshWrongAnswers()
    }

    fun startExam(topicId: String? = null, questionCount: Int = 5, isTimed: Boolean = true) {
        viewModelScope.launch {
            val questions = examRepository.getQuestionsForExam(topicId, questionCount)
            if (questions.isNotEmpty()) {
                _flowState.value = ExamFlowState.InProgress(
                    questions = questions,
                    currentIndex = 0,
                    userAnswers = emptyMap(),
                    isTimed = isTimed,
                    remainingSeconds = questionCount * 90 // 1.5 min per question standard
                )
            }
        }
    }

    fun selectAnswer(questionId: String, optionId: String) {
        val current = _flowState.value as? ExamFlowState.InProgress ?: return
        val updated = current.userAnswers + (questionId to optionId)
        _flowState.value = current.copy(userAnswers = updated)
    }

    fun nextQuestion() {
        val current = _flowState.value as? ExamFlowState.InProgress ?: return
        if (current.currentIndex + 1 < current.questions.size) {
            _flowState.value = current.copy(currentIndex = current.currentIndex + 1)
        }
    }

    fun previousQuestion() {
        val current = _flowState.value as? ExamFlowState.InProgress ?: return
        if (current.currentIndex > 0) {
            _flowState.value = current.copy(currentIndex = current.currentIndex - 1)
        }
    }

    fun submitExam() {
        val current = _flowState.value as? ExamFlowState.InProgress ?: return
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            val submissions = current.questions.map { q ->
                val chosen = current.userAnswers[q.id] ?: ""
                val isCorrect = chosen.equals(q.correctAnswerId, ignoreCase = true)
                QuestionSubmission(
                    questionId = q.id,
                    selectedOptionId = chosen,
                    isCorrect = isCorrect,
                    timeSpentSeconds = 45
                )
            }

            val result = examRepository.submitExamAttempt(
                userId = uid,
                examTitle = "General Surgery Board Mock Examination",
                isTimedMode = current.isTimed,
                timeLimitMinutes = current.remainingSeconds / 60,
                submissions = submissions
            )

            result.onSuccess { attempt ->
                _flowState.value = ExamFlowState.Review(attempt, current.questions)
                refreshWrongAnswers()
            }
        }
    }

    fun resetToConfiguration() {
        _flowState.value = ExamFlowState.Configuration
    }

    private fun refreshWrongAnswers() {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            _wrongAnswers.value = examRepository.observeWrongAnswerNotebook(uid)
        }
    }
}
