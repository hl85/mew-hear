package org.helo.mew.repository

import kotlinx.coroutines.flow.Flow
import org.helo.mew.model.*

/**
 * 听写数据仓库接口
 */
interface DictationRepository {
    // 教材与课程管理
    suspend fun getTextbooks(subject: Subject, grade: Grade): List<Textbook>
    suspend fun getUnits(textbookVersion: TextbookVersion, subject: Subject, grade: Grade): List<ClassUnit>
    suspend fun getLessons(unitId: String): List<Lesson>
    suspend fun getWords(lessonId: String): List<Word>
    suspend fun getWord(wordId: String): Word?
    
    // 用户设置
    suspend fun getUserSettings(userId: String): UserSettings
    suspend fun updateUserSettings(settings: UserSettings)
    
    // 听写记录
    suspend fun saveDictationRecord(record: DictationRecord)
    suspend fun getDictationRecords(userId: String): List<DictationRecord>
    suspend fun getDictationRecordsByLesson(userId: String, lessonId: String): List<DictationRecord>
    
    // 常错词管理
    suspend fun getErrorWords(userId: String): List<ErrorWord>
    suspend fun addErrorWord(errorWord: ErrorWord)
    suspend fun updateErrorWord(errorWord: ErrorWord)
    suspend fun getWordsToReview(userId: String): Flow<List<String>>
}
