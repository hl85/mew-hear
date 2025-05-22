package org.helo.mew.model

/**
 * 用户设置
 */
data class UserSettings(
    val preferredGrade: Grade? = Grade.K1,
    val preferredSubject: Subject? = Subject.ENGLISH,
    val preferredTextbookVersion: TextbookVersion? = TextbookVersion.PEP,
    val autoPlayAudio: Boolean = true,
    val playAudioTimes: Int = 2,
    val showTranslation: Boolean = false,
    val showExamples: Boolean = false,
    val volume: Float = 0.8f
)
