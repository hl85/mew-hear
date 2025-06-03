package org.helo.mew.data.models.record

import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * 常错词记录实体
 */
@Serializable
data class CommonMistakeWord(
    val id: String,
    val userId: String,
    val wordId: String,
    val mistakeCount: Int,               // 错误次数
    val totalAttempts: Int,              // 总尝试次数
    val lastMistakeAt: Instant,           // 最后错误时间
    val nextReviewAt: Instant,            // 下次复习时间（基于艾宾浩斯曲线）
    val isResolved: Boolean = false,     // 是否已解决
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(id.isNotBlank()) { "常错词记录ID不能为空" }
        require(userId.isNotBlank()) { "用户ID不能为空" }
        require(wordId.isNotBlank()) { "单词ID不能为空" }
        require(mistakeCount >= 0) { "错误次数不能为负数" }
        require(totalAttempts >= mistakeCount) { "总尝试次数不能少于错误次数" }
    }

    /**
     * 计算错误率
     */
    fun getErrorRate(): Float = if (totalAttempts > 0) mistakeCount.toFloat() / totalAttempts else 0f

    /**
     * 检查是否需要立即复习
     */
    fun needsImmediateReview(): Boolean = !isResolved && Clock.System.now() >= nextReviewAt

    private fun parseDateTime(dateTime: Instant): Long {
        return dateTime.toEpochMilliseconds()
    }
}
