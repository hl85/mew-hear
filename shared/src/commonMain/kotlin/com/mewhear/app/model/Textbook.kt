package com.mewhear.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Textbook(
    val id: String,
    val name: String, // e.g., "人教版", "北师大版"
    val grade: String, // e.g., "K1", "K2"
    val language: String, // "EN" or "ZH"
    val units: List<UnitText>
    // TODO: Add more fields here if needed in future
)
