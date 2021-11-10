package com.what3words.components.models

import androidx.lifecycle.MutableLiveData
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface DispatcherProvider {

    fun main(): CoroutineDispatcher = Dispatchers.Main
    fun default(): CoroutineDispatcher = Dispatchers.Default
    fun io(): CoroutineDispatcher = Dispatchers.IO
    fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}

class DefaultDispatcherProvider : DispatcherProvider

internal class AutosuggestViewModel(
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) {

    lateinit var microphone: Microphone
    lateinit var manager: AutosuggestLogicManager
    var builder = MutableLiveData<VoiceBuilder?>()
    val suggestions = MutableLiveData<List<Suggestion>>()
    val error = MutableLiveData<APIResponse.What3WordsError?>()
    val voiceError = MutableLiveData<APIResponse.What3WordsError?>()
    val didYouMean = MutableLiveData<Suggestion?>()
    val listeningState = MutableLiveData<W3WListeningState>()
    val voiceSuggestions = MutableLiveData<List<Suggestion>>()
    val selectedSuggestion = MutableLiveData<SuggestionWithCoordinates>()
    val multipleSelectedSuggestions = MutableLiveData<List<SuggestionWithCoordinates>>()

    val options: AutosuggestOptions by lazy {
        AutosuggestOptions()
    }

    fun autosuggest(searchText: String) {
        CoroutineScope(dispatchers.io()).launch {
            val res = manager.autosuggest(
                searchText.replace("/", ""), options
            )
            if (res.isSuccessful() && res.data() != null) {
                CoroutineScope(dispatchers.main()).launch {
                    res.data()!!.suggestions?.let {
                        suggestions.value = it
                    }
                    res.data()!!.didYouMean?.let {
                        didYouMean.value = it
                    }
                }
            } else if (!res.isSuccessful() && res.error() != null) {
                CoroutineScope(dispatchers.main()).launch {
                    this@AutosuggestViewModel.error.value = res.error()
                }
            }
        }
    }

    fun voiceAutosuggest(language: String) {
        CoroutineScope(dispatchers.io()).launch {
            if (builder.value?.isListening() == true) {
                builder.value?.stopListening()
                listeningState.value = W3WListeningState.Stopped
                return@launch
            }

            val builder = manager.autosuggest(
                microphone, options, language
            )

            builder.onSuggestions { suggestions ->
                CoroutineScope(dispatchers.main()).launch {
                    voiceSuggestions.value = suggestions
                    voiceError.value = null
                }
            }

            builder.onError {
                CoroutineScope(dispatchers.main()).launch {
                    error.value = it
                }
            }

            CoroutineScope(dispatchers.main()).launch {
                this@AutosuggestViewModel.builder.value = builder
            }
        }
    }

    fun onSuggestionClicked(rawQuery: String, suggestion: Suggestion?, returnCoordinates: Boolean) {
        CoroutineScope(dispatchers.io()).launch {
            if (suggestion == null) {
                selectedSuggestion.value = null
            } else if (!returnCoordinates) {
                val res = manager.selected(rawQuery, suggestion)
                if (res.isSuccessful()) {
                    CoroutineScope(dispatchers.main()).launch {
                        selectedSuggestion.value = res.data()
                    }
                }
            } else {
                val res = manager.selectedWithCoordinates(rawQuery, suggestion)
                CoroutineScope(dispatchers.main()).launch {
                    if (res.isSuccessful()) {
                        selectedSuggestion.value = res.data()
                    } else {
                        error.value = res.error()
                    }
                }
            }
        }
    }

    fun onMultipleSuggestionsSelected(
        rawQuery: String,
        suggestions: List<Suggestion>,
        returnCoordinates: Boolean
    ) {
        if (returnCoordinates) {
            CoroutineScope(dispatchers.io()).launch {
                val res = manager.multipleWithCoordinates(rawQuery, suggestions)
                CoroutineScope(dispatchers.main()).launch {
                    if (res.isSuccessful()) {
                        multipleSelectedSuggestions.value = res.data()
                    } else {
                        error.value = res.error()
                    }
                }
            }
        } else {
            multipleSelectedSuggestions.value = suggestions.map { SuggestionWithCoordinates(it) }
        }
    }
}
