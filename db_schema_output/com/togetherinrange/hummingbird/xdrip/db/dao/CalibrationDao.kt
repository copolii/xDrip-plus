package com.togetherinrange.hummingbird.xdrip.db.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.togetherinrange.hummingbird.xdrip.db.entity.Calibration
import kotlinx.coroutines.flow.Flow

@Dao
interface CalibrationDao {

    // ==================== Queries ====================

    @Query("SELECT * FROM calibrations ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<Calibration>>

    @Query("SELECT * FROM calibrations ORDER BY timestamp DESC")
    suspend fun getAll(): List<Calibration>

    @Query("SELECT * FROM calibrations WHERE id = :id")
    suspend fun getById(id: Long): Calibration?

    @Query("SELECT * FROM calibrations WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): Calibration?

    @Query("SELECT * FROM calibrations WHERE sensor_id = :sensorId AND is_valid = 1 ORDER BY timestamp DESC")
    suspend fun getValidForSensor(sensorId: Long): List<Calibration>

    @Query("SELECT * FROM calibrations WHERE sensor_id = :sensorId AND is_valid = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestForSensor(sensorId: Long): Calibration?

    @Query("SELECT * FROM calibrations WHERE sensor_id = :sensorId ORDER BY timestamp DESC")
    fun getForSensorFlow(sensorId: Long): Flow<List<Calibration>>

    @Query("SELECT * FROM calibrations WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getSince(since: Long): List<Calibration>

    @Query("SELECT COUNT(*) FROM calibrations WHERE sensor_id = :sensorId AND is_valid = 1")
    suspend fun countValidForSensor(sensorId: Long): Int

    // ==================== ContentProvider Support ====================

    @Query("SELECT * FROM calibrations ORDER BY timestamp DESC")
    fun getAllCursor(): Cursor

    @Query("SELECT * FROM calibrations WHERE id = :id")
    fun getByIdCursor(id: Long): Cursor

    // ==================== Mutations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calibration: Calibration): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(calibrations: List<Calibration>): List<Long>

    @Update
    suspend fun update(calibration: Calibration)

    @Delete
    suspend fun delete(calibration: Calibration)

    @Query("DELETE FROM calibrations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE calibrations SET is_valid = 0 WHERE id = :id")
    suspend fun invalidate(id: Long)

    @Query("UPDATE calibrations SET is_valid = 0 WHERE sensor_id = :sensorId")
    suspend fun invalidateForSensor(sensorId: Long)

    // ==================== Cleanup ====================

    @Query("DELETE FROM calibrations WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long): Int
}
