package com.what3words.autosuggest

import android.os.Handler
import android.os.Looper
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.what3words.autosuggest.util.MyDividerItemDecorator

internal fun W3WAutoSuggestEditText.buildErrorMessage() {
    val params = ViewGroup.MarginLayoutParams(
        width,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    errorMessage.apply {
        isFocusable = false
        isFocusableInTouchMode = false
        text = errorMessageText
        setBackgroundResource(R.drawable.bg_item)
        setTextColor(ContextCompat.getColor(context, R.color.w3wError))
        this.x = this@buildErrorMessage.x
        this.y =
            this@buildErrorMessage.y + this@buildErrorMessage.height - resources.getDimensionPixelSize(
                R.dimen.tiny_margin
            )
        layoutParams = params
        setPadding(
            resources.getDimensionPixelSize(R.dimen.xlarge_margin),
            resources.getDimensionPixelSize(R.dimen.medium_margin),
            resources.getDimensionPixelSize(R.dimen.xlarge_margin),
            resources.getDimensionPixelSize(R.dimen.medium_margin)
        )
        visibility = GONE
    }
    (parent as? ViewGroup)?.addView(errorMessage)
}

internal fun W3WAutoSuggestEditText.buildVoice() {
    val params = ViewGroup.MarginLayoutParams(
        resources.getDimensionPixelSize(R.dimen.voice_button_width),
        resources.getDimensionPixelSize(R.dimen.input_height)
    )
    inlineVoicePulseLayout.apply {
        this.x =
            this@buildVoice.x + this@buildVoice.width - (resources.getDimensionPixelSize(R.dimen.voice_button_width))
        this.y =
            this@buildVoice.y + (resources.getDimensionPixelSize(R.dimen.input_border_height))
        layoutParams = params
        visibility = VISIBLE
        setIsVoiceRunning(false)
    }
    (parent as? ViewGroup)?.addView(inlineVoicePulseLayout)
}

internal fun W3WAutoSuggestEditText.buildBackgroundVoice() {
    val params = ViewGroup.MarginLayoutParams(
        (parent as? ViewGroup)?.rootView?.width ?: 0,
        (parent as? ViewGroup)?.rootView?.height ?: 0
    )
    voicePulseLayout.apply {
        layoutParams = params
        setIsVoiceRunning(false)
    }
    ((parent as? ViewGroup)?.rootView as? ViewGroup)?.addView(voicePulseLayout)
}

internal fun W3WAutoSuggestEditText.removeBackgroundVoice() {
    ((parent as? ViewGroup)?.rootView as? ViewGroup)?.removeView(voicePulseLayout)
}

internal fun W3WAutoSuggestEditText.buildSuggestionList() {
    val params = ViewGroup.MarginLayoutParams(
        width,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    recyclerView.apply {
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
        ResourcesCompat.getDrawable(resources, R.drawable.divider, null)?.let {
            addItemDecoration(
                MyDividerItemDecorator(
                    it
                )
            )
        }
        adapter = suggestionsAdapter
        visibility = AppCompatEditText.GONE
    }
    (parent as? ViewGroup)?.apply {
        addView(recyclerView)
    }
}

internal fun W3WAutoSuggestEditText.showErrorMessage() {
    errorMessage.visibility = VISIBLE
    Handler(Looper.getMainLooper()).postDelayed({
        errorMessage.visibility = GONE
    }, 5000)
    suggestionsAdapter.refreshSuggestions(emptyList(), "")
}

internal fun W3WAutoSuggestEditText.showImages(showTick: Boolean = false) {
    isShowingTick = showTick
    setCompoundDrawables(
        slashes,
        null,
        if (showTick) tick else null,
        null
    )
    if (!showTick) {
        inlineVoicePulseLayout.visibility = VISIBLE
    } else {
        inlineVoicePulseLayout.visibility = GONE
    }
}