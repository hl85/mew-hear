package org.helo.mew.model

/**
 * 课程数据类
 */
data class Lesson(
    val id: String,
    val name: String,
    val words: List<Word>,
    val unitId: String
)
