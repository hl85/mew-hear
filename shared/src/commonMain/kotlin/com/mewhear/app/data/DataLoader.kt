package com.mewhear.app.data

import com.mewhear.app.model.Textbook
import kotlinx.serialization.json.Json

// Expected function to read a file from resources
internal expect fun loadJsonFromResources(fileName: String): String?

object DataLoader {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun loadTextbooks(): List<Textbook> {
        val jsonString = loadJsonFromResources("data/textbooks.json")
        return if (jsonString != null) {
            try {
                json.decodeFromString<List<Textbook>>(jsonString)
            } catch (e: Exception) {
                // Log error or handle it appropriately
                println("Error decoding textbooks: ${e.message}")
                emptyList()
            }
        } else {
            // Log error or handle missing file
            println("Error: textbooks.json not found.")
            emptyList()
        }
    }
}
