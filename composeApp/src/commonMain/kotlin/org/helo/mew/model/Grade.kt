package org.helo.mew.model

/**
 * 年级枚举
 */
enum class Grade(val id: String, val displayName: String) {
    K1("k1", "一年级"),
    K2("k2", "二年级"),
    K3("k3", "三年级"),
    K4("k4", "四年级"),
    K5("k5", "五年级"),
    K6("k6", "六年级");

    companion object {
        /**
         * 根据ID查找对应的年级枚举
         */
        fun fromId(id: String): Grade? {
            return values().find { it.id == id }
        }
    }
}
