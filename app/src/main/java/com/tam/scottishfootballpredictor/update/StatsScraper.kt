package com.tam.scottishfootballpredictor.update

import java.io.File

fun main() {
    val scraper = TableScraper()
    val json = scraper.scrapeAndGenerateJson()

    // Save to stats directory
    File("stats").mkdirs()
    File("stats/stats.json").writeText(json)
}