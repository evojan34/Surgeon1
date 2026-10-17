package com.surgicalboardmaster.app.feature.followups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.FollowUpTask
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.ClinicalCaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FollowUpCalendarViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val clinicalCaseRepository: ClinicalCaseRepository
) : ViewModel() {

    val followUpTasks: StateFlow<List<FollowUpTask>> = authRepository.observeAuthState()
        .map { state ->
            if (state is AuthState.Authenticated) state.user.uid
            else authRepository.requireCurrentUserId()
        }
        .flatMapLatest { uid -> clinicalCaseRepository.observeFollowUpTasks(uid) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun markCompleted(taskId: String) {
        viewModelScope.launch {
            val uid = authRepository.requireCurrentUserId()
            clinicalCaseRepository.markFollowUpTaskCompleted(uid, taskId)
        }
    }
}
