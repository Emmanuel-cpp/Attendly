package com.siamoonga.attendance.ui.lecturer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.siamoonga.attendance.model.Session
import com.siamoonga.attendance.ui.theme.Ink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private object ShellRoutes {
    const val COURSES = "lecturer_courses"
    const val REPORTS = "lecturer_reports"
    const val PROFILE = "lecturer_profile"
    const val SETTINGS = "lecturer_settings"
    const val LIVE_SESSION = "lecturer_live_session"
}

object LiveSessionHolder {
    private val _active = MutableStateFlow<Session?>(null)
    val active: StateFlow<Session?> = _active.asStateFlow()

    fun start(session: Session) { _active.value = session }
    fun clear() { _active.value = null }
    val current: Session? get() = _active.value
}

@Composable
fun LecturerHomeShell(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val activeSession by LiveSessionHolder.active.collectAsStateWithLifecycle()

    val bottomTabs = listOf(
        BottomTab(ShellRoutes.COURSES, "Courses", Icons.Filled.School),
        BottomTab(ShellRoutes.REPORTS, "Reports", Icons.Filled.BarChart),
        BottomTab(ShellRoutes.PROFILE, "Profile", Icons.Filled.Person)
    )

    val showBottomBar = currentRoute in bottomTabs.map { it.route }
    val showLiveBanner = activeSession != null && currentRoute != ShellRoutes.LIVE_SESSION

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                if (showLiveBanner) {
                    LiveSessionBanner(
                        session = activeSession!!,
                        onClick = { navController.navigate(ShellRoutes.LIVE_SESSION) }
                    )
                }
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = backStackEntry?.destination?.hierarchy
                                ?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != tab.route) {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ShellRoutes.COURSES,
            modifier = Modifier.padding(padding)
        ) {
            composable(ShellRoutes.COURSES) {
                LecturerCoursesScreen(
                    onLogout = onLogout,
                    onOpenSettings = { navController.navigate(ShellRoutes.SETTINGS) },
                    onSessionStarted = { session ->
                        LiveSessionHolder.start(session)
                        navController.navigate(ShellRoutes.LIVE_SESSION)
                    }
                )
            }
            composable(ShellRoutes.REPORTS) {
                LecturerReportsScreen(onLogout = onLogout)
            }
            composable(ShellRoutes.PROFILE) {
                LecturerProfileScreen(onLogout = onLogout)
            }
            composable(ShellRoutes.SETTINGS) {
                LecturerSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(ShellRoutes.LIVE_SESSION) {
                LiveSessionHolder.current?.let { session ->
                    LiveSessionScreen(
                        session = session,
                        onExit = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveSessionBanner(session: Session, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(0xFFE24B4A), CircleShape)
        )
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "${session.courseCode} · session in progress",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Tap to return to the live QR",
                color = Color(0xFF9AA0B6),
                fontSize = 11.sp
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color.White
        )
    }
}

private data class BottomTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)