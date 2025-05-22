package org.helo.mew.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * 听写记录项目
 */
data class DictationRecordItem(
    val wordId: String,
    val correct: Boolean, // 是否正确
    val timestamp: Instant = Clock.System.now() // 记录时间
)

/**
 * 听写记录
 */
data class DictationRecord(
    val id: String,
    val lessonId: String, // 关联的课程
    val startTime: Instant,
    val endTime: Instant,
    val items: List<DictationRecordItem>,
    val userId: String
) {
    // 计算正确率
    val correctRate: Float
        get() = if (items.isEmpty()) 0f else items.count { it.correct }.toFloat() / items.size
    
    // 计算用时（秒）
    val duration: Long
        get() = endTime.epochSeconds - startTime.epochSeconds
}

/**
 * 常错词汇
 */
data class ErrorWord(
    val wordId: String,
    val errorCount: Int, // 错误次数
    val lastErrorTime: Instant, // 最后错误时间
    val nextReviewTime: Instant, // 下次复习时间（基于艾宾浩斯曲线）
    val userId: String
)

/**
 * 用户设置
 */
data class UserSettings(
    val userId: String,
    val preferredTextbookVersion: TextbookVersion,
    val preferredGrade: Grade,
    val enableAudioAutoPlay: Boolean = true,
    val dictationInterval: Int = 3 // 听写间隔（秒）
)

/**
 * 用户数据
 */
data class UserData(
    val id: String,
    val settings: UserSettings,
    val dictationRecords: List<DictationRecord>,
    val errorWords: List<ErrorWord>
)
