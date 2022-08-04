package com.coletz.voidlauncher.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.models.TagEntity

@Database(
    entities = [AppEntity::class, TagEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3)
    ]
)
abstract class VoidDatabase: RoomDatabase() {

    abstract fun appEntityDao(): AppEntityDao
    abstract fun tagEntityDao(): TagEntityDao

    companion object {
        @Volatile
        private var INSTANCE: VoidDatabase? = null

        fun getDatabase(context: Context): VoidDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                return Room.databaseBuilder(
                    context.applicationContext,
                    VoidDatabase::class.java,
                    "void_database"
                )
                    .addMigrations(RoomMigrations.FROM_1_TO_2)
                    .build()
                    .apply { INSTANCE = this }
            }
        }
    }

}