package com.what3words.components.vm

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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal open class AutosuggestViewModel(
    val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) {
    lateinit var manager: AutosuggestLogicManager

    protected val _suggestions = MutableSharedFlow<List<Suggestion>>()
    val suggestions: SharedFlow<List<Suggestion>>
        get() = _suggestions

    protected val _selectedSuggestion = MutableSharedFlow<SuggestionWithCoordinates?>()
    val selectedSuggestion: SharedFlow<SuggestionWithCoordinates?>
        get() = _selectedSuggestion

    protected val _error = MutableSharedFlow<APIResponse.What3WordsError?>()
    val error: SharedFlow<APIResponse.What3WordsError?>
        get() = _error

    var options: AutosuggestOptions = AutosuggestOptions()

    fun onSuggestionSet(suggestionWithCoordinates: SuggestionWithCoordinates) {
        io(dispatchers) { _selectedSuggestion.emit(suggestionWithCoordinates) }
    }

    fun onSuggestionClicked(rawQuery: String, suggestion: Suggestion?, returnCoordinates: Boolean) {
        io(dispatchers) {
            if (suggestion == null) {
                // invalid suggestion picked
                main(dispatchers) {
                    _selectedSuggestion.emit(null)
                }
            } else if (!returnCoordinates) {
                // valid suggestion picked with no coordinates
                io(dispatchers) {
                    when (val res = manager.selected(rawQuery, suggestion)) {
                        is Either.Left -> {
                            _error.emit(res.a)
                        }
                        is Either.Right -> {
                            _selectedSuggestion.emit(res.b)
                        }
                    }
                }
            } else {
                // valid suggestion picked with coordinates
                when (val res = manager.selectedWithCoordinates(rawQuery, suggestion)) {
                    is Either.Left -> {
                        _error.emit(res.a)
                    }
                    is Either.Right -> {
                        _selectedSuggestion.emit(res.b)
                    }
                }
            }
        }
    }
}
