package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey
    val id: String, // e.g. "EMP-001" or unique code
    val name: String,
    val photoPath: String?, // Location of captured thumbnail locally
    val embeddingsFront: String, // Serialized floating array or descriptor
    val embeddingsLeft: String,  // Serialized left-angle embedding
    val embeddingsRight: String, // Serialized right-angle embedding
    val createdTime: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_backup", defaultValue = "'pending'")
    val is_backup: String = "pending"
) : java.io.Serializable
