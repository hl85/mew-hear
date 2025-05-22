package com.mewhear.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val id: String,
    val name: String,
    val words: List<Word>
    // TODO: Add more fields here if needed in future
)
