package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "step_readings",
    indices = [
        Index(value = ["timestamp"], unique = true)
    ]
)
data class StepReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "steps")
    val steps: Int,

    @ColumnInfo(name = "is_absolute", defaultValue = "0")
    val isAbsolute: Boolean = false,

    @ColumnInfo(name = "source")
    val source: String? = null,
) {
    companion object {
        const val SOURCE_WATCH = "watch"
        const val SOURCE_BAND = "band"
        const val SOURCE_PHONE = "phone"

        fun delta(
            steps: Int,
            timestamp: Long = System.currentTimeMillis(),
            source: String? = null,
        ) = StepReading(
            timestamp = timestamp,
            steps = steps,
            isAbsolute = false,
            source = source,
        )

        fun absolute(
            totalSteps: Int,
            timestamp: Long = System.currentTimeMillis(),
            source: String? = null,
        ) = StepReading(
            timestamp = timestamp,
            steps = totalSteps,
            isAbsolute = true,
            source = source,
        )
    }
}
