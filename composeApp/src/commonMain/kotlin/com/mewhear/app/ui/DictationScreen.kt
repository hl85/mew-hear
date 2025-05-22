package com.mewhear.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mewhear.app.model.Word // Not explicitly in function params, but good for context with viewModel

@Composable
fun DictationScreen(
    viewModel: AppViewModel,
    onDictationFinished: () -> Unit
) {
    // AppViewModel's currentWordIndex and currentLessonWords are already mutableStateOf
    // So, direct observation by Compose is enabled.
    // We can use them directly: viewModel.currentWordIndex and viewModel.currentLessonWords
    // rememberUpdatedState is useful if the parameters to the Composable themselves were changing frequently
    // from a non-State source, but here viewModel's internal state is already State-aware.

    val currentWord = viewModel.currentLessonWords.getOrNull(viewModel.currentWordIndex)

    // LaunchedEffect to play the first word when the screen appears or lesson changes
    // It should ideally depend on currentLessonWords to re-trigger if the lesson itself changes.
    LaunchedEffect(viewModel.currentLessonWords) {
        if (viewModel.currentLessonWords.isNotEmpty()) {
            viewModel.playCurrentWord() // This will play the word at currentWordIndex
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (currentWord != null) {
            Text(
                "Listen and Write...", // Or show the word if app rules allow (usually not for dictation)
                style = MaterialTheme.typography.h4
            )
            Text("Word ${viewModel.currentWordIndex + 1} of ${viewModel.currentLessonWords.size}", style = MaterialTheme.typography.caption)
            Spacer(modifier = Modifier.height(32.dp))

            // Hidden word display for debugging or specific modes (example, remove for actual dictation)
            // Text(currentWord.text, style = MaterialTheme.typography.h3)
            // Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { viewModel.playCurrentWord() }) {
                    Text("Replay Word")
                }
                Button(onClick = {
                    if (viewModel.currentWordIndex < viewModel.currentLessonWords.size - 1) {
                        viewModel.nextWord() // This will play the next word
                    } else {
                        // Last word has been processed (or was already the last one)
                        onDictationFinished()
                    }
                }) {
                    Text(if (viewModel.currentWordIndex < viewModel.currentLessonWords.size - 1) "Next Word" else "Finish")
                }
            }
        } else if (viewModel.currentLessonWords.isNotEmpty() && viewModel.currentWordIndex >= viewModel.currentLessonWords.size) {
             // This case means the lesson words are loaded, but index is past the end (i.e., after "Finish" was clicked on last word)
             Text("Lesson Finished!", style = MaterialTheme.typography.h5)
             Spacer(modifier = Modifier.height(16.dp))
             Button(onClick = onDictationFinished) {
                 Text("Go to Mark Answers") // Or whatever the next step is
             }
        } else {
            // This case handles when currentLessonWords is empty (e.g., loading or before selection)
            Text("Loading lesson...", style = MaterialTheme.typography.h5)
            // Or "Get Ready!"
        }
    }
}
