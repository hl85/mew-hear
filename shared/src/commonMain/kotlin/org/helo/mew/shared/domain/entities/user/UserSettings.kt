package org.helo.mew.shared.domain.entities.user

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.helo.mew.shared.domain.entities.word.Grade


/**
 * 用户设置实体
 */
@Serializable
data class UserSettings(
    val id: String,
    val userId: String,
    val grade: Grade,
    val textbookVersions: List<String> = emptyList(),
    val repeatCount: Int = 3,
    val autoPlayNext: Boolean = true,
    val showPinyin: Boolean = true,
    val showPhonetics: Boolean = false,
    val hapticFeedback: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        require(userId.isNotBlank()) { "用户ID不能为空" }
        require(repeatCount in 1..10) { "重复次数必须在1-10之间" }
    }


    /**
     * 检查是否支持指定教材版本
     */
    fun supportsTextbookVersion(version: String): Boolean {
        return textbookVersions.contains(version)
    }

    /**
     * 添加教材版本
     */
    fun addTextbookVersion(version: String): UserSettings {
        return if (!textbookVersions.contains(version)) {
            copy(textbookVersions = textbookVersions + version)
        } else {
            this
        }
    }

    /**
     * 移除教材版本
     */
    fun removeTextbookVersion(version: String): UserSettings {
        return copy(textbookVersions = textbookVersions - version)
    }

    /**
     * 更新年级
     */
    fun updateGrade(newGrade: Grade): UserSettings = copy(grade = newGrade)
    /**
     * 验证设置值是否有效
     */
    fun isValid(): Boolean {
        return repeatCount in 1..10
    }

    /**
     * 更新重复次数
     */
    fun updateRepeatCount(count: Int): UserSettings {
        return if (count in 1..10) {
            copy(repeatCount = count)
        } else {
            this
        }
    }

    /**
     * 切换拼音显示
     */
    fun togglePinyin(): UserSettings = copy(showPinyin = !showPinyin)

    /**
     * 切换音标显示
     */
    fun togglePhonetics(): UserSettings = copy(showPhonetics = !showPhonetics)

    /**
     * 切换自动播放下一个
     */
    fun toggleAutoPlayNext(): UserSettings = copy(autoPlayNext = !autoPlayNext)

    /**
     * 切换触觉反馈
     */
    fun toggleHapticFeedback(): UserSettings = copy(hapticFeedback = !hapticFeedback)
}
