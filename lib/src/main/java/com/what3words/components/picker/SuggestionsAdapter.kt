package com.what3words.components.picker

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.what3words.components.R
import com.what3words.components.models.DisplayUnits
import com.what3words.components.text.formatUnits
import com.what3words.components.utils.FlagResourceTranslatorImpl
import com.what3words.javawrapper.response.Suggestion
import kotlinx.android.synthetic.main.item_suggestion.view.w3wAddressFlagIcon
import kotlinx.android.synthetic.main.item_suggestion.view.w3wAddressLabel
import kotlinx.android.synthetic.main.item_suggestion.view.w3wDistanceToFocus
import kotlinx.android.synthetic.main.item_suggestion.view.w3wNearestPlaceLabel
import kotlinx.android.synthetic.main.item_suggestion.view.w3wSlashesLabel
import kotlinx.android.synthetic.main.item_suggestion.view.w3wSuggestionHolder

internal class SuggestionsAdapter(
    private val backgroundDrawable: Drawable?,
    private val backgroundColor: Int?,
    private val callback: ((Suggestion) -> Unit)?,
    private val titleTextSize: Int,
    private val titleTextColor: Int,
    private val subtitleTextSize: Int,
    private val subtitleTextColor: Int,
    private val titleFontFamily: Typeface?,
    private val subtitleFontFamily: Typeface?,
    private val itemPadding: Int
) :
    RecyclerView.Adapter<SuggestionsAdapter.W3WLocationViewHolder>() {

    private var displayUnits: DisplayUnits = DisplayUnits.SYSTEM
    private var suggestions: List<Suggestion>? = null
    private var query: String? = null

    fun refreshSuggestions(
        suggestions: List<Suggestion>,
        query: String?,
        displayUnits: DisplayUnits
    ) {
        this.suggestions = suggestions
        this.query = query
        this.displayUnits = displayUnits
        notifyDataSetChanged()
    }

    override fun getItemCount() = suggestions?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): W3WLocationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_suggestion, parent, false)
        return W3WLocationViewHolder(
            view,
            displayUnits,
            backgroundDrawable,
            backgroundColor,
            titleTextSize,
            titleTextColor,
            subtitleTextSize,
            subtitleTextColor,
            titleFontFamily,
            subtitleFontFamily,
            itemPadding
        )
    }

    override fun onBindViewHolder(holder: W3WLocationViewHolder, position: Int) {
        suggestions?.let {
            holder.bind(it[position], query) { suggestion ->
                callback?.let {
                    it(suggestion)
                }
            }
        }
    }

    class W3WLocationViewHolder(
        private val view: View,
        private val displayUnits: DisplayUnits,
        private val backgroundDrawable: Drawable?,
        private val backgroundColor: Int?,
        private val titleTextSize: Int,
        private val titleTextColor: Int,
        private val subtitleTextSize: Int,
        private val subtitleTextColor: Int,
        private val titleFontFamily: Typeface?,
        private val subtitleFontFamily: Typeface?,
        private val itemPadding: Int
    ) :
        RecyclerView.ViewHolder(view) {
        fun bind(
            suggestion: Suggestion,
            query: String?,
            onSuggestionClicked: (Suggestion) -> Unit
        ) {
            backgroundDrawable?.let {
                view.background = it
            } ?: run {
                backgroundColor?.let {
                    view.setBackgroundColor(it)
                }
                if (query?.replace(view.context.getString(R.string.w3w_slash), "")
                        .equals(suggestion.words, ignoreCase = true)
                ) {
                    view.w3wSuggestionHolder.setBackgroundColor(
                        ContextCompat.getColor(
                            view.context,
                            R.color.w3wHover
                        )
                    )
                } else {
                    view.w3wSuggestionHolder.setBackgroundColor(
                        ContextCompat.getColor(
                            view.context,
                            R.color.white
                        )
                    )
                }
            }

            view.w3wSuggestionHolder.setPadding(itemPadding, itemPadding, itemPadding, itemPadding)

            if (titleFontFamily != null) {
                view.w3wSlashesLabel.typeface = titleFontFamily
                view.w3wAddressLabel.typeface = titleFontFamily
            }

            if (subtitleFontFamily != null) {
                view.w3wNearestPlaceLabel.typeface = subtitleFontFamily
                view.w3wDistanceToFocus.typeface = subtitleFontFamily
            }

            view.w3wSlashesLabel.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, titleTextSize.toFloat()
            )
            view.w3wAddressLabel.setTextColor(titleTextColor)
            view.w3wAddressLabel.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, titleTextSize.toFloat()
            )
            view.w3wAddressLabel.text = suggestion.words
            view.w3wNearestPlaceLabel.setTextColor(subtitleTextColor)
            view.w3wNearestPlaceLabel.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, subtitleTextSize.toFloat()
            )
            if (!suggestion.nearestPlace.isNullOrEmpty()) {
                view.w3wNearestPlaceLabel.visibility = VISIBLE
                view.w3wNearestPlaceLabel.text =
                    if (suggestion.language != "en") suggestion.nearestPlace else view.w3wNearestPlaceLabel.context.getString(
                        R.string.near,
                        suggestion.nearestPlace
                    )
            } else {
                view.w3wNearestPlaceLabel.visibility = INVISIBLE
            }

            if (suggestion.country == "ZZ") {
                view.w3wAddressFlagIcon.visibility = VISIBLE
                FlagResourceTranslatorImpl(view.w3wAddressFlagIcon.context).let {
                    view.w3wAddressFlagIcon.setImageResource(it.translate(suggestion.country))
                }
            } else {
                view.w3wAddressFlagIcon.visibility = GONE
            }

            view.w3wDistanceToFocus.setTextColor(subtitleTextColor)
            view.w3wDistanceToFocus.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, subtitleTextSize.toFloat()
            )
            suggestion.distanceToFocusKm?.let {
                view.w3wDistanceToFocus.text =
                    formatUnits(suggestion.distanceToFocusKm, displayUnits, view.context)
                view.w3wDistanceToFocus.visibility = VISIBLE
            } ?: run {
                view.w3wDistanceToFocus.visibility = INVISIBLE
            }
            view.setOnClickListener {
                onSuggestionClicked(suggestion)
            }
        }
    }
}