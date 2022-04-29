package com.what3words.components.picker

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.what3words.components.R
import com.what3words.components.databinding.CorrectionLayoutBinding
import com.what3words.javawrapper.response.Suggestion

/**
 * A [View] styled and ready to show a 3 word address correction, i.e: "index home raft", will suggest "index.home.raft" which is a valid 3 word address
 */
class W3WAutoSuggestCorrectionPicker
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.customW3WAutoSuggestCorrectionPickerStyle
) : ConstraintLayout(
    ContextThemeWrapper(context, R.style.W3WAutoSuggestCorrectionPickerTheme),
    attrs,
    defStyleAttr
) {
    private var binding: CorrectionLayoutBinding = CorrectionLayoutBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private var suggestion: Suggestion? = null
    private var callback: ((selectedSuggestion: Suggestion) -> Unit)? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.W3WAutoSuggestCorrectionPicker,
            defStyleAttr, R.style.W3WAutoSuggestCorrectionPickerTheme
        ).apply {
            try {
                if (findViewById<W3WAutoSuggestPicker>(id) == null) id =
                    R.id.w3wAutoSuggestDefaultCorrectionPicker

                val backgroundDrawableId = getResourceId(
                    R.styleable.W3WAutoSuggestCorrectionPicker_correctionBackgroundDrawable,
                    -1
                )
                if (backgroundDrawableId != -1) {
                    binding.holderHint.background =
                        ContextCompat.getDrawable(context, backgroundDrawableId)
                } else {
                    binding.holderHint.setBackgroundColor(
                        getColor(
                            R.styleable.W3WAutoSuggestCorrectionPicker_correctionBackground,
                            ContextCompat.getColor(context, R.color.background)
                        )
                    )
                }

                getDimensionPixelSize(
                    R.styleable.W3WAutoSuggestCorrectionPicker_correctionTitleTextSize,
                    context.resources.getDimensionPixelSize(R.dimen.default_text)
                ).toFloat().let {
                    binding.w3wAddressLabel.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX, it
                    )
                    binding.w3wSlashesLabel.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX, it
                    )
                }

                binding.correctionLabel.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    getDimensionPixelSize(
                        R.styleable.W3WAutoSuggestCorrectionPicker_correctionSubtitleTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.secondary_text)
                    ).toFloat()
                )

                getColor(
                    R.styleable.W3WAutoSuggestCorrectionPicker_correctionTitleTextColor,
                    context.getColor(R.color.textColor)
                ).let {
                    binding.w3wAddressLabel.setTextColor(it)
                    binding.w3wSlashesLabel.setTextColor(it)
                }

                binding.correctionLabel.setTextColor(
                    getColor(
                        R.styleable.W3WAutoSuggestCorrectionPicker_correctionSubtitleTextColor,
                        context.getColor(R.color.subtextColor)
                    )
                )

                val titleFontFamilyId = getResourceId(
                    R.styleable.W3WAutoSuggestCorrectionPicker_correctionTitleFontFamily,
                    -1
                )
                if (titleFontFamilyId != -1) {
                    binding.w3wAddressLabel.typeface =
                        ResourcesCompat.getFont(context, titleFontFamilyId)
                    binding.w3wSlashesLabel.typeface =
                        ResourcesCompat.getFont(context, titleFontFamilyId)
                }

                val subtitleFontFamilyId = getResourceId(
                    R.styleable.W3WAutoSuggestCorrectionPicker_correctionSubtitleFontFamily,
                    -1
                )
                if (subtitleFontFamilyId != -1) {
                    binding.correctionLabel.typeface =
                        ResourcesCompat.getFont(context, subtitleFontFamilyId)
                }
                binding.w3wSlashesLabel.setTextColor(binding.w3wSlashesLabel.context.getColor(R.color.slashesColor))
            } finally {
                recycle()
            }
            binding.holderHint.setOnClickListener {
                if (suggestion != null) callback?.invoke(suggestion!!)
            }
            visibility = GONE
        }
    }

    internal fun populateAndShow(suggestion: Suggestion?) {
        this.suggestion = suggestion
        binding.w3wAddressLabel.text = suggestion?.words
        visibility = VISIBLE
    }

    internal fun internalCallback(callback: (selectedSuggestion: Suggestion) -> Unit): W3WAutoSuggestCorrectionPicker {
        this.callback = callback
        return this
    }

    fun setCorrectionMessage(message: String): W3WAutoSuggestCorrectionPicker {
        binding.correctionLabel.text = message
        return this
    }

    internal fun forceClearAndHide() {
        populateAndShow(null)
        visibility = GONE
    }
}
