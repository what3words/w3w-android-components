package com.what3words.components.picker

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.what3words.components.R
import com.what3words.components.models.AutosuggestViewModel
import com.what3words.components.models.DisplayUnits
import com.what3words.components.utils.MyDividerItemDecorator
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.Suggestion

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

    private var options: AutosuggestOptions = AutosuggestOptions()
    private var displayUnits: DisplayUnits = DisplayUnits.SYSTEM
    private var returnCoordinates: Boolean = false
    private var query: String = ""
    private var viewModel: AutosuggestViewModel? = null

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
        viewModel?.onSuggestionClicked(query, suggestion, returnCoordinates)
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

    internal fun setup(
        viewModel: AutosuggestViewModel,
        displayUnits: DisplayUnits
    ) {
        this.viewModel = viewModel
        this.displayUnits = displayUnits
    }

    internal fun refreshSuggestions(
        suggestions: List<Suggestion>,
        query: String?,
        options: AutosuggestOptions,
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
