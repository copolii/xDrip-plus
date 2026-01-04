package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "heart_rate_readings",
    indices = [
        Index(value = ["timestamp"], unique = true)
    ]
)
data class HeartRateReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "bpm")
    val bpm: Int,

    @ColumnInfo(name = "source")
    val source: String? = null,
) {
    companion object {
        const val SOURCE_WATCH = "watch"
        const val SOURCE_BAND = "band"
        const val SOURCE_MANUAL = "manual"

        fun create(
            bpm: Int,
            timestamp: Long = System.currentTimeMillis(),
            source: String? = null,
        ) = HeartRateReading(
            timestamp = timestamp,
            bpm = bpm,
            source = source,
        )
    }
}
