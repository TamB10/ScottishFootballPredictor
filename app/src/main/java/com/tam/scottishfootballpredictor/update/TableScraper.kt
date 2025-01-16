package com.tam.scottishfootballpredictor.update

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import com.google.gson.GsonBuilder

class TableScraper {
    private val leagues = mapOf(
        "Premiership" to "https://www.bbc.co.uk/sport/football/scottish-premiership/table",
        "Championship" to "https://www.bbc.co.uk/sport/football/scottish-championship/table",
        "League 1" to "https://www.bbc.co.uk/sport/football/scottish-league-one/table",
        "League 2" to "https://www.bbc.co.uk/sport/football/scottish-league-two/table"
    )

    fun scrapeAndGenerateJson(): String {
        try {
            val leagueData = leagues.mapValues { (leagueName, url) ->
                try {
                    println("Scraping $leagueName from $url")
                    val doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
                        .get()
                    scrapeTeamData(doc)
                } catch (e: Exception) {
                    println("Error scraping $leagueName: ${e.message}")
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

    private fun scrapeTeamData(doc: Document): List<TeamData> {
        println("\n=== Starting to scrape team data ===")
        println("Document HTML: ${doc.html()}")  // See what HTML we're getting

        val table = doc.select("table.gs-o-table")
        println("Found ${table.size} tables with class gs-o-table")

        // Try alternate selectors if main one fails
        if (table.isEmpty()) {
            println("Trying alternate selectors...")
            val allTables = doc.select("table")
            println("Found ${allTables.size} total tables")
            allTables.forEachIndexed { index, t ->
                println("Table $index classes: ${t.classNames()}")
                println("Table $index content: ${t.html()}")
            }
        }

        val rows = table.select("tr:not(:first-child)")  // Skip header row
        println("Found ${rows.size} team rows")

        return rows.mapNotNull { row ->
            try {
                println("\nProcessing row: ${row.html()}")
                val cells = row.select("td")
                println("Found ${cells.size} cells in row")

                cells.forEachIndexed { index, cell ->
                    println("Cell $index content: ${cell.text()}")
                }

                val teamData = TeamData(
                    position = cells.getOrNull(0)?.text()?.toIntOrNull() ?: 0,
                    name = cells.getOrNull(1)?.select("abbr")?.attr("title") ?:
                    cells.getOrNull(1)?.text() ?: "Unknown",
                    played = cells.getOrNull(2)?.text()?.toIntOrNull() ?: 0,
                    won = cells.getOrNull(3)?.text()?.toIntOrNull() ?: 0,
                    drawn = cells.getOrNull(4)?.text()?.toIntOrNull() ?: 0,
                    lost = cells.getOrNull(5)?.text()?.toIntOrNull() ?: 0,
                    goalsFor = cells.getOrNull(6)?.text()?.toIntOrNull() ?: 0,
                    goalsAgainst = cells.getOrNull(7)?.text()?.toIntOrNull() ?: 0,
                    cleanSheets = 0
                )

                println("Created team data: $teamData")
                teamData

            } catch (e: Exception) {
                println("Error processing row: ${e.message}")
                println("Row HTML causing error: ${row.html()}")
                e.printStackTrace()
                null
            }
        }.also { teams ->
            println("\nTotal teams parsed: ${teams.size}")
            teams.forEach { println(it) }
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