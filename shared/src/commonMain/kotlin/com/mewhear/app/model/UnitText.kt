package com.mewhear.app.model

import kotlinx.serialization.Serializable

@Serializable
data class UnitText(
    val id: String,
    val name: String,
    val lessons: List<Lesson>
    // TODO: Add more fields here if needed in future
)
