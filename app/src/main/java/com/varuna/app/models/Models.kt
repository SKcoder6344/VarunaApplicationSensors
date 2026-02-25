package com.varuna.app.models

// =================== WATER QUALITY RESULT ===================
data class WaterQualityResult(
    val wqiScore: Double,
    val classification: String,           // Safe / Moderate / Unsafe
    val ph: Double,
    val tds: Double,
    val turbidity: Double,
    val hardness: Double,
    val temperature: Double,
    val chloride: Double,
    val dissolvedOxygen: Double,
    val villageName: String,
    val purificationSuggestions: List<String>,
    val emergencyGuidelines: List<String>,
    val whoComplianceIssues: List<String>,
    val timestamp: Long
)

// =================== DISEASE RISK RESULT ===================
data class DiseaseRiskResult(
    val villageName: String,
    val choleraRisk: String,              // Low / Medium / High
    val typhoidRisk: String,
    val diarrheaRisk: String,
    val healthCasesReported: Int,
    val ph: Double,
    val tds: Double,
    val turbidity: Double,
    val temperature: Double,
    val preventionGuidelines: List<String>,
    val timestamp: Long
)

// =================== ALERT MODEL ===================
data class AlertModel(
    val id: String = "",
    val type: String,                     // Water Quality Alert / Disease Risk Alert
    val message: String,
    val village: String,
    val severity: String,                 // Low / Medium / High
    val timestamp: Long,
    val isRead: Boolean = false
)

// =================== USER MODEL ===================
data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val village: String = "",
    val role: String = "user",            // user / admin / health_officer
    val fcmToken: String = "",
    val createdAt: Long = 0L
)

// =================== REPORT MODEL ===================
data class ReportModel(
    val id: String = "",
    val userId: String = "",
    val villageName: String = "",
    val wqiScore: Double = 0.0,
    val classification: String = "",
    val choleraRisk: String = "",
    val typhoidRisk: String = "",
    val diarrheaRisk: String = "",
    val pdfUrl: String = "",
    val timestamp: Long = 0L
)

// =================== WHO/BIS STANDARD ===================
data class WaterStandard(
    val parameter: String,
    val whoLimit: String,
    val bisLimit: String,
    val unit: String,
    val description: String
) {
    companion object {
        val WHO_BIS_STANDARDS = listOf(
            WaterStandard("pH", "6.5–8.5", "6.5–8.5", "", "Acidity/Alkalinity measure"),
            WaterStandard("TDS", "≤500", "≤500", "mg/L", "Total Dissolved Solids"),
            WaterStandard("Turbidity", "≤4", "≤5", "NTU", "Cloudiness of water"),
            WaterStandard("Hardness", "≤200", "≤300", "mg/L", "Calcium and Magnesium content"),
            WaterStandard("Chloride", "≤250", "≤250", "mg/L", "Chloride ion concentration"),
            WaterStandard("Dissolved Oxygen", "≥5", "≥5", "mg/L", "Oxygen dissolved in water"),
            WaterStandard("Temperature", "10–30", "10–30", "°C", "Water temperature"),
            WaterStandard("Nitrate", "≤50", "≤45", "mg/L", "Nitrate concentration"),
            WaterStandard("Fluoride", "≤1.5", "≤1.0", "mg/L", "Fluoride concentration"),
            WaterStandard("Iron", "≤0.3", "≤0.3", "mg/L", "Iron concentration")
        )
    }
}
