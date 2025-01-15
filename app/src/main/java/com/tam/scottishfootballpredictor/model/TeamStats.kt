package com.tam.scottishfootballpredictor.model

data class TeamStats(
    val league: String,
    val strength: Double,
    val matches: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsScored: Int,
    val goalsConceded: Int,
    val cleanSheets: Int,
    val form: List<Int>
)