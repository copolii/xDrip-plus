package com.togetherinrange.hummingbird.xdrip.db.provider

import android.net.Uri

/**
 * Contract class defining the ContentProvider schema for external apps.
 *
 * External apps can use these constants to query the Hummingbird xDrip database.
 *
 * Example usage:
 * ```kotlin
 * // Query latest glucose reading
 * val cursor = contentResolver.query(
 *     GlucoseReadingContract.CONTENT_URI_LATEST,
 *     null, null, null, null
 * )
 *
 * cursor?.use {
 *     if (it.moveToFirst()) {
 *         val mgDl = it.getDouble(it.getColumnIndexOrThrow(GlucoseReadingContract.COLUMN_CALCULATED_MG_DL))
 *         val timestamp = it.getLong(it.getColumnIndexOrThrow(GlucoseReadingContract.COLUMN_TIMESTAMP))
 *     }
 * }
 * ```
 */
object ContentProviderContract {
    const val AUTHORITY = "com.togetherinrange.hummingbird.xdrip.provider"
    val BASE_URI: Uri = Uri.parse("content://$AUTHORITY")

    /**
     * Required permission for reading data
     */
    const val PERMISSION_READ = "com.togetherinrange.hummingbird.xdrip.permission.READ_DATA"

    /**
     * Required permission for writing data
     */
    const val PERMISSION_WRITE = "com.togetherinrange.hummingbird.xdrip.permission.WRITE_DATA"
}

/**
 * Contract for glucose_readings table
 */
object GlucoseReadingContract {
    const val TABLE_NAME = "glucose_readings"

    val CONTENT_URI: Uri = Uri.withAppendedPath(ContentProviderContract.BASE_URI, TABLE_NAME)
    val CONTENT_URI_LATEST: Uri = Uri.withAppendedPath(CONTENT_URI, "latest")

    fun contentUriForRange(startTimestamp: Long, endTimestamp: Long): Uri =
        Uri.withAppendedPath(CONTENT_URI, "range/$startTimestamp/$endTimestamp")

    // Column names
    const val COLUMN_ID = "id"
    const val COLUMN_UUID = "uuid"
    const val COLUMN_SENSOR_ID = "sensor_id"
    const val COLUMN_CALIBRATION_ID = "calibration_id"
    const val COLUMN_TIMESTAMP = "timestamp"
    const val COLUMN_RAW_VALUE = "raw_value"
    const val COLUMN_FILTERED_VALUE = "filtered_value"
    const val COLUMN_CALCULATED_MG_DL = "calculated_mg_dl"
    const val COLUMN_TREND_ARROW = "trend_arrow"
    const val COLUMN_NOISE_LEVEL = "noise_level"
    const val COLUMN_SOURCE = "source"
    const val COLUMN_IS_BACKFILL = "is_backfill"
    const val COLUMN_HIDE_SLOPE = "hide_slope"
}

/**
 * Contract for treatments table
 */
object TreatmentContract {
    const val TABLE_NAME = "treatments"

    val CONTENT_URI: Uri = Uri.withAppendedPath(ContentProviderContract.BASE_URI, TABLE_NAME)

    // Column names
    const val COLUMN_ID = "id"
    const val COLUMN_UUID = "uuid"
    const val COLUMN_TIMESTAMP = "timestamp"
    const val COLUMN_TREATMENT_TYPE = "treatment_type"
    const val COLUMN_CARBS_GRAMS = "carbs_grams"
    const val COLUMN_NOTES = "notes"
    const val COLUMN_ENTERED_BY = "entered_by"
    const val COLUMN_CREATED_AT = "created_at"
    const val COLUMN_IS_VALID = "is_valid"

    // Treatment types
    const val TYPE_INSULIN = "INSULIN"
    const val TYPE_CARBS = "CARBS"
    const val TYPE_NOTE = "NOTE"
    const val TYPE_EXERCISE = "EXERCISE"
    const val TYPE_SENSOR_START = "SENSOR_START"
    const val TYPE_SENSOR_STOP = "SENSOR_STOP"
}

/**
 * Contract for sensors table
 */
object SensorContract {
    const val TABLE_NAME = "sensors"

    val CONTENT_URI: Uri = Uri.withAppendedPath(ContentProviderContract.BASE_URI, TABLE_NAME)
    val CONTENT_URI_CURRENT: Uri = Uri.withAppendedPath(CONTENT_URI, "current")

    // Column names
    const val COLUMN_ID = "id"
    const val COLUMN_UUID = "uuid"
    const val COLUMN_STARTED_AT = "started_at"
    const val COLUMN_STOPPED_AT = "stopped_at"
    const val COLUMN_TRANSMITTER_ID = "transmitter_id"
    const val COLUMN_LOCATION = "location"
}

/**
 * Contract for blood_tests table
 */
object BloodTestContract {
    const val TABLE_NAME = "blood_tests"

    val CONTENT_URI: Uri = Uri.withAppendedPath(ContentProviderContract.BASE_URI, TABLE_NAME)

    // Column names
    const val COLUMN_ID = "id"
    const val COLUMN_UUID = "uuid"
    const val COLUMN_TIMESTAMP = "timestamp"
    const val COLUMN_MG_DL = "mg_dl"
    const val COLUMN_CREATED_AT = "created_at"
    const val COLUMN_SOURCE = "source"
    const val COLUMN_IS_VALID = "is_valid"
    const val COLUMN_USED_FOR_CALIBRATION = "used_for_calibration"
}

/**
 * Contract for calibrations table
 */
object CalibrationContract {
    const val TABLE_NAME = "calibrations"

    val CONTENT_URI: Uri = Uri.withAppendedPath(ContentProviderContract.BASE_URI, TABLE_NAME)

    // Column names
    const val COLUMN_ID = "id"
    const val COLUMN_UUID = "uuid"
    const val COLUMN_SENSOR_ID = "sensor_id"
    const val COLUMN_TIMESTAMP = "timestamp"
    const val COLUMN_BG_MG_DL = "bg_mg_dl"
    const val COLUMN_RAW_VALUE = "raw_value"
    const val COLUMN_SLOPE = "slope"
    const val COLUMN_INTERCEPT = "intercept"
    const val COLUMN_SLOPE_CONFIDENCE = "slope_confidence"
    const val COLUMN_IS_VALID = "is_valid"
    const val COLUMN_SOURCE = "source"
}
