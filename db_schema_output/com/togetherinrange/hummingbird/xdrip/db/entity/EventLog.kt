package com.togetherinrange.hummingbird.xdrip.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class LogLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    USER_EVENT;

    companion object {
        fun fromString(value: String): LogLevel =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: INFO
    }
}

@Entity(
    tableName = "event_log",
    indices = [
        Index("timestamp"),
        Index("level"),
        Index("tag")
    ]
)
data class EventLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "level")
    val level: LogLevel,

    @ColumnInfo(name = "tag")
    val tag: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "details")
    val details: String? = null,
) {
    companion object {
        fun debug(tag: String, message: String, details: String? = null) =
            EventLog(level = LogLevel.DEBUG, tag = tag, message = message, details = details)

        fun info(tag: String, message: String, details: String? = null) =
            EventLog(level = LogLevel.INFO, tag = tag, message = message, details = details)

        fun warning(tag: String, message: String, details: String? = null) =
            EventLog(level = LogLevel.WARNING, tag = tag, message = message, details = details)

        fun error(tag: String, message: String, details: String? = null) =
            EventLog(level = LogLevel.ERROR, tag = tag, message = message, details = details)

        fun error(tag: String, message: String, throwable: Throwable) =
            EventLog(
                level = LogLevel.ERROR,
                tag = tag,
                message = message,
                details = throwable.stackTraceToString()
            )

        fun userEvent(tag: String, message: String, details: String? = null) =
            EventLog(level = LogLevel.USER_EVENT, tag = tag, message = message, details = details)
    }
}
