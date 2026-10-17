package com.surgicalboardmaster.app.feature.simulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surgicalboardmaster.app.domain.model.AuthState
import com.surgicalboardmaster.app.domain.model.SimulationActionOption
import com.surgicalboardmaster.app.domain.model.SimulationDebriefReport
import com.surgicalboardmaster.app.domain.model.SimulationScenario
import com.surgicalboardmaster.app.domain.model.StageDecisionLog
import com.surgicalboardmaster.app.domain.model.SurgicalDomain
import com.surgicalboardmaster.app.domain.repository.AuthRepository
import com.surgicalboardmaster.app.domain.repository.SimulatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SimulatorUiState {
    data object ScenarioSelection : SimulatorUiState
    data class ActiveSimulation(
        val scenario: SimulationScenario,
        val currentStageIndex: Int,
        val selectedOptionForCurrentStage: SimulationActionOption?,
        val recordedLogs: List<StageDecisionLog>
    ) : SimulatorUiState
    data class Debriefing(val report: SimulationDebriefReport) : SimulatorUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ClinicalSimulatorViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val simulatorRepository: SimulatorRepository
) : ViewModel() {

    private val domainFilterFlow = MutableStateFlow<SurgicalDomain?>(null)
    val selectedDomainFilter: StateFlow<SurgicalDomain?> = domainFilterFlow.asStateFlow()

    val availableScenarios: StateFlow<List<SimulationScenario>> = domainFilterFlow
        .flatMapLatest { domain -> simulatorRepository.observeAvailableScenarios(domain) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val completedDebriefs: StateFlow<List<SimulationDebriefReport>> = authRepository.observeAuthState()
        .map { state ->
            if (state is AuthState.Authenticated) state.user.uid
            else authRepository.requireCurrentUserId()
        }
        .flatMapLatest { uid -> simulatorRepository.observeCompletedDebriefs(uid) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<SimulatorUiState>(SimulatorUiState.ScenarioSelection)
    val uiState: StateFlow<SimulatorUiState> = _uiState.asStateFlow()

    fun setDomainFilter(domain: SurgicalDomain?) {
        domainFilterFlow.value = domain
    }

    fun startScenario(scenario: SimulationScenario) {
        _uiState.value = SimulatorUiState.ActiveSimulation(
            scenario = scenario,
            currentStageIndex = 0,
            selectedOptionForCurrentStage = null,
            recordedLogs = emptyList()
        )
    }

    fun chooseStageAction(option: SimulationActionOption) {
        val current = _uiState.value as? SimulatorUiState.ActiveSimulation ?: return
        if (current.selectedOptionForCurrentStage != null) return // Already committed for this stage

        val stage = current.scenario.stages[current.currentStageIndex]
        val maxStageScore = stage.availableActions.maxOfOrNull { it.scoreDelta }?.coerceAtLeast(10) ?: 20
        val missedCritical = stage.availableActions.any { it.isCriticalSafetyAction } && !option.isCriticalSafetyAction

        val log = StageDecisionLog(
            stageType = stage.stageType,
            selectedOptionId = option.optionId,
            actionText = option.actionText,
            clinicalOutcomeReveal = option.clinicalOutcomeReveal,
            rationale = option.rationale,
            pointsEarned = option.scoreDelta.coerceAtLeast(0),
            maxPointsPossible = maxStageScore,
            missedCriticalSafety = missedCritical
        )

        _uiState.value = current.copy(
            selectedOptionForCurrentStage = option,
            recordedLogs = current.recordedLogs + log
        )
    }

    fun advanceToNextStageOrDebrief() {
        val current = _uiState.value as? SimulatorUiState.ActiveSimulation ?: return
        val nextIndex = current.currentStageIndex + 1

        if (nextIndex < current.scenario.stages.size) {
            _uiState.value = current.copy(
                currentStageIndex = nextIndex,
                selectedOptionForCurrentStage = null
            )
        } else {
            val totalEarned = current.recordedLogs.sumOf { it.pointsEarned }
            val maxPossible = current.recordedLogs.sumOf { it.maxPointsPossible }.coerceAtLeast(1)
            val percentage = ((totalEarned.toDouble() / maxPossible.toDouble()) * 100).toInt().coerceIn(0, 100)
            val criticalViolations = current.recordedLogs
                .filter { it.missedCriticalSafety }
                .map { "${it.stageType.titleEn}: Missed critical safety action (${it.rationale})" }

            val report = SimulationDebriefReport(
                scenarioId = current.scenario.id,
                scenarioTitle = current.scenario.title,
                totalScoreEarned = totalEarned,
                maxPossibleScore = maxPossible,
                percentageScore = percentage,
                criticalSafetyViolations = criticalViolations,
                stageLogs = current.recordedLogs,
                completedAtEpochMillis = System.currentTimeMillis()
            )

            viewModelScope.launch {
                val uid = authRepository.requireCurrentUserId()
                simulatorRepository.recordSimulationDebrief(uid, report)
                _uiState.value = SimulatorUiState.Debriefing(report)
            }
        }
    }

    fun returnToScenarioCatalog() {
        _uiState.value = SimulatorUiState.ScenarioSelection
    }
}
