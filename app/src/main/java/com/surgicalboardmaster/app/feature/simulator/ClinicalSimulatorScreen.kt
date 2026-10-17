package com.surgicalboardmaster.app.feature.simulator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surgicalboardmaster.app.core.ui.components.SectionHeader
import com.surgicalboardmaster.app.core.ui.components.StatusBadge
import com.surgicalboardmaster.app.core.ui.theme.ClinicalDanger
import com.surgicalboardmaster.app.core.ui.theme.ClinicalInfo
import com.surgicalboardmaster.app.core.ui.theme.ClinicalSuccess
import com.surgicalboardmaster.app.core.ui.theme.ClinicalWarning
import com.surgicalboardmaster.app.domain.model.SimulationDebriefReport
import com.surgicalboardmaster.app.domain.model.SimulationScenario
import com.surgicalboardmaster.app.domain.model.SurgicalDomain

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClinicalSimulatorScreen(
    viewModel: ClinicalSimulatorViewModel,
    isArabicRtl: Boolean
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scenarios by viewModel.availableScenarios.collectAsStateWithLifecycle()
    val selectedDomain by viewModel.selectedDomainFilter.collectAsStateWithLifecycle()
    val completedDebriefs by viewModel.completedDebriefs.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (val state = uiState) {
            is SimulatorUiState.ScenarioSelection -> {
                SectionHeader(
                    title = if (isArabicRtl) "المحاكي السريري التفاعلي (Clinical Case Simulator)" else "Interactive Clinical Case Simulator",
                    subtitle = "Progressive case reveal • Emergency prioritization • Evidence-based operative decisions"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedDomain == null,
                        onClick = { viewModel.setDomainFilter(null) },
                        label = { Text("All Surgical Domains") }
                    )
                    SurgicalDomain.entries.forEach { domain ->
                        FilterChip(
                            selected = selectedDomain == domain,
                            onClick = { viewModel.setDomainFilter(domain) },
                            label = { Text(domain.displayLabel) }
                        )
                    }
                }

                scenarios.forEach { scenario ->
                    ScenarioCatalogCard(
                        scenario = scenario,
                        isArabicRtl = isArabicRtl,
                        onStart = { viewModel.startScenario(scenario) }
                    )
                }

                if (completedDebriefs.isNotEmpty()) {
                    SectionHeader(
                        title = if (isArabicRtl) "سجل المحاكاة السابقة" else "Completed Simulation Debriefs",
                        subtitle = "Review clinical reasoning scores and safety checkpoints"
                    )
                    completedDebriefs.forEach { report ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(report.scenarioTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Safety Violations: ${report.criticalSafetyViolations.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(
                                    text = "${report.percentageScore}%",
                                    color = if (report.percentageScore >= 75) ClinicalSuccess else ClinicalWarning
                                )
                            }
                        }
                    }
                }
            }

            is SimulatorUiState.ActiveSimulation -> {
                ActiveSimulationStageView(
                    state = state,
                    isArabicRtl = isArabicRtl,
                    onSelectOption = viewModel::chooseStageAction,
                    onNextStage = viewModel::advanceToNextStageOrDebrief,
                    onAbort = viewModel::returnToScenarioCatalog
                )
            }

            is SimulatorUiState.Debriefing -> {
                SimulationDebriefView(
                    report = state.report,
                    isArabicRtl = isArabicRtl,
                    onDone = viewModel::returnToScenarioCatalog
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ScenarioCatalogCard(
    scenario: SimulationScenario,
    isArabicRtl: Boolean,
    onStart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(text = scenario.domain.displayLabel, color = MaterialTheme.colorScheme.secondary)
                StatusBadge(text = "${scenario.stages.size} Progressive Stages", color = ClinicalInfo)
            }
            Text(scenario.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(scenario.patientSummaryBanner, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(scenario.educationalDisclaimer, style = MaterialTheme.typography.labelMedium, color = ClinicalWarning)
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isArabicRtl) "بدء المحاكاة السريرية" else "Launch Interactive Simulation")
            }
        }
    }
}

@Composable
private fun ActiveSimulationStageView(
    state: SimulatorUiState.ActiveSimulation,
    isArabicRtl: Boolean,
    onSelectOption: (com.surgicalboardmaster.app.domain.model.SimulationActionOption) -> Unit,
    onNextStage: () -> Unit,
    onAbort: () -> Unit
) {
    val stage = state.scenario.stages[state.currentStageIndex]
    val progress = (state.currentStageIndex + 1).toFloat() / state.scenario.stages.size.toFloat()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusBadge(
            text = "Stage ${state.currentStageIndex + 1}/${state.scenario.stages.size}: ${if (isArabicRtl) stage.stageType.titleAr else stage.stageType.titleEn}",
            color = MaterialTheme.colorScheme.secondary
        )
        OutlinedButton(onClick = onAbort) {
            Text("Exit")
        }
    }

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
        color = MaterialTheme.colorScheme.secondary
    )

    // Vital Signs Monitor Banner
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "MONITOR • ${stage.vitalSignsBanner}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )
    }

    // Progressive Clinical Narrative
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = stage.progressiveCaseNarrative, style = MaterialTheme.typography.bodyLarge)
            HorizontalDivider()
            Text(text = stage.promptQuestion, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            stage.availableActions.forEach { actionOption ->
                val isSelected = state.selectedOptionForCurrentStage?.optionId == actionOption.optionId
                val isCorrect = actionOption.optionId == stage.correctOptionId
                val borderColor = when {
                    state.selectedOptionForCurrentStage == null -> MaterialTheme.colorScheme.surfaceVariant
                    isSelected && isCorrect -> ClinicalSuccess
                    isSelected && !isCorrect -> ClinicalDanger
                    isCorrect -> ClinicalSuccess
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Card(
                    onClick = { onSelectOption(actionOption) },
                    enabled = state.selectedOptionForCurrentStage == null,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, borderColor),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${actionOption.optionId}. ${actionOption.actionText}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }

    // Progressive Outcome Reveal after User Commits Decision
    val chosen = state.selectedOptionForCurrentStage
    if (chosen != null) {
        val isOptimal = chosen.optionId == stage.correctOptionId
        Card(
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (isOptimal) ClinicalSuccess else ClinicalDanger),
            colors = CardDefaults.cardColors(
                containerColor = (if (isOptimal) ClinicalSuccess else ClinicalDanger).copy(alpha = 0.10f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(
                    text = if (isOptimal) "OPTIMAL CLINICAL DECISION" else "SUBOPTIMAL / UNSAFE ACTION",
                    color = if (isOptimal) ClinicalSuccess else ClinicalDanger
                )
                Text(
                    text = "Clinical Outcome: ${chosen.clinicalOutcomeReveal}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Board Rationale: ${chosen.rationale}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onNextStage, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (state.currentStageIndex + 1 < state.scenario.stages.size) {
                            "Proceed to Next Clinical Stage"
                        } else {
                            "Complete Simulation & View Debriefing"
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun SimulationDebriefView(
    report: SimulationDebriefReport,
    isArabicRtl: Boolean,
    onDone: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("CLINICAL SIMULATION DEBRIEFING", style = MaterialTheme.typography.labelMedium)
            Text("${report.percentageScore}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Points: ${report.totalScoreEarned} / ${report.maxPossibleScore} (Configurable Criteria Assessment)",
                style = MaterialTheme.typography.bodyMedium
            )
            FilledTonalButton(onClick = onDone) {
                Text(if (isArabicRtl) "العودة لقائمة السيناريوهات" else "Return to Simulator Catalog")
            }
        }
    }

    if (report.criticalSafetyViolations.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ClinicalDanger),
            colors = CardDefaults.cardColors(containerColor = ClinicalDanger.copy(alpha = 0.10f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = ClinicalDanger)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Critical Patient Safety Checkpoints Missed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ClinicalDanger)
                }
                report.criticalSafetyViolations.forEach { violation ->
                    Text("• $violation", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    SectionHeader(title = "Stage-by-Stage Clinical Reasoning Breakdown")
    report.stageLogs.forEach { log ->
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(log.stageType.titleEn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    StatusBadge(
                        text = "${log.pointsEarned}/${log.maxPointsPossible} pts",
                        color = if (!log.missedCriticalSafety && log.pointsEarned > 0) ClinicalSuccess else ClinicalDanger
                    )
                }
                Text("Action Taken: ${log.actionText}", style = MaterialTheme.typography.bodyMedium)
                Text("Outcome: ${log.clinicalOutcomeReveal}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                Text("Rationale: ${log.rationale}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
