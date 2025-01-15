package com.tam.scottishfootballpredictor.model

import kotlin.random.Random
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.exp
//import kotlin.math.pow

import com.tam.scottishfootballpredictor.model.VenueStats
import com.tam.scottishfootballpredictor.model.TeamRecord
import com.tam.scottishfootballpredictor.model.MatchResult

class ScottishPredictor {
    private val statsCalculator = StatsCalculator()
    private val teamStats = mutableMapOf<String, TeamStats>()
    private val leagueStats = mutableMapOf<String, MutableMap<String, MutableMap<String, LeagueStats>>>()

    val leagues = mapOf(
        "Premiership" to League(
            teams = listOf(
                "Celtic", "Rangers", "Dundee United", "Aberdeen",
                "Motherwell", "Hibernian", "St. Mirren", "Dundee",
                "Kilmarnock", "Ross County", "Hearts", "St. Johnstone"
            ),
            avgGoals = 2.8,
            maxHome = 5,
            maxAway = 4,
            strengthModifier = 1.0,
            homeAdvantage = 1.35,
            teamModifiers = mapOf(
                "Celtic" to 1.4,
                "Rangers" to 1.35,
                "Hearts" to 1.2,
                "Aberdeen" to 1.15,
                "Hibernian" to 1.15,
                "Motherwell" to 1.1,
                "St. Mirren" to 1.1,
                "Kilmarnock" to 1.05,
                "St. Johnstone" to 1.05,
                "Ross County" to 1.0,
                "Dundee" to 1.0,
                "Dundee United" to 1.0
            )
        ),
        "Championship" to League(
            teams = listOf(
                "Falkirk", "Ayr United", "Livingston", "Partick Thistle",
                "Queen's Park", "Raith Rovers", "Greenock Morton",
                "Hamilton Academical", "Dunfermline Athletic", "Airdrieonians"
            ),
            avgGoals = 2.5,
            maxHome = 4,
            maxAway = 3,
            strengthModifier = 0.85,
            homeAdvantage = 1.3,
            teamModifiers = mapOf(
                "Falkirk" to 1.25,
                "Ayr United" to 1.2,
                "Partick Thistle" to 1.15,
                "Raith Rovers" to 1.15,
                "Queen's Park" to 1.1,
                "Dunfermline Athletic" to 1.1,
                "Hamilton Academical" to 1.05,
                "Greenock Morton" to 1.05,
                "Airdrieonians" to 1.0,
                "Livingston" to 1.0
            )
        ),
        "League One" to League(
            teams = listOf(
                "Arbroath", "Stenhousemuir", "Kelty Hearts", "Alloa Athletic",
                "Cove Rangers", "Queen of the South", "Montrose",
                "Annan Athletic", "Inverness CT", "Dumbarton"
            ),
            avgGoals = 2.3,
            maxHome = 3,
            maxAway = 3,
            strengthModifier = 0.75,
            homeAdvantage = 1.25,
            teamModifiers = mapOf(
                "Arbroath" to 1.2,
                "Inverness CT" to 1.15,
                "Queen of the South" to 1.15,
                "Alloa Athletic" to 1.1,
                "Kelty Hearts" to 1.1,
                "Montrose" to 1.05,
                "Cove Rangers" to 1.05,
                "Stenhousemuir" to 1.0,
                "Dumbarton" to 1.0,
                "Annan Athletic" to 1.0
            )
        ),
        "League Two" to League(
            teams = listOf(
                "East Fife", "Peterhead", "Elgin City", "Edinburgh City",
                "Stirling Albion", "Spartans", "Clyde", "Stranraer",
                "Bonnyrigg Rose", "Forfar Athletic"
            ),
            avgGoals = 2.2,
            maxHome = 3,
            maxAway = 2,
            strengthModifier = 0.65,
            homeAdvantage = 1.2,
            teamModifiers = mapOf(
                "East Fife" to 1.15,
                "Peterhead" to 1.15,
                "Stirling Albion" to 1.1,
                "Forfar Athletic" to 1.1,
                "Edinburgh City" to 1.05,
                "Spartans" to 1.05,
                "Clyde" to 1.0,
                "Elgin City" to 1.0,
                "Stranraer" to 1.0,
                "Bonnyrigg Rose" to 1.0
            )
        )
    )

    init {
        generateTeamStats()
        generateLeagueStats()
    }
    private fun generateLeagueStats() {
        leagues.forEach { (leagueName, league) ->
            leagueStats[leagueName] = mutableMapOf()
            league.teams.forEach { homeTeam ->
                leagueStats[leagueName]!![homeTeam] = mutableMapOf()
                league.teams.forEach { awayTeam ->
                    if (homeTeam != awayTeam) {
                        // Generate reasonable number of matches
                        val totalMatches = Random.nextInt(8, 16)

                        // Ensure homeWins is within valid range
                        val maxHomeWins = totalMatches
                        val homeWins = Random.nextInt(0, maxHomeWins + 1)

                        // Ensure awayWins is within valid range
                        val remainingMatches = totalMatches - homeWins
                        val awayWins = Random.nextInt(0, remainingMatches + 1)

                        // Calculate draws from remaining matches
                        val draws = totalMatches - homeWins - awayWins

                        // Generate realistic goals (at least 1 goal per win)
                        val homeGoals = homeWins + Random.nextInt(0, homeWins * 2 + 1)
                        val awayGoals = awayWins + Random.nextInt(0, awayWins * 2 + 1)

                        // Generate recent matches
                        val recentMatches = List(5) {
                            val homeScore = Random.nextInt(0, 4)
                            val awayScore = Random.nextInt(0, 3)
                            MatchResult(
                                homeScore = homeScore,
                                awayScore = awayScore,
                                date = "2023-${Random.nextInt(1, 13)}-${Random.nextInt(1, 29)}",
                                attendance = when (homeTeam) {
                                    "Celtic", "Rangers" -> Random.nextInt(45000, 60000)
                                    "Hearts", "Hibernian", "Aberdeen" -> Random.nextInt(15000, 25000)
                                    else -> Random.nextInt(5000, 15000)
                                }
                            )
                        }

                        val homeTeamRecord = TeamRecord(
                            wins = homeWins,
                            draws = draws,
                            losses = awayWins,
                            goalsScored = homeGoals,
                            goalsConceded = awayGoals,
                            cleanSheets = recentMatches.count { it.awayScore == 0 },
                            winPercentage = (homeWins.toDouble() / totalMatches) * 100,
                            averageGoalsScored = homeGoals.toDouble() / totalMatches,
                            averageGoalsConceded = awayGoals.toDouble() / totalMatches
                        )

                        val awayTeamRecord = TeamRecord(
                            wins = awayWins,
                            draws = draws,
                            losses = homeWins,
                            goalsScored = awayGoals,
                            goalsConceded = homeGoals,
                            cleanSheets = recentMatches.count { it.homeScore == 0 },
                            winPercentage = (awayWins.toDouble() / totalMatches) * 100,
                            averageGoalsScored = awayGoals.toDouble() / totalMatches,
                            averageGoalsConceded = homeGoals.toDouble() / totalMatches
                        )

                        val h2h = HeadToHeadStats(
                            totalMatches = totalMatches,
                            homeWins = homeWins,
                            draws = draws,
                            awayWins = awayWins,
                            homeGoals = homeGoals,
                            awayGoals = awayGoals,
                            recentMatches = recentMatches,
                            homeTeamRecord = homeTeamRecord,
                            awayTeamRecord = awayTeamRecord
                        )

                        val venueStats = VenueStats(
                            venueName = when (homeTeam) {
                                "Celtic" -> "Celtic Park"
                                "Rangers" -> "Ibrox Stadium"
                                "Hearts" -> "Tynecastle"
                                "Hibernian" -> "Easter Road"
                                "Aberdeen" -> "Pittodrie"
                                else -> "${homeTeam} Stadium"
                            },
                            capacity = when (homeTeam) {
                                "Celtic" -> 60411
                                "Rangers" -> 50817
                                "Hearts" -> 19852
                                "Hibernian" -> 20421
                                "Aberdeen" -> 22199
                                else -> 15000
                            },
                            averageAttendance = when (homeTeam) {
                                "Celtic", "Rangers" -> Random.nextInt(45000, 60000)
                                "Hearts", "Hibernian", "Aberdeen" -> Random.nextInt(15000, 25000)
                                else -> Random.nextInt(5000, 15000)
                            },
                            homeWinRate = homeWins.toDouble() / totalMatches,
                            averageGoalsScored = homeGoals.toDouble() / totalMatches,
                            averageGoalsConceded = awayGoals.toDouble() / totalMatches,
                            atmosphereEffect = when (homeTeam) {
                                "Celtic", "Rangers" -> 0.95
                                "Hearts", "Hibernian", "Aberdeen" -> 0.85
                                else -> 0.75
                            }
                        )

                        leagueStats[leagueName]!![homeTeam]!![awayTeam] = LeagueStats(
                            league = leagueName,
                            homeTeam = homeTeam,
                            awayTeam = awayTeam,
                            headToHead = h2h,
                            venueStats = venueStats
                        )
                    }
                }
            }
        }
    }

    private fun generateTeamStats() {
        leagues.forEach { (leagueName, league) ->
            league.teams.forEachIndexed { index, team ->
                val positionStrength = (league.teams.size - index).toDouble() / league.teams.size
                val teamModifier = league.teamModifiers[team] ?: 1.0
                val baseStrength = positionStrength * league.strengthModifier * teamModifier

                val matchesPlayed = Random.nextInt(15, 21)
                val winRatio = baseStrength * Random.nextDouble(0.8, 1.2)
                val wins = (matchesPlayed * winRatio * 0.5).roundToInt()
                val draws = (matchesPlayed * 0.25).roundToInt()
                val losses = matchesPlayed - wins - draws

                val avgGoals = league.avgGoals * baseStrength
                val goalsScored = (avgGoals * matchesPlayed).roundToInt()
                val goalsConceded = (league.avgGoals * (1 - baseStrength) * matchesPlayed).roundToInt()

                // Generate realistic form based on team strength
                val form = List(5) {
                    val chance = Random.nextDouble()
                    when {
                        chance < baseStrength * 0.7 -> 3  // Win
                        chance < baseStrength * 0.7 + 0.2 -> 1  // Draw
                        else -> 0  // Loss
                    }
                }

                teamStats[team] = TeamStats(
                    league = leagueName,
                    strength = baseStrength,
                    matches = matchesPlayed,
                    wins = wins,
                    draws = draws,
                    losses = losses,
                    goalsScored = goalsScored,
                    goalsConceded = goalsConceded,
                    cleanSheets = (matchesPlayed * baseStrength * 0.3).roundToInt(),
                    form = form
                )
            }
        }
    }

    fun predictScore(homeTeam: String, awayTeam: String, league: String): PredictionResult {
        val leagueData = leagues[league] ?: throw IllegalArgumentException("Invalid league")
        val homeStats = teamStats[homeTeam] ?: throw IllegalArgumentException("Invalid home team")
        val awayStats = teamStats[awayTeam] ?: throw IllegalArgumentException("Invalid away team")
        val matchStats = leagueStats[league]?.get(homeTeam)?.get(awayTeam)
            ?: throw IllegalArgumentException("No historical data")

        // Calculate clean sheet probabilities
        val homeCleanSheetProb = homeStats.cleanSheets.toDouble() / homeStats.matches
        val awayCleanSheetProb = awayStats.cleanSheets.toDouble() / awayStats.matches

        // Calculate expected goals
        val homeXg = calculateExpectedGoals(homeStats, awayStats, true)
        val awayXg = calculateExpectedGoals(awayStats, homeStats, false)

        // Generate scores
        val homeGoals = generateScore(homeXg, awayCleanSheetProb)
        val awayGoals = generateScore(awayXg, homeCleanSheetProb)

        return PredictionResult(
            homeGoals = homeGoals,
            awayGoals = awayGoals,
            homeXg = round(homeXg * 100) / 100,
            awayXg = round(awayXg * 100) / 100,
            homeWinProb = round(homeXg / (homeXg + awayXg) * 1000) / 10,
            drawProb = 25.0,
            awayWinProb = round(awayXg / (homeXg + awayXg) * 750) / 10,
            homeForm = calculateFormFactor(homeStats.form),
            awayForm = calculateFormFactor(awayStats.form),
            homeStats = homeStats,
            awayStats = awayStats,
            leagueStats = matchStats
        )
    }
    }
    private fun analyzeScoringPatterns(h2h: HeadToHeadStats): ScoringPatterns {
        val recentMatches = h2h.recentMatches

        // Calculate first/second half goals
        val firstHalfGoals = recentMatches.size
        val secondHalfGoals = recentMatches.size

        // Calculate goal timing patterns
        val earlyGoals = Random.nextInt(0, 3)
        val midGoals = Random.nextInt(1, 4)
        val lateGoals = Random.nextInt(0, 3)

        val cleanSheetStreak = calculateCleanSheetStreak(h2h)
        val scoringStreak = calculateScoringStreak(h2h)

        return ScoringPatterns(
            firstHalfGoals = firstHalfGoals,
            secondHalfGoals = secondHalfGoals,
            earlyGoals = earlyGoals,
            midGoals = midGoals,
            lateGoals = lateGoals,
            cleanSheetStreak = cleanSheetStreak,
            scoringStreak = scoringStreak,
            homeGoalPattern = generateGoalPattern(h2h.homeTeamRecord),
            awayGoalPattern = generateGoalPattern(h2h.awayTeamRecord)
        )
    }

    private fun generateGoalPattern(record: TeamRecord): GoalPattern {
        val firstHalfAvg = record.averageGoalsScored * 0.4
        val secondHalfAvg = record.averageGoalsScored * 0.6

        val goalTimings = mutableListOf<Int>()
        repeat((record.averageGoalsScored * 5).roundToInt()) {
            goalTimings.add(Random.nextInt(1, 91))
        }

        return GoalPattern(
            averageFirstHalfGoals = firstHalfAvg,
            averageSecondHalfGoals = secondHalfAvg,
            goalTimings = goalTimings.sorted(),
            strongestScoringPeriod = when {
                goalTimings.count { it <= 15 } > goalTimings.count { it > 75 } -> "Early (0-15)"
                goalTimings.count { it in 16..75 } >
                        (goalTimings.count { it <= 15 } + goalTimings.count { it > 75 }) -> "Mid-game (16-75)"
                else -> "Late (76-90)"
            },
            cleanSheetProbability = record.cleanSheets.toDouble() /
                    (record.wins + record.draws + record.losses)
        )
    }

    private fun calculateCleanSheetStreak(h2h: HeadToHeadStats): Int {
        var streak = 0
        for (match in h2h.recentMatches.reversed()) {
            if (match.awayScore == 0) streak++ else break
        }
        return streak
    }

    private fun calculateScoringStreak(h2h: HeadToHeadStats): Int {
        var streak = 0
        for (match in h2h.recentMatches.reversed()) {
            if (match.homeScore > 0) streak++ else break
        }
        return streak
    }

    private fun calculateForm(form: List<Int>): Double {
        return form.mapIndexed { index, result ->
            result * (1.0 + index * 0.1)
        }.sum() / (15 * 1.4)
    }

private fun calculateExpectedGoals(team: TeamStats, opponent: TeamStats, isHome: Boolean): Double {
    val baseGoals = if (isHome) 1.4 else 1.1  // Base goals are higher for home team

    // Attack strength using strength property and goals per game
    val attackStrength = (team.strength * team.goalsScored.toDouble() / team.matches)

    // Defense strength (including clean sheets)
    val cleanSheetRatio = opponent.cleanSheets.toDouble() / opponent.matches
    val goalsConceededRatio = opponent.goalsConceded.toDouble() / opponent.matches
    val defenseStrength = (cleanSheetRatio * 0.3 + (1 - goalsConceededRatio/3) * 0.7)

    // Form adjustment
    val formFactor = calculateFormFactor(team.form)

    // Calculate expected goals with team strength modifier
    val xG = baseGoals * attackStrength * (1 - defenseStrength) * formFactor

    // Cap maximum goals based on home/away
    return xG.coerceIn(0.0, if(isHome) 3.5 else 2.5)
}

private fun calculateFormFactor(form: List<Int>): Double {
    var factor = 1.0
    form.forEachIndexed { index, result ->
        val weight = 1.0 + (index * 0.1)  // More recent games count more
        factor *= when(result) {
            3 -> 1.1 * weight  // Win
            1 -> 1.0 * weight  // Draw
            else -> 0.9 * weight  // Loss
        }
    }
    return factor.coerceIn(0.8, 1.2)
}



    private fun generateScore(xg: Double, cleanSheetProbability: Double): Int {
        // Check for clean sheet first
        if (Random.nextDouble() < cleanSheetProbability) {
            return 0
        }

        // Otherwise generate score based on expected goals
        var score = 0
        var probability = exp(-xg)
        var sum = probability
        val random = Random.nextDouble()

        while (random > sum && score < 4) {  // Cap at 4 goals
            score++
            probability *= xg / score
            sum += probability
        }

        return score
    }
