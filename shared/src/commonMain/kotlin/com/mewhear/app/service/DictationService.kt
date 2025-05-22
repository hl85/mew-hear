package com.mewhear.app.service

import com.mewhear.app.data.DataLoader
import com.mewhear.app.model.Textbook
import com.mewhear.app.model.Lesson
import com.mewhear.app.model.Word
import com.mewhear.app.model.DictationRecord // New import
import com.mewhear.app.platform.TextToSpeechPlayer // New import
import com.mewhear.app.platform.TTSLanguage      // New import
import com.mewhear.app.platform.getTtsPlayer      // New import

class DictationService {
    private val allTextbooks: List<Textbook> by lazy { DataLoader.loadTextbooks() }
    private val ttsPlayer: TextToSpeechPlayer by lazy { getTtsPlayer() } // Get player instance

    // Simple in-memory store for error words for MVP
    private val commonErrorWords = mutableSetOf<Word>()

    fun getAvailableTextbookNames(): List<String> {
        return allTextbooks.map { it.name }.distinct()
    }

    fun getAvailableGrades(textbookName: String, language: String): List<String> {
        return allTextbooks
            .filter { it.name == textbookName && it.language == language }
            .map { it.grade }
            .distinct()
    }
    
    fun getAvailableLanguages(): List<String> {
        return allTextbooks.map { it.language }.distinct()
    }

    fun getTextbook(name: String, grade: String, language: String): Textbook? {
        return allTextbooks.firstOrNull { it.name == name && it.grade == grade && it.language == language }
    }

    // Placeholder for starting a dictation session
    fun startDictation(lesson: Lesson): List<Word> {
        // For now, just returns the words in order.
        // Later, this could include randomization or other logic.
        return lesson.words
    }

    // Function to play a single word
    fun playWord(word: Word) {
        val ttsLang = if (word.language.equals("EN", ignoreCase = true)) TTSLanguage.EN_US else TTSLanguage.ZH_CN
        ttsPlayer.play(word.text, ttsLang)
    }

    fun stopPlayback() {
        ttsPlayer.stop()
    }

    fun completeDictationSession(
        lessonId: String, // Or pass the whole Lesson object
        originalWords: List<Word>,
        markedCorrectWords: List<Word>,
        startTimeMillis: Long,
        endTimeMillis: Long
    ): DictationRecord {
        val incorrectWords = originalWords.filterNot { markedCorrectWords.contains(it) }
        val accuracy = if (originalWords.isNotEmpty()) {
            markedCorrectWords.size.toDouble() / originalWords.size.toDouble()
        } else {
            0.0
        }
        val timeTaken = endTimeMillis - startTimeMillis

        // Add incorrect words to our simple error word store
        commonErrorWords.addAll(incorrectWords)

        return DictationRecord(
            id = "dr_${System.currentTimeMillis()}", // Simple unique ID for now
            lessonId = lessonId,
            words = originalWords,
            correctWords = markedCorrectWords,
            incorrectWords = incorrectWords,
            startTime = startTimeMillis,
            endTime = endTimeMillis,
            timeTakenMillis = timeTaken,
            accuracy = accuracy
        )
    }

    // Function to get the current list of common error words
    fun getCommonErrorWords(): List<Word> {
        return commonErrorWords.toList() // Return a copy
    }

    // Optional: Function to start a dictation session with error words
    fun startErrorWordDictation(): List<Word> {
        // For MVP, just return all known error words.
        // Future: Could apply Ebbinghaus curve logic here for selection and ordering.
        return commonErrorWords.toList().shuffled() // Simple shuffle for variety
    }
    
    // Optional: Function to clear a specific word from error list (e.g., if user gets it right in review)
    fun removeWordFromErrors(word: Word) {
        commonErrorWords.remove(word)
    }
}
