package org.helo.mew.shared.domain.entities.word

/**
 * 教材版本枚举
 */
enum class TextbookVersion(
    val displayName: String,
    val code: String,
    val subject: Subject
) {
    // 英语教材版本
    PEP_ENGLISH("人教版英语", "PEP_EN", Subject.ENGLISH),
    BEIJING_NORMAL_ENGLISH("北师大版英语", "BNU_EN", Subject.ENGLISH),
    BEIJING_EDITION_ENGLISH("北京版英语", "BJ_EN", Subject.ENGLISH),
    SHANGHAI_EDITION_ENGLISH("沪教版英语", "SH_EN", Subject.ENGLISH),

    // 语文教材版本
    PEP_CHINESE("人教版语文", "PEP_CN", Subject.CHINESE),
    BEIJING_NORMAL_CHINESE("北师大版语文", "BNU_CN", Subject.CHINESE),
    BEIJING_EDITION_CHINESE("北京版语文", "BJ_CN", Subject.CHINESE),
    SHANGHAI_EDITION_CHINESE("沪教版语文", "SH_CN", Subject.CHINESE),
    UNKNOWN("未知版本", "UNKNOWN", Subject.CHINESE);

    companion object {
        fun bySubject(subject: Subject): List<TextbookVersion> =
            values().filter { it.subject == subject }

        fun fromCode(code: String): TextbookVersion? =
            values().find { it.code == code }

        fun fromDisplayName(name: String): TextbookVersion? =
            values().find { it.displayName == name }
    }
}