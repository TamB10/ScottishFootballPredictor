package com.tam.scottishfootballpredictor.update

data class StatsUpdate(
    val version: String,
    val lastUpdated: String,
    val leagues: Map<String, LeagueData>
)

data class LeagueData(
    val teams: Map<String, TeamStats>
)

data class TeamStats(
    val position: Int,
    val stats: TeamStatistics,
    val form: Form
)

data class TeamStatistics(
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val cleanSheets: Int
)

data class Form(
    val last5: List<String>
)