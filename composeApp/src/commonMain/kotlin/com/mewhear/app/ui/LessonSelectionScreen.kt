package com.mewhear.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Button // Added for back button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mewhear.app.model.Lesson
import com.mewhear.app.model.Textbook // Not directly used in function args, but good for context
import com.mewhear.app.model.UnitText

@Composable
fun LessonSelectionScreen(
    viewModel: AppViewModel, // Contains the selectedTextbook
    onLessonSelected: (Lesson) -> Unit,
    // Optional: onBack: () -> Unit // To navigate back to selection screen
) {
    val textbook = viewModel.selectedTextbook

    if (textbook == null) {
        Text("Error: No textbook selected. Please go back.")
        // Add a button to go back if onBack is provided
        // For example, if you add onBack: () -> Unit to parameters:
        // Button(onClick = onBack) { Text("Go Back") }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "${textbook.name} - ${textbook.grade} (${textbook.language})",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            textbook.units.forEach { unit -> // Using forEach as per prompt, though items {} is more idiomatic for LazyColumn
                item { // This makes each unit a single scrollable item.
                    UnitItem(unit, onLessonSelected)
                }
            }
        }
        // Optional: Add a back button here
        // Button(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Back") }
    }
}

@Composable
fun UnitItem(unit: UnitText, onLessonSelected: (Lesson) -> Unit) {
    Card(elevation = 4.dp) {
        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
            Text(unit.name, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            unit.lessons.forEach { lesson ->
                Text(
                    text = lesson.name,
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLessonSelected(lesson) }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
        }
    }
}
