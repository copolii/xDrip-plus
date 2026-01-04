package com.togetherinrange.hummingbird.xdrip.db.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.togetherinrange.hummingbird.xdrip.db.entity.EventLog
import com.togetherinrange.hummingbird.xdrip.db.entity.LogLevel
import kotlinx.coroutines.flow.Flow

@Dao
interface EventLogDao {

    // ==================== Queries ====================

    @Query("SELECT * FROM event_log ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestFlow(limit: Int = 500): Flow<List<EventLog>>

    @Query("SELECT * FROM event_log ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatest(limit: Int = 500): List<EventLog>

    @Query("SELECT * FROM event_log WHERE id = :id")
    suspend fun getById(id: Long): EventLog?

    @Query("SELECT * FROM event_log WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getSince(since: Long): List<EventLog>

    @Query("SELECT * FROM event_log WHERE level = :level ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getByLevel(level: LogLevel, limit: Int = 100): List<EventLog>

    @Query("SELECT * FROM event_log WHERE level IN (:levels) ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getByLevels(levels: List<LogLevel>, limit: Int = 500): List<EventLog>

    @Query("SELECT * FROM event_log WHERE tag = :tag ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getByTag(tag: String, limit: Int = 100): List<EventLog>

    @Query("SELECT * FROM event_log WHERE tag LIKE :tagPattern ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getByTagPattern(tagPattern: String, limit: Int = 100): List<EventLog>

    @Query("SELECT * FROM event_log WHERE message LIKE :searchPattern OR details LIKE :searchPattern ORDER BY timestamp DESC LIMIT :limit")
    suspend fun search(searchPattern: String, limit: Int = 100): List<EventLog>

    @Query("SELECT COUNT(*) FROM event_log WHERE level = :level AND timestamp >= :since")
    suspend fun countByLevelSince(level: LogLevel, since: Long): Int

    @Query("SELECT DISTINCT tag FROM event_log ORDER BY tag")
    suspend fun getAllTags(): List<String>

    // ==================== ContentProvider Support ====================

    @Query("SELECT * FROM event_log ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestCursor(limit: Int = 500): Cursor

    // ==================== Mutations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: EventLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<EventLog>): List<Long>

    @Delete
    suspend fun delete(log: EventLog)

    @Query("DELETE FROM event_log WHERE id = :id")
    suspend fun deleteById(id: Long)

    // ==================== Cleanup ====================

    @Query("DELETE FROM event_log WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long): Int

    @Query("DELETE FROM event_log WHERE level = :level AND timestamp < :before")
    suspend fun deleteByLevelOlderThan(level: LogLevel, before: Long): Int

    @Query("DELETE FROM event_log")
    suspend fun deleteAll()

    @Query("""
        DELETE FROM event_log WHERE id NOT IN (
            SELECT id FROM event_log ORDER BY timestamp DESC LIMIT :keepCount
        )
    """)
    suspend fun trimToCount(keepCount: Int): Int
}
