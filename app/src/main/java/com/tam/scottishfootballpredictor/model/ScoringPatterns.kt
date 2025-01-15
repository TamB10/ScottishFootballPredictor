package com.tam.scottishfootballpredictor.model

data class ScoringPatterns(
    val firstHalfGoals: Int,
    val secondHalfGoals: Int,
    val earlyGoals: Int, // 0-15 mins
    val midGoals: Int,   // 16-75 mins
    val lateGoals: Int,  // 76-90 mins
    val cleanSheetStreak: Int,
    val scoringStreak: Int,
    val homeGoalPattern: GoalPattern,
    val awayGoalPattern: GoalPattern
)

data class GoalPattern(
    val averageFirstHalfGoals: Double,
    val averageSecondHalfGoals: Double,
    val goalTimings: List<Int>, // Minutes when goals typically scored
    val strongestScoringPeriod: String,
    val cleanSheetProbability: Double
)

data class DetailedMatchStats(
    val date: String,
    val homeScore: Int,
    val awayScore: Int,
    val firstHalfScore: String,
    val secondHalfScore: String,
    val goalMinutes: List<Int>,
    val homeCleanSheet: Boolean,
    val awayCleanSheet: Boolean,
    val attendance: Int
)