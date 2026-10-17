package com.surgicalboardmaster.app.feature.exams

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import com.surgicalboardmaster.app.domain.model.ExamOption
import com.surgicalboardmaster.app.domain.model.ExamQuestion

@Composable
fun ExamCenterScreen(
    viewModel: ExamCenterViewModel,
    isArabicRtl: Boolean
) {
    val flowState by viewModel.flowState.collectAsStateWithLifecycle()
    val wrongNotebook by viewModel.wrongAnswers.collectAsStateWithLifecycle()
    val pastAttempts by viewModel.pastAttempts.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (val state = flowState) {
            is ExamFlowState.Configuration -> {
                ExamConfigurationView(
                    isArabicRtl = isArabicRtl,
                    onStartExam = { isTimed -> viewModel.startExam(isTimed = isTimed) }
                )

                if (wrongNotebook.isNotEmpty()) {
                    SectionHeader(
                        title = if (isArabicRtl) "دفتر الإجابات الخاطئة (Wrong-Answer Notebook)" else "Wrong-Answer Notebook",
                        subtitle = "Targeted spaced repetition for previously missed board questions"
                    )
                    wrongNotebook.forEach { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                StatusBadge(text = "Failed ${item.failureCount}x • ${item.question.topicTitle}", color = ClinicalDanger)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(item.question.clinicalVignette, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Correct: Option ${item.question.correctAnswerId}", fontWeight = FontWeight.Bold, color = ClinicalSuccess)
                            }
                        }
                    }
                }
            }

            is ExamFlowState.InProgress -> {
                val currentQ = state.questions[state.currentIndex]
                val selectedOption = state.userAnswers[currentQ.id]
                val progress = (state.currentIndex + 1).toFloat() / state.questions.size.toFloat()

                // Progress Bar & Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Question ${state.currentIndex + 1} of ${state.questions.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    StatusBadge(text = "${state.remainingSeconds / 60}m remaining", color = ClinicalWarning)
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = MaterialTheme.colorScheme.secondary
                )

                // Question Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatusBadge(text = "${currentQ.questionType.displayLabel} • ${currentQ.difficulty.displayLabel}", color = MaterialTheme.colorScheme.secondary)
                        Text(text = currentQ.clinicalVignette, style = MaterialTheme.typography.bodyLarge)
                        Text(text = currentQ.questionPrompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        HorizontalDivider()

                        currentQ.options.forEach { opt ->
                            OptionRow(
                                option = opt,
                                isSelected = selectedOption == opt.id,
                                onSelect = { viewModel.selectAnswer(currentQ.id, opt.id) }
                            )
                        }
                    }
                }

                // Nav Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = viewModel::previousQuestion, enabled = state.currentIndex > 0) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev")
                    }

                    if (state.currentIndex + 1 == state.questions.size) {
                        Button(onClick = viewModel::submitExam) {
                            Text("Submit Exam")
                        }
                    } else {
                        Button(onClick = viewModel::nextQuestion) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }

            is ExamFlowState.Review -> {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("EXAMINATION RESULTS", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${state.attempt.scorePercentage}%", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text("${state.attempt.correctCount} of ${state.attempt.totalQuestionsCount} correct", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        FilledTonalButton(onClick = viewModel::resetToConfiguration) {
                            Text("Return to Exam Center")
                        }
                    }
                }

                SectionHeader(title = "Detailed Question Debriefing & Rationales")
                state.questions.forEachIndexed { idx, q ->
                    val userSub = state.attempt.submissions.find { it.questionId == q.id }
                    val isCorrect = userSub?.isCorrect == true
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (isCorrect) ClinicalSuccess else ClinicalDanger),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusBadge(
                                text = if (isCorrect) "Q${idx + 1}: CORRECT" else "Q${idx + 1}: INCORRECT (Chose ${userSub?.selectedOptionId})",
                                color = if (isCorrect) ClinicalSuccess else ClinicalDanger
                            )
                            Text(q.questionPrompt, fontWeight = FontWeight.Bold)
                            Text(q.comprehensiveExplanation, style = MaterialTheme.typography.bodyMedium)
                            Text("Pearl: ${q.boardPearls}", style = MaterialTheme.typography.labelMedium, color = ClinicalWarning)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun ExamConfigurationView(
    isArabicRtl: Boolean,
    onStartExam: (isTimed: Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabicRtl) "مركز الامتحانات التجريبية (Board Exam Center)" else "Board Examination Simulator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Single Best Answer (SBA) questions with verified provenance from Bailey & Love (28th Ed) and Schwartz (11th Ed). Answer keys are verified upon submission.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { onStartExam(true) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.HourglassBottom, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabicRtl) "امتحان مؤقت (Timed)" else "Timed Exam")
                }
                OutlinedButton(onClick = { onStartExam(false) }, modifier = Modifier.weight(1f)) {
                    Text(if (isArabicRtl) "تدريب حر (Practice)" else "Practice Mode")
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    option: ExamOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onSelect)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "${option.id}. ${option.text}", style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
