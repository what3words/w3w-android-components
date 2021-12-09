package com.what3words.components.models

import com.what3words.androidwrapper.voice.Microphone
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates

interface AutosuggestLogicManager {

    suspend fun autosuggest(
        query: String,
        options: AutosuggestOptions?
    ): Result<Pair<List<Suggestion>?, Suggestion?>>

    suspend fun autosuggest(
        microphone: Microphone,
        options: AutosuggestOptions,
        voiceLanguage: String
    ): Result<VoiceAutosuggestManager>

    suspend fun selected(
        rawQuery: String,
        suggestion: Suggestion
    ): Result<SuggestionWithCoordinates>

    suspend fun selectedWithCoordinates(
        rawQuery: String,
        suggestion: Suggestion
    ): Result<SuggestionWithCoordinates>

    suspend fun multipleWithCoordinates(
        rawQuery: String,
        suggestions: List<Suggestion>
    ): Result<List<SuggestionWithCoordinates>>

    fun isVoiceEnabled(): Boolean
}
