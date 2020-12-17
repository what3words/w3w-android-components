package com.what3words.autosuggest.picker

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.autosuggest.R
import com.what3words.autosuggest.text.SuggestionsListPosition
import com.what3words.autosuggest.text.handleSelectionTrack
import com.what3words.autosuggest.utils.MyDividerItemDecorator
import com.what3words.autosuggest.voice.W3WSuggestion
import com.what3words.javawrapper.response.Suggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class W3WAutoSuggestPicker
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var queryMap: Map<String, String> = emptyMap()
    private var key: String = ""
    private var isEnterprise: Boolean = false
    private var returnCoordinates: Boolean = false
    private var wrapper: What3WordsV3? = null
    private var callback: ((suggestion: W3WSuggestion?) -> Unit)? =
        null
    private var internalCallback: ((suggestion: W3WSuggestion?) -> Unit)? =
        null

    private val suggestionsAdapter: SuggestionsAdapter by lazy {
        SuggestionsAdapter(
            Typeface.DEFAULT,
            R.color.w3wBlue
        ) { suggestion ->
            handleAddressPicked(suggestion)
        }
    }

    private fun handleAddressPicked(
        suggestion: Suggestion?
    ) {
        suggestionsAdapter.refreshSuggestions(emptyList(), null)
        visibility = GONE
        if (suggestion == null) {
            callback?.invoke(null)
            internalCallback?.invoke(null)
        } else {
            internalCallback?.invoke(W3WSuggestion(suggestion))
            if (!isEnterprise) handleSelectionTrack(suggestion, "", queryMap, key)
            if (!returnCoordinates) {
                callback?.invoke(W3WSuggestion(suggestion))
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val res = wrapper!!.convertToCoordinates(suggestion.words).execute()
                    CoroutineScope(Dispatchers.Main).launch {
                        callback?.invoke(W3WSuggestion(suggestion, res.coordinates))
                    }
                }
            }
        }
    }

    init {
        val linear = LinearLayoutManager(context)
        layoutManager = linear
        setHasFixedSize(true)
        background = AppCompatResources.getDrawable(context, R.drawable.bg_white_border_gray)
        resources.getDimensionPixelSize(R.dimen.tiny_margin).let {
            setPadding(it, it, it, it)
        }
        ResourcesCompat.getDrawable(resources, R.drawable.divider, null)?.let {
            addItemDecoration(
                MyDividerItemDecorator(
                    it,
                    SuggestionsListPosition.BELOW
                )
            )
        }
        adapter = suggestionsAdapter
        visibility = GONE
    }

    fun onSelected(callback: (selectedSuggestion: W3WSuggestion?) -> Unit): W3WAutoSuggestPicker {
        this.callback = callback
        return this
    }

    internal fun internalCallback(callback: (selectedSuggestion: W3WSuggestion?) -> Unit): W3WAutoSuggestPicker {
        this.internalCallback = callback
        return this
    }

    internal fun setup(
        wrapper: What3WordsV3,
        isEnterprise: Boolean,
        key: String
    ) {
        this.isEnterprise = isEnterprise
        this.key = key
        this.wrapper = wrapper
    }

    internal fun refreshSuggestions(
        suggestions: List<Suggestion>,
        query: String?,
        queryMap: Map<String, String> = emptyMap(),
        returnCoordinates: Boolean
    ) {
        suggestionsAdapter.refreshSuggestions(suggestions, query)
        this.returnCoordinates = returnCoordinates
        this.queryMap = queryMap
    }
}