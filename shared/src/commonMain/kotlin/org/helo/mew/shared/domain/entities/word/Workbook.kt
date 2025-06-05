package org.helo.mew.shared.data.entities.word

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 练习册实体
 */
@Serializable
data class Workbook(
    val id: String,
    val name: String,
    val description: String? = null,
    val subject: Subject,
    val type: WorkbookType,
    val isPublic: Boolean = true,        // 是否公开
    val createdBy: String? = null,       // 创建者ID
    val sortOrderByUser: Int = 0,   //用户自己的排序
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(id.isNotBlank()) { "练习册ID不能为空" }
        require(name.isNotBlank()) { "练习册名称不能为空" }
        require(sortOrderByUser >= 0) { "排序顺序不能为负数" }
    }


    /**
     * 检查是否需要用户权限
     */
    fun requiresUserPermission(userId: String?): Boolean =
        !isPublic && createdBy != null && createdBy != userId
}