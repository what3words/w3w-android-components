package com.what3words.autosuggest.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.res.Resources
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView

internal class PulseAnimator(
    private var innerMaxSizeDp: Float,
    private var midMaxSizeDp: Float,
    private var outerMaxSizeDp: Float,
    private var innerCircleView: ImageView,
    private var middleCircleView: ImageView,
    private var outerCircleView: ImageView,
    private var animatorList: MutableList<ValueAnimator.AnimatorUpdateListener>,
    private var voiceEndListener: Animator.AnimatorListener
) {

    private var voicePulseAnimator = AnimatorSet()
    private var innerCircleAnimator = ValueAnimator()
    private var middleCircleAnimator = ValueAnimator()
    private var outerCircleAnimator = ValueAnimator()
    private var initialSizeList = arrayListOf<Int>()
    private var maxSizeList = arrayListOf<Float>()

    init {
        with(DisplayMetricsConverter) {
            maxSizeList = arrayListOf(
                convertDpToPixel(innerMaxSizeDp),
                convertDpToPixel(midMaxSizeDp),
                convertDpToPixel(outerMaxSizeDp)
            )
        }
    }

    private fun setVoicePulseAnimator(signalStrength: Float) {
        initAnimators(signalStrength)
        setupAnimators(innerCircleAnimator, middleCircleAnimator, outerCircleAnimator)
        voicePulseAnimator.apply {
            duration = VOICE_ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            playTogether(innerCircleAnimator, middleCircleAnimator, outerCircleAnimator)
        }
    }

    private fun initAnimators(signalStrength: Float) {
        innerCircleAnimator = ValueAnimator.ofInt(
            innerCircleView.measuredHeight,
            getTargetSize(signalStrength, INNER_CIRCLE_INDEX)
        )
        middleCircleAnimator = ValueAnimator.ofInt(
            middleCircleView.measuredHeight,
            getTargetSize(signalStrength, MIDDLE_CIRCLE_INDEX)
        )
        outerCircleAnimator = ValueAnimator.ofInt(
            outerCircleView.measuredHeight,
            getTargetSize(signalStrength, OUTER_CIRCLE_INDEX)
        )
    }

    private fun setupAnimators(vararg animators: ValueAnimator) {
        animators.forEach { animator ->
            animator.apply {
                duration = VOICE_ANIMATION_DURATION
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener(animatorList[animators.indexOf(animator)])
                repeatMode = ValueAnimator.REVERSE
                repeatCount = REPEAT_COUNT
            }
        }
    }

    private fun getTargetSize(signalStrength: Float, index: Int): Int {
        if (initialSizeList.isEmpty()) return 0
        val targetSize = initialSizeList[index] * signalStrength
        if (targetSize > maxSizeList[index]) {
            return maxSizeList[index].toInt()
        } else {
            if (targetSize < initialSizeList[index]) {
                return initialSizeList[index]
            }
        }
        return (initialSizeList[index] * signalStrength).toInt()
    }

    fun getInitialSize() = initialSizeList

    fun setInitialSize(measuredHeight: ArrayList<Int>) {
        initialSizeList = measuredHeight
    }

    fun isRunning() = voicePulseAnimator.isRunning

    fun addEndListener() = voicePulseAnimator.addListener(voiceEndListener)

    fun runAnim(signalStrength: Float) {
        setVoicePulseAnimator(signalStrength)
        voicePulseAnimator.start()
    }

    companion object {

        const val INNER_CIRCLE_INDEX = 0
        const val MIDDLE_CIRCLE_INDEX = 1
        const val OUTER_CIRCLE_INDEX = 2

        private const val REPEAT_COUNT = 1

        private const val VOICE_ANIMATION_DURATION = 200L
    }

}

internal object DisplayMetricsConverter {

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float): Float {
        return dp * Resources.getSystem().displayMetrics.density
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px: Float): Float {
        return px / Resources.getSystem().displayMetrics.density
    }
}

internal fun getScaledSignal(
    valueIn: Float,
    minScaled: Float,
    maxScaled: Float
): Float {
    return (maxScaled - minScaled) * (valueIn - MIN_SIGNAL_LEVEL) / (MAX_SIGNAL_LEVEL - MIN_SIGNAL_LEVEL) + minScaled
}

internal fun transform(
    dBValue: Float,
    minScaled: Float = MIN_SCALED_LEVEL,
    maxScaled: Float = MAX_SCALED_LEVEL
) =
    when {
        dBValue < MIN_SIGNAL_LEVEL -> minScaled
        dBValue > MAX_SIGNAL_LEVEL -> maxScaled
        getScaledSignal(dBValue, minScaled, maxScaled) < minScaled -> minScaled
        getScaledSignal(dBValue, minScaled, maxScaled) > maxScaled -> maxScaled
        else -> getScaledSignal(dBValue, minScaled, maxScaled)
    }

private val MIN_SIGNAL_LEVEL = 0.0f
private val MAX_SIGNAL_LEVEL = 1.0f

private val MIN_SCALED_LEVEL = 1f
private val MAX_SCALED_LEVEL = 2.25f