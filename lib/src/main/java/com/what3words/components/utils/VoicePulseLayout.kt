package com.what3words.components.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import com.what3words.components.R
import com.what3words.components.databinding.VoicePulseLayoutBinding
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion

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
    private var binding: VoicePulseLayoutBinding = VoicePulseLayoutBinding.inflate(
        LayoutInflater.from(context), this, true)


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
            setIsVoiceRunning(false, true)
        }

        binding.voicePlaceholder.text = placeholder
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
                binding.voicePlaceholder.visibility = VISIBLE
                binding.voiceHolder.animate().translationY(
                    0f
                ).setDuration(
                    ANIMATION_TIME
                ).withEndAction {
                    binding.icClose.visibility = VISIBLE
                }.start()
            } else {
                binding.voicePlaceholder.visibility = VISIBLE
            }
        } else {
            if (shouldAnimate) {
                binding.icClose.visibility = GONE
                binding.voicePlaceholder.visibility = GONE
                binding.voiceHolder.animate().translationY(
                    resources.getDimensionPixelSize(R.dimen.voice_popup_height).toFloat()
                ).setDuration(
                    ANIMATION_TIME
                ).withEndAction {
                    visibility = GONE
                }.start()
            } else {
                binding.voicePlaceholder.visibility = GONE
            }
        }
    }

    fun setup(logicManager: AutosuggestLogicManager) {
        binding.autosuggestVoice.sdk(logicManager)
            .onInternalResults {
                resultsCallback?.accept(it)
            }.onError {
                errorCallback?.accept(it)
            }
    }

    fun toggle(options: AutosuggestOptions, returnCoordinates: Boolean, voiceLanguage: String) {
        if (!isVoiceRunning) {
            setIsVoiceRunning(true, true)
            binding.autosuggestVoice
                .options(options)
                .returnCoordinates(returnCoordinates)
                .voiceLanguage(voiceLanguage)
                .start()
        } else {
            binding.autosuggestVoice.stop()
        }
    }
}
