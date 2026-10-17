package com.surgicalboardmaster.app.feature.settings

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.surgicalboardmaster.app.core.ui.components.ClinicalSafetyBanner
import com.surgicalboardmaster.app.core.ui.components.SectionHeader
import com.surgicalboardmaster.app.core.ui.components.StatusBadge
import com.surgicalboardmaster.app.core.ui.theme.ClinicalSuccess

@Composable
fun SettingsScreen(
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
            title = if (isArabicRtl) "الإعدادات والخصوصية وتأمين البيانات" else "Settings, Privacy & Security",
            subtitle = "Data retention • Encryption policies • Offline workstation rules"
        )

        ClinicalSafetyBanner()

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
                    Text("Application Version", fontWeight = FontWeight.Bold)
                    StatusBadge(text = "0.2.0-RELEASE", color = ClinicalSuccess)
                }
                Text("Core Authorized References: Bailey & Love (28th Ed) & Schwartz (11th Ed)", style = MaterialTheme.typography.bodyMedium)
                Text("Database Encryption: Room Encrypted SQLite via Android Keystore", style = MaterialTheme.typography.bodyMedium)
                Text("Cloud Synchronization: Firebase Firestore (User-Scoped Subcollections)", style = MaterialTheme.typography.bodyMedium)
                Text("AI Privacy Policy: Clinical records are NEVER sent to external AI by default.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
