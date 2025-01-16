package com.tam.scottishfootballpredictor.update

import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.time.LocalDate
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

class TableScraper {
    private val leagues = mapOf(
        "Premiership" to "https://spfl.co.uk/spfl/league/premiership/table",
        "Championship" to "https://spfl.co.uk/spfl/league/championship/table",
        "League 1" to "https://spfl.co.uk/spfl/league/league-one/table",
        "League 2" to "https://spfl.co.uk/spfl/league/league-two/table"
    )

    fun scrapeAndGenerateJson(): String {
        var driver: WebDriver? = null
        try {
            println("Starting scraping process...")
            val leagueData = mutableMapOf<String, List<TeamData>>()

            val options = ChromeOptions().apply {
                addArguments("--headless")
                addArguments("--no-sandbox")
                addArguments("--disable-dev-shm-usage")
                addArguments("--disable-gpu")
                addArguments("--window-size=1920,1080")
            }

            driver = ChromeDriver(options)
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)

            leagues.forEach { (leagueName, url) ->
                try {
                    println("\nFetching $leagueName from $url")
                    driver.get(url)

                    // Take screenshot for debugging
                    (driver as? TakesScreenshot)?.let {
                        val screenshot = it.getScreenshotAs(OutputType.BYTES)
                        println("Screenshot taken, size: ${screenshot.size} bytes")
                    }

                    val teams = scrapeTeamData(driver)
                    println("Found ${teams.size} teams for $leagueName")
                    if (teams.isNotEmpty()) {
                        leagueData[leagueName] = teams
                    }
                } catch (e: Exception) {
                    println("Error fetching $leagueName: ${e.message}")
                    e.printStackTrace()
                }
            }

            return formatToJson(leagueData)
        } catch (e: Exception) {
            println("Fatal error: ${e.message}")
            e.printStackTrace()
            return "{\"version\":\"1.0.0\",\"lastUpdated\":\"${LocalDate.now()}\",\"leagues\":{}}"
        } finally {
            driver?.quit()
        }
    }

    private fun scrapeTeamData(driver: WebDriver): List<TeamData> {
        println("Starting to parse table data")

        // Wait for table to load
        Thread.sleep(5000)  // Give more time for JavaScript to load

        // Print page source to debug
        println("Page source: ${driver.pageSource}")

        // Try multiple table selectors
        val rows = try {
            driver.findElements(By.cssSelector("table tbody tr")).also {
                println("Found ${it.size} rows using table tbody tr")
            }
        } catch (e: Exception) {
            println("Error finding rows with first selector: ${e.message}")
            try {
                driver.findElements(By.cssSelector(".league-table tr")).also {
                    println("Found ${it.size} rows using .league-table tr")
                }
            } catch (e: Exception) {
                println("Error finding rows with second selector: ${e.message}")
                emptyList()
            }
        }

        println("Processing ${rows.size} rows")

        return rows.mapNotNull { row ->
            try {
                val cells = row.findElements(By.tagName("td"))
                println("Processing row with ${cells.size} cells")
                cells.forEach { cell ->
                    println("Cell content: ${cell.text}")
                }

                if (cells.size >= 9) {
                    TeamData(
                        name = cells[0].text,
                        position = cells[0].text.toIntOrNull() ?: 0,
                        played = cells[2].text.toIntOrNull() ?: 0,
                        won = cells[3].text.toIntOrNull() ?: 0,
                        drawn = cells[4].text.toIntOrNull() ?: 0,
                        lost = cells[5].text.toIntOrNull() ?: 0,
                        goalsFor = cells[6].text.toIntOrNull() ?: 0,
                        goalsAgainst = cells[7].text.toIntOrNull() ?: 0,
                        cleanSheets = cells[8].text.toIntOrNull() ?: 0
                    ).also { team ->
                        println("Successfully parsed team: $team")
                    }
                } else {
                    println("Skipping row - insufficient cells")
                    null
                }
            } catch (e: Exception) {
                println("Error processing row: ${e.message}")
                null
            }
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

        println("\nGenerating JSON output")
        val json = GsonBuilder().setPrettyPrinting().create().toJson(statsUpdate)
        println("JSON length: ${json.length}")
        return json
    }

    private fun calculateForm(team: TeamData): Form {
        val winRate = team.won.toDouble() / team.played.coerceAtLeast(1)
        val drawRate = team.drawn.toDouble() / team.played.coerceAtLeast(1)

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