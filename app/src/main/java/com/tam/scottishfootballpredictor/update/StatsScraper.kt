package com.tam.scottishfootballpredictor.update

import com.google.gson.GsonBuilder
import java.io.File
import java.time.LocalDate

object StatsScraper {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting scraper...")

        try {
            // Create dummy data
            val leagueData = mapOf(
                "Premiership" to listOf(
                    TeamData("Celtic", 1, 20, 15, 3, 2, 45, 15, 8),
                    TeamData("Rangers", 2, 20, 14, 4, 2, 40, 18, 7)
                )
            )

            // Convert to StatsUpdate format
            val statsUpdate = StatsUpdate(
                version = "1.0.${LocalDate.now()}",
                lastUpdated = LocalDate.now().toString(),
                leagues = leagueData.mapValues { (_, teams) ->
                    LeagueData(
                        teams = teams.associate { team ->
                            team.name to TeamStats(
                                position = team.position,
                                stats = TeamStatistics(
                                    played = team.played,
                                    wins = team.won,
                                    draws = team.drawn,
                                    losses = team.lost,
                                    goalsFor = team.goalsFor,
                                    goalsAgainst = team.goalsAgainst,
                                    cleanSheets = team.cleanSheets
                                ),
                                form = Form(last5 = listOf("W", "D", "L", "W", "W"))
                            )
                        }
                    )
                }
            )

            // Convert to JSON
            val json = GsonBuilder().setPrettyPrinting().create().toJson(statsUpdate)
            println("Generated JSON: $json")

            // Write to file
            val statsFile = File("stats/stats.json")
            statsFile.parentFile?.mkdirs()
            statsFile.writeText(json)
            println("Stats written to: ${statsFile.absolutePath}")

        } catch (e: Exception) {
            println("Error running scraper:")
            e.printStackTrace()
        }
    }
}