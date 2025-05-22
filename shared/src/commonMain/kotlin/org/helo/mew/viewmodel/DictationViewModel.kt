package org.helo.mew.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.helo.mew.model.*
import org.helo.mew.repository.DictationRepository
import org.helo.mew.service.AudioService
import kotlin.random.Random

/**
 * 听写功能的ViewModel
 */
class DictationViewModel(
    private val repository: DictationRepository,
    private val audioService: AudioService,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    // 当前用户ID
    private val userId = "user1" // 在MVP版本中硬编码用户ID
    
    // 听写会话状态
    private val _sessionState = MutableStateFlow<DictationSessionState>(DictationSessionState.Idle)
    val sessionState: StateFlow<DictationSessionState> = _sessionState.asStateFlow()
    
    // 当前课程
    private val _currentLesson = MutableStateFlow<Lesson?>(null)
    val currentLesson: StateFlow<Lesson?> = _currentLesson.asStateFlow()
    
    // 当前单词
    private val _currentWord = MutableStateFlow<Word?>(null)
    val currentWord: StateFlow<Word?> = _currentWord.asStateFlow()
    
    // 当前听写记录
    private val _dictationItems = MutableStateFlow<List<DictationRecordItem>>(emptyList())
    
    // 听写结果
    private val _dictationResult = MutableStateFlow<DictationRecord?>(null)
    val dictationResult: StateFlow<DictationRecord?> = _dictationResult.asStateFlow()
    
    // 常错词列表
    private val _errorWords = MutableStateFlow<List<Word>>(emptyList())
    val errorWords: StateFlow<List<Word>> = _errorWords.asStateFlow()
    
    // 历史记录
    private val _dictationHistory = MutableStateFlow<List<DictationRecord>>(emptyList())
    val dictationHistory: StateFlow<List<DictationRecord>> = _dictationHistory.asStateFlow()
    
    // 用户设置
    private val _userSettings = MutableStateFlow<UserSettings?>(null)
    val userSettings: StateFlow<UserSettings?> = _userSettings.asStateFlow()
    
    // 初始化函数
    fun initialize() {
        scope.launch {
            loadUserSettings()
            loadErrorWords()
            loadDictationHistory()
        }
    }
    
    // 加载用户设置
    private fun loadUserSettings() {
        scope.launch {
            val settings = repository.getUserSettings(userId)
            _userSettings.value = settings
        }
    }
    
    // 更新用户设置
    fun updateSettings(settings: UserSettings) {
        scope.launch {
            repository.updateUserSettings(settings)
            _userSettings.value = settings
        }
    }
    
    // 加载常错词
    private fun loadErrorWords() {
        scope.launch {
            val errors = repository.getErrorWords(userId)
            val wordIds = errors.map { it.wordId }
            val words = wordIds.mapNotNull { repository.getWord(it) }
            _errorWords.value = words
        }
    }
    
    // 加载听写历史
    private fun loadDictationHistory() {
        scope.launch {
            val history = repository.getDictationRecords(userId)
            _dictationHistory.value = history
        }
    }
    
    // 开始听写课程
    fun startLessonDictation(lessonId: String) {
        scope.launch {
            val lesson = repository.getLessons("")
                .find { it.id == lessonId } ?: return@launch
                
            _currentLesson.value = lesson
            _dictationItems.value = emptyList()
            _sessionState.value = DictationSessionState.Ready
        }
    }
    
    // 开始常错词听写
    fun startErrorWordsDictation() {
        scope.launch {
            // 获取需要复习的常错词
            repository.getWordsToReview(userId).collect { wordIds ->
                val words = wordIds.mapNotNull { repository.getWord(it) }
                if (words.isNotEmpty()) {
                    val customLesson = Lesson(
                        id = "error_words_${System.currentTimeMillis()}",
                        name = "常错词复习",
                        words = words,
                        unitId = "error_words_unit"
                    )
                    _currentLesson.value = customLesson
                    _dictationItems.value = emptyList()
                    _sessionState.value = DictationSessionState.Ready
                }
            }
        }
    }
    
    // 开始当前听写会话
    fun startDictation() {
        val lesson = _currentLesson.value ?: return
        
        if (lesson.words.isEmpty()) {
            _sessionState.value = DictationSessionState.Error("当前课程没有单词")
            return
        }
        
        val startTime = Clock.System.now()
        _sessionState.value = DictationSessionState.InProgress(
            totalWords = lesson.words.size,
            currentWordIndex = 0,
            startTime = startTime
        )
        
        playNextWord()
    }
    
    // 播放下一个单词
    private fun playNextWord() {
        val lesson = _currentLesson.value ?: return
        val state = _sessionState.value
        
        if (state !is DictationSessionState.InProgress) return
        
        if (state.currentWordIndex >= lesson.words.size) {
            // 听写完成
            finishDictation()
            return
        }
        
        val word = lesson.words[state.currentWordIndex]
        _currentWord.value = word
        
        // 播放音频
        audioService.playAudio(word.audioUrl) {
            // 音频播放完成后的回调
            // 这里可以自动进入下一个单词或等待用户操作
        }
    }
    
    // 下一个单词（用户手动操作）
    fun nextWord() {
        val state = _sessionState.value
        if (state !is DictationSessionState.InProgress) return
        
        val currentWord = _currentWord.value ?: return
        
        // 默认标记为未检查
        val dictationItem = DictationRecordItem(
            wordId = currentWord.id,
            correct = false  // 默认未检查，用户需要手动标记正确
        )
        _dictationItems.value = _dictationItems.value + dictationItem
        
        // 更新状态，移动到下一个单词
        _sessionState.value = state.copy(
            currentWordIndex = state.currentWordIndex + 1
        )
        
        playNextWord()
    }
    
    // 标记单词结果
    fun markWordResult(wordId: String, isCorrect: Boolean) {
        val items = _dictationItems.value.toMutableList()
        val index = items.indexOfLast { it.wordId == wordId }
        
        if (index != -1) {
            items[index] = items[index].copy(correct = isCorrect)
            _dictationItems.value = items
        }
    }
    
    // 完成听写
    private fun finishDictation() {
        val lesson = _currentLesson.value ?: return
        val state = _sessionState.value
        
        if (state !is DictationSessionState.InProgress) return
        
        val endTime = Clock.System.now()
        val dictationRecord = DictationRecord(
            id = "dictation_${Random.nextInt(10000)}",
            lessonId = lesson.id,
            startTime = state.startTime,
            endTime = endTime,
            items = _dictationItems.value,
            userId = userId
        )
        
        scope.launch {
            repository.saveDictationRecord(dictationRecord)
            _dictationResult.value = dictationRecord
            _sessionState.value = DictationSessionState.Completed(dictationRecord)
            
            // 刷新听写历史
            loadDictationHistory()
            // 刷新常错词
            loadErrorWords()
        }
    }
    
    // 重播当前单词
    fun replayCurrentWord() {
        val word = _currentWord.value ?: return
        audioService.playAudio(word.audioUrl)
    }
}

/**
 * 听写会话状态
 */
sealed class DictationSessionState {
    object Idle : DictationSessionState()
    object Ready : DictationSessionState()
    data class InProgress(
        val totalWords: Int,
        val currentWordIndex: Int,
        val startTime: Instant
    ) : DictationSessionState()
    data class Completed(val result: DictationRecord) : DictationSessionState()
    data class Error(val message: String) : DictationSessionState()
}
