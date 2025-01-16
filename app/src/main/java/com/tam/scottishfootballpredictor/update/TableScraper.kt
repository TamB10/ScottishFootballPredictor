package com.tam.scottishfootballpredictor.update

import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.GsonBuilder
import java.time.LocalDate

class TableScraper {
    private val client = OkHttpClient()

    private val leagues = mapOf(
        "Premiership" to "https://spfl.co.uk/api/competition.php?feed_type=league&id=premiership",
        "Championship" to "https://spfl.co.uk/api/competition.php?feed_type=league&id=championship",
        "League 1" to "https://spfl.co.uk/api/competition.php?feed_type=league&id=league-one",
        "League 2" to "https://spfl.co.uk/api/competition.php?feed_type=league&id=league-two"
    )

    fun scrapeAndGenerateJson(): String {
        try {
            val leagueData = leagues.mapValues { (leagueName, url) ->
                try {
                    println("Fetching data for $leagueName from $url")

                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .build()

                    client.newCall(request).execute().use { response ->
                        val body = response.body?.string()
                        println("Response for $leagueName: $body")
                        parseTeamData(body ?: "")
                    }

                } catch (e: Exception) {
                    println("Error fetching $leagueName: ${e.message}")
                    e.printStackTrace()
                    emptyList()
                }
            }

            return formatToJson(leagueData)

        } catch (e: Exception) {
            println("Fatal error in scraping: ${e.message}")
            e.printStackTrace()
            return "{\"version\":\"1.0.0\",\"lastUpdated\":\"${LocalDate.now()}\",\"leagues\":{},\"error\":\"${e.message}\"}"
        }
    }

    private fun parseTeamData(jsonString: String): List<TeamData> {
        try {
            println("Parsing JSON response: $jsonString")

            // For now, return empty list until we see the actual JSON structure
            return emptyList()

        } catch (e: Exception) {
            println("Error parsing response: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
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
        val winRate = team.won.toDouble() / team.played
        val drawRate = team.drawn.toDouble() / team.played

        return Form(
            last5 = List(5) {
                when {
                    Math.random() < winRate -> "W"
                    Math.random() < winRate + drawRate -> "D"
                    else -> "L"
                }
            }
        )
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