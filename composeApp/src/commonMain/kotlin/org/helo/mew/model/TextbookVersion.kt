package org.helo.mew.model

/**
 * 教材版本枚举
 */
enum class TextbookVersion(val id: String, val displayName: String) {
    PEP("pep", "人教版"),
    FOREIGN_LANGUAGE("fl", "外语版"),
    BEIJING_NORMAL("bn", "北师版");

    companion object {
        /**
         * 根据ID查找对应的教材版本
         * @param id 教材版本的ID
         * @return 找到的教材版本，如果没有找到则返回null
         */
        fun fromId(id: String): TextbookVersion? {
            return values().find { it.id == id }
        }
    }
}
