package com.surgicalboardmaster.app.domain.model

enum class TeachingLanguage(val displayLabel: String) {
    ARABIC_WITH_ENGLISH_TERMS("العربية مع المصطلحات الجراحية الإنجليزية (Bailey Standard)"),
    ENGLISH_ACADEMIC("Academic Surgical English")
}

enum class TeachingDepth(val displayLabel: String) {
    FOUNDATION_CORE("Foundation & Core Principles"),
    BOARD_CANDIDATE("General Surgery Board Candidate"),
    FELLOWSHIP_CONSULTANT("Subspecialty / Viva Consultant Level")
}

enum class ExamTargetLevel(val displayLabel: String) {
    ARAB_IRAQI_BOARD("Arab / Iraqi Board of General Surgery"),
    MRCS_FRCS_UK("Intercollegiate MRCS / FRCS"),
    ABSITE_BOARD("ABSITE / American Board Standard")
}

enum class LessonSectionType(val orderIndex: Int, val titleEn: String, val titleAr: String) {
    LEARNING_OBJECTIVES(1, "Learning Objectives", "الأهداف التعليمية"),
    DEFINITION_CLASSIFICATION(2, "Definition & Classification", "التعريف والتصنيف المعتمد"),
    RELEVANT_ANATOMY(3, "Relevant Surgical Anatomy", "التشريح الجراحي الدقيق"),
    PHYSIOLOGY_PATHOPHYSIOLOGY(4, "Physiology & Pathophysiology", "الفيزيولوجيا والآلية المرضية"),
    ETIOLOGY_RISK_FACTORS(5, "Etiology & Risk Factors", "الأسباب وعوامل الخطورة"),
    CLINICAL_PRESENTATION(6, "Clinical Presentation & Signs", "التظاهرات السريرية وعلامات الفحص"),
    DIFFERENTIAL_DIAGNOSIS(7, "Differential Diagnosis", "التشخيص التفريقي"),
    INVESTIGATIONS_INTERPRETATION(8, "Investigations & Interpretation", "الاستقصاءات وتفسيرها"),
    MANAGEMENT_ALGORITHMS(9, "Management Algorithms", "خوارزميات التدبير الجراحي"),
    OPERATIVE_INDICATIONS(10, "Operative Indications & Contraindications", "استطبابات ومضادات استطباب الجراحة"),
    SURGICAL_PRINCIPLES(11, "Operative Steps & Surgical Principles", "المبادئ والخطوات الجراحية"),
    COMPLICATIONS(12, "Intraoperative & Postoperative Complications", "المضاعفات الجراحية وتدبيرها"),
    POSTOPERATIVE_CARE(13, "Postoperative Care & ERAS", "الرعاية بعد الجراحة"),
    COMPARISONS_TABLES(14, "High-Yield Comparisons", "المقارنات الجراحية الحاسمة"),
    BOARD_PEARLS(15, "Board Examination Pearls", "درر امتحانات البورد"),
    COMMON_MISTAKES(16, "Common Pitfalls & Traps", "الأخطاء الشائعة وأفخاخ الامتحان"),
    SUMMARY(17, "Executive Summary", "الملخص التنفيذي"),
    PRACTICE_QUESTIONS(18, "Embedded Practice Prompts", "أسئلة تطبيقية سريعة"),
    SOURCES_AND_PROVENANCE(19, "Source Provenance & Verification Notice", "توثيق المصادر والمنهجية")
}

data class LessonSection(
    val sectionId: String,
    val type: LessonSectionType,
    val bodyContent: String,
    val isVerifiedReferenceFact: Boolean,
    val isBookmarked: Boolean = false,
    val highlightText: String = ""
)

data class StructuredSurgicalLesson(
    val lessonId: String,
    val userId: String,
    val bookId: String,
    val bookTitle: String,
    val edition: String,
    val chapterId: String,
    val chapterTitle: String,
    val topicId: String,
    val topicTitle: String,
    val language: TeachingLanguage,
    val depth: TeachingDepth,
    val format: LessonFormat,
    val examLevel: ExamTargetLevel,
    val sections: List<LessonSection>,
    val personalNotes: String,
    val isCompleted: Boolean,
    val lastOpenedEpochMillis: Long,
    val nextRevisionEpochMillis: Long?,
    val provenanceBanner: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
