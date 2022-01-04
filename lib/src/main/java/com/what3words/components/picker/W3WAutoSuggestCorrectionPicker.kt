package com.what3words.components.picker

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.what3words.components.databinding.CorrectionLayoutBinding
import com.what3words.javawrapper.response.Suggestion

/**
 * A [View] styled and ready to show a 3 word address correction, i.e: "index home raft", will suggest "index.home.raft" which is a valid 3 word address
 */
class W3WAutoSuggestCorrectionPicker
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: CorrectionLayoutBinding = CorrectionLayoutBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private var suggestion: Suggestion? = null
    private var callback: ((selectedSuggestion: Suggestion) -> Unit)? = null

    init {
        binding.holderHint.setOnClickListener {
            if (suggestion != null) callback?.invoke(suggestion!!)
        }
        visibility = GONE
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
