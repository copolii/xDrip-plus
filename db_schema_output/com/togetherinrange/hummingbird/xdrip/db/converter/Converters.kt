package com.togetherinrange.hummingbird.xdrip.db.converter

import androidx.room.TypeConverter
import com.togetherinrange.hummingbird.xdrip.db.entity.LogLevel
import com.togetherinrange.hummingbird.xdrip.db.entity.SyncAction
import com.togetherinrange.hummingbird.xdrip.db.entity.TreatmentType

/**
 * Room TypeConverters for enum types and other non-primitive types.
 *
 * All timestamps are stored as Long (UTC milliseconds) and don't need conversion.
 * Boolean values are stored as INTEGER (0/1) by Room automatically.
 */
class Converters {

    // ==================== TreatmentType ====================

    @TypeConverter
    fun fromTreatmentType(type: TreatmentType): String = type.name

    @TypeConverter
    fun toTreatmentType(value: String): TreatmentType =
        TreatmentType.entries.find { it.name == value } ?: TreatmentType.NOTE

    // ==================== SyncAction ====================

    @TypeConverter
    fun fromSyncAction(action: SyncAction): String = action.name

    @TypeConverter
    fun toSyncAction(value: String): SyncAction =
        SyncAction.entries.find { it.name == value } ?: SyncAction.CREATE

    // ==================== LogLevel ====================

    @TypeConverter
    fun fromLogLevel(level: LogLevel): String = level.name

    @TypeConverter
    fun toLogLevel(value: String): LogLevel =
        LogLevel.entries.find { it.name == value } ?: LogLevel.INFO
}
