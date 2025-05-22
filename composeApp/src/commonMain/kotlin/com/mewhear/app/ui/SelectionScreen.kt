package com.mewhear.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.* // Assuming Material Design components
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// This Composable will be in composeApp/src/commonMain/kotlin
// It will use the AppViewModel (which is in shared/src/commonMain/kotlin)
// For this to work, AppViewModel needs to be accessible.
// We might need to adjust dependencies or how ViewModel is provided.
// For now, let's assume it can be instantiated directly or via a simple factory.

@Composable
fun SelectionScreen(
    viewModel: AppViewModel, // Instance passed in or obtained via DI/CompositionLocal
    onTextbookSelected: (com.mewhear.app.model.Textbook) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(viewModel.selectedLanguage) }
    var selectedTextbookName by remember { mutableStateOf(viewModel.selectedTextbookName) }
    var selectedGrade by remember { mutableStateOf(viewModel.selectedGrade) }

    // Observe changes from ViewModel (simplified)
    // In a real app, you'd use StateFlow, LiveData, or similar from ViewModel
    LaunchedEffect(viewModel.selectedLanguage) { selectedLanguage = viewModel.selectedLanguage }
    LaunchedEffect(viewModel.selectedTextbookName) { selectedTextbookName = viewModel.selectedTextbookName }
    LaunchedEffect(viewModel.selectedGrade) { selectedGrade = viewModel.selectedGrade }
    // Re-observe available options when upstream selections change
    LaunchedEffect(selectedLanguage) {
        if (selectedLanguage != null) {
            // This will trigger re-composition for textbook name dropdown
        }
    }
    LaunchedEffect(selectedTextbookName) {
        if (selectedTextbookName != null) {
            // This will trigger re-composition for grade dropdown
        }
    }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Select Your Material", style = MaterialTheme.typography.h5)

        // Language Dropdown
        Dropdown(
            label = "Language",
            selectedValue = selectedLanguage,
            options = viewModel.availableLanguages,
            onSelected = { lang ->
                viewModel.selectLanguage(lang)
                // selectedLanguage will update via LaunchedEffect
                // To ensure dependent dropdowns refresh, we might need to explicitly update their selected values to null
                // if the viewmodel's logic doesn't cascade this.
                // For now, relying on LaunchedEffect on viewModel's properties.
            }
        )

        // Textbook Name Dropdown
        if (selectedLanguage != null) {
            Dropdown(
                label = "Textbook",
                selectedValue = selectedTextbookName,
                options = viewModel.availableTextbookNames, // This list updates when language changes in VM
                onSelected = { name ->
                    viewModel.selectTextbookName(name)
                }
            )
        }

        // Grade Dropdown
        if (selectedTextbookName != null && selectedLanguage != null) { // Also check selectedLanguage to ensure context
            Dropdown(
                label = "Grade",
                selectedValue = selectedGrade,
                options = viewModel.availableGrades, // This list updates when textbook name changes in VM
                onSelected = { grade ->
                    viewModel.selectGrade(grade)
                }
            )
        }
        
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.selectedTextbook?.let { onTextbookSelected(it) }
            },
            enabled = viewModel.selectedTextbook != null
        ) {
            Text("Next")
        }
    }
}

@Composable
fun Dropdown(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selectedValue ?: "Select $label",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                TextButton(onClick = { expanded = true }) { Text("▼") }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onSelected(option)
                    expanded = false
                }) {
                    Text(text = option)
                }
            }
        }
    }
}
