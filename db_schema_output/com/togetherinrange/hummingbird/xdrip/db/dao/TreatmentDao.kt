package com.togetherinrange.hummingbird.xdrip.db.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.togetherinrange.hummingbird.xdrip.db.entity.InsulinDose
import com.togetherinrange.hummingbird.xdrip.db.entity.Treatment
import com.togetherinrange.hummingbird.xdrip.db.entity.TreatmentType
import com.togetherinrange.hummingbird.xdrip.db.entity.TreatmentWithDoses
import kotlinx.coroutines.flow.Flow

@Dao
interface TreatmentDao {

    // ==================== Queries ====================

    @Query("SELECT * FROM treatments WHERE is_valid = 1 ORDER BY timestamp DESC")
    fun getAllValidFlow(): Flow<List<Treatment>>

    @Query("SELECT * FROM treatments ORDER BY timestamp DESC")
    suspend fun getAll(): List<Treatment>

    @Query("SELECT * FROM treatments WHERE id = :id")
    suspend fun getById(id: Long): Treatment?

    @Query("SELECT * FROM treatments WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): Treatment?

    @Query("SELECT * FROM treatments WHERE timestamp >= :since AND is_valid = 1 ORDER BY timestamp DESC")
    suspend fun getSince(since: Long): List<Treatment>

    @Query("SELECT * FROM treatments WHERE timestamp >= :since AND is_valid = 1 ORDER BY timestamp DESC")
    fun getSinceFlow(since: Long): Flow<List<Treatment>>

    @Query("SELECT * FROM treatments WHERE timestamp BETWEEN :start AND :end AND is_valid = 1 ORDER BY timestamp ASC")
    suspend fun getInRange(start: Long, end: Long): List<Treatment>

    @Query("SELECT * FROM treatments WHERE treatment_type = :type AND is_valid = 1 ORDER BY timestamp DESC")
    suspend fun getByType(type: TreatmentType): List<Treatment>

    @Query("SELECT * FROM treatments WHERE treatment_type IN (:types) AND is_valid = 1 ORDER BY timestamp DESC")
    suspend fun getByTypes(types: List<TreatmentType>): List<Treatment>

    @Query("SELECT * FROM treatments WHERE carbs_grams IS NOT NULL AND carbs_grams > 0 AND is_valid = 1 ORDER BY timestamp DESC")
    suspend fun getWithCarbs(): List<Treatment>

    // ==================== With Doses ====================

    @Transaction
    @Query("SELECT * FROM treatments WHERE is_valid = 1 ORDER BY timestamp DESC")
    fun getAllWithDosesFlow(): Flow<List<TreatmentWithDoses>>

    @Transaction
    @Query("SELECT * FROM treatments WHERE id = :id")
    suspend fun getWithDosesById(id: Long): TreatmentWithDoses?

    @Transaction
    @Query("SELECT * FROM treatments WHERE timestamp >= :since AND is_valid = 1 ORDER BY timestamp DESC")
    suspend fun getWithDosesSince(since: Long): List<TreatmentWithDoses>

    @Transaction
    @Query("SELECT * FROM treatments WHERE timestamp >= :since AND is_valid = 1 ORDER BY timestamp DESC")
    fun getWithDosesSinceFlow(since: Long): Flow<List<TreatmentWithDoses>>

    @Transaction
    @Query("SELECT * FROM treatments WHERE treatment_type = 'INSULIN' AND is_valid = 1 ORDER BY timestamp DESC")
    suspend fun getInsulinTreatmentsWithDoses(): List<TreatmentWithDoses>

    // ==================== ContentProvider Support ====================

    @Query("SELECT * FROM treatments WHERE is_valid = 1 ORDER BY timestamp DESC")
    fun getAllCursor(): Cursor

    @Query("SELECT * FROM treatments WHERE id = :id")
    fun getByIdCursor(id: Long): Cursor

    // ==================== Mutations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(treatment: Treatment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(treatments: List<Treatment>): List<Long>

    @Update
    suspend fun update(treatment: Treatment)

    @Delete
    suspend fun delete(treatment: Treatment)

    @Query("DELETE FROM treatments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE treatments SET is_valid = 0 WHERE id = :id")
    suspend fun invalidate(id: Long)

    // ==================== Insulin Doses ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDose(dose: InsulinDose): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoses(doses: List<InsulinDose>): List<Long>

    @Query("SELECT * FROM insulin_doses WHERE treatment_id = :treatmentId")
    suspend fun getDosesForTreatment(treatmentId: Long): List<InsulinDose>

    @Query("DELETE FROM insulin_doses WHERE treatment_id = :treatmentId")
    suspend fun deleteDosesForTreatment(treatmentId: Long)

    // ==================== Transactions ====================

    @Transaction
    suspend fun insertWithDoses(treatment: Treatment, doses: List<InsulinDose>): Long {
        val treatmentId = insert(treatment)
        if (doses.isNotEmpty()) {
            insertDoses(doses.map { it.copy(treatmentId = treatmentId) })
        }
        return treatmentId
    }

    @Transaction
    suspend fun updateWithDoses(treatment: Treatment, doses: List<InsulinDose>) {
        update(treatment)
        deleteDosesForTreatment(treatment.id)
        if (doses.isNotEmpty()) {
            insertDoses(doses.map { it.copy(treatmentId = treatment.id) })
        }
    }

    // ==================== Cleanup ====================

    @Query("DELETE FROM treatments WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long): Int
}
