package org.helo.mew.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.helo.mew.model.*
import org.helo.mew.service.ReviewPlanner

/**
 * 基于内存的听写数据仓库实现（MVP版本）
 */
class InMemoryDictationRepository : DictationRepository {

    private val reviewPlanner = ReviewPlanner()

    // 模拟数据存储
    private val textbooks = mutableListOf<Textbook>()
    private val classUnits = mutableListOf<ClassUnit>()
    private val lessons = mutableListOf<Lesson>()
    private val words = mutableListOf<Word>()
    private val userSettings = mutableMapOf<String, UserSettings>()
    private val dictationRecords = mutableListOf<DictationRecord>()
    private val errorWords = mutableListOf<ErrorWord>()

    // 示例数据初始化
    init {
        initializeExampleData()
    }

    private fun initializeExampleData() {
        // 添加一些示例单词
        words.addAll(
            listOf(
                Word("w1", "apple", "apple.mp3", "ˈæpəl", "苹果", "I like eating apples."),
                Word("w2", "banana", "banana.mp3", "bəˈnɑːnə", "香蕉", "This banana is ripe."),
                Word("w3", "cat", "cat.mp3", "kæt", "猫", "The cat is sleeping."),
                Word("w4", "dog", "dog.mp3", "dɔːɡ", "狗", "My dog is friendly."),
                Word("w5", "你好", "nihao.mp3", "nǐ hǎo", "hello", "你好，世界！"),
                Word("w6", "早上好", "zaoshanghao.mp3", "zǎo shang hǎo", "good morning", "早上好，老师！"),
                Word("w7", "谢谢", "xiexie.mp3", "xiè xie", "thank you", "谢谢你的帮助！"),
                Word("w8", "再见", "zaijian.mp3", "zài jiàn", "goodbye", "再见，明天见！")
            )
        )

        // 添加一些示例课程
        lessons.addAll(
            listOf(
                Lesson("l1", "Fruits", listOf(words[0], words[1]), "u1"),
                Lesson("l2", "Animals", listOf(words[2], words[3]), "u1"),
                Lesson("l3", "基础问候语", listOf(words[4], words[5]), "u2"),
                Lesson("l4", "礼貌用语", listOf(words[6], words[7]), "u2")
            )
        )

        // 添加一些示例单元
        classUnits.addAll(
            listOf(
                ClassUnit("u1", "Unit 1: Everyday English", listOf(lessons[0], lessons[1]), Grade.K1, Subject.ENGLISH),
                ClassUnit("u2", "单元1: 日常用语", listOf(lessons[2], lessons[3]), Grade.K1, Subject.CHINESE)
            )
        )

        // 添加一些示例教材
        textbooks.addAll(
            listOf(
                Textbook(TextbookVersion.PEOPLE_EDUCATION, Grade.K1, Subject.ENGLISH, listOf(classUnits[0])),
                Textbook(TextbookVersion.PEOPLE_EDUCATION, Grade.K1, Subject.CHINESE, listOf(classUnits[1]))
            )
        )

        // 添加默认用户设置
        userSettings["user1"] = UserSettings(
            "user1",
            TextbookVersion.PEOPLE_EDUCATION,
            Grade.K1,
            true,
            3
        )
    }

    override suspend fun getTextbooks(subject: Subject, grade: Grade): List<Textbook> {
        return textbooks.filter { it.subject == subject && it.grade == grade }
    }

    override suspend fun getUnits(textbookVersion: TextbookVersion, subject: Subject, grade: Grade): List<ClassUnit> {
        return classUnits.filter {
            it.subject == subject &&
            it.grade == grade &&
            textbooks.any { textbook -> 
                textbook.version == textbookVersion && 
                textbook.classUnits.any { unit -> unit.id == it.id }
            }
        }
    }

    override suspend fun getLessons(unitId: String): List<Lesson> {
        return lessons.filter { it.unitId == unitId }
    }

    override suspend fun getWords(lessonId: String): List<Word> {
        val lesson = lessons.find { it.id == lessonId } ?: return emptyList()
        return lesson.words
    }

    override suspend fun getWord(wordId: String): Word? {
        return words.find { it.id == wordId }
    }

    override suspend fun getUserSettings(userId: String): UserSettings {
        return userSettings[userId] ?: UserSettings(
            userId,
            TextbookVersion.PEOPLE_EDUCATION,
            Grade.K1,
            true,
            3
        )
    }

    override suspend fun updateUserSettings(settings: UserSettings) {
        userSettings[settings.userId] = settings
    }

    override suspend fun saveDictationRecord(record: DictationRecord) {
        // 移除相同ID的记录（如果存在）
        dictationRecords.removeAll { it.id == record.id }
        dictationRecords.add(record)
        
        // 更新常错词
        for (item in record.items) {
            if (!item.correct) {
                // 找到现有的错误词记录或创建新的
                val existingErrorWord = errorWords.find { it.wordId == item.wordId && it.userId == record.userId }
                if (existingErrorWord != null) {
                    val updatedErrorWord = existingErrorWord.copy(
                        errorCount = existingErrorWord.errorCount + 1,
                        lastErrorTime = item.timestamp,
                        nextReviewTime = reviewPlanner.calculateNextReviewTime(existingErrorWord.errorCount + 1, item.timestamp)
                    )
                    errorWords.remove(existingErrorWord)
                    errorWords.add(updatedErrorWord)
                } else {
                    errorWords.add(
                        ErrorWord(
                            item.wordId,
                            1,
                            item.timestamp,
                            reviewPlanner.calculateNextReviewTime(1, item.timestamp),
                            record.userId
                        )
                    )
                }
            }
        }
    }

    override suspend fun getDictationRecords(userId: String): List<DictationRecord> {
        return dictationRecords.filter { it.userId == userId }.sortedByDescending { it.startTime }
    }

    override suspend fun getDictationRecordsByLesson(userId: String, lessonId: String): List<DictationRecord> {
        return dictationRecords.filter { it.userId == userId && it.lessonId == lessonId }
            .sortedByDescending { it.startTime }
    }

    override suspend fun getErrorWords(userId: String): List<ErrorWord> {
        return errorWords.filter { it.userId == userId }
    }

    override suspend fun addErrorWord(errorWord: ErrorWord) {
        errorWords.add(errorWord)
    }

    override suspend fun updateErrorWord(errorWord: ErrorWord) {
        val index = errorWords.indexOfFirst { it.wordId == errorWord.wordId && it.userId == errorWord.userId }
        if (index != -1) {
            errorWords[index] = errorWord
        }
    }

    // 获取需要复习的单词流
    private val wordsToReviewFlow = MutableStateFlow<Map<String, List<String>>>(emptyMap())

    override suspend fun getWordsToReview(userId: String): Flow<List<String>> {
        // 更新需要复习的单词
        val now = Clock.System.now()
        val wordsToReview = reviewPlanner.getWordsToReview(
            errorWords.filter { it.userId == userId }, 
            now
        )
        
        // 更新流
        val currentMap = wordsToReviewFlow.value.toMutableMap()
        currentMap[userId] = wordsToReview
        wordsToReviewFlow.value = currentMap
        
        // 返回该用户的复习单词流
        return wordsToReviewFlow.map { it[userId] ?: emptyList() }
    }
}
