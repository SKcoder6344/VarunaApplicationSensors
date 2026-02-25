package com.varuna.app.database

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VarunaRepository(context: Context) {
    private val database = VarunaDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val waterQualityDao = database.waterQualityDao()
    private val diseaseRiskDao = database.diseaseRiskDao()
    private val alertDao = database.alertDao()
    private val helpRequestDao = database.helpRequestDao()

    // ── User Operations ──────────────────────────────────────────────────────
    
    suspend fun registerUser(name: String, email: String, password: String, village: String, role: String = "user"): Long {
        return withContext(Dispatchers.IO) {
            val user = UserEntity(
                name = name,
                email = email,
                password = VarunaDatabase.hashPassword(password),
                village = village,
                role = role
            )
            userDao.insert(user)
        }
    }

    suspend fun loginUser(email: String, password: String): UserEntity? {
        return withContext(Dispatchers.IO) {
            val user = userDao.getUserByEmail(email)
            if (user != null && VarunaDatabase.verifyPassword(password, user.password)) {
                user
            } else {
                null
            }
        }
    }

    suspend fun getUserById(userId: Long): UserEntity? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
    }

    suspend fun updateUser(user: UserEntity) {
        withContext(Dispatchers.IO) {
            userDao.update(user)
        }
    }

    // ── Water Quality Operations ──────────────────────────────────────────────

    suspend fun saveWaterQualityResult(
        userId: Long,
        villageName: String,
        ph: Double,
        tds: Double,
        turbidity: Double,
        hardness: Double,
        temperature: Double,
        chloride: Double,
        dissolvedOxygen: Double,
        wqiScore: Double,
        classification: String,
        whoCompliance: String,
        purificationSuggestions: String,
        emergencyGuidelines: String
    ): Long {
        return withContext(Dispatchers.IO) {
            val result = WaterQualityEntity(
                userId = userId,
                villageName = villageName,
                ph = ph,
                tds = tds,
                turbidity = turbidity,
                hardness = hardness,
                temperature = temperature,
                chloride = chloride,
                dissolvedOxygen = dissolvedOxygen,
                wqiScore = wqiScore,
                classification = classification,
                whoCompliance = whoCompliance,
                purificationSuggestions = purificationSuggestions,
                emergencyGuidelines = emergencyGuidelines
            )
            waterQualityDao.insert(result)
        }
    }

    fun getUserWaterQualityResults(userId: Long): LiveData<List<WaterQualityEntity>> {
        return waterQualityDao.getUserResults(userId)
    }

    fun getAllWaterQualityResults(): LiveData<List<WaterQualityEntity>> {
        return waterQualityDao.getAllResults()
    }

    suspend fun getLatestWaterQualityResult(userId: Long): WaterQualityEntity? {
        return withContext(Dispatchers.IO) {
            waterQualityDao.getLatestResult(userId)
        }
    }

    suspend fun getWaterQualityStats(): Map<String, Int> {
        return withContext(Dispatchers.IO) {
            mapOf(
                "Safe" to waterQualityDao.getCountByClassification("Safe"),
                "Moderate" to waterQualityDao.getCountByClassification("Moderate"),
                "Unsafe" to waterQualityDao.getCountByClassification("Unsafe")
            )
        }
    }

    // ── Disease Risk Operations ────────────────────────────────────────────────

    suspend fun saveDiseaseRiskResult(
        userId: Long,
        villageName: String,
        choleraRisk: String,
        typhoidRisk: String,
        diarrheaRisk: String,
        healthCasesReported: Int,
        ph: Double,
        tds: Double,
        turbidity: Double,
        temperature: Double,
        preventionGuidelines: String
    ): Long {
        return withContext(Dispatchers.IO) {
            val result = DiseaseRiskEntity(
                userId = userId,
                villageName = villageName,
                choleraRisk = choleraRisk,
                typhoidRisk = typhoidRisk,
                diarrheaRisk = diarrheaRisk,
                healthCasesReported = healthCasesReported,
                ph = ph,
                tds = tds,
                turbidity = turbidity,
                temperature = temperature,
                preventionGuidelines = preventionGuidelines
            )
            diseaseRiskDao.insert(result)
        }
    }

    fun getUserDiseaseRisks(userId: Long): LiveData<List<DiseaseRiskEntity>> {
        return diseaseRiskDao.getUserRisks(userId)
    }

    fun getAllDiseaseRisks(): LiveData<List<DiseaseRiskEntity>> {
        return diseaseRiskDao.getAllRisks()
    }

    suspend fun getLatestDiseaseRisk(userId: Long): DiseaseRiskEntity? {
        return withContext(Dispatchers.IO) {
            diseaseRiskDao.getLatestRisk(userId)
        }
    }

    // ── Alert Operations ────────────────────────────────────────────────────────

    suspend fun createAlert(
        userId: Long,
        type: String,
        message: String,
        village: String,
        severity: String
    ): Long {
        return withContext(Dispatchers.IO) {
            val alert = AlertEntity(
                userId = userId,
                type = type,
                message = message,
                village = village,
                severity = severity
            )
            alertDao.insert(alert)
        }
    }

    fun getUserAlerts(userId: Long): LiveData<List<AlertEntity>> {
        return alertDao.getUserAlerts(userId)
    }

    fun getAllAlerts(): LiveData<List<AlertEntity>> {
        return alertDao.getAllAlerts()
    }

    fun getAlertsByType(userId: Long, type: String): LiveData<List<AlertEntity>> {
        return alertDao.getAlertsByType(userId, type)
    }

    suspend fun markAlertAsRead(alertId: Long) {
        withContext(Dispatchers.IO) {
            alertDao.markAsRead(alertId)
        }
    }

    suspend fun getUnreadAlertCount(userId: Long): Int {
        return withContext(Dispatchers.IO) {
            alertDao.getUnreadCount(userId)
        }
    }

    // ── Help Request Operations ──────────────────────────────────────────────────

    suspend fun submitHelpRequest(
        userId: Long,
        userName: String,
        village: String,
        location: String,
        issueDescription: String
    ): Long {
        return withContext(Dispatchers.IO) {
            val request = HelpRequestEntity(
                userId = userId,
                userName = userName,
                village = village,
                location = location,
                issueDescription = issueDescription
            )
            helpRequestDao.insert(request)
        }
    }

    fun getUserHelpRequests(userId: Long): LiveData<List<HelpRequestEntity>> {
        return helpRequestDao.getUserRequests(userId)
    }

    fun getAllHelpRequests(): LiveData<List<HelpRequestEntity>> {
        return helpRequestDao.getAllRequests()
    }

    fun getPendingHelpRequests(): LiveData<List<HelpRequestEntity>> {
        return helpRequestDao.getRequestsByStatus("pending")
    }

    suspend fun updateHelpRequestStatus(requestId: Long, status: String) {
        withContext(Dispatchers.IO) {
            helpRequestDao.updateStatus(requestId, status)
        }
    }
}
