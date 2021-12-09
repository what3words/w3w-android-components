package com.what3words.components.vm

import androidx.lifecycle.MutableLiveData
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.components.models.DispatcherProvider
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DefaultDispatcherProvider : DispatcherProvider

internal open class AutosuggestViewModel(
    val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) {
    lateinit var manager: AutosuggestLogicManager
    val suggestions = MutableLiveData<List<Suggestion>>()
    val selectedSuggestion = MutableLiveData<SuggestionWithCoordinates>()
    val error = MutableLiveData<APIResponse.What3WordsError?>()
    var options: AutosuggestOptions = AutosuggestOptions()

    fun onSuggestionClicked(rawQuery: String, suggestion: Suggestion?, returnCoordinates: Boolean) {
        CoroutineScope(dispatchers.io()).launch {
            if (suggestion == null) {
                CoroutineScope(dispatchers.main()).launch {
                    selectedSuggestion.value = null
                }
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
}

internal class AutosuggestTextViewModel(
    dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AutosuggestViewModel(dispatchers) {

    val didYouMean = MutableLiveData<Suggestion?>()

    fun autosuggest(searchText: String) {
        CoroutineScope(dispatchers.io()).launch {
            val res = manager.autosuggest(
                searchText.replace("/", ""), options
            )
            if (res.isSuccessful() && res.data() != null) {
                CoroutineScope(dispatchers.main()).launch {
                    res.data()!!.first?.let {
                        suggestions.value = it
                    }
                    res.data()!!.second?.let {
                        didYouMean.value = it
                    }
                }
            } else if (!res.isSuccessful() && res.error() != null) {
                CoroutineScope(dispatchers.main()).launch {
                    this@AutosuggestTextViewModel.error.value = res.error()
                }
            }
        }
    }
}
