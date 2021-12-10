package com.what3words.components.models

import com.what3words.androidwrapper.voice.Microphone
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates

interface AutosuggestLogicManager {

    suspend fun autosuggest(
        query: String,
        options: AutosuggestOptions?
    ): Either<APIResponse.What3WordsError, Pair<List<Suggestion>?, Suggestion?>>

    suspend fun autosuggest(
        microphone: Microphone,
        options: AutosuggestOptions,
        voiceLanguage: String
    ): Either<APIResponse.What3WordsError, VoiceAutosuggestManager>

    suspend fun selected(
        rawQuery: String,
        suggestion: Suggestion
    ): Either<APIResponse.What3WordsError, SuggestionWithCoordinates>

    suspend fun selectedWithCoordinates(
        rawQuery: String,
        suggestion: Suggestion
    ): Either<APIResponse.What3WordsError, SuggestionWithCoordinates>

    suspend fun multipleWithCoordinates(
        rawQuery: String,
        suggestions: List<Suggestion>
    ): Either<APIResponse.What3WordsError, List<SuggestionWithCoordinates>>

    fun isVoiceEnabled(): Boolean
}
