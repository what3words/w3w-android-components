package com.what3words.components.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import com.what3words.components.R
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import kotlinx.android.synthetic.main.inline_voice_pulse_layout.view.autosuggestVoice
import kotlinx.android.synthetic.main.inline_voice_pulse_layout.view.fakeClick

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

    init {
        View.inflate(context, R.layout.inline_voice_pulse_layout, this)

        fakeClick.setOnClickListener {
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
        autosuggestVoice.sdk(logicManager)
            .onInternalResults {
                resultsCallback?.accept(it)
            }.onError {
                errorCallback?.accept(it)
            }
    }

    fun toggle(options: AutosuggestOptions, returnCoordinates: Boolean, voiceLanguage: String) {
        if (!isVoiceRunning) {
            setIsVoiceRunning(true)
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
