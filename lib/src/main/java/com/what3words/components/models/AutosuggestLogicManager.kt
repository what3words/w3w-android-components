package com.what3words.components.models

import com.what3words.androidwrapper.voice.Microphone
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates

data class AutosuggestWithDidyouMean(
    val suggestions: List<Suggestion>?,
    val didYouMean: Suggestion?
)

class Result<T> {
    private var error: APIResponse.What3WordsError? = null
    private var data: T? = null

    constructor(data: T) {
        this.data = data
    }

    constructor(error: APIResponse.What3WordsError) {
        this.error = error
    }

    fun isSuccessful() = this.data != null && error == null

    fun error(): APIResponse.What3WordsError? = this.error

    fun data(): T? = this.data
}

interface AutosuggestLogicManager {

    suspend fun autosuggest(
        query: String,
        options: AutosuggestOptions?
    ): Result<AutosuggestWithDidyouMean>

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
