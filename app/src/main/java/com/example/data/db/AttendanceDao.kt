package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Employee
import com.example.data.model.AttendanceLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("DELETE FROM employees")
    suspend fun clearEmployees()

    @Query("SELECT * FROM attendance_logs ORDER BY timestamp DESC")
    fun getAllAttendanceLogs(): Flow<List<AttendanceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceLog(log: AttendanceLog)

    @Query("DELETE FROM attendance_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM attendance_logs WHERE timestamp >= :startOfDay")
    fun getTodayLogsCount(startOfDay: Long): Flow<Int>

    @Query("SELECT * FROM attendance_logs WHERE employeeId = :employeeId AND timestamp >= :sinceTime ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLogForEmployeeSince(employeeId: String, sinceTime: Long): AttendanceLog?

    @Query("SELECT * FROM employees WHERE is_backup = 'pending'")
    suspend fun getPendingEmployees(): List<Employee>

    @Query("SELECT * FROM attendance_logs WHERE is_backup = 'pending'")
    suspend fun getPendingAttendanceLogs(): List<AttendanceLog>

    @Query("UPDATE employees SET is_backup = 'completed' WHERE id IN (:ids)")
    suspend fun markEmployeesAsBackedUp(ids: List<String>)

    @Query("UPDATE attendance_logs SET is_backup = 'completed' WHERE id IN (:ids)")
    suspend fun markLogsAsBackedUp(ids: List<Int>)
}
