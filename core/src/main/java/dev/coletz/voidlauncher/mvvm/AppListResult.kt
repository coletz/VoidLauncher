package dev.coletz.voidlauncher.mvvm

import dev.coletz.voidlauncher.models.AppEntity

sealed class AppListResult {
    class Success(val list: List<AppEntity>): AppListResult()
    object MissingContext: AppListResult()
}
