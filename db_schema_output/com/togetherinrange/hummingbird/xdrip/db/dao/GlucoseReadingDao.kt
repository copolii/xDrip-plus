package com.togetherinrange.hummingbird.xdrip.db.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.togetherinrange.hummingbird.xdrip.db.entity.GlucoseReading
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseReadingDao {

    // ==================== Queries ====================

    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestFlow(limit: Int = 100): Flow<List<GlucoseReading>>

    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): GlucoseReading?

    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC LIMIT 1")
    fun getLatestFlow(): Flow<GlucoseReading?>

    @Query("SELECT * FROM glucose_readings WHERE id = :id")
    suspend fun getById(id: Long): GlucoseReading?

    @Query("SELECT * FROM glucose_readings WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): GlucoseReading?

    @Query("SELECT * FROM glucose_readings WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getSince(since: Long): List<GlucoseReading>

    @Query("SELECT * FROM glucose_readings WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getSinceFlow(since: Long): Flow<List<GlucoseReading>>

    @Query("SELECT * FROM glucose_readings WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    suspend fun getInRange(start: Long, end: Long): List<GlucoseReading>

    @Query("SELECT * FROM glucose_readings WHERE sensor_id = :sensorId ORDER BY timestamp DESC")
    suspend fun getBySensor(sensorId: Long): List<GlucoseReading>

    @Query("SELECT * FROM glucose_readings WHERE sensor_id = :sensorId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getBySensorLimited(sensorId: Long, limit: Int): List<GlucoseReading>

    @Query("SELECT COUNT(*) FROM glucose_readings WHERE timestamp >= :since")
    suspend fun countSince(since: Long): Int

    @Query("""
        SELECT * FROM glucose_readings
        WHERE calculated_mg_dl IS NOT NULL
        AND calculated_mg_dl >= 39
        AND calculated_mg_dl <= 400
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getValidReadings(limit: Int = 100): List<GlucoseReading>

    // ==================== ContentProvider Support ====================

    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC")
    fun getAllCursor(): Cursor

    @Query("SELECT * FROM glucose_readings WHERE id = :id")
    fun getByIdCursor(id: Long): Cursor

    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC LIMIT 1")
    fun getLatestCursor(): Cursor

    @Query("SELECT * FROM glucose_readings WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    fun getInRangeCursor(start: Long, end: Long): Cursor

    // ==================== Mutations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: GlucoseReading): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<GlucoseReading>): List<Long>

    @Update
    suspend fun update(reading: GlucoseReading)

    @Delete
    suspend fun delete(reading: GlucoseReading)

    @Query("DELETE FROM glucose_readings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE glucose_readings SET calibration_id = :calibrationId WHERE id = :id")
    suspend fun updateCalibration(id: Long, calibrationId: Long?)

    // ==================== Cleanup ====================

    @Query("DELETE FROM glucose_readings WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long): Int

    @Query("DELETE FROM glucose_readings WHERE sensor_id = :sensorId")
    suspend fun deleteBySensor(sensorId: Long): Int
}
