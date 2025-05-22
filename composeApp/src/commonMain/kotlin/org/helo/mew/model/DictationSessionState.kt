package org.helo.mew.model

/**
 * 听写会话状态
 */
sealed class DictationSessionState {
    /**
     * 初始状态
     */
    object Idle : DictationSessionState()
    
    /**
     * 准备状态，已选择听写内容但尚未开始
     */
    data class Ready(
        val words: List<Word>,
        val lessonId: String? = null,
        val lessonName: String? = null
    ) : DictationSessionState()
    
    /**
     * 进行中状态
     */
    data class InProgress(
        val words: List<Word>,
        val currentWordIndex: Int,
        val userInputs: List<String>,
        val totalWords: Int,
        val lessonId: String? = null,
        val lessonName: String? = null
    ) : DictationSessionState()
    
    /**
     * 完成状态
     */
    data class Completed(
        val words: List<Word>,
        val userInputs: List<String>,
        val correctCount: Int,
        val lessonId: String? = null,
        val lessonName: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : DictationSessionState()
}
