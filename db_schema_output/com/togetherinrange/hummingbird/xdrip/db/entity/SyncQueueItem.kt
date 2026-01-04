package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SyncAction {
    CREATE,
    UPDATE,
    DELETE;

    companion object {
        fun fromString(value: String): SyncAction =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: CREATE
    }
}

enum class SyncDestination(val bit: Int) {
    NIGHTSCOUT(1),
    TIDEPOOL(2),
    INFLUXDB(4),
    WATCH(8),
    FOLLOWER(16);

    companion object {
        fun fromBits(bits: Int): Set<SyncDestination> =
            entries.filter { (bits and it.bit) != 0 }.toSet()

        fun toBits(destinations: Set<SyncDestination>): Int =
            destinations.fold(0) { acc, dest -> acc or dest.bit }

        val ALL = entries.toSet()
    }
}

@Entity(
    tableName = "sync_queue",
    indices = [
        Index("created_at"),
        Index("entity_type"),
        Index("pending_destinations")
    ]
)
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "entity_type")
    val entityType: String,

    @ColumnInfo(name = "entity_uuid")
    val entityUuid: String,

    @ColumnInfo(name = "action")
    val action: SyncAction,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "pending_destinations")
    val pendingDestinations: Int,

    @ColumnInfo(name = "completed_destinations", defaultValue = "0")
    val completedDestinations: Int = 0,

    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long? = null,

    @ColumnInfo(name = "attempt_count", defaultValue = "0")
    val attemptCount: Int = 0,

    @ColumnInfo(name = "last_error")
    val lastError: String? = null,
) {
    /**
     * Get the set of destinations still pending
     */
    fun getPendingDestinations(): Set<SyncDestination> =
        SyncDestination.fromBits(pendingDestinations and completedDestinations.inv())

    /**
     * Get the set of destinations completed
     */
    fun getCompletedDestinations(): Set<SyncDestination> =
        SyncDestination.fromBits(completedDestinations)

    /**
     * Check if all destinations are complete
     */
    val isComplete: Boolean
        get() = (pendingDestinations and completedDestinations.inv()) == 0

    /**
     * Mark a destination as complete
     */
    fun markComplete(destination: SyncDestination): SyncQueueItem =
        copy(completedDestinations = completedDestinations or destination.bit)

    /**
     * Record a failed attempt
     */
    fun recordFailure(error: String): SyncQueueItem =
        copy(
            lastAttemptAt = System.currentTimeMillis(),
            attemptCount = attemptCount + 1,
            lastError = error,
        )

    companion object {
        const val ENTITY_GLUCOSE_READING = "glucose_reading"
        const val ENTITY_TREATMENT = "treatment"
        const val ENTITY_CALIBRATION = "calibration"
        const val ENTITY_BLOOD_TEST = "blood_test"
        const val ENTITY_SENSOR = "sensor"

        fun create(
            entityType: String,
            entityUuid: String,
            action: SyncAction,
            destinations: Set<SyncDestination>,
        ) = SyncQueueItem(
            entityType = entityType,
            entityUuid = entityUuid,
            action = action,
            pendingDestinations = SyncDestination.toBits(destinations),
        )

        fun forGlucoseReading(uuid: String, action: SyncAction = SyncAction.CREATE) =
            create(ENTITY_GLUCOSE_READING, uuid, action, SyncDestination.ALL)

        fun forTreatment(uuid: String, action: SyncAction = SyncAction.CREATE) =
            create(ENTITY_TREATMENT, uuid, action, SyncDestination.ALL)
    }
}
