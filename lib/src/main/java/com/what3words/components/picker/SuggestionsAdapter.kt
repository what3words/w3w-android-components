package com.what3words.components.picker

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.what3words.components.R
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.components.text.formatUnits
import com.what3words.components.utils.DisplayUnits
import com.what3words.components.utils.FlagResourceTranslatorImpl
import com.what3words.javawrapper.response.Suggestion
import kotlinx.android.synthetic.main.item_suggestion.view.*

internal class SuggestionsAdapter(
    private val typeface: Typeface,
    private val textColor: Int,
    private val callback: ((Suggestion) -> Unit)?
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
        return W3WLocationViewHolder(view, typeface, textColor, displayUnits)
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
        private val typeface: Typeface,
        private val textColor: Int,
        private val displayUnits: DisplayUnits
    ) :
        RecyclerView.ViewHolder(view) {
        fun bind(
            suggestion: Suggestion,
            query: String?,
            onSuggestionClicked: (Suggestion) -> Unit
        ) {
            if (query?.replace(view.context.getString(R.string.w3w_slash), "")
                    .equals(suggestion.words, ignoreCase = true)
            ) {
                view.w3wSuggestionHolder.setBackgroundColor(ContextCompat.getColor(view.context, R.color.w3wHover))
            } else {
                view.w3wSuggestionHolder.setBackgroundColor(ContextCompat.getColor(view.context, R.color.white))
            }
            view.w3wAddressLabel.text = suggestion.words
            view.w3wAddressLabel.setTextColor(ContextCompat.getColor(view.context, textColor))
            if (!suggestion.nearestPlace.isNullOrEmpty()) {
                view.w3wNearestPlaceLabel.visibility = VISIBLE
                view.w3wNearestPlaceLabel.text =
                    if (suggestion.language != "en") suggestion.nearestPlace else view.w3wNearestPlaceLabel.context.getString(
                        R.string.near,
                        suggestion.nearestPlace
                    )
            } else {
                view.w3wNearestPlaceLabel.visibility = GONE
            }
            view.w3wNearestPlaceLabel.setTypeface(typeface, Typeface.NORMAL)
            if (suggestion.country == "ZZ") {
                view.w3wAddressFlagIcon.visibility = VISIBLE
                FlagResourceTranslatorImpl(view.w3wAddressFlagIcon.context).let {
                    view.w3wAddressFlagIcon.setImageResource(it.translate(suggestion.country))
                }
            } else {
                view.w3wAddressFlagIcon.visibility = GONE
            }
            suggestion.distanceToFocusKm?.let {
                view.w3wDistanceToFocus.text =
                    formatUnits(suggestion.distanceToFocusKm, displayUnits, view.context)
                view.w3wDistanceToFocus.visibility = VISIBLE
            } ?: run {
                view.w3wDistanceToFocus.visibility = GONE
            }
            view.setOnClickListener {
                onSuggestionClicked(suggestion)
            }
        }
    }
}