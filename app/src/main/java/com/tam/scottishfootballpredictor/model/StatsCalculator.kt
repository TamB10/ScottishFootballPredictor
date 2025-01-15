package com.tam.scottishfootballpredictor.model

import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.round

class StatsCalculator {
    fun calculateVenueEffect(stats: LeagueStats): Double {
        val venue = stats.venueStats
        return (venue.atmosphereEffect + (venue.homeWinRate - 0.5)).coerceIn(-0.5, 0.5)
    }

    fun calculateMatchProbabilities(
        homeXg: Double,
        awayXg: Double,
        h2h: HeadToHeadStats
    ): Triple<Double, Double, Double> {
        var homeWinProb = 0.0
        var drawProb = 0.0
        var awayWinProb = 0.0

        for (i in 0..6) {
            for (j in 0..6) {
                val prob = poissonProbability(i, homeXg) * poissonProbability(j, awayXg)
                when {
                    i > j -> homeWinProb += prob
                    i < j -> awayWinProb += prob
                    else -> drawProb += prob
                }
            }
        }

        val historicalFactor = if (h2h.totalMatches > 0) {
            (h2h.homeWins.toDouble() / h2h.totalMatches - 0.5) * 0.2
        } else 0.0

        homeWinProb *= (1 + historicalFactor)
        awayWinProb *= (1 - historicalFactor)

        val total = homeWinProb + drawProb + awayWinProb
        return Triple(
            round(homeWinProb / total * 1000) / 10,
            round(drawProb / total * 1000) / 10,
            round(awayWinProb / total * 1000) / 10
        )
    }

    private fun poissonProbability(k: Int, lambda: Double): Double {
        return exp(-lambda) * lambda.pow(k) / factorial(k)
    }

    private fun factorial(n: Int): Double {
        var result = 1.0
        for (i in 2..n) {
            result *= i
        }
        return result
    }
}