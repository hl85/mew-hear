package org.helo.mew.model

/**
 * 学科枚举
 */
enum class Subject(val id: String, val displayName: String) {
    ENGLISH("english", "英语"),
    CHINESE("chinese", "语文");

    companion object {
        /**
         * 根据ID查找学科
         * @param id 学科ID
         * @return 找到的学科或null
         */
        fun fromId(id: String): Subject? {
            return values().find { it.id == id }
        }
    }
}
