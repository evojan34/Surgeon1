package com.surgicalboardmaster.app.domain.model

enum class SurgicalDomain(val displayLabel: String) {
    UPPER_GI("Upper GI"),
    COLORECTAL("Colorectal"),
    HEPATOBILIARY("Hepatobiliary"),
    PANCREATIC("Pancreatic"),
    BREAST("Breast"),
    ENDOCRINE("Endocrine"),
    TRAUMA_EMERGENCY("Trauma & Emergency Surgery"),
    VASCULAR("Vascular"),
    HERNIA_ABDOMINAL_WALL("Hernia & Abdominal Wall"),
    SURGICAL_CRITICAL_CARE("Surgical Critical Care")
}

enum class SimulationStageType(val stepNumber: Int, val titleEn: String, val titleAr: String) {
    CHIEF_COMPLAINT(1, "Chief Complaint & Triage", "الشكوى الرئيسية والفرز"),
    HISTORY_TAKING(2, "Focused History Taking", "أخذ القصة السريرية الموجهة"),
    EXAMINATION_FINDINGS(3, "Physical Examination", "الفحص السريري"),
    INITIAL_INVESTIGATIONS(4, "Investigations & Interpretation", "الاستقصاءات وتفسيرها"),
    INITIAL_MANAGEMENT(5, "Resuscitation & Initial Management", "الإنعاش والتدبير الأولي"),
    OPERATIVE_PLANNING(6, "Definitive Operative Decision", "القرار والتخطيط الجراحي"),
    COMPLICATIONS_FOLLOW_UP(7, "Postoperative Complication & Follow-up", "المضاعفات والمتابعة")
}

data class SimulationActionOption(
    val optionId: String,
    val actionText: String,
    val clinicalOutcomeReveal: String,
    val scoreDelta: Int, // Positive for sound clinical reasoning, negative for unsafe/unnecessary action
    val isCriticalSafetyAction: Boolean,
    val rationale: String
)

data class SimulationStage(
    val stageType: SimulationStageType,
    val progressiveCaseNarrative: String,
    val vitalSignsBanner: String,
    val promptQuestion: String,
    val availableActions: List<SimulationActionOption>,
    val correctOptionId: String
)

data class SimulationScenario(
    val id: String,
    val title: String,
    val domain: SurgicalDomain,
    val difficulty: DifficultyLevel,
    val patientSummaryBanner: String,
    val educationalDisclaimer: String,
    val stages: List<SimulationStage>,
    val sourceReference: String
)

data class StageDecisionLog(
    val stageType: SimulationStageType,
    val selectedOptionId: String,
    val actionText: String,
    val clinicalOutcomeReveal: String,
    val rationale: String,
    val pointsEarned: Int,
    val maxPointsPossible: Int,
    val missedCriticalSafety: Boolean
)

data class SimulationDebriefReport(
    val scenarioId: String,
    val scenarioTitle: String,
    val totalScoreEarned: Int,
    val maxPossibleScore: Int,
    val percentageScore: Int,
    val criticalSafetyViolations: List<String>,
    val stageLogs: List<StageDecisionLog>,
    val completedAtEpochMillis: Long
)
