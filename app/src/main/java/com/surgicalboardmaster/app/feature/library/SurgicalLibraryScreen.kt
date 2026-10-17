package com.surgicalboardmaster.app.feature.library

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.surgicalboardmaster.app.domain.model.Book
import com.surgicalboardmaster.app.domain.model.Chapter
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.StudyStatus
import com.surgicalboardmaster.app.domain.model.Topic

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SurgicalLibraryScreen(
    viewModel: SurgicalLibraryViewModel,
    isArabicRtl: Boolean,
    onStartTutorForTopic: (Topic) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddBookDialog by remember { mutableStateOf(false) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var activeChapterForNewTopic by remember { mutableStateOf<Chapter?>(null) }
    var activeTopicForEdit by remember { mutableStateOf<Topic?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!state.errorMessage.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::clearError) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // Reference Book Selector & Edition Configuration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(
                title = if (isArabicRtl) "المراجع الجراحية المعتمدة" else "Authorized Reference Books",
                subtitle = "Bailey & Love / Schwartz • Configure editions, chapters & custom sequences",
                modifier = Modifier.weight(1f)
            )
            FilledTonalButton(onClick = { showAddBookDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isArabicRtl) "إضافة مرجع" else "Add Book")
            }
        }

        state.books.forEach { book ->
            val isSelected = book.id == state.selectedBook?.id
            BookSelectorItem(
                book = book,
                isSelected = isSelected,
                onSelect = { viewModel.selectBook(book.id) }
            )
        }

        // View Mode & Status Filters
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LibraryViewMode.entries.forEach { mode ->
                FilterChip(
                    selected = state.viewMode == mode,
                    onClick = { viewModel.setViewMode(mode) },
                    label = {
                        Text(if (isArabicRtl) mode.labelAr else mode.labelEn)
                    }
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = state.statusFilter == null,
                onClick = { viewModel.setStatusFilter(null) },
                label = { Text("All Statuses") }
            )
            StudyStatus.entries.forEach { status ->
                FilterChip(
                    selected = state.statusFilter == status,
                    onClick = { viewModel.setStatusFilter(status) },
                    label = { Text(status.displayLabel) }
                )
            }
        }

        when (state.viewMode) {
            LibraryViewMode.CHAPTER_HIERARCHY -> {
                val selectedBook = state.selectedBook
                if (selectedBook != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedBook.title} (${selectedBook.edition})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(onClick = { showAddChapterDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Chapter")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabicRtl) "فصل جديد" else "Add Chapter")
                        }
                    }

                    if (state.chaptersForSelectedBook.isEmpty()) {
                        Text(
                            text = "No chapters added to this book yet. Tap 'Add Chapter' to configure your syllabus.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    state.chaptersForSelectedBook.forEach { chapter ->
                        val chapterTopics = state.topicsByChapterId[chapter.id].orEmpty()
                        ChapterExpandableCard(
                            chapter = chapter,
                            topics = chapterTopics,
                            onMoveChapterUp = {
                                viewModel.moveChapter(selectedBook.id, chapter.id, moveUp = true)
                            },
                            onMoveChapterDown = {
                                viewModel.moveChapter(selectedBook.id, chapter.id, moveUp = false)
                            },
                            onDeleteChapter = { viewModel.deleteChapter(chapter.id) },
                            onAddTopic = { activeChapterForNewTopic = chapter },
                            onEditTopic = { activeTopicForEdit = it },
                            onMoveTopicUp = { topic ->
                                viewModel.moveTopic(chapter.id, topic.id, moveUp = true)
                            },
                            onMoveTopicDown = { topic ->
                                viewModel.moveTopic(chapter.id, topic.id, moveUp = false)
                            },
                            onDeleteTopic = { topic -> viewModel.deleteTopic(topic.id) },
                            onStartTutor = onStartTutorForTopic
                        )
                    }
                }
            }

            LibraryViewMode.PERSONALIZED_SEQUENCE -> {
                SectionHeader(
                    title = if (isArabicRtl) "التسلسل الدراسي المخصص" else "Personalized Board Study Sequence",
                    subtitle = "Independent of textbook chapter order • Prioritizes In-Progress & Needs-Revision topics"
                )
                state.personalizedSequence.forEachIndexed { idx, topic ->
                    TopicSequenceCard(
                        sequenceIndex = idx + 1,
                        topic = topic,
                        onEditTopic = { activeTopicForEdit = topic },
                        onMoveUp = { viewModel.moveTopic(topic.chapterId, topic.id, moveUp = true) },
                        onMoveDown = { viewModel.moveTopic(topic.chapterId, topic.id, moveUp = false) },
                        onDelete = { viewModel.deleteTopic(topic.id) },
                        onStartTutor = { onStartTutorForTopic(topic) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onSave = { title, edition, authors, year ->
                viewModel.saveBook(
                    title = title,
                    edition = edition,
                    authorsCsv = authors,
                    publicationYear = year
                )
                showAddBookDialog = false
            }
        )
    }

    val currentBook = state.selectedBook
    if (showAddChapterDialog && currentBook != null) {
        AddChapterDialog(
            bookTitle = currentBook.title,
            onDismiss = { showAddChapterDialog = false },
            onSave = { title, number, description ->
                viewModel.saveChapter(
                    bookId = currentBook.id,
                    title = title,
                    chapterNumber = number,
                    description = description
                )
                showAddChapterDialog = false
            }
        )
    }

    val targetChapter = activeChapterForNewTopic
    if (targetChapter != null && currentBook != null) {
        AddTopicDialog(
            chapterTitle = targetChapter.title,
            onDismiss = { activeChapterForNewTopic = null },
            onSave = { title, difficulty, status, confidence, notes ->
                viewModel.saveTopic(
                    bookId = currentBook.id,
                    chapterId = targetChapter.id,
                    title = title,
                    difficulty = difficulty,
                    studyStatus = status,
                    confidenceScore = confidence,
                    studyNotes = notes
                )
                activeChapterForNewTopic = null
            }
        )
    }

    val topicToEdit = activeTopicForEdit
    if (topicToEdit != null) {
        EditTopicProgressDialog(
            topic = topicToEdit,
            onDismiss = { activeTopicForEdit = null },
            onSave = { status, confidence, notes, revisionDays ->
                viewModel.updateTopicProgress(
                    topicId = topicToEdit.id,
                    studyStatus = status,
                    confidenceScore = confidence,
                    studyNotes = notes,
                    scheduleRevisionInDays = revisionDays
                )
                activeTopicForEdit = null
            }
        )
    }
}

@Composable
private fun BookSelectorItem(
    book: Book,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        },
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = book.title,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${book.edition} • ${book.completedTopicsCount}/${book.totalTopicsCount} topics completed",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                StatusBadge(text = "Selected", color = ClinicalSuccess)
            }
        }
    }
}

@Composable
private fun ChapterExpandableCard(
    chapter: Chapter,
    topics: List<Topic>,
    onMoveChapterUp: () -> Unit,
    onMoveChapterDown: () -> Unit,
    onDeleteChapter: () -> Unit,
    onAddTopic: () -> Unit,
    onEditTopic: (Topic) -> Unit,
    onMoveTopicUp: (Topic) -> Unit,
    onMoveTopicDown: (Topic) -> Unit,
    onDeleteTopic: (Topic) -> Unit,
    onStartTutor: (Topic) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val numberPrefix = chapter.chapterNumber?.let { "Ch $it: " } ?: ""
                    Text(
                        text = "$numberPrefix${chapter.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (chapter.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = chapter.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(onClick = onMoveChapterUp) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Chapter Up")
                    }
                    IconButton(onClick = onMoveChapterDown) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Chapter Down")
                    }
                    IconButton(onClick = onDeleteChapter) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Chapter")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            topics.forEachIndexed { idx, topic ->
                TopicSequenceCard(
                    sequenceIndex = idx + 1,
                    topic = topic,
                    onEditTopic = { onEditTopic(topic) },
                    onMoveUp = { onMoveTopicUp(topic) },
                    onMoveDown = { onMoveTopicDown(topic) },
                    onDelete = { onDeleteTopic(topic) },
                    onStartTutor = { onStartTutor(topic) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            FilledTonalButton(onClick = onAddTopic) {
                Icon(Icons.Default.Add, contentDescription = "Add Subtopic")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Subtopic to Chapter")
            }
        }
    }
}

@Composable
private fun TopicSequenceCard(
    sequenceIndex: Int,
    topic: Topic,
    onEditTopic: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onStartTutor: () -> Unit
) {
    val statusColor = when (topic.studyStatus) {
        StudyStatus.COMPLETED -> ClinicalSuccess
        StudyStatus.IN_PROGRESS -> ClinicalInfo
        StudyStatus.NEEDS_REVISION -> ClinicalDanger
        StudyStatus.NOT_STARTED -> ClinicalWarning
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = "#$sequenceIndex • ${topic.studyStatus.displayLabel}",
                    color = statusColor
                )
                StatusBadge(
                    text = "${topic.difficulty.displayLabel} • Conf ${topic.confidenceScore}/5",
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = topic.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (topic.studyNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${topic.studyNotes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(onClick = onEditTopic) {
                        Icon(Icons.Default.EditNote, contentDescription = "Update Status & Notes")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Progress / Notes")
                    }
                    OutlinedButton(onClick = onStartTutor) {
                        Text("AI Lesson")
                    }
                }
                Row {
                    IconButton(onClick = onMoveUp) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                    }
                    IconButton(onClick = onMoveDown) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Topic")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddBookDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, edition: String, authors: String, year: Int?) -> Unit
) {
    var title by remember { mutableStateOf("Bailey & Love's Short Practice of Surgery") }
    var edition by remember { mutableStateOf("28th Edition") }
    var authors by remember { mutableStateOf("Norman S. Williams, P. Ronan O'Connell") }
    var yearText by remember { mutableStateOf("2023") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Surgical Reference Book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = edition,
                    onValueChange = { edition = it },
                    label = { Text("Exact Edition (e.g., 28th Edition)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = authors,
                    onValueChange = { authors = it },
                    label = { Text("Authors (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { yearText = it },
                    label = { Text("Publication Year") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, edition, authors, yearText.toIntOrNull()) }) {
                Text("Save Book")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddChapterDialog(
    bookTitle: String,
    onDismiss: () -> Unit,
    onSave: (title: String, chapterNumber: Int?, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var numberText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Chapter to $bookTitle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = numberText,
                    onValueChange = { numberText = it },
                    label = { Text("Chapter Number (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Chapter Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("High-Yield Scope / Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, numberText.toIntOrNull(), description) }) {
                Text("Save Chapter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddTopicDialog(
    chapterTitle: String,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        difficulty: DifficultyLevel,
        status: StudyStatus,
        confidence: Int,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(DifficultyLevel.BOARD_ADVANCED) }
    var status by remember { mutableStateOf(StudyStatus.NOT_STARTED) }
    var confidence by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subtopic • $chapterTitle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Topic Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Difficulty Level", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DifficultyLevel.entries.forEach { level ->
                        FilterChip(
                            selected = difficulty == level,
                            onClick = { difficulty = level },
                            label = { Text(level.displayLabel) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Surgical Pearls / Study Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, difficulty, status, confidence, notes) }) {
                Text("Add Topic")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditTopicProgressDialog(
    topic: Topic,
    onDismiss: () -> Unit,
    onSave: (
        status: StudyStatus,
        confidence: Int,
        notes: String,
        revisionDays: Int?
    ) -> Unit
) {
    var status by remember { mutableStateOf(topic.studyStatus) }
    var confidence by remember { mutableIntStateOf(topic.confidenceScore) }
    var notes by remember { mutableStateOf(topic.studyNotes) }
    var revisionDays by remember { mutableStateOf<Int?>(7) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Topic Mastery & Notes") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(topic.title, style = MaterialTheme.typography.titleMedium)

                Text("Study Status", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StudyStatus.entries.forEach { item ->
                        FilterChip(
                            selected = status == item,
                            onClick = { status = item },
                            label = { Text(item.displayLabel) }
                        )
                    }
                }

                Text("Confidence Score (1 = Low, 5 = Board Ready)", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { score ->
                        FilterChip(
                            selected = confidence == score,
                            onClick = { confidence = score },
                            label = { Text("$score") }
                        )
                    }
                }

                Text("Schedule Spaced Revision", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(3, 7, 14, 30).forEach { days ->
                        FilterChip(
                            selected = revisionDays == days,
                            onClick = { revisionDays = days },
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                            },
                            label = { Text("+${days}d") }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Personal Surgical Notes & Board Pearls") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(status, confidence, notes, revisionDays) }) {
                Text("Save Progress")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
