package com.tam.scottishfootballpredictor.model

data class League(
    val teams: List<String>,
    val avgGoals: Double,
    val maxHome: Int,
    val maxAway: Int,
    val strengthModifier: Double,
    val homeAdvantage: Double,
    val teamModifiers: Map<String, Double> = emptyMap()
)