package com.what3words.components.picker

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.fonts.FontFamily
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.what3words.components.R
import com.what3words.components.models.DisplayUnits
import com.what3words.components.utils.MyDividerItemDecorator
import com.what3words.components.vm.AutosuggestTextViewModel
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.Suggestion

/**
 * A [RecyclerView] to show [Suggestion] returned by w3w auto suggest component
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

    private var titleFontFamily: Typeface? = null
    private var subtitleFontFamily: Typeface? = null
    private var subtitleTextColor: Int
    private var titleTextColor: Int
    private var itemHighlightBackground: Int
    private var subtitleTextSize: Int
    private var titleTextSize: Int
    private var options: AutosuggestOptions = AutosuggestOptions()
    private var displayUnits: DisplayUnits = DisplayUnits.SYSTEM
    private var returnCoordinates: Boolean = false
    private var query: String = ""
    private var viewModel: AutosuggestTextViewModel? = null
    internal var itemBackgroundDrawable: Drawable? = null
    internal var itemBackgroundHighlightedDrawable: Drawable? = null
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
                if (findViewById<W3WAutoSuggestPicker>(id) == null) id =
                    R.id.w3wAutoSuggestDefaultPicker

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

                val itemBackgroundHighlightedDrawableId = getResourceId(
                    R.styleable.W3WAutoSuggestPicker_pickerItemBackgroundHighlightedDrawable,
                    -1
                )

                if (itemBackgroundHighlightedDrawableId != -1) {
                    itemBackgroundHighlightedDrawable =
                        ContextCompat.getDrawable(context, itemBackgroundHighlightedDrawableId)
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
                    -1
                )

                val itemSpacing = getDimension(
                    R.styleable.W3WAutoSuggestPicker_pickerItemSpacing,
                    0f
                )

                val itemPadding = getDimensionPixelSize(
                    R.styleable.W3WAutoSuggestPicker_pickerItemPadding,
                    resources.getDimensionPixelSize(R.dimen.large_margin)
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
                        context.getColor(R.color.textColor)
                    )

                itemHighlightBackground =
                    getColor(
                        R.styleable.W3WAutoSuggestPicker_pickerItemHighlightBackground,
                        context.getColor(R.color.hoverColor)
                    )

                subtitleTextColor =
                    getColor(
                        R.styleable.W3WAutoSuggestPicker_pickerItemSubtitleTextColor,
                        context.getColor(R.color.subtextColor)
                    )

                itemBackgroundColor = getColor(
                    R.styleable.W3WAutoSuggestPicker_pickerItemBackground,
                    ContextCompat.getColor(context, R.color.background)
                )

                val dividerVisible = getBoolean(
                    R.styleable.W3WAutoSuggestPicker_pickerDividerVisible,
                    true
                )

                val titleFontFamilyId = getResourceId(
                    R.styleable.W3WAutoSuggestPicker_pickerItemTitleFontFamily,
                    -1
                )
                if (titleFontFamilyId != -1) {
                    titleFontFamily = getFont(context, titleFontFamilyId)
                }

                val subtitleFontFamilyId = getResourceId(
                    R.styleable.W3WAutoSuggestPicker_pickerItemSubtitleFontFamily,
                    -1
                )

                if (subtitleFontFamilyId != -1) {
                    subtitleFontFamily = getFont(context, subtitleFontFamilyId)
                }

                if (dividerDrawableId != -1) {
                    ContextCompat.getDrawable(context, dividerDrawableId)?.let {
                        addItemDecoration(
                            MyDividerItemDecorator(
                                if (dividerVisible) it else null,
                                itemSpacing,
                                (layoutManager as LinearLayoutManager).reverseLayout
                            )
                        )
                    } ?: kotlin.run {
                        addItemDecoration(
                            MyDividerItemDecorator(
                                null,
                                itemSpacing,
                                (layoutManager as LinearLayoutManager).reverseLayout
                            )
                        )
                    }
                } else {
                    addItemDecoration(
                        MyDividerItemDecorator(
                            null,
                            itemSpacing,
                            (layoutManager as LinearLayoutManager).reverseLayout
                        )
                    )
                }

                suggestionsAdapter = SuggestionsAdapter(
                    itemBackgroundDrawable,
                    itemBackgroundColor,
                    { suggestion ->
                        handleAddressPicked(suggestion)
                    },
                    titleTextSize,
                    titleTextColor,
                    subtitleTextSize,
                    subtitleTextColor,
                    itemHighlightBackground,
                    itemBackgroundHighlightedDrawable,
                    titleFontFamily,
                    subtitleFontFamily,
                    itemPadding
                )
            } finally {
                recycle()
            }

            adapter = suggestionsAdapter
            visibility = GONE
        }
    }

    internal fun setup(
        viewModel: AutosuggestTextViewModel,
        displayUnits: DisplayUnits
    ) {
        this.viewModel = viewModel
        this.displayUnits = displayUnits
    }

    internal fun populateAndSetVisibility(
        suggestions: List<Suggestion>,
        query: String?,
        options: AutosuggestOptions,
        returnCoordinates: Boolean
    ) {
        this.visibility = if (suggestions.isEmpty()) View.GONE else View.VISIBLE
        suggestionsAdapter.refreshSuggestions(suggestions, query, displayUnits)
        this.returnCoordinates = returnCoordinates
        this.query = query ?: ""
        this.options = options
    }

    internal fun forceClearAndHide() {
        suggestionsAdapter.refreshSuggestions(emptyList(), "", displayUnits)
        visibility = GONE
    }
}
