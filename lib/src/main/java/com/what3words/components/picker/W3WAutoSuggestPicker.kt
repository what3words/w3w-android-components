package com.what3words.components.picker

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.components.R
import com.what3words.components.text.AutoSuggestOptions
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.components.text.handleSelectionTrack
import com.what3words.components.utils.DisplayUnits
import com.what3words.components.utils.MyDividerItemDecorator
import com.what3words.components.utils.W3WSuggestion
import com.what3words.javawrapper.response.Suggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A [RecyclerView] to show [W3WSuggestion] returned by w3w auto suggest component
 * modularized to allow developers to choose picker location on the screen.
 */
class W3WAutoSuggestPicker
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var options: AutoSuggestOptions = AutoSuggestOptions()
    private var displayUnits: DisplayUnits = DisplayUnits.SYSTEM
    private var key: String = ""
    private var isEnterprise: Boolean = false
    private var returnCoordinates: Boolean = false
    private var query: String = ""
    private var wrapper: What3WordsV3? = null
    private var callback: ((suggestion: W3WSuggestion?) -> Unit)? =
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
        suggestionsAdapter.refreshSuggestions(emptyList(), null, displayUnits)
        visibility = GONE
        if (suggestion == null) {
            callback?.invoke(null)
        } else {
            if (!isEnterprise && wrapper != null) handleSelectionTrack(
                suggestion,
                query,
                options,
                wrapper!!
            )
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
        val linear = LinearLayoutManager(context, attrs, defStyleAttr, 0)
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
                    (layoutManager as LinearLayoutManager).reverseLayout
                )
            )
        }
        adapter = suggestionsAdapter
        visibility = GONE
    }

    internal fun internalCallback(callback: (selectedSuggestion: W3WSuggestion?) -> Unit): W3WAutoSuggestPicker {
        this.callback = callback
        return this
    }

    internal fun setup(
        wrapper: What3WordsV3,
        isEnterprise: Boolean,
        key: String,
        displayUnits: DisplayUnits
    ) {
        this.isEnterprise = isEnterprise
        this.key = key
        this.wrapper = wrapper
        this.displayUnits = displayUnits
    }

    internal fun refreshSuggestions(
        suggestions: List<Suggestion>,
        query: String?,
        options: AutoSuggestOptions,
        returnCoordinates: Boolean
    ) {
        suggestionsAdapter.refreshSuggestions(suggestions, query, displayUnits)
        this.returnCoordinates = returnCoordinates
        this.query = query ?: ""
        this.options = options
    }

    internal fun forceClear() {
        suggestionsAdapter.refreshSuggestions(emptyList(), "", displayUnits)
        visibility = GONE
    }
}