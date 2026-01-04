package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sensors",
    indices = [
        Index("started_at"),
        Index(value = ["uuid"], unique = true)
    ]
)
data class Sensor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    @ColumnInfo(name = "stopped_at")
    val stoppedAt: Long? = null,

    @ColumnInfo(name = "transmitter_id")
    val transmitterId: String? = null,

    @ColumnInfo(name = "location")
    val location: String? = null,
) {
    val isActive: Boolean
        get() = stoppedAt == null

    val durationMs: Long
        get() = (stoppedAt ?: System.currentTimeMillis()) - startedAt

    companion object {
        fun create(
            startedAt: Long = System.currentTimeMillis(),
            transmitterId: String? = null,
            location: String? = null,
        ) = Sensor(
            startedAt = startedAt,
            transmitterId = transmitterId,
            location = location,
        )
    }
}
