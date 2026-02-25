package com.varuna.app.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users WHERE role = 'admin' OR role = 'health_officer'")
    suspend fun getAdminCount(): Int
}

@Dao
interface WaterQualityDao {
    @Insert
    suspend fun insert(result: WaterQualityEntity): Long

    @Query("SELECT * FROM water_quality_results WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserResults(userId: Long): LiveData<List<WaterQualityEntity>>

    @Query("SELECT * FROM water_quality_results ORDER BY timestamp DESC LIMIT 50")
    fun getAllResults(): LiveData<List<WaterQualityEntity>>

    @Query("SELECT * FROM water_quality_results WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestResult(userId: Long): WaterQualityEntity?

    @Query("SELECT COUNT(*) FROM water_quality_results WHERE classification = :classification")
    suspend fun getCountByClassification(classification: String): Int

    @Query("SELECT * FROM water_quality_results WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getResultsInRange(userId: Long, startTime: Long, endTime: Long): LiveData<List<WaterQualityEntity>>

    @Query("DELETE FROM water_quality_results WHERE userId = :userId")
    suspend fun deleteUserResults(userId: Long)
}

@Dao
interface DiseaseRiskDao {
    @Insert
    suspend fun insert(risk: DiseaseRiskEntity): Long

    @Query("SELECT * FROM disease_risk_results WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserRisks(userId: Long): LiveData<List<DiseaseRiskEntity>>

    @Query("SELECT * FROM disease_risk_results ORDER BY timestamp DESC LIMIT 50")
    fun getAllRisks(): LiveData<List<DiseaseRiskEntity>>

    @Query("SELECT * FROM disease_risk_results WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRisk(userId: Long): DiseaseRiskEntity?

    @Query("DELETE FROM disease_risk_results WHERE userId = :userId")
    suspend fun deleteUserRisks(userId: Long)
}

@Dao
interface AlertDao {
    @Insert
    suspend fun insert(alert: AlertEntity): Long

    @Query("SELECT * FROM alerts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserAlerts(userId: Long): LiveData<List<AlertEntity>>

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC LIMIT 100")
    fun getAllAlerts(): LiveData<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    fun getAlertsByType(userId: Long, type: String): LiveData<List<AlertEntity>>

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAsRead(alertId: Long)

    @Query("SELECT COUNT(*) FROM alerts WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadCount(userId: Long): Int

    @Query("DELETE FROM alerts WHERE userId = :userId")
    suspend fun deleteUserAlerts(userId: Long)
}

@Dao
interface HelpRequestDao {
    @Insert
    suspend fun insert(request: HelpRequestEntity): Long

    @Query("SELECT * FROM help_requests WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserRequests(userId: Long): LiveData<List<HelpRequestEntity>>

    @Query("SELECT * FROM help_requests ORDER BY timestamp DESC")
    fun getAllRequests(): LiveData<List<HelpRequestEntity>>

    @Query("SELECT * FROM help_requests WHERE status = :status ORDER BY timestamp DESC")
    fun getRequestsByStatus(status: String): LiveData<List<HelpRequestEntity>>

    @Query("UPDATE help_requests SET status = :status WHERE id = :requestId")
    suspend fun updateStatus(requestId: Long, status: String)

    @Query("DELETE FROM help_requests WHERE userId = :userId")
    suspend fun deleteUserRequests(userId: Long)
}
