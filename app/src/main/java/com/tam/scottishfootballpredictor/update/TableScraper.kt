package com.tam.scottishfootballpredictor.update

import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.GsonBuilder
import java.time.LocalDate

class TableScraper {
    private val client = OkHttpClient()

    // SofaScore API endpoint for Scottish leagues
    private val leagues = mapOf(
        "Premiership" to "https://api.sofascore.com/api/v1/tournament/12/standing/total",
        "Championship" to "https://api.sofascore.com/api/v1/tournament/13/standing/total",
        "League 1" to "https://api.sofascore.com/api/v1/tournament/14/standing/total",
        "League 2" to "https://api.sofascore.com/api/v1/tournament/15/standing/total"
    )

    fun scrapeAndGenerateJson(): String {
        try {
            println("Starting data fetch...")
            val leagueData = mutableMapOf<String, List<TeamData>>()

            leagues.forEach { (leagueName, url) ->
                try {
                    println("\nFetching $leagueName from $url")
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "Mozilla/5.0")
                        .build()

                    // Log before API call
                    println("Making API call for $leagueName")

                    client.newCall(request).execute().use { response ->
                        println("Got response code: ${response.code}")

                        val body = response.body?.string()
                        println("Response body (first 100 chars): ${body?.take(100)}")

                        // Get dummy data
                        val teams = listOf(
                            TeamData("Celtic", 1, 20, 15, 3, 2, 45, 15, 8),
                            TeamData("Rangers", 2, 20, 14, 4, 2, 40, 18, 7)
                        )
                        println("Created ${teams.size} dummy teams for $leagueName")

                        leagueData[leagueName] = teams
                        println("Added teams to leagueData")
                    }
                } catch (e: Exception) {
                    println("Error fetching $leagueName: ${e.message}")
                    e.printStackTrace()
                }
            }

            println("\nCreating JSON from league data with ${leagueData.size} leagues")
            val json = formatToJson(leagueData)
            println("JSON length: ${json.length}")
            println("First 200 chars of JSON: ${json.take(200)}")

            return json

        } catch (e: Exception) {
            println("Fatal error: ${e.message}")
            e.printStackTrace()
            return "{\"version\":\"1.0.0\",\"lastUpdated\":\"${LocalDate.now()}\",\"leagues\":{}}"
        }
    }

    private fun parseResponse(jsonString: String): List<TeamData> {
        // For now, return dummy data to test the pipeline
        return listOf(
            TeamData("Celtic", 1, 20, 15, 3, 2, 45, 15, 8),
            TeamData("Rangers", 2, 20, 14, 4, 2, 40, 18, 7)
        )
    }

    private fun formatToJson(leagueData: Map<String, List<TeamData>>): String {
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
                            form = calculateForm(team)
                        )
                    }
                )
            }
        )

        return GsonBuilder().setPrettyPrinting().create().toJson(statsUpdate)
    }

    private fun calculateForm(team: TeamData): Form {
        return Form(last5 = listOf("W", "D", "L", "W", "W"))
    }
}

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