package com.tam.scottishfootballpredictor.model

data class MatchResult(
    val homeScore: Int,
    val awayScore: Int,
    val date: String,
    val attendance: Int
)