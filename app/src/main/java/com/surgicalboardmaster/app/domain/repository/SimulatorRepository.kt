package com.surgicalboardmaster.app.domain.repository

import com.surgicalboardmaster.app.domain.model.SimulationDebriefReport
import com.surgicalboardmaster.app.domain.model.SimulationScenario
import com.surgicalboardmaster.app.domain.model.SurgicalDomain
import kotlinx.coroutines.flow.Flow

interface SimulatorRepository {
    fun observeAvailableScenarios(domainFilter: SurgicalDomain? = null): Flow<List<SimulationScenario>>
    fun observeCompletedDebriefs(userId: String): Flow<List<SimulationDebriefReport>>
    suspend fun getScenarioById(scenarioId: String): SimulationScenario?
    suspend fun recordSimulationDebrief(userId: String, report: SimulationDebriefReport): Result<Unit>
}
