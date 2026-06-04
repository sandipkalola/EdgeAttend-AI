package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AttendanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: AttendanceViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(navController = navController)
                        }
                        composable(Screen.Login.route) {
                            LoginScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Screen.Register.route) {
                            RegisterScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Screen.ScanAttendance.route) {
                            ScanAttendanceScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Screen.EmployeeList.route) {
                            EmployeeListScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Screen.AttendanceHistory.route) {
                            AttendanceHistoryScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(navController = navController, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
