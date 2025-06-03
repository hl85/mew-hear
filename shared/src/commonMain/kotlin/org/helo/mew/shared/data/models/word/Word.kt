package org.helo.mew.data.models.word

import kotlinx.datetime.Instant

/**
 * 单词实体
 */
data class Word(
    val id: String,
    val lessonId: String,
    val text: String,
    val pinyin: String? = null,       // 中文拼音
    val phonetics: String? = null,    // 英文音标
    val translation: String? = null,  // 翻译
    val definition: String? = null,   // 定义/解释
    val examples: List<String> = emptyList(), // 例句
    val audioUrl: String? = null,     // 音频文件URL
    val imageUrl: String? = null,     // 图片URL（用于图文对照）
    val orderIndex: Int,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /**
     * 检查单词是否有效
     */
    fun isValid(): Boolean {
        return text.isNotBlank() &&
                lessonId.isNotBlank() &&
                orderIndex >= 0
    }

    /**
     * 获取显示文本（包含拼音或音标）
     */
    fun getDisplayText(): String {
        return when {
            pinyin?.isNotBlank() == true -> "$text ($pinyin)"
            phonetics?.isNotBlank() == true -> "$text [$phonetics]"
            else -> text
        }
    }

    /**
     * 检查是否有音频
     */
    fun hasAudio(): Boolean = !audioUrl.isNullOrBlank()

    /**
     * 检查是否有图片
     */
    fun hasImage(): Boolean = !imageUrl.isNullOrBlank()

    /**
     * 检查是否有例句
     */
    fun hasExamples(): Boolean = examples.isNotEmpty()

    /**
     * 添加例句
     */
    fun addExample(example: String): Word {
        return if (example.isNotBlank() && !examples.contains(example)) {
            copy(examples = examples + example)
        } else {
            this
        }
    }

    /**
     * 移除例句
     */
    fun removeExample(example: String): Word {
        return copy(examples = examples - example)
    }


    /**
     * 获取所有相关文本（用于搜索）
     */
    fun getAllSearchableText(): String {
        return listOfNotNull(
            text,
            pinyin,
            phonetics,
            translation,
            definition
        ).plus(examples).joinToString(" ")
    }
}