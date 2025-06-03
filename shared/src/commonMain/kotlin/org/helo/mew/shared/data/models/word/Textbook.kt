package org.helo.mew.data.models.word

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 教材实体
 */
@Serializable
data class Textbook(
    val id: String,
    val name: String,
    val publisher: String,
    val grade: Grade,
    val version: TextbookVersion,
    val description: String? = null,
    val coverImage: String? = null,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /**
     * 获取教材完整名称
     */
    fun getFullName(): String = "${grade.displayName} ${version.displayName} - $name ($publisher)"

    /**
     * 检查是否适用于指定年级
     */
    fun isApplicableForGrade(targetGrade: Grade): Boolean = grade == targetGrade

    /**
     * 检查教材是否有效
     */
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                publisher.isNotBlank() &&
                version != TextbookVersion.UNKNOWN &&
                grade != Grade.UNKNOWN
    }
}