package com.surgicalboardmaster.app.domain.model

data class LessonSection(
    val title: String,
    val markdownContent: String,
    val isPearlOrCallout: Boolean = false,
    val isAlgorithmOrTable: Boolean = false
)

data class StructuredLesson(
    val id: String,
    val userId: String,
    val bookId: String,
    val bookTitle: String,
    val chapterId: String,
    val chapterTitle: String,
    val topicId: String,
    val topicTitle: String,
    val format: LessonFormat,
    val language: String, // "Arabic (with English Terminology)" / "English"
    val teachingDepth: String, // "Board Exam Focus", "Viva & Fellowship", "Foundation"
    val learningObjectives: List<String>,
    val relevantAnatomy: String,
    val pathophysiology: String,
    val clinicalPresentation: String,
    val investigationsAndInterpretation: String,
    val managementAlgorithms: String,
    val operativePrinciples: String,
    val complications: String,
    val boardPearls: List<String>,
    val commonMistakes: List<String>,
    val summary: String,
    val references: List<String>,
    val additionalSections: List<LessonSection> = emptyList(),
    val isBookmarked: Boolean = false,
    val userPersonalNotes: String = "",
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

data class LessonConfig(
    val bookTitle: String,
    val chapterTitle: String,
    val topicTitle: String,
    val format: LessonFormat = LessonFormat.BOARD_EXAM_PREP,
    val language: String = "Arabic (مع مصطلحات إنجليزية)",
    val teachingDepth: String = "Board Examination Level",
    val examinationTarget: String = "Arab Board / MRCS / Jordanian Board / Iraqi Board"
)
