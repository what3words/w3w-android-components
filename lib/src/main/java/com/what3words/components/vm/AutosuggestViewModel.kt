package com.what3words.components.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.what3words.androidwrapper.helpers.DefaultDispatcherProvider
import com.what3words.androidwrapper.helpers.DispatcherProvider
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.components.models.Either
import com.what3words.components.utils.io
import com.what3words.components.utils.main
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates

internal open class AutosuggestViewModel(
    val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) {
    lateinit var manager: AutosuggestLogicManager

    protected val _suggestions = MutableLiveData<List<Suggestion>>()
    val suggestions: LiveData<List<Suggestion>>
        get() = _suggestions

    protected val _selectedSuggestion = MutableLiveData<SuggestionWithCoordinates>()
    val selectedSuggestion: LiveData<SuggestionWithCoordinates>
        get() = _selectedSuggestion

    protected val _error = MutableLiveData<APIResponse.What3WordsError?>()
    val error: LiveData<APIResponse.What3WordsError?>
        get() = _error

    var options: AutosuggestOptions = AutosuggestOptions()

    fun onSuggestionClicked(rawQuery: String, suggestion: Suggestion?, returnCoordinates: Boolean) {
        io(dispatchers) {
            if (suggestion == null) {
                //invalid suggestion picked
                main(dispatchers) {
                    _selectedSuggestion.postValue(null)
                }
            } else if (!returnCoordinates) {
                //valid suggestion picked with no coordinates
                val res = manager.selected(rawQuery, suggestion)
                main(dispatchers) {
                    when (res) {
                        is Either.Left -> {
                            _error.postValue(res.a)
                        }
                        is Either.Right -> {
                            _selectedSuggestion.postValue(res.b)
                        }
                    }
                }
            } else {
                //valid suggestion picked with coordinates
                val res = manager.selectedWithCoordinates(rawQuery, suggestion)
                main(dispatchers) {
                    when (res) {
                        is Either.Left -> {
                            _error.postValue(res.a)
                        }
                        is Either.Right -> {
                            _selectedSuggestion.postValue(res.b)
                        }
                    }
                }
            }
        }
    }
}