package dev.coletz.voidlauncher.models

import android.content.Context
import android.content.Intent
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_entity")
class AppEntity(
    @PrimaryKey
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "official_name") val officialName: String,
    @ColumnInfo(name = "edited_name") val editedName: String? = null,
    @ColumnInfo(name = "is_intent") val isIntent: Boolean = false,
    @ColumnInfo(name = "is_hidden") val isHidden: Boolean = false,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false
) {

    val uiName: String
        get() = editedName ?: officialName

    fun launch(context: Context?, onError: (Throwable) -> Unit) {
        context ?: return
        if(isIntent) {
            context.startActivity(Intent(packageName))
        } else {
            context
                .packageManager
                .getLaunchIntentForPackage(packageName)
                .runCatching { context.startActivity(this) }
                .exceptionOrNull()
                ?.run(onError)
        }
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if(other !is AppEntity) return false
        return packageName == other.packageName
    }
}