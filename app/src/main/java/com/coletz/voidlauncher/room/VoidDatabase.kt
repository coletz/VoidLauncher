package com.coletz.voidlauncher.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.coletz.voidlauncher.models.AppEntity
import com.coletz.voidlauncher.models.FolderEntity
import com.coletz.voidlauncher.models.TagEntity
import com.coletz.voidlauncher.room.dao.AppEntityDao
import com.coletz.voidlauncher.room.dao.FolderEntityDao
import com.coletz.voidlauncher.room.dao.TagEntityDao
import com.coletz.voidlauncher.models.FoldersAppsCrossRef

@Database(
    entities = [AppEntity::class, TagEntity::class, FolderEntity::class, FoldersAppsCrossRef::class],
    version = 1,
    exportSchema = true
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
                    .build()
                    .apply { INSTANCE = this }
            }
        }
    }

}