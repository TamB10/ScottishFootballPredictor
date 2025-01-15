package com.tam.scottishfootballpredictor.update

class StatsValidator {
    fun validateStats(stats: StatsUpdate): List<String> {
        val errors = mutableListOf<String>()

        stats.leagues.forEach { (leagueName, league) ->
            // Check for duplicate positions
            val positions = league.teams.values.map { it.position }
            if (positions.distinct().size != positions.size) {
                errors.add("$leagueName has duplicate positions")
            }

            league.teams.forEach { (teamName, team) ->
                // Validate basic stats
                with(team.stats) {
                    if (played != wins + draws + losses) {
                        errors.add("$teamName: Played matches don't match W/D/L total")
                    }

                    if (cleanSheets > played) {
                        errors.add("$teamName: Clean sheets cannot exceed games played")
                    }

                    val avgGoalsPerGame = goalsFor.toDouble() / played
                    if (avgGoalsPerGame > 4.0) {
                        errors.add("$teamName: Unusually high scoring rate")
                    }
                }

                // Validate form
                team.form.last5.forEach { result ->
                    if (result !in listOf("W", "D", "L")) {
                        errors.add("$teamName: Invalid form result: $result")
                    }
                }
            }
        }

        return errors
    }

    fun validateTeamData(teamData: TeamData): List<String> {
        val errors = mutableListOf<String>()

        if (teamData.played < 0) errors.add("Games played cannot be negative")
        if (teamData.won < 0) errors.add("Wins cannot be negative")
        if (teamData.drawn < 0) errors.add("Draws cannot be negative")
        if (teamData.lost < 0) errors.add("Losses cannot be negative")
        if (teamData.goalsFor < 0) errors.add("Goals for cannot be negative")
        if (teamData.goalsAgainst < 0) errors.add("Goals against cannot be negative")
        if (teamData.cleanSheets < 0) errors.add("Clean sheets cannot be negative")

        if (teamData.played != teamData.won + teamData.drawn + teamData.lost) {
            errors.add("Games played don't match sum of results")
        }

        if (teamData.cleanSheets > teamData.played) {
            errors.add("Clean sheets cannot exceed games played")
        }

        return errors
    }
}