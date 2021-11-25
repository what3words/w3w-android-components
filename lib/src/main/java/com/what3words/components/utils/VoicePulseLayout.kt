package com.what3words.components.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import com.what3words.components.R
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import kotlinx.android.synthetic.main.voice_pulse_layout.view.*
import kotlinx.android.synthetic.main.voice_pulse_layout.view.autosuggestVoice
import kotlinx.android.synthetic.main.voice_pulse_layout.view.icClose
import kotlinx.android.synthetic.main.voice_pulse_layout.view.icLogo
import kotlinx.android.synthetic.main.voice_pulse_layout.view.voiceHolder
import kotlinx.android.synthetic.main.voice_pulse_layout.view.voicePlaceholder

internal class VoicePulseLayout
@JvmOverloads constructor(
    context: Context,
    placeholder: String,
    backgroundColor: Int,
    backgroundDrawable: Drawable?,
    iconTintColor: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val ANIMATION_TIME = 250L
    }

    var isVoiceRunning: Boolean = false
    private var resultsCallback: Consumer<List<Suggestion>>? = null
    private var errorCallback: Consumer<APIResponse.What3WordsError>? = null

    init {
        View.inflate(context, R.layout.voice_pulse_layout, this)

        icClose.setColorFilter(iconTintColor)
        icLogo.setColorFilter(iconTintColor)
        voicePlaceholder.setTextColor(iconTintColor)
        if (backgroundDrawable != null) {
            voiceHolder.background = backgroundDrawable
        } else {
            voiceHolder.setBackgroundColor(backgroundColor)
        }
        icClose.setOnClickListener {
            autosuggestVoice.stop()
            setIsVoiceRunning(false, true)
        }

        voicePlaceholder.text = placeholder
    }

    fun onResultsCallback(callback: Consumer<List<Suggestion>>) {
        this.resultsCallback = callback
    }

    fun onErrorCallback(callback: Consumer<APIResponse.What3WordsError>) {
        this.errorCallback = callback
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
//                    w3wLogo.setImageResource(R.drawable.ic_voice_active)
//                    innerCircleView.visibility = VISIBLE
//                    midCircleView.visibility = VISIBLE
//                    outerCircleView.visibility = VISIBLE
                    icClose.visibility = VISIBLE
                }.start()
            } else {
                voicePlaceholder.visibility = VISIBLE
//                w3wLogo.setImageResource(R.drawable.ic_voice_active)
//                innerCircleView.visibility = VISIBLE
//                midCircleView.visibility = VISIBLE
//                outerCircleView.visibility = VISIBLE
            }
        } else {
            if (shouldAnimate) {
                icClose.visibility = GONE
                voicePlaceholder.visibility = GONE
                voiceHolder.animate().translationY(
                    resources.getDimensionPixelSize(R.dimen.voice_popup_height).toFloat()
                ).setDuration(
                    ANIMATION_TIME
                ).withEndAction {
//                    w3wLogo.setImageResource(R.drawable.ic_voice)
//                    innerCircleView.visibility = GONE
//                    midCircleView.visibility = GONE
//                    outerCircleView.visibility = GONE
                    visibility = GONE
                }.start()
            } else {
                voicePlaceholder.visibility = GONE
//                w3wLogo.setImageResource(R.drawable.ic_voice)
//                innerCircleView.visibility = GONE
//                midCircleView.visibility = GONE
//                outerCircleView.visibility = GONE
            }
        }
    }

    fun setup(logicManager: AutosuggestLogicManager) {
        autosuggestVoice.sdk(logicManager)
            .onInternalResults {
                resultsCallback?.accept(it)
            }.onError {
                errorCallback?.accept(it)
            }
    }

    fun toggle(options: AutosuggestOptions, returnCoordinates: Boolean, voiceLanguage: String) {
        if (!isVoiceRunning) {
            setIsVoiceRunning(true, true)
            autosuggestVoice
                .options(options)
                .returnCoordinates(returnCoordinates)
                .voiceLanguage(voiceLanguage)
                .start()
        } else {
            autosuggestVoice.stop()
        }
    }
}