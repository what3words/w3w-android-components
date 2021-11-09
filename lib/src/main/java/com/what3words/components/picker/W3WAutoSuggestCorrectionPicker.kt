package com.what3words.components.picker

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.what3words.components.R
import com.what3words.javawrapper.response.Suggestion
import kotlinx.android.synthetic.main.correction_layout.view.*

/**
 * A [View] styled and ready to show a 3 word address correction, i.e: "index home raft", will suggest "index.home.raft" which is a valid 3 word address
 */
class W3WAutoSuggestCorrectionPicker
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var suggestion: Suggestion? = null
    private var callback: ((selectedSuggestion: Suggestion) -> Unit)? = null

    init {
        View.inflate(context, R.layout.correction_layout, this)
        holderHint.setOnClickListener {
            if (suggestion != null) callback?.invoke(suggestion!!)
        }
        visibility = GONE
    }

    internal fun setSuggestion(suggestion: Suggestion?) {
        this.suggestion = suggestion
        w3wAddressLabel.text = suggestion?.words
    }

    internal fun internalCallback(callback: (selectedSuggestion: Suggestion) -> Unit): W3WAutoSuggestCorrectionPicker {
        this.callback = callback
        return this
    }

    fun setCorrectionMessage(message: String): W3WAutoSuggestCorrectionPicker {
        correctionLabel.text = message
        return this
    }
}
