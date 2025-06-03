package org.helo.mew.data.models.word

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 练习册词汇关联实体
 */
@Serializable
data class WorkbookWord(
    val id: String,
    val workbookId: String,
    val wordId: String? = null, // 可选的词汇ID，如果是自定义词汇则为null
    var text: String,
    val sortOrder: Int,
    val addedAt: Instant
) {
    init {
        require(id.isNotBlank()) { "关联ID不能为空" }
        require(workbookId.isNotBlank()) { "练习册ID不能为空" }
        require(text.isNotBlank()) { "词汇不能为空" }
        require(sortOrder >= 0) { "排序顺序不能为负数" }
    }
}