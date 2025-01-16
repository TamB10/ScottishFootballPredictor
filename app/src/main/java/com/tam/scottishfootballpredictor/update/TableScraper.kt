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
                    println("Starting to scrape $leagueName from $url")
                    val response = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(30000) // Increased timeout
                        .ignoreHttpErrors(true) // Add this to see any HTTP errors
                        .execute()

                    println("HTTP Status Code for $leagueName: ${response.statusCode()}")
                    println("HTTP Status Message: ${response.statusMessage()}")

                    val doc = response.parse()
                    println("HTML Content Length: ${doc.html().length}")
                    println("First 500 chars of HTML: ${doc.html().take(500)}")

                    val teamsData = scrapeTeamData(doc)
                    println("Teams found for $leagueName: ${teamsData.size}")
                    teamsData
                } catch (e: Exception) {
                    println("Error scraping $leagueName")
                    e.printStackTrace()
                    emptyList()
                }
            }

            // Check if any data was scraped
            val totalTeams = leagueData.values.sumOf { it.size }
            println("Total teams scraped across all leagues: $totalTeams")

            return formatToJson(leagueData)
        } catch (e: Exception) {
            println("Fatal error in scraping")
            e.printStackTrace()
            return "{\"version\":\"1.0.0\",\"lastUpdated\":\"${LocalDate.now()}\",\"leagues\":{},\"error\":\"${e.message}\"}"
        }
    }

    private fun scrapeTeamData(doc: Document): List<TeamData> {
        println("\nStarting to parse document")

        val tableRows = doc.select("table tr")
        println("Found ${tableRows.size} rows in total")

        return tableRows.drop(1).mapNotNull { row -> // drop header row
            try {
                val cells = row.select("td")
                if (cells.size < 15) {
                    println("Skipping row - insufficient cells (${cells.size})")
                    return@mapNotNull null
                }

                // Mapping based on actual table structure:
                // Position | Team | HOME (P W D L GF GA) | AWAY (W D L GF GA) | GD | PTS
                val position = cells[0].text().toIntOrNull() ?: 0
                val name = cells[1].text()

                // Home stats
                val homePlayed = cells[2].text().toIntOrNull() ?: 0
                val homeWins = cells[3].text().toIntOrNull() ?: 0
                val homeDraws = cells[4].text().toIntOrNull() ?: 0
                val homeLosses = cells[5].text().toIntOrNull() ?: 0
                val homeGoalsFor = cells[6].text().toIntOrNull() ?: 0
                val homeGoalsAgainst = cells[7].text().toIntOrNull() ?: 0

                // Away stats
                val awayWins = cells[8].text().toIntOrNull() ?: 0
                val awayDraws = cells[9].text().toIntOrNull() ?: 0
                val awayLosses = cells[10].text().toIntOrNull() ?: 0
                val awayGoalsFor = cells[11].text().toIntOrNull() ?: 0
                val awayGoalsAgainst = cells[12].text().toIntOrNull() ?: 0

                // Total stats
                val goalDiff = cells[13].text().toIntOrNull() ?: 0
                val points = cells[14].text().toIntOrNull() ?: 0

                println("Processing team: $name (Pos: $position, Pts: $points)")

                TeamData(
                    name = name,
                    position = position,
                    played = homePlayed + (awayWins + awayDraws + awayLosses),
                    won = homeWins + awayWins,
                    drawn = homeDraws + awayDraws,
                    lost = homeLosses + awayLosses,
                    goalsFor = homeGoalsFor + awayGoalsFor,
                    goalsAgainst = homeGoalsAgainst + awayGoalsAgainst,
                    cleanSheets = 0  // We'll need to calculate this separately if needed
                )
            } catch (e: Exception) {
                println("Error parsing row: ${e.message}")
                println("Row HTML: ${row.html()}")
                e.printStackTrace()
                null
            }
        }.also {
            println("Successfully parsed ${it.size} teams")
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