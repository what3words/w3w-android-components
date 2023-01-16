package com.what3words.components.models

import com.what3words.androidwrapper.What3WordsWrapper
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class AutosuggestRepository(private val wrapper: What3WordsWrapper) {

    suspend fun autosuggest(
        query: String,
        options: AutosuggestOptions?,
        allowFlexibleDelimiters: Boolean
    ): Either<APIResponse.What3WordsError, Pair<List<Suggestion>?, Suggestion?>> =
        suspendCoroutine { cont ->
            if (options != null) wrapper.helper.options(options)
            wrapper.helper.allowFlexibleDelimiters(allowFlexibleDelimiters)
            wrapper.helper.update(
                query,
                {
                    cont.resume(Either.Right(Pair(it, null)))
                },
                {
                    cont.resume(Either.Left(it))
                },
                {
                    cont.resume(Either.Right(Pair(null, it)))
                }
            )
        }

    suspend fun autosuggest(
        microphone: Microphone,
        options: AutosuggestOptions,
        voiceLanguage: String
    ): Either<APIResponse.What3WordsError, VoiceAutosuggestRepository> = suspendCoroutine { cont ->
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
        val voiceManager = VoiceAutosuggestRepository(builder)
        cont.resume(Either.Right(voiceManager))
    }

    suspend fun selected(
        rawQuery: String,
        suggestion: Suggestion
    ): Either<APIResponse.What3WordsError, SuggestionWithCoordinates> = suspendCoroutine { cont ->
        wrapper.helper.selected(
            rawQuery,
            suggestion
        ) {
            cont.resume(Either.Right(SuggestionWithCoordinates(it)))
        }
    }

    suspend fun selectedWithCoordinates(
        rawQuery: String,
        suggestion: Suggestion
    ): Either<APIResponse.What3WordsError, SuggestionWithCoordinates> = suspendCoroutine { cont ->
        wrapper.helper.selectedWithCoordinates(
            rawQuery,
            suggestion,
            {
                cont.resume(Either.Right(it))
            },
            {
                cont.resume(Either.Left(it))
            }
        )
    }

    suspend fun multipleWithCoordinates(
        rawQuery: String,
        suggestions: List<Suggestion>
    ): Either<APIResponse.What3WordsError, List<SuggestionWithCoordinates>> =
        suspendCoroutine { cont ->
            val list = mutableListOf<SuggestionWithCoordinates>()
            var allSuccess = true
            suggestions.forEach {
                val res = wrapper.convertToCoordinates(it.words).execute()
                if (res.isSuccessful) {
                    list.add(SuggestionWithCoordinates(it, res.coordinates))
                } else {
                    allSuccess = false
                    cont.resume(Either.Left(res.error))
                    return@forEach
                }
            }
            if (allSuccess) cont.resume(Either.Right(list))
            else cont.resume(Either.Left(APIResponse.What3WordsError.UNKNOWN_ERROR))
        }

    fun isVoiceEnabled(): Boolean {
        return true
    }
}
