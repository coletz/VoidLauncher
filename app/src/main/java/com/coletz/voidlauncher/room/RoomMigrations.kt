package com.coletz.voidlauncher.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigrations{
    val FROM_1_TO_2: Migration = object: Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE app_entity ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
        }
    }
}