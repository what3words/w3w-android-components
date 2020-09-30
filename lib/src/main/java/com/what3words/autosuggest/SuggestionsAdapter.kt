package com.what3words.autosuggest

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.what3words.javawrapper.response.Suggestion
import com.what3words.autosuggest.util.FlagResourceTranslatorImpl
import kotlinx.android.synthetic.main.item_suggestion.view.*

class SuggestionsAdapter(
    private val typeface: Typeface,
    private val textColor: Int,
    private val callback: ((Suggestion) -> Unit)?
) :
    RecyclerView.Adapter<SuggestionsAdapter.W3WLocationViewHolder>() {

    private var suggestions: List<Suggestion>? = null
    private var query: String? = null

    fun refreshSuggestions(suggestions: List<Suggestion>, query: String?) {
        this.suggestions = suggestions
        this.query = query
        notifyDataSetChanged()
    }

    override fun getItemCount() = suggestions?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): W3WLocationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_suggestion, parent, false)
        return W3WLocationViewHolder(view, typeface, textColor)
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
        private val textColor: Int
    ) :
        RecyclerView.ViewHolder(view) {
        fun bind(
            suggestion: Suggestion,
            query: String?,
            onSuggestionClicked: (Suggestion) -> Unit
        ) {
            if (query == suggestion.words) {
                view.w3wAddressLabel.setTypeface(typeface, Typeface.BOLD)
            } else {
                view.w3wAddressLabel.setTypeface(typeface, Typeface.NORMAL)
            }
            view.w3wAddressLabel.text = suggestion.words
            view.w3wAddressLabel.setTextColor(textColor)
            view.w3wNearestPlaceLabel.text = suggestion.nearestPlace
            view.w3wNearestPlaceLabel.setTypeface(typeface, Typeface.NORMAL)
            if (suggestion.country.isNullOrEmpty() || suggestion.country != "-99") {
                FlagResourceTranslatorImpl(view.w3wAddressFlagIcon.context).let {
                    view.w3wAddressFlagIcon.setImageResource(it.translate(suggestion.country))
                }
            } else {
                view.w3wAddressFlagIcon.setImageResource(R.drawable.ic_location_pin)
            }
            view.setOnClickListener {
                onSuggestionClicked(suggestion)
            }
        }
    }
}