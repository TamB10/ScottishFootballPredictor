package com.tam.scottishfootballpredictor

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PredictorViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PredictorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PredictorViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}