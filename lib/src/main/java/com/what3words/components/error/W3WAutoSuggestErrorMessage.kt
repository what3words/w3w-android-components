package com.what3words.components.error

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.what3words.components.R

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
        isFocusable = false
        isFocusableInTouchMode = false
        setPadding(
            resources.getDimensionPixelSize(R.dimen.xlarge_margin),
            resources.getDimensionPixelSize(R.dimen.medium_margin),
            resources.getDimensionPixelSize(R.dimen.xlarge_margin),
            resources.getDimensionPixelSize(R.dimen.medium_margin)
        )
        setBackgroundResource(R.drawable.bg_item)
        setTextColor(ContextCompat.getColor(context, R.color.w3wError))
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
