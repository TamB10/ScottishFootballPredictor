package com.tam.scottishfootballpredictor.venue

data class VenueData(
    val team: String,
    val name: String,
    val capacity: Int,
    val atmosphereEffect: Double,
    val historicalData: VenueHistoricalData = VenueHistoricalData()
)

data class VenueHistoricalData(
    val homeWinRate: Double = 0.0,
    val averageAttendance: Int = 0,
    val averageGoalsScored: Double = 0.0,
    val averageGoalsConceded: Double = 0.0
)

object VenueDatabase {
    val venues = mapOf(
        "Celtic" to VenueData(
            team = "Celtic",
            name = "Celtic Park",
            capacity = 60411,
            atmosphereEffect = 0.95,
            historicalData = VenueHistoricalData(
                homeWinRate = 0.85,
                averageAttendance = 58000,
                averageGoalsScored = 2.8,
                averageGoalsConceded = 0.8
            )
        ),
        "Rangers" to VenueData(
            team = "Rangers",
            name = "Ibrox Stadium",
            capacity = 50817,
            atmosphereEffect = 0.92,
            historicalData = VenueHistoricalData(
                homeWinRate = 0.82,
                averageAttendance = 48000,
                averageGoalsScored = 2.6,
                averageGoalsConceded = 0.9
            )
        ),
        // Add other venues similarly
    )
}