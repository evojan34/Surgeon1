package com.surgicalboardmaster.app.feature.clinicalcases

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surgicalboardmaster.app.core.ui.components.ClinicalSafetyBanner
import com.surgicalboardmaster.app.core.ui.components.SectionHeader
import com.surgicalboardmaster.app.core.ui.components.StatusBadge
import com.surgicalboardmaster.app.core.ui.theme.ClinicalDanger
import com.surgicalboardmaster.app.core.ui.theme.ClinicalInfo
import com.surgicalboardmaster.app.core.ui.theme.ClinicalSuccess
import com.surgicalboardmaster.app.domain.model.CaseCategory
import com.surgicalboardmaster.app.domain.model.CaseStatus
import com.surgicalboardmaster.app.domain.model.ClinicalCaseDetail

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClinicalCasesScreen(
    viewModel: ClinicalCasesViewModel,
    isArabicRtl: Boolean
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val feedback by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    var showNewCaseDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ClinicalSafetyBanner()

        if (!feedback.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(feedback.orEmpty(), modifier = Modifier.weight(1f))
                    TextButton(onClick = viewModel::clearFeedback) { Text("Dismiss") }
                }
            }
        }

        when (val state = uiState) {
            is ClinicalCasesUiState.Catalog -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = if (isArabicRtl) "إدارة الحالات السريرية الخاصة" else "Private Clinical Case Log",
                        subtitle = "Pseudonymous IDs • Zero PII • Append-only audit timeline",
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(onClick = { showNewCaseDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabicRtl) "حالة جديدة" else "New Case")
                    }
                }

                OutlinedTextField(
                    value = state.filter.searchQuery,
                    onValueChange = { query -> viewModel.updateFilter(state.filter.copy(searchQuery = query)) },
                    label = { Text("Search by Pseudonymous ID, Chief Complaint, Diagnosis") },
                    modifier = Modifier.fillMaxWidth()
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = state.filter.category == null,
                        onClick = { viewModel.updateFilter(state.filter.copy(category = null)) },
                        label = { Text("All Categories") }
                    )
                    CaseCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = state.filter.category == cat,
                            onClick = { viewModel.updateFilter(state.filter.copy(category = cat)) },
                            label = { Text(cat.name) }
                        )
                    }
                }

                if (state.cases.isEmpty()) {
                    Text(
                        text = "No clinical cases logged. Tap 'New Case' to create a private or educational record.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                state.cases.forEach { detail ->
                    ClinicalCaseSummaryCard(
                        detail = detail,
                        onClick = { viewModel.selectCase(detail.caseId) }
                    )
                }
            }

            is ClinicalCasesUiState.Detail -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { viewModel.selectCase(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back to Catalog")
                    }
                    Row {
                        IconButton(onClick = { viewModel.exportCase(state.caseDetail.caseId) }) {
                            Icon(Icons.Default.Share, contentDescription = "Export De-Identified Summary")
                        }
                        IconButton(onClick = { viewModel.archiveCase(state.caseDetail.caseId, !state.caseDetail.isArchived) }) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive Case")
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusBadge(text = state.caseDetail.pseudonymousId, color = MaterialTheme.colorScheme.secondary)
                            StatusBadge(text = state.caseDetail.status.displayLabel, color = ClinicalInfo)
                        }

                        Text(text = state.caseDetail.chiefComplaint, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = "Diagnosis: ${state.caseDetail.finalDiagnosis.ifBlank { "Pending working diagnosis" }}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Treatment Plan: ${state.caseDetail.currentTreatmentPlan}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Next Action: ${state.caseDetail.nextAction}", style = MaterialTheme.typography.bodyMedium, color = ClinicalSuccess)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    if (showNewCaseDialog) {
        NewClinicalCaseDialog(
            onDismiss = { showNewCaseDialog = false },
            onSave = { detail ->
                viewModel.saveCase(detail)
                showNewCaseDialog = false
            }
        )
    }
}

@Composable
private fun ClinicalCaseSummaryCard(
    detail: ClinicalCaseDetail,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusBadge(text = detail.pseudonymousId, color = MaterialTheme.colorScheme.secondary)
                StatusBadge(text = detail.status.displayLabel, color = ClinicalInfo)
            }
            Text(text = detail.chiefComplaint, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Diagnosis: ${detail.finalDiagnosis.ifBlank { "Pending" }}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NewClinicalCaseDialog(
    onDismiss: () -> Unit,
    onSave: (ClinicalCaseDetail) -> Unit
) {
    var pseudonymousId by remember { mutableStateOf("SBM-2026-HPB-099") }
    var ageRange by remember { mutableStateOf("50-59y") }
    var chiefComplaint by remember { mutableStateOf("Acute abdominal pain") }
    var diagnosis by remember { mutableStateOf("Suspected Acute Appendicitis") }
    var plan by remember { mutableStateOf("Diagnostic laparoscopy scheduled") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Clinical Record (Privacy Guard Enforced)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ensure no patient names, phone numbers, or national IDs are entered.", style = MaterialTheme.typography.labelMedium, color = ClinicalDanger)
                OutlinedTextField(value = pseudonymousId, onValueChange = { pseudonymousId = it }, label = { Text("Pseudonymous ID (e.g. SBM-2026-001)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ageRange, onValueChange = { ageRange = it }, label = { Text("Age Range (e.g. 40-49y)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = chiefComplaint, onValueChange = { chiefComplaint = it }, label = { Text("Chief Complaint") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = diagnosis, onValueChange = { diagnosis = it }, label = { Text("Working / Final Diagnosis") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = plan, onValueChange = { plan = it }, label = { Text("Current Treatment Plan") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val detail = ClinicalCaseDetail(
                    caseId = "",
                    userId = "",
                    pseudonymousId = pseudonymousId,
                    category = CaseCategory.PRIVATE_CLINICAL_RECORD,
                    ageRange = ageRange,
                    chiefComplaint = chiefComplaint,
                    finalDiagnosis = diagnosis,
                    currentTreatmentPlan = plan,
                    createdAtEpochMillis = System.currentTimeMillis(),
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
                onSave(detail)
            }) {
                Text("Save Case")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
