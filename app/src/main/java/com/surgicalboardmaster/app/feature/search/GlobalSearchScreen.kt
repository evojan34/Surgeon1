package com.surgicalboardmaster.app.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surgicalboardmaster.app.core.ui.components.SectionHeader
import com.surgicalboardmaster.app.core.ui.components.StatusBadge

@Composable
fun GlobalSearchScreen(
    viewModel: GlobalSearchViewModel,
    isArabicRtl: Boolean
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = if (isArabicRtl) "البحث الشامل (Global Search)" else "Global Surgical Search",
            subtitle = "Instantly search across authorized reference books, AI lessons, and clinical cases"
        )

        OutlinedTextField(
            value = query,
            onValueChange = viewModel::updateQuery,
            label = { Text("Search surgical topics, chapters, keywords...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (query.isBlank()) {
            Text(
                text = "Type any keyword (e.g., 'Mirizzi', 'Cholescystitis', 'Pancreatitis') to search your workstation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Found ${results.size} matches for '$query':",
                style = MaterialTheme.typography.titleMedium
            )
            results.forEach { result ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        StatusBadge(text = result.sourceCategory, color = MaterialTheme.colorScheme.secondary)
                        Text(result.title, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text(result.snippet, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
