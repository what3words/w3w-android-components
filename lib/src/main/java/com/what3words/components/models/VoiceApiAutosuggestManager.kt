package com.what3words.components.models

import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.javawrapper.response.Suggestion
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface VoiceAutosuggestManager {
    fun isListening(): Boolean
    fun stopListening()
    suspend fun startListening(): Result<List<Suggestion>>
}

class VoiceApiAutosuggestManager(private val voiceBuilder: VoiceBuilder) : VoiceAutosuggestManager {
    override fun isListening(): Boolean {
        return voiceBuilder.isListening()
    }

    override fun stopListening() {
        voiceBuilder.stopListening()
    }

    override suspend fun startListening(): Result<List<Suggestion>> = suspendCoroutine { cont ->
        voiceBuilder.onSuggestions { suggestions ->
            cont.resume(Result(suggestions))
        }

        voiceBuilder.onError {
            cont.resume(Result(it))
        }

        voiceBuilder.startListening()
    }
}
