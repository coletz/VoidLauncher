package com.coletz.voidlauncher.mvvm

import com.coletz.voidlauncher.models.AppEntity

sealed class AppListResult {
    class Success(val list: List<AppEntity>): AppListResult()
    object MissingContext: AppListResult()
}
