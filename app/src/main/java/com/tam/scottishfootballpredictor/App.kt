package com.tam.scottishfootballpredictor

import android.app.Application
import com.tam.scottishfootballpredictor.update.StatsUpdateWorker

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        StatsUpdateWorker.schedule(this)
    }
}