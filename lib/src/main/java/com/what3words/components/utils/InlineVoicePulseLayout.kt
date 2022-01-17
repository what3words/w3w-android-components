package com.what3words.components.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import com.what3words.components.databinding.InlineVoicePulseLayoutBinding
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.components.models.W3WListeningState
import com.what3words.components.text.VoiceScreenType
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.components.voice.W3WAutoSuggestVoice
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion

/**
 * [W3WAutoSuggestEditText.voiceScreenType] [VoiceScreenType.Inline] voice layout.
 *
 * This view will be always visible and will start [W3WAutoSuggestVoice] inline or work as a button to toggle [VoicePulseLayoutFullScreen] and [VoicePulseLayout] depending on [W3WAutoSuggestEditText.voiceScreenType].
 *
 * @param context view context.
 * @property isVoiceRunning keeps the state of the voice component, if listening or not, this logic might need a refactor to properly use [W3WAutoSuggestVoice.onListeningStateChanged].
 * @property resultsCallback a callback to subscribe on [W3WAutoSuggestEditText] for when [W3WAutoSuggestVoice] returns suggestions.
 * @property errorCallback a callback to subscribe on [W3WAutoSuggestEditText] for when [W3WAutoSuggestVoice] returns an error.
 * @constructor Creates a new view [InlineVoicePulseLayout] programmatically.
 */
internal class InlineVoicePulseLayout
@JvmOverloads constructor(
    context: Context,
    iconColor: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var isVoiceRunning: Boolean = false
    private var startVoiceClick: (() -> Unit)? = null
    private var listeningStateCallback: Consumer<W3WListeningState>? = null
    private var resultsCallback: Consumer<List<Suggestion>>? = null
    private var errorCallback: Consumer<APIResponse.What3WordsError>? = null

    private var binding: InlineVoicePulseLayoutBinding = InlineVoicePulseLayoutBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        binding.fakeClick.setOnClickListener {
            startVoiceClick?.invoke()
        }
        binding.icMic.setColorFilter(iconColor)
    }

    fun onStartVoiceClick(callback: () -> Unit) {
        this.startVoiceClick = callback
    }

    fun onListeningStateChanged(callback: Consumer<W3WListeningState>) {
        this.listeningStateCallback = callback
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

    /**
     * [setup] should be called by [W3WAutoSuggestEditText] having the [AutosuggestLogicManager] which can be SDK or API as a parameter, using the internal [W3WAutoSuggestVoice.manager].
     * This flow should only happen when using [W3WAutoSuggestVoice] inside [W3WAutoSuggestEditText].
     * [W3WAutoSuggestVoice.onInternalResults] callback is needed to receive the suggestions from [W3WAutoSuggestVoice].
     * [W3WAutoSuggestVoice.onError] callback is needed to get any [APIResponse.What3WordsError] returned by [W3WAutoSuggestVoice].
     */
    fun setup(logicManager: AutosuggestLogicManager) {
        binding.autosuggestVoice.manager(logicManager)
            .onInternalResults {
                resultsCallback?.accept(it)
            }.onListeningStateChanged {
                if (it == null) return@onListeningStateChanged
                listeningStateCallback?.accept(it)
                when (it) {
                    W3WListeningState.Connecting -> {
                        binding.autosuggestVoice.visibility = VISIBLE
                        binding.icMic.visibility = GONE
                    }
                    W3WListeningState.Started -> {
                        binding.autosuggestVoice.visibility = VISIBLE
                        binding.icMic.visibility = GONE
                    }
                    W3WListeningState.Stopped -> {
                        binding.autosuggestVoice.visibility = INVISIBLE
                        binding.icMic.visibility = VISIBLE
                        setIsVoiceRunning(false)
                    }
                }
            }.onError {
                errorCallback?.accept(it)
            }
    }

    /**
     * [toggle] should be called by [W3WAutoSuggestEditText] to toggle the [W3WAutoSuggestVoice] inside the [InlineVoicePulseLayout].
     * if [isVoiceRunning] is true will call [W3WAutoSuggestVoice.stop] and update [isVoiceRunning] accordingly.
     * if [isVoiceRunning] is false will call [W3WAutoSuggestVoice.start] and update [isVoiceRunning] accordingly.
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

    fun setCustomIcon(icon: Drawable) {
        binding.icMic.setImageDrawable(icon)
    }
}
