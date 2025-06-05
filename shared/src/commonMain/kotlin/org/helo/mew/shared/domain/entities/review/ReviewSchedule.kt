package org.helo.mew.shared.domain.entities.review

import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * 复习计划实体（艾宾浩斯遗忘曲线）
 * TODO: 重新设计复习计划算法、数据结构和逻辑
 */
@Serializable
data class ReviewSchedule(
    val id: String,
    val userId: String,
    val wordId: String,
    val currentInterval: Int,            // 当前间隔天数
    val easinessFactor: Float = 2.5f,    // 容易因子
    val repetitions: Int = 0,            // 重复次数
    val nextReviewAt: Instant,          // 下次复习日期
    val quality: Int? = null,            // 上次复习质量(0-5)
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(id.isNotBlank()) { "复习计划ID不能为空" }
        require(userId.isNotBlank()) { "用户ID不能为空" }
        require(wordId.isNotBlank()) { "单词ID不能为空" }
        require(currentInterval > 0) { "间隔天数必须大于0" }
        require(easinessFactor >= 1.3f) { "容易因子不能小于1.3" }
        require(repetitions >= 0) { "重复次数不能为负数" }
        quality?.let { require(it in 0..5) { "质量评分必须在0-5之间" } }
    }

    /**
     * 检查是否到了复习时间
     */
    fun isDueForReview(): Boolean = isActive && Clock.System.now() >= nextReviewAt

    private fun parseDateTime(dateTime: String): Long {
        return try {
            kotlinx.datetime.Instant.parse(dateTime).toEpochMilliseconds()
        } catch (e: Exception) {
            0L
        }
    }
}