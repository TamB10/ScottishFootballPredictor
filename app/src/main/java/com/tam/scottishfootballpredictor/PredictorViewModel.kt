package com.tam.scottishfootballpredictor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tam.scottishfootballpredictor.model.PredictionResult
import com.tam.scottishfootballpredictor.model.ScottishPredictor
import com.tam.scottishfootballpredictor.update.StatsUpdateManager

class PredictorViewModel(application: Application) : AndroidViewModel(application) {
    private val statsUpdateManager = StatsUpdateManager(application)
    private val predictor = ScottishPredictor()
    private val _predictionResult = MutableLiveData<PredictionResult?>()
    val predictionResult: LiveData<PredictionResult?> = _predictionResult

    init {
        checkForUpdates()
    }

    fun getLeagues(): List<String> = predictor.leagues.keys.toList()

    fun getTeamsForLeague(league: String): List<String> =
        predictor.leagues[league]?.teams ?: emptyList()

    fun predictScore(homeTeam: String, awayTeam: String, league: String) {
        try {
            val result = predictor.predictScore(homeTeam, awayTeam, league)
            _predictionResult.value = result
        } catch (e: Exception) {
            e.printStackTrace()
            _predictionResult.value = null
        }
    }

    fun clearPrediction() {
        _predictionResult.value = null
    }

    fun checkForUpdates() {
        statsUpdateManager.checkForUpdates()
    }
}