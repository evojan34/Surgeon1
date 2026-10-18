package com.surgicalboardmaster.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surgicalboardmaster.app.core.navigation.SbmAppShell
import com.surgicalboardmaster.app.core.ui.theme.SurgicalBoardMasterTheme
import com.surgicalboardmaster.app.feature.auth.AuthViewModel
import com.surgicalboardmaster.app.feature.clinicalcases.ClinicalCasesViewModel
import com.surgicalboardmaster.app.feature.dashboard.DashboardViewModel
import com.surgicalboardmaster.app.feature.exams.ExamCenterViewModel
import com.surgicalboardmaster.app.feature.flashcards.FlashcardsViewModel
import com.surgicalboardmaster.app.feature.followups.FollowUpCalendarViewModel
import com.surgicalboardmaster.app.feature.library.SurgicalLibraryViewModel
import com.surgicalboardmaster.app.feature.search.GlobalSearchViewModel
import com.surgicalboardmaster.app.feature.simulator.ClinicalSimulatorViewModel
import com.surgicalboardmaster.app.feature.tutor.AiTutorViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val authViewModel: AuthViewModel = hiltViewModel()
            val libraryViewModel: SurgicalLibraryViewModel = hiltViewModel()
            val tutorViewModel: AiTutorViewModel = hiltViewModel()
            val examViewModel: ExamCenterViewModel = hiltViewModel()
            val flashcardsViewModel: FlashcardsViewModel = hiltViewModel()
            val simulatorViewModel: ClinicalSimulatorViewModel = hiltViewModel()
            val clinicalCasesViewModel: ClinicalCasesViewModel = hiltViewModel()
            val followUpCalendarViewModel: FollowUpCalendarViewModel = hiltViewModel()
            val globalSearchViewModel: GlobalSearchViewModel = hiltViewModel()

            val isArabicRtl by dashboardViewModel.isArabicRtl.collectAsStateWithLifecycle()
            val isOfflineSimulated by dashboardViewModel.isOfflineSimulated.collectAsStateWithLifecycle()

            SurgicalBoardMasterTheme(isArabicRtl = isArabicRtl) {
                SbmAppShell(
                    dashboardViewModel = dashboardViewModel,
                    authViewModel = authViewModel,
                    libraryViewModel = libraryViewModel,
                    tutorViewModel = tutorViewModel,
                    examViewModel = examViewModel,
                    flashcardsViewModel = flashcardsViewModel,
                    simulatorViewModel = simulatorViewModel,
                    clinicalCasesViewModel = clinicalCasesViewModel,
                    followUpCalendarViewModel = followUpCalendarViewModel,
                    globalSearchViewModel = globalSearchViewModel,
                    isArabicRtl = isArabicRtl,
                    isOfflineSimulated = isOfflineSimulated,
                    onToggleLanguage = dashboardViewModel::toggleLanguageDirection,
                    onToggleOfflineMode = dashboardViewModel::toggleOfflineMode
                )
            }
        }
    }
}
