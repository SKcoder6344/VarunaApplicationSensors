package com.varuna.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.varuna.app.R
import com.varuna.app.activities.MainActivity

object NotificationHelper {

    private const val CHANNEL_WATER_QUALITY = "varuna_water_quality"
    private const val CHANNEL_DISEASE_RISK = "varuna_disease_risk"
    private const val CHANNEL_EMERGENCY = "varuna_emergency"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Water Quality Channel
            NotificationChannel(
                CHANNEL_WATER_QUALITY,
                "Water Quality Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts about water quality status changes"
                nm.createNotificationChannel(this)
            }

            // Disease Risk Channel
            NotificationChannel(
                CHANNEL_DISEASE_RISK,
                "Disease Risk Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts about disease risk predictions"
                nm.createNotificationChannel(this)
            }

            // Emergency Channel
            NotificationChannel(
                CHANNEL_EMERGENCY,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Critical emergency water safety alerts"
                nm.createNotificationChannel(this)
            }
        }
    }

    fun sendWaterQualityAlert(
        context: Context,
        villageName: String,
        classification: String,
        wqiScore: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, body) = when (classification) {
            "Unsafe" -> Pair(
                "⚠️ UNSAFE WATER DETECTED – $villageName",
                "WQI Score: ${String.format("%.1f", wqiScore)}. DO NOT drink untreated water! Tap for details."
            )
            "Moderate" -> Pair(
                "⚠️ Water Quality Moderate – $villageName",
                "WQI Score: ${String.format("%.1f", wqiScore)}. Treat water before use. Tap for details."
            )
            else -> return
        }

        val channel = if (classification == "Unsafe") CHANNEL_EMERGENCY else CHANNEL_WATER_QUALITY

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    fun sendDiseaseRiskAlert(
        context: Context,
        villageName: String,
        riskMap: Map<String, String>
    ) {
        val highRisk = riskMap.filter { it.value == "High" }
        if (highRisk.isEmpty()) return

        val diseaseList = highRisk.keys.joinToString(", ")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_EMERGENCY)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("🚨 HIGH DISEASE RISK – $villageName")
            .setContentText("High risk: $diseaseList. Seek medical attention and avoid contaminated water!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("High disease risk detected for: $diseaseList in $villageName.\n\nAction: Boil water, wash hands, use ORS if needed, consult health officer."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            (System.currentTimeMillis() + 1).toInt(),
            notification
        )
    }
}
