package com.varuna.app.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varuna.app.models.DiseaseRiskResult
import com.varuna.app.models.WaterQualityResult

object AlertManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun triggerWaterQualityAlert(context: Context, result: WaterQualityResult) {
        val uid = auth.currentUser?.uid ?: return

        val severity = when (result.classification) {
            "Unsafe" -> "High"
            "Moderate" -> "Medium"
            else -> "Low"
        }

        val alertData = hashMapOf(
            "userId" to uid,
            "type" to "Water Quality Alert",
            "message" to "WQI Score: ${String.format("%.1f", result.wqiScore)} | ${result.classification} water detected in ${result.villageName}",
            "village" to result.villageName,
            "severity" to severity,
            "wqiScore" to result.wqiScore,
            "classification" to result.classification,
            "timestamp" to result.timestamp,
            "isRead" to false
        )

        db.collection("alerts").add(alertData)

        // If High severity, also notify health officer/admin
        if (severity == "High") {
            notifyAdmin(
                alertType = "WATER QUALITY EMERGENCY",
                message = "Unsafe water detected in ${result.villageName}. WQI: ${String.format("%.1f", result.wqiScore)}",
                village = result.villageName,
                uid = uid
            )
        }
    }

    fun triggerDiseaseRiskAlert(context: Context, result: DiseaseRiskResult) {
        val uid = auth.currentUser?.uid ?: return

        val highRiskDiseases = buildList {
            if (result.choleraRisk == "High") add("Cholera")
            if (result.typhoidRisk == "High") add("Typhoid")
            if (result.diarrheaRisk == "High") add("Diarrhea")
        }

        val severity = when {
            highRiskDiseases.isNotEmpty() -> "High"
            listOf(result.choleraRisk, result.typhoidRisk, result.diarrheaRisk).any { it == "Medium" } -> "Medium"
            else -> "Low"
        }

        val alertData = hashMapOf(
            "userId" to uid,
            "type" to "Disease Risk Alert",
            "message" to buildString {
                append("High disease risk in ${result.villageName}: ")
                if (result.choleraRisk != "Low") append("Cholera(${result.choleraRisk}) ")
                if (result.typhoidRisk != "Low") append("Typhoid(${result.typhoidRisk}) ")
                if (result.diarrheaRisk != "Low") append("Diarrhea(${result.diarrheaRisk})")
            },
            "village" to result.villageName,
            "severity" to severity,
            "choleraRisk" to result.choleraRisk,
            "typhoidRisk" to result.typhoidRisk,
            "diarrheaRisk" to result.diarrheaRisk,
            "timestamp" to result.timestamp,
            "isRead" to false
        )

        db.collection("alerts").add(alertData)

        if (severity == "High") {
            notifyAdmin(
                alertType = "DISEASE RISK EMERGENCY",
                message = "High disease risk in ${result.villageName}: ${highRiskDiseases.joinToString(", ")}",
                village = result.villageName,
                uid = uid
            )
        }
    }

    private fun notifyAdmin(alertType: String, message: String, village: String, uid: String) {
        val adminAlert = hashMapOf(
            "triggeredByUser" to uid,
            "type" to alertType,
            "message" to message,
            "village" to village,
            "timestamp" to System.currentTimeMillis(),
            "severity" to "High",
            "isRead" to false,
            "isAdminAlert" to true
        )
        // Store in admin_alerts collection
        db.collection("admin_alerts").add(adminAlert)
    }
}
