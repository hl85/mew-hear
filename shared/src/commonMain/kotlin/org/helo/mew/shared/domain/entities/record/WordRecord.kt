package org.helo.mew.shared.data.entities.record

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 单词听写记录实体
 */
@Serializable
data class WordRecord(
    val id: String,
    val sessionId: String,
    val wordId: String,
    val wordText: String,
    val isCorrect: Boolean,
    val timeSpent: Long,                 // 耗时（毫秒）
    val attempts: Int = 1,               // 尝试次数
    val mistakeType: org.helo.mew.shared.data.entities.record.MistakeType? = null, // 错误类型
    val recordedAt: Instant
) {
    init {
        require(id.isNotBlank()) { "听写记录ID不能为空" }
        require(sessionId.isNotBlank()) { "会话ID不能为空" }
        require(wordId.isNotBlank()) { "单词ID不能为空" }
        require(wordText.isNotBlank()) { "单词文本不能为空" }
        require(timeSpent >= 0) { "耗时不能为负数" }
        require(attempts > 0) { "尝试次数必须大于0" }
        require(timeSpent <= 300_000) { "单词耗时不能超过5分钟" }
    }

    /**
     * 检查是否需要复习
     */
    fun needsReview(): Boolean = !isCorrect || attempts > 1 || timeSpent > 10_000
}