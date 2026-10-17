package com.surgicalboardmaster.app.feature.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.LessonConfig
import com.surgicalboardmaster.app.domain.model.LessonFormat
import com.surgicalboardmaster.app.domain.model.StructuredLesson
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.FlashcardRepository
import com.surgicalboardmaster.app.domain.repository.LessonRepository
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

sealed interface TutorUiState {
    data object Idle : TutorUiState
    data object Generating : TutorUiState
    data class LessonReady(val lesson: StructuredLesson) : TutorUiState
    data class Error(val message: String) : TutorUiState
}

@HiltViewModel
class AiTutorViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val lessonRepository: LessonRepository,
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TutorUiState>(TutorUiState.Idle)
    val uiState: StateFlow<TutorUiState> = _uiState.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    val savedLessons: StateFlow<List<StructuredLesson>> = authRepository.observeAuthState()
        .map { state -> if (state is AuthState.Authenticated) state.user.uid else authRepository.requireCurrentUserId() }
        .flatMapLatest { uid -> lessonRepository.observeSavedLessons(uid) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun startLesson(
        topicId: String = "topic_mirizzi",
        bookTitle: String = "Bailey & Love's Short Practice of Surgery (28th Ed)",
        chapterTitle: String = "Ch 67: The Gallbladder and Bile Ducts",
        topicTitle: String = "Mirizzi Syndrome & Strasberg Critical View of Safety",
        format: LessonFormat = LessonFormat.BOARD_EXAM_PREP
    ) {
        viewModelScope.launch {
            _uiState.value = TutorUiState.Generating
            val uid = authRepository.requireCurrentUserId()
            val config = LessonConfig(
                bookTitle = bookTitle,
                chapterTitle = chapterTitle,
                topicTitle = topicTitle,
                format = format
            )
            val result = lessonRepository.generateOrFetchStructuredLesson(uid, topicId, config)
            result.fold(
                onSuccess = { _uiState.value = TutorUiState.LessonReady(it) },
                onFailure = { _uiState.value = TutorUiState.Error(it.message ?: "Failed to generate lesson") }
            )
        }
    }

    fun openSavedLesson(lesson: StructuredLesson) {
        _uiState.value = TutorUiState.LessonReady(lesson)
    }

    fun toggleBookmark(lessonId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            lessonRepository.toggleBookmark(lessonId, uid, !currentStatus)
            val current = _uiState.value
            if (current is TutorUiState.LessonReady && current.lesson.id == lessonId) {
                _uiState.value = TutorUiState.LessonReady(current.lesson.copy(isBookmarked = !currentStatus))
            }
        }
    }

    fun saveNotes(lessonId: String, notes: String) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            lessonRepository.savePersonalNotes(lessonId, uid, notes)
            _feedbackMessage.value = "Personal notes saved."
        }
    }

    fun generateFlashcardsFromActiveLesson() {
        val current = _uiState.value as? TutorUiState.LessonReady ?: return
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            val res = flashcardRepository.generateFlashcardsFromLesson(
                userId = uid,
                topicId = current.lesson.topicId,
                topicTitle = current.lesson.topicTitle,
                pearls = current.lesson.boardPearls
            )
            res.onSuccess { count ->
                _feedbackMessage.value = "Generated $count high-yield flashcards added to spaced repetition deck!"
            }
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }
}
