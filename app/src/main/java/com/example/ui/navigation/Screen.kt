package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Register : Screen("register")
    object ScanAttendance : Screen("scan_attendance")
    object EmployeeList : Screen("employee_list")
    object AttendanceHistory : Screen("attendance_history")
    object Settings : Screen("settings")
}
