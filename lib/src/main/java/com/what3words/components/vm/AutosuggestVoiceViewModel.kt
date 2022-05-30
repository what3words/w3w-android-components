package com.what3words.components.vm

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal class AutosuggestVoiceViewModel(
    dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AutosuggestViewModel(dispatchers) {
    private var listeningJob: Job? = null
    private lateinit var microphone: Microphone

    internal var voiceManager: VoiceAutosuggestManager? = null

    private var _currentState: W3WListeningState? = null

    private val _listeningState = MutableSharedFlow<Pair<W3WListeningState, Boolean>>()
    val listeningState: SharedFlow<Pair<W3WListeningState, Boolean>>
        get() = _listeningState

    private val _multipleSelectedSuggestions = MutableSharedFlow<List<SuggestionWithCoordinates>>()
    val multipleSelectedSuggestions: SharedFlow<List<SuggestionWithCoordinates>>
        get() = _multipleSelectedSuggestions

    private val _volume = MutableSharedFlow<Float?>()
    val volume: SharedFlow<Float?>
        get() = _volume

    private var animationRefreshTime: Int = 50

    fun autosuggest(language: String) {
        if (voiceManager != null) {
            if (voiceManager!!.isListening()) {
                stopListening()
            } else {
                startListening()
            }
        } else {
            io(dispatchers) {
                val builder = manager.autosuggest(
                    microphone, options, language
                )

                when (builder) {
                    is Either.Left -> {
                        _error.emit(builder.a)
                    }
                    is Either.Right -> {
                        voiceManager = builder.b
                        startListening()
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
                            _error.emit(res.a)
                        }
                        is Either.Right -> {
                            _multipleSelectedSuggestions.emit(res.b)
                        }
                    }
                }
            } else {
                _multipleSelectedSuggestions.emit(
                    suggestions.map {
                        SuggestionWithCoordinates(
                            it
                        )
                    }
                )
            }
        }
    }

    fun startListening() {
        voiceManager?.let {
            listeningJob = io(dispatchers) {
                _currentState = W3WListeningState.Connecting
                _listeningState.emit(Pair(_currentState!!, false))
                it.updateOptions(options)
                when (val res = it.startListening()) {
                    is Either.Left -> {
                        _error.emit(res.a)
                        _currentState = W3WListeningState.Stopped
                        _listeningState.emit(Pair(_currentState!!, true))
                    }
                    is Either.Right -> {
                        _suggestions.emit(res.b)
                        _currentState = W3WListeningState.Stopped
                        _listeningState.emit(Pair(_currentState!!, false))
                    }
                }
            }
        }
    }

    fun stopListening() {
        voiceManager?.let {
            if (it.isListening()) {
                io(dispatchers) {
                    listeningJob?.cancel()
                    it.stopListening()
                    _currentState = W3WListeningState.Stopped
                    _listeningState.emit(Pair(_currentState!!, false))
                }
            }
        }
    }

    fun setPermissionError() {
        main(dispatchers) {
            _error.emit(
                APIResponse.What3WordsError.UNKNOWN_ERROR.apply {
                    message = "Microphone permission required"
                }
            )
        }
    }

    fun setCustomAnimationRefreshTime(newTime: Int) {
        animationRefreshTime = newTime
    }

    fun setMicrophone(customMicrophone: Microphone) {
        this.microphone = customMicrophone
        var oldTimestamp = System.currentTimeMillis()
        microphone.onListening {
            io(dispatchers) {
                if (_currentState != W3WListeningState.Started) {
                    _currentState = W3WListeningState.Started
                    _listeningState.emit(
                        Pair(
                            _currentState!!,
                            false
                        )
                    )
                }
                if (it != null && (System.currentTimeMillis() - oldTimestamp) > animationRefreshTime) {
                    oldTimestamp = System.currentTimeMillis()
                    _volume.emit(transform(it))
                }
            }
        }
        microphone.onError { microphoneError ->
            io(dispatchers) {
                _error.emit(
                    APIResponse.What3WordsError.UNKNOWN_ERROR.also {
                        it.message = microphoneError
                    }
                )
            }
        }
    }
}
