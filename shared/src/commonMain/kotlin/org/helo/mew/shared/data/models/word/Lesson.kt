package org.helo.mew.data.models.word

import kotlinx.datetime.Instant

/**
 * 课程实体
 */
data class Lesson(
    val id: String,
    val unitId: String,
    val name: String,
    val orderIndex: Int,
    val estimatedDuration: Int = 30, // 预计学习时长（分钟）
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /**
     * 检查课程是否有效
     */
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                unitId.isNotBlank() &&
                orderIndex >= 0 &&
                estimatedDuration > 0
    }

    /**
     * 获取课程显示名称
     */
    fun getDisplayName(): String = "第${orderIndex}课 $name"

}