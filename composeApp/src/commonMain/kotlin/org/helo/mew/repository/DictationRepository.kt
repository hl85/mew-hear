package org.helo.mew.repository

import kotlinx.coroutines.flow.Flow
import org.helo.mew.model.*

/**
 * 听写存储库接口
 */
interface DictationRepository {
    /**
     * 初始化仓库
     */
    suspend fun initialize()
    
    /**
     * 获取所有年级
     */
    fun getGrades(): Flow<List<Grade>>
    
    /**
     * 获取所有学科
     */
    fun getSubjects(): Flow<List<Subject>>
    
    /**
     * 获取指定年级和学科下的所有单元
     */
    fun getUnitsForGradeAndSubject(grade: Grade, subject: Subject): Flow<List<ClassUnit>>
    
    /**
     * 获取指定单元的所有课程
     */
    fun getLessonsForUnit(unitId: String): Flow<List<Lesson>>
    
    /**
     * 获取指定课程的所有单词
     */
    fun getWordsForLesson(lessonId: String): Flow<List<Word>>
    
    /**
     * 添加听写记录
     */
    suspend fun addDictationRecord(record: DictationRecord)
    
    /**
     * 获取所有听写记录
     */
    fun getDictationHistory(): Flow<List<DictationRecord>>
    
    /**
     * 获取常错词列表
     */
    fun getErrorWords(): Flow<List<Word>>
    
    /**
     * 获取用户设置
     */
    fun getUserSettings(): Flow<UserSettings>
    
    /**
     * 更新用户设置
     */
    suspend fun updateUserSettings(settings: UserSettings)
}
