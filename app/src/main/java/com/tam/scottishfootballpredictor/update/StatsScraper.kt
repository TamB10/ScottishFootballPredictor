package com.tam.scottishfootballpredictor.update

import java.io.File

fun main() {
    println("Starting scraper...")
    try {
        val scraper = TableScraper()
        val json = scraper.scrapeAndGenerateJson()
        println("Generated JSON: $json")

        val statsFile = File("stats/stats.json")
        statsFile.parentFile?.mkdirs()
        statsFile.writeText(json)
        println("Stats written to: ${statsFile.absolutePath}")
    } catch (e: Exception) {
        println("Error running scraper:")
        e.printStackTrace()
    }
}