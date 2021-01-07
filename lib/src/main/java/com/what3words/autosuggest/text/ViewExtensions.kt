package com.what3words.autosuggest.text

import android.app.Activity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.what3words.autosuggest.R
import com.what3words.autosuggest.utils.MyDividerItemDecorator
import com.what3words.autosuggest.utils.VoicePulseLayout
import java.util.*

internal fun W3WAutoSuggestEditText.buildErrorMessage() {
    val params = ViewGroup.MarginLayoutParams(
        width,
        WRAP_CONTENT
    )
    defaultInvalidAddressMessageView.apply {
        this.x = this@buildErrorMessage.x
        this.y =
            this@buildErrorMessage.y + this@buildErrorMessage.height - resources.getDimensionPixelSize(
                R.dimen.tiny_margin
            )
        layoutParams = params
    }
    (parent as? ViewGroup)?.addView(defaultInvalidAddressMessageView)
}

internal fun W3WAutoSuggestEditText.buildVoice() {
    val params = ViewGroup.MarginLayoutParams(
        resources.getDimensionPixelSize(R.dimen.voice_button_width),
        resources.getDimensionPixelSize(R.dimen.input_height)
    )
    inlineVoicePulseLayout.apply {
        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR) {
            this.x =
                this@buildVoice.x + this@buildVoice.width - (resources.getDimensionPixelSize(R.dimen.voice_button_width))
        } else {
            this.x =
                this@buildVoice.x
        }
        this.y =
            this@buildVoice.y + (resources.getDimensionPixelSize(R.dimen.input_border_height))
        layoutParams = params
        visibility = if (voiceEnabled) VISIBLE else GONE
        setIsVoiceRunning(false)
    }
    (parent as? ViewGroup)?.addView(inlineVoicePulseLayout)
}

internal fun W3WAutoSuggestEditText.buildBackgroundVoice() {
    voicePulseLayout = VoicePulseLayout(context, voicePlaceholder)
    val params = ViewGroup.MarginLayoutParams(
        (parent as? ViewGroup)?.rootView?.width ?: 0,
        (parent as? ViewGroup)?.rootView?.height ?: 0
    )
    voicePulseLayout!!.apply {
        visibility = GONE
        layoutParams = params
        setIsVoiceRunning(false, shouldAnimate = false)
    }
    ((parent as? ViewGroup)?.rootView as? ViewGroup)?.addView(voicePulseLayout)
}

internal fun W3WAutoSuggestEditText.buildSuggestionList() {
     val params = ViewGroup.MarginLayoutParams(
        width,
        WRAP_CONTENT
    )
    defaultPicker.apply {
        isFocusable = false
        isFocusableInTouchMode = false
        this.x = this@buildSuggestionList.x
        this.y =
            this@buildSuggestionList.y + this@buildSuggestionList.height + resources.getDimensionPixelSize(
                R.dimen.input_margin
            )
        layoutParams = params
        val linear = LinearLayoutManager(context)
        background = AppCompatResources.getDrawable(context, R.drawable.bg_white_border_gray)
        resources.getDimensionPixelSize(R.dimen.tiny_margin).let {
            setPadding(it, it, it, it)
        }
        layoutManager = linear
        setHasFixedSize(true)
        visibility = GONE
        ResourcesCompat.getDrawable(resources, R.drawable.divider, null)?.let {
            addItemDecoration(
                MyDividerItemDecorator(
                    it,
                )
            )
        }
    }
    (parent as? ViewGroup)?.apply {
        addView(defaultPicker)
    }
}

internal fun W3WAutoSuggestEditText.showImages(showTick: Boolean = false) {
    isShowingTick = showTick
    if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        setCompoundDrawables(
            slashes,
            null,
            if (showTick) tick else null,
            null
        )
    } else {
        setCompoundDrawables(
            if (showTick) tick else null,
            null,
            slashes,
            null
        )
    }

    if (!showTick && voiceEnabled) {
        inlineVoicePulseLayout.visibility = VISIBLE
    } else {
        inlineVoicePulseLayout.visibility = GONE
    }
}

internal fun W3WAutoSuggestEditText.showKeyboard() {
    this.requestFocus()
    this.setSelection(this.text!!.length)
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
}

internal fun W3WAutoSuggestEditText.hideKeyboard() {
    this.requestFocus()
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}