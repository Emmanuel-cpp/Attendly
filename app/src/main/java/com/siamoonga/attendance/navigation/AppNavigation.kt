package com.siamoonga.attendance.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.siamoonga.attendance.model.Role
import com.siamoonga.attendance.ui.lecturer.LecturerHomeShell
import com.siamoonga.attendance.ui.login.LoginScreen
import com.siamoonga.attendance.ui.screens.PlaceholderScreen
import com.siamoonga.attendance.ui.student.StudentHomeShell

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = { role ->
                    val destination = when (role) {
                        Role.STUDENT -> Routes.STUDENT_HOME
                        Role.LECTURER -> Routes.LECTURER_HOME
                        Role.ADMIN -> Routes.ADMIN_HOME
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.STUDENT_HOME) {
            StudentHomeShell(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.STUDENT_HOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LECTURER_HOME) {
            LecturerHomeShell(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LECTURER_HOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ADMIN_HOME) { PlaceholderScreen("Admin Home") }
    }
}