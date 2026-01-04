package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "blood_tests",
    indices = [
        Index("timestamp"),
        Index(value = ["uuid"], unique = true)
    ]
)
data class BloodTest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "mg_dl")
    val mgDl: Double,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "source")
    val source: String? = null,

    @ColumnInfo(name = "is_valid", defaultValue = "1")
    val isValid: Boolean = true,

    @ColumnInfo(name = "used_for_calibration", defaultValue = "0")
    val usedForCalibration: Boolean = false,
) {
    companion object {
        const val SOURCE_MANUAL = "manual"
        const val SOURCE_CONTOUR_NEXT = "contour_next"
        const val SOURCE_LIBRE = "libre"
        const val SOURCE_ACCU_CHEK = "accu_chek"

        fun create(
            mgDl: Double,
            timestamp: Long = System.currentTimeMillis(),
            source: String? = null,
        ) = BloodTest(
            timestamp = timestamp,
            mgDl = mgDl,
            source = source,
        )
    }
}
