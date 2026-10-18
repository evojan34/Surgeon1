package com.surgicalboardmaster.app.feature.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.surgicalboardmaster.app.core.ui.components.MetricStatCard
import com.surgicalboardmaster.app.core.ui.components.SectionHeader
import com.surgicalboardmaster.app.core.ui.theme.ClinicalSuccess

@Composable
fun StudyAnalyticsScreen(
    isArabicRtl: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = if (isArabicRtl) "تحليلات الأداء الدراسي (Study Analytics)" else "Board Study Analytics",
            subtitle = "Syllabus coverage progress • Exam score trends • Spaced repetition mastery"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = "Total Topics Mastered",
                value = "29 / 78",
                subtitle = "Bailey & Love & Schwartz",
                icon = Icons.Default.CheckCircleOutline,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = "Average Mock Exam",
                value = "84%",
                subtitle = "Last 5 SBA Exams",
                icon = Icons.Default.Quiz,
                accentColor = ClinicalSuccess,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Surgical Syllabus Mastery Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("• Hepatobiliary Surgery: 78% Mastered", style = MaterialTheme.typography.bodyMedium)
                Text("• Upper Gastrointestinal: 64% Mastered", style = MaterialTheme.typography.bodyMedium)
                Text("• Colorectal Surgery: 52% Mastered", style = MaterialTheme.typography.bodyMedium)
                Text("• Endocrine & Breast: 45% Mastered", style = MaterialTheme.typography.bodyMedium)
                Text("• Trauma & Critical Care: 30% Mastered", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
