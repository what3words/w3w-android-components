package com.what3words.components.models

import android.Manifest
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.intentfilter.androidpermissions.PermissionManager
import com.intentfilter.androidpermissions.models.DeniedPermissions
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
import java.util.*

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

    fun voiceAutosuggest(language: String, context: Context) {
        if (builder.value?.isListening() == true) {
            builder.value?.stopListening()
            listeningState.value = W3WListeningState.Stopped
            return
        }

        val permissionManager: PermissionManager = PermissionManager.getInstance(context)
        permissionManager.checkPermissions(
            Collections.singleton(Manifest.permission.RECORD_AUDIO),
            object : PermissionManager.PermissionRequestListener {
                override fun onPermissionGranted() {
                    builder.value = manager!!.autosuggest(
                        microphone, options, language,
                        { suggestions ->
                            CoroutineScope(Dispatchers.Main).launch {
                                voiceSuggestions.value = suggestions
                                voiceError.value = null
                            }
                        },
                        {
                            CoroutineScope(Dispatchers.Main).launch {
                                error.value = it
                            }
                        }
                    )
                }

                override fun onPermissionDenied(deniedPermissions: DeniedPermissions) {
                    voiceError.value = APIResponse.What3WordsError.UNKNOWN_ERROR.apply {
                        message = "Microphone permission required"
                    }
                }
            }
        )
    }

    fun onSuggestionClicked(rawQuery: String, suggestion: Suggestion?, returnCoordinates: Boolean) {
        CoroutineScope(dispatchers.io()).launch {
            if (suggestion == null) {
                selectedSuggestion.value = null
            } else if (!returnCoordinates) {
                val res = manager.selected(rawQuery, suggestion)
                if (res.isSuccessful()) {
                    selectedSuggestion.value = res.data()
                }
            } else {
                val res = manager.selectedWithCoordinates(rawQuery, suggestion)
                if (res.isSuccessful()) {
                    selectedSuggestion.value = res.data()
                } else {
                    error.value = res.error()
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
            CoroutineScope(Dispatchers.IO).launch {
                manager?.multipleWithCoordinates("", suggestions, {
                    CoroutineScope(Dispatchers.Main).launch {
                        multipleSelectedSuggestions.value = it
                    }
                }, {
                    CoroutineScope(Dispatchers.Main).launch {
                        error.value = it
                    }
                })
            }
        } else {
            multipleSelectedSuggestions.value = suggestions.map { SuggestionWithCoordinates(it) }
        }
    }
}