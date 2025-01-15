package com.tam.scottishfootballpredictor.model

import com.tam.scottishfootballpredictor.model.MatchResult
import com.tam.scottishfootballpredictor.model.TeamRecord
import com.tam.scottishfootballpredictor.model.VenueStats

data class LeagueStats(
    val league: String,
    val homeTeam: String,
    val awayTeam: String,
    val headToHead: HeadToHeadStats,
    val venueStats: VenueStats
)

data class HeadToHeadStats(
    val totalMatches: Int,
    val homeWins: Int,
    val draws: Int,
    val awayWins: Int,
    val homeGoals: Int,
    val awayGoals: Int,
    val recentMatches: List<MatchResult>,
    val homeTeamRecord: TeamRecord,
    val awayTeamRecord: TeamRecord
)