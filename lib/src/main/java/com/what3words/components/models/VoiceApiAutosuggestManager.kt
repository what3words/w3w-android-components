package com.what3words.components.models

import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface VoiceAutosuggestManager {
    fun isListening(): Boolean
    fun stopListening()
    fun updateOptions(options: AutosuggestOptions)
    suspend fun startListening(): Either<APIResponse.What3WordsError, List<Suggestion>>
}

internal class VoiceApiAutosuggestManager(private val voiceBuilder: VoiceBuilder) : VoiceAutosuggestManager {
    override fun isListening(): Boolean {
        return voiceBuilder.isListening()
    }

    override fun stopListening() {
        voiceBuilder.stopListening()
    }

    override fun updateOptions(options: AutosuggestOptions) {
        voiceBuilder.apply {
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
    }

    override suspend fun startListening(): Either<APIResponse.What3WordsError, List<Suggestion>> =
        suspendCoroutine { cont ->
            voiceBuilder.onSuggestions { suggestions ->
                cont.resume(Either.Right(suggestions))
            }

            voiceBuilder.onError {
                cont.resume(Either.Left(it))
            }

            voiceBuilder.startListening()
        }
}
