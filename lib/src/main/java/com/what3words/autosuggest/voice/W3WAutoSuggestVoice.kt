package com.what3words.autosuggest.voice

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import com.intentfilter.androidpermissions.PermissionManager
import com.intentfilter.androidpermissions.models.DeniedPermissions
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.autosuggest.BuildConfig
import com.what3words.autosuggest.R
import com.what3words.autosuggest.picker.W3WAutoSuggestPicker
import com.what3words.autosuggest.utils.DisplayMetricsConverter.convertPixelsToDp
import com.what3words.autosuggest.utils.PulseAnimator
import com.what3words.autosuggest.utils.transform
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.Suggestion
import kotlinx.android.synthetic.main.w3w_voice_only.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

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

    private var suggestionsPicker: W3WAutoSuggestPicker? = null
    private var isVoiceRunning: Boolean = false
    private lateinit var pulseAnimator: PulseAnimator

    private var initialSizeList = arrayListOf<Int>()
    private var animatorList = arrayListOf<ValueAnimator.AnimatorUpdateListener>()

    private lateinit var innerUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var middleUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var outerUpdateListener: ValueAnimator.AnimatorUpdateListener
    private lateinit var voicePulseEndListener: Animator.AnimatorListener

    private var key: String? = null
    private var queryMap: MutableMap<String, String> = mutableMapOf()
    private var isEnterprise: Boolean = false
    private var errorMessageText: String? = null
    private var callback: ((suggestions: List<W3WSuggestion>) -> Unit)? =
        null
    private var returnCoordinates: Boolean = false
    private var voiceLanguage: String = "en"
    private var clipToPolygon: Array<Coordinates>? = null
    private var clipToBoundingBox: BoundingBox? = null
    private var clipToCircle: Coordinates? = null
    private var clipToCircleRadius: Double? = null
    private var clipToCountry: Array<String>? = null
    private var nFocusResults: Int? = null
    private var animationRefreshTime: Int = 2
    private var focus: Coordinates? = null
    private var nResults: Int? = null
    private var wrapper: What3WordsV3? = null
    private var builder: VoiceBuilder? = null

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
                nResults = getInteger(R.styleable.W3WAutoSuggestEditText_nResults, 3)
                returnCoordinates =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_returnCoordinates, false)
                voiceLanguage = getString(R.styleable.W3WAutoSuggestEditText_voiceLanguage) ?: "en"
            } finally {
                recycle()
            }
        }

        View.inflate(context, R.layout.w3w_voice_only, this)

        setVoicePulseListeners()

        w3wLogo.setOnClickListener {
            handleVoice()
        }

        // Add a viewTreeObserver to obtain the initial size of the circle overlays
        voicePulseLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                setOverlayBaseSize()
                voicePulseLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun setOverlayBaseSize() {
        initialSizeList = arrayListOf(
            innerCircleView.measuredWidth,
            midCircleView.measuredWidth,
            outerCircleView.measuredWidth
        )
        pulseAnimator = PulseAnimator(
            convertPixelsToDp(innerCircleView.measuredWidth * 1.18f),
            convertPixelsToDp(midCircleView.measuredWidth * 1.32f),
            convertPixelsToDp(outerCircleView.measuredWidth * 1.48f),
            innerCircleView,
            midCircleView,
            outerCircleView,
            animatorList,
            voicePulseEndListener
        )
        setIsVoiceRunning(false)
        pulseAnimator.setInitialSize(initialSizeList)
    }

    private fun setVoicePulseListeners() {
        innerUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val animValue = it.animatedValue as Int
            setLayout(innerCircleView, animValue)
        }
        middleUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val animValue = it.animatedValue as Int
            setLayout(midCircleView, animValue)
        }
        outerUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val animValue = it.animatedValue as Int
            setLayout(outerCircleView, animValue)
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
                                setLayout(innerCircleView, initialSizeList[i])
                            }
                            PulseAnimator.MIDDLE_CIRCLE_INDEX -> {
                                setLayout(midCircleView, initialSizeList[i])
                            }
                            PulseAnimator.OUTER_CIRCLE_INDEX -> {
                                setLayout(innerCircleView, initialSizeList[i])
                            }
                        }
                    }
                    w3wLogo.setImageResource(R.drawable.ic_small_voice)
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

    fun setIsVoiceRunning(isVoiceRunning: Boolean) {
        this.isVoiceRunning = isVoiceRunning
        if (isVoiceRunning) {
            w3wLogo.setImageResource(R.drawable.ic_voice_only_active)
            View.VISIBLE
        } else {
            resetLayout()
            w3wLogo.setImageResource(R.drawable.ic_voice_only_inactive)
            View.INVISIBLE
        }.let {
            innerCircleView.visibility = it
            midCircleView.visibility = it
            outerCircleView.visibility = it
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
        setLayout(innerCircleView, initialSizeList[PulseAnimator.INNER_CIRCLE_INDEX])
        setLayout(midCircleView, initialSizeList[PulseAnimator.MIDDLE_CIRCLE_INDEX])
        setLayout(outerCircleView, initialSizeList[PulseAnimator.OUTER_CIRCLE_INDEX])
    }

    fun setup(builder: VoiceBuilder, microphone: VoiceBuilder.Microphone) {
        if (!isVoiceRunning) {
            var oldTimestamp = System.currentTimeMillis()
            microphone.onListening {
                if (!isVoiceRunning) setIsVoiceRunning(true)
                if (it != null) {
                    if ((System.currentTimeMillis() - oldTimestamp) > animationRefreshTime) {
                        oldTimestamp = System.currentTimeMillis()
                        onSignalUpdate(transform(it))
                    }
                }
            }
            builder.startListening()
        } else {
            builder.stopListening()
            setIsVoiceRunning(false)
        }
    }

    private fun handleVoice() {
        if (wrapper == null) throw Exception("Please use apiKey")
        if (builder?.isListening() == true) {
            builder?.stopListening()
            setIsVoiceRunning(false)
            return
        }
        queryMap.clear()
        queryMap["n-results"] = nResults.toString()
        queryMap["source-api"] = "voice"
        queryMap["voice-language"] = voiceLanguage
        val permissionManager: PermissionManager = PermissionManager.getInstance(context)
        permissionManager.checkPermissions(
            Collections.singleton(Manifest.permission.RECORD_AUDIO),
            object : PermissionManager.PermissionRequestListener {
                override fun onPermissionGranted() {
                    suggestionsPicker?.refreshSuggestions(
                        emptyList(),
                        "",
                        emptyMap(),
                        returnCoordinates
                    )
                    suggestionsPicker?.visibility = GONE
                    val microphone = VoiceBuilder.Microphone()
                    builder = wrapper!!.autosuggest(microphone, voiceLanguage).apply {
                        nResults?.let {
                            this.nResults(it)
                            queryMap["n-results"] = it.toString()
                        }
                        focus?.let {
                            this.focus(it)
                            queryMap["focus"] = it.lat.toString() + "," + it.lng.toString()
                        }
                        nFocusResults?.let {
                            this.nFocusResults(it)
                            queryMap["n-focus-results"] = it.toString()
                        }
                        clipToCountry?.let {
                            this.clipToCountry(it.toList())
                            queryMap["clip-to-country"] = it.joinToString(",")
                        }
                        clipToCircle?.let {
                            this.clipToCircle(it, clipToCircleRadius ?: 0.0)
                            queryMap["clip-to-circle"] =
                                it.lat.toString() + "," + it.lng.toString() + "," + (clipToCircleRadius?.toString()
                                    ?: "0")
                        }
                        clipToBoundingBox?.let {
                            this.clipToBoundingBox(it)
                            queryMap["clip-to-bounding-box"] =
                                it.sw.lat.toString() + "," + it.sw.lng.toString() + "," + it.ne.lat.toString() + "," + it.ne.lng.toString()
                        }
                        clipToPolygon?.let { coordinates ->
                            this.clipToPolygon(coordinates.toList())
                            queryMap["clip-to-polygon"] =
                                coordinates.joinToString(",") { "${it.lat},${it.lng}" }
                        }
                        this.onSuggestions { suggestions ->
                            if (suggestions.isEmpty()) {
                                //TODO showErrorMessage()
                            } else {
                                handleSuggestions(suggestions)
                            }
                            setIsVoiceRunning(false)
                        }
                        this.onError {
                            //TODO showErrorMessage()
                            setIsVoiceRunning(false)
                        }
                    }

                    setup(builder!!, microphone)
                }

                override fun onPermissionDenied(deniedPermissions: DeniedPermissions) {
                    //TODO
                }
            })
    }

    private fun handleSuggestions(suggestions: List<Suggestion>) {
        suggestionsPicker?.let {
            it.visibility = VISIBLE
            it.refreshSuggestions(suggestions, null, queryMap, returnCoordinates)
        } ?: run {
            if (returnCoordinates) {
                CoroutineScope(Dispatchers.IO).launch {
                    val listWithCoordinates = mutableListOf<W3WSuggestion>()
                    suggestions.forEach {
                        val res = wrapper!!.convertToCoordinates(it.words).execute()
                        listWithCoordinates.add(W3WSuggestion(it, res.coordinates))
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        callback?.invoke(
                            listWithCoordinates
                        )
                    }
                }
            } else {
                callback?.invoke(suggestions.map { W3WSuggestion(it, null) })
            }
        }
    }

    //region Properties

    /** Set your What3Words API Key which will be used to get suggestions and coordinates (if enabled)
     *
     * @param key your API key from what3words developer dashboard
     * @ return a
    { @link W3WAutoSuggestEditText } instance
     */
    fun apiKey(key: String): W3WAutoSuggestVoice {
        this.key = key
        wrapper = What3WordsV3(
            key,
            context,
            mapOf("X-W3W-AS-Component" to "what3words-Android/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE})")
        )
        return this
    }

    /** Set your What3Words API Key and the Enterprise Suite API Server endpoint which will be used to get suggestions and coordinates (if enabled)
     *
     * @param key your API key from what3words developer dashboard
     * @param endpoint your Enterprise API endpoint
     * @param headers any custom headers needed for your Enterprise API
     * @ return a
    { @link W3WAutoSuggestEditText } instance
     */
    fun apiKey(
        key: String,
        endpoint: String,
        headers: Map<String, String> = mapOf()
    ): W3WAutoSuggestVoice {
        isEnterprise = true
        this.key = key
        wrapper = What3WordsV3(
            key,
            endpoint,
            context,
            headers
        )
        return this
    }

    fun voiceLanguage(language: String): W3WAutoSuggestVoice {
        this.voiceLanguage = language
        return this
    }

    /**
     * This is a location, specified as a latitude (often where the user making the query is). If specified, the results will be weighted to
     * give preference to those near the <code>focus</code>. For convenience, longitude is allowed to wrap around the 180 line, so 361 is equivalent to 1.
     *
     * @param coordinates the focus to use
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun focus(coordinates: Coordinates?): W3WAutoSuggestVoice {
        focus = coordinates
        return this
    }

    /**
     * Set the number of AutoSuggest results to return. A maximum of 100 results can be specified, if a number greater than this is requested,
     * this will be truncated to the maximum. The default is 3
     *
     * @param n the number of AutoSuggest results to return
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun nResults(n: Int?): W3WAutoSuggestVoice {
        nResults = n ?: 3
        return this
    }

    /**
     * Specifies the number of results (must be &lt;= nResults) within the results set which will have a focus. Defaults to <code>nResults</code>.
     * This allows you to run autosuggest with a mix of focussed and unfocussed results, to give you a "blend" of the two. This is exactly what the old V2
     * <code>standardblend</code> did, and <code>standardblend</code> behaviour can easily be replicated by passing <code>nFocusResults=1</code>,
     * which will return just one focussed result and the rest unfocussed.
     *
     * @param n number of results within the results set which will have a focus
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun nFocusResults(n: Int?): W3WAutoSuggestVoice {
        nFocusResults = n
        return this
    }

    /**
     * Restrict autosuggest results to a circle, specified by <code>Coordinates</code> representing the centre of the circle, plus the
     * <code>radius</code> in kilometres. For convenience, longitude is allowed to wrap around 180 degrees. For example 181 is equivalent to -179.
     *
     * @param centre the centre of the circle
     * @param radius the radius of the circle in kilometres
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun clipToCircle(
        centre: Coordinates?,
        radius: Double?
    ): W3WAutoSuggestVoice {
        clipToCircle = centre
        clipToCircleRadius = radius
        return this
    }

    /**
     * Restricts autosuggest to only return results inside the countries specified by comma-separated list of uppercase ISO 3166-1 alpha-2 country codes
     * (for example, to restrict to Belgium and the UK, use <code>clipToCountry("GB", "BE")</code>. <code>clipToCountry</code> will also accept lowercase
     * country codes. Entries must be two a-z letters. WARNING: If the two-letter code does not correspond to a country, there is no error: API simply
     * returns no results.
     *
     * @param countryCodes countries to clip results too
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun clipToCountry(countryCodes: List<String>): W3WAutoSuggestVoice {
        clipToCountry = if (countryCodes.isNotEmpty()) countryCodes.toTypedArray() else null
        return this
    }

    /**
     * Restrict autosuggest results to a <code>BoundingBox</code>.
     *
     * @param boundingBox <code>BoundingBox</code> to clip results too
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun clipToBoundingBox(
        boundingBox: BoundingBox?
    ): W3WAutoSuggestVoice {
        clipToBoundingBox = boundingBox
        return this
    }

    /**
     * Restrict autosuggest results to a polygon, specified by a collection of <code>Coordinates</code>. The polygon should be closed,
     * i.e. the first element should be repeated as the last element; also the list should contain at least 4 entries. The API is currently limited to
     * accepting up to 25 pairs.
     *
     * @param polygon the polygon to clip results too
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun clipToPolygon(
        polygon: List<Coordinates>
    ): W3WAutoSuggestVoice {
        clipToPolygon = if (polygon.isNotEmpty()) polygon.toTypedArray() else null
        return this
    }

    /**
     * Enable autosuggest results to return coordinates
     *
     * @param enabled if callback should return coordinates
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun returnCoordinates(
        enabled: Boolean
    ): W3WAutoSuggestVoice {
        this.returnCoordinates = enabled
        return this
    }

    /**
     * Enable autosuggest results to return coordinates
     *
     * @param returnCoordinates if callback should return coordinates
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun animationRefreshTime(
        millis: Int
    ): W3WAutoSuggestVoice {
        this.animationRefreshTime = millis
        return this
    }

    /**
     * Add W3WAutoSuggestPicker to allow user to pick one suggestion.
     *
     * @param picker W3WAutoSuggestPicker view
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun picker(
        picker: W3WAutoSuggestPicker
    ): W3WAutoSuggestVoice {
        picker.setup(wrapper!!, isEnterprise, key!!)
        this.suggestionsPicker = picker
        return this
    }

    fun errorMessage(
        error: String
    ): W3WAutoSuggestVoice {
        this.errorMessageText = error
        return this
    }

    fun onSuggestions(callback: (suggestions: List<W3WSuggestion>) -> Unit): W3WAutoSuggestVoice {
        this.callback = callback
        return this
    }
}

data class W3WSuggestion(
    val info: Suggestion,
    val coordinates: com.what3words.javawrapper.response.Coordinates? = null
)