package org.helo.mew.shared.data.entities.record

/**
 * 错误类型枚举
 */
enum class MistakeType(val displayName: String, val description: String) {
    MEMORY("记错", "初始记忆错误，此类错误通常比较顽固"),
    UNFAMILIAR("不熟", "对该词汇不熟悉，没有认真记过，此类错误通常较易纠正"),
    CARELESS("粗心", "因为粗心导致的错误，需要提高注意力"),;

    companion object {
        fun fromDisplayName(name: String): MistakeType? = values().find { it.displayName == name }
    }
}