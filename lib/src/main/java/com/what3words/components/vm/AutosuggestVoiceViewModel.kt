package com.what3words.components.vm

import androidx.lifecycle.MutableLiveData
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.components.models.DispatcherProvider
import com.what3words.components.models.VoiceAutosuggestManager
import com.what3words.components.models.W3WListeningState
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class AutosuggestVoiceViewModel(
    dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AutosuggestViewModel(dispatchers) {
    lateinit var microphone: Microphone
    val voiceManager = MutableLiveData<VoiceAutosuggestManager?>()
    val listeningState = MutableLiveData<W3WListeningState>()
    val multipleSelectedSuggestions = MutableLiveData<List<SuggestionWithCoordinates>>()

    fun autosuggest(language: String) {
        CoroutineScope(dispatchers.io()).launch {
            if (voiceManager.value?.isListening() == true) {
                voiceManager.value?.stopListening()
                CoroutineScope(dispatchers.main()).launch {
                    listeningState.value = W3WListeningState.Stopped
                }
                return@launch
            }

            val builder = manager.autosuggest(
                microphone, options, language
            )

            CoroutineScope(dispatchers.main()).launch {
                if (builder.isSuccessful()) {
                    this@AutosuggestVoiceViewModel.voiceManager.value = builder.data()
                } else {
                    this@AutosuggestVoiceViewModel.error.value = builder.error()
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

    fun startListening() {
        voiceManager.value?.let {
            CoroutineScope(dispatchers.io()).launch {
                it.updateOptions(options)
                val res = it.startListening()
                CoroutineScope(dispatchers.main()).launch {
                    if (res.isSuccessful()) {
                        suggestions.value = res.data()
                    } else error.value = res.error()
                }
            }
        }
    }

    fun stopListening() {
        voiceManager.value?.let {
            CoroutineScope(dispatchers.io()).launch {
                it.stopListening()
            }
        }
    }
}
