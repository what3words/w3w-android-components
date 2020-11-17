package com.what3words.autosuggest.voiceutils

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.constraintlayout.widget.ConstraintLayout
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.autosuggest.R
import kotlinx.android.synthetic.main.voice_pulse_layout.view.*

class VoicePulseLayout
@JvmOverloads constructor(
    context: Context,
    placeholder: String,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val INNER_MAX_SIZE_DP = 104F
        private const val MID_MAX_SIZE_DP = 152F
        private const val OUTER_MAX_SIZE_DP = 216F
        private const val ANIMATION_TIME = 250L
    }

    var isVoiceRunning: Boolean = false
    private var pulseAnimator: PulseAnimator

    private var initialSizeList = arrayListOf<Int>()
    private var animatorList = arrayListOf<ValueAnimator.AnimatorUpdateListener>()

    private lateinit var innerUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var middleUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var outerUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var voicePulseEndListener: Animator.AnimatorListener
    private var closeCallback: (() -> Unit)? = null
    private var onToggle: (() -> Unit)? = null

    init {
        View.inflate(context, R.layout.voice_pulse_layout, this)
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

        // Add a viewTreeObserver to obtain the initial size of the circle overlays
        voicePulseLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                setOverlayBaseSize()
                voicePulseLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        icClose.setOnClickListener {
            closeCallback?.invoke()
        }

        voiceHolderFullscreen.setOnClickListener {
            closeCallback?.invoke()
        }

        w3wLogo.setOnClickListener {
            onToggle?.invoke()
        }

        voicePlaceholder.text = placeholder
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
                    w3wLogo.setImageResource(R.drawable.ic_voice)
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

    fun setIsVoiceRunning(isVoiceRunning: Boolean, shouldAnimate: Boolean) {
        this.isVoiceRunning = isVoiceRunning
        if (isVoiceRunning) {
            if (shouldAnimate) {
                visibility = VISIBLE
                voicePlaceholder.visibility = VISIBLE
                voiceHolder.animate().translationY(
                    0f
                ).setDuration(
                    ANIMATION_TIME
                ).withEndAction {
                    w3wLogo.setImageResource(R.drawable.ic_voice_active)
                    innerCircleView.visibility = VISIBLE
                    midCircleView.visibility = VISIBLE
                    outerCircleView.visibility = VISIBLE
                    icClose.visibility = VISIBLE
                }.start()
            } else {
                voicePlaceholder.visibility = VISIBLE
                w3wLogo.setImageResource(R.drawable.ic_voice_active)
                innerCircleView.visibility = VISIBLE
                midCircleView.visibility = VISIBLE
                outerCircleView.visibility = VISIBLE
            }
        } else {
            resetLayout()
            if (shouldAnimate) {
                icClose.visibility = GONE
                voicePlaceholder.visibility = GONE
                voiceHolder.animate().translationY(
                    resources.getDimensionPixelSize(R.dimen.voice_popup_height).toFloat()
                ).setDuration(
                    ANIMATION_TIME
                ).withEndAction {
                    w3wLogo.setImageResource(R.drawable.ic_voice)
                    innerCircleView.visibility = GONE
                    midCircleView.visibility = GONE
                    outerCircleView.visibility = GONE
                    visibility = GONE
                }.start()
            } else {
                voicePlaceholder.visibility = GONE
                w3wLogo.setImageResource(R.drawable.ic_voice)
                innerCircleView.visibility = GONE
                midCircleView.visibility = GONE
                outerCircleView.visibility = GONE
            }
        }
    }

    private fun invalidatePulse(view: View) {
        view.postInvalidateOnAnimation()
    }

    fun onSignalUpdate(signalStrength: Float) {
        pulseAnimator.runAnim(signalStrength)
    }

    fun onCloseCallback(callback: () -> Unit): VoicePulseLayout {
        this.closeCallback = callback
        return this
    }

    private fun resetLayout() {
        if (initialSizeList.isEmpty()) return
        setLayout(innerCircleView, initialSizeList[PulseAnimator.INNER_CIRCLE_INDEX])
        setLayout(midCircleView, initialSizeList[PulseAnimator.MIDDLE_CIRCLE_INDEX])
        setLayout(outerCircleView, initialSizeList[PulseAnimator.OUTER_CIRCLE_INDEX])
    }

    fun setup(builder: VoiceBuilder, microphone: VoiceBuilder.Microphone) {
        microphone.onListening {
            if (it != null) {
                onSignalUpdate(transform(it))
                if (it > 0.7) voicePlaceholder.visibility = GONE
            }
        }
        onToggle = {
            if (isVoiceRunning) {
                builder.stopListening()
                setIsVoiceRunning(false, shouldAnimate = false)
            } else {
                builder.startListening()
                setIsVoiceRunning(true, shouldAnimate = false)
            }
        }
        setIsVoiceRunning(true, shouldAnimate = true)
        voicePlaceholder.visibility = VISIBLE
        builder.startListening()
    }
}