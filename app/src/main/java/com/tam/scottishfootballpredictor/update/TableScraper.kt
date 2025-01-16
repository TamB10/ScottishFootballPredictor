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

        // Try to find the table first
        val tables = doc.select("table")
        println("Found ${tables.size} tables")
        tables.forEachIndexed { index, table ->
            println("Table $index classes: ${table.classNames()}")
        }

        // Try different selectors
        val tableRows = doc.select("table tr")
        println("Found ${tableRows.size} rows in total")

        // Print structure of first few rows
        tableRows.take(3).forEachIndexed { index, row ->
            println("\nRow $index structure:")
            println("Cells count: ${row.select("td").size}")
            println("Row HTML: ${row.html()}")
        }

        return tableRows.drop(1).mapNotNull { row -> // drop header row
            try {
                val cells = row.select("td")
                if (cells.size < 9) {
                    println("Skipping row - insufficient cells (${cells.size})")
                    return@mapNotNull null
                }

                val teamData = TeamData(
                    name = cells[1].text().also { println("Team name found: $it") },
                    position = cells[0].text().toIntOrNull().also { println("Position: $it") } ?: 0,
                    played = cells[2].text().toIntOrNull().also { println("Played: $it") } ?: 0,
                    won = cells[3].text().toIntOrNull().also { println("Won: $it") } ?: 0,
                    drawn = cells[4].text().toIntOrNull().also { println("Drawn: $it") } ?: 0,
                    lost = cells[5].text().toIntOrNull().also { println("Lost: $it") } ?: 0,
                    goalsFor = cells[6].text().toIntOrNull().also { println("Goals For: $it") } ?: 0,
                    goalsAgainst = cells[7].text().toIntOrNull().also { println("Goals Against: $it") } ?: 0,
                    cleanSheets = cells[8].text().toIntOrNull().also { println("Clean Sheets: $it") } ?: 0
                )
                println("Successfully parsed team: ${teamData.name}")
                teamData
            } catch (e: Exception) {
                println("Error parsing row: ${e.message}")
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