package com.tam.scottishfootballpredictor.model

data class TeamRecord(
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsScored: Int,
    val goalsConceded: Int,
    val cleanSheets: Int,
    val winPercentage: Double,
    val averageGoalsScored: Double,
    val averageGoalsConceded: Double
)