package com.surgicalboardmaster.app.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.Book
import com.surgicalboardmaster.app.domain.model.Chapter
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.StudyStatus
import com.surgicalboardmaster.app.domain.model.Topic
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.SurgicalLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryViewMode(val labelEn: String, val labelAr: String) {
    CHAPTER_HIERARCHY("Chapter Hierarchy", "حسب الفصول"),
    PERSONALIZED_SEQUENCE("Custom Study Sequence", "التسلسل المخصص")
}

data class SurgicalLibraryUiState(
    val userId: String = "local_board_candidate",
    val books: List<Book> = emptyList(),
    val selectedBook: Book? = null,
    val chaptersForSelectedBook: List<Chapter> = emptyList(),
    val topicsByChapterId: Map<String, List<Topic>> = emptyMap(),
    val personalizedSequence: List<Topic> = emptyList(),
    val viewMode: LibraryViewMode = LibraryViewMode.CHAPTER_HIERARCHY,
    val statusFilter: StudyStatus? = null,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SurgicalLibraryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val libraryRepository: SurgicalLibraryRepository
) : ViewModel() {

    private val selectedBookIdFlow = MutableStateFlow<String?>(null)
    private val viewModeFlow = MutableStateFlow(LibraryViewMode.CHAPTER_HIERARCHY)
    private val statusFilterFlow = MutableStateFlow<StudyStatus?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val activeUserIdFlow = authRepository.observeAuthState()
        .map { state ->
            if (state is AuthState.Authenticated) state.user.uid
            else authRepository.requireCurrentUserId()
        }
        .distinctUntilChanged()
        .onEach { uid ->
            libraryRepository.ensureDefaultReferenceBooksSeeded(uid)
        }

    val uiState: StateFlow<SurgicalLibraryUiState> = combine(
        activeUserIdFlow.flatMapLatest { uid -> libraryRepository.observeLibrarySnapshot(uid) },
        selectedBookIdFlow,
        viewModeFlow,
        statusFilterFlow,
        _errorMessage
    ) { snapshot, selectedBookId, viewMode, statusFilter, errorMsg ->
        val resolvedBook = snapshot.books.find { it.id == selectedBookId }
            ?: snapshot.books.firstOrNull()

        val rawChapters = resolvedBook?.let { snapshot.chaptersByBookId[it.id].orEmpty() }.orEmpty()
        val filteredTopicsByChapter = snapshot.topicsByChapterId.mapValues { (_, topics) ->
            if (statusFilter == null) topics
            else topics.filter { it.studyStatus == statusFilter }
        }
        val filteredPersonalized = if (statusFilter == null) {
            snapshot.personalizedTopicSequence
        } else {
            snapshot.personalizedTopicSequence.filter { it.studyStatus == statusFilter }
        }

        SurgicalLibraryUiState(
            userId = snapshot.userId,
            books = snapshot.books,
            selectedBook = resolvedBook,
            chaptersForSelectedBook = rawChapters,
            topicsByChapterId = filteredTopicsByChapter,
            personalizedSequence = filteredPersonalized,
            viewMode = viewMode,
            statusFilter = statusFilter,
            errorMessage = errorMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SurgicalLibraryUiState()
    )

    fun selectBook(bookId: String) {
        selectedBookIdFlow.value = bookId
    }

    fun setViewMode(mode: LibraryViewMode) {
        viewModeFlow.value = mode
    }

    fun setStatusFilter(status: StudyStatus?) {
        statusFilterFlow.value = status
    }

    fun saveBook(
        bookId: String? = null,
        title: String,
        edition: String,
        authorsCsv: String,
        publicationYear: Int?
    ) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            val authors = authorsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val result = libraryRepository.saveBook(
                userId = uid,
                bookId = bookId,
                title = title,
                edition = edition,
                authors = authors,
                publicationYear = publicationYear
            )
            result.onSuccess { saved ->
                selectedBookIdFlow.value = saved.id
                _errorMessage.value = null
            }.onFailure { err ->
                _errorMessage.value = err.message
            }
        }
    }

    fun saveChapter(
        chapterId: String? = null,
        bookId: String,
        title: String,
        chapterNumber: Int?,
        description: String
    ) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            val result = libraryRepository.saveChapter(
                userId = uid,
                chapterId = chapterId,
                bookId = bookId,
                title = title,
                chapterNumber = chapterNumber,
                description = description
            )
            result.onFailure { _errorMessage.value = it.message }
        }
    }

    fun moveChapter(bookId: String, chapterId: String, moveUp: Boolean) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            libraryRepository.reorderChapter(uid, bookId, chapterId, moveUp)
        }
    }

    fun deleteChapter(chapterId: String) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            libraryRepository.deleteChapter(uid, chapterId)
        }
    }

    fun saveTopic(
        topicId: String? = null,
        bookId: String,
        chapterId: String,
        title: String,
        difficulty: DifficultyLevel,
        studyStatus: StudyStatus,
        confidenceScore: Int,
        studyNotes: String
    ) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            val result = libraryRepository.saveTopic(
                userId = uid,
                topicId = topicId,
                bookId = bookId,
                chapterId = chapterId,
                title = title,
                difficulty = difficulty,
                studyStatus = studyStatus,
                confidenceScore = confidenceScore,
                studyNotes = studyNotes
            )
            result.onFailure { _errorMessage.value = it.message }
        }
    }

    fun updateTopicProgress(
        topicId: String,
        studyStatus: StudyStatus,
        confidenceScore: Int,
        studyNotes: String,
        scheduleRevisionInDays: Int? = null
    ) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            libraryRepository.updateTopicProgress(
                userId = uid,
                topicId = topicId,
                studyStatus = studyStatus,
                confidenceScore = confidenceScore,
                studyNotes = studyNotes,
                scheduleRevisionInDays = scheduleRevisionInDays
            )
        }
    }

    fun moveTopic(chapterId: String, topicId: String, moveUp: Boolean) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            libraryRepository.reorderTopic(uid, chapterId, topicId, moveUp)
        }
    }

    fun deleteTopic(topicId: String) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            libraryRepository.deleteTopic(uid, topicId)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
