package com.what3words.components.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.res.Resources
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import kotlin.math.min

internal class PulseAnimator(
    innerMaxSizePixel: Float,
    midMaxSizePixel: Float,
    outerMaxSizePixel: Float,
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
        maxSizeList = arrayListOf(
            innerMaxSizePixel,
            midMaxSizePixel,
            outerMaxSizePixel
        )
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
        val minSize = initialSizeList[index].toFloat()
        val maxSize = maxSizeList[index]
        return getScaledSignal(signalStrength, minSize, maxSize).toInt()
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


internal fun getScaledSignal(
    valueIn: Float,
    minScaled: Float,
    maxScaled: Float
): Float {
    return (maxScaled - minScaled) * (valueIn - 0.0f) / (1.0f - 0.0f) + minScaled
}