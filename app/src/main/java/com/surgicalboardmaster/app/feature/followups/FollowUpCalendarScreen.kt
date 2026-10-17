package com.surgicalboardmaster.app.feature.followups

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
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
import com.surgicalboardmaster.app.core.ui.theme.ClinicalSuccess
import com.surgicalboardmaster.app.core.ui.theme.ClinicalWarning
import com.surgicalboardmaster.app.domain.model.FollowUpStatus
import com.surgicalboardmaster.app.domain.model.FollowUpTask

@Composable
fun FollowUpCalendarScreen(
    viewModel: FollowUpCalendarViewModel,
    isArabicRtl: Boolean
) {
    val tasks by viewModel.followUpTasks.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = if (isArabicRtl) "تقويم ومتابعات الحالات السريرية" else "Clinical Follow-up Calendar",
            subtitle = "Zero-PII local scheduling • WorkManager notifications"
        )

        if (tasks.isEmpty()) {
            Text(
                text = "No pending or overdue follow-up tasks. All clinical cases are up to date.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        tasks.forEach { task ->
            FollowUpCalendarCard(
                task = task,
                onComplete = { viewModel.markCompleted(task.taskId) }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun FollowUpCalendarCard(
    task: FollowUpTask,
    onComplete: () -> Unit
) {
    val isOverdue = task.status == FollowUpStatus.OVERDUE
    val statusColor = when (task.status) {
        FollowUpStatus.OVERDUE -> ClinicalDanger
        FollowUpStatus.COMPLETED -> ClinicalSuccess
        else -> ClinicalWarning
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isOverdue) ClinicalDanger else MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(text = task.pseudonymousCaseId, color = MaterialTheme.colorScheme.secondary)
                StatusBadge(text = task.status.name, color = statusColor)
            }
            Text(text = task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = task.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            FilledTonalButton(onClick = onComplete) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Mark Completed")
            }
        }
    }
}
