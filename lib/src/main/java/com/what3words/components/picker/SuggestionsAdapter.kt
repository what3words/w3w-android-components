package com.what3words.components.picker

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.what3words.components.R
import com.what3words.components.databinding.ItemSuggestionBinding
import com.what3words.components.models.DisplayUnits
import com.what3words.components.text.formatUnits
import com.what3words.components.utils.FlagResourceTranslatorImpl
import com.what3words.javawrapper.response.Suggestion

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
        val binding =
            ItemSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return W3WLocationViewHolder(
            binding,
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
        private val binding: ItemSuggestionBinding,
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
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            suggestion: Suggestion,
            query: String?,
            onSuggestionClicked: (Suggestion) -> Unit
        ) {
            backgroundDrawable?.let {
                binding.root.background = it
            } ?: run {
                backgroundColor?.let {
                    binding.root.setBackgroundColor(it)
                }
                if (query?.replace(binding.root.context.getString(R.string.w3w_slash), "")
                        .equals(suggestion.words, ignoreCase = true)
                ) {
                    binding.w3wSuggestionHolder.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.w3wHover
                        )
                    )
                } else {
                    binding.w3wSuggestionHolder.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.white
                        )
                    )
                }
            }

            binding.w3wSuggestionHolder.setPadding(
                itemPadding,
                itemPadding,
                itemPadding,
                itemPadding
            )

            if (titleFontFamily != null) {
                binding.w3wSlashesLabel.typeface = titleFontFamily
                binding.w3wAddressLabel.typeface = titleFontFamily
            }

            if (subtitleFontFamily != null) {
                binding.w3wNearestPlaceLabel.typeface = subtitleFontFamily
                binding.w3wDistanceToFocus.typeface = subtitleFontFamily
            }

            binding.w3wSlashesLabel.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, titleTextSize.toFloat()
            )
            binding.w3wAddressLabel.setTextColor(titleTextColor)
            binding.w3wAddressLabel.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, titleTextSize.toFloat()
            )
            binding.w3wAddressLabel.text = suggestion.words
            binding.w3wNearestPlaceLabel.setTextColor(subtitleTextColor)
            binding.w3wNearestPlaceLabel.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, subtitleTextSize.toFloat()
            )
            if (!suggestion.nearestPlace.isNullOrEmpty()) {
                binding.w3wNearestPlaceLabel.visibility = VISIBLE
                binding.w3wNearestPlaceLabel.text =
                    if (suggestion.language != "en") suggestion.nearestPlace else binding.w3wNearestPlaceLabel.context.getString(
                        R.string.near,
                        suggestion.nearestPlace
                    )
            } else {
                binding.w3wNearestPlaceLabel.visibility = INVISIBLE
            }

            if (suggestion.country == "ZZ") {
                binding.w3wAddressFlagIcon.visibility = VISIBLE
                FlagResourceTranslatorImpl(binding.w3wAddressFlagIcon.context).let {
                    binding.w3wAddressFlagIcon.setImageResource(it.translate(suggestion.country))
                }
            } else {
                binding.w3wAddressFlagIcon.visibility = GONE
            }

            binding.w3wDistanceToFocus.setTextColor(subtitleTextColor)
            binding.w3wDistanceToFocus.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, subtitleTextSize.toFloat()
            )
            suggestion.distanceToFocusKm?.let {
                binding.w3wDistanceToFocus.text =
                    formatUnits(suggestion.distanceToFocusKm, displayUnits, binding.root.context)
                binding.w3wDistanceToFocus.visibility = VISIBLE
            } ?: run {
                binding.w3wDistanceToFocus.visibility = INVISIBLE
            }
            binding.root.setOnClickListener {
                onSuggestionClicked(suggestion)
            }
        }
    }
}
