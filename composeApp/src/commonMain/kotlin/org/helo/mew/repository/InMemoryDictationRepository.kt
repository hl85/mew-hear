package org.helo.mew.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.helo.mew.model.*

/**
 * 内存实现的听写存储库
 * 注意：这是一个MVP版本，只用于演示，数据存储在内存中，重启应用后数据会丢失
 */
class InMemoryDictationRepository : DictationRepository {
    // 模拟数据
    private val gradesFlow = MutableStateFlow<List<Grade>>(emptyList())
    private val subjectsFlow = MutableStateFlow<List<Subject>>(emptyList())
    private val unitsFlow = MutableStateFlow<List<ClassUnit>>(emptyList())
    private val lessonsFlow = MutableStateFlow<List<Lesson>>(emptyList())
    private val wordsFlow = MutableStateFlow<List<Word>>(emptyList())
    private val dictationHistoryFlow = MutableStateFlow<List<DictationRecord>>(emptyList())
    private val userSettingsFlow = MutableStateFlow(UserSettings())

    override suspend fun initialize() {
        // 初始化模拟数据
        gradesFlow.value = Grade.values().toList()
        subjectsFlow.value = Subject.values().toList()
        
        // 模拟一些单元数据
        val units = listOf(
            ClassUnit("u1", "Unit 1: Everyday English", emptyList(), Grade.K1, Subject.ENGLISH),
            ClassUnit("u2", "Unit 2: Animals & Plants", emptyList(), Grade.K1, Subject.ENGLISH),
            ClassUnit("u3", "单元1: 日常用语", emptyList(), Grade.K1, Subject.CHINESE),
            ClassUnit("u4", "单元2: 自然现象", emptyList(), Grade.K1, Subject.CHINESE)
        )
        unitsFlow.value = units
        
        // 模拟一些课程数据
        val lessons = listOf(
            Lesson("l1", "Lesson 1: Greetings", getEnglishWordsList1(), "u1"),
            Lesson("l2", "Lesson 2: School Life", getEnglishWordsList2(), "u1"),
            Lesson("l3", "Lesson 1: Animals", emptyList(), "u2"),
            Lesson("l4", "第1课: 问候语", getChineseWordsList1(), "u3"),
            Lesson("l5", "第2课: 学校生活", emptyList(), "u3")
        )
        lessonsFlow.value = lessons
        
        // 设置所有单词的集合
        wordsFlow.value = lessons.flatMap { it.words }
    }

    override fun getGrades(): Flow<List<Grade>> = gradesFlow

    override fun getSubjects(): Flow<List<Subject>> = subjectsFlow

    override fun getUnitsForGradeAndSubject(grade: Grade, subject: Subject): Flow<List<ClassUnit>> {
        return unitsFlow.map { units ->
            units.filter { it.grade == grade && it.subject == subject }
        }
    }

    override fun getLessonsForUnit(unitId: String): Flow<List<Lesson>> {
        return lessonsFlow.map { lessons ->
            lessons.filter { it.unitId == unitId }
        }
    }

    override fun getWordsForLesson(lessonId: String): Flow<List<Word>> {
        return lessonsFlow.map { lessons ->
            lessons.find { it.id == lessonId }?.words ?: emptyList()
        }
    }

    override suspend fun addDictationRecord(record: DictationRecord) {
        val currentRecords = dictationHistoryFlow.value.toMutableList()
        currentRecords.add(record)
        dictationHistoryFlow.value = currentRecords
    }

    override fun getDictationHistory(): Flow<List<DictationRecord>> = dictationHistoryFlow

    override fun getErrorWords(): Flow<List<Word>> {
        // 在实际应用中，这应该从错误记录中计算得出
        // MVP版本中，我们简单返回一些预定义的词
        return wordsFlow.map { words ->
            words.take(5)
        }
    }

    override fun getUserSettings(): Flow<UserSettings> = userSettingsFlow

    override suspend fun updateUserSettings(settings: UserSettings) {
        userSettingsFlow.value = settings
    }

    // 模拟英语单词列表1
    private fun getEnglishWordsList1(): List<Word> {
        return listOf(
            Word("w1", "Hello", "audio/hello.mp3", "你好", listOf("Hello, how are you?")),
            Word("w2", "Goodbye", "audio/goodbye.mp3", "再见", listOf("Goodbye, see you tomorrow.")),
            Word("w3", "Thank you", "audio/thankyou.mp3", "谢谢", listOf("Thank you for your help."))
        )
    }

    // 模拟英语单词列表2
    private fun getEnglishWordsList2(): List<Word> {
        return listOf(
            Word("w4", "School", "audio/school.mp3", "学校", listOf("I go to school every day.")),
            Word("w5", "Teacher", "audio/teacher.mp3", "老师", listOf("My teacher is very kind."))
        )
    }

    // 模拟中文词语列表1
    private fun getChineseWordsList1(): List<Word> {
        return listOf(
            Word("w6", "你好", "audio/nihao.mp3", "Hello", listOf("你好，早上好！")),
            Word("w7", "谢谢", "audio/xiexie.mp3", "Thank you", listOf("非常谢谢你的帮助。"))
        )
    }
}
