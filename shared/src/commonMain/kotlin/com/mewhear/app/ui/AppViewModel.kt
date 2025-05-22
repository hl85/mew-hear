package com.mewhear.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mewhear.app.model.DictationRecord // Ensure this is imported
import com.mewhear.app.model.Lesson
import com.mewhear.app.model.Textbook
import com.mewhear.app.model.Word
import com.mewhear.app.service.DictationService
// For state management in a Compose-friendly way, if not using a KMP ViewModel library yet:
// You might need to add a KMP state management library or use platform-specific observable properties.
// For now, let's keep it simple and assume direct state updates or simple callbacks for UI.
// Or, for commonMain, we can use basic mutable state and manage recomposition manually in Compose UI.
// For a more robust solution, libraries like KMM-ViewModel or MVIKotlin are options.
// Let's use basic mutable properties for now and refine later if needed.

class AppViewModel {
    private val dictationService = DictationService()

    // State for selection
    var availableLanguages: List<String> = emptyList()
        private set
    var availableTextbookNames: List<String> = emptyList()
        private set
    var availableGrades: List<String> = emptyList()
        private set

    var selectedLanguage: String? = null
        private set
    var selectedTextbookName: String? = null
        private set
    var selectedGrade: String? = null
        private set
    
    var selectedTextbook: Textbook? = null
        private set

    // State for dictation
    var currentLessonWords: List<Word> by mutableStateOf(emptyList())
        private set
    var currentWordIndex: Int by mutableStateOf(0)
        private set
    // ... other relevant states

    init {
        loadInitialSelectionData()
    }

    private fun loadInitialSelectionData() {
        availableLanguages = dictationService.getAvailableLanguages()
        // Load others based on default or first language
        if (availableLanguages.isNotEmpty()) {
            selectLanguage(availableLanguages.first())
        }
    }

    fun selectLanguage(language: String) {
        selectedLanguage = language
        // Get all textbook names, then filter them by checking if they have any grades for the selected language.
        availableTextbookNames = dictationService.getAvailableTextbookNames().filter { textbookName ->
            dictationService.getAvailableGrades(textbookName, language).isNotEmpty()
        }

        selectedTextbookName = null
        selectedGrade = null
        availableGrades = emptyList()
        if (availableTextbookNames.isNotEmpty()) {
            selectTextbookName(availableTextbookNames.first())
        }
    }

    fun selectTextbookName(name: String) {
        selectedTextbookName = name
        selectedLanguage?.let { lang ->
            availableGrades = dictationService.getAvailableGrades(name, lang)
            selectedGrade = null
            if (availableGrades.isNotEmpty()) {
                selectGrade(availableGrades.first())
            }
        }
    }

    fun selectGrade(grade: String) {
        selectedGrade = grade
        // Potentially load the textbook details now
        selectedTextbook = dictationService.getTextbook(selectedTextbookName!!, selectedGrade!!, selectedLanguage!!)
    }
    
    fun startDictationForLesson(lesson: Lesson) {
        currentLessonWords = dictationService.startDictation(lesson)
        currentWordIndex = 0
        playCurrentWord()
    }

    fun playCurrentWord() {
        if (currentLessonWords.isNotEmpty() && currentWordIndex < currentLessonWords.size) {
            dictationService.playWord(currentLessonWords[currentWordIndex])
        }
    }
    
    fun nextWord() {
        if (currentWordIndex < currentLessonWords.size - 1) {
            currentWordIndex++
            playCurrentWord()
        } else {
            // Lesson finished
        }
    }
    // Add other methods for UI interaction, e.g., handling word marking, reports

    var latestDictationRecord: DictationRecord? by mutableStateOf(null) // To hold the report
        private set

    fun finalizeDictation(lessonId: String, markedCorrectWords: List<Word>, startTime: Long, endTime: Long) {
        // currentLessonWords holds the original list of words for the dictation
        latestDictationRecord = dictationService.completeDictationSession(
            lessonId = lessonId,
            originalWords = currentLessonWords,
            markedCorrectWords = markedCorrectWords,
            startTimeMillis = startTime, // These times should be tracked more accurately
            endTimeMillis = endTime
        )
        // Optionally, add incorrect words to common error list (already handled by service)
        // commonErrorWords.addAll(latestDictationRecord.incorrectWords) // Or similar logic
    }
}
