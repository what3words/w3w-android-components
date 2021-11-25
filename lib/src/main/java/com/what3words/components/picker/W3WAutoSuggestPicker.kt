package com.what3words.components.picker

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
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
    defStyleAttr: Int = R.attr.customW3WAutoSuggestPickerStyle
) : RecyclerView(
    ContextThemeWrapper(context, R.style.W3WAutoSuggestPickerTheme),
    attrs,
    defStyleAttr
) {

    private var subtitleTextColor: Int
    private var titleTextColor: Int
    private var subtitleTextSize: Int
    private var titleTextSize: Int
    private var options: AutosuggestOptions = AutosuggestOptions()
    private var displayUnits: DisplayUnits = DisplayUnits.SYSTEM
    private var returnCoordinates: Boolean = false
    private var query: String = ""
    private var viewModel: AutosuggestViewModel? = null
    internal var itemBackgroundDrawable: Drawable? = null
    internal var itemBackgroundColor: Int
    private var suggestionsAdapter: SuggestionsAdapter

    private fun handleAddressPicked(
        suggestion: Suggestion?
    ) {
        suggestionsAdapter.refreshSuggestions(emptyList(), null, displayUnits)
        visibility = GONE
        viewModel?.onSuggestionClicked(query, suggestion, returnCoordinates)
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.W3WAutoSuggestPicker,
            defStyleAttr, R.style.W3WAutoSuggestPickerTheme
        ).apply {
            try {
                val linear = LinearLayoutManager(context, attrs, defStyleAttr, 0)
                layoutManager = linear
                setHasFixedSize(true)

                resources.getDimensionPixelSize(R.dimen.tiny_margin).let {
                    setPadding(it, it, it, it)
                }

                val itemBackgroundDrawableId = getResourceId(
                    R.styleable.W3WAutoSuggestPicker_pickerItemBackgroundDrawable,
                    -1
                )

                if (itemBackgroundDrawableId != -1) {
                    itemBackgroundDrawable =
                        ContextCompat.getDrawable(context, itemBackgroundDrawableId)
                }

                val backgroundDrawableId = getResourceId(
                    R.styleable.W3WAutoSuggestPicker_pickerBackgroundDrawable,
                    -1
                )
                if (backgroundDrawableId != -1) {
                    background =
                        ContextCompat.getDrawable(context, backgroundDrawableId)
                }
                val dividerDrawableId = getResourceId(
                    R.styleable.W3WAutoSuggestPicker_pickerDivider,
                    R.drawable.divider
                )

                val itemSpacing = getDimension(
                    R.styleable.W3WAutoSuggestPicker_pickerItemSpacing,
                    0f
                )

                titleTextSize =
                    getDimensionPixelSize(
                        R.styleable.W3WAutoSuggestPicker_pickerItemTitleTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.default_text)
                    )

                subtitleTextSize =
                    getDimensionPixelSize(
                        R.styleable.W3WAutoSuggestPicker_pickerItemSubtitleTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.secondary_text)

                    )

                titleTextColor =
                    getColor(
                        R.styleable.W3WAutoSuggestPicker_pickerItemTitleTextColor,
                        context.getColor(R.color.w3wRed)
                    )

                subtitleTextColor =
                    getColor(
                        R.styleable.W3WAutoSuggestPicker_pickerItemSubtitleTextColor,
                        context.getColor(R.color.w3wGray)
                    )

                itemBackgroundColor = getColor(
                    R.styleable.W3WAutoSuggestPicker_pickerItemBackground,
                    ContextCompat.getColor(context, R.color.white)
                )

                val dividerVisible = getBoolean(
                    R.styleable.W3WAutoSuggestPicker_pickerDividerVisible,
                    true
                )

                ResourcesCompat.getDrawable(resources, dividerDrawableId, null)?.let {
                    addItemDecoration(
                        MyDividerItemDecorator(
                            if (dividerVisible) it else null,
                            itemSpacing,
                            (layoutManager as LinearLayoutManager).reverseLayout
                        )
                    )
                }
                suggestionsAdapter = SuggestionsAdapter(
                    Typeface.DEFAULT,
                    R.color.w3wBlue,
                    itemBackgroundDrawable,
                    itemBackgroundColor,
                    { suggestion ->
                        handleAddressPicked(suggestion)
                    },
                    titleTextSize,
                    titleTextColor,
                    subtitleTextSize,
                    subtitleTextColor
                )

            } finally {
                recycle()
            }

            adapter = suggestionsAdapter
            visibility = GONE
        }
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

fun Context.dipToPixels(dipValue: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.displayMetrics)
