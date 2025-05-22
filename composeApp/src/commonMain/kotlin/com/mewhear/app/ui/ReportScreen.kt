package com.mewhear.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mewhear.app.model.Word
import kotlin.math.roundToInt // For percentage

@Composable
fun ReportScreen(
    viewModel: AppViewModel,
    onDone: () -> Unit
) {
    val record = viewModel.latestDictationRecord

    if (record == null) {
        Text("No report available. Please complete a dictation session.",
            modifier = Modifier.padding(16.dp))
        Button(onClick = onDone, modifier = Modifier.padding(16.dp)) { Text("Back to Start") }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dictation Report", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Lesson ID: ${record.lessonId}") // Or display lesson name if available
        Text("Accuracy: ${(record.accuracy * 100).roundToInt()}% (${record.correctWords.size}/${record.words.size})")
        Text("Time Taken: ${record.timeTakenMillis / 1000} seconds")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Correct Words: ${record.correctWords.size}", style = MaterialTheme.typography.subtitle1)
                WordList(words = record.correctWords, color = Color.Green)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Incorrect Words: ${record.incorrectWords.size}", style = MaterialTheme.typography.subtitle1)
                WordList(words = record.incorrectWords, color = Color.Red)
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes button to bottom

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done (Back to Start)")
        }
    }
}

@Composable
fun WordList(words: List<Word>, color: Color) {
    if (words.isEmpty()){
        Text("- None -", color = color.copy(alpha = 0.7f))
        return
    }
    LazyColumn(modifier = Modifier.heightIn(max = 200.dp).padding(vertical = 8.dp)) { // Limit height
        items(words) { word ->
            Text(word.text, color = color, modifier = Modifier.padding(vertical = 2.dp))
        }
    }
}
