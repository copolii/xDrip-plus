package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "active_alerts",
    foreignKeys = [
        ForeignKey(
            entity = AlertProfile::class,
            parentColumns = ["id"],
            childColumns = ["alert_profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("alert_profile_id")
    ]
)
data class ActiveAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "alert_profile_id")
    val alertProfileId: Long,

    @ColumnInfo(name = "triggered_at")
    val triggeredAt: Long,

    @ColumnInfo(name = "snoozed_until")
    val snoozedUntil: Long? = null,

    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,
) {
    /**
     * Whether this alert is currently snoozed
     */
    fun isSnoozed(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        return snoozedUntil != null && currentTimeMs < snoozedUntil
    }

    /**
     * Create a snoozed copy of this alert
     */
    fun snooze(durationMinutes: Int): ActiveAlert {
        val snoozeUntil = System.currentTimeMillis() + (durationMinutes * 60_000L)
        return copy(snoozedUntil = snoozeUntil)
    }

    /**
     * Create a dismissed copy of this alert
     */
    fun dismiss(): ActiveAlert {
        return copy(isActive = false)
    }

    companion object {
        fun create(alertProfileId: Long) = ActiveAlert(
            alertProfileId = alertProfileId,
            triggeredAt = System.currentTimeMillis(),
        )
    }
}
