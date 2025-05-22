package org.helo.mew.service

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.helo.mew.model.ErrorWord
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * 基于艾宾浩斯遗忘曲线的复习规划服务
 */
class ReviewPlanner {

    /**
     * 艾宾浩斯间隔（分钟、小时、天）
     * 复习间隔：第1次 - 5分钟, 第2次 - 30分钟, 第3次 - 12小时, 第4次 - 1天, 第5次 - 2天,
     * 第6次 - 4天, 第7次 - 7天, 第8次 - 15天, 第9次 - 30天
     */
    private val reviewIntervals = listOf(
        5.minutes,
        30.minutes,
        12.hours,
        1.days,
        2.days,
        4.days,
        7.days,
        15.days,
        30.days
    )

    /**
     * 计算下次复习时间
     * @param errorCount 错误次数（决定复习的紧急程度）
     * @param lastErrorTime 最后一次错误的时间
     * @return 下次复习的时间点
     */
    fun calculateNextReviewTime(errorCount: Int, lastErrorTime: Instant): Instant {
        // 根据错误次数和艾宾浩斯曲线确定复习间隔
        // 错误次数越多，复习间隔越短（最短为5分钟）
        val intervalIndex = if (errorCount >= reviewIntervals.size) {
            0 // 如果错误次数太多，立即复习
        } else {
            reviewIntervals.size - errorCount - 1 // 错误次数越多，间隔越短
        }.coerceIn(0, reviewIntervals.size - 1)
        
        val interval = reviewIntervals[intervalIndex]
        return lastErrorTime.plus(interval)
    }

    /**
     * 获取需要复习的单词
     * @param errorWords 错误单词列表
     * @param now 当前时间（默认为系统时间）
     * @return 需要复习的单词ID列表
     */
    fun getWordsToReview(errorWords: List<ErrorWord>, now: Instant = Clock.System.now()): List<String> {
        return errorWords
            .filter { it.nextReviewTime <= now }
            .sortedBy { it.nextReviewTime }
            .map { it.wordId }
    }

    /**
     * 更新错误单词的复习计划
     * @param errorWord 错误单词
     * @param isCorrect 本次复习是否正确
     * @return 更新后的错误单词对象
     */
    fun updateReviewPlan(errorWord: ErrorWord, isCorrect: Boolean): ErrorWord {
        val now = Clock.System.now()
        
        return if (isCorrect) {
            // 正确：减少错误次数（但不少于0），推迟下次复习时间
            val newErrorCount = (errorWord.errorCount - 1).coerceAtLeast(0)
            errorWord.copy(
                errorCount = newErrorCount,
                nextReviewTime = calculateNextReviewTime(newErrorCount, now)
            )
        } else {
            // 错误：增加错误次数，提前下次复习时间
            val newErrorCount = errorWord.errorCount + 1
            errorWord.copy(
                errorCount = newErrorCount,
                lastErrorTime = now,
                nextReviewTime = calculateNextReviewTime(newErrorCount, now)
            )
        }
    }
}
