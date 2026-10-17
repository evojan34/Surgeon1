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
import com.surgicalboardmaster.app.feature.dashboard.DashboardViewModel
import com.surgicalboardmaster.app.feature.library.SurgicalLibraryViewModel
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

            val isArabicRtl by dashboardViewModel.isArabicRtl.collectAsStateWithLifecycle()
            val isOfflineSimulated by dashboardViewModel.isOfflineSimulated.collectAsStateWithLifecycle()

            SurgicalBoardMasterTheme(isArabicRtl = isArabicRtl) {
                SbmAppShell(
                    dashboardViewModel = dashboardViewModel,
                    authViewModel = authViewModel,
                    libraryViewModel = libraryViewModel,
                    isArabicRtl = isArabicRtl,
                    isOfflineSimulated = isOfflineSimulated,
                    onToggleLanguage = dashboardViewModel::toggleLanguageDirection,
                    onToggleOfflineMode = dashboardViewModel::toggleOfflineMode
                )
            }
        }
    }
}
