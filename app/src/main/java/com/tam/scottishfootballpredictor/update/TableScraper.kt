package com.tam.scottishfootballpredictor.update

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import com.google.gson.GsonBuilder

class TableScraper {
    private val leagues = mapOf(
        "Premiership" to "https://www.skysports.com/scottish-premiership-table",
        "Championship" to "https://www.skysports.com/scottish-championship-table",
        "League 1" to "https://www.skysports.com/scottish-league-one-table",
        "League 2" to "https://www.skysports.com/scottish-league-two-table"
    )

    fun scrapeAndGenerateJson(): String {
        try {
            println("Starting scraping process...")
            val leagueData = mutableMapOf<String, List<TeamData>>()

            leagues.forEach { (leagueName, url) ->
                try {
                    println("\nFetching $leagueName from $url")
                    val doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(30000)
                        .get()
                    println("Successfully fetched $leagueName page")

                    val teams = scrapeTeamData(doc)
                    println("Found ${teams.size} teams for $leagueName")
                    if (teams.isNotEmpty()) {
                        leagueData[leagueName] = teams
                    }
                } catch (e: Exception) {
                    println("Error fetching $leagueName: ${e.message}")
                    e.printStackTrace()
                }
            }

            return formatToJson(leagueData)
        } catch (e: Exception) {
            println("Fatal error: ${e.message}")
            e.printStackTrace()
            return "{\"version\":\"1.0.0\",\"lastUpdated\":\"${LocalDate.now()}\",\"leagues\":{}}"
        }
    }

    private fun scrapeTeamData(doc: Document): List<TeamData> {
        println("Starting to parse table data")
        try {
            // First print all table classes we find
            doc.select("table").forEach { table ->
                println("Found table with classes: ${table.classNames()}")
            }

            val rows = doc.select("table.standing-table tbody tr")
            println("Found ${rows.size} rows in standing-table")

            return rows.mapNotNull { row ->
                try {
                    val cells = row.select("td")
                    println("\nProcessing row with ${cells.size} cells")
                    cells.forEachIndexed { index, cell ->
                        println("Cell $index content: '${cell.text()}'")
                    }

                    if (cells.size >= 8) {
                        TeamData(
                            name = cells[1].text(),
                            position = cells[0].text().toIntOrNull() ?: 0,
                            played = cells[2].text().toIntOrNull() ?: 0,
                            won = cells[3].text().toIntOrNull() ?: 0,
                            drawn = cells[4].text().toIntOrNull() ?: 0,
                            lost = cells[5].text().toIntOrNull() ?: 0,
                            goalsFor = cells[6].text().toIntOrNull() ?: 0,
                            goalsAgainst = cells[7].text().toIntOrNull() ?: 0,
                            cleanSheets = 0
                        ).also { team ->
                            println("Successfully parsed team: ${team.name}")
                        }
                    } else {
                        println("Skipping row - insufficient cells")
                        null
                    }
                } catch (e: Exception) {
                    println("Error parsing row: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            println("Error parsing table: ${e.message}")
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

        println("\nGenerating JSON output")
        val json = GsonBuilder().setPrettyPrinting().create().toJson(statsUpdate)
        println("JSON length: ${json.length}")
        return json
    }

    private fun calculateForm(team: TeamData): Form {
        val winRate = team.won.toDouble() / team.played.coerceAtLeast(1)
        val drawRate = team.drawn.toDouble() / team.played.coerceAtLeast(1)

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