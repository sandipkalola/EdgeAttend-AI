package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.AttendanceViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // PIN Form states
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    // GPS location parameters
    val fetchedGPSCoords by viewModel.fetchedGPSCoords.collectAsState()

    // Reset Confirmations
    var showResetDialog by remember { mutableStateOf(false) }
    var backupMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings Wizard", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BlueNavy, letterSpacing = (-0.5).sp) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(36.dp)
                            .background(SlateBackground, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = SlateTextPrimary,
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
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Administrator PIN Update Custom Form
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Password, contentDescription = "Pin Key", tint = BluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("1. Update Security Pin", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = BlueNavy)
                        }

                        Text(
                            "Change default PIN required to access administrative database. (Default pin is '1234')",
                            fontSize = 12.sp,
                            color = SlateTextSecondary,
                            lineHeight = 16.sp
                        )

                        OutlinedTextField(
                            value = oldPin,
                            onValueChange = { oldPin = it.take(4) },
                            placeholder = { Text("Current 4-digit Pin", color = SlateTextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SlateTextPrimary,
                                unfocusedTextColor = SlateTextPrimary,
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = SlateBorder,
                                focusedContainerColor = PureWhite,
                                unfocusedContainerColor = PureWhite,
                                focusedLabelColor = BluePrimary,
                                unfocusedLabelColor = SlateTextSecondary
                            )
                        )

                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { newPin = it.take(4) },
                            placeholder = { Text("New 4-digit Pin", color = SlateTextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SlateTextPrimary,
                                unfocusedTextColor = SlateTextPrimary,
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = SlateBorder,
                                focusedContainerColor = PureWhite,
                                unfocusedContainerColor = PureWhite,
                                focusedLabelColor = BluePrimary,
                                unfocusedLabelColor = SlateTextSecondary
                            )
                        )

                        Button(
                            onClick = {
                                if (oldPin.isBlank() || newPin.length < 4) {
                                    Toast.makeText(context, "Please enter a valid 4-digit PIN", Toast.LENGTH_SHORT).show()
                                } else {
                                    val success = viewModel.updatePin(oldPin, newPin)
                                    if (success) {
                                        Toast.makeText(context, "Security PIN updated successfully!", Toast.LENGTH_SHORT).show()
                                        oldPin = ""
                                        newPin = ""
                                    } else {
                                        Toast.makeText(context, "Current PIN incorrect", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Apply PIN Change", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        }
                    }
                }

                // Section 2: Real-time GPS Location Coordinates
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "GPS Lock", tint = BluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("2. Hardware GPS Coordinates", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = BlueNavy)
                        }

                        Text(
                            "The system logs precise real-time physical GPS coordinates to verify high-accuracy on-site presence. Captured real-time hardware status:",
                            fontSize = 12.sp,
                            color = SlateTextSecondary,
                            lineHeight = 16.sp
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Current GPS Coords", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateTextSecondary)
                                    Text(
                                        text = fetchedGPSCoords ?: "Acquiring satellite lock...",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (fetchedGPSCoords != null) BluePrimary else PendingYellow
                                    )
                                }

                                Button(
                                    onClick = { 
                                        viewModel.fetchRealTimeGPSLocation()
                                        Toast.makeText(context, "Scanning hardware GPS...", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = PureWhite, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Refresh", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                                }
                            }
                        }
                    }
                }

                // Section 3: Server Backup & Local Sync to JSON files
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Backup, contentDescription = "Sync Server Backup", tint = BluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("3. Server Backup & Local Sync", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = BlueNavy)
                        }

                        Text(
                            "Export all locally saved records and registered employees which are pending server audit. The system generates structured JSON backup text files, and flags the corresponding database entries as 'completed' without removing them.",
                            fontSize = 12.sp,
                            color = SlateTextSecondary,
                            lineHeight = 16.sp
                        )

                        Button(
                            onClick = {
                                viewModel.backupPendingData(context) { success, msg ->
                                    if (success) {
                                        backupMessage = msg
                                    } else {
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Upload", tint = PureWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync & Export Backup Now", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        }
                    }
                }

                // Section 4: Diagnostic settings and bypass switch panel
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Dns, contentDescription = "Biometric Bypass", tint = BluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("4. Biometric Diagnostic Modes", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = BlueNavy)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Fast Match Testing Bypass", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateTextPrimary)
                                Text("Skips interactive blink/pose checks and matching triggers directly on employee target database.", fontSize = 11.sp, color = SlateTextSecondary, lineHeight = 14.sp)
                            }

                            Switch(
                                checked = viewModel.bypassLiveness,
                                onCheckedChange = { enabled ->
                                    viewModel.setLivenessBypass(enabled)
                                    Toast.makeText(context, if (enabled) "Fast-Match Bypass Enabled!" else "Liveness Security Re-Enforced!", Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureWhite,
                                    checkedTrackColor = BluePrimary,
                                    uncheckedThumbColor = SlateTextSecondary,
                                    uncheckedTrackColor = SlateBorder
                                )
                            )
                        }
                    }
                }

                // Section 4: Emergency database reset mechanisms
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Purge logs", tint = AlertRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emergency Database Wipe", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = AlertRed)
                        }

                        Text(
                            "CRITICAL ACTION: This purges all SQLite biometric templates, captured face embeddings, and local matched logs on this device completely. This action cannot be undone.",
                            fontSize = 12.sp,
                            color = AlertRed,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Purge Offline Datastores", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // System info stats footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EdgeAttend Platform Console v3.1",
                        fontSize = 11.sp,
                        color = SlateTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Complies fully to local AES-256 and FIPS Biometric params. Running local SQLite on-device only.",
                        fontSize = 10.sp,
                        color = SlateTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Master Wipe Confirm dialogue
        if (showResetDialog) {
            AlertDialog(
                containerColor = PureWhite,
                onDismissRequest = { showResetDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearDatabase()
                            showResetDialog = false
                            Toast.makeText(context, "All tables wiped!", Toast.LENGTH_LONG).show()
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Confirm Wiping DB", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showResetDialog = false },
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = SlateTextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                title = { Text("Danger: Wipe SQLite Stores?", fontWeight = FontWeight.ExtraBold, color = BlueNavy, fontSize = 18.sp) },
                text = {
                    Text(
                        "This process completely destroys all offline staff profiles, face descriptors, and matched check-in verification log logs. Proceed with wipe?",
                        fontSize = 13.sp,
                        color = SlateTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            )
        }

        if (backupMessage != null) {
            AlertDialog(
                containerColor = PureWhite,
                onDismissRequest = { backupMessage = null },
                confirmButton = {
                    Button(
                        onClick = { backupMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("OK", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                },
                title = { Text("Backup & Sync Status", fontWeight = FontWeight.ExtraBold, color = BlueNavy, fontSize = 18.sp) },
                text = {
                    Text(
                        text = backupMessage ?: "",
                        fontSize = 13.sp,
                        color = SlateTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            )
        }
    }
}

@Composable
fun PresetButton(label: String, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onSelect,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateTextPrimary, containerColor = PureWhite),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = modifier.height(34.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
