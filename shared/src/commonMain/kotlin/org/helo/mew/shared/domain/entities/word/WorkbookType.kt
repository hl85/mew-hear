package org.helo.mew.shared.domain.entities.word

/**
 * 练习册类型枚举
 */
enum class WorkbookType(val displayName: String, val description: String) {
    COMMON_MISTAKES("常错词汇库", "系统自动收集的用户常错词汇"),
    REVIEW("复习册", "基于艾宾浩斯遗忘曲线的复习词汇"),
    CUSTOM("自定义", "用户自定义分类的词汇集合");
    
    companion object {
        fun fromDisplayName(name: String): WorkbookType? = values().find { it.displayName == name }
        
        /**
         * 获取系统自动管理的类型
         */
        fun getSystemManagedTypes(): List<WorkbookType> = listOf(COMMON_MISTAKES, REVIEW)

        /**
         * 获取用户可编辑的类型
         */
        fun getUserEditableTypes(): List<WorkbookType> = listOf(CUSTOM)
    }
} 