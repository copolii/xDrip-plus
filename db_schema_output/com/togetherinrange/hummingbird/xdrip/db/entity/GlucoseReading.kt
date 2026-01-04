package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "glucose_readings",
    foreignKeys = [
        ForeignKey(
            entity = Sensor::class,
            parentColumns = ["id"],
            childColumns = ["sensor_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Calibration::class,
            parentColumns = ["id"],
            childColumns = ["calibration_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("timestamp"),
        Index("sensor_id"),
        Index("calibration_id"),
        Index(value = ["uuid"], unique = true)
    ]
)
data class GlucoseReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "sensor_id")
    val sensorId: Long,

    @ColumnInfo(name = "calibration_id")
    val calibrationId: Long? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "raw_value")
    val rawValue: Double,

    @ColumnInfo(name = "filtered_value")
    val filteredValue: Double,

    @ColumnInfo(name = "calculated_mg_dl")
    val calculatedMgDl: Double? = null,

    @ColumnInfo(name = "trend_arrow")
    val trendArrow: Int? = null,

    @ColumnInfo(name = "noise_level", defaultValue = "0")
    val noiseLevel: Int = 0,

    @ColumnInfo(name = "source")
    val source: String? = null,

    @ColumnInfo(name = "is_backfill", defaultValue = "0")
    val isBackfill: Boolean = false,

    @ColumnInfo(name = "hide_slope", defaultValue = "0")
    val hideSlope: Boolean = false,
) {
    /**
     * Trend arrow values following Dexcom conventions:
     * 1 = DoubleUp (rising fast)
     * 2 = SingleUp
     * 3 = FortyFiveUp
     * 4 = Flat
     * 5 = FortyFiveDown
     * 6 = SingleDown
     * 7 = DoubleDown (falling fast)
     */
    val trendDescription: String?
        get() = when (trendArrow) {
            1 -> "rising_fast"
            2 -> "rising"
            3 -> "rising_slow"
            4 -> "flat"
            5 -> "falling_slow"
            6 -> "falling"
            7 -> "falling_fast"
            else -> null
        }

    val isValid: Boolean
        get() = calculatedMgDl != null && calculatedMgDl in 39.0..400.0

    companion object {
        const val MIN_VALID_MG_DL = 39.0
        const val MAX_VALID_MG_DL = 400.0
        const val ERROR_VALUE_MG_DL = 38.0
    }
}
