package com.tam.scottishfootballpredictor.model

data class PredictionResult(
    val homeGoals: Int,
    val awayGoals: Int,
    val homeXg: Double,
    val awayXg: Double,
    val homeWinProb: Double,
    val drawProb: Double,
    val awayWinProb: Double,
    val homeForm: Double,
    val awayForm: Double,
    val homeStats: TeamStats,
    val awayStats: TeamStats,
    val leagueStats: LeagueStats,
    //val scoringPatterns: ScoringPatterns  // Added this line
)