package org.helo.mew.shared.data.entities.user

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 用户实体
 * 代表应用中的用户对象，包含用户的核心业务属性
 */
@Serializable
data class User(
    val id: String,
    val name: String,
    val avatar: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(id.isNotBlank()) { "用户ID不能为空" }
        require(name.isNotBlank()) { "用户名不能为空" }
        require(name.length in 3..20) { "用户名长度必须在3-20个字符之间" }
        require(name.matches(Regex("^[a-zA-Z0-9_]+$"))) { "用户名只能包含字母、数字和下划线" }
    }

    /**
     * 获取用户显示名称
     */
    fun getDisplayName(): String =  name

}