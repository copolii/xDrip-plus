package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class TreatmentType {
    INSULIN,
    CARBS,
    NOTE,
    EXERCISE,
    SENSOR_START,
    SENSOR_STOP,
    ANNOUNCEMENT,
    TEMP_BASAL,
    PROFILE_SWITCH;

    companion object {
        fun fromString(value: String): TreatmentType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: NOTE
    }
}

@Entity(
    tableName = "treatments",
    indices = [
        Index("timestamp"),
        Index("treatment_type"),
        Index(value = ["uuid"], unique = true)
    ]
)
data class Treatment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "treatment_type")
    val treatmentType: TreatmentType,

    @ColumnInfo(name = "carbs_grams")
    val carbsGrams: Double? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "entered_by")
    val enteredBy: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_valid", defaultValue = "1")
    val isValid: Boolean = true,
) {
    companion object {
        const val ENTERED_BY_USER = "user"
        const val ENTERED_BY_SYNC = "sync"
        const val ENTERED_BY_PUMP = "pump"

        fun carbs(
            grams: Double,
            timestamp: Long = System.currentTimeMillis(),
            notes: String? = null,
        ) = Treatment(
            timestamp = timestamp,
            treatmentType = TreatmentType.CARBS,
            carbsGrams = grams,
            notes = notes,
            enteredBy = ENTERED_BY_USER,
        )

        fun note(
            text: String,
            timestamp: Long = System.currentTimeMillis(),
        ) = Treatment(
            timestamp = timestamp,
            treatmentType = TreatmentType.NOTE,
            notes = text,
            enteredBy = ENTERED_BY_USER,
        )

        fun sensorStart(
            timestamp: Long = System.currentTimeMillis(),
        ) = Treatment(
            timestamp = timestamp,
            treatmentType = TreatmentType.SENSOR_START,
            enteredBy = ENTERED_BY_USER,
        )

        fun sensorStop(
            timestamp: Long = System.currentTimeMillis(),
        ) = Treatment(
            timestamp = timestamp,
            treatmentType = TreatmentType.SENSOR_STOP,
            enteredBy = ENTERED_BY_USER,
        )
    }
}
