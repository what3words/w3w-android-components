package com.what3words.components.vm

import com.what3words.androidwrapper.helpers.DefaultDispatcherProvider
import com.what3words.androidwrapper.helpers.DispatcherProvider
import com.what3words.components.models.Either
import com.what3words.components.utils.io
import com.what3words.javawrapper.response.Suggestion
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal class AutosuggestTextViewModel(
    dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AutosuggestViewModel(dispatchers) {

    private val _didYouMean = MutableSharedFlow<Suggestion?>()
    val didYouMean: SharedFlow<Suggestion?>
        get() = _didYouMean

    fun autosuggest(searchText: String, allowFlexibleDelimiters: Boolean = false) {
        io(dispatchers) {
            val res = manager.autosuggest(
                searchText.replace("/", ""), options, allowFlexibleDelimiters
            )
            when (res) {
                is Either.Left -> {
                    _error.emit(res.a)
                }
                is Either.Right -> {
                    res.b.first?.let {
                        _suggestions.emit(it)
                    }
                    res.b.second?.let {
                        // didn't match regex pattern but did you mean is triggered, i.e: index home raft
                        _didYouMean.emit(it)
                    }
                }
            }
        }
    }
}
