package com.surgicalboardmaster.app.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.surgicalboardmaster.app.data.local.dao.ExamDao
import com.surgicalboardmaster.app.data.local.entity.ExamAttemptEntity
import com.surgicalboardmaster.app.data.local.entity.ExamQuestionEntity
import com.surgicalboardmaster.app.domain.model.DifficultyLevel
import com.surgicalboardmaster.app.domain.model.ExamAttempt
import com.surgicalboardmaster.app.domain.model.ExamOption
import com.surgicalboardmaster.app.domain.model.ExamQuestion
import com.surgicalboardmaster.app.domain.model.QuestionSubmission
import com.surgicalboardmaster.app.domain.model.QuestionType
import com.surgicalboardmaster.app.domain.model.WrongAnswerItem
import com.surgicalboardmaster.app.domain.repository.ExamRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstExamRepository @Inject constructor(
    private val examDao: ExamDao,
    @ApplicationContext private val context: Context
) : ExamRepository {

    override fun observeAllQuestions(): Flow<List<ExamQuestion>> {
        return examDao.observeAllQuestions().map { entities ->
            entities.map { it.toDomainQuestion() }
        }
    }

    override fun observeExamAttempts(userId: String): Flow<List<ExamAttempt>> {
        return examDao.observeExamAttempts(userId).map { entities ->
            entities.map { it.toDomainAttempt() }
        }
    }

    override suspend fun getQuestionsForExam(topicId: String?, limit: Int): List<ExamQuestion> {
        ensureDefaultQuestionsSeeded()
        val all = if (topicId.isNullOrBlank()) {
            examDao.observeAllQuestions().first().map { it.toDomainQuestion() }
        } else {
            examDao.getQuestionsForTopic(topicId).map { it.toDomainQuestion() }
        }
        return all.shuffled().take(limit)
    }

    override suspend fun submitExamAttempt(
        userId: String,
        examTitle: String,
        isTimedMode: Boolean,
        timeLimitMinutes: Int,
        submissions: List<QuestionSubmission>
    ): Result<ExamAttempt> {
        val now = System.currentTimeMillis()
        val total = submissions.size
        val correct = submissions.count { it.isCorrect }
        val scorePercent = if (total == 0) 0 else ((correct.toDouble() / total) * 100).toInt()

        val attemptId = "att_${UUID.randomUUID()}"
        val submissionsJson = serializeSubmissions(submissions)

        val entity = ExamAttemptEntity(
            id = attemptId,
            userId = userId,
            examTitle = examTitle,
            isTimedMode = isTimedMode,
            timeLimitMinutes = timeLimitMinutes,
            totalQuestionsCount = total,
            correctCount = correct,
            scorePercentage = scorePercent,
            submissionsJson = submissionsJson,
            completedAtEpochMillis = now,
            isPendingCloudSync = true
        )

        examDao.insertExamAttempt(entity)
        syncAttemptToFirestoreIfAuthenticated(entity)

        return Result.success(entity.toDomainAttempt())
    }

    override suspend fun observeWrongAnswerNotebook(userId: String): List<WrongAnswerItem> {
        val attempts = examDao.observeExamAttempts(userId).first()
        val allQuestions = examDao.observeAllQuestions().first().map { it.toDomainQuestion() }.associateBy { it.id }

        val failureMap = mutableMapOf<String, Pair<QuestionSubmission, Int>>()
        attempts.forEach { att ->
            val subs = deserializeSubmissions(att.submissionsJson)
            subs.filter { !it.isCorrect }.forEach { sub ->
                val prev = failureMap[sub.questionId]
                val count = (prev?.second ?: 0) + 1
                failureMap[sub.questionId] = Pair(sub, count)
            }
        }

        return failureMap.mapNotNull { (qId, pair) ->
            val question = allQuestions[qId] ?: return@mapNotNull null
            WrongAnswerItem(
                question = question,
                selectedOptionId = pair.first.selectedOptionId,
                failureCount = pair.second,
                lastAttemptEpochMillis = System.currentTimeMillis()
            )
        }
    }

    override suspend fun ensureDefaultQuestionsSeeded() {
        if (examDao.countQuestions() > 0) return

        val sampleQuestions = listOf(
            ExamQuestionEntity(
                id = "q_hpb_001",
                topicId = "topic_mirizzi",
                topicTitle = "Mirizzi Syndrome",
                bookReference = "Bailey & Love 28th Ed, Ch 67, p. 1184",
                questionTypeName = QuestionType.SINGLE_BEST_ANSWER.name,
                difficultyName = DifficultyLevel.BOARD_ADVANCED.name,
                clinicalVignette = "A 54-year-old woman presents with recurrent episodes of right upper quadrant pain, low-grade fever, and fluctuating jaundice. Transabdominal ultrasound reveals a 2.5-cm impacted calculus in the gallbladder infundibulum with proximal common hepatic duct dilatation (11 mm) and a normal-caliber distal common bile duct. MRCP demonstrates a cholecystobiliary fistula involving approximately 40% of the common hepatic duct circumference.",
                questionPrompt = "According to the Csendes classification, what is the classification of this lesion, and what is the definitive surgical intervention?",
                optionsJson = serializeOptions(
                    listOf(
                        ExamOption("A", "Csendes Type I — Laparoscopic total cholecystectomy alone", "Type I is extrinsic compression without fistula; this patient has ductal wall destruction."),
                        ExamOption("B", "Csendes Type II — Laparoscopic cholecystectomy with primary ductal closure over a T-tube", "Defect exceeds 1/3 of duct circumference; direct suture closure risks severe biliary stricture."),
                        ExamOption("C", "Csendes Type III — Roux-en-Y Hepaticojejunostomy", "Correct. Fistula defect involving >1/3 to 2/3 of duct circumference is Type III; attempts at primary closure lead to high-grade stricture, mandating Roux-en-Y reconstruction."),
                        ExamOption("D", "Csendes Type IV — Urgent endoprosthesis placement followed by observation", "Complete fistula destruction requires operative bilioenteric bypass, not palliative stenting."),
                        ExamOption("E", "Csendes Type V — Right hemihepatectomy", "Type V indicates cholecystoenteric fistula (gallstone ileus); liver resection is unnecessary.")
                    )
                ),
                correctAnswerId = "C",
                comprehensiveExplanation = "This case represents Mirizzi Syndrome Csendes Type III. Csendes classification: Type I (extrinsic CHD compression by impacted stone), Type II (cholecystobiliary fistula involving <33% of duct circumference), Type III (fistula involving 33–66% of duct circumference), Type IV (complete destruction of duct wall), Type V (any type plus cholecystoenteric fistula). For Type III and IV, extensive ductal wall destruction precludes primary closure over T-tube due to guaranteed ischemic stricture; Roux-en-Y hepaticojejunostomy is the gold standard procedure.",
                boardPearls = "In suspected Mirizzi syndrome, intraoperative frozen section or fundus-first dissection is advised to avoid mistaking the dilated CHD for the gallbladder and transecting the main biliary confluence.",
                verifiedSourceProvenance = "Bailey & Love 28th Ed, Section 11 (HPB), Chapter 67."
            ),
            ExamQuestionEntity(
                id = "q_pancreas_002",
                topicId = "top_atlanta",
                topicTitle = "Acute Pancreatitis Severity",
                bookReference = "Schwartz 11th Ed, Ch 33, p. 1420",
                questionTypeName = QuestionType.NEXT_BEST_STEP.name,
                difficultyName = DifficultyLevel.BOARD_ADVANCED.name,
                clinicalVignette = "A 48-year-old male is admitted with severe gallstone pancreatitis. On day 18 of admission, while in the surgical step-down ward, he develops high swinging fevers (39.2°C), tachypnea, and worsening leukocytosis (WBC 21,500/mm³). Contrast-enhanced CT scan reveals extensive necrotizing pancreatitis involving >50% of the pancreatic parenchyma with multiple bubbles of retroperitoneal gas within a well-circumscribed collection.",
                questionPrompt = "According to the PANTER trial and current international step-up consensus guidelines, what is the next best step in management?",
                optionsJson = serializeOptions(
                    listOf(
                        ExamOption("A", "Immediate open necrosectomy and continuous postoperative retroperitoneal lavage", "Historic open necrosectomy carries a 40% mortality rate and has been supplanted by the step-up approach."),
                        ExamOption("B", "Percutaneous catheter drainage (PCD) or endoscopic transmural drainage of the collection", "Correct. The 'step-up approach' starts with minimally invasive catheter drainage (percutaneous or endoscopic); 35-50% of patients recover without requiring subsequent surgical necrosectomy."),
                        ExamOption("C", "Switch IV antibiotics to high-dose carbapenems alone and re-image in 2 weeks", "Presence of infected necrosis (retroperitoneal gas) with septic physiology requires mechanical source control (drainage)."),
                        ExamOption("D", "Immediate emergency laparoscopic subtotal pancreatectomy", "Resection of the pancreas in acute necrotizing pancreatitis is obsolete and hazardous."),
                        ExamOption("E", "Transjugular intrahepatic portosystemic shunt (TIPS)", "Irrelevant to infected pancreatic necrosis.")
                    )
                ),
                correctAnswerId = "B",
                comprehensiveExplanation = "The presence of extraluminal retroperitoneal gas within a pancreatic collection in a deteriorating septic patient establishes the diagnosis of infected pancreatic necrosis. According to the landmark PANTER trial (Dutch Pancreatitis Study Group), the minimally invasive 'Step-Up' approach is vastly superior to open necrosectomy (reducing major complications and long-term pancreatic insufficiency). The first step is image-guided percutaneous retroperitoneal catheter drainage or endoscopic cystogastrostomy. Video-assisted retroperitoneal debridement (VARD) is reserved only for patients who fail to improve after 72 hours of catheter drainage.",
                boardPearls = "Step-Up Approach rule: Delay invasive interventions for at least 4 weeks until the necrotic collection has encapsulated into 'Walled-Off Pancreatic Necrosis' (WOPN/WON).",
                verifiedSourceProvenance = "Schwartz's Principles of Surgery 11th Ed, Chapter 33; Dutch Pancreatitis Study Group PANTER Trial."
            )
        )

        examDao.insertQuestions(sampleQuestions)
    }

    private fun syncAttemptToFirestoreIfAuthenticated(attempt: ExamAttemptEntity) {
        if (!runCatching { FirebaseApp.getApps(context).isNotEmpty() && FirebaseAuth.getInstance().currentUser?.uid == attempt.userId }.getOrDefault(false)) return
        runCatching {
            val payload = mapOf(
                "id" to attempt.id,
                "examTitle" to attempt.examTitle,
                "scorePercentage" to attempt.scorePercentage,
                "correctCount" to attempt.correctCount,
                "totalQuestionsCount" to attempt.totalQuestionsCount,
                "completedAt" to FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(attempt.userId)
                .collection("examAttempts")
                .document(attempt.id)
                .set(payload, SetOptions.merge())
        }
    }

    private fun serializeOptions(options: List<ExamOption>): String {
        return options.joinToString("|||") { "${it.id}###${it.text}###${it.distractorRationale}" }
    }

    private fun deserializeOptions(raw: String): List<ExamOption> {
        if (raw.isBlank()) return emptyList()
        return raw.split("|||").mapNotNull { item ->
            val parts = item.split("###")
            if (parts.size >= 3) ExamOption(parts[0], parts[1], parts[2]) else null
        }
    }

    private fun serializeSubmissions(subs: List<QuestionSubmission>): String {
        return subs.joinToString("|||") { "${it.questionId}###${it.selectedOptionId}###${it.isCorrect}###${it.timeSpentSeconds}" }
    }

    private fun deserializeSubmissions(raw: String): List<QuestionSubmission> {
        if (raw.isBlank()) return emptyList()
        return raw.split("|||").mapNotNull { item ->
            val p = item.split("###")
            if (p.size >= 4) QuestionSubmission(p[0], p[1], p[2].toBoolean(), p[3].toIntOrNull() ?: 0) else null
        }
    }

    private fun ExamQuestionEntity.toDomainQuestion(): ExamQuestion = ExamQuestion(
        id = id,
        topicId = topicId,
        topicTitle = topicTitle,
        bookReference = bookReference,
        questionType = runCatching { QuestionType.valueOf(questionTypeName) }.getOrDefault(QuestionType.SINGLE_BEST_ANSWER),
        difficulty = runCatching { DifficultyLevel.valueOf(difficultyName) }.getOrDefault(DifficultyLevel.BOARD_ADVANCED),
        clinicalVignette = clinicalVignette,
        questionPrompt = questionPrompt,
        options = deserializeOptions(optionsJson),
        correctAnswerId = correctAnswerId,
        comprehensiveExplanation = comprehensiveExplanation,
        boardPearls = boardPearls,
        verifiedSourceProvenance = verifiedSourceProvenance
    )

    private fun ExamAttemptEntity.toDomainAttempt(): ExamAttempt = ExamAttempt(
        id = id,
        userId = userId,
        examTitle = examTitle,
        isTimedMode = isTimedMode,
        timeLimitMinutes = timeLimitMinutes,
        totalQuestionsCount = totalQuestionsCount,
        correctCount = correctCount,
        scorePercentage = scorePercentage,
        submissions = deserializeSubmissions(submissionsJson),
        completedAtEpochMillis = completedAtEpochMillis
    )
}
