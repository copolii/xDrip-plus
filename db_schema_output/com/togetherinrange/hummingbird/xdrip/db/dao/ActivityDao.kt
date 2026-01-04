package com.togetherinrange.hummingbird.xdrip.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.togetherinrange.hummingbird.xdrip.db.entity.HeartRateReading
import com.togetherinrange.hummingbird.xdrip.db.entity.StepReading
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    // ==================== Heart Rate ====================

    @Query("SELECT * FROM heart_rate_readings ORDER BY timestamp DESC LIMIT :limit")
    fun getHeartRateFlow(limit: Int = 100): Flow<List<HeartRateReading>>

    @Query("SELECT * FROM heart_rate_readings ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestHeartRate(): HeartRateReading?

    @Query("SELECT * FROM heart_rate_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getHeartRateSince(since: Long): List<HeartRateReading>

    @Query("SELECT * FROM heart_rate_readings WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    suspend fun getHeartRateInRange(start: Long, end: Long): List<HeartRateReading>

    @Query("SELECT AVG(bpm) FROM heart_rate_readings WHERE timestamp >= :since")
    suspend fun getAverageHeartRateSince(since: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeartRate(reading: HeartRateReading): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeartRates(readings: List<HeartRateReading>): List<Long>

    @Delete
    suspend fun deleteHeartRate(reading: HeartRateReading)

    @Query("DELETE FROM heart_rate_readings WHERE timestamp < :before")
    suspend fun deleteHeartRateOlderThan(before: Long): Int

    // ==================== Steps ====================

    @Query("SELECT * FROM step_readings ORDER BY timestamp DESC LIMIT :limit")
    fun getStepsFlow(limit: Int = 100): Flow<List<StepReading>>

    @Query("SELECT * FROM step_readings ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSteps(): StepReading?

    @Query("SELECT * FROM step_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getStepsSince(since: Long): List<StepReading>

    @Query("SELECT * FROM step_readings WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    suspend fun getStepsInRange(start: Long, end: Long): List<StepReading>

    @Query("SELECT SUM(steps) FROM step_readings WHERE timestamp >= :since AND is_absolute = 0")
    suspend fun getTotalDeltaStepsSince(since: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(reading: StepReading): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSteps(readings: List<StepReading>): List<Long>

    @Delete
    suspend fun deleteSteps(reading: StepReading)

    @Query("DELETE FROM step_readings WHERE timestamp < :before")
    suspend fun deleteStepsOlderThan(before: Long): Int
}
