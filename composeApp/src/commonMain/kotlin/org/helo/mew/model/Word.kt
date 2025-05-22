package org.helo.mew.model

/**
 * 单词/词语
 */
data class Word(
    val id: String,
    val content: String,
    val audioUrl: String, // 音频地址
    val translation: String = "", // 翻译/解释
    val examples: List<String> = emptyList() // 例句
)
