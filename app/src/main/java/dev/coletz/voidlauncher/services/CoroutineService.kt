package dev.coletz.voidlauncher.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.Nullable
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


abstract class CoroutineService : Service(), CoroutineScope {
    private lateinit var coroutineJob: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    override fun onCreate() {
        super.onCreate()
        coroutineJob = Job()
    }

    override fun onDestroy() {
        coroutineJob.cancel()
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? = null
}