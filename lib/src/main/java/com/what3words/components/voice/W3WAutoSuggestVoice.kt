package com.what3words.components.voice

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioFormat
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import com.intentfilter.androidpermissions.BuildConfig.VERSION_NAME
import com.intentfilter.androidpermissions.PermissionManager
import com.intentfilter.androidpermissions.models.DeniedPermissions
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.helpers.DefaultDispatcherProvider
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.components.R
import com.what3words.components.databinding.W3wVoiceOnlyBinding
import com.what3words.components.models.AutosuggestApiManager
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.components.models.DisplayUnits
import com.what3words.components.models.W3WListeningState
import com.what3words.components.picker.W3WAutoSuggestPicker
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.components.utils.DisplayMetricsConverter.convertPixelsToDp
import com.what3words.components.utils.PulseAnimator
import com.what3words.components.utils.main
import com.what3words.components.vm.AutosuggestVoiceViewModel
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import java.util.Collections

/**
 * A [View] to simplify the integration of what3words voice auto-suggest API in your app.
 */
class W3WAutoSuggestVoice
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.customW3WAutoSuggestVoiceStyle
) : ConstraintLayout(
    ContextThemeWrapper(context, R.style.W3WAutoSuggestVoiceTheme),
    attrs,
    defStyleAttr
) {

    private var errorJob: Job? = null
    private var selectedSuggestionJob: Job? = null
    private var suggestionsJob: Job? = null
    private var volumeJob: Job? = null
    private var multipleSelectedSuggestionsJob: Job? = null
    private var listeningStateJob: Job? = null

    private var isVoiceRunning: Boolean = false
    private lateinit var pulseAnimator: PulseAnimator

    private var initialSizeList = arrayListOf<Int>()
    private var animatorList = arrayListOf<ValueAnimator.AnimatorUpdateListener>()

    private lateinit var innerUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var middleUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var outerUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var voicePulseEndListener: Animator.AnimatorListener

    private var errorMessageText: String? = null
    private var callback: Consumer<List<SuggestionWithCoordinates>>? =
        null
    private var internalCallback: Consumer<List<Suggestion>>? =
        null
    private var selectedCallback: Consumer<SuggestionWithCoordinates?>? =
        null
    private var errorCallback: Consumer<APIResponse.What3WordsError>? =
        null
    private var onListeningCallback: Consumer<W3WListeningState>? =
        null
    private var returnCoordinates: Boolean = false
    private var voiceLanguage: String
    private var displayUnits: DisplayUnits = DisplayUnits.SYSTEM

    internal val viewModel: AutosuggestVoiceViewModel by lazy {
        AutosuggestVoiceViewModel()
    }

    private var binding: W3wVoiceOnlyBinding = W3wVoiceOnlyBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.W3WAutoSuggestEditText,
            defStyleAttr, R.style.W3WAutoSuggestVoiceTheme
        ).apply {
            try {
                errorMessageText = getString(
                    R.styleable.W3WAutoSuggestEditText_errorMessage
                ) ?: resources.getString(R.string.error_message)
                returnCoordinates =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_returnCoordinates, false)
                voiceLanguage =
                    getString(R.styleable.W3WAutoSuggestEditText_voiceLanguage)
                        ?: "en"
                displayUnits =
                    DisplayUnits.values()[getInt(R.styleable.W3WAutoSuggestEditText_displayUnit, 0)]
            } finally {
                recycle()
            }
        }

        setVoicePulseListeners()

        binding.w3wLogo.setOnClickListener {
            if (isEnabled) {
                val permissionManager: PermissionManager = PermissionManager.getInstance(context)
                permissionManager.checkPermissions(
                    Collections.singleton(Manifest.permission.RECORD_AUDIO),
                    object : PermissionManager.PermissionRequestListener {
                        override fun onPermissionGranted() {
                            viewModel.autosuggest(voiceLanguage)
                        }

                        override fun onPermissionDenied(deniedPermissions: DeniedPermissions) {
                            viewModel.setPermissionError()
                        }
                    }
                )
            }
        }

        // Add a viewTreeObserver to obtain the initial size of the circle overlays
        binding.voicePulseLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
                OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (binding.innerCircleView.measuredWidth != 0) {
                        setOverlayBaseSize()
                        binding.voicePulseLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            })

        viewModel.manager = AutosuggestApiManager(What3WordsV3("", context))
    }

    fun setOverlayBaseSize() {
        initialSizeList = arrayListOf(
            binding.innerCircleView.measuredWidth,
            binding.midCircleView.measuredWidth,
            binding.outerCircleView.measuredWidth
        )
        pulseAnimator = PulseAnimator(
            convertPixelsToDp(binding.innerCircleView.measuredWidth * 1.18f),
            convertPixelsToDp(binding.midCircleView.measuredWidth * 1.32f),
            convertPixelsToDp(binding.outerCircleView.measuredWidth * 1.48f),
            binding.innerCircleView,
            binding.midCircleView,
            binding.outerCircleView,
            animatorList,
            voicePulseEndListener
        )
        setIsVoiceRunning(false)
        pulseAnimator.setInitialSize(initialSizeList)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        listeningStateJob = main(DefaultDispatcherProvider()) {
            viewModel.listeningState.collect {
                listeningStateObserver(it)
            }
        }

        multipleSelectedSuggestionsJob = main(DefaultDispatcherProvider()) {
            viewModel.multipleSelectedSuggestions.collect {
                multipleSelectedSuggestionsObserver(it)
            }
        }

        volumeJob = main(DefaultDispatcherProvider()) {
            viewModel.volume.collect {
                volumeObserver(it)
            }
        }

        suggestionsJob = main(DefaultDispatcherProvider()) {
            viewModel.suggestions.collect {
                suggestionObserver(it)
            }
        }

        selectedSuggestionJob = main(DefaultDispatcherProvider()) {
            viewModel.selectedSuggestion.collect {
                selectedSuggestionObserver(it)
            }
        }

        errorJob = main(DefaultDispatcherProvider()) {
            viewModel.error.collect {
                errorObserver(it)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        volumeJob?.cancel()
        selectedSuggestionJob?.cancel()
        errorJob?.cancel()
        suggestionsJob?.cancel()
        multipleSelectedSuggestionsJob?.cancel()
        listeningStateJob?.cancel()
    }

    private fun suggestionObserver(suggestions: List<Suggestion>) {
        handleSuggestions(suggestions)
        internalCallback?.accept(suggestions)
    }

    private fun errorObserver(error: APIResponse.What3WordsError?) {
        if (error != null) {
            errorCallback?.accept(error)
        }
    }

    private fun listeningStateObserver(state: Pair<W3WListeningState, Boolean>) {
        onListeningCallback?.accept(state.first)
        when (state.first) {
            W3WListeningState.Stopped -> {
                setIsVoiceRunning(isVoiceRunning = false, withError = state.second)
            }
            W3WListeningState.Connecting -> {
                // FUTURE LOADING STATE
            }
            W3WListeningState.Started -> {
                setIsVoiceRunning(isVoiceRunning = true, withError = state.second)
            }
        }
    }

    private fun multipleSelectedSuggestionsObserver(suggestions: List<SuggestionWithCoordinates>) {
        callback?.accept(suggestions)
    }

    private fun selectedSuggestionObserver(suggestion: SuggestionWithCoordinates?) {
        selectedCallback?.accept(suggestion)
    }

    private fun volumeObserver(volume: Float?) {
        volume?.let { onSignalUpdate(it) }
    }

    private fun setVoicePulseListeners() {
        innerUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val animValue = it.animatedValue as Int
            setLayout(binding.innerCircleView, animValue)
        }
        middleUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val animValue = it.animatedValue as Int
            setLayout(binding.midCircleView, animValue)
        }
        outerUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val animValue = it.animatedValue as Int
            setLayout(binding.outerCircleView, animValue)
        }
        animatorList.apply {
            add(innerUpdateListener)
            add(middleUpdateListener)
            add(outerUpdateListener)
        }
        voicePulseEndListener = object : Animator.AnimatorListener {

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                Log.d("ANIM_END", "VOICE END REVERSE")
                if (isReverse) {
                    // Animation will ensure the pulse is reset to initial state before animating logo
                    for (i in pulseAnimator.getInitialSize().indices) {
                        when (i) {
                            PulseAnimator.INNER_CIRCLE_INDEX -> {
                                setLayout(binding.innerCircleView, initialSizeList[i])
                            }
                            PulseAnimator.MIDDLE_CIRCLE_INDEX -> {
                                setLayout(binding.midCircleView, initialSizeList[i])
                            }
                            PulseAnimator.OUTER_CIRCLE_INDEX -> {
                                setLayout(binding.innerCircleView, initialSizeList[i])
                            }
                        }
                    }
                    binding.w3wLogo.setImageResource(R.drawable.ic_small_voice)
                }
            }

            override fun onAnimationCancel(animator: Animator?) {
            }

            override fun onAnimationEnd(animator: Animator?) {
                Log.d("ANIM_END", "VOICE END")
            }

            override fun onAnimationRepeat(animator: Animator?) {
            }

            override fun onAnimationStart(animator: Animator?) {
            }
        }
    }

    private fun setLayout(view: View, layoutValue: Int) {
        // overwrite the layout params for the gradient overlay
        val layoutParams = view.layoutParams
        layoutParams.height = layoutValue
        layoutParams.width = layoutValue
        view.layoutParams = layoutParams
        view.requestLayout()
        invalidatePulse(view)
    }

    private val changeBackIcon: Runnable =
        Runnable { binding.w3wLogo.setImageResource(R.drawable.ic_voice_only_inactive) }

    private fun setIsVoiceRunning(isVoiceRunning: Boolean, withError: Boolean = false) {
        this.isVoiceRunning = isVoiceRunning
        if (isVoiceRunning) {
            handler?.removeCallbacks(changeBackIcon)
            binding.w3wLogo.setImageResource(R.drawable.ic_voice_only_active)
            View.VISIBLE
        } else {
            resetLayout()
            if (withError) {
                binding.w3wLogo.setImageResource(R.drawable.ic_voice_only_error)
                handler?.postDelayed(
                    changeBackIcon,
                    5000
                ) ?: run {
                    changeBackIcon.run()
                }
            } else binding.w3wLogo.setImageResource(R.drawable.ic_voice_only_inactive)
            View.INVISIBLE
        }.let {
            binding.innerCircleView.visibility = it
            binding.midCircleView.visibility = it
            binding.outerCircleView.visibility = it
        }
    }

    private fun invalidatePulse(view: View) {
        view.postInvalidateOnAnimation()
    }

    private fun onSignalUpdate(signalStrength: Float) {
        pulseAnimator.runAnim(signalStrength)
    }

    private fun resetLayout() {
        if (initialSizeList.isEmpty()) return
        setLayout(binding.innerCircleView, initialSizeList[PulseAnimator.INNER_CIRCLE_INDEX])
        setLayout(binding.midCircleView, initialSizeList[PulseAnimator.MIDDLE_CIRCLE_INDEX])
        setLayout(binding.outerCircleView, initialSizeList[PulseAnimator.OUTER_CIRCLE_INDEX])
    }

    private fun handleSuggestions(suggestions: List<Suggestion>) {
        viewModel.onMultipleSuggestionsSelected("", suggestions, returnCoordinates)
    }

    //region Properties
    /** Set your What3Words API Key which will be used to get suggestions and coordinates (if enabled)
     *
     * @param key your API key from what3words developer dashboard
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun apiKey(key: String): W3WAutoSuggestVoice {
        viewModel.manager = AutosuggestApiManager(
            What3WordsV3(
                key,
                context,
                mapOf("X-W3W-AS-Component" to "what3words-Android/$VERSION_NAME (Android ${Build.VERSION.RELEASE})")
            )
        )
        viewModel.setMicrophone(Microphone())
        return this
    }

    //region Properties
    /** Set your What3Words API Key which will be used to get suggestions and coordinates (if enabled)
     *
     * @param key your API key from what3words developer dashboard
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun options(options: AutosuggestOptions): W3WAutoSuggestVoice {
        this.viewModel.options = options
        return this
    }

    /** Set your What3Words API Key and the Enterprise Suite API Server endpoint which will be used to get suggestions and coordinates (if enabled)
     *
     * @param key your API key from what3words developer dashboard
     * @param endpoint your Enterprise API endpoint
     * @param headers any custom headers needed for your Enterprise API
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun apiKey(
        key: String,
        endpoint: String,
        headers: Map<String, String> = mapOf()
    ): W3WAutoSuggestVoice {
        viewModel.manager = AutosuggestApiManager(
            What3WordsV3(
                key,
                endpoint,
                context,
                headers
            )
        )
        viewModel.setMicrophone(Microphone())
        return this
    }

    /** Set your What3Words Manager with your SDK instance
     *
     * @param logicManager manager created using SDK instead of API
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun sdk(
        logicManager: AutosuggestLogicManager
    ): W3WAutoSuggestVoice {
        viewModel.manager = logicManager
        viewModel.setMicrophone(Microphone())
        return this
    }

    /** Set a custom Microphone setup i.e: recording rate, encoding, channel in, etc.
     *
     * @param recordingRate your custom recording rate
     * @param encoding your custom encoding [AudioFormat.ENCODING_]
     * @param channel your custom channel_in [AudioFormat.CHANNEL_IN_]
     * @param format your custom channel_in [AudioFormat.CHANNEL_IN_]
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun microphone(
        recordingRate: Int,
        encoding: Int,
        channel: Int,
        audioSource: Int
    ): W3WAutoSuggestVoice {
        viewModel.setMicrophone(Microphone(recordingRate, encoding, channel, audioSource))
        return this
    }

    /**
     * For voice input, specifies the language our API will be listening for, default is English.
     * Available voice languages:
     * - ar for Arabic
     * - cmn for Mandarin Chinese
     * - de for German
     * - en Global English (default)
     * - es for Spanish
     * - hi for Hindi
     * - ja for Japanese
     * - ko for Korean
     *
     * @param language the voice language (from list above)
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun voiceLanguage(language: String): W3WAutoSuggestVoice {
        voiceLanguage = language
        return this
    }

    /**
     * This is a location [Coordinates], specified as a latitude (often where the user making the query is). If specified, the results will be weighted to
     * give preference to those near the focus. For convenience, longitude is allowed to wrap around the 180 line, so 361 is equivalent to 1.
     *
     * @param coordinates the focus to use
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun focus(coordinates: Coordinates?): W3WAutoSuggestVoice {
        viewModel.options.focus = coordinates
        return this
    }

    /**
     * Set the number of AutoSuggest results to return. A maximum of 100 results can be specified, if a number greater than this is requested,
     * this will be truncated to the maximum. The default is 3
     *
     * @param n the number of AutoSuggest results to return
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun nResults(n: Int?): W3WAutoSuggestVoice {
        viewModel.options.nResults = n ?: 3
        return this
    }

    /**
     * Specifies the number of results within the results set which will have a focus. Defaults to [nResults].
     * This allows you to run autosuggest with a mix of focussed and unfocussed results, to give you a "blend" of the two. This is exactly what the old V2
     * standardblend did, and standardblend behaviour can easily be replicated by passing [nFocusResults] (1)
     * which will return just one focussed result and the rest unfocussed.
     *
     * @param n number of results within the results set which will have a focus
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun nFocusResults(n: Int?): W3WAutoSuggestVoice {
        viewModel.options.nFocusResults = n
        return this
    }

    /**
     * Restrict autosuggest results to a circle, specified by [Coordinates] representing the centre of the circle, plus the
     * [radius] in kilometres. For convenience, longitude is allowed to wrap around 180 degrees. For example 181 is equivalent to -179.
     *
     * @param centre the centre of the circle
     * @param radius the radius of the circle in kilometres
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun clipToCircle(
        centre: Coordinates?,
        radius: Double?
    ): W3WAutoSuggestVoice {
        viewModel.options.clipToCircle = centre
        viewModel.options.clipToCircleRadius = radius
        return this
    }

    /**
     * Restricts autosuggest to only return results inside the countries specified by comma-separated list of uppercase ISO 3166-1 alpha-2 country codes
     * (for example, to restrict to Belgium and the UK, use [clipToCountry] (listOf("GB", "BE")). [clipToCountry] will also accept lowercase
     * country codes. Entries must be two a-z letters. WARNING: If the two-letter code does not correspond to a country, there is no error: API simply
     * returns no results.
     *
     * @param countryCodes countries to clip results too
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun clipToCountry(countryCodes: List<String>): W3WAutoSuggestVoice {
        viewModel.options.clipToCountry =
            if (countryCodes.isNotEmpty()) countryCodes else null
        return this
    }

    /**
     * Restrict autosuggest results to a [BoundingBox].
     *
     * @param boundingBox [BoundingBox] to clip results too
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun clipToBoundingBox(
        boundingBox: BoundingBox?
    ): W3WAutoSuggestVoice {
        viewModel.options.clipToBoundingBox = boundingBox
        return this
    }

    /**
     * Restrict autosuggest results to a polygon, specified by a collection of [Coordinates]. The polygon should be closed,
     * i.e. the first element should be repeated as the last element; also the list should contain at least 4 entries. The API is currently limited to
     * accepting up to 25 pairs.
     *
     * @param polygon the polygon to clip results too
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun clipToPolygon(
        polygon: List<Coordinates>
    ): W3WAutoSuggestVoice {
        viewModel.options.clipToPolygon = if (polygon.isNotEmpty()) polygon else null
        return this
    }

    /**
     * Enable autosuggest results to return coordinates
     *
     * @param enabled if callback should return coordinates
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun returnCoordinates(
        enabled: Boolean
    ): W3WAutoSuggestVoice {
        this.returnCoordinates = enabled
        return this
    }

    /**
     * Allows to set animation refresh time, lower spec devices (i.e wearOS) might require a lower animation refresh rate for performance reasons.
     *
     * @param millis animation refresh time in milliseconds
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun animationRefreshTime(
        millis: Int
    ): W3WAutoSuggestVoice {
        viewModel.setCustomAnimationRefreshTime(millis)
        return this
    }

    fun errorMessage(
        error: String
    ): W3WAutoSuggestVoice {
        this.errorMessageText = error
        return this
    }

    /**
     * onResults without [W3WAutoSuggestPicker] will provide a list of 3 word addresses found using our voice API.
     *
     * @param callback will return a list of [SuggestionWithCoordinates].
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun onResults(callback: Consumer<List<SuggestionWithCoordinates>>): W3WAutoSuggestVoice {
        this.callback = callback
        // this.suggestionsPicker = null
        return this
    }

    internal fun onInternalResults(callback: Consumer<List<Suggestion>>): W3WAutoSuggestVoice {
        this.internalCallback = callback
        // this.suggestionsPicker = null
        return this
    }

//    /**
//     * onResults with will provide the 3 word address selected by the end-user using the [W3WAutoSuggestPicker] provided.
//     *
//     * @param picker [W3WAutoSuggestPicker] to show on screen the list of 3 word addresses found using our voice API.
//     * @param callback will return the [SuggestionWithCoordinates] picked by the end-user, coordinates will be null if returnCoordinates = false.
//     * @return same [W3WAutoSuggestVoice] instance
//     */
//    fun onResults(
//        picker: W3WAutoSuggestPicker,
//        callback: Consumer<SuggestionWithCoordinates?>
//    ): W3WAutoSuggestVoice {
//        this.selectedCallback = callback
//        picker.setup(viewModel, displayUnits)
//        this.suggestionsPicker = picker
//        return this
//    }

    /**
     * onError will provide any errors [APIResponse.What3WordsError] that might happen during the API call
     *
     * @param errorCallback will return [APIResponse.What3WordsError] with information about the error occurred.
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun onError(
        errorCallback: Consumer<APIResponse.What3WordsError>
    ): W3WAutoSuggestVoice {
        this.errorCallback = errorCallback
        return this
    }

    fun start() {
        val permissionManager: PermissionManager = PermissionManager.getInstance(context)
        permissionManager.checkPermissions(
            Collections.singleton(Manifest.permission.RECORD_AUDIO),
            object : PermissionManager.PermissionRequestListener {
                override fun onPermissionGranted() {
                    viewModel.autosuggest(voiceLanguage)
                }

                override fun onPermissionDenied(deniedPermissions: DeniedPermissions) {
                    viewModel.setPermissionError()
                }
            }
        )
        return
    }

    fun stop() {
        viewModel.stopListening()
    }

    /**
     * onListening will return [W3WListeningState] updating the current state of the component (Connecting, Started, Stopped).
     *
     * @param callback will return a [W3WListeningState].
     * @return same [W3WAutoSuggestVoice] instance
     */
    fun onListeningStateChanged(callback: Consumer<W3WListeningState>): W3WAutoSuggestVoice {
        this.onListeningCallback = callback
        return this
    }
}
