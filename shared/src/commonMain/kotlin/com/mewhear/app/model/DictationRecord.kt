package com.mewhear.app.model

import kotlinx.serialization.Serializable

@Serializable
data class DictationRecord(
    val id: String,
    val lessonId: String,
    val words: List<Word>,
    val correctWords: List<Word>,
    val incorrectWords: List<Word>,
    val startTime: Long, // timestamp
    val endTime: Long, // timestamp
    val timeTakenMillis: Long,
    val accuracy: Double // 0.0 to 1.0
    // TODO: Add more fields here if needed in future
)
