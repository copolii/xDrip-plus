package com.togetherinrange.hummingbird.xdrip.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.togetherinrange.hummingbird.xdrip.db.entity.ActiveAlert
import com.togetherinrange.hummingbird.xdrip.db.entity.AlertProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    // ==================== Alert Profiles ====================

    @Query("SELECT * FROM alert_profiles ORDER BY threshold_mg_dl")
    fun getAllProfilesFlow(): Flow<List<AlertProfile>>

    @Query("SELECT * FROM alert_profiles ORDER BY threshold_mg_dl")
    suspend fun getAllProfiles(): List<AlertProfile>

    @Query("SELECT * FROM alert_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): AlertProfile?

    @Query("SELECT * FROM alert_profiles WHERE uuid = :uuid")
    suspend fun getProfileByUuid(uuid: String): AlertProfile?

    @Query("SELECT * FROM alert_profiles WHERE is_enabled = 1 ORDER BY threshold_mg_dl")
    suspend fun getEnabledProfiles(): List<AlertProfile>

    @Query("SELECT * FROM alert_profiles WHERE is_enabled = 1 AND is_high_alert = :isHigh ORDER BY threshold_mg_dl")
    suspend fun getEnabledProfilesByType(isHigh: Boolean): List<AlertProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: AlertProfile): Long

    @Update
    suspend fun updateProfile(profile: AlertProfile)

    @Delete
    suspend fun deleteProfile(profile: AlertProfile)

    @Query("DELETE FROM alert_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)

    @Query("UPDATE alert_profiles SET is_enabled = :enabled WHERE id = :id")
    suspend fun setProfileEnabled(id: Long, enabled: Boolean)

    // ==================== Active Alerts ====================

    @Query("SELECT * FROM active_alerts WHERE is_active = 1")
    fun getActiveAlertsFlow(): Flow<List<ActiveAlert>>

    @Query("SELECT * FROM active_alerts WHERE is_active = 1")
    suspend fun getActiveAlerts(): List<ActiveAlert>

    @Query("SELECT * FROM active_alerts WHERE id = :id")
    suspend fun getActiveAlertById(id: Long): ActiveAlert?

    @Query("SELECT * FROM active_alerts WHERE alert_profile_id = :profileId AND is_active = 1 LIMIT 1")
    suspend fun getActiveAlertForProfile(profileId: Long): ActiveAlert?

    @Query("SELECT * FROM active_alerts WHERE is_active = 1 AND (snoozed_until IS NULL OR snoozed_until < :currentTime)")
    suspend fun getUnsnoozedAlerts(currentTime: Long = System.currentTimeMillis()): List<ActiveAlert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveAlert(alert: ActiveAlert): Long

    @Update
    suspend fun updateActiveAlert(alert: ActiveAlert)

    @Delete
    suspend fun deleteActiveAlert(alert: ActiveAlert)

    @Query("UPDATE active_alerts SET is_active = 0 WHERE id = :id")
    suspend fun dismissAlert(id: Long)

    @Query("UPDATE active_alerts SET is_active = 0")
    suspend fun dismissAllAlerts()

    @Query("UPDATE active_alerts SET snoozed_until = :until WHERE id = :id")
    suspend fun snoozeAlert(id: Long, until: Long)

    @Query("UPDATE active_alerts SET snoozed_until = :until WHERE is_active = 1")
    suspend fun snoozeAllAlerts(until: Long)

    @Query("DELETE FROM active_alerts WHERE is_active = 0 AND triggered_at < :before")
    suspend fun cleanupOldAlerts(before: Long): Int
}
