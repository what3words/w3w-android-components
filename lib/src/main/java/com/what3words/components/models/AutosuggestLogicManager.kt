package com.what3words.components.models

import androidx.core.util.Consumer
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates

data class AutosuggestWithDidyouMean(
    val suggestions: List<Suggestion>?,
    val didYouMean: Suggestion?
)

interface AutosuggestLogicManager {

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

    suspend fun autosuggest(
        query: String,
        options: AutosuggestOptions?
    ): Result<AutosuggestWithDidyouMean>

    fun autosuggest(
        microphone: Microphone,
        options: AutosuggestOptions,
        voiceLanguage: String,
        callback: Consumer<List<Suggestion>>,
        errorCallback: Consumer<APIResponse.What3WordsError>?
    ): VoiceBuilder

    suspend fun selected(
        rawQuery: String,
        suggestion: Suggestion
    ) : Result<SuggestionWithCoordinates>

    suspend fun selectedWithCoordinates(
        rawQuery: String,
        suggestion: Suggestion
    ) : Result<SuggestionWithCoordinates>

    fun multipleWithCoordinates(
        rawQuery: String,
        suggestions: List<Suggestion>,
        callback: Consumer<List<SuggestionWithCoordinates>>,
        errorCallback: Consumer<APIResponse.What3WordsError>
    )
}
