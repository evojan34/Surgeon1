package com.surgicalboardmaster.app.feature.clinicalcases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.CaseCategory
import com.surgicalboardmaster.app.domain.model.CaseEventType
import com.surgicalboardmaster.app.domain.model.CaseStatus
import com.surgicalboardmaster.app.domain.model.CaseTimelineEvent
import com.surgicalboardmaster.app.domain.model.ClinicalCaseDetail
import com.surgicalboardmaster.app.domain.model.ClinicalCaseFilter
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.ClinicalCaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ClinicalCasesUiState {
    data class Catalog(
        val cases: List<ClinicalCaseDetail>,
        val filter: ClinicalCaseFilter
    ) : ClinicalCasesUiState

    data class Detail(
        val caseDetail: ClinicalCaseDetail,
        val timelineEvents: List<CaseTimelineEvent>,
        val exportText: String? = null
    ) : ClinicalCasesUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ClinicalCasesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val caseRepository: ClinicalCaseRepository
) : ViewModel() {

    private val selectedCaseIdFlow = MutableStateFlow<String?>(null)
    private val filterFlow = MutableStateFlow(ClinicalCaseFilter())

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val activeUserIdFlow = authRepository.observeAuthState()
        .map { state ->
            if (state is AuthState.Authenticated) state.user.uid
            else authRepository.requireCurrentUserId()
        }
        .map { uid ->
            caseRepository.ensureDefaultEducationalCasesSeeded(uid)
            uid
        }

    val uiState: StateFlow<ClinicalCasesUiState> = combine(
        activeUserIdFlow.flatMapLatest { uid -> caseRepository.observeCases(uid) },
        selectedCaseIdFlow,
        filterFlow
    ) { allCases, selectedId, filter ->
        if (selectedId != null) {
            val detail = allCases.find { it.caseId == selectedId }
            if (detail != null) {
                // Return detail view (timeline observation wired separately or via repository)
                ClinicalCasesUiState.Detail(
                    caseDetail = detail,
                    emptyList()
                )
            } else {
                ClinicalCasesUiState.Catalog(allCases, filter)
            }
        } else {
            val filtered = allCases.filter { c ->
                val matchSearch = filter.searchQuery.isBlank() ||
                    c.pseudonymousId.contains(filter.searchQuery, ignoreCase = true) ||
                    c.chiefComplaint.contains(filter.searchQuery, ignoreCase = true) ||
                    c.finalDiagnosis.contains(filter.searchQuery, ignoreCase = true)

                val matchCategory = filter.category == null || c.category == filter.category
                val matchStatus = filter.status == null || c.status == filter.status
                val matchArchived = filter.includeArchived || !c.isArchived

                matchSearch && matchCategory && matchStatus && matchArchived
            }
            ClinicalCasesUiState.Catalog(filtered, filter)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClinicalCasesUiState.Catalog(emptyList(), ClinicalCaseFilter())
    )

    fun selectCase(caseId: String?) {
        selectedCaseIdFlow.value = caseId
    }

    fun updateFilter(newFilter: ClinicalCaseFilter) {
        filterFlow.value = newFilter
    }

    fun saveCase(detail: ClinicalCaseDetail) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            val result = caseRepository.saveClinicalCase(uid, detail)
            result.onSuccess {
                _feedbackMessage.value = "Clinical case saved securely."
                selectedCaseIdFlow.value = it.caseId
            }.onFailure { err ->
                _feedbackMessage.value = err.message
            }
        }
    }

    fun appendTimelineEvent(caseId: String, eventType: CaseEventType, summary: String, details: String) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            val res = caseRepository.appendTimelineEvent(uid, caseId, eventType, summary, details)
            res.onSuccess { _feedbackMessage.value = "Append-only timeline event recorded." }
                .onFailure { _feedbackMessage.value = it.message }
        }
    }

    fun exportCase(caseId: String) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            val res = caseRepository.exportDeIdentifiedCaseSummary(uid, caseId)
            res.onSuccess { text ->
                _feedbackMessage.value = "Case exported successfully (De-identified)."
            }
        }
    }

    fun archiveCase(caseId: String, archive: Boolean) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            caseRepository.archiveCase(uid, caseId, archive)
            selectedCaseIdFlow.value = null
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }
}
