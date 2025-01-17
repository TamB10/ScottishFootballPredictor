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
            println("Writing to: ${statsFile.absolutePath}")
            statsFile.parentFile?.mkdirs()
            statsFile.writeText(json)
            println("Successfully wrote stats to file")

        } catch (e: Exception) {
            println("Error running scraper:")
            e.printStackTrace()
        }
    }
}