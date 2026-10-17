package com.surgicalboardmaster.app.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.surgicalboardmaster.app.feature.auth.AuthAccountDialog
import com.surgicalboardmaster.app.feature.auth.AuthViewModel
import com.surgicalboardmaster.app.feature.dashboard.DashboardScreen
import com.surgicalboardmaster.app.feature.dashboard.DashboardViewModel
import com.surgicalboardmaster.app.feature.library.SurgicalLibraryScreen
import com.surgicalboardmaster.app.feature.library.SurgicalLibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SbmAppShell(
    dashboardViewModel: DashboardViewModel,
    authViewModel: AuthViewModel,
    libraryViewModel: SurgicalLibraryViewModel,
    isArabicRtl: Boolean,
    isOfflineSimulated: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleOfflineMode: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: SbmDestination.DASHBOARD.route

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val authFeedback by authViewModel.feedbackMessage.collectAsStateWithLifecycle()
    var showAuthDialog by remember { mutableStateOf(false) }

    val bottomDestinations = SbmDestination.entries.filter { it.showInBottomBar }

    fun navigateToDestination(destination: SbmDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Surgical Board Master",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isArabicRtl) {
                            "بورد الجراحة العامة • Bailey & Love / Schwartz"
                        } else {
                            "General Surgery Board • Bailey & Love / Schwartz"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                SbmDestination.entries.forEach { destination ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = if (isArabicRtl) {
                                    "${destination.titleAr} (${destination.titleEn})"
                                } else {
                                    destination.titleEn
                                }
                            )
                        },
                        selected = currentRoute == destination.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateToDestination(destination)
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.titleEn
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val activeDest = SbmDestination.entries.find { it.route == currentRoute }
                            ?: SbmDestination.DASHBOARD
                        Text(
                            text = if (isArabicRtl) {
                                "${activeDest.titleAr} • ${activeDest.titleEn}"
                            } else {
                                activeDest.titleEn
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Drawer"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navigateToDestination(SbmDestination.GLOBAL_SEARCH) }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Global Search"
                            )
                        }
                        IconButton(onClick = onToggleOfflineMode) {
                            Icon(
                                imageVector = if (isOfflineSimulated) {
                                    Icons.Default.CloudOff
                                } else {
                                    Icons.Default.CloudDone
                                },
                                contentDescription = "Toggle Connectivity Mode"
                            )
                        }
                        IconButton(onClick = onToggleLanguage) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Toggle Arabic RTL / English LTR"
                            )
                        }
                        IconButton(onClick = { showAuthDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Account & Cloud Sync"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navigateToDestination(destination) },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.titleEn
                                )
                            },
                            label = {
                                Text(
                                    text = if (isArabicRtl) destination.titleAr else destination.titleEn,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = SbmDestination.DASHBOARD.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(SbmDestination.DASHBOARD.route) {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        isArabicRtl = isArabicRtl,
                        isOffline = isOfflineSimulated,
                        onNavigate = { dest -> navigateToDestination(dest) }
                    )
                }

                composable(SbmDestination.SURGICAL_LIBRARY.route) {
                    SurgicalLibraryScreen(
                        viewModel = libraryViewModel,
                        isArabicRtl = isArabicRtl,
                        onStartTutorForTopic = {
                            navigateToDestination(SbmDestination.AI_TUTOR)
                        }
                    )
                }

                // Remaining Phase 5-10 Module Destinations
                SbmDestination.entries
                    .filter {
                        it != SbmDestination.DASHBOARD &&
                            it != SbmDestination.SURGICAL_LIBRARY
                    }
                    .forEach { destination ->
                        composable(destination.route) {
                            ModuleFoundationPlaceholderScreen(
                                destination = destination,
                                isArabicRtl = isArabicRtl,
                                onBackToDashboard = { navigateToDestination(SbmDestination.DASHBOARD) }
                            )
                        }
                    }
            }
        }
    }

    if (showAuthDialog) {
        AuthAccountDialog(
            authState = authState,
            feedbackMessage = authFeedback,
            onSignIn = authViewModel::signIn,
            onRegister = authViewModel::register,
            onContinueOffline = authViewModel::switchToOfflineWorkstation,
            onSignOut = authViewModel::signOut,
            onDismiss = {
                authViewModel.clearFeedback()
                showAuthDialog = false
            }
        )
    }
}

@Composable
private fun ModuleFoundationPlaceholderScreen(
    destination: SbmDestination,
    isArabicRtl: Boolean,
    onBackToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.titleEn,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isArabicRtl) {
                            "${destination.titleAr} (${destination.titleEn})"
                        } else {
                            destination.titleEn
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Route '${destination.route}' is wired in Navigation Compose and protected by user-scoped repository contracts. Full interactive screens for this module are scheduled in the upcoming verified phase.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
