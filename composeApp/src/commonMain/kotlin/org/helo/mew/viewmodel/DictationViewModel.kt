package org.helo.mew.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.helo.mew.model.*
import org.helo.mew.repository.DictationRepository
import org.helo.mew.service.AudioService

/**
 * 听写ViewModel
 */
class DictationViewModel(
    private val repository: DictationRepository,
    private val audioService: AudioService
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)
    
    // 会话状态
    private val _sessionState = MutableStateFlow<DictationSessionState>(DictationSessionState.Idle)
    val sessionState: StateFlow<DictationSessionState> = _sessionState.asStateFlow()
    
    // 用户设置
    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings

    // 听写历史记录
    private val _dictationHistory = MutableStateFlow<List<DictationRecord>>(emptyList())
    val dictationHistory: StateFlow<List<DictationRecord>> = _dictationHistory
    
    // 常错词
    private val _errorWords = MutableStateFlow<List<Word>>(emptyList())
    val errorWords: StateFlow<List<Word>> = _errorWords
    
    /**
     * 初始化ViewModel
     */
    suspend fun initialize() {
        repository.initialize()
        
        // 监听数据变化
        viewModelScope.launch {
            repository.getUserSettings().collect { settings ->
                _userSettings.value = settings
            }
        }
        
        viewModelScope.launch {
            repository.getDictationHistory().collect { records ->
                _dictationHistory.value = records
            }
        }
        
        viewModelScope.launch {
            repository.getErrorWords().collect { words ->
                _errorWords.value = words
            }
        }
    }
    
    /**
     * 准备听写会话
     */
    fun prepareSession(words: List<Word>, lessonId: String? = null, lessonName: String? = null) {
        _sessionState.value = DictationSessionState.Ready(words, lessonId, lessonName)
    }
    
    /**
     * 开始听写会话
     */
    fun startSession() {
        val readyState = _sessionState.value as? DictationSessionState.Ready ?: return
        
        val words = readyState.words
        if (words.isEmpty()) return
        
        _sessionState.value = DictationSessionState.InProgress(
            words = words,
            currentWordIndex = 0,
            userInputs = List(words.size) { "" },
            totalWords = words.size,
            lessonId = readyState.lessonId,
            lessonName = readyState.lessonName
        )
        
        // 自动播放当前词语的音频
        playCurrentWordAudio()
    }
    
    /**
     * 播放当前词语的音频
     */
    fun playCurrentWordAudio() {
        val currentState = _sessionState.value as? DictationSessionState.InProgress ?: return
        val currentWord = currentState.words.getOrNull(currentState.currentWordIndex) ?: return
        
        audioService.playAudio(currentWord.audioUrl) {
            // 音频播放完成后的回调
        }
    }
    
    /**
     * 更新用户输入
     */
    fun updateUserInput(input: String) {
        val currentState = _sessionState.value as? DictationSessionState.InProgress ?: return
        val newInputs = currentState.userInputs.toMutableList()
        newInputs[currentState.currentWordIndex] = input
        
        _sessionState.value = currentState.copy(userInputs = newInputs)
    }
    
    /**
     * 移动到下一个词
     */
    fun moveToNext() {
        val currentState = _sessionState.value as? DictationSessionState.InProgress ?: return
        val nextIndex = currentState.currentWordIndex + 1
        
        if (nextIndex >= currentState.totalWords) {
            // 听写完成
            completeSession()
        } else {
            _sessionState.value = currentState.copy(currentWordIndex = nextIndex)
            // 自动播放下一个词的音频
            playCurrentWordAudio()
        }
    }
    
    /**
     * 移动到上一个词
     */
    fun moveToPrevious() {
        val currentState = _sessionState.value as? DictationSessionState.InProgress ?: return
        val prevIndex = (currentState.currentWordIndex - 1).coerceAtLeast(0)
        
        _sessionState.value = currentState.copy(currentWordIndex = prevIndex)
        // 自动播放当前词的音频
        playCurrentWordAudio()
    }
    
    /**
     * 完成听写会话
     */
    fun completeSession() {
        val currentState = _sessionState.value as? DictationSessionState.InProgress ?: return
        
        // 计算正确数量
        val correctCount = calculateCorrectAnswers(currentState.words, currentState.userInputs)
        
        _sessionState.value = DictationSessionState.Completed(
            words = currentState.words,
            userInputs = currentState.userInputs,
            correctCount = correctCount,
            lessonId = currentState.lessonId,
            lessonName = currentState.lessonName
        )
        
        // 保存听写记录
        val completedState = _sessionState.value as DictationSessionState.Completed
        saveDictationRecord(completedState)
    }
    
    /**
     * 计算正确答案数量
     */
    private fun calculateCorrectAnswers(words: List<Word>, userInputs: List<String>): Int {
        return words.zip(userInputs).count { (word, input) ->
            word.content.trim().equals(input.trim(), ignoreCase = true)
        }
    }
    
    /**
     * 保存听写记录
     */
    private fun saveDictationRecord(state: DictationSessionState.Completed) {
        val record = DictationRecord(
            id = "record_${System.currentTimeMillis()}",
            lessonId = state.lessonId,
            lessonName = state.lessonName,
            words = state.words,
            userInputs = state.userInputs,
            correctCount = state.correctCount,
            timestamp = System.currentTimeMillis()
        )
        
        viewModelScope.launch {
            repository.addDictationRecord(record)
        }
    }
    
    /**
     * 重置会话状态
     */
    fun resetSession() {
        _sessionState.value = DictationSessionState.Idle
    }
    
    /**
     * 更新用户设置
     */
    fun updateUserSettings(settings: UserSettings) {
        viewModelScope.launch {
            repository.updateUserSettings(settings)
        }
    }
}
