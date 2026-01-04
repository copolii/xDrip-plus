package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "insulin_doses",
    foreignKeys = [
        ForeignKey(
            entity = Treatment::class,
            parentColumns = ["id"],
            childColumns = ["treatment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("treatment_id")
    ]
)
data class InsulinDose(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "treatment_id")
    val treatmentId: Long,

    @ColumnInfo(name = "insulin_name")
    val insulinName: String,

    @ColumnInfo(name = "units")
    val units: Double,

    @ColumnInfo(name = "is_basal", defaultValue = "0")
    val isBasal: Boolean = false,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int? = null,
) {
    companion object {
        // Common insulin names
        const val INSULIN_HUMALOG = "Humalog"
        const val INSULIN_NOVOLOG = "NovoLog"
        const val INSULIN_APIDRA = "Apidra"
        const val INSULIN_FIASP = "Fiasp"
        const val INSULIN_LYUMJEV = "Lyumjev"
        const val INSULIN_LANTUS = "Lantus"
        const val INSULIN_LEVEMIR = "Levemir"
        const val INSULIN_TRESIBA = "Tresiba"
        const val INSULIN_TOUJEO = "Toujeo"
        const val INSULIN_BASAGLAR = "Basaglar"
        const val INSULIN_NPH = "NPH"
        const val INSULIN_REGULAR = "Regular"
        const val INSULIN_UNKNOWN = "Unknown"

        fun bolus(
            treatmentId: Long,
            insulinName: String,
            units: Double,
        ) = InsulinDose(
            treatmentId = treatmentId,
            insulinName = insulinName,
            units = units,
            isBasal = false,
        )

        fun basal(
            treatmentId: Long,
            insulinName: String,
            units: Double,
        ) = InsulinDose(
            treatmentId = treatmentId,
            insulinName = insulinName,
            units = units,
            isBasal = true,
        )

        fun extendedBolus(
            treatmentId: Long,
            insulinName: String,
            units: Double,
            durationMinutes: Int,
        ) = InsulinDose(
            treatmentId = treatmentId,
            insulinName = insulinName,
            units = units,
            isBasal = false,
            durationMinutes = durationMinutes,
        )
    }
}
