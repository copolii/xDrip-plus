package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "calibrations",
    foreignKeys = [
        ForeignKey(
            entity = Sensor::class,
            parentColumns = ["id"],
            childColumns = ["sensor_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("timestamp"),
        Index("sensor_id"),
        Index(value = ["uuid"], unique = true)
    ]
)
data class Calibration(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "sensor_id")
    val sensorId: Long,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "bg_mg_dl")
    val bgMgDl: Double,

    @ColumnInfo(name = "raw_value")
    val rawValue: Double,

    @ColumnInfo(name = "slope", defaultValue = "1.0")
    val slope: Double = 1.0,

    @ColumnInfo(name = "intercept", defaultValue = "0.0")
    val intercept: Double = 0.0,

    @ColumnInfo(name = "slope_confidence")
    val slopeConfidence: Double? = null,

    @ColumnInfo(name = "is_valid", defaultValue = "1")
    val isValid: Boolean = true,

    @ColumnInfo(name = "source")
    val source: String? = null,
) {
    /**
     * Calculate glucose from raw value using this calibration
     */
    fun calculateGlucose(rawValue: Double): Double {
        return (rawValue * slope) + intercept
    }

    companion object {
        const val SOURCE_MANUAL = "manual"
        const val SOURCE_BLOOD_TEST = "blood_test"
        const val SOURCE_PLUGIN = "plugin"
        const val SOURCE_NATIVE = "native"
    }
}
