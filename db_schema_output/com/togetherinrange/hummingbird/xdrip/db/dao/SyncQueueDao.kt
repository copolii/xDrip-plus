package com.togetherinrange.hummingbird.xdrip.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.togetherinrange.hummingbird.xdrip.db.entity.SyncQueueItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    // ==================== Queries ====================

    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC")
    fun getAllFlow(): Flow<List<SyncQueueItem>>

    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC")
    suspend fun getAll(): List<SyncQueueItem>

    @Query("SELECT * FROM sync_queue WHERE id = :id")
    suspend fun getById(id: Long): SyncQueueItem?

    @Query("""
        SELECT * FROM sync_queue
        WHERE (pending_destinations & ~completed_destinations) != 0
        ORDER BY created_at ASC
    """)
    suspend fun getPending(): List<SyncQueueItem>

    @Query("""
        SELECT * FROM sync_queue
        WHERE (pending_destinations & ~completed_destinations) != 0
        ORDER BY created_at ASC
        LIMIT :limit
    """)
    suspend fun getPendingLimited(limit: Int): List<SyncQueueItem>

    @Query("""
        SELECT * FROM sync_queue
        WHERE (pending_destinations & :destinationBit) != 0
        AND (completed_destinations & :destinationBit) = 0
        ORDER BY created_at ASC
    """)
    suspend fun getPendingForDestination(destinationBit: Int): List<SyncQueueItem>

    @Query("SELECT * FROM sync_queue WHERE entity_type = :entityType ORDER BY created_at ASC")
    suspend fun getByEntityType(entityType: String): List<SyncQueueItem>

    @Query("SELECT * FROM sync_queue WHERE entity_uuid = :uuid")
    suspend fun getByEntityUuid(uuid: String): SyncQueueItem?

    @Query("SELECT COUNT(*) FROM sync_queue WHERE (pending_destinations & ~completed_destinations) != 0")
    suspend fun countPending(): Int

    @Query("SELECT COUNT(*) FROM sync_queue WHERE (pending_destinations & ~completed_destinations) != 0")
    fun countPendingFlow(): Flow<Int>

    // ==================== Mutations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SyncQueueItem>): List<Long>

    @Update
    suspend fun update(item: SyncQueueItem)

    @Delete
    suspend fun delete(item: SyncQueueItem)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sync_queue WHERE entity_uuid = :uuid")
    suspend fun deleteByEntityUuid(uuid: String)

    @Query("""
        UPDATE sync_queue
        SET completed_destinations = completed_destinations | :destinationBit
        WHERE id = :id
    """)
    suspend fun markDestinationComplete(id: Long, destinationBit: Int)

    @Query("""
        UPDATE sync_queue
        SET last_attempt_at = :attemptTime,
            attempt_count = attempt_count + 1,
            last_error = :error
        WHERE id = :id
    """)
    suspend fun recordAttempt(id: Long, attemptTime: Long = System.currentTimeMillis(), error: String?)

    // ==================== Cleanup ====================

    @Query("""
        DELETE FROM sync_queue
        WHERE (pending_destinations & ~completed_destinations) = 0
        AND created_at < :before
    """)
    suspend fun deleteCompletedOlderThan(before: Long): Int

    @Query("DELETE FROM sync_queue WHERE created_at < :before")
    suspend fun deleteOlderThan(before: Long): Int

    @Query("DELETE FROM sync_queue WHERE attempt_count >= :maxAttempts AND created_at < :before")
    suspend fun deleteFailedOlderThan(maxAttempts: Int, before: Long): Int
}
