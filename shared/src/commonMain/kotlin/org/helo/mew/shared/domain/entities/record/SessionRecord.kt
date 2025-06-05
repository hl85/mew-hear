package org.helo.mew.shared.domain.entities.record

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 听写会话实体
 */
@Serializable
data class SessionRecord(
    val id: String,
    val userId: String,
    val type: SessionRecordType,
    val sourceId: String,                // 课程ID、练习册ID等
    val sourceName: String,              // 来源名称
    val totalWords: Int,
    val correctWords: Int,
    val accuracy: Float,                 // 正确率
    val totalTime: Long,                 // 总耗时（毫秒）
    val averageTimePerWord: Long,        // 平均每词耗时
    val startedAt: Instant,
    val completedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(id.isNotBlank()) { "听写会话ID不能为空" }
        require(userId.isNotBlank()) { "用户ID不能为空" }
        require(sourceId.isNotBlank()) { "来源ID不能为空" }
        require(sourceName.isNotBlank()) { "来源名称不能为空" }
        require(totalWords >= 0) { "总单词数不能为负数" }
        require(correctWords >= 0) { "正确单词数不能为负数" }
        require(correctWords <= totalWords) { "正确单词数不能超过总单词数" }
        require(accuracy in 0.0f..1.0f) { "正确率必须在0-1之间" }
        require(totalTime >= 0) { "总耗时不能为负数" }
        require(averageTimePerWord >= 0) { "平均耗时不能为负数" }
    }
    
    /**
     * 检查会话是否已完成
     */
    fun isCompleted(): Boolean = completedAt != null
    
    /**
     * 计算错误单词数
     */
    fun getIncorrectWords(): Int = totalWords - correctWords
    
    /**
     * 获取等级评价
     */
    fun getGradeLevel(): String = when {
        accuracy >= 0.95f -> "优秀"
        accuracy >= 0.85f -> "良好"
        accuracy >= 0.70f -> "及格"
        else -> "需要加强"
    }
}

