package com.varuna.app.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varuna.app.utils.NotificationHelper

class VarunaMessagingService : FirebaseMessagingService() {

    private val TAG = "VarunaFCM"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        saveFcmToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        NotificationHelper.createNotificationChannels(applicationContext)

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Notification: ${notification.title} | ${notification.body}")
        }

        // Handle data payload
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            val type = data["type"] ?: "general"
            val village = data["village"] ?: "Unknown"
            val classification = data["classification"]
            val wqiScore = data["wqiScore"]?.toDoubleOrNull()
            val choleraRisk = data["choleraRisk"]
            val typhoidRisk = data["typhoidRisk"]
            val diarrheaRisk = data["diarrheaRisk"]

            when (type) {
                "water_quality" -> {
                    if (classification != null && wqiScore != null) {
                        NotificationHelper.sendWaterQualityAlert(
                            applicationContext, village, classification, wqiScore
                        )
                    }
                }
                "disease_risk" -> {
                    val riskMap = buildMap<String, String> {
                        choleraRisk?.let { put("Cholera", it) }
                        typhoidRisk?.let { put("Typhoid", it) }
                        diarrheaRisk?.let { put("Diarrhea", it) }
                    }
                    NotificationHelper.sendDiseaseRiskAlert(applicationContext, village, riskMap)
                }
                "emergency" -> {
                    // Direct emergency notification with full text
                    val title = data["title"] ?: "🚨 Emergency Alert"
                    val body = data["body"] ?: "Urgent action required!"
                    // Show emergency notification
                    showEmergencyNotification(title, body)
                }
            }
        }
    }

    private fun saveFcmToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update FCM token", e)
            }
    }

    private fun showEmergencyNotification(title: String, body: String) {
        // Emergency notification shown directly
        Log.d(TAG, "Emergency: $title | $body")
    }
}
