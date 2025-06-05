package org.helo.mew.shared.domain.entities.record

/**
 * 听写会话类型枚举
 */
enum class SessionRecordType(val displayName: String, val description: String) {
    LESSON("单课听写", "对特定课程的单词进行听写"),
    WORKBOOK("练习册听写", "对练习册中的单词进行听写"),
    COMMON_MISTAKES("常错词听写", "对用户常错的单词进行专项练习"),
    REVIEW("复习听写", "基于艾宾浩斯遗忘曲线的复习听写");

    companion object {
        fun fromDisplayName(name: String): SessionRecordType? = values().find { it.displayName == name }
    }
}