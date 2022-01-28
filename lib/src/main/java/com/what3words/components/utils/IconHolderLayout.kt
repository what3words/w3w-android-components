package com.what3words.components.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.text.TextUtilsCompat
import androidx.core.util.Consumer
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import com.what3words.components.R
import com.what3words.components.databinding.IconHolderLayoutBinding
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.components.models.VoiceScreenType
import com.what3words.components.models.W3WListeningState
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.components.voice.W3WAutoSuggestVoice
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import java.util.Locale

/**
 * This view will hold all icons on [W3WAutoSuggestEditText] starting with [VoiceScreenType.Inline] animation and clear text feature, more icons to be added in the future.
 * This view automatically support RTL and LTR with use of constraints, some complex view size calculations related to the voice animation cut around [W3WAutoSuggestEditText] edges.
 *
 * @param iconColor sets all action icons colors (except voice animations).
 * @param iconClearTextColor set clear text icon color.
 * @property isVoiceRunning keeps the state of the voice component, if listening or not, this logic might need a refactor to properly use [W3WAutoSuggestVoice.onListeningStateChanged].
 * @property resultsCallback a callback to subscribe on [W3WAutoSuggestEditText] for when [W3WAutoSuggestVoice] returns suggestions.
 * @property errorCallback a callback to subscribe on [W3WAutoSuggestEditText] for when [W3WAutoSuggestVoice] returns an error.
 * @property listeningStateCallback a callback to subscribe on [W3WAutoSuggestEditText] for when [W3WAutoSuggestVoice] listening status changes, this will help with setting different hints/hiding views if needed.
 * @property clearTextClick a callback to subscribe on [W3WAutoSuggestEditText] for when clear text icon is clicked.
 * @property startVoiceClick a callback to subscribe on [W3WAutoSuggestEditText] for when voice icon is clicked.
 * @constructor Creates a new view [IconHolderLayout] programmatically.
 */
internal class IconHolderLayout
@JvmOverloads constructor(
    context: Context,
    iconColor: Int = -1,
    iconClearTextColor: Int = -1,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr), ViewTreeObserver.OnGlobalLayoutListener {

    private var isVoiceRunning: Boolean = false
    private var startVoiceClick: (() -> Unit)? = null
    private var clearTextClick: (() -> Unit)? = null
    private var listeningStateCallback: Consumer<W3WListeningState>? = null
    private var resultsCallback: Consumer<List<Suggestion>>? = null
    private var errorCallback: Consumer<APIResponse.What3WordsError>? = null

    private var binding: IconHolderLayoutBinding = IconHolderLayoutBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private val autosuggestVoice: W3WAutoSuggestVoice by lazy {
        W3WAutoSuggestVoice(context)
    }

    init {
        binding.fakeClick.setOnClickListener {
            startVoiceClick?.invoke()
        }
        if (iconColor != -1) binding.icMic.setColorFilter(iconColor)
        if (iconClearTextColor != -1) binding.btnClear.setColorFilter(iconClearTextColor)
        binding.btnClear.setOnClickListener {
            clearTextClick?.invoke()
        }
        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    fun onStartVoiceClick(callback: () -> Unit) {
        this.startVoiceClick = callback
    }

    fun onClearTextClick(callback: () -> Unit) {
        this.clearTextClick = callback
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
        autosuggestVoice.manager(logicManager)
            .onInternalResults {
                resultsCallback?.accept(it)
            }.onListeningStateChanged {
                if (it == null) return@onListeningStateChanged
                listeningStateCallback?.accept(it)
                when (it) {
                    W3WListeningState.Connecting -> {
                        autosuggestVoice.visibility = VISIBLE
                        setVoiceVisibility(GONE)
                    }
                    W3WListeningState.Started -> {
                        autosuggestVoice.visibility = VISIBLE
                        setVoiceVisibility(GONE)
                    }
                    W3WListeningState.Stopped -> {
                        setIsVoiceRunning(false)
                        autosuggestVoice.visibility = INVISIBLE
                        setVoiceVisibility(VISIBLE)
                    }
                }
            }.onError {
                errorCallback?.accept(it)
            }
    }

    /**
     * [toggle] should be called by [W3WAutoSuggestEditText] to toggle the [W3WAutoSuggestVoice] inside the [IconHolderLayout].
     * if [isVoiceRunning] is true will call [W3WAutoSuggestVoice.stop] and update [isVoiceRunning] accordingly.
     * if [isVoiceRunning] is false will call [W3WAutoSuggestVoice.start] and update [isVoiceRunning] accordingly.
     */
    fun toggle(options: AutosuggestOptions, returnCoordinates: Boolean, voiceLanguage: String) {
        if (!isVoiceRunning) {
            setIsVoiceRunning(true)
            autosuggestVoice
                .options(options)
                .returnCoordinates(returnCoordinates)
                .voiceLanguage(voiceLanguage)
                .start()
        } else {
            setIsVoiceRunning(false)
            autosuggestVoice.stop()
        }
    }

    fun setCustomIcon(icon: Drawable) {
        binding.icMic.setImageDrawable(icon)
    }

    override fun onGlobalLayout() {
        if (binding.voicePulseLayout.height != 0) {
            autosuggestVoice.layoutParams = MarginLayoutParams(
                (binding.voicePulseLayout.height * 2),
                (binding.voicePulseLayout.height)
            )
            autosuggestVoice.visibility = INVISIBLE
            binding.autosuggestVoiceHolder.addView(autosuggestVoice)
            binding.voicePulseLayout.apply {
                if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                    x += (binding.voicePulseLayout.height / 2)
                } else {
                    x -= (binding.voicePulseLayout.height / 2)
                }
            }
            binding.btnClear.updateLayoutParams {
                width = (this@IconHolderLayout.height * 0.6).toInt()
            }
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }

    fun setClearVisibility(visibility: Int) {
        binding.btnClear.visibility = visibility
    }

    fun setVoiceVisibility(visibility: Int) {
        binding.icMic.visibility = visibility
        binding.fakeClick.visibility = visibility

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.voicePulseLayout)
        constraintSet.clear(binding.btnClear.id, ConstraintSet.END)
        if (visibility == VISIBLE) {
            constraintSet.connect(
                binding.btnClear.id,
                ConstraintSet.END,
                binding.icMic.id,
                ConstraintSet.START,
                resources.getDimensionPixelSize(R.dimen.medium_margin)
            )
        } else {
            constraintSet.connect(
                binding.btnClear.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                    (this.height / 2) + resources.getDimensionPixelSize(R.dimen.large_margin)
                } else {
                    (this.height / 2) + resources.getDimensionPixelSize(R.dimen.large_margin)
                }
            )
        }
        constraintSet.applyTo(binding.voicePulseLayout)
    }
}
