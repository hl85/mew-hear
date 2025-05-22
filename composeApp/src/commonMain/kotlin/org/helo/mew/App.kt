package org.helo.mew

import androidx.compose.material.MaterialTheme // Changed from material3 to material
import androidx.compose.runtime.*
import com.mewhear.app.model.Textbook // Correct model import
import com.mewhear.app.ui.AppViewModel
import com.mewhear.app.ui.SelectionScreen
import com.mewhear.app.ui.LessonSelectionScreen
import com.mewhear.app.ui.DictationScreen
import com.mewhear.app.ui.PostDictationReviewScreen
import com.mewhear.app.ui.ReportScreen // New import

// Preview annotation might need to be adjusted or removed if it causes issues with MaterialTheme
// For now, keeping it.
@Composable
// @Preview // Preview might not work well with the new structure immediately, can be re-added/configured later.
fun App() {
    MaterialTheme {
        val appViewModel = remember { AppViewModel() }
        var currentScreen by remember { mutableStateOf(Screen.Selection) }
        // selectedTextbookForNav is already declared for passing to LessonSelectionScreen
        // var selectedTextbookForNav by remember { mutableStateOf<Textbook?>(null) } // This line is in existing code, but not in prompt's new App.kt. AppViewModel holds the state.

        when (currentScreen) {
            Screen.Selection -> SelectionScreen(appViewModel) {
                currentScreen = Screen.LessonSelection
            }
            Screen.LessonSelection -> LessonSelectionScreen(appViewModel) { lesson ->
               appViewModel.startDictationForLesson(lesson)
               currentScreen = Screen.Dictation 
            }
            Screen.Dictation -> DictationScreen(appViewModel) {
               currentScreen = Screen.PostDictationReview
            }
            Screen.PostDictationReview -> PostDictationReviewScreen(appViewModel) {
                currentScreen = Screen.Report 
            }
            Screen.Report -> ReportScreen(appViewModel) {
                currentScreen = Screen.Selection // Navigate back to start or lesson selection
            }
        }
    }
}

enum class Screen {
    Selection,
    LessonSelection,
    Dictation,
    PostDictationReview,
    Report // Added
}