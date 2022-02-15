package com.what3words.components.error

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * A [AppCompatTextView] styled and ready to show error messages.
 */
class W3WAutoSuggestErrorMessage
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        visibility = GONE
    }
}

internal fun AppCompatTextView.populateAndShow(errorMessage: String?) {
    text = errorMessage
    visibility = AppCompatTextView.VISIBLE
    Handler(Looper.getMainLooper()).postDelayed(
        {
            visibility = AppCompatTextView.GONE
        },
        5000
    )
}

internal fun AppCompatTextView.forceClearAndHide() {
    text = ""
    visibility = AppCompatTextView.GONE
}
