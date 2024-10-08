package com.what3words.components.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.media.AudioFormat
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.components.R
import com.what3words.components.databinding.VoicePulseLayoutBinding
import com.what3words.components.models.AutosuggestRepository
import com.what3words.components.models.VoiceScreenType
import com.what3words.components.models.W3WListeningState
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.components.voice.W3WAutoSuggestVoice
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion

/**
 * [W3WAutoSuggestEditText.voiceScreenType] [VoiceScreenType.AnimatedPopup] voice layout.
 *
 * This view will animate from bottom to top with a shadow background, contains the [W3WAutoSuggestVoice] and a close button.
 *
 * @param context view context.
 * @param placeholder the tip text placeholder, this can be changed using attribute [W3WAutoSuggestEditText.voicePlaceholder].
 * @param backgroundColor the fullscreen background color, this can be changed using attribute [W3WAutoSuggestEditText.voiceBackgroundColor].
 * @param backgroundDrawable the fullscreen background drawable, i.e: gradients, etc, this can be changed using attribute [W3WAutoSuggestEditText.voiceBackgroundDrawable].
 * @param iconTintColor the icons and text color, this can be changed using attribute [W3WAutoSuggestEditText] voiceIconsColor.
 * @property isVoiceRunning keeps the state of the voice component, if listening or not, this logic might need a refactor to properly use [W3WAutoSuggestVoice.onListeningStateChanged].
 * @property resultsCallback a callback to subscribe on [W3WAutoSuggestEditText] for when [W3WAutoSuggestVoice] returns suggestions.
 * @property errorCallback a callback to subscribe on [W3WAutoSuggestEditText] for when [W3WAutoSuggestVoice] returns an error.
 * @constructor Creates a new view [VoicePulseLayout] programmatically.
 */
@SuppressLint("ViewConstructor")
internal class VoicePulseLayout
@JvmOverloads constructor(
    context: Context,
    private val placeholder: String,
    val errorLabel: String,
    val tryAgainLabel: String,
    loadingLabel: String,
    placeholderTextColor: Int,
    backgroundColor: Int,
    backgroundDrawable: Drawable?,
    iconTintColor: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val ANIMATION_TIME = 250L
    }

    private var isVoiceRunning: Boolean = false
    private var resultsCallback: Consumer<List<Suggestion>>? = null
    private var errorCallback: Consumer<APIResponse.What3WordsError?>? = null
    private var binding: VoicePulseLayoutBinding = VoicePulseLayoutBinding.inflate(
        LayoutInflater.from(context), null, false
    )
    /**
     * Instance of [BottomSheetDialog] used to show the VoicePulseLayout in an independent native window
     * **/
    private val bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(context)

    init {
        binding.icLogo.setColorFilter(iconTintColor)
        if (backgroundDrawable != null) {
            binding.voiceHolder.background = backgroundDrawable
        } else {
            binding.voiceHolder.setBackgroundColor(backgroundColor)
        }
        binding.icClose.setOnClickListener {
            stopVoiceListener()
        }

        binding.voiceHolderFullscreen.setOnClickListener {
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

    fun setIsVoiceRunning(isVoiceRunning: Boolean, shouldAnimate: Boolean, shouldClose: Boolean) {
        this.isVoiceRunning = isVoiceRunning
        if (isVoiceRunning) {
            bottomSheetDialog.show()
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
            if (shouldClose) {
                bottomSheetDialog.dismiss()
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
                if(it.isNotEmpty()) {
                    resultsCallback?.accept(it)
                    setIsVoiceRunning(
                        isVoiceRunning = false,
                        shouldAnimate = true,
                        shouldClose = true
                    )
                }
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

    fun stopVoiceListener(){
        binding.autosuggestVoice.stop()
        setIsVoiceRunning(isVoiceRunning = false, shouldAnimate = true, shouldClose = true)
        errorCallback?.accept(null)
    }

    private fun configureBottomSheetDialog(){
        // configure bottom sheet dialog
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        bottomSheetDialog.window?.setBackgroundDrawableResource(R.drawable.bg_voice)
        bottomSheetDialog.setOnCancelListener {
            stopVoiceListener()
        }
        bottomSheetDialog.behavior.isDraggable = false
    }

    /**
     * [toggle] should be called by [W3WAutoSuggestEditText] to toggle the [W3WAutoSuggestVoice] inside the [VoicePulseLayout].
     * if [isVoiceRunning] is true will call [W3WAutoSuggestVoice.stop].
     * if [isVoiceRunning] is false will call [W3WAutoSuggestVoice.start] and change this view visibility to VISIBLE with animation.
     */
    fun toggle(options: AutosuggestOptions, returnCoordinates: Boolean, voiceLanguage: String) {
        if (!isVoiceRunning) {
            setIsVoiceRunning(isVoiceRunning = true, shouldAnimate = true, shouldClose = false)
            binding.autosuggestVoice
                .options(options)
                .returnCoordinates(returnCoordinates)
                .voiceLanguage(voiceLanguage)
                .start()
        } else {
            binding.autosuggestVoice.stop()
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
