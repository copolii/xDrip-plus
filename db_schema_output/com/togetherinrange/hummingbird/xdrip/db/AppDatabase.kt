package com.togetherinrange.hummingbird.xdrip.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.togetherinrange.hummingbird.xdrip.db.converter.Converters
import com.togetherinrange.hummingbird.xdrip.db.dao.ActivityDao
import com.togetherinrange.hummingbird.xdrip.db.dao.AlertDao
import com.togetherinrange.hummingbird.xdrip.db.dao.BloodTestDao
import com.togetherinrange.hummingbird.xdrip.db.dao.CalibrationDao
import com.togetherinrange.hummingbird.xdrip.db.dao.EventLogDao
import com.togetherinrange.hummingbird.xdrip.db.dao.GlucoseReadingDao
import com.togetherinrange.hummingbird.xdrip.db.dao.SensorDao
import com.togetherinrange.hummingbird.xdrip.db.dao.SyncQueueDao
import com.togetherinrange.hummingbird.xdrip.db.dao.TreatmentDao
import com.togetherinrange.hummingbird.xdrip.db.entity.ActiveAlert
import com.togetherinrange.hummingbird.xdrip.db.entity.AlertProfile
import com.togetherinrange.hummingbird.xdrip.db.entity.BloodTest
import com.togetherinrange.hummingbird.xdrip.db.entity.Calibration
import com.togetherinrange.hummingbird.xdrip.db.entity.EventLog
import com.togetherinrange.hummingbird.xdrip.db.entity.GlucoseReading
import com.togetherinrange.hummingbird.xdrip.db.entity.HeartRateReading
import com.togetherinrange.hummingbird.xdrip.db.entity.InsulinDose
import com.togetherinrange.hummingbird.xdrip.db.entity.Sensor
import com.togetherinrange.hummingbird.xdrip.db.entity.StepReading
import com.togetherinrange.hummingbird.xdrip.db.entity.SyncQueueItem
import com.togetherinrange.hummingbird.xdrip.db.entity.Treatment

@Database(
    version = 1,
    entities = [
        // Core glucose data
        Sensor::class,
        GlucoseReading::class,
        Calibration::class,
        BloodTest::class,

        // Treatments
        Treatment::class,
        InsulinDose::class,

        // Alerts
        AlertProfile::class,
        ActiveAlert::class,

        // Sync
        SyncQueueItem::class,

        // Activity tracking
        HeartRateReading::class,
        StepReading::class,

        // Logs
        EventLog::class,
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // ==================== DAOs ====================

    abstract fun sensorDao(): SensorDao
    abstract fun glucoseReadingDao(): GlucoseReadingDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun bloodTestDao(): BloodTestDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun alertDao(): AlertDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun activityDao(): ActivityDao
    abstract fun eventLogDao(): EventLogDao

    companion object {
        const val DATABASE_NAME = "hummingbird_xdrip.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // For version 1, no migration needed
                .build()
        }

        /**
         * For testing only
         */
        fun getInMemoryDatabase(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
