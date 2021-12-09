package com.what3words.components.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import com.what3words.components.databinding.VoicePulseLayoutFullScreenBinding
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion

internal class VoicePulseLayoutFullScreen
@JvmOverloads constructor(
    context: Context,
    placeholder: String,
    backgroundColor: Int,
    backgroundDrawable: Drawable?,
    iconTintColor: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: VoicePulseLayoutFullScreenBinding = VoicePulseLayoutFullScreenBinding.inflate(
        LayoutInflater.from(context), this, true)

    var isVoiceRunning: Boolean = false
    private var resultsCallback: Consumer<List<Suggestion>>? = null
    private var errorCallback: Consumer<APIResponse.What3WordsError>? = null

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
        }

        binding.voicePlaceholder.text = placeholder
    }

    fun onResultsCallback(callback: Consumer<List<Suggestion>>) {
        this.resultsCallback = callback
    }

    fun onErrorCallback(callback: Consumer<APIResponse.What3WordsError>) {
        this.errorCallback = callback
    }

    fun setIsVoiceRunning(isVoiceRunning: Boolean) {
        this.isVoiceRunning = isVoiceRunning
        if (isVoiceRunning) {
            binding.voicePlaceholder.visibility = VISIBLE
            visibility = VISIBLE
        } else {
            binding.voicePlaceholder.visibility = GONE
            visibility = GONE
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
            setIsVoiceRunning(true)
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
