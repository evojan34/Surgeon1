package com.surgicalboardmaster.app.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.SimulationActionOption
import com.surgicalboardmaster.app.domain.model.SimulationDebriefReport
import com.surgicalboardmaster.app.domain.model.SimulationScenario
import com.surgicalboardmaster.app.domain.model.SimulationStage
import com.surgicalboardmaster.app.domain.model.SimulationStageType
import com.surgicalboardmaster.app.domain.model.SurgicalDomain
import com.surgicalboardmaster.app.domain.repository.SimulatorRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstSimulatorRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SimulatorRepository {

    private val scenariosState = MutableStateFlow(buildDefaultSurgicalScenarios())
    private val debriefsByUser = MutableStateFlow<Map<String, List<SimulationDebriefReport>>>(emptyMap())

    override fun observeAvailableScenarios(domainFilter: SurgicalDomain?): Flow<List<SimulationScenario>> {
        return scenariosState.map { list ->
            if (domainFilter == null) list
            else list.filter { it.domain == domainFilter }
        }
    }

    override fun observeCompletedDebriefs(userId: String): Flow<List<SimulationDebriefReport>> {
        return debriefsByUser.map { map -> map[userId].orEmpty() }
    }

    override suspend fun getScenarioById(scenarioId: String): SimulationScenario? {
        return scenariosState.value.find { it.id == scenarioId }
    }

    override suspend fun recordSimulationDebrief(
        userId: String,
        report: SimulationDebriefReport
    ): Result<Unit> {
        debriefsByUser.update { current ->
            val userList = current[userId].orEmpty()
            current + (userId to (listOf(report) + userList))
        }
        syncDebriefToFirestoreIfAuthenticated(userId, report)
        return Result.success(Unit)
    }

    private fun syncDebriefToFirestoreIfAuthenticated(
        userId: String,
        report: SimulationDebriefReport
    ) {
        if (!runCatching {
                FirebaseApp.getApps(context).isNotEmpty() &&
                    FirebaseAuth.getInstance().currentUser?.uid == userId
            }.getOrDefault(false)
        ) return

        runCatching {
            val simId = "sim_${UUID.randomUUID()}"
            val payload = mapOf(
                "id" to simId,
                "scenarioId" to report.scenarioId,
                "scenarioTitle" to report.scenarioTitle,
                "percentageScore" to report.percentageScore,
                "criticalViolationsCount" to report.criticalSafetyViolations.size,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("simulations")
                .document(simId)
                .set(payload, SetOptions.merge())
        }
    }

    private fun buildDefaultSurgicalScenarios(): List<SimulationScenario> {
        return listOf(
            SimulationScenario(
                id = "sim_upper_gi_perforation",
                title = "Acute Generalized Peritonitis — Perforated Duodenal Ulcer",
                domain = SurgicalDomain.UPPER_GI,
                difficulty = DifficultyLevel.BOARD_ADVANCED,
                patientSummaryBanner = "46M • Sudden excruciating epigastric pain becoming generalized • NSAID use",
                educationalDisclaimer = "Educational Simulation Only — Scored against Bailey & Love 28th Ed (Ch 63) & Schwartz 11th Ed criteria.",
                sourceReference = "Bailey & Love's Short Practice of Surgery, 28th Ed — Chapter 63: Stomach and Duodenum",
                stages = listOf(
                    SimulationStage(
                        stageType = SimulationStageType.CHIEF_COMPLAINT,
                        progressiveCaseNarrative = "A 46-year-old man arrives in the Emergency Department lying completely still on the trolley. He reports sudden-onset severe epigastric pain that began 4 hours ago while lifting boxes, now radiating across the entire abdomen.",
                        vitalSignsBanner = "HR: 118 bpm | BP: 96/62 mmHg | RR: 24/min | Temp: 37.9°C | SpO2: 97% RA",
                        promptQuestion = "What is your immediate priority in the primary triage and initial assessment?",
                        availableActions = listOf(
                            SimulationActionOption(
                                optionId = "A",
                                actionText = "Establish 2 large-bore IV cannulae, start isotonic crystalloid resuscitation, draw bloods (FBC, U&E, Amylase/Lipase, VBG, Group & Save), and provide IV opioid analgesia.",
                                clinicalOutcomeReveal = "IV access secured; 1L Hartmann's solution infusing. Pain controlled enough to permit a cooperative history and abdominal examination.",
                                scoreDelta = 20,
                                isCriticalSafetyAction = true,
                                rationale = "Resuscitation (ABCDE) and early titrated IV analgesia are safe and do not mask peritoneal signs in modern surgical practice."
                            ),
                            SimulationActionOption(
                                optionId = "B",
                                actionText = "Withhold all analgesia until the senior consultant examines the abdomen, and send the patient directly to the radiology waiting room for an abdominal ultrasound.",
                                clinicalOutcomeReveal = "Patient becomes more tachycardic (HR 130 bpm) and hypotensive in the unmonitored radiology corridor.",
                                scoreDelta = -10,
                                isCriticalSafetyAction = false,
                                rationale = "Withholding analgesia is outdated and harmful; sending an unstable hypotensive patient to ultrasound without resuscitation is unsafe."
                            ),
                            SimulationActionOption(
                                optionId = "C",
                                actionText = "Order an urgent upper GI endoscopy (OGD) to inspect the stomach and duodenum.",
                                clinicalOutcomeReveal = "Endoscopy unit refuses request due to suspected hollow viscus perforation.",
                                scoreDelta = -15,
                                isCriticalSafetyAction = false,
                                rationale = "Suspected perforated peptic ulcer is an absolute contraindication to endoscopy because air insufflation converts a contained leak into tension pneumoperitoneum."
                            )
                        ),
                        correctOptionId = "A"
                    ),
                    SimulationStage(
                        stageType = SimulationStageType.HISTORY_TAKING,
                        progressiveCaseNarrative = "After initial fluid bolus, you take a focused history. Which specific history elements are most critical to elicit now?",
                        vitalSignsBanner = "HR: 106 bpm | BP: 108/70 mmHg | Urine output via Foley: 25 mL/hr",
                        promptQuestion = "Select the highest-yield focused surgical history inquiry:",
                        availableActions = listOf(
                            SimulationActionOption(
                                optionId = "A",
                                actionText = "Inquire about chronic NSAID/steroid intake, previous peptic ulcer disease/H. pylori status, smoking, alcohol binge, and exact timing of pain onset.",
                                clinicalOutcomeReveal = "Patient reveals taking high-dose Diclofenac daily for 6 weeks for lumbar disc pain and smokes 25 cigarettes/day.",
                                scoreDelta = 20,
                                isCriticalSafetyAction = false,
                                rationale = "NSAID use + smoking strongly point toward anterior duodenal bulb perforation."
                            ),
                            SimulationActionOption(
                                optionId = "B",
                                actionText = "Focus primarily on family history of colonic polyps and childhood vaccinations.",
                                clinicalOutcomeReveal = "No relevant family history identified; precious time lost.",
                                scoreDelta = 0,
                                isCriticalSafetyAction = false,
                                rationale = "Low diagnostic yield in acute chemical/bacterial peritonitis."
                            )
                        ),
                        correctOptionId = "A"
                    ),
                    SimulationStage(
                        stageType = SimulationStageType.EXAMINATION_FINDINGS,
                        progressiveCaseNarrative = "On abdominal examination: The abdomen moves poorly with respiration. Palpation reveals generalized board-like rigidity (involuntary guarding) and percussion tenderness maximally in the epigastrium and right iliac fossa (Valentino's sign). Bowel sounds are absent.",
                        vitalSignsBanner = "HR: 104 bpm | BP: 112/72 mmHg | Lactate: 2.8 mmol/L | Serum Amylase: 190 U/L (mildly elevated)",
                        promptQuestion = "Which diagnostic investigation will confirm your working diagnosis rapidly and accurately?",
                        availableActions = listOf(
                            SimulationActionOption(
                                optionId = "A",
                                actionText = "Erect Chest Radiograph (CXR) or CT Abdomen/Pelvis with IV contrast (Gold Standard).",
                                clinicalOutcomeReveal = "Erect CXR shows free crescentic subdiaphragmatic gas under the right hemidiaphragm; CT confirms free intraperitoneal air and periduodenal fluid with a normal pancreas.",
                                scoreDelta = 20,
                                isCriticalSafetyAction = true,
                                rationale = "Erect CXR detects free gas in 75–80% of perforated duodenal ulcers; CT Abdomen/Pelvis has >95% sensitivity and localizes the site of perforation."
                            ),
                            SimulationActionOption(
                                optionId = "B",
                                actionText = "Diagnostic Peritoneal Lavage (DPL).",
                                clinicalOutcomeReveal = "Invasive procedure performed unnecessarily when non-invasive imaging is definitive.",
                                scoreDelta = -5,
                                isCriticalSafetyAction = false,
                                rationale = "DPL is obsolete for non-traumatic perforated viscus when erect CXR and CT are available."
                            )
                        ),
                        correctOptionId = "A"
                    ),
                    SimulationStage(
                        stageType = SimulationStageType.INITIAL_MANAGEMENT,
                        progressiveCaseNarrative = "Pneumoperitoneum is confirmed. You are preparing the patient for the operating theatre (Boey score = 1).",
                        vitalSignsBanner = "HR: 100 bpm | BP: 115/74 mmHg | Urine Output: 40 mL/hr",
                        promptQuestion = "What preoperative bundle must be instituted immediately before skin incision?",
                        availableActions = listOf(
                            SimulationActionOption(
                                optionId = "A",
                                actionText = "Nasogastric tube (NGT) decompression, IV Proton Pump Inhibitor (Omeprazole), broad-spectrum IV antibiotics (covering enteric Gram-negatives, anaerobes, and consider antifungal in delayed presentation), and informed consent.",
                                clinicalOutcomeReveal = "400 mL bilious fluid aspirated via NGT; IV antibiotics and PPI administered. Patient transferred safely to theatre.",
                                scoreDelta = 20,
                                isCriticalSafetyAction = true,
                                rationale = "NG decompression halts ongoing gastroduodenal spillage; broad-spectrum antibiotics and IV PPI are mandatory."
                            ),
                            SimulationActionOption(
                                optionId = "B",
                                actionText = "Administer oral water-soluble contrast challenge and observe on the ward for 48 hours.",
                                clinicalOutcomeReveal = "Patient develops worsening septic shock from uncontained generalized peritonitis.",
                                scoreDelta = -20,
                                isCriticalSafetyAction = false,
                                rationale = "Generalized peritonitis with board-like rigidity mandates urgent surgical source control."
                            )
                        ),
                        correctOptionId = "A"
                    ),
                    SimulationStage(
                        stageType = SimulationStageType.OPERATIVE_PLANNING,
                        progressiveCaseNarrative = "Intraoperatively (laparoscopic or upper midline laparotomy), you identify a 7-mm acute perforation on the anterior wall of the first part of the duodenum (D1) with fibrinous exudate.",
                        vitalSignsBanner = "Intraoperative: Hemodynamically stable on minimal vasopressor support",
                        promptQuestion = "What is the definitive operative procedure of choice for this 7-mm D1 perforation?",
                        availableActions = listOf(
                            SimulationActionOption(
                                optionId = "A",
                                actionText = "Thorough four-quadrant peritoneal lavage with warm saline + Omental Patch Repair (Graham / Cellan-Jones vascularized pedicled omental plug) + H. pylori eradication + lifelong NSAID cessation.",
                                clinicalOutcomeReveal = "Vascularized omental patch secured tension-free; thorough peritoneal toilet completed. Postoperative recovery is smooth.",
                                scoreDelta = 20,
                                isCriticalSafetyAction = true,
                                rationale = "For duodenal perforations <1-2 cm, Cellan-Jones / Graham omental patch repair with postoperative H. pylori triple therapy is the gold standard; routine truncal vagotomy is no longer required."
                            ),
                            SimulationActionOption(
                                optionId = "B",
                                actionText = "Emergency Whipple procedure (Pancreaticoduodenectomy).",
                                clinicalOutcomeReveal = "Massive physiological insult and prohibitive mortality in an acute peritonitic field.",
                                scoreDelta = -25,
                                isCriticalSafetyAction = false,
                                rationale = "Whipple is never indicated for a simple 7-mm anterior D1 peptic perforation."
                            )
                        ),
                        correctOptionId = "A"
                    )
                )
            ),
            SimulationScenario(
                id = "sim_trauma_spleen",
                title = "Blunt Abdominal Trauma — Grade IV Splenic Injury",
                domain = SurgicalDomain.TRAUMA_EMERGENCY,
                difficulty = DifficultyLevel.BOARD_ADVANCED,
                patientSummaryBanner = "27M • High-speed motorcycle collision • LUQ tenderness & positive Kehr's sign",
                educationalDisclaimer = "Educational Simulation Only — ATLS 10th Ed & Bailey & Love 28th Ed Trauma Principles.",
                sourceReference = "Bailey & Love's Short Practice of Surgery, 28th Ed — Chapter 26: Abdominal Trauma",
                stages = listOf(
                    SimulationStage(
                        stageType = SimulationStageType.CHIEF_COMPLAINT,
                        progressiveCaseNarrative = "A 27-year-old motorcyclist is brought to the trauma bay after striking a guardrail. Airway is patent, C-spine immobilized, breath sounds equal bilaterally. He complains of left upper quadrant pain and left shoulder tip pain (Kehr's sign).",
                        vitalSignsBanner = "HR: 124 bpm | BP: 84/52 mmHg | RR: 26/min | GCS: 15/15 | FAST: Free fluid in Morison's pouch & splenorenal recess",
                        promptQuestion = "FAST is positive for hemoperitoneum and the patient is hypotensive (BP 84/52). What is the immediate next step?",
                        availableActions = listOf(
                            SimulationActionOption(
                                optionId = "A",
                                actionText = "Activate balanced hemostatic resuscitation (pRBC:FFP:Platelets 1:1:1 + IV Tranexamic Acid 1g within 3 hours) and assess hemodynamic response before deciding between CT vs direct laparotomy.",
                                clinicalOutcomeReveal = "After 1 unit pRBC + TXA, BP remains 76/44 mmHg and HR rises to 136 bpm (Non-Responder).",
                                scoreDelta = 50,
                                isCriticalSafetyAction = true,
                                rationale = "ATLS mandates balanced blood product resuscitation + TXA (CRASH-2); non-responders with positive FAST go directly to the operating room."
                            ),
                            SimulationActionOption(
                                optionId = "B",
                                actionText = "Transfer the hypotensive non-resuscitated patient directly to the CT scanner for a triple-phase scan.",
                                clinicalOutcomeReveal = "Patient suffers cardiac arrest in the CT scanner (" + "donut of death" + ").",
                                scoreDelta = -30,
                                isCriticalSafetyAction = false,
                                rationale = "Hemodynamically unstable trauma patients with a positive FAST must never be sent to the CT scanner."
                            )
                        ),
                        correctOptionId = "A"
                    ),
                    SimulationStage(
                        stageType = SimulationStageType.OPERATIVE_PLANNING,
                        progressiveCaseNarrative = "Because the patient is a hemodynamic non-responder with a positive FAST, you proceed to emergency midline trauma laparotomy. Upon opening the peritoneum, 1.8 L of blood is evacuated and brisk bleeding is identified from a shattered spleen (AAST Grade IV/V).",
                        vitalSignsBanner = "Intraoperative: BP 82/50 mmHg | Core Temp: 35.4°C | pH: 7.22",
                        promptQuestion = "What is the definitive intraoperative decision?",
                        availableActions = listOf(
                            SimulationActionOption(
                                optionId = "A",
                                actionText = "Four-quadrant packing, mobilize the spleen by dividing the splenorenal and splenophrenic ligaments, control the splenic hilar vessels (avoiding the tail of the pancreas), perform emergency total splenectomy, and plan post-splenectomy vaccination.",
                                clinicalOutcomeReveal = "Splenic hilum controlled rapidly; bleeding arrested. Hemodynamics stabilize (BP 116/74 mmHg). Post-op OPSI prophylaxis scheduled at day 14.",
                                scoreDelta = 50,
                                isCriticalSafetyAction = true,
                                rationale = "In an unstable patient with a shattered spleen, rapid total splenectomy is life-saving; splenic salvage is contraindicated in hemodynamic instability."
                            ),
                            SimulationActionOption(
                                optionId = "B",
                                actionText = "Spend 90 minutes attempting delicate partial splenectomy and absorbable mesh splenorrhaphy.",
                                clinicalOutcomeReveal = "Patient develops the lethal triad of hypothermia, acidosis, and coagulopathy.",
                                scoreDelta = -25,
                                isCriticalSafetyAction = false,
                                rationale = "Prolonged attempts at splenic preservation in a hypotensive, acidotic trauma patient are fatal."
                            )
                        ),
                        correctOptionId = "A"
                    )
                )
            )
        )
    }
}
