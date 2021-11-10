package com.what3words.components.text

import android.app.Activity
import android.content.Context
import android.icu.text.MeasureFormat
import android.icu.text.MeasureFormat.FormatWidth
import android.icu.util.Measure
import android.icu.util.MeasureUnit
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
import com.what3words.components.R
import com.what3words.components.models.DisplayUnits
import com.what3words.components.utils.MyDividerItemDecorator
import com.what3words.components.utils.VoicePulseLayout
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

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

internal fun W3WAutoSuggestEditText.buildCorrection() {
    val params = ViewGroup.MarginLayoutParams(
        width,
        WRAP_CONTENT
    )
    defaultCorrectionPicker.apply {
        this.x = this@buildCorrection.x
        this.y =
            this@buildCorrection.y + this@buildCorrection.height - resources.getDimensionPixelSize(
            R.dimen.tiny_margin
        )
        layoutParams = params
    }
    (parent as? ViewGroup)?.addView(defaultCorrectionPicker)
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
            null,
            null,
            if (showTick) tick else null,
            null
        )
    } else {
        setCompoundDrawables(
            if (showTick) tick else null,
            null,
            null,
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
