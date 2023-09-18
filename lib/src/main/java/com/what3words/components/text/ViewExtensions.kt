package com.what3words.components.text

import android.app.Activity
import android.content.Context
import android.icu.text.MeasureFormat
import android.icu.text.MeasureFormat.FormatWidth
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.util.Log
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import com.what3words.components.R
import com.what3words.components.models.DisplayUnits
import com.what3words.components.text.ViewAnchorUtils.anchor
import com.what3words.components.utils.VoicePulseLayout
import com.what3words.components.utils.VoicePulseLayoutFullScreen
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

internal fun W3WAutoSuggestEditText.buildErrorMessage() {
    val params = ViewGroup.MarginLayoutParams(
        width,
        WRAP_CONTENT
    )
    defaultInvalidAddressMessageView.apply {
        layoutParams = params
        translationZ = context.resources.getDimension(R.dimen.overlay_z)
        outlineProvider = null
    }
    (parent as? ViewGroup)?.addView(defaultInvalidAddressMessageView)

    defaultInvalidAddressMessageView.anchor(
        target = this@buildErrorMessage,
        position = ViewAnchorUtils.Position.BELOW,
        spacing = -resources.getDimensionPixelSize(
            R.dimen.tiny_margin
        )
    )
}

internal fun W3WAutoSuggestEditText.buildCorrection() {
    val params = ViewGroup.MarginLayoutParams(
        width,
        WRAP_CONTENT
    )
    defaultCorrectionPicker.apply {
        layoutParams = params
        translationZ = context.resources.getDimension(R.dimen.overlay_z)
        outlineProvider = null
    }
    (parent as? ViewGroup)?.addView(defaultCorrectionPicker)

    defaultCorrectionPicker.anchor(
        target = this@buildCorrection,
        position = ViewAnchorUtils.Position.BELOW,
        spacing = -resources.getDimensionPixelSize(
            R.dimen.tiny_margin
        )
    )
}

internal fun W3WAutoSuggestEditText.buildIconHolderLayout() {
    iconHolderLayout.layoutParams = ViewGroup.MarginLayoutParams(
        this.width - (resources.getDimensionPixelSize(R.dimen.input_border_height) * 2),
        this.height - (resources.getDimensionPixelSize(R.dimen.input_border_height) * 2)
    )
    (parent as? ViewGroup)?.addView(iconHolderLayout)

    iconHolderLayout.anchor(
        target = this@buildIconHolderLayout,
        position = ViewAnchorUtils.Position.ALIGN,
        spacing =  resources.getDimensionPixelSize(R.dimen.input_border_height)
    )
}

internal fun W3WAutoSuggestEditText.buildVoiceAnimatedPopup() {
    try {
        voiceAnimatedPopup = VoicePulseLayout(
            context,
            voicePlaceholder,
            voiceErrorLabel,
            voiceTryAgainLabel,
            voiceLoadingLabel,
            currentTextColor,
            voiceBackgroundColor,
            voiceBackgroundDrawable,
            voiceIconsColor
        )
        val params = ViewGroup.MarginLayoutParams(
            (parent as? ViewGroup)?.rootView?.width ?: 0,
            (parent as? ViewGroup)?.rootView?.height ?: 0
        )
        voiceAnimatedPopup!!.apply {
            visibility = GONE
            layoutParams = params
            setIsVoiceRunning(false, shouldAnimate = false, shouldClose = true)
            translationZ = context.resources.getDimension(R.dimen.overlay_z)
            outlineProvider = null
        }
    } catch (e: Exception) {
        Log.e(
            "W3WAutoSuggestEditText",
            e.message?.plus(", fallback to inline voice")
                ?: "Issue adding to rootView, check if parent allows multiple children"
        )
        voiceEnabled(true)
    }
}

internal fun W3WAutoSuggestEditText.buildVoiceFullscreen() {
    try {
        voicePulseLayoutFullScreen = VoicePulseLayoutFullScreen(
            context,
            voicePlaceholder,
            voiceErrorLabel,
            voiceTryAgainLabel,
            voiceLoadingLabel,
            currentTextColor,
            voiceBackgroundColor,
            voiceBackgroundDrawable,
            voiceIconsColor
        )
        voicePulseLayoutFullScreen!!.apply {
            visibility = GONE
            setIsVoiceRunning(false, true)
            translationZ = context.resources.getDimension(R.dimen.overlay_z)
            outlineProvider = null
        }
    } catch (e: Exception) {
        Log.e(
            "W3WAutoSuggestEditText",
            e.message?.plus(", fallback to inline voice")
                ?: "Issue adding to rootView, check if parent allows multiple children"
        )
        voiceEnabled(true)
    }
}

internal fun W3WAutoSuggestEditText.buildSuggestionList() {
    val params = ViewGroup.MarginLayoutParams(
        width,
        WRAP_CONTENT
    )
    defaultPicker.apply {
        isFocusable = false
        isFocusableInTouchMode = false
        layoutParams = params
        resources.getDimensionPixelSize(R.dimen.tiny_margin).let {
            setPadding(it, it, it, it)
        }
        visibility = GONE
        translationZ = context.resources.getDimension(R.dimen.overlay_z)
        outlineProvider = null
    }
    (parent as? ViewGroup)?.apply {
        addView(defaultPicker)
    }

    defaultPicker.anchor(
        target = this@buildSuggestionList,
        position = ViewAnchorUtils.Position.BELOW,
        spacing = -resources.getDimensionPixelSize(
            R.dimen.tiny_margin
        )
    )
}

internal fun W3WAutoSuggestEditText.showImages(showTick: Boolean = false) {
    isShowingTick = showTick
    setCompoundDrawablesRelative(
        drawableStart,
        null,
        if (showTick && !hideSelectedIcon) {
            tick
        } else {
            null
        },
        null
    )
    if (!showTick && voiceEnabled) {
        iconHolderLayout.setVoiceVisibility(VISIBLE)
    } else {
        iconHolderLayout.setVoiceVisibility(INVISIBLE)
    }
}

internal fun W3WAutoSuggestEditText.showKeyboard() {
    this.requestFocus()
    this.setSelection(this.length())
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

internal fun W3WAutoSuggestEditText.hideKeyboard() {
    this.clearFocus()
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

internal fun formatUnits(distanceKm: Int, displayUnits: DisplayUnits, context: Context): String {
    if (distanceKm == 0 ||
        (
            displayUnits == DisplayUnits.SYSTEM && !Locale.getDefault()
                .isMetric() && (distanceKm / 1.609) < 1
            ) ||
        (displayUnits == DisplayUnits.IMPERIAL && (distanceKm / 1.609) < 1)
    ) {
        if ((displayUnits == DisplayUnits.SYSTEM && Locale.getDefault().isMetric()) ||
            displayUnits == DisplayUnits.METRIC
        ) {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val fmtFr = MeasureFormat.getInstance(Locale.getDefault(), FormatWidth.SHORT)
                val measureF = Measure(1, MeasureUnit.KILOMETER)
                context.getString(R.string.distance_metric_low, fmtFr.format(measureF))
            } else {
                context.getString(R.string.distance_metric_low, "1 km")
            }
        } else {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val fmtFr = MeasureFormat.getInstance(Locale.getDefault(), FormatWidth.SHORT)
                val measureF = Measure(1, MeasureUnit.MILE)
                context.getString(R.string.distance_metric_low, fmtFr.format(measureF))
            } else {
                context.getString(R.string.distance_imperial_low, "1 mi")
            }
        }
    } else {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            val fmtFr = MeasureFormat.getInstance(Locale.getDefault(), FormatWidth.SHORT)
            return if ((
                displayUnits == DisplayUnits.SYSTEM && Locale.getDefault()
                    .isMetric()
                ) || displayUnits == DisplayUnits.METRIC
            ) {
                val measureF = Measure(distanceKm, MeasureUnit.KILOMETER)
                fmtFr.format(measureF)
            } else {
                val measureF = Measure((distanceKm / 1.609).roundToInt(), MeasureUnit.MILE)
                fmtFr.format(measureF)
            }
        } else {
            val nFormat = NumberFormat.getNumberInstance(Locale.getDefault())
            return if ((
                displayUnits == DisplayUnits.SYSTEM && Locale.getDefault()
                    .isMetric()
                ) || displayUnits == DisplayUnits.METRIC
            ) {
                context.getString(R.string.distance_metric, nFormat.format(distanceKm))
            } else {
                context.getString(
                    R.string.distance_imperial,
                    nFormat.format((distanceKm / 1.609).roundToInt())
                )
            }
        }
    }
}

internal fun Locale.isMetric(): Boolean {
    return when (country.uppercase()) {
        "US", "GB", "MM", "LR" -> false
        else -> true
    }
}
