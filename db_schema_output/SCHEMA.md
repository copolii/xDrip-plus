# Hummingbird xDrip Database Schema

## Overview

This document describes the Room database schema for Hummingbird xDrip. The database is designed with the following principles:

- **Version 1** - Fresh start, no migration from legacy xDrip+
- **Timestamps** - All timestamps stored as UTC milliseconds (Long)
- **UUIDs** - Used for sync/deduplication of exported data
- **Foreign Keys** - Proper constraints with appropriate cascade actions
- **ContentProvider** - Schema designed for external access via ContentProvider
- **Room-native** - camelCase for Kotlin properties, snake_case for column names

## Entity Relationship Diagram

```
┌─────────────────┐       ┌─────────────────────┐       ┌─────────────────┐
│     Sensor      │       │   GlucoseReading    │       │   Calibration   │
├─────────────────┤       ├─────────────────────┤       ├─────────────────┤
│ id (PK)         │◄──────│ sensorId (FK)       │       │ id (PK)         │
│ uuid            │       │ calibrationId (FK)  │──────►│ sensorId (FK)   │
│ startedAt       │       │ id (PK)             │       │ uuid            │
│ stoppedAt       │       │ uuid                │       │ timestamp       │
│ transmitterId   │       │ timestamp           │       │ bgMgDl          │
│ location        │       │ rawValue            │       │ slope           │
└─────────────────┘       │ filteredValue       │       │ intercept       │
                          │ calculatedMgDl      │       └─────────────────┘
                          │ trendArrow          │
                          └─────────────────────┘

┌─────────────────┐       ┌─────────────────────┐
│    Treatment    │◄──────│    InsulinDose      │
├─────────────────┤       ├─────────────────────┤
│ id (PK)         │       │ id (PK)             │
│ uuid            │       │ treatmentId (FK)    │
│ timestamp       │       │ insulinName         │
│ treatmentType   │       │ units               │
│ carbsGrams      │       │ isBasal             │
│ notes           │       │ durationMinutes     │
└─────────────────┘       └─────────────────────┘

┌─────────────────┐       ┌─────────────────────┐
│  AlertProfile   │◄──────│    ActiveAlert      │
├─────────────────┤       ├─────────────────────┤
│ id (PK)         │       │ id (PK)             │
│ uuid            │       │ alertProfileId (FK) │
│ name            │       │ triggeredAt         │
│ isHighAlert     │       │ snoozedUntil        │
│ thresholdMgDl   │       │ isActive            │
│ ...             │       └─────────────────────┘
└─────────────────┘

┌─────────────────┐       ┌─────────────────────┐       ┌─────────────────┐
│   BloodTest     │       │    SyncQueueItem    │       │    EventLog     │
├─────────────────┤       ├─────────────────────┤       ├─────────────────┤
│ id (PK)         │       │ id (PK)             │       │ id (PK)         │
│ uuid            │       │ entityType          │       │ timestamp       │
│ timestamp       │       │ entityUuid          │       │ level           │
│ mgDl            │       │ action              │       │ tag             │
│ source          │       │ pendingDestinations │       │ message         │
└─────────────────┘       │ completedDests      │       └─────────────────┘
                          └─────────────────────┘

┌─────────────────┐       ┌─────────────────────┐
│ HeartRateReading│       │    StepReading      │
├─────────────────┤       ├─────────────────────┤
│ id (PK)         │       │ id (PK)             │
│ timestamp       │       │ timestamp           │
│ bpm             │       │ steps               │
│ source          │       │ isAbsolute          │
└─────────────────┘       └─────────────────────┘
```

## Tables

### sensors

Represents a CGM sensor session.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| uuid | TEXT | UNIQUE, NOT NULL | UUID for sync/dedup |
| started_at | INTEGER | NOT NULL, INDEX | Session start (UTC ms) |
| stopped_at | INTEGER | NULL | Session end (UTC ms) |
| transmitter_id | TEXT | NULL | Transmitter serial/ID |
| location | TEXT | NULL | Body location (left_arm, etc.) |

### glucose_readings

Individual glucose readings from the sensor.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| uuid | TEXT | UNIQUE, NOT NULL | UUID for sync/dedup |
| sensor_id | INTEGER | FK → sensors(id) ON DELETE CASCADE | Parent sensor |
| calibration_id | INTEGER | FK → calibrations(id) ON DELETE SET NULL | Applied calibration |
| timestamp | INTEGER | NOT NULL, INDEX | Reading time (UTC ms) |
| raw_value | REAL | NOT NULL | Raw sensor value |
| filtered_value | REAL | NOT NULL | Filtered sensor value |
| calculated_mg_dl | REAL | NULL | Calculated glucose mg/dL |
| trend_arrow | INTEGER | NULL | Trend direction (1-7) |
| noise_level | INTEGER | DEFAULT 0 | Signal noise level |
| source | TEXT | NULL | Data source identifier |
| is_backfill | INTEGER | DEFAULT 0 | Boolean: backfilled data |
| hide_slope | INTEGER | DEFAULT 0 | Boolean: hide trend |

### calibrations

Calibration points linking fingerstick to sensor values.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| uuid | TEXT | UNIQUE, NOT NULL | UUID for sync/dedup |
| sensor_id | INTEGER | FK → sensors(id) ON DELETE CASCADE | Parent sensor |
| timestamp | INTEGER | NOT NULL, INDEX | Calibration time (UTC ms) |
| bg_mg_dl | REAL | NOT NULL | Reference BG value |
| raw_value | REAL | NOT NULL | Raw sensor value at time |
| slope | REAL | DEFAULT 1.0 | Calibration slope |
| intercept | REAL | DEFAULT 0.0 | Calibration intercept |
| slope_confidence | REAL | NULL | Slope confidence factor |
| is_valid | INTEGER | DEFAULT 1 | Boolean: valid calibration |
| source | TEXT | NULL | Calibration source |

### blood_tests

Fingerstick blood glucose tests.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| uuid | TEXT | UNIQUE, NOT NULL | UUID for sync/dedup |
| timestamp | INTEGER | NOT NULL, INDEX | Test time (UTC ms) |
| mg_dl | REAL | NOT NULL | Blood glucose mg/dL |
| created_at | INTEGER | NOT NULL | Record creation time |
| source | TEXT | NULL | Meter/source identifier |
| is_valid | INTEGER | DEFAULT 1 | Boolean: valid reading |
| used_for_calibration | INTEGER | DEFAULT 0 | Boolean: used for cal |

### treatments

Treatment entries (insulin, carbs, notes, events).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| uuid | TEXT | UNIQUE, NOT NULL | UUID for sync/dedup |
| timestamp | INTEGER | NOT NULL, INDEX | Treatment time (UTC ms) |
| treatment_type | TEXT | NOT NULL, INDEX | Enum: INSULIN, CARBS, etc. |
| carbs_grams | REAL | NULL | Carbohydrate amount |
| notes | TEXT | NULL | User notes |
| entered_by | TEXT | NULL | Entry source/user |
| created_at | INTEGER | NOT NULL | Record creation time |
| is_valid | INTEGER | DEFAULT 1 | Boolean: not deleted |

**Treatment Types:** `INSULIN`, `CARBS`, `NOTE`, `EXERCISE`, `SENSOR_START`, `SENSOR_STOP`, `ANNOUNCEMENT`, `TEMP_BASAL`, `PROFILE_SWITCH`

### insulin_doses

Individual insulin doses linked to treatments (replaces JSON blob).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| treatment_id | INTEGER | FK → treatments(id) ON DELETE CASCADE | Parent treatment |
| insulin_name | TEXT | NOT NULL | Insulin type name |
| units | REAL | NOT NULL | Dose amount |
| is_basal | INTEGER | DEFAULT 0 | Boolean: basal insulin |
| duration_minutes | INTEGER | NULL | Extended bolus duration |

### alert_profiles

Alert configuration profiles.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| uuid | TEXT | UNIQUE, NOT NULL | UUID for sync/dedup |
| name | TEXT | NOT NULL | Profile name |
| is_enabled | INTEGER | DEFAULT 1 | Boolean: enabled |
| is_high_alert | INTEGER | NOT NULL | Boolean: high (1) or low (0) |
| threshold_mg_dl | REAL | NOT NULL | Trigger threshold |
| is_predictive | INTEGER | DEFAULT 0 | Boolean: predictive alert |
| prediction_minutes | INTEGER | NULL | Prediction lookahead |
| is_all_day | INTEGER | DEFAULT 1 | Boolean: 24h active |
| start_minute | INTEGER | DEFAULT 0 | Day start (mins from midnight) |
| end_minute | INTEGER | DEFAULT 1439 | Day end (mins from midnight) |
| volume_percent | INTEGER | DEFAULT 100 | Alert volume 0-100 |
| vibrate | INTEGER | DEFAULT 1 | Boolean: vibrate |
| override_silent | INTEGER | DEFAULT 0 | Boolean: override silent mode |
| snooze_minutes | INTEGER | DEFAULT 30 | Default snooze duration |
| re_alert_minutes | INTEGER | DEFAULT 5 | Re-alert interval |
| sound_uri | TEXT | NULL | Custom sound URI |

### active_alerts

Currently active/snoozed alerts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| alert_profile_id | INTEGER | FK → alert_profiles(id) ON DELETE CASCADE | Parent profile |
| triggered_at | INTEGER | NOT NULL | Trigger time (UTC ms) |
| snoozed_until | INTEGER | NULL | Snooze end time (UTC ms) |
| is_active | INTEGER | DEFAULT 1 | Boolean: still active |

### sync_queue

Unified upload queue for all sync destinations.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| entity_type | TEXT | NOT NULL, INDEX | Table name of source entity |
| entity_uuid | TEXT | NOT NULL | UUID of source entity |
| action | TEXT | NOT NULL | Enum: CREATE, UPDATE, DELETE |
| created_at | INTEGER | NOT NULL, INDEX | Queue time (UTC ms) |
| pending_destinations | INTEGER | NOT NULL | Bitfield of pending dests |
| completed_destinations | INTEGER | DEFAULT 0 | Bitfield of completed dests |
| last_attempt_at | INTEGER | NULL | Last sync attempt time |
| attempt_count | INTEGER | DEFAULT 0 | Number of attempts |
| last_error | TEXT | NULL | Last error message |

**Sync Destinations (bitfield):**
- `NIGHTSCOUT = 1`
- `TIDEPOOL = 2`
- `INFLUXDB = 4`
- `WATCH = 8`
- `FOLLOWER = 16`

### heart_rate_readings

Heart rate data from wearables.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| timestamp | INTEGER | UNIQUE, NOT NULL | Reading time (UTC ms) |
| bpm | INTEGER | NOT NULL | Beats per minute |
| source | TEXT | NULL | Device source |

### step_readings

Step count data from wearables.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| timestamp | INTEGER | UNIQUE, NOT NULL | Reading time (UTC ms) |
| steps | INTEGER | NOT NULL | Step count |
| is_absolute | INTEGER | DEFAULT 0 | Boolean: absolute vs delta |
| source | TEXT | NULL | Device source |

### event_log

Application event/error log.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Local row ID |
| timestamp | INTEGER | NOT NULL, INDEX | Event time (UTC ms) |
| level | TEXT | NOT NULL, INDEX | Enum: DEBUG, INFO, etc. |
| tag | TEXT | NOT NULL, INDEX | Log tag/category |
| message | TEXT | NOT NULL | Log message |
| details | TEXT | NULL | Additional details |

**Log Levels:** `DEBUG`, `INFO`, `WARNING`, `ERROR`, `USER_EVENT`

## ContentProvider URIs

The database is exposed via ContentProvider at authority: `com.togetherinrange.hummingbird.xdrip.provider`

### URI Patterns

| URI | Description |
|-----|-------------|
| `content://authority/glucose_readings` | All glucose readings |
| `content://authority/glucose_readings/#` | Single reading by ID |
| `content://authority/glucose_readings/latest` | Most recent reading |
| `content://authority/glucose_readings/range/#/#` | Readings between timestamps |
| `content://authority/treatments` | All treatments |
| `content://authority/treatments/#` | Single treatment by ID |
| `content://authority/sensors` | All sensors |
| `content://authority/sensors/current` | Current active sensor |
| `content://authority/blood_tests` | All blood tests |
| `content://authority/stats/tir/#/#` | Time-in-range for period |

### Permissions

- `com.togetherinrange.hummingbird.xdrip.permission.READ_DATA` - Read access
- `com.togetherinrange.hummingbird.xdrip.permission.WRITE_DATA` - Write access

## Indexes

| Table | Index | Columns |
|-------|-------|---------|
| sensors | idx_sensors_started_at | started_at |
| sensors | idx_sensors_uuid | uuid (UNIQUE) |
| glucose_readings | idx_glucose_timestamp | timestamp |
| glucose_readings | idx_glucose_sensor | sensor_id |
| glucose_readings | idx_glucose_uuid | uuid (UNIQUE) |
| calibrations | idx_calibrations_timestamp | timestamp |
| calibrations | idx_calibrations_sensor | sensor_id |
| treatments | idx_treatments_timestamp | timestamp |
| treatments | idx_treatments_type | treatment_type |
| blood_tests | idx_blood_tests_timestamp | timestamp |
| sync_queue | idx_sync_queue_created | created_at |
| sync_queue | idx_sync_queue_pending | pending_destinations |
| event_log | idx_event_log_timestamp | timestamp |
| event_log | idx_event_log_level | level |

## Data Retention

Recommended cleanup policies:

| Table | Retention | Notes |
|-------|-----------|-------|
| glucose_readings | 90 days | Configurable |
| treatments | 90 days | Configurable |
| blood_tests | 90 days | Configurable |
| calibrations | Sensor lifetime | Delete with sensor |
| sync_queue | 7 days | After completion |
| event_log | 7 days | Configurable |
| heart_rate_readings | 30 days | Configurable |
| step_readings | 30 days | Configurable |

## Migration Notes

This is **Version 1** - a clean start. No migration from legacy xDrip+ ActiveAndroid database.

For users migrating from xDrip+, a separate one-time import tool should be provided that:
1. Reads the legacy ActiveAndroid database
2. Transforms data to new schema
3. Generates new UUIDs where missing
4. Inserts into the new Room database

## Not Stored in Database

The following are stored in DataStore/Preferences, not the database:

- **Active Bluetooth Device** - Single device, use DataStore
- **User Preferences** - App settings
- **Transmitter State** - Runtime state
- **Cache Data** - Transient data
