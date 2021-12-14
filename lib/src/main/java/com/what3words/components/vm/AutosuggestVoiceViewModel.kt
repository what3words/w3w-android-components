package com.what3words.components.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.what3words.androidwrapper.helpers.DefaultDispatcherProvider
import com.what3words.androidwrapper.helpers.DispatcherProvider
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.components.models.Either
import com.what3words.components.models.VoiceAutosuggestManager
import com.what3words.components.models.W3WListeningState
import com.what3words.components.utils.io
import com.what3words.components.utils.main
import com.what3words.components.utils.transform
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates

internal class AutosuggestVoiceViewModel(
    dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AutosuggestViewModel(dispatchers) {
    private lateinit var microphone: Microphone

    private val _voiceManager = MutableLiveData<VoiceAutosuggestManager?>()
    val voiceManager: LiveData<VoiceAutosuggestManager?>
        get() = _voiceManager

    private val _listeningState = MutableLiveData<W3WListeningState>()
    val listeningState: LiveData<W3WListeningState>
        get() = _listeningState

    private val _multipleSelectedSuggestions = MutableLiveData<List<SuggestionWithCoordinates>>()
    val multipleSelectedSuggestions: LiveData<List<SuggestionWithCoordinates>>
        get() = _multipleSelectedSuggestions

    private val _volume = MutableLiveData<Float?>()
    val volume: LiveData<Float?>
        get() = _volume

    private var animationRefreshTime: Int = 50

    fun autosuggest(language: String) {
        io(dispatchers) launch@{
            if (voiceManager.value?.isListening() == true) {
                voiceManager.value?.stopListening()
                main(dispatchers) {
                    _listeningState.postValue(W3WListeningState.Stopped)
                }
                return@launch
            }

            val builder = manager.autosuggest(
                microphone, options, language
            )

            main(dispatchers) {
                when (builder) {
                    is Either.Left -> {
                        _error.postValue(builder.a)
                    }
                    is Either.Right -> {
                        _listeningState.postValue(W3WListeningState.Connecting)
                        _voiceManager.postValue(builder.b)
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
        io(dispatchers) {
            if (returnCoordinates) {
                val res = manager.multipleWithCoordinates(rawQuery, suggestions)
                main(dispatchers) {
                    when (res) {
                        is Either.Left -> {
                            _error.postValue(res.a)
                        }
                        is Either.Right -> {
                            _multipleSelectedSuggestions.postValue(res.b)
                        }
                    }
                }
            } else {
                main(dispatchers) {
                    _multipleSelectedSuggestions.postValue(
                        suggestions.map {
                            SuggestionWithCoordinates(
                                it
                            )
                        }
                    )
                }
            }
            main(dispatchers) {
                _listeningState.postValue(W3WListeningState.Stopped)
            }
        }
    }

    fun startListening() {
        voiceManager.value?.let {
            io(dispatchers) {
                it.updateOptions(options)
                val res = it.startListening()
                main(dispatchers) {
                    when (res) {
                        is Either.Left -> {
                            _error.postValue(res.a)
                        }
                        is Either.Right -> {
                            _suggestions.postValue(res.b)
                        }
                    }
                }
            }
        }
    }

    fun stopListening() {
        voiceManager.value?.let {
            if (it.isListening()) {
                io(dispatchers) {
                    it.stopListening()
                    microphone.onListening {}
                    main(dispatchers) {
                        _listeningState.postValue(W3WListeningState.Stopped)
                    }
                }
            }
        }
    }

    fun setPermissionError() {
        _error.postValue(
            APIResponse.What3WordsError.UNKNOWN_ERROR.apply {
                message = "Microphone permission required"
            }
        )
    }

    fun setCustomAnimationRefreshTime(newTime: Int) {
        animationRefreshTime = newTime
    }

    fun setMicrophone(customMicrophone: Microphone) {
        this.microphone = customMicrophone
        var oldTimestamp = System.currentTimeMillis()
        microphone.onListening {
            if (_listeningState.value != W3WListeningState.Started) _listeningState.postValue(
                W3WListeningState.Started
            )
            if (it != null) {
                if ((System.currentTimeMillis() - oldTimestamp) > animationRefreshTime) {
                    oldTimestamp = System.currentTimeMillis()
                    _volume.postValue(transform(it))
                }
            }
        }
        microphone.onError { microphoneError ->
            _error.postValue(
                APIResponse.What3WordsError.UNKNOWN_ERROR.also {
                    it.message = microphoneError
                }
            )
        }
    }
}
