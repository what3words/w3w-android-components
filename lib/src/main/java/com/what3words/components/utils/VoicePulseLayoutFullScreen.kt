package com.what3words.components.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.media.AudioFormat
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.components.R
import com.what3words.components.databinding.VoicePulseLayoutFullScreenBinding
import com.what3words.components.models.AutosuggestRepository
import com.what3words.components.models.VoiceScreenType
import com.what3words.components.models.W3WListeningState
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
    private val errorLabel: String,
    private val tryAgainLabel: String,
    loadingLabel: String,
    placeholderTextColor: Int,
    backgroundColor: Int,
    backgroundDrawable: Drawable?,
    iconTintColor: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: VoicePulseLayoutFullScreenBinding =
        VoicePulseLayoutFullScreenBinding.inflate(
            LayoutInflater.from(context), null, false
        )

    /**
     * Instance of [BottomSheetDialog] used to show the VoicePulseLayout in an independent native window
     * **/
    private val bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(context)

    private var isVoiceRunning: Boolean = false
    private var resultsCallback: Consumer<List<Suggestion>>? = null
    private var errorCallback: Consumer<APIResponse.What3WordsError?>? = null

    init {
        binding.icClose.setColorFilter(iconTintColor)
        binding.icLogo.setColorFilter(iconTintColor)
        if (backgroundDrawable != null) {
            binding.voiceHolder.background = backgroundDrawable
        } else {
            binding.voiceHolder.setBackgroundColor(backgroundColor)
        }

        binding.icClose.setOnClickListener {
            stopVoiceListener()
        }

        binding.voicePlaceholder.text = loadingLabel
        binding.voicePlaceholder.setTextColor(placeholderTextColor)

        configureBottomSheetDialog()
    }

    fun onResultsCallback(callback: Consumer<List<Suggestion>>) {
        this.resultsCallback = callback
    }

    fun onErrorCallback(callback: Consumer<APIResponse.What3WordsError?>) {
        this.errorCallback = callback
    }

    fun stopVoiceListener(){
        binding.autosuggestVoice.stop()
        setIsVoiceRunning(false, true)
        errorCallback?.accept(null)
    }

    fun setIsVoiceRunning(isVoiceRunning: Boolean, shouldClose: Boolean) {
        this.isVoiceRunning = isVoiceRunning
        if (isVoiceRunning) {
            bottomSheetDialog.show()
            visibility = VISIBLE
        } else {
            if (shouldClose) {
                bottomSheetDialog.hide()
                visibility = GONE
            }
        }
    }

    /**
     * [setup] should be called by [W3WAutoSuggestEditText] having the [AutosuggestRepository] which can be SDK or API as a parameter, using the internal [W3WAutoSuggestVoice.manager].
     * This flow should only happen when using [W3WAutoSuggestVoice] inside [W3WAutoSuggestEditText].
     * [W3WAutoSuggestVoice.onInternalResults] callback is needed to receive the suggestions from [W3WAutoSuggestVoice].
     * [W3WAutoSuggestVoice.onListeningStateChanged] callback is needed to hide this view when [W3WAutoSuggestVoice] [W3WListeningState].
     * [W3WAutoSuggestVoice.onError] callback is needed to get any [APIResponse.What3WordsError] returned by [W3WAutoSuggestVoice].
     */
    fun setup(logicManager: AutosuggestRepository, microphone: Microphone) {
        binding.autosuggestVoice.manager(logicManager, microphone)
            .onInternalResults {
                if (it.isNotEmpty()) {
                    resultsCallback?.accept(it)
                    setIsVoiceRunning(
                        isVoiceRunning = false,
                        shouldClose = true
                    )
                } else showErrorInModal()
            }.onListeningStateChanged {
                if (it == null) return@onListeningStateChanged
                when (it) {
                    W3WListeningState.Connecting -> {
                        binding.voicePlaceholder.text =
                            context.getString(R.string.loading)
                        binding.voiceErrorMessage.visibility = GONE
                    }
                    W3WListeningState.Started -> {
                        binding.voicePlaceholder.text = placeholder
                        binding.autosuggestVoice.isEnabled = false
                    }
                    W3WListeningState.Stopped -> {
                        binding.autosuggestVoice.isEnabled = true
                    }
                }
            }.onError {
                showErrorInModal()
            }
    }

    private fun showErrorInModal() {
        binding.voicePlaceholder.text = tryAgainLabel
        binding.voiceErrorMessage.text = errorLabel
        binding.voiceErrorMessage.visibility = VISIBLE
    }

    private fun configureBottomSheetDialog(){
        bottomSheetDialog.setContentView(
            binding.root, LayoutParams(
                MATCH_PARENT,
                resources.displayMetrics.heightPixels
            )
        )
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.dismissWithAnimation = false
        bottomSheetDialog.behavior.isDraggable = false
        bottomSheetDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        bottomSheetDialog.setCanceledOnTouchOutside(false)
    }

    /**
     * [toggle] should be called by [W3WAutoSuggestEditText] to toggle the [W3WAutoSuggestVoice] inside the [VoicePulseLayoutFullScreen].
     * if [isVoiceRunning] is true will call [W3WAutoSuggestVoice.stop] and change this view visibility to GONE.
     * if [isVoiceRunning] is false will call [W3WAutoSuggestVoice.start] and change this view visibility to VISIBLE.
     */
    fun toggle(options: AutosuggestOptions, returnCoordinates: Boolean, voiceLanguage: String) {
        if (!isVoiceRunning) {
            setIsVoiceRunning(true, false)
            binding.autosuggestVoice
                .options(options)
                .returnCoordinates(returnCoordinates)
                .voiceLanguage(voiceLanguage)
                .start()
        } else {
            setIsVoiceRunning(false, false)
            binding.autosuggestVoice.stop()
        }
    }

    fun applySize(width: Int) {
        binding.autosuggestVoice.updateLayoutParams {
            this.width = (width / 1.6).toInt()
            this.height = (width / 1.6).toInt()
        }
    }

    /** Set a custom Microphone setup i.e: recording rate, encoding, channel in, etc.
     *
     * @param recordingRate your custom recording rate
     * @param encoding your custom encoding i.e [AudioFormat.ENCODING_PCM_16BIT]
     * @param channel your custom channel_in i.e [AudioFormat.CHANNEL_IN_MONO]
     * @param audioSource your audioSource i.e [MediaRecorder.AudioSource.MIC]
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun microphone(
        recordingRate: Int,
        encoding: Int,
        channel: Int,
        audioSource: Int
    ) {
        binding.autosuggestVoice.microphone(recordingRate, encoding, channel, audioSource)
    }
}
