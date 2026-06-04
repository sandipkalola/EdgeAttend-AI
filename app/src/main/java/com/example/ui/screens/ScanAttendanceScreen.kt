package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.biometrics.LivenessStep
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AttendanceViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


fun isLocationServicesEnabled(context: android.content.Context): Boolean {
    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
    return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
           locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanAttendanceScreen(navController: NavController, viewModel: AttendanceViewModel) {
    val scanFeedback by viewModel.scanFeedback.collectAsState()
    val scanStatusMessage by viewModel.scanStatusMessage.collectAsState()
    val scannedMatchResult by viewModel.scannedMatchResult.collectAsState()
    val scanConfidence by viewModel.scanConfidence.collectAsState()
    val attendanceLogType by viewModel.attendanceLogType.collectAsState()
    val recentAttendanceCompleted by viewModel.recentAttendanceCompleted.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()
    val invalidPersonAttempted by viewModel.invalidPersonAttempted.collectAsState()
    val simulateStrangerFace by viewModel.simulateStrangerFace.collectAsState()
    val isFaceDetectedInCamera by viewModel.isFaceDetectedInCamera.collectAsState()

    val context = LocalContext.current
    var showLocationRequestDialog by remember { mutableStateOf(false) }

    var countdownSeconds by remember { mutableStateOf(3) }
    var isScanningActive by remember { mutableStateOf(false) }

    // Geolocation and Camera permission check state
    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var isDeviceLocationEnabled by remember {
        mutableStateOf(isLocationServicesEnabled(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        cameraPermissionGranted = permissions[android.Manifest.permission.CAMERA] ?: cameraPermissionGranted
        locationPermissionGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: locationPermissionGranted
        if (locationPermissionGranted) {
            viewModel.fetchRealTimeGPSLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted || !locationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.fetchRealTimeGPSLocation()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            isDeviceLocationEnabled = isLocationServicesEnabled(context)
            if (isDeviceLocationEnabled && locationPermissionGranted) {
                viewModel.fetchRealTimeGPSLocation()
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    LaunchedEffect(
        cameraPermissionGranted,
        isFaceDetectedInCamera,
        locationPermissionGranted,
        isDeviceLocationEnabled,
        attendanceLogType,
        scannedMatchResult,
        recentAttendanceCompleted,
        invalidPersonAttempted,
        simulateStrangerFace
    ) {
        if (cameraPermissionGranted &&
            locationPermissionGranted &&
            isDeviceLocationEnabled &&
            isFaceDetectedInCamera &&
            scannedMatchResult == null &&
            recentAttendanceCompleted == null &&
            !invalidPersonAttempted
        ) {
            isScanningActive = true
            for (i in 3 downTo 1) {
                countdownSeconds = i
                kotlinx.coroutines.delay(1000)
            }
            // final validation on count finish
            if (isFaceDetectedInCamera &&
                locationPermissionGranted &&
                isDeviceLocationEnabled &&
                scannedMatchResult == null &&
                recentAttendanceCompleted == null &&
                !invalidPersonAttempted
            ) {
                viewModel.fetchRealTimeGPSLocation()
                viewModel.forceBypassLivenessSuccess(null)
            }
            isScanningActive = false
        } else {
            isScanningActive = false
            countdownSeconds = 3
        }
    }

    // High performance selfie picture capture launcher
    val takePicturePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val selfieFile = File(context.cacheDir, "selfie_${System.currentTimeMillis()}.png")
            try {
                FileOutputStream(selfieFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                }
                // Verify, run match with targeted staff profile, and log real-time coords
                viewModel.fetchRealTimeGPSLocation()
                viewModel.forceBypassLivenessSuccess(selfieFile.absolutePath)
            } catch (e: IOException) {
                e.printStackTrace()
                viewModel.forceBypassLivenessSuccess(null)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biometric Scan Panel", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BlueNavy, letterSpacing = (-0.5).sp) },
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
            HorizontalDivider(color = SlateBorder, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Check-in / Check-out Toggle selector in Clean Minimalism high contrast
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    listOf("CHECK_IN", "CHECK_OUT").forEach { type ->
                        val isSelected = attendanceLogType == type
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) PureWhite
                                    else Color.Transparent
                                )
                                .clickable { viewModel.selectAttendanceType(type) }
                        ) {
                            Text(
                                text = if (type == "CHECK_IN") "CHECK-IN" else "CHECK-OUT",
                                color = if (isSelected) BluePrimary else SlateTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                              )
                        }
                    }
                }

                // High Fidelity Live Camera Preview viewfinder inside a Slate Card
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val activeStatusColor = when {
                            scannedMatchResult != null -> EmeraldLive
                            invalidPersonAttempted -> AlertRed
                            isScanningActive -> BluePrimary
                            else -> Color(0xFFF59E0B) // Amber
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.25f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F172A)), // Sleek slate-900 background
                            contentAlignment = Alignment.Center
                        ) {
                            if (cameraPermissionGranted && locationPermissionGranted && isDeviceLocationEnabled) {
                                CameraPreview(
                                    modifier = Modifier.fillMaxSize(),
                                    onFaceDetected = { detected, width, eyes, mouth ->
                                        viewModel.updateFaceDetectionState(detected, width, eyes, mouth)
                                    }
                                )
                            } else {
                                LaunchedEffect(Unit) {
                                    viewModel.updateFaceDetectionState(false)
                                }
                            }

                            if (!locationPermissionGranted || !isDeviceLocationEnabled) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xE60F172A))
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOff,
                                        contentDescription = "Location Access Required",
                                        tint = AlertRed,
                                        modifier = Modifier.size(52.dp)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "GPS / Location Coordinates Required",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = PureWhite,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (!locationPermissionGranted) {
                                            "This application requires location permissions to record on-site biometric attendance logs. Please authorize access."
                                        } else {
                                            "On-device GPS / Location service is currently disabled. Please enable it in system settings to run verification scans."
                                        },
                                        fontSize = 11.sp,
                                        color = SlateTextMuted,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            if (!locationPermissionGranted) {
                                                permissionLauncher.launch(
                                                    arrayOf(
                                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            } else {
                                                try {
                                                    val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = if (!locationPermissionGranted) "Authorize Permission" else "Enable Hardware Location/GPS",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite
                                        )
                                    }
                                }
                            } else {
                                // Circular liveness overlay mask
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, activeStatusColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!cameraPermissionGranted) {
                                        Icon(
                                            imageVector = Icons.Default.Face,
                                            contentDescription = "Facial alignment alignment",
                                            tint = activeStatusColor.copy(alpha = 0.25f),
                                            modifier = Modifier.size(110.dp)
                                        )
                                    }
                                    // Overlaid scanning laser simulator line
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(activeStatusColor.copy(alpha = 0.6f))
                                            .align(Alignment.Center)
                                    )
                                }
                            }

                            if (!cameraPermissionGranted && locationPermissionGranted && isDeviceLocationEnabled) {
                                Text(
                                    text = "Camera Permission Required",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp)
                                )
                            }
                        }

                        // Guidance Status Text Message
                        Text(
                            text = scanStatusMessage,
                            color = SlateTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp)
                        )
                    }
                }

                // Dynamic Status Banner for completely hands-free camera-pose process
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (scannedMatchResult != null) {
                            EmeraldLive.copy(alpha = 0.1f)
                        } else if (invalidPersonAttempted) {
                            AlertRed.copy(alpha = 0.1f)
                        } else if (isScanningActive) {
                            BluePrimary.copy(alpha = 0.1f)
                        } else {
                            Color(0xFFF59E0B).copy(alpha = 0.1f) // Amber
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (scannedMatchResult != null) {
                            EmeraldLive
                        } else if (invalidPersonAttempted) {
                            AlertRed
                        } else if (isScanningActive) {
                            BluePrimary
                        } else {
                            Color(0xFFF59E0B) // Amber
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (scannedMatchResult != null) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Matched",
                                tint = EmeraldLiveDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "VERIFIED: ${scannedMatchResult?.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLiveDark
                            )
                        } else if (invalidPersonAttempted) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Failed",
                                tint = AlertRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "VERIFICATION FAILED",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                        } else if (isScanningActive) {
                            CircularProgressIndicator(
                                color = BluePrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "AUTOMATIC FACE DETECTING ($countdownSeconds" + "s)...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Awaiting Face",
                                tint = Color(0xFFD97706), // Amber dark
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "AWAITING FACE IN CAMERA VIEWPORT",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }

                // Liveness criteria check-list feedback board
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ANTI-SPOOF LIVENESS DIRECTORY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextSecondary,
                            letterSpacing = 1.sp
                        )

                        LivenessCheckRow(
                            title = "1. Smiling Portrait Verification",
                            status = scanFeedback.lookStraightAndSmileStatus,
                            progress = scanFeedback.smileProgress
                        )
                        LivenessCheckRow(
                            title = "2. Eye Blink Speed Benchmark",
                            status = scanFeedback.blinkStatus,
                            progress = scanFeedback.blinkProgress
                        )
                        LivenessCheckRow(
                            title = "3. Micro-Turn Spatial Headpose",
                            status = scanFeedback.headMovementStatus,
                            progress = scanFeedback.headTurnProgress
                        )
                    }
                }

                // DIAGNOSTIC DEVELOPER CONTROLS MATCHING DESIGN RULES
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "DIAGNOSTIC DEVELOPER CONTROLS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BlueNavy,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.forceBypassLivenessSuccess() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateTextPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Full Bypass (Fast Match)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { viewModel.startFaceScanningVerification() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Reset Reader", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AlertRed)
                            }
                        }
                    }
                }

                // Results overlay success dialog banner showing live GPS and taken selfie
                AnimatedVisibility(
                    visible = scannedMatchResult != null && recentAttendanceCompleted != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    scannedMatchResult?.let { emp ->
                        recentAttendanceCompleted?.let { log ->
                            AlertDialog(
                                containerColor = PureWhite,
                                onDismissRequest = { viewModel.startFaceScanningVerification() },
                                confirmButton = {
                                    Button(
                                        onClick = { navController.popBackStack() },
                                        colors = ButtonDefaults.buttonColors(containerColor = BlueNavy),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Go to Dashboard", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                },
                                dismissButton = {
                                    OutlinedButton(
                                        onClick = { viewModel.startFaceScanningVerification() },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateTextPrimary),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Scan Next Person", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Success check",
                                            tint = EmeraldLive,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Verified Successfully", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BlueNavy)
                                    }
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (log.photoPath != null) {
                                            Text("SAVED ATTENDANCE SELFIE:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateTextSecondary)
                                            AsyncImage(
                                                model = File(log.photoPath),
                                                contentDescription = "Attendance Selfie Snapshot",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                                    .background(Color.LightGray),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Text("Staff: ${emp.name}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = SlateTextPrimary)
                                        Text("ID: ${emp.id}", fontSize = 12.sp, color = SlateTextSecondary)
                                        Text("GPS Real Coords: ${log.gpsLocation}", fontSize = 12.sp, color = SlateTextSecondary, fontWeight = FontWeight.Bold)
                                        Text("Similarity Score: ${String.format("%.1f", log.confidenceScore)}% match", fontSize = 12.sp, color = SlateTextSecondary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                                        ) {
                                            Text(
                                                text = "Real-time selfie and GPS lat-long logged successfully in database.",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldLiveDark,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (invalidPersonAttempted) {
        AlertDialog(
            onDismissRequest = { viewModel.resetInvalidPersonAttempt() },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetInvalidPersonAttempt() },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Try Again", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Failure check",
                        tint = AlertRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invalid Person", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BlueNavy)
                }
            },
            text = {
                Text(
                    text = "No matching face hash was found in the database. Please make sure the employee is correctly registered before clocking in/out.",
                    fontSize = 14.sp,
                    color = SlateTextPrimary
                )
            }
        )
    }

    if (showLocationRequestDialog) {
        AlertDialog(
            onDismissRequest = { showLocationRequestDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showLocationRequestDialog = false
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Enable Location", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLocationRequestDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateTextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Location icon",
                        tint = BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Location Required", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BlueNavy)
                }
            },
            text = {
                Text(
                    text = "This app requires location coordinates to mark biometric attendance. Please click 'Enable Location' to authorize tracking.",
                    fontSize = 14.sp,
                    color = SlateTextPrimary
                )
            }
        )
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA,
    onFaceDetected: (Boolean, Float, Float, Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val faceDetector = remember {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()
        FaceDetection.getClient(options)
    }

    val analysisExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            faceDetector.process(image)
                                .addOnSuccessListener { faces ->
                                    val detected = faces.isNotEmpty()
                                    if (detected) {
                                        val mainFace = faces.first()
                                        
                                        // Extract key facial landmarks to form a unique offline biometric signature
                                        val leftEye = mainFace.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_EYE)
                                        val rightEye = mainFace.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_EYE)
                                        val noseBase = mainFace.getLandmark(com.google.mlkit.vision.face.FaceLandmark.NOSE_BASE)
                                        val mouthBottom = mainFace.getLandmark(com.google.mlkit.vision.face.FaceLandmark.MOUTH_BOTTOM)

                                        val bboxWidth = mainFace.boundingBox.width().toFloat()
                                        val frameWidth = imageProxy.width.toFloat()
                                        val faceWidthRatio = if (frameWidth > 0) bboxWidth / frameWidth else 0.6f

                                        var eyeSpacing = 0.42f
                                        var mouthHeight = 0.25f

                                        if (leftEye != null && rightEye != null) {
                                            val dx = leftEye.position.x - rightEye.position.x
                                            val dy = leftEye.position.y - rightEye.position.y
                                            eyeSpacing = kotlin.math.sqrt(dx * dx + dy * dy) / bboxWidth
                                        }

                                        if (mouthBottom != null && noseBase != null) {
                                            val dx = mouthBottom.position.x - noseBase.position.x
                                            val dy = mouthBottom.position.y - noseBase.position.y
                                            mouthHeight = kotlin.math.sqrt(dx * dx + dy * dy) / bboxWidth
                                        }

                                        onFaceDetected(true, faceWidthRatio, eyeSpacing, mouthHeight)
                                    } else {
                                        onFaceDetected(false, 0f, 0.42f, 0.25f)
                                    }
                                    imageProxy.close()
                                }
                                .addOnFailureListener {
                                    onFaceDetected(false, 0f, 0.42f, 0.25f)
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
            previewView
        },
        modifier = modifier
    )
}

@Composable
fun LivenessCheckRow(title: String, status: String, progress: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Dot indicator
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    if (progress >= 1f) EmeraldLive
                    else SlateBorder
                )
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
            Text(
                text = status,
                fontSize = 11.sp,
                color = if (progress >= 1f) EmeraldLiveDark else SlateTextSecondary,
                fontWeight = if (progress >= 1f) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
