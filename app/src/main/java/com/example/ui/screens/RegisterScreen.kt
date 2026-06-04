package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.viewmodel.AttendanceViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.ui.theme.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, viewModel: AttendanceViewModel) {
    val regEmployeeId by viewModel.regEmployeeId.collectAsState()
    val regEmployeeName by viewModel.regEmployeeName.collectAsState()
    val registrationStep by viewModel.registrationStep.collectAsState()
    val registrationError by viewModel.registrationError.collectAsState()

    val capturedFrontPath by viewModel.capturedFrontPath.collectAsState()
    val capturedLeftPath by viewModel.capturedLeftPath.collectAsState()
    val capturedRightPath by viewModel.capturedRightPath.collectAsState()

    var tempId by remember { mutableStateOf("") }
    var tempName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biometric Enrollment", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BlueNavy, letterSpacing = (-0.5).sp) },
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
            ) {
                // Visual Progress bar step-by-step
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val stepsList = listOf(
                        1 to "Details",
                        2 to "Face Biometric",
                        5 to "Verify"
                    )
                    stepsList.forEach { (stepNum, stepName) ->
                        val isActive = if (stepNum == 5) registrationStep >= 5 else registrationStep >= stepNum
                        val isCurrent = (registrationStep == stepNum) || (stepNum == 2 && (registrationStep == 2 || registrationStep == 3 || registrationStep == 4))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (isActive) BluePrimary
                                        else SlateBorder
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stepName,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) BluePrimary else SlateTextSecondary
                            )
                        }
                    }
                }

                // Error display space
                if (registrationError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Error, contentDescription = "Error", tint = AlertRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = registrationError ?: "",
                                color = AlertRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when (registrationStep) {
                        1 -> StepEmployeeDetails(
                            idInput = tempId,
                            onIdChange = { tempId = it },
                            nameInput = tempName,
                            onNameChange = { tempName = it },
                            onNext = { viewModel.setRegInfo(tempId.trim(), tempName.trim()) }
                        )
                        2, 3, 4 -> StepCaptureBiometrics(
                            angle = "FRONT",
                            description = "Position face inside frame to scan.",
                            onCapture = { width, eyes, mouth, path -> viewModel.captureAngleBiometrics("FRONT", width, eyes, mouth, path) }
                        )
                        5 -> StepReviewAndSubmit(
                            id = regEmployeeId,
                            name = regEmployeeName,
                            frontOk = capturedFrontPath != null,
                            leftOk = capturedLeftPath != null,
                            rightOk = capturedRightPath != null,
                            photoPath = capturedFrontPath,
                            onSubmit = {
                                val success = viewModel.submitEmployeeRegistration(navController.context)
                                if (success) {
                                    tempId = ""
                                    tempName = ""
                                    navController.popBackStack()
                                }
                            },
                            onReset = { viewModel.resetRegistration() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepEmployeeDetails(
    idInput: String,
    onIdChange: (String) -> Unit,
    nameInput: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "1. Enter Employee Records",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BlueNavy,
                letterSpacing = (-0.3).sp
            )

            OutlinedTextField(
                value = idInput,
                onValueChange = onIdChange,
                placeholder = { Text("Employee ID Code, e.g. EMP-2026-04", color = SlateTextMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SlateTextPrimary,
                    unfocusedTextColor = SlateTextPrimary,
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite,
                    focusedLabelColor = BluePrimary,
                    unfocusedLabelColor = SlateTextSecondary
                ),
                leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = "ID Key", tint = SlateTextSecondary, modifier = Modifier.size(18.dp)) }
            )

            OutlinedTextField(
                value = nameInput,
                onValueChange = onNameChange,
                placeholder = { Text("Full Legal Name, e.g. Ramesh Kumar", color = SlateTextMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SlateTextPrimary,
                    unfocusedTextColor = SlateTextPrimary,
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite,
                    focusedLabelColor = BluePrimary,
                    unfocusedLabelColor = SlateTextSecondary
                ),
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Name Key", tint = SlateTextSecondary, modifier = Modifier.size(18.dp)) }
            )

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Proceed to Biometric Capture", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "proceed", tint = PureWhite, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun StepCaptureBiometrics(
    angle: String,
    description: String,
    onCapture: (Float, Float, Float, String?) -> Unit
) {
    val context = LocalContext.current

    // Geolocation and Camera permission check state
    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        cameraPermissionGranted = permissions[android.Manifest.permission.CAMERA] ?: cameraPermissionGranted
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.CAMERA
                )
            )
        }
    }

    var isFaceDetected by remember { mutableStateOf(false) }
    var detectedFaceWidth by remember { mutableStateOf(0f) }
    var detectedEyeSpacing by remember { mutableStateOf(0.42f) }
    var detectedMouthHeight by remember { mutableStateOf(0.25f) }

    // Auto-scan logic timer to simulate real face scanning capture automatically
    var countdownSeconds by remember { mutableStateOf(3) }
    var isCaptured by remember { mutableStateOf(false) }
    var triggerCapture by remember { mutableStateOf(false) }

    LaunchedEffect(cameraPermissionGranted, isFaceDetected) {
        if (cameraPermissionGranted && isFaceDetected && !isCaptured) {
            for (i in 3 downTo 1) {
                countdownSeconds = i
                kotlinx.coroutines.delay(1000)
            }
            if (isFaceDetected && !isCaptured) {
                triggerCapture = true
            }
        } else if (!isFaceDetected) {
            countdownSeconds = 3
            triggerCapture = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // High fidelity simulated camera viewfinder inside border Card
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.25f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A)), // Sleek slate-900 background
                    contentAlignment = Alignment.Center
                ) {
                    if (cameraPermissionGranted) {
                        EnrollCameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            triggerCapture = triggerCapture,
                            onPhotoCaptured = { photoPath ->
                                isCaptured = true
                                onCapture(detectedFaceWidth, detectedEyeSpacing, detectedMouthHeight, photoPath.ifEmpty { null })
                            },
                            onFaceDetected = { detected, width, eyes, mouth ->
                                isFaceDetected = detected
                                detectedFaceWidth = width
                                detectedEyeSpacing = eyes
                                detectedMouthHeight = mouth
                            }
                        )
                    }

                    // Scanning Face Guides / Safe zone oval
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (isCaptured) EmeraldLive else BluePrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!cameraPermissionGranted) {
                            // Alignment overlay placeholder mesh representation
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Alignment Mesh",
                                tint = BluePrimary.copy(alpha = 0.25f),
                                modifier = Modifier.size(110.dp)
                            )
                        }

                        // Face orientation lock label overlay
                        Box(
                            modifier = Modifier
                                .background(if (isCaptured) EmeraldLive else BluePrimary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = if (isCaptured) "SUCCESS" else "SCANNING FACE",
                                color = PureWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Radar laser bar scanning line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (isCaptured) EmeraldLive.copy(alpha = 0.6f) else BluePrimary.copy(alpha = 0.6f))
                            .align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Biometric Face Scan Scanner",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BlueNavy
                )

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = SlateTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (!isCaptured) {
                CircularProgressIndicator(color = BluePrimary, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Detecting and analyzing facial signature in $countdownSeconds s...",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
            } else {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Done", tint = EmeraldLive, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Face coordinates analyzed and captured! ✅",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLive
                )
            }
        }
    }
}

@Composable
fun StepReviewAndSubmit(
    id: String,
    name: String,
    frontOk: Boolean,
    leftOk: Boolean,
    rightOk: Boolean,
    photoPath: String?,
    onSubmit: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Ok",
                    tint = EmeraldLive,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Biometric Signatures Ready",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BlueNavy
                )
            }

            if (photoPath != null && File(photoPath).exists()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(photoPath),
                        contentDescription = "Registered Employee Photo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(2.dp, BluePrimary, CircleShape)
                            .background(SlateBackground),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Summary profile card properties
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateBackground, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row {
                    Text("Staff Name: ", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateTextSecondary)
                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = SlateTextPrimary)
                }
                Row {
                    Text("Employee Code: ", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateTextSecondary)
                    Text(id, fontSize = 12.sp, color = BluePrimary, fontWeight = FontWeight.ExtraBold)
                }
                Row {
                    Text("Secure Cryptography: ", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateTextSecondary)
                    Text("AES Key-Store Protected", fontSize = 11.sp, color = EmeraldLiveDark, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "BIOMETRIC ANALYSIS STATUS:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextSecondary,
                letterSpacing = 1.sp
            )

            // Single unified biometric validation indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AngleCheckIndicator("Face Biometric Scan", frontOk, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "save", tint = PureWhite, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Write Enrolled Profile to DB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PureWhite)
            }

            OutlinedButton(
                onClick = onReset,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reset Wizard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AngleCheckIndicator(label: String, isOk: Boolean, modifier: Modifier = Modifier) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOk) Color(0xFFECFDF5) else Color(0xFFF1F5F9)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isOk) Color(0xFFA7F3D0) else SlateBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Pending,
                contentDescription = label,
                tint = if (isOk) EmeraldLive else SlateTextMuted,
                modifier = Modifier.size(20.dp)
            )
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = SlateTextPrimary)
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
private fun EnrollCameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA,
    triggerCapture: Boolean,
    onPhotoCaptured: (String) -> Unit,
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

    val currentTriggerCapture = rememberUpdatedState(triggerCapture)
    val hasCaptured = remember { mutableStateOf(false) }

    LaunchedEffect(triggerCapture) {
        if (!triggerCapture) {
            hasCaptured.value = false
        }
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
                                        
                                        // Extract key facial proportions to form a unique, high-contrast offline signature
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

                                        if (currentTriggerCapture.value && !hasCaptured.value) {
                                            hasCaptured.value = true
                                            try {
                                                val bitmap = imageProxy.toBitmap()
                                                val rotation = imageProxy.imageInfo.rotationDegrees
                                                val rotatedBitmap = if (rotation != 0) {
                                                    val matrix = android.graphics.Matrix().apply { postRotate(rotation.toFloat()) }
                                                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                                                } else {
                                                    bitmap
                                                }
                                                val file = File(context.cacheDir, "emp_reg_${System.currentTimeMillis()}.jpg")
                                                FileOutputStream(file).use { out ->
                                                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                                }
                                                onPhotoCaptured(file.absolutePath)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                onPhotoCaptured("")
                                            }
                                        }
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

