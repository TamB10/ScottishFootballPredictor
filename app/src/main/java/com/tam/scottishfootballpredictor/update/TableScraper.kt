package com.tam.scottishfootballpredictor.update

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import com.google.gson.GsonBuilder

class TableScraper {
    private val leagues = mapOf(
        "Premiership" to "https://spfl.co.uk/league/premiership/table",
        "Championship" to "https://spfl.co.uk/league/championship/table",
        "League 1" to "https://spfl.co.uk/league/league-one/table",
        "League 2" to "https://spfl.co.uk/league/league-two/table"
    )

    fun scrapeAndGenerateJson(): String {
        try {
            val leagueData = leagues.mapValues { (leagueName, url) ->
                try {
                    println("Attempting to scrape $leagueName from $url")

                    val doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(10000)
                        .get()

                    println("Got response for $leagueName. Document length: ${doc.html().length}")

                    scrapeTeamData(doc)
                } catch (e: Exception) {
                    println("Error scraping $leagueName: ${e.message}")
                    e.printStackTrace()
                    emptyList()
                }
            }

            println("Finished scraping all leagues")
            return formatToJson(leagueData)
        } catch (e: Exception) {
            println("Fatal error in scraping: ${e.message}")
            e.printStackTrace()
            return "{\"version\":\"1.0.0\",\"lastUpdated\":\"${LocalDate.now()}\",\"leagues\":{},\"error\":\"${e.message}\"}"
        }
    }

    private fun scrapeTeamData(doc: Document): List<TeamData> {
        val table = doc.select("table.league-table")
        if (table.isEmpty()) {
            println("Table not found. Available classes: ${doc.select("table").map { it.className() }}")
            return emptyList()
        }

        val rows = table.select("tbody tr")
        println("Found ${rows.size} team rows")

        return rows.mapNotNull { row ->
            try {
                TeamData(
                    name = row.select("td.team-name").text().also { println("Team name: $it") },
                    position = row.select("td.position").text().toIntOrNull() ?: 0,
                    played = row.select("td.played").text().toIntOrNull() ?: 0,
                    won = row.select("td.won").text().toIntOrNull() ?: 0,
                    drawn = row.select("td.drawn").text().toIntOrNull() ?: 0,
                    lost = row.select("td.lost").text().toIntOrNull() ?: 0,
                    goalsFor = row.select("td.goals-for").text().toIntOrNull() ?: 0,
                    goalsAgainst = row.select("td.goals-against").text().toIntOrNull() ?: 0,
                    cleanSheets = row.select("td.clean-sheets").text().toIntOrNull() ?: 0
                )
            } catch (e: Exception) {
                println("Error parsing row: ${e.message}")
                println("Row HTML: ${row.html()}")
                null
            }
        }
    }

    private fun formatToJson(leagueData: Map<String, List<TeamData>>): String {
        val statsUpdate = StatsUpdate(
            version = "1.0.${LocalDate.now()}",
            lastUpdated = LocalDate.now().toString(),
            leagues = leagueData.mapValues { (leagueName, teams) ->
                println("Formatting league: $leagueName with ${teams.size} teams")
                LeagueData(
                    teams = teams.associate { team ->
                        println("Processing team: ${team.name}")
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

        val json = GsonBuilder().setPrettyPrinting().create().toJson(statsUpdate)
        println("Generated JSON: $json")
        return json
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