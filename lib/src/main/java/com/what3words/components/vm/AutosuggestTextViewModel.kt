package com.what3words.components.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.what3words.androidwrapper.helpers.DefaultDispatcherProvider
import com.what3words.androidwrapper.helpers.DispatcherProvider
import com.what3words.components.models.Either
import com.what3words.components.utils.io
import com.what3words.components.utils.main
import com.what3words.javawrapper.response.Suggestion

internal class AutosuggestTextViewModel(
    dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AutosuggestViewModel(dispatchers) {

    private val _didYouMean = MutableLiveData<Suggestion?>()
    val didYouMean: LiveData<Suggestion?>
        get() = _didYouMean

    fun autosuggest(searchText: String) {
        io(dispatchers) {
            val res = manager.autosuggest(
                searchText.replace("/", ""), options
            )
            main(dispatchers) {
                when (res) {
                    is Either.Left -> {
                        _error.postValue(res.a)
                    }
                    is Either.Right -> {
                        res.b.first?.let {
                            _suggestions.postValue(it)
                        }
                        res.b.second?.let {
                            //didn't match regex pattern but did you mean is triggered, i.e: index home raft
                            _didYouMean.postValue(it)
                        }
                    }
                }
            }
        }
    }
}
