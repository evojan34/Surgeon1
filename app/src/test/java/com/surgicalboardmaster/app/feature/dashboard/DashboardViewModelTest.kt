package com.surgicalboardmaster.app.feature.dashboard

import com.surgicalboardmaster.app.data.repository.OfflineFirstDashboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: OfflineFirstDashboardRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = OfflineFirstDashboardRepository()
        viewModel = DashboardViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `repository emits real Bailey and Love and Schwartz book progress`() = runTest {
        val summary = repository.observeDashboardSummary().first()
        assertEquals(2, summary.books.size)
        assertTrue(summary.books.any { it.title.contains("Bailey & Love") })
        assertTrue(summary.books.any { it.title.contains("Schwartz") })
    }

    @Test
    fun `completing follow-up task removes it from pending follow-ups list`() = runTest {
        val initialSummary = repository.observeDashboardSummary().first()
        val initialCount = initialSummary.pendingFollowUps.size
        val targetTaskId = initialSummary.pendingFollowUps.first().taskId

        viewModel.completeFollowUp(targetTaskId)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedSummary = repository.observeDashboardSummary().first()
        assertEquals(initialCount - 1, updatedSummary.pendingFollowUps.size)
        assertFalse(updatedSummary.pendingFollowUps.any { it.taskId == targetTaskId })
    }

    @Test
    fun `recording quick topic revision increments completed topics count`() = runTest {
        val initialSummary = repository.observeDashboardSummary().first()
        val initialCompleted = initialSummary.completedTopicsTotal
        val weakTopicId = initialSummary.weakTopics.first().id

        viewModel.markTopicRevised(weakTopicId)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedSummary = repository.observeDashboardSummary().first()
        assertEquals(initialCompleted + 1, updatedSummary.completedTopicsTotal)
        assertFalse(updatedSummary.weakTopics.any { it.id == weakTopicId })
    }
}

