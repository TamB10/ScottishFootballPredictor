package com.tam.scottishfootballpredictor.update

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class StatsUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val updateManager = StatsUpdateManager(applicationContext)
        updateManager.checkForUpdates()
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<StatsUpdateWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "stats_update",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}