package com.mewhear.app.platform

// Enum to represent language for TTS, can be expanded
enum class TTSLanguage {
    EN_US,
    ZH_CN 
    // Add other languages as needed e.g. EN_GB
}

interface TextToSpeechPlayer {
    fun play(text: String, language: TTSLanguage)
    fun stop()
    // Optional: fun isSpeaking(): Boolean
    // Optional: fun setSpeechRate(rate: Float)
    // Optional: fun setPitch(pitch: Float)
}

// Expect declaration for getting a TTS player instance
expect fun getTtsPlayer(): TextToSpeechPlayer
