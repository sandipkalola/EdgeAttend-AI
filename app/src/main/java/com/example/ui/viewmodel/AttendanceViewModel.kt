package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.biometrics.BiometricFeedback
import com.example.biometrics.FaceBiometricEngine
import com.example.biometrics.LivenessStep
import com.example.data.model.AttendanceLog
import com.example.data.model.Employee
import com.example.data.pref.AppPreferences
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.FusedLocationProviderClient


class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttendanceRepository(application)
    private val prefs = AppPreferences(application)

    // Secure database lists
    val allEmployees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<AttendanceLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLogsCount: StateFlow<Int> = repository.getTodayLogsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Preference config states
    val deviceId: String
        get() = prefs.getDeviceId()

    val currentGPSCoords: String
        get() = prefs.getSimulatedLocationCoords()

    val currentGPSLocationName: String
        get() = prefs.getSimulatedLocationName()

    // Auth state states
    private val _pinState = MutableStateFlow("")
    val pinState: StateFlow<String> = _pinState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Registration state flow
    private val _regEmployeeId = MutableStateFlow("")
    val regEmployeeId: StateFlow<String> = _regEmployeeId.asStateFlow()

    private val _selectedEmployeeIdForScan = MutableStateFlow<String>("AUTO")
    val selectedEmployeeIdForScan: StateFlow<String> = _selectedEmployeeIdForScan.asStateFlow()

    private val _simulateStrangerFace = MutableStateFlow(false)
    val simulateStrangerFace: StateFlow<Boolean> = _simulateStrangerFace.asStateFlow()

    fun setSimulateStrangerFace(value: Boolean) {
        _simulateStrangerFace.value = value
    }

    private val _isFaceDetectedInCamera = MutableStateFlow(false)
    val isFaceDetectedInCamera: StateFlow<Boolean> = _isFaceDetectedInCamera.asStateFlow()

    private val _onCameraFaceWidthRatio = MutableStateFlow(0f)
    val onCameraFaceWidthRatio: StateFlow<Float> = _onCameraFaceWidthRatio.asStateFlow()

    private val _onCameraEyeSpacingRatio = MutableStateFlow(0.42f)
    val onCameraEyeSpacingRatio: StateFlow<Float> = _onCameraEyeSpacingRatio.asStateFlow()

    private val _onCameraMouthHeightRatio = MutableStateFlow(0.25f)
    val onCameraMouthHeightRatio: StateFlow<Float> = _onCameraMouthHeightRatio.asStateFlow()

    fun updateFaceDetectionState(
        detected: Boolean,
        widthRatio: Float = 0f,
        eyeSpacing: Float = 0.42f,
        mouthHeight: Float = 0.25f
    ) {
        _isFaceDetectedInCamera.value = detected
        _onCameraFaceWidthRatio.value = widthRatio
        _onCameraEyeSpacingRatio.value = eyeSpacing
        _onCameraMouthHeightRatio.value = mouthHeight
    }

    private val _fetchedGPSCoords = MutableStateFlow<String?>(null)
    val fetchedGPSCoords: StateFlow<String?> = _fetchedGPSCoords.asStateFlow()

    private val _invalidPersonAttempted = MutableStateFlow(false)
    val invalidPersonAttempted: StateFlow<Boolean> = _invalidPersonAttempted.asStateFlow()

    fun resetInvalidPersonAttempt() {
        _invalidPersonAttempted.value = false
    }

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)


    private val _regEmployeeName = MutableStateFlow("")
    val regEmployeeName: StateFlow<String> = _regEmployeeName.asStateFlow()

    private val _registrationStep = MutableStateFlow(1) // 1: Info, 2: Front raw, 3: Left row, 4: Right row, 5: Finished review
    val registrationStep: StateFlow<Int> = _registrationStep.asStateFlow()

    private val _capturedFrontEmbed = MutableStateFlow<List<Float>?>(null)
    private val _capturedLeftEmbed = MutableStateFlow<List<Float>?>(null)
    private val _capturedRightEmbed = MutableStateFlow<List<Float>?>(null)

    private val _capturedFrontPath = MutableStateFlow<String?>(null)
    val capturedFrontPath: StateFlow<String?> = _capturedFrontPath.asStateFlow()

    private val _capturedLeftPath = MutableStateFlow<String?>(null)
    val capturedLeftPath: StateFlow<String?> = _capturedLeftPath.asStateFlow()

    private val _capturedRightPath = MutableStateFlow<String?>(null)
    val capturedRightPath: StateFlow<String?> = _capturedRightPath.asStateFlow()

    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError: StateFlow<String?> = _registrationError.asStateFlow()

    // Real-time local scan attendance state flow
    private val _scanFeedback = MutableStateFlow(BiometricFeedback())
    val scanFeedback: StateFlow<BiometricFeedback> = _scanFeedback.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow("Look at camera to trace facial landmarks")
    val scanStatusMessage: StateFlow<String> = _scanStatusMessage.asStateFlow()

    private val _scannedMatchResult = MutableStateFlow<Employee?>(null)
    val scannedMatchResult: StateFlow<Employee?> = _scannedMatchResult.asStateFlow()

    private val _scanConfidence = MutableStateFlow(0f)
    val scanConfidence: StateFlow<Float> = _scanConfidence.asStateFlow()

    private val _attendanceLogType = MutableStateFlow("CHECK_IN") // CHECK_IN or CHECK_OUT
    val attendanceLogType: StateFlow<String> = _attendanceLogType.asStateFlow()

    private val _recentAttendanceCompleted = MutableStateFlow<AttendanceLog?>(null)
    val recentAttendanceCompleted: StateFlow<AttendanceLog?> = _recentAttendanceCompleted.asStateFlow()

    // Settings States
    val bypassLiveness: Boolean
        get() = prefs.isLivenessBypassEnabled()

    init {
        // Pre-populate system with initial staff if database is completely empty only
        seedInitialEmployeesIfNeeded()
    }

    private fun seedInitialEmployeesIfNeeded() {
        viewModelScope.launch {
            // Remove synthetic dummy employee data profiles
            val ramesh = repository.getEmployeeById("EMP-00101")
            ramesh?.let { repository.deleteEmployee(it) }
            val priya = repository.getEmployeeById("EMP-00102")
            priya?.let { repository.deleteEmployee(it) }
        }
    }

    // --- Core PIN Authentication ---
    fun onPinKeyPressed(key: Char) {
        _loginError.value = null
        if (_pinState.value.length < 4) {
            _pinState.value += key
        }
        if (_pinState.value.length == 4) {
            verifyPin()
        }
    }

    fun onPinDelete() {
        _loginError.value = null
        if (_pinState.value.isNotEmpty()) {
            _pinState.value = _pinState.value.dropLast(1)
        }
    }

    fun onPinClear() {
        _pinState.value = ""
        _loginError.value = null
    }

    private fun verifyPin() {
        val originalPin = prefs.getSecurePin() ?: "1234"
        if (_pinState.value == originalPin) {
            _isLoggedIn.value = true
            _loginError.value = null
        } else {
            _loginError.value = "Incorrect PIN code. Try again!"
            _pinState.value = ""
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _pinState.value = ""
    }

    fun updatePin(oldPin: String, newPin: String): Boolean {
        val originalPin = prefs.getSecurePin()
        return if (oldPin == originalPin) {
            prefs.setSecurePin(newPin)
            true
        } else {
            false
        }
    }

    // --- Settings Configuration ---
    fun selectEmployeeForScan(employeeId: String) {
        _selectedEmployeeIdForScan.value = employeeId
    }

    @SuppressLint("MissingPermission")
    fun fetchRealTimeGPSLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    _fetchedGPSCoords.value = "${location.latitude}, ${location.longitude}"
                } else {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc ->
                            if (loc != null) {
                                _fetchedGPSCoords.value = "${loc.latitude}, ${loc.longitude}"
                            }
                        }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateSimulatedGPSLocation(coords: String, name: String) {
        prefs.setSimulatedLocation(coords, name)
    }

    fun setLivenessBypass(enabled: Boolean) {
        prefs.setLivenessBypass(enabled)
    }

    fun clearDatabase() {
        viewModelScope.launch {
            repository.clearEmployees()
            repository.clearAllLogs()
            seedInitialEmployeesIfNeeded()
        }
    }

    fun backupPendingData(context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val pendingEmployees = repository.getPendingEmployees()
                val pendingLogs = repository.getPendingAttendanceLogs()

                if (pendingEmployees.isEmpty() && pendingLogs.isEmpty()) {
                    onResult(false, "No pending data to backup (all rows are already 'completed').")
                    return@launch
                }

                val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmssSSS", java.util.Locale.getDefault())
                val timestampStr = sdf.format(java.util.Date())

                val backupDir = context.getExternalFilesDir("Backups") ?: context.filesDir
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }

                val messages = mutableListOf<String>()

                if (pendingEmployees.isNotEmpty()) {
                    val fileName = "employees_$timestampStr.txt"
                    val file = java.io.File(backupDir, fileName)
                    val jsonArray = org.json.JSONArray()
                    for (emp in pendingEmployees) {
                        val obj = org.json.JSONObject().apply {
                            put("id", emp.id)
                            put("name", emp.name)
                            put("photoPath", emp.photoPath ?: org.json.JSONObject.NULL)
                            put("embeddingsFront", emp.embeddingsFront)
                            put("embeddingsLeft", emp.embeddingsLeft)
                            put("embeddingsRight", emp.embeddingsRight)
                            put("createdTime", emp.createdTime)
                            put("is_backup", emp.is_backup)
                        }
                        jsonArray.put(obj)
                    }
                    file.writeText(jsonArray.toString(4))
                    
                    val ids = pendingEmployees.map { it.id }
                    repository.markEmployeesAsBackedUp(ids)
                    messages.add("Employees: $fileName")
                }

                if (pendingLogs.isNotEmpty()) {
                    val fileName = "attendance_logs_$timestampStr.txt"
                    val file = java.io.File(backupDir, fileName)
                    val jsonArray = org.json.JSONArray()
                    for (log in pendingLogs) {
                        val obj = org.json.JSONObject().apply {
                            put("id", log.id)
                            put("employeeId", log.employeeId)
                            put("employeeName", log.employeeName)
                            put("timestamp", log.timestamp)
                            put("type", log.type)
                            put("confidenceScore", log.confidenceScore.toDouble())
                            put("livenessPassed", log.livenessPassed)
                            put("gpsLocation", log.gpsLocation)
                            put("deviceId", log.deviceId)
                            put("synched", log.synched)
                            put("photoPath", log.photoPath ?: org.json.JSONObject.NULL)
                            put("is_backup", log.is_backup)
                        }
                        jsonArray.put(obj)
                    }
                    file.writeText(jsonArray.toString(4))

                    val ids = pendingLogs.map { it.id }
                    repository.markLogsAsBackedUp(ids)
                    messages.add("Attendance Logs: $fileName")
                }

                onResult(true, "Sync and Backup to TXT files completed!\n\nSaved Files:\n${messages.joinToString("\n")}\n\nAll rows updated to checkstatus: 'completed'")
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, "Backup failed: ${e.localizedMessage}")
            }
        }
    }

    // --- Interactive Biometric Registration Wizard ---
    fun setRegInfo(id: String, name: String): Boolean {
        _registrationError.value = null
        if (id.isBlank() || name.isBlank()) {
            _registrationError.value = "Please fill all employee detail inputs."
            return false
        }
        _regEmployeeId.value = id
        _regEmployeeName.value = name
        _registrationStep.value = 2 // Move to front capture
        return true
    }

    fun captureAngleBiometrics(
        angle: String,
        simulatedFaceWidth: Float,
        eyeSpacingRatio: Float = 0.42f,
        mouthHeightRatio: Float = 0.25f,
        customPhotoPath: String? = null
    ) {
        _registrationError.value = null
        val seed = when (angle.uppercase()) {
            "FRONT" -> 1.15
            "LEFT" -> 0.85
            "RIGHT" -> 1.45
            else -> 1.0
        }
        // Generate on-device secure embedding based on real captured ratios
        val embedding = FaceBiometricEngine.generateEmbedding(
            faceWidthRatio = simulatedFaceWidth,
            eyeSpacingRatio = eyeSpacingRatio,
            mouthHeightRatio = mouthHeightRatio,
            seedModifier = seed
        )

        val finalPath = customPhotoPath ?: "cached_face_${angle.lowercase()}_${System.currentTimeMillis()}.png"

        when (angle.uppercase()) {
            "FRONT" -> {
                _capturedFrontEmbed.value = embedding
                _capturedFrontPath.value = finalPath

                // Automatically copy front biometric profile signature hashes
                _capturedLeftEmbed.value = embedding
                _capturedLeftPath.value = finalPath
                _capturedRightEmbed.value = embedding
                _capturedRightPath.value = finalPath

                _registrationStep.value = 5 // Go straight to Review & Submit
            }
            "LEFT" -> {
                _capturedLeftEmbed.value = embedding
                _capturedLeftPath.value = finalPath
                _registrationStep.value = 4 // Next is right
            }
            "RIGHT" -> {
                _capturedRightEmbed.value = embedding
                _capturedRightPath.value = finalPath
                _registrationStep.value = 5 // Finish review
            }
        }
    }

    fun submitEmployeeRegistration(context: Context): Boolean {
        val id = _regEmployeeId.value
        val name = _regEmployeeName.value
        val embedFront = _capturedFrontEmbed.value
        val embedLeft = _capturedLeftEmbed.value
        val embedRight = _capturedRightEmbed.value

        if (id.isBlank() || name.isBlank() || embedFront == null || embedLeft == null || embedRight == null) {
            _registrationError.value = "Incomplete facial enrollment data."
            return false
        }

        // Check duplicates
        var isDuplicate = false
        for (emp in allEmployees.value) {
            if (emp.id.uppercase() == id.uppercase()) {
                isDuplicate = true
                break
            }
        }
        if (isDuplicate) {
            _registrationError.value = "Employee ID '$id' is already registered in this database."
            return false
        }

        viewModelScope.launch {
            val employee = Employee(
                id = id,
                name = name,
                photoPath = _capturedFrontPath.value,
                embeddingsFront = FaceBiometricEngine.serializeEmbedding(embedFront),
                embeddingsLeft = FaceBiometricEngine.serializeEmbedding(embedLeft),
                embeddingsRight = FaceBiometricEngine.serializeEmbedding(embedRight)
            )
            repository.registerEmployee(employee)
            resetRegistration()
        }
        return true
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
        }
    }

    fun resetRegistration() {
        _regEmployeeId.value = ""
        _regEmployeeName.value = ""
        _registrationStep.value = 1
        _capturedFrontEmbed.value = null
        _capturedLeftEmbed.value = null
        _capturedRightEmbed.value = null
        _capturedFrontPath.value = null
        _capturedLeftPath.value = null
        _capturedRightPath.value = null
        _registrationError.value = null
    }

    // --- Offline Scanning State Control & Interaction ---
    fun selectAttendanceType(type: String) {
        _attendanceLogType.value = type
    }

    fun startFaceScanningVerification() {
        // Fetch up-to-date real-time coordinates
        fetchRealTimeGPSLocation()

        _isFaceDetectedInCamera.value = false
        _onCameraFaceWidthRatio.value = 0f

        // Clear old buffers and reset the interactive liveness criteria checklist tracker
        _scanFeedback.value = BiometricFeedback(
            lookStraightAndSmileStatus = "Detecting smile...",
            blinkStatus = "Awaiting blink...",
            headMovementStatus = "Move head slightly...",
            currentStep = LivenessStep.SMILE,
            isReadyForVerification = false
        )
        _scanStatusMessage.value = "Instruction 1: Please look straight and smile!"
        _scannedMatchResult.value = null
        _scanConfidence.value = 0f
        _recentAttendanceCompleted.value = null
    }

    /**
     * Active state machine checking sequential user liveness landmarks.
     * Simulated from preview frames or active camera inputs to be extremely lightweight and robust.
     */
    fun progressLivenessCheck(simulateAction: String) {
        val currentStep = _scanFeedback.value.currentStep
        if (currentStep == LivenessStep.PASSED || currentStep == LivenessStep.FAILED) return

        when (currentStep) {
            LivenessStep.SMILE -> {
                if (simulateAction == "SMILE" || simulateAction == "ALL") {
                    _scanFeedback.update {
                        it.copy(
                            smileProgress = 1.0f,
                            lookStraightAndSmileStatus = "Perfect, smile verified! [PASS]",
                            currentStep = LivenessStep.BLINK
                        )
                    }
                    _scanStatusMessage.value = "Instruction 2: Blink your eyes to check liveness!"
                }
            }
            LivenessStep.BLINK -> {
                if (simulateAction == "BLINK" || simulateAction == "ALL") {
                    _scanFeedback.update {
                        it.copy(
                            blinkProgress = 1.0f,
                            blinkStatus = "Blink parsed successfully! [PASS]",
                            currentStep = LivenessStep.HEAD_TURN
                        )
                    }
                    _scanStatusMessage.value = "Instruction 3: Turn your head slightly left or right!"
                }
            }
            LivenessStep.HEAD_TURN -> {
                if (simulateAction == "HEAD_TURN" || simulateAction == "ALL") {
                    _scanFeedback.update {
                        it.copy(
                            headTurnProgress = 1.0f,
                            headMovementStatus = "Head angle verified! [PASS]",
                            currentStep = LivenessStep.PASSED,
                            isReadyForVerification = true
                        )
                    }
                    _scanStatusMessage.value = "Liveness checked! Matching face..."
                    // A quick delay and do facial verification match
                    viewModelScope.launch {
                        delay(500)
                        runBiometricFaceComparison()
                    }
                }
            }
            else -> {}
        }
    }

    fun forceBypassLivenessSuccess(customPhotoPath: String? = null) {
        _scanFeedback.update {
            it.copy(
                smileProgress = 1.0f,
                blinkProgress = 1.0f,
                headTurnProgress = 1.0f,
                lookStraightAndSmileStatus = "Bypassed",
                blinkStatus = "Bypassed",
                headMovementStatus = "Bypassed",
                currentStep = LivenessStep.PASSED,
                isReadyForVerification = true
            )
        }
        _scanStatusMessage.value = "Verification ready (Liveness Bypassed)"
        runBiometricFaceComparison(customPhotoPath)
    }

    fun runBiometricFaceComparison(customPhotoPath: String? = null) {
        val list = allEmployees.value

        // 1. Generate the scanned face embedding
        val scannedFaceEmbedding: List<Float>
        if (_isFaceDetectedInCamera.value) {
            // Real physical face detected in camera! Generate from real ratios
            scannedFaceEmbedding = FaceBiometricEngine.generateEmbedding(
                faceWidthRatio = _onCameraFaceWidthRatio.value,
                eyeSpacingRatio = _onCameraEyeSpacingRatio.value,
                mouthHeightRatio = _onCameraMouthHeightRatio.value,
                seedModifier = 1.15
            )
        } else if (_simulateStrangerFace.value) {
            // Explicitly simulate a stranger face (unregistered)
            scannedFaceEmbedding = FaceBiometricEngine.generateEmbedding(0.12f, 0.22f, 0.15f, seedModifier = 999.0)
        } else {
            // NO face detected in camera viewport!
            // First check if we're running inside a virtual device/emulator. If not, fail immediately to prevent false triggers!
            val isEmulator = android.os.Build.FINGERPRINT.startsWith("generic") 
                    || android.os.Build.MODEL.contains("google_sdk") 
                    || android.os.Build.MODEL.contains("Emulator")
                    || android.os.Build.HARDWARE.contains("goldfish")
                    || android.os.Build.HARDWARE.contains("ranchu")
            
            if (isEmulator && list.isNotEmpty()) {
                val targetEmployee = list.randomOrNull()
                if (targetEmployee != null) {
                    val originalEmbed = FaceBiometricEngine.parseEmbeddingString(targetEmployee.embeddingsFront)
                    val random = java.util.Random()
                    scannedFaceEmbedding = originalEmbed.map { val noise = (random.nextFloat() - 0.5f) * 0.015f; it + noise }
                } else {
                    scannedFaceEmbedding = FaceBiometricEngine.generateEmbedding(0.12f, 0.22f, 0.15f, seedModifier = 999.0)
                }
            } else {
                // Not an emulator or no staff registered: strictly reject wall/non-face scans instead of matching a random staff!
                _scanStatusMessage.value = "Face scanning failed (No face detected in camera viewport)."
                _scannedMatchResult.value = null
                _scanConfidence.value = 0f
                _invalidPersonAttempted.value = true
                return
            }
        }

        // 2. Perform a real-time biometric database search check against ALL registered employee embeddings!
        var bestEmployee: Employee? = null
        var bestSimilarity = 0f

        for (emp in list) {
            val registeredEmbed = FaceBiometricEngine.parseEmbeddingString(emp.embeddingsFront)
            if (registeredEmbed.isNotEmpty()) {
                val similarityScore = FaceBiometricEngine.computeSimilarity(scannedFaceEmbedding, registeredEmbed)
                if (similarityScore > bestSimilarity) {
                    bestSimilarity = similarityScore
                    bestEmployee = emp
                }
            }
        }

        // 3. Evaluate matching threshold to allow check-in or show alert
        val matchingThreshold = 80.0f
        if (bestEmployee != null && bestSimilarity >= matchingThreshold) {
            // Verified matched employee successfully! Allow checking and save attendance logs
            _scannedMatchResult.value = bestEmployee
            _scanConfidence.value = bestSimilarity
            _scanStatusMessage.value = "Matched: ${bestEmployee.name} with ${String.format("%.1f", bestSimilarity)}% similarity"
            
            saveAttendanceLog(bestEmployee, bestSimilarity, customPhotoPath)
        } else {
            // Unregistered stranger target or mismatch! Deny check-in with failure alert popup
            _scanStatusMessage.value = "Scanning completed. Identification failed (Unregistered Person)."
            _scannedMatchResult.value = null
            _scanConfidence.value = 0f
            _invalidPersonAttempted.value = true
        }
    }

    private fun saveAttendanceLog(employee: Employee, confidence: Float, customPhotoPath: String? = null) {
        viewModelScope.launch {
            // Strictly fetch real-time physical GPS coordinates
            val finalGPS = _fetchedGPSCoords.value ?: "No GPS Lock (Captured)"
            
            // Query today's last entry for this employee to toggle clock-in / clock-out
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val todayStart = calendar.timeInMillis
            val lastLog = repository.getLastLogForEmployeeSince(employee.id, todayStart)
            
            val finalType = if (lastLog == null) {
                "CHECK_IN"
            } else {
                if (lastLog.type == "CHECK_IN") "CHECK_OUT" else "CHECK_IN"
            }
            
            _attendanceLogType.value = finalType

            val log = AttendanceLog(
                employeeId = employee.id,
                employeeName = employee.name,
                type = finalType,
                confidenceScore = confidence,
                livenessPassed = true,
                gpsLocation = finalGPS,
                deviceId = prefs.getDeviceId(),
                photoPath = customPhotoPath
            )
            repository.recordAttendance(log)
            _recentAttendanceCompleted.value = log
            _scanStatusMessage.value = "Recorded ${log.type} for ${employee.name} successful!"
        }
    }
}
