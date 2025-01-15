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
            val doc = Jsoup.connect(url).get()
            scrapeTeamData(doc)
        }

        return formatToJson(leagueData)
    }

    private fun scrapeTeamData(doc: Document): List<TeamData> {
        return doc.select("table.league-table tbody tr").map { row ->
            TeamData(
                name = row.select("td.team-name").text(),
                position = row.select("td.position").text().toIntOrNull() ?: 0,
                played = row.select("td.played").text().toIntOrNull() ?: 0,
                won = row.select("td.won").text().toIntOrNull() ?: 0,
                drawn = row.select("td.drawn").text().toIntOrNull() ?: 0,
                lost = row.select("td.lost").text().toIntOrNull() ?: 0,
                goalsFor = row.select("td.goals-for").text().toIntOrNull() ?: 0,
                goalsAgainst = row.select("td.goals-against").text().toIntOrNull() ?: 0,
                cleanSheets = row.select("td.clean-sheets").text().toIntOrNull() ?: 0
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
        // This would ideally come from recent matches data
        // For now, generate based on overall performance
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