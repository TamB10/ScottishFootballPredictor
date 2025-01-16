package com.tam.scottishfootballpredictor.update

import java.io.File

object StatsScraper {
    @JvmStatic
    fun main(args: Array<String>) {
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

            // Write empty JSON on error
            val statsFile = File("stats/stats.json")
            statsFile.parentFile?.mkdirs()
            statsFile.writeText("{\"version\":\"1.0.0\",\"lastUpdated\":\"${java.time.LocalDate.now()}\",\"leagues\":{}}")
        }
    }
}