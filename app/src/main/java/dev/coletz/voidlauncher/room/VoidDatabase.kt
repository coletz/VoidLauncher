package dev.coletz.voidlauncher.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.coletz.voidlauncher.models.AppEntity
import dev.coletz.voidlauncher.models.FolderEntity
import dev.coletz.voidlauncher.models.TagEntity
import dev.coletz.voidlauncher.room.dao.AppEntityDao
import dev.coletz.voidlauncher.room.dao.FolderEntityDao
import dev.coletz.voidlauncher.room.dao.TagEntityDao
import dev.coletz.voidlauncher.models.FoldersAppsCrossRef

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