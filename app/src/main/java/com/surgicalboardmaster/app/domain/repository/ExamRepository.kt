package com.surgicalboardmaster.app.domain.repository

import com.surgicalboardmaster.app.domain.model.ExamAttempt
import com.surgicalboardmaster.app.domain.model.ExamQuestion
import com.surgicalboardmaster.app.domain.model.QuestionSubmission
import com.surgicalboardmaster.app.domain.model.WrongAnswerItem
import kotlinx.coroutines.flow.Flow

interface ExamRepository {
    fun observeAllQuestions(): Flow<List<ExamQuestion>>
    fun observeExamAttempts(userId: String): Flow<List<ExamAttempt>>
    suspend fun getQuestionsForExam(topicId: String?, limit: Int): List<ExamQuestion>
    suspend fun submitExamAttempt(
        userId: String,
        examTitle: String,
        isTimedMode: Boolean,
        timeLimitMinutes: Int,
        submissions: List<QuestionSubmission>
    ): Result<ExamAttempt>
    suspend fun observeWrongAnswerNotebook(userId: String): List<WrongAnswerItem>
    suspend fun ensureDefaultQuestionsSeeded()
}
