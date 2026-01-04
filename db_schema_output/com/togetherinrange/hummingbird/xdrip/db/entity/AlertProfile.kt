package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "alert_profiles",
    indices = [
        Index(value = ["uuid"], unique = true)
    ]
)
data class AlertProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "is_enabled", defaultValue = "1")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "is_high_alert")
    val isHighAlert: Boolean,

    @ColumnInfo(name = "threshold_mg_dl")
    val thresholdMgDl: Double,

    @ColumnInfo(name = "is_predictive", defaultValue = "0")
    val isPredictive: Boolean = false,

    @ColumnInfo(name = "prediction_minutes")
    val predictionMinutes: Int? = null,

    @ColumnInfo(name = "is_all_day", defaultValue = "1")
    val isAllDay: Boolean = true,

    @ColumnInfo(name = "start_minute", defaultValue = "0")
    val startMinute: Int = 0,

    @ColumnInfo(name = "end_minute", defaultValue = "1439")
    val endMinute: Int = 1439,

    @ColumnInfo(name = "volume_percent", defaultValue = "100")
    val volumePercent: Int = 100,

    @ColumnInfo(name = "vibrate", defaultValue = "1")
    val vibrate: Boolean = true,

    @ColumnInfo(name = "override_silent", defaultValue = "0")
    val overrideSilent: Boolean = false,

    @ColumnInfo(name = "snooze_minutes", defaultValue = "30")
    val snoozeMinutes: Int = 30,

    @ColumnInfo(name = "re_alert_minutes", defaultValue = "5")
    val reAlertMinutes: Int = 5,

    @ColumnInfo(name = "sound_uri")
    val soundUri: String? = null,
) {
    /**
     * Check if this alert is active at the given minute of day
     */
    fun isActiveAtMinute(minuteOfDay: Int): Boolean {
        if (!isEnabled) return false
        if (isAllDay) return true

        return if (startMinute <= endMinute) {
            minuteOfDay in startMinute..endMinute
        } else {
            // Wraps around midnight
            minuteOfDay >= startMinute || minuteOfDay <= endMinute
        }
    }

    /**
     * Check if the given glucose value should trigger this alert
     */
    fun shouldTrigger(mgDl: Double): Boolean {
        return if (isHighAlert) {
            mgDl >= thresholdMgDl
        } else {
            mgDl <= thresholdMgDl
        }
    }

    companion object {
        const val MINUTES_PER_DAY = 1440

        fun highAlert(
            name: String,
            thresholdMgDl: Double,
            snoozeMinutes: Int = 30,
        ) = AlertProfile(
            name = name,
            isHighAlert = true,
            thresholdMgDl = thresholdMgDl,
            snoozeMinutes = snoozeMinutes,
        )

        fun lowAlert(
            name: String,
            thresholdMgDl: Double,
            snoozeMinutes: Int = 15,
        ) = AlertProfile(
            name = name,
            isHighAlert = false,
            thresholdMgDl = thresholdMgDl,
            snoozeMinutes = snoozeMinutes,
        )

        fun urgentLowAlert(
            thresholdMgDl: Double = 55.0,
        ) = AlertProfile(
            name = "Urgent Low",
            isHighAlert = false,
            thresholdMgDl = thresholdMgDl,
            snoozeMinutes = 5,
            reAlertMinutes = 2,
            overrideSilent = true,
            volumePercent = 100,
        )
    }
}
