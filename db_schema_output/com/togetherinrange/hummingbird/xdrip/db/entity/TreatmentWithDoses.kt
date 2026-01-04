package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relation class for Treatment with its associated insulin doses.
 * Used for queries that need the full treatment data including all insulin doses.
 */
data class TreatmentWithDoses(
    @Embedded
    val treatment: Treatment,

    @Relation(
        parentColumn = "id",
        entityColumn = "treatment_id"
    )
    val insulinDoses: List<InsulinDose>
) {
    /**
     * Total insulin units across all doses in this treatment
     */
    val totalInsulinUnits: Double
        get() = insulinDoses.sumOf { it.units }

    /**
     * Total bolus (non-basal) insulin units
     */
    val bolusUnits: Double
        get() = insulinDoses.filter { !it.isBasal }.sumOf { it.units }

    /**
     * Total basal insulin units
     */
    val basalUnits: Double
        get() = insulinDoses.filter { it.isBasal }.sumOf { it.units }

    /**
     * Whether this treatment has any insulin doses
     */
    val hasInsulin: Boolean
        get() = insulinDoses.isNotEmpty()

    /**
     * Whether this treatment has only basal insulin
     */
    val isBasalOnly: Boolean
        get() = insulinDoses.isNotEmpty() && insulinDoses.all { it.isBasal }
}
