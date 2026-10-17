package com.surgicalboardmaster.app.feature.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.Flashcard
import com.surgicalboardmaster.app.domain.model.ReviewRating
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.FlashcardRepository
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

data class FlashcardsUiState(
    val activeDeck: List<Flashcard> = emptyList(),
    val currentCardIndex: Int = 0,
    val isAnswerFlipped: Boolean = false,
    val isDeckCompleted: Boolean = false
)

@HiltViewModel
class FlashcardsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardsUiState())
    val uiState: StateFlow<FlashcardsUiState> = _uiState.asStateFlow()

    val allCards: StateFlow<List<Flashcard>> = authRepository.observeAuthState()
        .map { state -> if (state is AuthState.Authenticated) state.user.uid else authRepository.requireCurrentUserId() }
        .flatMapLatest { uid -> flashcardRepository.observeAllFlashcards(uid) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        loadDueCards()
    }

    fun loadDueCards() {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            flashcardRepository.ensureDefaultFlashcardsSeeded(uid)
            val due = flashcardRepository.getDueFlashcards(uid)
            _uiState.value = FlashcardsUiState(
                activeDeck = due,
                currentCardIndex = 0,
                isAnswerFlipped = false,
                isDeckCompleted = due.isEmpty()
            )
        }
    }

    fun flipCard() {
        _uiState.value = _uiState.value.copy(isAnswerFlipped = !_uiState.value.isAnswerFlipped)
    }

    fun answerCard(rating: ReviewRating) {
        val current = _uiState.value
        val card = current.activeDeck.getOrNull(current.currentCardIndex) ?: return

        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            flashcardRepository.reviewFlashcard(uid, card.id, rating)

            val nextIdx = current.currentCardIndex + 1
            if (nextIdx < current.activeDeck.size) {
                _uiState.value = current.copy(currentCardIndex = nextIdx, isAnswerFlipped = false)
            } else {
                _uiState.value = current.copy(isDeckCompleted = true)
            }
        }
    }
}
