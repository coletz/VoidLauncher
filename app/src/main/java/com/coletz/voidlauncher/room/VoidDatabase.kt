package com.coletz.voidlauncher.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.coletz.voidlauncher.models.AppEntity

@Database(
    entities = [AppEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = []
)
abstract class VoidDatabase: RoomDatabase() {

    abstract fun appEntityDao(): AppEntityDao

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