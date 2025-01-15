package com.tam.scottishfootballpredictor.update

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import com.google.gson.GsonBuilder

class TableScraper {
    private val leagues = mapOf(
        "premiership" to "https://spfl.co.uk/league/premiership/table",
        "championship" to "https://spfl.co.uk/league/championship/table",
        "league1" to "https://spfl.co.uk/league/league-one/table",
        "league2" to "https://spfl.co.uk/league/league-two/table"
    )

    fun scrapeAndGenerateJson(): String {
        val leagueData = leagues.mapValues { (_, url) ->
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .get()
            scrapeTeamData(doc)
        }

        return formatToJson(leagueData)
    }

    private fun scrapeTeamData(doc: Document): List<TeamData> {
        println("Scraping HTML: ${doc.html()}")  // Debug line

        return doc.select("table tbody tr").map { row ->
            println("Processing row: ${row.html()}")  // Debug line

            TeamData(
                name = row.select("td:nth-child(2)").text(),
                position = row.select("td:nth-child(1)").text().toIntOrNull() ?: 0,
                played = row.select("td:nth-child(3)").text().toIntOrNull() ?: 0,
                won = row.select("td:nth-child(4)").text().toIntOrNull() ?: 0,
                drawn = row.select("td:nth-child(5)").text().toIntOrNull() ?: 0,
                lost = row.select("td:nth-child(6)").text().toIntOrNull() ?: 0,
                goalsFor = row.select("td:nth-child(7)").text().toIntOrNull() ?: 0,
                goalsAgainst = row.select("td:nth-child(8)").text().toIntOrNull() ?: 0,
                cleanSheets = row.select("td:nth-child(9)").text().toIntOrNull() ?: 0
            )
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