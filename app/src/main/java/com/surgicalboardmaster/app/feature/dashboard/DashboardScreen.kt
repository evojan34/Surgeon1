package com.surgicalboardmaster.app.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surgicalboardmaster.app.core.navigation.SbmDestination
import com.surgicalboardmaster.app.core.ui.components.BookProgressCard
import com.surgicalboardmaster.app.core.ui.components.ClinicalSafetyBanner
import com.surgicalboardmaster.app.core.ui.components.MetricStatCard
import com.surgicalboardmaster.app.core.ui.components.OfflineStatusBanner
import com.surgicalboardmaster.app.core.ui.components.QuickActionChip
import com.surgicalboardmaster.app.core.ui.components.SectionHeader
import com.surgicalboardmaster.app.core.ui.components.StatusBadge
import com.surgicalboardmaster.app.core.ui.theme.ClinicalDanger
import com.surgicalboardmaster.app.core.ui.theme.ClinicalSuccess
import com.surgicalboardmaster.app.core.ui.theme.ClinicalWarning
import com.surgicalboardmaster.app.domain.model.DashboardSummary
import com.surgicalboardmaster.app.domain.model.FollowUpStatus
import com.surgicalboardmaster.app.domain.model.FollowUpTask
import com.surgicalboardmaster.app.domain.model.Topic

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    isArabicRtl: Boolean,
    isOffline: Boolean,
    onNavigate: (SbmDestination) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is DashboardUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is DashboardUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        is DashboardUiState.Success -> {
            DashboardContent(
                summary = state.summary,
                isArabicRtl = isArabicRtl,
                isOffline = isOffline,
                onNavigate = onNavigate,
                onCompleteFollowUp = viewModel::completeFollowUp,
                onMarkTopicRevised = viewModel::markTopicRevised
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashboardContent(
    summary: DashboardSummary,
    isArabicRtl: Boolean,
    isOffline: Boolean,
    onNavigate: (SbmDestination) -> Unit,
    onCompleteFollowUp: (String) -> Unit,
    onMarkTopicRevised: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        OfflineStatusBanner(isOffline = isOffline)
        ClinicalSafetyBanner()

        // Active Study Session Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (isArabicRtl) "جلسة الدراسة النشطة الآن" else "ACTIVE SURGICAL LESSON",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = summary.activeLessonTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summary.activeLessonBook,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(onClick = { onNavigate(SbmDestination.AI_TUTOR) }) {
                    Text(text = if (isArabicRtl) "متابعة الدرس (Resume Lesson)" else "Continue Studying")
                }
            }
        }

        // Repository-Driven Key Performance Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = if (isArabicRtl) "المواضيع المنجزة" else "Topics Mastered",
                value = "${summary.completedTopicsTotal}/${summary.totalTopicsCount}",
                subtitle = "Bailey & Love + Schwartz",
                icon = Icons.Default.CheckCircleOutline,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = if (isArabicRtl) "متوسط الامتحانات" else "Mock Exam Avg",
                value = "${summary.averageExamScorePercent}%",
                subtitle = "${summary.completedSimulationsCount} Clinical Sims",
                icon = Icons.Default.Quiz,
                accentColor = ClinicalSuccess,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = if (isArabicRtl) "حالاتي السريرية" else "Active Clinical Cases",
                value = "${summary.activeClinicalCasesCount}",
                subtitle = "Pseudonymous Private Log",
                icon = Icons.Default.FolderSpecial,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = if (isArabicRtl) "متابعات مستحقة" else "Pending Follow-ups",
                value = "${summary.pendingFollowUps.size}",
                subtitle = "${summary.pendingFollowUps.count { it.status == FollowUpStatus.OVERDUE }} Overdue",
                icon = Icons.Default.EventAvailable,
                accentColor = if (summary.pendingFollowUps.any { it.status == FollowUpStatus.OVERDUE }) {
                    ClinicalDanger
                } else {
                    ClinicalSuccess
                },
                modifier = Modifier.weight(1f)
            )
        }

        // 7 Required Quick Actions
        SectionHeader(
            title = if (isArabicRtl) "الإجراءات السريعة (Quick Actions)" else "Quick Workstation Actions",
            subtitle = if (isArabicRtl) "انتقل مباشرة إلى مهام البورد والممارسة السريرية" else "Direct access to board study, simulations, and clinical tracking"
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionChip(
                label = if (isArabicRtl) "متابعة الدراسة" else "Continue studying",
                icon = Icons.Default.PlayArrow,
                onClick = { onNavigate(SbmDestination.AI_TUTOR) }
            )
            QuickActionChip(
                label = if (isArabicRtl) "درس جديد" else "Start a new lesson",
                icon = Icons.Default.AutoAwesome,
                onClick = { onNavigate(SbmDestination.AI_TUTOR) }
            )
            QuickActionChip(
                label = if (isArabicRtl) "امتحان تجريبي" else "Start a mock examination",
                icon = Icons.Default.Quiz,
                onClick = { onNavigate(SbmDestination.EXAM_CENTER) }
            )
            QuickActionChip(
                label = if (isArabicRtl) "محاكاة سريرية" else "Start a clinical simulation",
                icon = Icons.Default.MedicalServices,
                onClick = { onNavigate(SbmDestination.CLINICAL_SIMULATOR) }
            )
            QuickActionChip(
                label = if (isArabicRtl) "إضافة حالة سريرية" else "Add a clinical case",
                icon = Icons.Default.AddCircleOutline,
                onClick = { onNavigate(SbmDestination.MY_CLINICAL_CASES) }
            )
            QuickActionChip(
                label = if (isArabicRtl) "المتابعات المعلقة" else "View pending follow-ups",
                icon = Icons.Default.EventAvailable,
                onClick = { onNavigate(SbmDestination.FOLLOW_UP_CALENDAR) }
            )
            QuickActionChip(
                label = if (isArabicRtl) "مراجعة نقاط الضعف" else "Review weak topics",
                icon = Icons.Default.Psychology,
                onClick = { onNavigate(SbmDestination.SURGICAL_LIBRARY) }
            )
        }

        // Core Reference Books Progress
        SectionHeader(
            title = if (isArabicRtl) "المراجع المعتمدة (Core Surgical References)" else "Authorized Reference Progress",
            subtitle = "Custom chapter sequences and board topic mastery"
        )

        summary.books.forEach { book ->
            BookProgressCard(
                title = book.title,
                edition = book.edition,
                completedTopics = book.completedTopicsCount,
                totalTopics = book.totalTopicsCount,
                lastStudiedChapter = book.activeChapterTitle,
                onContinueClick = { onNavigate(SbmDestination.SURGICAL_LIBRARY) }
            )
        }

        // Pending Clinical Follow-Ups
        SectionHeader(
            title = if (isArabicRtl) "متابعات الحالات السريرية (Pending Follow-ups)" else "Pending Clinical Follow-ups",
            subtitle = "Zero-PII pseudonymous identifiers • Append-only clinical timeline linked"
        )

        if (summary.pendingFollowUps.isEmpty()) {
            Text(
                text = "All clinical follow-ups are completed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            summary.pendingFollowUps.forEach { task ->
                FollowUpTaskRow(
                    task = task,
                    onMarkDone = { onCompleteFollowUp(task.taskId) }
                )
            }
        }

        // Weak Topics Needing Revision
        SectionHeader(
            title = if (isArabicRtl) "مواضيع تحتاج مراجعة (Weak Topics)" else "High-Yield Topics Needing Revision",
            subtitle = "Prioritized by exam distractor analysis and confidence score"
        )

        summary.weakTopics.forEach { topic ->
            WeakTopicRow(
                topic = topic,
                onMarkRevised = { onMarkTopicRevised(topic.id) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FollowUpTaskRow(
    task: FollowUpTask,
    onMarkDone: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatusBadge(
                    text = task.pseudonymousCaseId,
                    color = MaterialTheme.colorScheme.secondary
                )
                StatusBadge(
                    text = if (task.status == FollowUpStatus.OVERDUE) "OVERDUE • ${task.priority.displayLabel}" else task.priority.displayLabel,
                    color = if (task.status == FollowUpStatus.OVERDUE) ClinicalDanger else ClinicalWarning
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FilledTonalButton(onClick = onMarkDone) {
                Text(text = "Mark Follow-up Completed")
            }
        }
    }
}

@Composable
private fun WeakTopicRow(
    topic: Topic,
    onMarkRevised: () -> Unit
) {
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
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${topic.bookTitle} • ${topic.difficulty.displayLabel} • Confidence: ${topic.confidenceScore}/5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(onClick = onMarkRevised) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Mark Revised"
                )
            }
        }
    }
}

