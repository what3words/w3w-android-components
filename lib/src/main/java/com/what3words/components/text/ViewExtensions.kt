package com.what3words.components.text

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.text.MeasureFormat
import android.icu.text.MeasureFormat.FormatWidth
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.what3words.components.R
import com.what3words.components.models.DisplayUnits
import com.what3words.components.utils.MyDividerItemDecorator
import com.what3words.components.utils.VoicePulseLayout
import com.what3words.components.utils.VoicePulseLayoutFullScreen
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min
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
        this.height * 2,
        this.height - (resources.getDimensionPixelSize(R.dimen.input_border_height) * 2)
    )
    val view = ConstraintLayout(context)
    view.layoutParams = ViewGroup.MarginLayoutParams(
        (this.height * 1.5).toInt(),
        this.height - (resources.getDimensionPixelSize(R.dimen.input_border_height) * 2)
    )
    view.apply {
        this.x =
            (this@buildVoice.x + this@buildVoice.width - (this@buildVoice.height * 1.5f)) - (
            resources.getDimensionPixelSize(
                R.dimen.input_border_height
            )
            )
        this.y =
            this@buildVoice.y + (resources.getDimensionPixelSize(R.dimen.input_border_height))
    }
    inlineVoicePulseLayout.apply {
        layoutParams = params
        visibility = if (voiceEnabled) VISIBLE else GONE
    }
    view.addView(inlineVoicePulseLayout)
    (parent as? ViewGroup)?.addView(view)
}

fun Drawable.resizeTo(context: Context, size: Int) =
    BitmapDrawable(context.resources, toBitmap(size, size))

internal fun W3WAutoSuggestEditText.buildCross() {
    cross.layoutParams = ViewGroup.MarginLayoutParams(
        this.height,
        this.height
    )
    cross.isClickable = true
    cross.setOnClickListener {
        this.setText(context.getString(R.string.w3w_slashes))
        this.setSelection(this.length())
    }
    cross.setImageDrawable(
        ContextCompat.getDrawable(context, R.drawable.ic_close)
            ?.resizeTo(context, (this.height * .6).toInt())
            .apply {
                this?.setTint(this@buildCross.currentHintTextColor)
            }
    )
    cross.scaleType = ImageView.ScaleType.CENTER
    cross.apply {
        val diff: Float = if (voiceEnabled) this@buildCross.height * 0.85f else 0f
        this.x =
            (this@buildCross.x + this@buildCross.width - (this@buildCross.height)) - (
            resources.getDimensionPixelSize(
                R.dimen.input_border_height
            )
            ) - diff
        this.y =
            this@buildCross.y
    }
    cross.visibility = GONE
    (parent as? ViewGroup)?.addView(cross)
}

internal fun W3WAutoSuggestEditText.buildVoiceAnimatedPopup() {
    voiceAnimatedPopup = VoicePulseLayout(
        context,
        voicePlaceholder,
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
        setIsVoiceRunning(false, shouldAnimate = false)
    }
    ((parent as? ViewGroup)?.rootView as? ViewGroup)?.addView(voiceAnimatedPopup)
}

internal fun W3WAutoSuggestEditText.buildVoiceFullscreen() {
    voicePulseLayoutFullScreen = VoicePulseLayoutFullScreen(
        context,
        voicePlaceholder,
        voiceBackgroundColor,
        voiceBackgroundDrawable,
        voiceIconsColor
    )
    val displayFrame = Rect()
    (parent as? ViewGroup)?.rootView?.getWindowVisibleDisplayFrame(displayFrame)
    val params = ViewGroup.MarginLayoutParams(
        displayFrame.width(),
        displayFrame.height()
    )
    params.topMargin = displayFrame.top
    params.bottomMargin = displayFrame.bottom
    voicePulseLayoutFullScreen!!.apply {
        visibility = GONE
        layoutParams = params
        this.applySize(min(context.resources.displayMetrics.widthPixels, context.resources.displayMetrics.heightPixels))
        setIsVoiceRunning(false)
    }
    ((parent as? ViewGroup)?.rootView as? ViewGroup)?.addView(voicePulseLayoutFullScreen)
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
            this@buildSuggestionList.y + this@buildSuggestionList.height - resources.getDimensionPixelSize(
                R.dimen.tiny_margin
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
                    0f
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
    } else {
        setCompoundDrawablesRelative(
            if (showTick && !hideSelectedIcon) {
                tick
            } else {
                null
            },
            null,
            drawableStart,
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
