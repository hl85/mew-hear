package org.helo.mew.model

/**
 * 教材版本
 */
enum class TextbookVersion {
    PEOPLE_EDUCATION, // 人教版
    BEIJING_NORMAL_UNIVERSITY, // 北师大版
    BEIJING, // 北京版
    SHANGHAI, // 沪教版
    CUSTOM // 自定义/手工录入
}

/**
 * 学科
 */
enum class Subject {
    ENGLISH, // 英语
    CHINESE  // 语文
}

/**
 * 年级
 */
enum class Grade {
    K1, K2, K3, K4, K5, K6, CUSTOM
}

/**
 * 单词实体
 */
data class Word(
    val id: String,
    val content: String, // 单词内容
    val audioUrl: String, // 音频URL（本地或远程）
    val pronunciation: String = "", // 发音指南
    val translation: String = "", // 翻译/释义
    val example: String = "" // 示例
)

/**
 * 课程
 */
data class Lesson(
    val id: String,
    val name: String,
    val words: List<Word>,
    val unitId: String
)

/**
 * 单元
 */
data class ClassUnit(
    val id: String,
    val name: String,
    val lessons: List<Lesson>,
    val grade: Grade,
    val subject: Subject
)

/**
 * 教材
 */
data class Textbook(
    val version: TextbookVersion,
    val grade: Grade,
    val subject: Subject,
    val classUnits: List<ClassUnit>
)
