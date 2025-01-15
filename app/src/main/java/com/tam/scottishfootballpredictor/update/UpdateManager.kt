package com.tam.scottishfootballpredictor.update

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class UpdateManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val updateService = UpdateService(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _updateInfo = MutableLiveData<VersionInfo?>()
    val updateInfo: LiveData<VersionInfo?> = _updateInfo

    private val _downloadProgress = MutableLiveData<Int>()
    val downloadProgress: LiveData<Int> = _downloadProgress

    private var currentDownloadId: Long? = null

    fun checkForUpdates() {
        val lastCheck = prefs.getLong(LAST_CHECK_KEY, 0)
        val now = System.currentTimeMillis()

        if (now - lastCheck > TimeUnit.HOURS.toMillis(1)) {
            scope.launch {
                val version = updateService.checkForUpdates(CURRENT_VERSION)
                _updateInfo.value = version
                prefs.edit().putLong(LAST_CHECK_KEY, now).apply()
            }
        }
    }

    fun downloadUpdate() {
        val versionInfo = _updateInfo.value ?: return
        currentDownloadId = updateService.downloadUpdate(versionInfo)
    }

    fun installUpdate() {
        currentDownloadId?.let { updateService.installUpdate(it) }
    }

    companion object {
        private const val PREFS_NAME = "update_prefs"
        private const val LAST_CHECK_KEY = "last_check"
        private const val CURRENT_VERSION = "1.0.0" // Match with build.gradle versionName
    }
}