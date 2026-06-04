package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_logs")
data class AttendanceLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val employeeId: String,
    val employeeName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "CHECK_IN" or "CHECK_OUT"
    val confidenceScore: Float, // Confidence percentage (e.g. 0.94)
    val livenessPassed: Boolean, // True if liveness criteria met
    val gpsLocation: String, // e.g. "28.6139, 77.2090" (New Delhi coordinates)
    val deviceId: String, // Unique local device ID
    val synched: Boolean = false, // Tracked locally for eventual sync if asked, but default off
    val photoPath: String? = null, // Captured selfie file path at check-in/out
    @ColumnInfo(name = "is_backup", defaultValue = "'pending'")
    val is_backup: String = "pending"
)
