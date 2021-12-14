package com.what3words.components.utils

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import com.what3words.components.databinding.InlineVoicePulseLayoutBinding
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion

internal class InlineVoicePulseLayout
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var isVoiceRunning: Boolean = false
    private var startVoiceClick: (() -> Unit)? = null
    private var resultsCallback: Consumer<List<Suggestion>>? = null
    private var errorCallback: Consumer<APIResponse.What3WordsError>? = null

    private var binding: InlineVoicePulseLayoutBinding = InlineVoicePulseLayoutBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        binding.fakeClick.setOnClickListener {
            startVoiceClick?.invoke()
        }
    }

    fun onStartVoiceClick(callback: () -> Unit) {
        this.startVoiceClick = callback
    }

    fun onResultsCallback(callback: Consumer<List<Suggestion>>) {
        this.resultsCallback = callback
    }

    fun onErrorCallback(callback: Consumer<APIResponse.What3WordsError>) {
        this.errorCallback = callback
    }

    fun setIsVoiceRunning(isVoiceRunning: Boolean) {
        this.isVoiceRunning = isVoiceRunning
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
