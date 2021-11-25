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
import kotlinx.android.synthetic.main.voice_pulse_layout_full_screen.view.autosuggestVoice
import kotlinx.android.synthetic.main.voice_pulse_layout_full_screen.view.icClose
import kotlinx.android.synthetic.main.voice_pulse_layout_full_screen.view.icLogo
import kotlinx.android.synthetic.main.voice_pulse_layout_full_screen.view.voiceHolder
import kotlinx.android.synthetic.main.voice_pulse_layout_full_screen.view.voicePlaceholder

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

    var isVoiceRunning: Boolean = false
    private var resultsCallback: Consumer<List<Suggestion>>? = null
    private var errorCallback: Consumer<APIResponse.What3WordsError>? = null

    init {
        View.inflate(context, R.layout.voice_pulse_layout_full_screen, this)

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
            setIsVoiceRunning(false)
        }

        voicePlaceholder.text = placeholder
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
            voicePlaceholder.visibility = VISIBLE
            visibility = VISIBLE
        } else {
            voicePlaceholder.visibility = GONE
            visibility = GONE
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