package com.surgicalboardmaster.app.domain.model

data class ClinicalCaseDetail(
    val caseId: String,
    val userId: String,
    val pseudonymousId: String,
    val category: CaseCategory,
    val ageRange: String,
    val chiefComplaint: String,
    val historyOfPresentIllness: String = "",
    val relevantMedicalSurgicalHistory: String = "",
    val medicationsAndAllergies: String = "",
    val examinationFindings: String = "",
    val laboratoryResults: String = "",
    val imagingReports: String = "",
    val differentialDiagnosis: String = "",
    val finalDiagnosis: String = "",
    val admissionInformation: String = "",
    val operationDetails: String = "",
    val operativeNotes: String = "",
    val postoperativeProgress: String = "",
    val complications: String = "",
    val currentTreatmentPlan: String = "",
    val pendingResults: String = "",
    val nextAction: String = "",
    val dischargeSummary: String = "",
    val followUpPlan: String = "",
    val learningNotes: String = "",
    val relatedEducationalTopics: List<String> = emptyList(),
    val status: CaseStatus = CaseStatus.DRAFT,
    val isArchived: Boolean = false,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
) {
    fun toSummary(latestUpdateText: String): ClinicalCaseSummary = ClinicalCaseSummary(
        caseId = caseId,
        pseudonymousId = pseudonymousId,
        category = category,
        ageRange = ageRange,
        chiefComplaint = chiefComplaint,
        workingDiagnosis = finalDiagnosis.ifBlank { differentialDiagnosis.ifBlank { "Working diagnosis pending" } },
        procedureName = operationDetails.ifBlank { null },
        status = status,
        latestClinicalUpdate = latestUpdateText.ifBlank { postoperativeProgress.ifBlank { "Initial assessment logged" } },
        nextAction = nextAction.ifBlank { followUpPlan.ifBlank { "Review clinical plan" } },
        updatedAtEpochMillis = updatedAtEpochMillis
    )
}

data class ClinicalCaseFilter(
    val searchQuery: String = "",
    val category: CaseCategory? = null,
    val status: CaseStatus? = null,
    val includeArchived: Boolean = false
)

/**
 * Enforces Part 9 Clinical Privacy Requirements:
 * Rejects direct patient identifiers (phone numbers, long national ID numbers)
 * and requires pseudonymous case codes (e.g., SBM-2026-HPB-014).
 */
object ClinicalPrivacyValidator {
    private val phoneOrNationalIdRegex = Regex("\\b\\d{9,15}\\b")

    fun validatePseudonymousId(pseudonymousId: String): Result<String> {
        val clean = pseudonymousId.trim()
        if (clean.length < 4) {
            return Result.failure(
                IllegalArgumentException("Pseudonymous Case ID must be at least 4 characters (e.g., SBM-2026-001).")
            )
        }
        if (phoneOrNationalIdRegex.containsMatchIn(clean)) {
            return Result.failure(
                IllegalArgumentException("Privacy Guard: Do not use phone numbers, national IDs, or hospital MRNs as the Pseudonymous ID.")
            )
        }
        return Result.success(clean)
    }

    fun validateNoRawIdentifiersInText(fieldName: String, content: String): Result<String> {
        val clean = content.trim()
        if (phoneOrNationalIdRegex.containsMatchIn(clean)) {
            return Result.failure(
                IllegalArgumentException("Privacy Guard: Potential 9-15 digit phone number or national ID detected in '$fieldName'. Please remove direct identifiers.")
            )
        }
        return Result.success(clean)
    }
}
