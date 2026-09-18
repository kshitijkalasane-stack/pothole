package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PotholeStatus
import com.example.data.models.Severity
import com.example.data.models.UserRole
import com.example.ui.components.PotholeDetailModalSheet
import com.example.ui.screens.ActivityHistoryScreen
import com.example.ui.screens.AuthorityDashboardScreen
import com.example.ui.screens.CitizenHomeScreen
import com.example.ui.screens.InteractivePotholeMapScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ManualReportScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.PhenomenonCanvas
import com.example.ui.theme.PhenomenonCrimson
import com.example.ui.theme.PhenomenonCyanElectric
import com.example.ui.theme.PhenomenonElectricLime
import com.example.ui.theme.PhenomenonPurpleNeon
import com.example.ui.theme.PhenomenonSurface
import com.example.ui.theme.PhenomenonSurfaceElevated
import com.example.ui.theme.PhenomenonTextPrimary
import com.example.ui.theme.PhenomenonTextSecondary
import com.example.ui.theme.PhenomenonTextTertiary

@Composable
fun MainAppNavigationRoot(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val cloudPotholes by viewModel.cloudPotholes.collectAsState()
    val localDetections by viewModel.localDetections.collectAsState()
    val localReports by viewModel.localReports.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    if (!uiState.isLoggedIn) {
        LoginScreen(
            onCitizenLogin = { id, name -> viewModel.loginCitizen(id, name) },
            onAuthorityLogin = { offId, pass, dept -> viewModel.loginAuthority(offId, pass, dept) }
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(PhenomenonCanvas),
        bottomBar = {
            if (uiState.currentRole == UserRole.CITIZEN) {
                CitizenBottomNavigationBar(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            } else {
                AuthorityBottomNavigationBar(
                    selectedTab = uiState.authorityTab,
                    onTabSelected = { viewModel.selectAuthorityTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Role Views
            if (uiState.currentRole == UserRole.CITIZEN) {
                when (uiState.selectedTab) {
                    0 -> CitizenHomeScreen(
                        monitoringState = uiState.monitoringState,
                        userLocation = userLocation,
                        telemetry = telemetry,
                        nearbyPotholes = cloudPotholes,
                        onStartMonitoring = { viewModel.startMonitoring() },
                        onStopMonitoring = { viewModel.stopMonitoring() },
                        onSimulateBump = { viewModel.simulateDrivingBump(Severity.HIGH) },
                        onQuickReport = { viewModel.selectTab(2) },
                        onSyncNow = { viewModel.syncPendingData() },
                        onSelectPothole = { viewModel.inspectPothole(it) },
                        onSignOut = { viewModel.logout() }
                    )
                    1 -> InteractivePotholeMapScreen(
                        potholes = cloudPotholes,
                        userLocation = userLocation,
                        onSelectPothole = { viewModel.inspectPothole(it) }
                    )
                    2 -> ManualReportScreen(
                        userLocation = userLocation,
                        onSubmitReport = { sev, desc, road, photo ->
                            viewModel.submitReport(sev, desc, road, photo)
                            viewModel.selectTab(3)
                        }
                    )
                    3 -> ActivityHistoryScreen(
                        detections = localDetections,
                        reports = localReports,
                        onSyncNow = { viewModel.syncPendingData() }
                    )
                    4 -> ProfileScreen(
                        userProfile = uiState.userProfile,
                        onLogout = { viewModel.logout() }
                    )
                }
            } else {
                // Authority Mode Views
                when (uiState.authorityTab) {
                    0 -> AuthorityDashboardScreen(
                        potholes = cloudPotholes,
                        aiSummary = uiState.aiSummaryText,
                        isAiLoading = uiState.isAiLoading,
                        onGenerateAiSummary = { viewModel.generateAiAreaSummary() },
                        onSelectPothole = { viewModel.inspectPothole(it) },
                        onUpdatePotholeStatus = { id, newStatus, crew ->
                            viewModel.updatePotholeStatus(id, newStatus, crew)
                        },
                        onSignOut = { viewModel.logout() }
                    )
                    1 -> InteractivePotholeMapScreen(
                        potholes = cloudPotholes,
                        userLocation = userLocation,
                        onSelectPothole = { viewModel.inspectPothole(it) }
                    )
                    2 -> ProfileScreen(
                        userProfile = uiState.userProfile,
                        onLogout = { viewModel.logout() }
                    )
                }
            }

            // Top Alert Banner with Phenomenon styling
            AnimatedVisibility(
                visible = uiState.alertMessage != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                uiState.alertMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PhenomenonElectricLime)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = PhenomenonCanvas,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    color = PhenomenonCanvas,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearAlert() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = PhenomenonCanvas,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Inspection Detail Modal Sheet
            if (uiState.activePotholeDetail != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .clickable { viewModel.inspectPothole(null) },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    PotholeDetailModalSheet(
                        pothole = uiState.activePotholeDetail!!,
                        userRole = uiState.currentRole,
                        onDismiss = { viewModel.inspectPothole(null) },
                        onVerifyPothole = {
                            viewModel.updatePotholeStatus(uiState.activePotholeDetail!!.potholeId, PotholeStatus.UNDER_VERIFICATION)
                        },
                        onUpdateStatus = { newStatus, crew ->
                            viewModel.updatePotholeStatus(uiState.activePotholeDetail!!.potholeId, newStatus, crew)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CitizenBottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = PhenomenonSurface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("citizen_bottom_nav")
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Monitor") },
            label = { Text("Monitor", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PhenomenonCanvas,
                selectedTextColor = PhenomenonElectricLime,
                indicatorColor = PhenomenonElectricLime,
                unselectedIconColor = PhenomenonTextTertiary,
                unselectedTextColor = PhenomenonTextTertiary
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
            label = { Text("Map", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PhenomenonCanvas,
                selectedTextColor = PhenomenonElectricLime,
                indicatorColor = PhenomenonElectricLime,
                unselectedIconColor = PhenomenonTextTertiary,
                unselectedTextColor = PhenomenonTextTertiary
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = "Report") },
            label = { Text("Report", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PhenomenonCanvas,
                selectedTextColor = PhenomenonElectricLime,
                indicatorColor = PhenomenonElectricLime,
                unselectedIconColor = PhenomenonTextTertiary,
                unselectedTextColor = PhenomenonTextTertiary
            )
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.History, contentDescription = "Activity") },
            label = { Text("Activity", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PhenomenonCanvas,
                selectedTextColor = PhenomenonElectricLime,
                indicatorColor = PhenomenonElectricLime,
                unselectedIconColor = PhenomenonTextTertiary,
                unselectedTextColor = PhenomenonTextTertiary
            )
        )
        NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PhenomenonCanvas,
                selectedTextColor = PhenomenonElectricLime,
                indicatorColor = PhenomenonElectricLime,
                unselectedIconColor = PhenomenonTextTertiary,
                unselectedTextColor = PhenomenonTextTertiary
            )
        )
    }
}

@Composable
fun AuthorityBottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = PhenomenonSurface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("authority_bottom_nav")
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Queue") },
            label = { Text("Queue", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PhenomenonPurpleNeon,
                indicatorColor = PhenomenonPurpleNeon,
                unselectedIconColor = PhenomenonTextTertiary,
                unselectedTextColor = PhenomenonTextTertiary
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
            label = { Text("Zonal Map", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PhenomenonPurpleNeon,
                indicatorColor = PhenomenonPurpleNeon,
                unselectedIconColor = PhenomenonTextTertiary,
                unselectedTextColor = PhenomenonTextTertiary
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Console") },
            label = { Text("Console", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PhenomenonPurpleNeon,
                indicatorColor = PhenomenonPurpleNeon,
                unselectedIconColor = PhenomenonTextTertiary,
                unselectedTextColor = PhenomenonTextTertiary
            )
        )
    }
}
