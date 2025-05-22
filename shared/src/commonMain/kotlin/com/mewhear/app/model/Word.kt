package com.mewhear.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val id: String,
    val text: String,
    val language: String, // "EN" or "ZH"
    // TODO: Add other relevant fields like pronunciation, definition if needed for future
)
