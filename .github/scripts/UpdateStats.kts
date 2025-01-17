package com.tam.scottishfootballpredictor.update

import com.tam.scottishfootballpredictor.model.*
import com.tam.scottishfootballpredictor.update.*
import java.io.File
import java.net.URL
import kotlinx.serialization.json.*
import java.time.LocalDateTime

fun fetchStatsFromAPI(apiKey: String): String {
    // Premiership ID: 179
    val url = URL("https://api-football-v1.p.rapidapi.com/v3/standings?league=179&season=2023")
    val connection = url.openConnection()
    connection.setRequestProperty("x-rapidapi-host", "api-football-v1.p.rapidapi.com")
    connection.setRequestProperty("x-rapidapi-key", apiKey)

    return connection.getInputStream().bufferedReader().use { it.readText() }
}

fun processResponse(jsonResponse: String): List<TeamRecord> {
    val json = Json.parseToJsonElement(jsonResponse)
    val standings = json.jsonObject["response"]?.jsonArray?.get(0)
        ?.jsonObject?.get("league")?.jsonObject?.get("standings")
        ?.jsonArray?.get(0)?.jsonArray

    return standings?.map { team ->
        val teamObj = team.jsonObject

        val venueStats = VenueStats(
            homeWins = teamObj["home"]?.jsonObject?.get("win")?.jsonPrimitive?.int ?: 0,
            homeDraws = teamObj["home"]?.jsonObject?.get("draw")?.jsonPrimitive?.int ?: 0,
            homeLosses = teamObj["home"]?.jsonObject?.get("lose")?.jsonPrimitive?.int ?: 0,
            homeGoalsFor = teamObj["home"]?.jsonObject?.get("goals")?.jsonObject?.get("for")?.jsonPrimitive?.int ?: 0,
            homeGoalsAgainst = teamObj["home"]?.jsonObject?.get("goals")?.jsonObject?.get("against")?.jsonPrimitive?.int ?: 0
        )

        TeamRecord(
            teamName = teamObj["team"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "",
            position = teamObj["rank"]?.jsonPrimitive?.int ?: 0,
            played = teamObj["all"]?.jsonObject?.get("played")?.jsonPrimitive?.int ?: 0,
            won = teamObj["all"]?.jsonObject?.get("win")?.jsonPrimitive?.int ?: 0,
            drawn = teamObj["all"]?.jsonObject?.get("draw")?.jsonPrimitive?.int ?: 0,
            lost = teamObj["all"]?.jsonObject?.get("lose")?.jsonPrimitive?.int ?: 0,
            goalsFor = teamObj["all"]?.jsonObject?.get("goals")?.jsonObject?.get("for")?.jsonPrimitive?.int ?: 0,
            goalsAgainst = teamObj["all"]?.jsonObject?.get("goals")?.jsonObject?.get("against")?.jsonPrimitive?.int ?: 0,
            goalDifference = teamObj["goalsDiff"]?.jsonPrimitive?.int ?: 0,
            points = teamObj["points"]?.jsonPrimitive?.int ?: 0,
            form = teamObj["form"]?.jsonPrimitive?.content ?: "",
            league = League.PREMIERSHIP,
            venueStats = venueStats
        )
    } ?: emptyList()
}

fun updatePredictions(teamRecords: List<TeamRecord>) {
    val statsCalculator = StatsCalculator()
    val scottishPredictor = ScottishPredictor()
    val leagueStats = LeagueStats()
    val updateManager = UpdateManager()

    // Update league statistics
    leagueStats.updateStats(League.PREMIERSHIP, teamRecords)

    // Calculate scoring patterns for the league
    val scoringPatterns = statsCalculator.calculateScoringPatterns(teamRecords)

    // Generate predictions for each possible match in the league
    teamRecords.forEach { homeTeam ->
        teamRecords.filter { it != homeTeam }.forEach { awayTeam ->
            val predictionResult = scottishPredictor.predictMatch(
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                leagueStats = leagueStats,
                scoringPatterns = scoringPatterns
            )

            // Store prediction result
            updateManager.storePrediction(predictionResult)
        }
    }
}

fun main() {
    val apiKey = System.getenv("API_KEY") ?: throw IllegalStateException("API_KEY environment variable not set")

    try {
        println("Fetching Premiership stats...")
        val response = fetchStatsFromAPI(apiKey)
        val teamRecords = processResponse(response)

        // Validate the data
        val statsValidator = StatsValidator()
        if (!statsValidator.validateTeamRecords(teamRecords)) {
            throw IllegalStateException("Invalid team records received from API")
        }

        println("Successfully fetched ${teamRecords.size} team records")

        // Update predictions using the collected stats
        updatePredictions(teamRecords)

        println("Successfully updated stats and predictions for Premiership")

    } catch (e: Exception) {
        println("Error updating stats: ${e.message}")
        System.exit(1)
    }
}