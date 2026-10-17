package com.surgicalboardmaster.app.feature.flashcards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.surgicalboardmaster.app.domain.model.ReviewRating

@Composable
fun FlashcardsScreen(
    viewModel: FlashcardsViewModel,
    isArabicRtl: Boolean
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = if (isArabicRtl) "البطاقات التكرارية الذكية (Spaced Repetition)" else "Active Spaced Repetition Deck",
            subtitle = "SM-2 Algorithm scheduling • Focus on high-yield recall"
        )

        if (state.isDeckCompleted || state.activeDeck.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Style, contentDescription = null, tint = ClinicalSuccess, modifier = Modifier.height(48.dp).width(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("All Due Cards Mastered!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("No further cards scheduled for review today. New cards are added as you study AI lessons.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = viewModel::loadDueCards) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reload Deck")
                    }
                }
            }
        } else {
            val card = state.activeDeck[state.currentCardIndex]
            val progress = (state.currentCardIndex + 1).toFloat() / state.activeDeck.size.toFloat()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Card ${state.currentCardIndex + 1} of ${state.activeDeck.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                StatusBadge(text = "Interval: ${card.intervalDays}d", color = MaterialTheme.colorScheme.secondary)
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.secondary
            )

            // Flip Card Surface
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.flipCard() }
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatusBadge(text = card.topicTitle, color = MaterialTheme.colorScheme.secondary)
                    Text(text = card.frontPrompt, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)

                    if (state.isAnswerFlipped) {
                        HorizontalDivider()
                        Text("CORE ANSWER:", style = MaterialTheme.typography.labelMedium, color = ClinicalSuccess)
                        Text(text = card.backAnswer, style = MaterialTheme.typography.bodyLarge)
                        if (card.clinicalPearl.isNotBlank()) {
                            Text("PEARL: ${card.clinicalPearl}", style = MaterialTheme.typography.bodyMedium, color = ClinicalWarning)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(30.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Tap to reveal board answer", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            // Rating Buttons (SM-2)
            if (state.isAnswerFlipped) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(onClick = { viewModel.answerCard(ReviewRating.AGAIN) }, modifier = Modifier.weight(1f)) {
                        Text("Again", color = ClinicalDanger)
                    }
                    OutlinedButton(onClick = { viewModel.answerCard(ReviewRating.HARD) }, modifier = Modifier.weight(1f)) {
                        Text("Hard", color = ClinicalWarning)
                    }
                    Button(onClick = { viewModel.answerCard(ReviewRating.GOOD) }, modifier = Modifier.weight(1f)) {
                        Text("Good")
                    }
                    Button(onClick = { viewModel.answerCard(ReviewRating.EASY) }, modifier = Modifier.weight(1f)) {
                        Text("Easy")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
