package com.togetherinrange.hummingbird.xdrip.db.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.togetherinrange.hummingbird.xdrip.db.AppDatabase

/**
 * ContentProvider for exposing the Hummingbird xDrip database to external apps.
 *
 * This provider exposes read-only access to glucose readings, treatments, sensors,
 * and blood tests. Write access requires the WRITE_DATA permission.
 *
 * URI patterns:
 * - content://authority/glucose_readings - All glucose readings
 * - content://authority/glucose_readings/# - Single reading by ID
 * - content://authority/glucose_readings/latest - Most recent reading
 * - content://authority/glucose_readings/range/#/# - Readings between timestamps
 * - content://authority/treatments - All treatments
 * - content://authority/treatments/# - Single treatment by ID
 * - content://authority/sensors - All sensors
 * - content://authority/sensors/current - Current active sensor
 * - content://authority/blood_tests - All blood tests
 * - content://authority/blood_tests/# - Single blood test by ID
 */
class HummingbirdContentProvider : ContentProvider() {

    private lateinit var database: AppDatabase

    companion object {
        const val AUTHORITY = "com.togetherinrange.hummingbird.xdrip.provider"

        // Permission constants
        const val PERMISSION_READ = "com.togetherinrange.hummingbird.xdrip.permission.READ_DATA"
        const val PERMISSION_WRITE = "com.togetherinrange.hummingbird.xdrip.permission.WRITE_DATA"

        // URI codes
        private const val GLUCOSE_READINGS = 100
        private const val GLUCOSE_READING_ID = 101
        private const val GLUCOSE_READING_LATEST = 102
        private const val GLUCOSE_READING_RANGE = 103

        private const val TREATMENTS = 200
        private const val TREATMENT_ID = 201

        private const val SENSORS = 300
        private const val SENSOR_ID = 301
        private const val SENSOR_CURRENT = 302

        private const val BLOOD_TESTS = 400
        private const val BLOOD_TEST_ID = 401

        private const val CALIBRATIONS = 500
        private const val CALIBRATION_ID = 501

        private const val EVENT_LOG = 600

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            // Glucose readings
            addURI(AUTHORITY, "glucose_readings", GLUCOSE_READINGS)
            addURI(AUTHORITY, "glucose_readings/#", GLUCOSE_READING_ID)
            addURI(AUTHORITY, "glucose_readings/latest", GLUCOSE_READING_LATEST)
            addURI(AUTHORITY, "glucose_readings/range/#/#", GLUCOSE_READING_RANGE)

            // Treatments
            addURI(AUTHORITY, "treatments", TREATMENTS)
            addURI(AUTHORITY, "treatments/#", TREATMENT_ID)

            // Sensors
            addURI(AUTHORITY, "sensors", SENSORS)
            addURI(AUTHORITY, "sensors/#", SENSOR_ID)
            addURI(AUTHORITY, "sensors/current", SENSOR_CURRENT)

            // Blood tests
            addURI(AUTHORITY, "blood_tests", BLOOD_TESTS)
            addURI(AUTHORITY, "blood_tests/#", BLOOD_TEST_ID)

            // Calibrations
            addURI(AUTHORITY, "calibrations", CALIBRATIONS)
            addURI(AUTHORITY, "calibrations/#", CALIBRATION_ID)

            // Event log
            addURI(AUTHORITY, "event_log", EVENT_LOG)
        }

        // Content URIs for external access
        val CONTENT_URI_GLUCOSE_READINGS: Uri = Uri.parse("content://$AUTHORITY/glucose_readings")
        val CONTENT_URI_TREATMENTS: Uri = Uri.parse("content://$AUTHORITY/treatments")
        val CONTENT_URI_SENSORS: Uri = Uri.parse("content://$AUTHORITY/sensors")
        val CONTENT_URI_BLOOD_TESTS: Uri = Uri.parse("content://$AUTHORITY/blood_tests")
        val CONTENT_URI_CALIBRATIONS: Uri = Uri.parse("content://$AUTHORITY/calibrations")

        // MIME types
        private const val MIME_TYPE_DIR = "vnd.android.cursor.dir/vnd.$AUTHORITY"
        private const val MIME_TYPE_ITEM = "vnd.android.cursor.item/vnd.$AUTHORITY"
    }

    override fun onCreate(): Boolean {
        context?.let {
            database = AppDatabase.getInstance(it)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val context = context ?: return null

        val cursor: Cursor? = when (uriMatcher.match(uri)) {
            // Glucose readings
            GLUCOSE_READINGS -> database.glucoseReadingDao().getAllCursor()
            GLUCOSE_READING_ID -> {
                val id = ContentUris.parseId(uri)
                database.glucoseReadingDao().getByIdCursor(id)
            }
            GLUCOSE_READING_LATEST -> database.glucoseReadingDao().getLatestCursor()
            GLUCOSE_READING_RANGE -> {
                val pathSegments = uri.pathSegments
                if (pathSegments.size >= 4) {
                    val start = pathSegments[2].toLongOrNull() ?: return null
                    val end = pathSegments[3].toLongOrNull() ?: return null
                    database.glucoseReadingDao().getInRangeCursor(start, end)
                } else null
            }

            // Treatments
            TREATMENTS -> database.treatmentDao().getAllCursor()
            TREATMENT_ID -> {
                val id = ContentUris.parseId(uri)
                database.treatmentDao().getByIdCursor(id)
            }

            // Sensors
            SENSORS -> database.sensorDao().getAllCursor()
            SENSOR_ID -> {
                val id = ContentUris.parseId(uri)
                database.sensorDao().getByIdCursor(id)
            }
            SENSOR_CURRENT -> database.sensorDao().getCurrentCursor()

            // Blood tests
            BLOOD_TESTS -> database.bloodTestDao().getAllCursor()
            BLOOD_TEST_ID -> {
                val id = ContentUris.parseId(uri)
                database.bloodTestDao().getByIdCursor(id)
            }

            // Calibrations
            CALIBRATIONS -> database.calibrationDao().getAllCursor()
            CALIBRATION_ID -> {
                val id = ContentUris.parseId(uri)
                database.calibrationDao().getByIdCursor(id)
            }

            // Event log
            EVENT_LOG -> database.eventLogDao().getLatestCursor(500)

            else -> null
        }

        cursor?.setNotificationUri(context.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            GLUCOSE_READINGS, GLUCOSE_READING_RANGE -> "$MIME_TYPE_DIR.glucose_reading"
            GLUCOSE_READING_ID, GLUCOSE_READING_LATEST -> "$MIME_TYPE_ITEM.glucose_reading"

            TREATMENTS -> "$MIME_TYPE_DIR.treatment"
            TREATMENT_ID -> "$MIME_TYPE_ITEM.treatment"

            SENSORS -> "$MIME_TYPE_DIR.sensor"
            SENSOR_ID, SENSOR_CURRENT -> "$MIME_TYPE_ITEM.sensor"

            BLOOD_TESTS -> "$MIME_TYPE_DIR.blood_test"
            BLOOD_TEST_ID -> "$MIME_TYPE_ITEM.blood_test"

            CALIBRATIONS -> "$MIME_TYPE_DIR.calibration"
            CALIBRATION_ID -> "$MIME_TYPE_ITEM.calibration"

            EVENT_LOG -> "$MIME_TYPE_DIR.event_log"

            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Write operations require WRITE_DATA permission
        // Implement as needed based on your requirements
        // For now, we return null (read-only provider)
        return null
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        // Write operations require WRITE_DATA permission
        // Implement as needed based on your requirements
        return 0
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        // Write operations require WRITE_DATA permission
        // Implement as needed based on your requirements
        return 0
    }
}
