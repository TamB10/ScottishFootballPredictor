package com.tam.scottishfootballpredictor.update

// Result wrapper
data class StatsUpdate(
    val version: String,
    val lastUpdated: String,
    val leagues: Map<String, LeagueData>
)

// League data
data class LeagueData(
    val teams: Map<String, TeamStats>
)

// Team statistics
data class TeamStats(
    val position: Int,
    val stats: TeamStatistics,
    val form: Form
)

// Detailed team stats
data class TeamStatistics(
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val cleanSheets: Int
)

// Team form
data class Form(
    val last5: List<String>
)

// Raw team data from scraping
data class TeamData(
    val name: String,
    val position: Int,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val cleanSheets: Int
)