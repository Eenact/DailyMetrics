package com.eenact.dailymetrics.data.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/** File name used by every released version of the app. Never change it. */
const val DATABASE_NAME = "task_database"

@Database(
    entities = [TaskEntity::class, TaskValueEntity::class],
    version = 4,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskValueDao(): TaskValueDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/** Applies the configuration shared by all platforms to a platform specific [builder]. */
fun buildAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()

// Migrations 1→2 and 2→3 are copied verbatim from the original Android app.

internal val MIGRATION_1_2 = Migration(1, 2) { connection ->
    connection.execSQL("ALTER TABLE TaskValue ADD COLUMN task_value TEXT NOT NULL DEFAULT '-1';")
    connection.execSQL("UPDATE TaskValue SET task_value = CAST(value AS TEXT);")
}

internal val MIGRATION_2_3 = Migration(2, 3) { connection ->
    connection.execSQL("ALTER TABLE TaskEntity ADD COLUMN is_notification_enabled INTEGER NOT NULL DEFAULT 0")
    connection.execSQL("ALTER TABLE TaskEntity ADD COLUMN notification_time TEXT")
    connection.execSQL("ALTER TABLE TaskEntity ADD COLUMN notification_days TEXT")
}

/**
 * No schema change. Version 3 databases were written by Room 2 on Android; bumping the version makes
 * Room validate the existing tables against the KMP entities and store a fresh identity hash,
 * instead of failing on an identity hash produced by the old code generator.
 */
internal val MIGRATION_3_4 = Migration(3, 4) { }
