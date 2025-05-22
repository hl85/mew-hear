package com.mewhear.app.platform

import android.speech.tts.TextToSpeech
import com.mewhear.app.AppContext
import java.util.Locale

class AndroidTextToSpeechPlayer : TextToSpeechPlayer, TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var initialized = false
    private var lastSpokenText: String? = null
    private var lastSpokenLanguage: TTSLanguage? = null

    init {
        tts = TextToSpeech(AppContext.context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            initialized = true
            // If there was a pending play request
            lastSpokenText?.let { text ->
                lastSpokenLanguage?.let { lang ->
                    playInternal(text, lang)
                }
            }
        } else {
            // Handle TTS initialization failure
            println("TTS Initialization Failed!")
        }
    }

    private fun playInternal(text: String, language: TTSLanguage) {
        val locale = when (language) {
            TTSLanguage.EN_US -> Locale.US
            TTSLanguage.ZH_CN -> Locale.SIMPLIFIED_CHINESE
            // Add other mappings as needed
        }
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun play(text: String, language: TTSLanguage) {
        if (initialized) {
            playInternal(text, language)
        } else {
            // Store for later playback if TTS not ready yet
            lastSpokenText = text
            lastSpokenLanguage = language
        }
    }

    override fun stop() {
        tts?.stop()
    }

    // Optional: Call this to release resources when no longer needed
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        initialized = false
    }
}

// Actual function to get a TTS player instance for Android
actual fun getTtsPlayer(): TextToSpeechPlayer {
    return AndroidTextToSpeechPlayer() // For now, a new instance each time.
                                     // Consider Singleton or DI later.
}
