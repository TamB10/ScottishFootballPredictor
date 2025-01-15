package com.tam.scottishfootballpredictor.update

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface StatsApi {
    @GET("stats.json")
    suspend fun getLatestStats(): StatsUpdate
}

class StatsUpdateManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Main)

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/TamB10/ScottishFootballPredictor/main/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(StatsApi::class.java)

    private val _statsUpdate = MutableLiveData<StatsUpdate?>()
    val statsUpdate: LiveData<StatsUpdate?> = _statsUpdate

    fun checkForUpdates() {
        val lastCheck = prefs.getLong(LAST_CHECK_KEY, 0)
        val now = System.currentTimeMillis()

        // Check every 24 hours
        if (now - lastCheck > TimeUnit.HOURS.toMillis(24)) {
            scope.launch {
                try {
                    val latestStats = api.getLatestStats()
                    val currentVersion = prefs.getString(STATS_VERSION_KEY, "1.0.0")

                    if (latestStats.version != currentVersion) {
                        _statsUpdate.value = latestStats
                    }

                    prefs.edit()
                        .putLong(LAST_CHECK_KEY, now)
                        .putString(STATS_VERSION_KEY, latestStats.version)
                        .apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "stats_update_prefs"
        private const val LAST_CHECK_KEY = "last_check"
        private const val STATS_VERSION_KEY = "stats_version"
    }
}