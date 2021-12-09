package com.what3words.components.models

import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.helpers.AutosuggestHelper
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.javawrapper.request.AutosuggestOptions
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
    ): Result<Pair<List<Suggestion>?, Suggestion?>> = suspendCoroutine { cont ->
        if (options != null) autosuggestHelper.options(options)
        autosuggestHelper.update(
            query,
            {
                cont.resume(Result(Pair(it, null)))
            },
            {
                cont.resume(Result(it))
            },
            {
                cont.resume(Result(Pair(null, it)))
            }
        )
    }

    override suspend fun autosuggest(
        microphone: Microphone,
        options: AutosuggestOptions,
        voiceLanguage: String
    ): Result<VoiceAutosuggestManager> = suspendCoroutine { cont ->
        val builder = wrapper.autosuggest(microphone, voiceLanguage).apply {
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
        }
        val voiceManager = VoiceApiAutosuggestManager(builder)
        cont.resume(Result(voiceManager))
    }

    override suspend fun selected(
        rawQuery: String,
        suggestion: Suggestion
    ): Result<SuggestionWithCoordinates> = suspendCoroutine { cont ->
        autosuggestHelper.selected(
            rawQuery,
            suggestion
        ) {
            cont.resume(Result(SuggestionWithCoordinates(it)))
        }
    }

    override suspend fun selectedWithCoordinates(
        rawQuery: String,
        suggestion: Suggestion
    ): Result<SuggestionWithCoordinates> = suspendCoroutine { cont ->
        autosuggestHelper.selectedWithCoordinates(
            rawQuery,
            suggestion,
            {
                cont.resume(Result(it))
            },
            {
                cont.resume(Result(it))
            }
        )
    }

    override suspend fun multipleWithCoordinates(
        rawQuery: String,
        suggestions: List<Suggestion>
    ): Result<List<SuggestionWithCoordinates>> = suspendCoroutine { cont ->
        val list = mutableListOf<SuggestionWithCoordinates>()
        var allSuccess = true
        suggestions.forEach {
            val res = wrapper.convertToCoordinates(it.words).execute()
            if (res.isSuccessful) {
                list.add(SuggestionWithCoordinates(it, res.coordinates))
            } else {
                allSuccess = false
                cont.resume(Result(res.error))
                return@forEach
            }
        }
        if (allSuccess) cont.resume(Result(list))
    }

    override fun isVoiceEnabled(): Boolean {
        return true
    }
}
