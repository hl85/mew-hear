package com.mewhear.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle // For correct
import androidx.compose.material.icons.filled.Close // For incorrect (or RadioButtonUnchecked)
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mewhear.app.model.Word

@Composable
fun PostDictationReviewScreen(
    viewModel: AppViewModel,
    onMarkingFinished: () -> Unit
) {
    val wordsToReview = viewModel.currentLessonWords
    // Keep track of which words are marked correct. Initialize all as incorrect or based on some logic.
    // For simplicity, let's default to all being correct and let user unmark. Or start all as unmarked.
    val markedCorrectStates = remember { mutableStateMapOf<String, Boolean>().apply {
        wordsToReview.forEach { put(it.id, false) } // Start all as incorrect
    } }
    
    val startTime = remember { System.currentTimeMillis() } // Placeholder for actual start time

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mark Your Answers", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(wordsToReview, key = { it.id }) { word ->
                WordMarkingItem(
                    word = word,
                    isCorrect = markedCorrectStates[word.id] ?: false,
                    onToggleCorrect = {
                        markedCorrectStates[word.id] = !(markedCorrectStates[word.id] ?: false)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val correctWords = wordsToReview.filter { markedCorrectStates[it.id] == true }
                // In a real app, startTime and endTime should be tracked more accurately by ViewModel
                // For now, we use a placeholder for startTime and current time for endTime
                val endTime = System.currentTimeMillis() 
                // The lessonId should ideally come from the current lesson object in ViewModel
                val lessonId = viewModel.currentLessonWords.firstOrNull()?.let { word ->
                    viewModel.selectedTextbook?.units?.flatMap { it.lessons }?.firstOrNull { it.words.contains(word) }?.id
                } ?: "unknown_lesson"

                viewModel.finalizeDictation(lessonId, correctWords, startTime, endTime)
                onMarkingFinished()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Report")
        }
    }
}

@Composable
fun WordMarkingItem(
    word: Word,
    isCorrect: Boolean,
    onToggleCorrect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleCorrect)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(word.text, style = MaterialTheme.typography.body1, modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Close, // Or RadioButtonUnchecked
            contentDescription = if (isCorrect) "Correct" else "Incorrect",
            tint = if (isCorrect) Color.Green else Color.Red
        )
    }
}
