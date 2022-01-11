package com.what3words.components.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import androidx.core.view.updateLayoutParams
import com.what3words.components.R
import com.what3words.components.databinding.VoicePulseLayoutFullScreenBinding
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.components.models.W3WListeningState
import com.what3words.components.text.VoiceScreenType
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.components.voice.W3WAutoSuggestVoice
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion

/**
 * [W3WAutoSuggestEditText.voiceScreenType] [VoiceScreenType.Fullscreen] voice layout.
 *
 * This view will be shown on top of all others and contains the [W3WAutoSuggestVoice] and a close button.
 *
 * @param context view context.
 * @param placeholder the tip text placeholder, this can be changed using attribute [W3WAutoSuggestEditText.voicePlaceholder].
 * @param backgroundColor the fullscreen background color, this can be changed using attribute [W3WAutoSuggestEditText.voiceBackgroundColor].
 * @param backgroundDrawable the fullscreen background drawable, i.e: gradients, etc, this can be changed using attribute [W3WAutoSuggestEditText.voiceBackgroundDrawable].
 * @param iconTintColor the icons and text color, this can be changed using attribute [W3WAutoSuggestEditText] voiceIconsColor.
 * @property isVoiceRunning keeps the state of the voice component, if listening or not, this logic might need a refactor to properly use [W3WAutoSuggestVoice.onListeningStateChanged].
 * @property resultsCallback a callback to subscribe on [W3WAutoSuggestEditText] for when [W3WAutoSuggestVoice] returns suggestions.
 * @property errorCallback a callback to subscribe on [W3WAutoSuggestEditText] for when [W3WAutoSuggestVoice] returns an error.
 * @constructor Creates a new view [VoicePulseLayoutFullScreen] programmatically.
 */
@SuppressLint("ViewConstructor")
internal class VoicePulseLayoutFullScreen
@JvmOverloads constructor(
    context: Context,
    private val placeholder: String,
    backgroundColor: Int,
    backgroundDrawable: Drawable?,
    iconTintColor: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: VoicePulseLayoutFullScreenBinding =
        VoicePulseLayoutFullScreenBinding.inflate(
            LayoutInflater.from(context), this, true
        )

    private var isVoiceRunning: Boolean = false
    private var resultsCallback: Consumer<List<Suggestion>>? = null
    private var errorCallback: Consumer<APIResponse.What3WordsError?>? = null

    init {
        binding.icClose.setColorFilter(iconTintColor)
        binding.icLogo.setColorFilter(iconTintColor)
        binding.voicePlaceholder.setTextColor(iconTintColor)
        if (backgroundDrawable != null) {
            binding.voiceHolder.background = backgroundDrawable
        } else {
            binding.voiceHolder.setBackgroundColor(backgroundColor)
        }

        binding.icClose.setOnClickListener {
            binding.autosuggestVoice.stop()
            setIsVoiceRunning(false)
            errorCallback?.accept(null)
        }

        binding.voicePlaceholder.text = context.getString(R.string.loading)
    }

    fun onResultsCallback(callback: Consumer<List<Suggestion>>) {
        this.resultsCallback = callback
    }

    fun onErrorCallback(callback: Consumer<APIResponse.What3WordsError?>) {
        this.errorCallback = callback
    }

    fun setIsVoiceRunning(isVoiceRunning: Boolean) {
        this.isVoiceRunning = isVoiceRunning
        visibility = if (isVoiceRunning) {
            VISIBLE
        } else {
            GONE
        }
    }

    /**
     * [setup] should be called by [W3WAutoSuggestEditText] having the [AutosuggestLogicManager] which can be SDK or API as a parameter, using the internal [W3WAutoSuggestVoice.manager].
     * This flow should only happen when using [W3WAutoSuggestVoice] inside [W3WAutoSuggestEditText].
     * [W3WAutoSuggestVoice.onInternalResults] callback is needed to receive the suggestions from [W3WAutoSuggestVoice].
     * [W3WAutoSuggestVoice.onListeningStateChanged] callback is needed to hide this view when [W3WAutoSuggestVoice] [W3WListeningState].
     * [W3WAutoSuggestVoice.onError] callback is needed to get any [APIResponse.What3WordsError] returned by [W3WAutoSuggestVoice].
     */
    fun setup(logicManager: AutosuggestLogicManager) {
        binding.autosuggestVoice.manager(logicManager)
            .onInternalResults {
                resultsCallback?.accept(it)
            }.onListeningStateChanged {
                when (it) {
                    W3WListeningState.Connecting -> binding.voicePlaceholder.text =
                        context.getString(R.string.loading)
                    W3WListeningState.Started -> binding.voicePlaceholder.text = placeholder
                    W3WListeningState.Stopped -> setIsVoiceRunning(
                        isVoiceRunning = false
                    )
                }
            }.onError {
                errorCallback?.accept(it)
            }
    }

    /**
     * [toggle] should be called by [W3WAutoSuggestEditText] to toggle the [W3WAutoSuggestVoice] inside the [VoicePulseLayoutFullScreen].
     * if [isVoiceRunning] is true will call [W3WAutoSuggestVoice.stop] and change this view visibility to GONE.
     * if [isVoiceRunning] is false will call [W3WAutoSuggestVoice.start] and change this view visibility to VISIBLE.
     */
    fun toggle(options: AutosuggestOptions, returnCoordinates: Boolean, voiceLanguage: String) {
        if (!isVoiceRunning) {
            setIsVoiceRunning(true)
            binding.autosuggestVoice
                .options(options)
                .returnCoordinates(returnCoordinates)
                .voiceLanguage(voiceLanguage)
                .start()
        } else {
            setIsVoiceRunning(false)
            binding.autosuggestVoice.stop()
        }
    }

    fun applySize(width: Int) {
        binding.autosuggestVoice.updateLayoutParams {
            this.width = (width / 1.6).toInt()
            this.height = (width / 1.6).toInt()
        }
    }
}
