package org.helo.mew.shared.data.entities.word

/**
 * 年级枚举
 */
enum class Grade(val displayName: String, val value: Int) {
    GRADE_1("一年级", 1),
    GRADE_2("二年级", 2),
    GRADE_3("三年级", 3),
    GRADE_4("四年级", 4),
    GRADE_5("五年级", 5),
    GRADE_6("六年级", 6),
    UNKNOWN("未知", 0);

    companion object {
        fun fromValue(value: Int): Grade {
            return values().find { it.value == value } ?: UNKNOWN
        }

        fun fromString(str: String): Grade {
            return when (str.lowercase()) {
                "grade_1", "1", "一年级" -> GRADE_1
                "grade_2", "2", "二年级" -> GRADE_2
                "grade_3", "3", "三年级" -> GRADE_3
                "grade_4", "4", "四年级" -> GRADE_4
                "grade_5", "5", "五年级" -> GRADE_5
                "grade_6", "6", "六年级" -> GRADE_6
                else -> UNKNOWN
            }
        }
    }
}
