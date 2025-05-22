package org.helo.mew.model

/**
 * 听写记录
 */
data class DictationRecord(
    val id: String,
    val lessonId: String? = null,
    val lessonName: String? = null,
    val words: List<Word>,
    val userInputs: List<String>,
    val correctCount: Int,
    val timestamp: Long,
    val score: Int = calculateScore(correctCount, words.size)
) {
    companion object {
        fun calculateScore(correctCount: Int, totalCount: Int): Int {
            if (totalCount == 0) return 0
            return ((correctCount.toFloat() / totalCount) * 100).toInt()
        }
    }
}
