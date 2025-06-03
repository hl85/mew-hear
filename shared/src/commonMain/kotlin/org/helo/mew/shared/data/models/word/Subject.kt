package org.helo.mew.data.models.word

/**
 * 学科枚举
 */
enum class Subject(val displayName: String, val code: String) {
    CHINESE("语文", "chinese"),
    ENGLISH("英语", "english");

    companion object {
        fun fromCode(code: String): Subject? {
            return values().find { it.code == code }
        }
    }
}


