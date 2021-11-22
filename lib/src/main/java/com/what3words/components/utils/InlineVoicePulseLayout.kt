package com.what3words.components.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.constraintlayout.widget.ConstraintLayout
import com.what3words.components.R
import com.what3words.components.models.AutosuggestViewModel
import kotlinx.android.synthetic.main.inline_voice_pulse_layout.view.*

internal class InlineVoicePulseLayout
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val INNER_MAX_SIZE_DP = 48F
        private const val MID_MAX_SIZE_DP = 80F
        private const val OUTER_MAX_SIZE_DP = 128F
    }

    private var isVoiceRunning: Boolean = false
    private var pulseAnimator: PulseAnimator
    private var startVoiceClick: (() -> Unit)? = null

    private var initialSizeList = arrayListOf<Int>()
    private var animatorList = arrayListOf<ValueAnimator.AnimatorUpdateListener>()

    private lateinit var innerUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var middleUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var outerUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var voicePulseEndListener: Animator.AnimatorListener

    init {
        View.inflate(context, R.layout.inline_voice_pulse_layout, this)
        setVoicePulseListeners()

        pulseAnimator = PulseAnimator(
            INNER_MAX_SIZE_DP,
            MID_MAX_SIZE_DP,
            OUTER_MAX_SIZE_DP,
            innerCircleView,
            midCircleView,
            outerCircleView,
            animatorList,
            voicePulseEndListener
        )

        w3wLogo.setOnClickListener {
            startVoiceClick?.invoke()
        }

        // Add a viewTreeObserver to obtain the initial size of the circle overlays
        voicePulseLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
                OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    setOverlayBaseSize()
                    voicePulseLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
    }

    fun setOverlayBaseSize() {
        initialSizeList = arrayListOf(
            innerCircleView.drawable.intrinsicHeight,
            midCircleView.drawable.intrinsicHeight,
            outerCircleView.drawable.intrinsicHeight
        )
        pulseAnimator.setInitialSize(initialSizeList)
    }

    private fun setVoicePulseListeners() {
        innerUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val animValue = it.animatedValue as Int
            setLayout(innerCircleView, animValue)
        }
        middleUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val animValue = it.animatedValue as Int
            setLayout(midCircleView, animValue)
        }
        outerUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val animValue = it.animatedValue as Int
            setLayout(outerCircleView, animValue)
        }
        animatorList.apply {
            add(innerUpdateListener)
            add(middleUpdateListener)
            add(outerUpdateListener)
        }
        voicePulseEndListener = object : Animator.AnimatorListener {

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                Log.d("ANIM_END", "VOICE END REVERSE")
                if (isReverse) {
                    // Animation will ensure the pulse is reset to initial state before animating logo
                    for (i in pulseAnimator.getInitialSize().indices) {
                        when (i) {
                            PulseAnimator.INNER_CIRCLE_INDEX -> {
                                setLayout(innerCircleView, initialSizeList[i])
                            }
                            PulseAnimator.MIDDLE_CIRCLE_INDEX -> {
                                setLayout(midCircleView, initialSizeList[i])
                            }
                            PulseAnimator.OUTER_CIRCLE_INDEX -> {
                                setLayout(innerCircleView, initialSizeList[i])
                            }
                        }
                    }
                    w3wLogo.setImageResource(R.drawable.ic_small_voice)
                }
            }

            override fun onAnimationCancel(animator: Animator?) {
            }

            override fun onAnimationEnd(animator: Animator?) {
                Log.d("ANIM_END", "VOICE END")
            }

            override fun onAnimationRepeat(animator: Animator?) {
            }

            override fun onAnimationStart(animator: Animator?) {
            }
        }
    }

    private fun setLayout(view: View, layoutValue: Int) {
        // overwrite the layout params for the gradient overlay
        val layoutParams = view.layoutParams
        layoutParams.height = layoutValue
        layoutParams.width = layoutValue
        view.layoutParams = layoutParams
        view.requestLayout()
        invalidatePulse(view)
    }

    fun setIsVoiceRunning(isVoiceRunning: Boolean) {
        this.isVoiceRunning = isVoiceRunning
        if (isVoiceRunning) {
            w3wLogo.setImageResource(R.drawable.ic_small_voice_active)
            View.VISIBLE
        } else {
            resetLayout()
            w3wLogo.setImageResource(R.drawable.ic_small_voice)
            View.INVISIBLE
        }.let {
            innerCircleView.visibility = it
            midCircleView.visibility = it
            outerCircleView.visibility = it
        }
    }

    private fun invalidatePulse(view: View) {
        view.postInvalidateOnAnimation()
    }

    private fun onSignalUpdate(signalStrength: Float) {
        pulseAnimator.runAnim(signalStrength)
    }

    private fun resetLayout() {
        if (initialSizeList.isEmpty()) return
        setLayout(innerCircleView, initialSizeList[PulseAnimator.INNER_CIRCLE_INDEX])
        setLayout(midCircleView, initialSizeList[PulseAnimator.MIDDLE_CIRCLE_INDEX])
        setLayout(outerCircleView, initialSizeList[PulseAnimator.OUTER_CIRCLE_INDEX])
    }

    fun onStartVoiceClick(callback: () -> Unit) {
        this.startVoiceClick = callback
    }

    fun setup(viewModel: AutosuggestViewModel) {
        if (!isVoiceRunning) {
            viewModel.microphone.onListening {
                if (!isVoiceRunning) setIsVoiceRunning(true)
                if (it != null) onSignalUpdate(transform(it))
            }
            viewModel.startListening()
        } else {
            viewModel.stopListening()
            setIsVoiceRunning(false)
        }
    }
}
