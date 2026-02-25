package com.varuna.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val password: String, // Hashed
    val village: String,
    val role: String = "user", // user, admin, health_officer
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_quality_results")
data class WaterQualityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val villageName: String,
    val ph: Double,
    val tds: Double,
    val turbidity: Double,
    val hardness: Double,
    val temperature: Double,
    val chloride: Double,
    val dissolvedOxygen: Double,
    val wqiScore: Double,
    val classification: String, // Safe, Moderate, Unsafe
    val whoCompliance: String,
    val purificationSuggestions: String,
    val emergencyGuidelines: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "disease_risk_results")
data class DiseaseRiskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val villageName: String,
    val choleraRisk: String, // Low, Medium, High
    val typhoidRisk: String,
    val diarrheaRisk: String,
    val healthCasesReported: Int,
    val ph: Double,
    val tds: Double,
    val turbidity: Double,
    val temperature: Double,
    val preventionGuidelines: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val type: String, // water_quality, disease_risk, emergency
    val message: String,
    val village: String,
    val severity: String, // Low, Medium, High
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "help_requests")
data class HelpRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val userName: String,
    val village: String,
    val location: String,
    val issueDescription: String,
    val status: String = "pending", // pending, resolved
    val timestamp: Long = System.currentTimeMillis()
)
