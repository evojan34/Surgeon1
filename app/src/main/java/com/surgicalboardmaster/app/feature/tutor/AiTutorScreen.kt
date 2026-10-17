package com.surgicalboardmaster.app.feature.tutor

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.surgicalboardmaster.app.core.ui.components.SectionHeader
import com.surgicalboardmaster.app.core.ui.components.StatusBadge
import com.surgicalboardmaster.app.core.ui.theme.ClinicalDanger
import com.surgicalboardmaster.app.core.ui.theme.ClinicalInfo
import com.surgicalboardmaster.app.core.ui.theme.ClinicalSuccess
import com.surgicalboardmaster.app.core.ui.theme.ClinicalWarning
import com.surgicalboardmaster.app.domain.model.LessonFormat
import com.surgicalboardmaster.app.domain.model.StructuredLesson

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiTutorScreen(
    viewModel: AiTutorViewModel,
    isArabicRtl: Boolean,
    onStartExamForTopic: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedLessons by viewModel.savedLessons.collectAsStateWithLifecycle()
    val feedbackMsg by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    var selectedFormat by remember { mutableStateOf(LessonFormat.BOARD_EXAM_PREP) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!feedbackMsg.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(feedbackMsg.orEmpty(), modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = viewModel::clearFeedback) { Text("OK") }
                }
            }
        }

        // Lesson Generator Configuration Bar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabicRtl) "المدرّس الجراحي المتقدم (AI Board Tutor)" else "AI Surgical Board Tutor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Generates high-yield, structured lessons strictly from Bailey & Love (28th Ed) and Schwartz (11th Ed). Caches locally to avoid repeated token costs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(if (isArabicRtl) "اختر نمط الدرس:" else "Select Lesson Teaching Format:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LessonFormat.entries.take(4).forEach { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            label = { Text(format.displayLabel) }
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.startLesson(
                            topicId = "topic_mirizzi",
                            bookTitle = "Bailey & Love's Short Practice of Surgery (28th Ed)",
                            chapterTitle = "Ch 67: The Gallbladder and Bile Ducts",
                            topicTitle = "Mirizzi Syndrome & Strasberg Critical View of Safety",
                            format = selectedFormat
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabicRtl) "بدء دراسة موضوع الغرفة الصفراوية والمرارة" else "Start High-Yield Board Lesson (HPB Surgery)")
                }
            }
        }

        // Active State Dispatcher
        when (val state = uiState) {
            is TutorUiState.Generating -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Synthesizing verified surgical textbook content & algorithms...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            is TutorUiState.Error -> {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(text = state.message, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            is TutorUiState.LessonReady -> {
                StructuredLessonViewer(
                    lesson = state.lesson,
                    isArabicRtl = isArabicRtl,
                    onToggleBookmark = { viewModel.toggleBookmark(state.lesson.id, state.lesson.isBookmarked) },
                    onSaveNotes = { notes -> viewModel.saveNotes(state.lesson.id, notes) },
                    onGenerateFlashcards = viewModel::generateFlashcardsFromActiveLesson,
                    onStartQuiz = { onStartExamForTopic(state.lesson.topicId) }
                )
            }
            TutorUiState.Idle -> {
                if (savedLessons.isNotEmpty()) {
                    SectionHeader(
                        title = if (isArabicRtl) "الدروس المحفوظة في محطتك" else "Cached & Saved Lessons",
                        subtitle = "Reopen any generated lesson offline without consuming tokens"
                    )
                    savedLessons.forEach { saved ->
                        SavedLessonRow(
                            lesson = saved,
                            onOpen = { viewModel.openSavedLesson(saved) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun StructuredLessonViewer(
    lesson: StructuredLesson,
    isArabicRtl: Boolean,
    onToggleBookmark: () -> Unit,
    onSaveNotes: (String) -> Unit,
    onGenerateFlashcards: () -> Unit,
    onStartQuiz: () -> Unit
) {
    var notesText by remember { mutableStateOf(lesson.userPersonalNotes) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(text = lesson.format.displayLabel, color = MaterialTheme.colorScheme.secondary)
                Row {
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (lesson.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Lesson",
                            tint = if (lesson.isBookmarked) ClinicalWarning else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(onClick = onGenerateFlashcards) {
                        Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Make Cards")
                    }
                }
            }

            Text(text = lesson.topicTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = "${lesson.bookTitle} • ${lesson.chapterTitle}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)

            HorizontalDivider()

            // Learning Objectives
            LessonBlock(title = "1. Learning Objectives", color = ClinicalInfo) {
                lesson.learningObjectives.forEach { obj ->
                    Text(text = "• $obj", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Relevant Surgical Anatomy
            LessonBlock(title = "2. Surgical Anatomy & Boundaries", color = MaterialTheme.colorScheme.secondary) {
                Text(text = lesson.relevantAnatomy, style = MaterialTheme.typography.bodyMedium)
            }

            // Pathophysiology & Etiology
            LessonBlock(title = "3. Pathophysiology & Mechanisms", color = MaterialTheme.colorScheme.primary) {
                Text(text = lesson.pathophysiology, style = MaterialTheme.typography.bodyMedium)
            }

            // Clinical Presentation & Signs
            LessonBlock(title = "4. Clinical Presentation & Signs", color = ClinicalInfo) {
                Text(text = lesson.clinicalPresentation, style = MaterialTheme.typography.bodyMedium)
            }

            // Investigations & Diagnostic Algorithms
            LessonBlock(title = "5. Investigations & Tokyo Grading", color = ClinicalSuccess) {
                Text(text = lesson.investigationsAndInterpretation, style = MaterialTheme.typography.bodyMedium)
            }

            // Management Algorithms & Bail-Out Rules
            LessonBlock(title = "6. Operative Principles & Bail-Out Strategies", color = ClinicalDanger) {
                Text(text = lesson.operativePrinciples, style = MaterialTheme.typography.bodyMedium)
            }

            // Board Examination Pearls
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicalWarning.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, ClinicalWarning),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = ClinicalWarning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Board Examination Pearls (High-Yield)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ClinicalWarning)
                    }
                    lesson.boardPearls.forEach { pearl ->
                        Text("★ $pearl", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Common Mistakes & Traps
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicalDanger.copy(alpha = 0.10f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = ClinicalDanger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dangerous Pitfalls & Board Traps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ClinicalDanger)
                    }
                    lesson.commonMistakes.forEach { mistake ->
                        Text("✕ $mistake", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Personal Notes Input
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("My Personal Revision Notes & Observations") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { onSaveNotes(notesText) }) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Notes")
            }

            // Bottom Actions (Quiz generation)
            FilledTonalButton(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth()) {
                Text("Test Mastery: Launch Topic Exam Questions")
            }
        }
    }
}

@Composable
private fun LessonBlock(
    title: String,
    color: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        content()
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun SavedLessonRow(lesson: StructuredLesson, onOpen: () -> Unit) {
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = lesson.topicTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${lesson.bookTitle} • ${lesson.format.displayLabel}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.PlayArrow, contentDescription = "Open Lesson", tint = MaterialTheme.colorScheme.secondary)
        }
    }
}
