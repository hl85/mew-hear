package org.helo.mew.data.models.word

import kotlinx.datetime.Instant

/**
 * 单元实体
 */
data class Unit(
    val id: String,
    val textbookId: String,
    val name: String,
    val orderIndex: Int,
    val description: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /**
     * 检查单元是否有效
     */
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                textbookId.isNotBlank() &&
                orderIndex >= 0
    }

    /**
     * 获取单元显示名称
     */
    fun getDisplayName(): String = "第${orderIndex}单元 $name"
}
