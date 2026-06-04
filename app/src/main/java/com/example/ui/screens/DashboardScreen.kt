package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.theme.*
import androidx.compose.runtime.remember


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: AttendanceViewModel) {
    val totalEmployees by viewModel.allEmployees.collectAsState()
    val todayAttendanceCount by viewModel.todayLogsCount.collectAsState()
    val totalLogs by viewModel.allLogs.collectAsState()

    val lastEntryTime = remember(totalLogs) {
        if (totalLogs.isNotEmpty()) {
            val lastLog = totalLogs.maxByOrNull { it.timestamp }
            if (lastLog != null) {
                val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                sdf.format(java.util.Date(lastLog.timestamp))
            } else {
                "None"
            }
        } else {
            "None yet"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "EdgeAttend AI",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BlueNavy,
                            letterSpacing = (-0.5).sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldLive)
                            )
                            Text(
                                text = "OFFLINE ENGINE ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Screen.Settings.route) },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(38.dp)
                            .background(
                                color = SlateBackground,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "System Settings",
                            tint = SlateTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = Color(0xFFFEE2E2),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout Admin",
                            tint = AlertRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBackground)
                .padding(innerPadding)
        ) {
            // Elegant 1px minimalist header boundary
            Divider(color = SlateBorder, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2-Column Summary Dashboard Stats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MinimalStatCard(
                        title = "REGISTERED",
                        value = totalEmployees.size.toString(),
                        footerText = "On-device database",
                        valueColor = BlueNavy,
                        modifier = Modifier.weight(1f)
                    )
                    MinimalStatCard(
                        title = "TODAY",
                        value = todayAttendanceCount.toString(),
                        footerText = "Last: $lastEntryTime",
                        valueColor = EmeraldLiveDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "OPERATIONS DIRECTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )

                // Quick Action Grid matching Clean Minimalism
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        MinimalActionCard(
                            title = "Verify Face Scan",
                            subtitle = "Liveness Match Checklist",
                            icon = Icons.Default.Face,
                            iconBg = Color(0xFFEFF6FF),
                            iconTint = BluePrimary
                        ) {
                            viewModel.startFaceScanningVerification()
                            navController.navigate(Screen.ScanAttendance.route)
                        }
                    }

                    item {
                        MinimalActionCard(
                            title = "Enroll Employee",
                            subtitle = "Multi-Angle Capture",
                            icon = Icons.Default.PersonAdd,
                            iconBg = Color(0xFFECFDF5),
                            iconTint = EmeraldLiveDark
                        ) {
                            viewModel.resetRegistration()
                            navController.navigate(Screen.Register.route)
                        }
                    }

                    item {
                        MinimalActionCard(
                            title = "Staff Database",
                            subtitle = "View registered profiles",
                            icon = Icons.Default.List,
                            iconBg = Color(0xFFFAF5FF),
                            iconTint = Color(0xFF9333EA)
                        ) {
                            navController.navigate(Screen.EmployeeList.route)
                        }
                    }

                    item {
                        MinimalActionCard(
                            title = "Historical Logs",
                            subtitle = "Local Verification log activity",
                            icon = Icons.Default.HistoryToggleOff,
                            iconBg = Color(0xFFF1F5F9),
                            iconTint = SlateTextPrimary
                        ) {
                            navController.navigate(Screen.AttendanceHistory.route)
                        }
                    }
                }

                // Device Health Footer matching the Clean Minimalism HTML
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF1F5F9)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(BlueSecondary)
                            )
                            Text(
                                text = "Local Database Encrypted (AES-256)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateTextSecondary,
                                style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            )
                        }

                        Text(
                            text = "RAM: 1.2GB FREE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateTextMuted,
                            letterSpacing = (-0.2).sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalStatCard(
    title: String,
    value: String,
    footerText: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = SlateTextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = footerText,
                fontSize = 10.sp,
                color = SlateTextMuted,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun MinimalActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary,
                letterSpacing = (-0.2).sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = SlateTextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}
