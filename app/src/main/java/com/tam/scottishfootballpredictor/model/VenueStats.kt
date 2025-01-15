package com.tam.scottishfootballpredictor.model

data class VenueStats(
    val venueName: String,
    val capacity: Int,
    val averageAttendance: Int,
    val homeWinRate: Double,
    val averageGoalsScored: Double,
    val averageGoalsConceded: Double,
    val atmosphereEffect: Double
)