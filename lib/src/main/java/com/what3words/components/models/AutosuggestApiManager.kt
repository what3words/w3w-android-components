package com.what3words.components.models

import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.helpers.AutosuggestHelper
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AutosuggestApiManager(private val wrapper: What3WordsV3) : AutosuggestLogicManager {

    private val autosuggestHelper by lazy {
        AutosuggestHelper(wrapper)
    }

    override suspend fun autosuggest(
        query: String,
        options: AutosuggestOptions?
    ): AutosuggestLogicManager.Result<AutosuggestWithDidyouMean> = suspendCoroutine { cont ->
        if (options != null) autosuggestHelper.options(options)
        autosuggestHelper.update(query, {
            cont.resume(AutosuggestLogicManager.Result(AutosuggestWithDidyouMean(it, null)))
        }, {
            cont.resume(AutosuggestLogicManager.Result(it))
        }, {
            cont.resume(AutosuggestLogicManager.Result(AutosuggestWithDidyouMean(null, it)))
        })
    }

    override fun autosuggest(
        microphone: Microphone,
        options: AutosuggestOptions,
        voiceLanguage: String,
        callback: Consumer<List<Suggestion>>,
        errorCallback: Consumer<APIResponse.What3WordsError>?
    ): VoiceBuilder {
        return wrapper.autosuggest(microphone, voiceLanguage).apply {
            options.nResults?.let {
                this.nResults(it)
            }
            options.focus?.let {
                this.focus(it)
            }
            options.nFocusResults?.let {
                this.nFocusResults(it)
            }
            options.clipToCountry?.let {
                this.clipToCountry(it.toList())
            }
            options.clipToCircle?.let {
                this.clipToCircle(it, options.clipToCircleRadius ?: 0.0)
            }
            options.clipToBoundingBox?.let {
                this.clipToBoundingBox(it)
            }
            options.clipToPolygon?.let { coordinates ->
                this.clipToPolygon(coordinates.toList())
            }

            this.onSuggestions(callback)
            if (errorCallback != null) this.onError(errorCallback)
        }
    }

    override suspend fun selected(
        rawQuery: String,
        suggestion: Suggestion
    ): AutosuggestLogicManager.Result<SuggestionWithCoordinates> = suspendCoroutine { cont ->
        autosuggestHelper.selected(
            rawQuery,
            suggestion
        ) {
            cont.resume(AutosuggestLogicManager.Result(SuggestionWithCoordinates(it)))
        }
    }

    override suspend fun selectedWithCoordinates(
        rawQuery: String,
        suggestion: Suggestion
    ): AutosuggestLogicManager.Result<SuggestionWithCoordinates> = suspendCoroutine { cont ->
        autosuggestHelper.selectedWithCoordinates(
            rawQuery,
            suggestion,
            {
                cont.resume(AutosuggestLogicManager.Result(it))
            }, {
                cont.resume(AutosuggestLogicManager.Result(it))
            }
        )
    }

    override fun multipleWithCoordinates(
        rawQuery: String,
        suggestions: List<Suggestion>,
        callback: Consumer<List<SuggestionWithCoordinates>>,
        errorCallback: Consumer<APIResponse.What3WordsError>
    ) {
        val list = mutableListOf<SuggestionWithCoordinates>()
        var allSuccess = true
        suggestions.forEach {
            val res = wrapper.convertToCoordinates(it.words).execute()
            if (res.isSuccessful) {
                list.add(SuggestionWithCoordinates(it, res.coordinates))
            } else {
                allSuccess = false
                errorCallback.accept(res.error)
                return@forEach
            }
        }
        if (allSuccess) callback.accept(list)
    }
}