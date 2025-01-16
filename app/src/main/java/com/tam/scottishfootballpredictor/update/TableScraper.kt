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
                    println("\n\nScraping $leagueName from $url")

                    val doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(30000)
                        .get()

                    println("Got page HTML. Looking for tables...")
                    println("Found ${doc.select("table").size} tables on page")

                    // Print the actual HTML of the first table we find
                    doc.select("table").firstOrNull()?.let {
                        println("\nFirst table HTML:\n${it.html()}")
                    }

                    // Print all table selectors we can find
                    println("\nAll table selectors found:")
                    doc.select("table").forEach { table ->
                        println("Table class: ${table.className()}")
                        println("Table id: ${table.id()}")
                    }

                    val teams = scrapeTeamData(doc)
                    println("\nScraped ${teams.size} teams from $leagueName")
                    teams

                } catch (e: Exception) {
                    println("\nError scraping $leagueName: ${e.message}")
                    e.printStackTrace()
                    emptyList()
                }
            }

            // Print what we're about to convert to JSON
            println("\nPreparing to create JSON for leagues:")
            leagueData.forEach { (league, teams) ->
                println("$league: ${teams.size} teams")
                teams.forEach { team ->
                    println("  - ${team.name}: pos ${team.position}, played ${team.played}")
                }
            }

            val json = formatToJson(leagueData)
            println("\nGenerated JSON length: ${json.length}")
            println("First 500 chars of JSON: ${json.take(500)}")

            return json

        } catch (e: Exception) {
            println("\nFatal error in scraping: ${e.message}")
            e.printStackTrace()
            return "{\"version\":\"1.0.0\",\"lastUpdated\":\"${LocalDate.now()}\",\"leagues\":{},\"error\":\"${e.message}\"}"
        }
    }

    private fun scrapeTeamData(doc: Document): List<TeamData> {
        println("\nStarting to parse document for team data")

        // Try multiple table selectors
        val tables = doc.select("table, .league-table, .standings")
        println("Found ${tables.size} potential tables")

        val tableToUse = tables.firstOrNull { table ->
            // Look for a table that has the expected column structure
            val headerRow = table.select("tr").firstOrNull()
            val headerCells = headerRow?.select("th")?.size ?: 0
            println("Table with ${headerCells} header cells found")
            headerCells >= 13  // We expect at least 13 columns for a valid table
        }

        if (tableToUse == null) {
            println("No suitable table found!")
            return emptyList()
        }

        println("Found suitable table, processing rows...")
        val rows = tableToUse.select("tr:not(:first-child)") // Skip header row
        println("Processing ${rows.size} team rows")

        return rows.mapNotNull { row ->
            try {
                val cells = row.select("td")
                if (cells.size < 15) {
                    println("Skipping row - insufficient cells (${cells.size})")
                    return@mapNotNull null
                }

                // Mapping based on actual table structure
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