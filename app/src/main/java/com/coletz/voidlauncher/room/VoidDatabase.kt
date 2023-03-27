package com.coletz.voidlauncher.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.models.FolderEntity
import com.coletz.voidlauncher.models.TagEntity

@Database(
    entities = [AppEntity::class, TagEntity::class, FolderEntity::class, FolderAppsCrossRef::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
abstract class VoidDatabase: RoomDatabase() {

    abstract fun appEntityDao(): AppEntityDao
    abstract fun tagEntityDao(): TagEntityDao
    abstract fun folderEntityDao(): FolderEntityDao

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