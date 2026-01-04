package com.togetherinrange.hummingbird.xdrip.db.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.togetherinrange.hummingbird.xdrip.db.entity.BloodTest
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodTestDao {

    // ==================== Queries ====================

    @Query("SELECT * FROM blood_tests WHERE is_valid = 1 ORDER BY timestamp DESC")
    fun getAllValidFlow(): Flow<List<BloodTest>>

    @Query("SELECT * FROM blood_tests ORDER BY timestamp DESC")
    suspend fun getAll(): List<BloodTest>

    @Query("SELECT * FROM blood_tests WHERE id = :id")
    suspend fun getById(id: Long): BloodTest?

    @Query("SELECT * FROM blood_tests WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): BloodTest?

    @Query("SELECT * FROM blood_tests WHERE is_valid = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): BloodTest?

    @Query("SELECT * FROM blood_tests WHERE timestamp >= :since AND is_valid = 1 ORDER BY timestamp DESC")
    suspend fun getSince(since: Long): List<BloodTest>

    @Query("SELECT * FROM blood_tests WHERE timestamp BETWEEN :start AND :end AND is_valid = 1 ORDER BY timestamp ASC")
    suspend fun getInRange(start: Long, end: Long): List<BloodTest>

    @Query("SELECT * FROM blood_tests WHERE used_for_calibration = 0 AND is_valid = 1 ORDER BY timestamp DESC")
    suspend fun getUnusedForCalibration(): List<BloodTest>

    @Query("SELECT COUNT(*) FROM blood_tests WHERE is_valid = 1")
    suspend fun countValid(): Int

    // ==================== ContentProvider Support ====================

    @Query("SELECT * FROM blood_tests WHERE is_valid = 1 ORDER BY timestamp DESC")
    fun getAllCursor(): Cursor

    @Query("SELECT * FROM blood_tests WHERE id = :id")
    fun getByIdCursor(id: Long): Cursor

    // ==================== Mutations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bloodTest: BloodTest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bloodTests: List<BloodTest>): List<Long>

    @Update
    suspend fun update(bloodTest: BloodTest)

    @Delete
    suspend fun delete(bloodTest: BloodTest)

    @Query("DELETE FROM blood_tests WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE blood_tests SET is_valid = 0 WHERE id = :id")
    suspend fun invalidate(id: Long)

    @Query("UPDATE blood_tests SET used_for_calibration = 1 WHERE id = :id")
    suspend fun markUsedForCalibration(id: Long)

    // ==================== Cleanup ====================

    @Query("DELETE FROM blood_tests WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long): Int
}
