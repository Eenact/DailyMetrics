package com.eenact.dailymetrics.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TaskEntity::class, TaskValue::class], exportSchema = false, version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskEntitiesDao(): TaskEntitiesDao

    abstract fun taskValuesDao(): TaskValuesDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "task_database"
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE TaskValue ADD COLUMN task_value TEXT NOT NULL DEFAULT '-1';")
                database.execSQL("UPDATE TaskValue SET task_value = CAST(value AS TEXT);")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE TaskEntity ADD COLUMN is_notification_enabled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE TaskEntity ADD COLUMN notification_time TEXT")
                database.execSQL("ALTER TABLE TaskEntity ADD COLUMN notification_days TEXT")
            }
        }
    }
}