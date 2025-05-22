package org.helo.mew.model

/**
 * 学习单元
 */
data class ClassUnit(
    val id: String,
    val name: String,
    val lessons: List<Lesson>,
    val grade: Grade,
    val subject: Subject
)
