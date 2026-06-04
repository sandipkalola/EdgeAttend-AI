package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.AttendanceDao
import com.example.data.model.Employee
import com.example.data.model.AttendanceLog
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class AttendanceRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao: AttendanceDao = db.attendanceDao()

    val allEmployees: Flow<List<Employee>> = dao.getAllEmployees()
    val allLogs: Flow<List<AttendanceLog>> = dao.getAllAttendanceLogs()

    suspend fun getEmployeeById(id: String): Employee? {
        return dao.getEmployeeById(id)
    }

    suspend fun registerEmployee(employee: Employee) {
        dao.insertEmployee(employee)
    }

    suspend fun deleteEmployee(employee: Employee) {
        dao.deleteEmployee(employee)
    }

    suspend fun clearEmployees() {
        dao.clearEmployees()
    }

    suspend fun recordAttendance(log: AttendanceLog) {
        dao.insertAttendanceLog(log)
    }

    suspend fun clearAllLogs() {
        dao.clearAllLogs()
    }

    fun getTodayLogsCount(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return dao.getTodayLogsCount(calendar.timeInMillis)
    }

    suspend fun getLastLogForEmployeeSince(employeeId: String, sinceTime: Long): AttendanceLog? {
        return dao.getLastLogForEmployeeSince(employeeId, sinceTime)
    }

    suspend fun getPendingEmployees(): List<Employee> {
        return dao.getPendingEmployees()
    }

    suspend fun getPendingAttendanceLogs(): List<AttendanceLog> {
        return dao.getPendingAttendanceLogs()
    }

    suspend fun markEmployeesAsBackedUp(ids: List<String>) {
        dao.markEmployeesAsBackedUp(ids)
    }

    suspend fun markLogsAsBackedUp(ids: List<Int>) {
        dao.markLogsAsBackedUp(ids)
    }
}
