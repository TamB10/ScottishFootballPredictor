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

        // Find the table with team standings
        val tableRows = doc.select("table tr")
        println("Found ${tableRows.size} rows in total")

        return tableRows.drop(1).mapNotNull { row -> // drop header row
            try {
                val cells = row.select("td")
                if (cells.size < 5) {
                    println("Skipping row - insufficient cells (${cells.size})")
                    return@mapNotNull null
                }

                val position = cells[0].text().toIntOrNull() ?: 0
                val name = cells[1].select("strong").text()
                val played = cells[2].text().toIntOrNull() ?: 0
                val goalDiff = cells[3].text().toIntOrNull() ?: 0
                val points = cells[4].text().toIntOrNull() ?: 0

                println("Processing team: $name (Pos: $position, Pld: $played, GD: $goalDiff, Pts: $points)")

                // Since we don't have all stats directly, we'll estimate some
                // Using a placeholder value of 0 for stats we can't determine
                TeamData(
                    name = name,
                    position = position,
                    played = played,
                    won = 0,  // Can't determine directly
                    drawn = 0, // Can't determine directly
                    lost = 0,  // Can't determine directly
                    goalsFor = 0,    // Can't determine without additional data
                    goalsAgainst = 0, // Can't determine without additional data
                    cleanSheets = 0   // Can't determine without additional data
                )
            } catch (e: Exception) {
                println("Error parsing row: ${e.message}")
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