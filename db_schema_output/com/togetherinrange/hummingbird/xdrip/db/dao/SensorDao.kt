package com.togetherinrange.hummingbird.xdrip.db.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.togetherinrange.hummingbird.xdrip.db.entity.Sensor
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {

    // ==================== Queries ====================

    @Query("SELECT * FROM sensors ORDER BY started_at DESC")
    fun getAllFlow(): Flow<List<Sensor>>

    @Query("SELECT * FROM sensors ORDER BY started_at DESC")
    suspend fun getAll(): List<Sensor>

    @Query("SELECT * FROM sensors WHERE id = :id")
    suspend fun getById(id: Long): Sensor?

    @Query("SELECT * FROM sensors WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): Sensor?

    @Query("SELECT * FROM sensors WHERE stopped_at IS NULL ORDER BY started_at DESC LIMIT 1")
    suspend fun getCurrent(): Sensor?

    @Query("SELECT * FROM sensors WHERE stopped_at IS NULL ORDER BY started_at DESC LIMIT 1")
    fun getCurrentFlow(): Flow<Sensor?>

    @Query("SELECT * FROM sensors WHERE stopped_at IS NOT NULL ORDER BY stopped_at DESC LIMIT 1")
    suspend fun getLastStopped(): Sensor?

    @Query("SELECT * FROM sensors WHERE started_at >= :since ORDER BY started_at DESC")
    suspend fun getSince(since: Long): List<Sensor>

    // ==================== ContentProvider Support ====================

    @Query("SELECT * FROM sensors ORDER BY started_at DESC")
    fun getAllCursor(): Cursor

    @Query("SELECT * FROM sensors WHERE id = :id")
    fun getByIdCursor(id: Long): Cursor

    @Query("SELECT * FROM sensors WHERE stopped_at IS NULL ORDER BY started_at DESC LIMIT 1")
    fun getCurrentCursor(): Cursor

    // ==================== Mutations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sensor: Sensor): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sensors: List<Sensor>): List<Long>

    @Update
    suspend fun update(sensor: Sensor)

    @Delete
    suspend fun delete(sensor: Sensor)

    @Query("DELETE FROM sensors WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE sensors SET stopped_at = :stoppedAt WHERE id = :id")
    suspend fun stop(id: Long, stoppedAt: Long = System.currentTimeMillis())

    @Query("UPDATE sensors SET stopped_at = :stoppedAt WHERE stopped_at IS NULL")
    suspend fun stopAll(stoppedAt: Long = System.currentTimeMillis())

    // ==================== Cleanup ====================

    @Query("DELETE FROM sensors WHERE started_at < :before")
    suspend fun deleteOlderThan(before: Long): Int
}
