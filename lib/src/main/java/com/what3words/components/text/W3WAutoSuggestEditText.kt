package com.what3words.components.text

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.intentfilter.androidpermissions.BuildConfig.VERSION_NAME
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.helpers.didYouMean3wa
import com.what3words.androidwrapper.helpers.isPossible3wa
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.components.R
import com.what3words.components.error.W3WAutoSuggestErrorMessage
import com.what3words.components.error.showError
import com.what3words.components.models.AutosuggestApiManager
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.components.models.AutosuggestViewModel
import com.what3words.components.models.DisplayUnits
import com.what3words.components.picker.W3WAutoSuggestCorrectionPicker
import com.what3words.components.picker.W3WAutoSuggestPicker
import com.what3words.components.utils.InlineVoicePulseLayout
import com.what3words.components.utils.VoicePulseLayout
import com.what3words.components.utils.VoicePulseLayoutFullScreen
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import java.util.*

/**
 * A [AppCompatEditText] to simplify the integration of what3words text and voice auto-suggest API in your app.
 */
@SuppressLint("ClickableViewAccessibility")
class W3WAutoSuggestEditText
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.customW3WAutoSuggestEditTextStyle
) : AppCompatEditText(
    ContextThemeWrapper(context, R.style.W3WAutoSuggestEditTextTheme),
    attrs,
    defStyleAttr
) {

    private var drawableStartCallback: (() -> Unit)? = null
    internal var drawableStart: Drawable? = null
    private var oldHint: String = ""
    private var focusFromVoice: Boolean = false
    private var isRendered: Boolean = false
    internal var pickedFromVoice: Boolean = false
    private var pickedFromDropDown: Boolean = false
    private var slashesColor: Int = ContextCompat.getColor(context, R.color.w3wRed)
    private var fromPaste: Boolean = false

    internal var isShowingTick: Boolean = false

    // internal var key: String? = null
    internal var errorMessageText: String? = null
    internal var displayUnits: DisplayUnits = DisplayUnits.SYSTEM
    internal var correctionMessage: String = context.getString(R.string.correction_message)
    internal var invalidSelectionMessageText: String? = null
    internal var lastSuggestions: MutableList<Suggestion> = mutableListOf()
    internal var callback: Consumer<SuggestionWithCoordinates?>? =
        null
    internal var errorCallback: Consumer<APIResponse.What3WordsError>? =
        null
    internal var onDisplaySuggestions: Consumer<Boolean>? =
        null
    internal var returnCoordinates: Boolean = false
    internal var voiceEnabled: Boolean = false
    internal var voiceScreenType: VoiceScreenType = VoiceScreenType.Inline
    internal var allowInvalid3wa: Boolean = false
    internal var voicePlaceholder: String
    internal var voiceBackgroundColor: Int =
        ContextCompat.getColor(context, R.color.w3wVoiceBackground)
    internal var voiceBackgroundDrawable: Drawable? = null
    internal var voiceIconsColor: Int =
        ContextCompat.getColor(context, R.color.w3wGray)
    internal var voiceLanguage: String
    internal var customPicker: W3WAutoSuggestPicker? = null
    internal var customErrorView: AppCompatTextView? = null
    internal var customCorrectionPicker: W3WAutoSuggestCorrectionPicker? = null
    internal var customInvalidAddressMessageView: AppCompatTextView? = null

    internal val tick: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_tick).apply {
            this?.setBounds(
                0,
                0,
                (this@W3WAutoSuggestEditText.textSize * 1.20).toInt(),
                (this@W3WAutoSuggestEditText.textSize * 1.20).toInt()
            )
        }
    }

    internal val tickHolder: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_empty).apply {
            this?.setTint(context.getColor(R.color.transparent))
            this?.setBounds(
                0,
                0,
                (this@W3WAutoSuggestEditText.textSize * 1.20).toInt() + context.resources.getDimensionPixelSize(
                    R.dimen.medium_margin
                ),
                (this@W3WAutoSuggestEditText.textSize * 1.20).toInt()
            )
        }
    }

    internal val viewModel: AutosuggestViewModel by lazy {
        AutosuggestViewModel()
    }

    internal val defaultPicker: W3WAutoSuggestPicker by lazy {
        W3WAutoSuggestPicker(ContextThemeWrapper(context, R.style.W3WAutoSuggestPicker)).apply {
            setup(viewModel, displayUnits)
        }
    }

    internal val defaultCorrectionPicker: W3WAutoSuggestCorrectionPicker by lazy {
        W3WAutoSuggestCorrectionPicker(context).apply {
            setCorrectionMessage(correctionMessage).internalCallback { selectedSuggestion ->
                setText(
                    context.getString(
                        R.string.w3w_slashes_with_address,
                        selectedSuggestion.words
                    )
                )
                visibility = GONE
            }
        }
    }

    internal val defaultInvalidAddressMessageView: W3WAutoSuggestErrorMessage by lazy {
        W3WAutoSuggestErrorMessage(context)
    }

    internal val inlineVoicePulseLayout: InlineVoicePulseLayout by lazy {
        InlineVoicePulseLayout(context).apply {
            this.onResultsCallback {
                handleVoiceSuggestions(it)
                this.setIsVoiceRunning(false)
            }
            this.onErrorCallback {
                handleVoiceError(it)
                this.setIsVoiceRunning(false)
            }
        }
    }

    internal var voiceAnimatedPopup: VoicePulseLayout? = null

    internal var voicePulseLayoutFullScreen: VoicePulseLayoutFullScreen? = null

    private val watcher by lazy {
        object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()

                if (fromPaste) {
                    if (searchText.removePrefix(context.getString(R.string.w3w_slashes))
                            .isPossible3wa()
                    ) {
                        fromPaste = false
                        setText(searchText.removePrefix(context.getString(R.string.w3w_slashes)))
                    }

                    if (fromPaste) {
                        Uri.parse(searchText).lastPathSegment?.let {
                            if (it.isPossible3wa()) {
                                fromPaste = false
                                setText(it)
                            }
                        }
                    }

                    if (fromPaste) {
                        fromPaste = false
                        setText("")
                    }
                    return
                }

                if (pickedFromDropDown) {
                    pickedFromDropDown = false
                    return
                }
                if (pickedFromVoice) {
                    pickedFromVoice = false
                    return
                }

                if (searchText.isPossible3wa() || searchText.didYouMean3wa()) {
                    viewModel.autosuggest(searchText)
                } else {
                    onDisplaySuggestions?.accept(false)
                    getPicker().visibility = GONE
                    getPicker().refreshSuggestions(
                        emptyList(),
                        searchText,
                        viewModel.options,
                        returnCoordinates
                    )
                    getCorrectionPicker().setSuggestion(null)
                    getCorrectionPicker().visibility = GONE
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit
        }
    }

    internal fun getPicker(): W3WAutoSuggestPicker {
        return customPicker ?: defaultPicker
    }

    internal fun getCorrectionPicker(): W3WAutoSuggestCorrectionPicker {
        return customCorrectionPicker ?: defaultCorrectionPicker
    }

    internal fun getInvalidAddressView(): AppCompatTextView {
        return customInvalidAddressMessageView ?: defaultInvalidAddressMessageView
    }

    internal fun getErrorView(): AppCompatTextView {
        return customErrorView ?: defaultInvalidAddressMessageView
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.W3WAutoSuggestEditText,
            defStyleAttr, R.style.W3WAutoSuggestEditTextTheme
        ).apply {
            try {
                errorMessageText = getString(
                    R.styleable.W3WAutoSuggestEditText_errorMessage
                ) ?: resources.getString(R.string.error_message)
                invalidSelectionMessageText = getString(
                    R.styleable.W3WAutoSuggestEditText_invalidAddressMessage
                ) ?: resources.getString(R.string.invalid_address_message)
                correctionMessage = getString(
                    R.styleable.W3WAutoSuggestEditText_correctionMessage
                ) ?: resources.getString(R.string.correction_message)
                voicePlaceholder = getString(R.styleable.W3WAutoSuggestEditText_voicePlaceholder)
                    ?: resources.getString(R.string.voice_placeholder)
                slashesColor = getColor(
                    R.styleable.W3WAutoSuggestEditText_imageTintColor,
                    ContextCompat.getColor(context, R.color.w3wRed)
                )
                voiceBackgroundColor = getColor(
                    R.styleable.W3WAutoSuggestEditText_voiceBackgroundColor,
                    ContextCompat.getColor(context, R.color.w3wVoiceBackground)
                )
                val drawableId = getResourceId(
                    R.styleable.W3WAutoSuggestEditText_voiceBackgroundDrawable,
                    -1
                )
                voiceBackgroundDrawable =
                    if (drawableId != -1) ContextCompat.getDrawable(context, drawableId) else null
                voiceIconsColor = getColor(
                    R.styleable.W3WAutoSuggestEditText_voiceIconsColor,
                    ContextCompat.getColor(context, R.color.w3wGray)
                )
                returnCoordinates =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_returnCoordinates, false)
                voiceEnabled =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_voiceEnabled, false)
                voiceScreenType =
                    VoiceScreenType.values()[getInt(
                        R.styleable.W3WAutoSuggestEditText_voiceScreenType,
                        0
                    )]
                voiceLanguage =
                    getString(R.styleable.W3WAutoSuggestEditText_voiceLanguage) ?: "en"
                displayUnits =
                    DisplayUnits.values()[getInt(R.styleable.W3WAutoSuggestEditText_displayUnit, 0)]
                if (compoundDrawablesRelative.isNotEmpty()) {
                    drawableStart = compoundDrawablesRelative[0]
                }
            } finally {
                this@W3WAutoSuggestEditText.textDirection = TEXT_DIRECTION_LOCALE
                showImages()
                recycle()
            }
        }

// <editor-fold desc="text observers">
        viewModel.suggestions.observeForever { suggestions ->
            if (suggestions != null && hasFocus()) {
                lastSuggestions.apply {
                    clear()
                    addAll(suggestions)
                }
                onDisplaySuggestions?.accept(suggestions.isNotEmpty())
                getPicker().visibility =
                    if (suggestions.isEmpty()) View.GONE else View.VISIBLE
                getPicker().refreshSuggestions(
                    suggestions,
                    text.toString(),
                    viewModel.options,
                    returnCoordinates
                )
            }
        }

        viewModel.error.observeForever {
            if (it != null) {
                getErrorView().showError(errorMessageText)
                errorCallback?.accept(it) ?: run {
                    Log.e("W3WAutoSuggestEditText", it.message)
                }
            } else {
                getErrorView().visibility = GONE
            }
        }

        viewModel.didYouMean.observeForever {
            if (it != null && hasFocus()) {
                getCorrectionPicker().setSuggestion(it)
                getCorrectionPicker().visibility = View.VISIBLE
            } else {
                getCorrectionPicker().setSuggestion(null)
                getCorrectionPicker().visibility = View.GONE
            }
        }
// </editor-fold>

        inlineVoicePulseLayout.onStartVoiceClick {
            handleVoiceClick()
        }

        viewModel.selectedSuggestion.observeForever { suggestion ->
            pickedFromDropDown = true
            if (getPicker().visibility == VISIBLE && suggestion == null) {
                getInvalidAddressView().showError(invalidSelectionMessageText)
            }
            showImages(suggestion != null)
            getPicker().refreshSuggestions(emptyList(), null, viewModel.options, returnCoordinates)
            getPicker().visibility = GONE
            onDisplaySuggestions?.accept(false)
            getCorrectionPicker().setSuggestion(null)
            getCorrectionPicker().visibility = GONE
            clearFocus()
            if (suggestion != null) {
                setText(context.getString(R.string.w3w_slashes_with_address, suggestion.words))
            } else {
                text = null
            }

            callback?.accept(suggestion)
        }

        setOnEditorActionListener { _, i, event ->
            if (i == EditorInfo.IME_ACTION_DONE || (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))) {
                viewModel.onSuggestionClicked(
                    text.toString(),
                    lastSuggestions.firstOrNull { it.words == text.toString() },
                    returnCoordinates
                )
                true
            } else {
                false
            }
        }

        setOnFocusChangeListener { _, isFocused ->
            when {
                !pickedFromDropDown && !isFocused && isReal3wa(text.toString()) -> {
                    viewModel.onSuggestionClicked(
                        text.toString(),
                        getReal3wa(text.toString()),
                        returnCoordinates
                    )
                }
                !pickedFromDropDown && !isFocused && !isReal3wa(text.toString()) -> {
                    viewModel.onSuggestionClicked(text.toString(), null, returnCoordinates)
                }
            }
            if (!isFocused) {
                val keyboard: InputMethodManager =
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                keyboard.hideSoftInputFromWindow(windowToken, 0)
            } else {
                if (this.text.isNullOrEmpty() && !focusFromVoice) this.setText(
                    context.getString(R.string.w3w_slashes)
                )
                showImages(false)
            }
            focusFromVoice = false
        }

        this.setOnTouchListener(OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && drawableStart != null && drawableStartCallback != null) {
                val textLocation = IntArray(2)
                this.getLocationOnScreen(textLocation)
                if (event.rawX <= textLocation[0] + this.totalPaddingLeft) {
                    drawableStartCallback!!.invoke()
                    return@OnTouchListener true
                }
            }
            false
        })

        addTextChangedListener(watcher)

        viewTreeObserver.addOnGlobalLayoutListener(
            object :
                OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (!isRendered && visibility == VISIBLE) {
                        isRendered = true
                        if (customPicker == null) buildSuggestionList()
                        if (customErrorView == null) buildErrorMessage()
                        if (customCorrectionPicker == null) buildCorrection()
                        buildVoice()
                        when (voiceScreenType) {
                            VoiceScreenType.Inline -> {
                                inlineVoicePulseLayout.visibility =
                                    if (voiceEnabled && !isShowingTick) VISIBLE else GONE
                                inlineVoicePulseLayout.setup(viewModel.manager)
                            }
                            VoiceScreenType.AnimatedPopup -> {
                                setupAnimatedPopupVoice()
                            }
                            VoiceScreenType.Fullscreen -> {
                                setupFullScreenVoice()
                            }
                        }
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            })
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        when (id) {
            android.R.id.paste, android.R.id.pasteAsPlainText -> {
                onTextPaste()
            }
        }
        return super.onTextContextMenuItem(id)
    }

    private fun onTextPaste() {
        fromPaste = true
    }

    private fun handleVoiceError(error: APIResponse.What3WordsError) {
        getErrorView().showError(errorMessageText)
        errorCallback?.accept(error) ?: run {
            Log.e("W3WAutoSuggestEditText", error.message)
        }
        when (voiceScreenType) {
            VoiceScreenType.Inline -> {
                inlineVoicePulseLayout.setIsVoiceRunning(false)
            }
            VoiceScreenType.AnimatedPopup -> voiceAnimatedPopup?.setIsVoiceRunning(
                false,
                shouldAnimate = true
            )
            VoiceScreenType.Fullscreen -> voicePulseLayoutFullScreen?.setIsVoiceRunning(
                false
            )
        }
    }

    private fun handleVoiceClick() {
        focusFromVoice = true
        if (!isShowingTick) {
            hideKeyboard()
            when (voiceScreenType) {
                VoiceScreenType.Inline -> {
                    inlineVoicePulseLayout.toggle(
                        viewModel.options,
                        returnCoordinates,
                        voiceLanguage
                    )
                }
                VoiceScreenType.AnimatedPopup -> {
                    voiceAnimatedPopup?.toggle(
                        viewModel.options,
                        returnCoordinates,
                        voiceLanguage
                    )
                }
                VoiceScreenType.Fullscreen -> {
                    voicePulseLayoutFullScreen?.toggle(
                        viewModel.options,
                        returnCoordinates,
                        voiceLanguage
                    )
                }
            }
        }
    }

    private fun handleVoiceSuggestions(suggestions: List<Suggestion>) {
        this.hint = oldHint
        if (suggestions.isEmpty()) {
            getInvalidAddressView().showError(invalidSelectionMessageText)
            onDisplaySuggestions?.accept(false)
        } else {
            pickedFromVoice = true
            this.setText(
                context.getString(
                    R.string.w3w_slashes_with_address,
                    suggestions.minByOrNull { it.rank }!!.words
                )
            )
            getPicker().visibility = VISIBLE
            onDisplaySuggestions?.accept(true)
            // Query empty because we don't want to highlight when using voice.
            getPicker().refreshSuggestions(
                suggestions,
                "",
                viewModel.options,
                returnCoordinates
            )
            showKeyboard()
        }
        when (voiceScreenType) {
            VoiceScreenType.Inline -> {
                inlineVoicePulseLayout.setIsVoiceRunning(false)
            }
            VoiceScreenType.AnimatedPopup -> voiceAnimatedPopup?.setIsVoiceRunning(
                false,
                shouldAnimate = true
            )
            VoiceScreenType.Fullscreen -> voicePulseLayoutFullScreen?.setIsVoiceRunning(
                false
            )
        }
    }

//region Properties

    /** Set your What3Words API Key which will be used to get suggestions and coordinates (if enabled)
     *
     * @param key your API key from what3words developer dashboard
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun apiKey(key: String): W3WAutoSuggestEditText {
        viewModel.manager =
            AutosuggestApiManager(
                What3WordsV3(
                    key,
                    context,
                    mapOf("X-W3W-AS-Component" to "what3words-Android/$VERSION_NAME (Android ${Build.VERSION.RELEASE})")
                )
            )
        viewModel.microphone = Microphone()
        return this
    }

    /** Set your What3Words API Key and the Enterprise Suite API Server endpoint which will be used to get suggestions and coordinates (if enabled)
     *
     * @param key your API key from what3words developer dashboard
     * @param endpoint your Enterprise API endpoint
     * @param headers any custom headers needed for your Enterprise API
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun apiKey(
        key: String,
        endpoint: String,
        headers: Map<String, String> = mapOf()
    ): W3WAutoSuggestEditText {
        viewModel.manager =
            AutosuggestApiManager(
                What3WordsV3(
                    key,
                    endpoint,
                    context,
                    headers
                )
            )
        viewModel.microphone = Microphone()
        return this
    }

    /** Set your What3Words Manager with your SDK instance
     *
     * @param logicManager manager created using SDK instead of API
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun sdk(
        logicManager: AutosuggestLogicManager
    ): W3WAutoSuggestEditText {
        viewModel.manager = logicManager
        viewModel.microphone = Microphone()
        return this
    }

    /**
     * For normal text input, specifies a fallback language, which will help guide AutoSuggest if the input is particularly messy. If specified,
     * this parameter must be a supported 3 word address language as an ISO 639-1 2 letter code. For voice input (see voice section),
     * language must always be specified.
     *
     * @param language the fallback language
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun language(language: String): W3WAutoSuggestEditText {
        viewModel.options.language = language
        return this
    }

    /**
     * Set different [Microphone] setup
     *
     * @param microphone custom microphone setup
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun microphone(microphone: Microphone): W3WAutoSuggestEditText {
        viewModel.microphone = microphone
        return this
    }

    /**
     * For voice input, specifies the language our API will be listening for, default is English.
     * Available voice languages: ar for Arabic, cmn for Mandarin Chinese, de for German, en for Global English (default), es for Spanish, hi for Hindi, ja for Japanese, ko for Korean
     *
     * @param language the voice language (from list above)
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun voiceLanguage(language: String): W3WAutoSuggestEditText {
        voiceLanguage = language
        return this
    }

    /**
     * This is a location [Coordinates], specified as a latitude (often where the user making the query is). If specified, the results will be weighted to
     * give preference to those near the <code>focus</code>. For convenience, longitude is allowed to wrap around the 180 line, so 361 is equivalent to 1.
     *
     * @param coordinates the focus to use
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun focus(coordinates: Coordinates?): W3WAutoSuggestEditText {
        viewModel.options.focus = coordinates
        return this
    }

    /**
     * Set the number of AutoSuggest results to return. A maximum of 100 results can be specified, if a number greater than this is requested,
     * this will be truncated to the maximum. The default is 3
     *
     * @param n the number of AutoSuggest results to return
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun nResults(n: Int?): W3WAutoSuggestEditText {
        viewModel.options.nResults = n ?: 3
        return this
    }

    /**
     * Specifies the number of results (must be &lt;= nResults) within the results set which will have a focus. Defaults to <code>nResults</code>.
     * This allows you to run autosuggest with a mix of focussed and unfocussed results, to give you a "blend" of the two. This is exactly what the old V2
     * <code>standardblend</code> did, and <code>standardblend</code> behaviour can easily be replicated by passing nFocusResults(1)
     * which will return just one focussed result and the rest unfocussed.
     *
     * @param n number of results within the results set which will have a focus
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun nFocusResults(n: Int?): W3WAutoSuggestEditText {
        viewModel.options.nFocusResults = n
        return this
    }

    /**
     * Restrict autosuggest results to a circle, specified by [Coordinates] representing the centre of the circle, plus the
     * <code>radius</code> in kilometres. For convenience, longitude is allowed to wrap around 180 degrees. For example 181 is equivalent to -179.
     *
     * @param centre the centre of the circle
     * @param radius the radius of the circle in kilometres
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun clipToCircle(
        centre: Coordinates?,
        radius: Double?
    ): W3WAutoSuggestEditText {
        viewModel.options.clipToCircle = centre
        viewModel.options.clipToCircleRadius = radius
        return this
    }

    /**
     * Restricts autosuggest to only return results inside the countries specified by comma-separated list of uppercase ISO 3166-1 alpha-2 country codes
     * (for example, to restrict to Belgium and the UK, use <code>[clipToCountry](listOf("GB", "BE"))</code>. [clipToCountry] will also accept lowercase
     * country codes. Entries must be two a-z letters. WARNING: If the two-letter code does not correspond to a country, there is no error: API simply
     * returns no results.
     *
     * @param countryCodes countries to clip results too
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun clipToCountry(countryCodes: List<String>): W3WAutoSuggestEditText {
        viewModel.options.clipToCountry =
            if (countryCodes.isNotEmpty()) countryCodes else null
        return this
    }

    /**
     * Restrict autosuggest results to a [BoundingBox].
     *
     * @param boundingBox [BoundingBox] to clip results too
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun clipToBoundingBox(
        boundingBox: BoundingBox?
    ): W3WAutoSuggestEditText {
        viewModel.options.clipToBoundingBox = boundingBox
        return this
    }

    /**
     * Restrict autosuggest results to a polygon, specified by a collection of [Coordinates]. The polygon should be closed,
     * i.e. the first element should be repeated as the last element; also the list should contain at least 4 entries. The API is currently limited to
     * accepting up to 25 pairs.
     *
     * @param polygon the polygon to clip results too
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun clipToPolygon(
        polygon: List<Coordinates>
    ): W3WAutoSuggestEditText {
        viewModel.options.clipToPolygon = if (polygon.isNotEmpty()) polygon else null
        return this
    }

    /**
     * Enable autosuggest results to return coordinates
     *
     * @param enabled if callback should return coordinates
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun returnCoordinates(
        enabled: Boolean
    ): W3WAutoSuggestEditText {
        this.returnCoordinates = enabled
        return this
    }

    /**
     * Enable voice for autosuggest component
     *
     * @param enabled if voice should be enabled
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun voiceEnabled(
        enabled: Boolean
    ): W3WAutoSuggestEditText {
        this.voiceEnabled = enabled
        inlineVoicePulseLayout.setup(viewModel.manager)
        inlineVoicePulseLayout.visibility = if (enabled && !isShowingTick) VISIBLE else GONE
        return this
    }

    /**
     * Enable voice for autosuggest component with custom voice view
     *
     * @param enabled if voice should be enabled
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun voiceEnabled(
        enabled: Boolean,
        type: VoiceScreenType
    ): W3WAutoSuggestEditText {
        this.voiceEnabled = enabled
        this.voiceScreenType = type
        inlineVoicePulseLayout.visibility = if (enabled && !isShowingTick) VISIBLE else GONE
        when (type) {
            VoiceScreenType.Inline -> {
                inlineVoicePulseLayout.setup(viewModel.manager)
            }
            VoiceScreenType.AnimatedPopup -> {
                if (enabled && voiceAnimatedPopup == null) {
                    setupAnimatedPopupVoice()
                }
            }
            VoiceScreenType.Fullscreen -> {
                if (enabled && voicePulseLayoutFullScreen == null) {
                    setupFullScreenVoice()
                }
            }
        }
        return this
    }

    private fun setupFullScreenVoice() {
        buildVoiceFullscreen()
        voicePulseLayoutFullScreen?.let { fullScreenVoice ->
            fullScreenVoice.setup(viewModel.manager)
            fullScreenVoice.onResultsCallback {
                handleVoiceSuggestions(it)
                fullScreenVoice.setIsVoiceRunning(false)
            }
            fullScreenVoice.onErrorCallback {
                handleVoiceError(it)
                fullScreenVoice.setIsVoiceRunning(false)
            }
        }
    }

    private fun setupAnimatedPopupVoice() {
        buildVoiceAnimatedPopup()
        voiceAnimatedPopup?.let { voiceAnimatedPopup ->
            voiceAnimatedPopup.setup(viewModel.manager)
            voiceAnimatedPopup.onResultsCallback {
                handleVoiceSuggestions(it)
                voiceAnimatedPopup.setIsVoiceRunning(false, true)
            }
            voiceAnimatedPopup.onErrorCallback {
                handleVoiceError(it)
                voiceAnimatedPopup.setIsVoiceRunning(false, true)
            }
        }
    }

    /**
     * Enable voice fullscreen popup for autosuggest component
     *
     * @param enabled if voice fullscreen should be enabled
     * @return same [W3WAutoSuggestEditText] instance
     */
    @Deprecated("Use enabledVoice(boolean, screenType)")
    fun voiceFullscreen(
        enabled: Boolean
    ): W3WAutoSuggestEditText {
        this.voiceScreenType = VoiceScreenType.AnimatedPopup
        if (enabled && voiceAnimatedPopup == null) buildVoiceAnimatedPopup()
        return this
    }

    /**
     * Voice placeholder for fullscreen popup for autosuggest component
     *
     * @param placeholder text to show before user starts speaking.
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun voicePlaceholder(
        placeholder: String
    ): W3WAutoSuggestEditText {
        this.voicePlaceholder = placeholder
        return this
    }

    /**
     * Set end-user error message for API related issues, default: An error occurred. Please try again later
     *
     * @param message error message.
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun errorMessage(
        message: String
    ): W3WAutoSuggestEditText {
        this.errorMessageText = message
        return this
    }

    /**
     * Set end-user invalid address message for when user selects invalid three word address, default: No valid what3words address found
     *
     * @param message invalid address message
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun invalidSelectionMessage(
        message: String
    ): W3WAutoSuggestEditText {
        this.invalidSelectionMessageText = message
        return this
    }

    /**
     * Will provide the user selected 3 word address, if user selects an invalid 3 word address [SuggestionWithCoordinates] will be null.
     *
     * @param picker set custom 3 word address picker view [W3WAutoSuggestPicker], default picker will show below [W3WAutoSuggestEditText]
     * @param invalidAddressMessageView set custom invalid address view can be any [AppCompatTextView] or [W3WAutoSuggestErrorMessage], default view will show below [W3WAutoSuggestEditText]
     * @param callback will return the [SuggestionWithCoordinates] picked by the end-user, coordinates will be null if returnCoordinates = false.
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun onSelected(
        picker: W3WAutoSuggestPicker? = null,
        invalidAddressMessageView: AppCompatTextView? = null,
        callback: Consumer<SuggestionWithCoordinates?>,
    ): W3WAutoSuggestEditText {
        this.callback = callback
        if (picker != null) {
            picker.setup(viewModel, displayUnits)
            defaultPicker.forceClear()
        } else customPicker?.forceClear()
        this.customInvalidAddressMessageView = invalidAddressMessageView
        this.customPicker = picker
        return this
    }

    /**
     * Will provide any errors [APIResponse.What3WordsError] that might happen during the API call
     *
     * @param errorView set custom error view can be any [AppCompatTextView] or [W3WAutoSuggestErrorMessage], default view will show below [W3WAutoSuggestEditText] (this will only show end-user error friendly message or message provided on [errorMessage])
     * @param errorCallback will return [APIResponse.What3WordsError] with information about the error occurred.
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun onError(
        errorView: AppCompatTextView? = null,
        errorCallback: Consumer<APIResponse.What3WordsError>,
    ): W3WAutoSuggestEditText {
        this.errorCallback = errorCallback
        this.customErrorView = errorView
        return this
    }


    /**
     * If DrawableStart is set and it's pressed callback will be called, usage example is to have a back button as drawableStart.
     *
     * @param onHomeClickCallback will be called when drawableStart is pressed.
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun onHomeClick(
        onHomeClickCallback: (() -> Unit),
    ): W3WAutoSuggestEditText {
        this.drawableStartCallback = onHomeClickCallback
        return this
    }

    /**
     * Callback to update view when suggestion picker is being displayed or not, i.e: show tips when false hide tips when true
     *
     * @param displaySuggestionsCallback Boolean callback with the picker visibility status (true visible, false gone)
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun onDisplaySuggestions(
        displaySuggestionsCallback: Consumer<Boolean>,
    ): W3WAutoSuggestEditText {
        this.onDisplaySuggestions = displaySuggestionsCallback
        return this
    }

    /**
     * Add custom correction view to [W3WAutoSuggestEditText].
     *
     * @param customCorrectionPicker custom correct picker view.
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun customCorrectionPicker(
        customCorrectionPicker: W3WAutoSuggestCorrectionPicker? = null,
    ): W3WAutoSuggestEditText {
        this.customCorrectionPicker = customCorrectionPicker
        this.customCorrectionPicker?.setCorrectionMessage(correctionMessage)
            ?.internalCallback { selectedSuggestion ->
                setText(
                    context.getString(
                        R.string.w3w_slashes_with_address,
                        selectedSuggestion.words
                    )
                )
                this.customCorrectionPicker?.visibility = GONE
            }
        return this
    }

    /**
     * Set end-user correction picker title, default: "Did you mean?"
     *
     * @param message correction picker title
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun correctionMessage(
        message: String
    ): W3WAutoSuggestEditText {
        this.correctionMessage = message
        return this
    }

    /**
     * Set end-user display unit, [DisplayUnits.SYSTEM], [DisplayUnits.METRIC], [DisplayUnits.IMPERIAL]
     *
     * @param units [DisplayUnits.SYSTEM], [DisplayUnits.METRIC], [DisplayUnits.IMPERIAL],
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun displayUnit(
        units: DisplayUnits
    ): W3WAutoSuggestEditText {
        this.displayUnits = units
        return this
    }

    /**
     * Allow EditText to keep any text user types, default is false, by default EditText will be cleared if not a valid 3 word address, set to true to ignore this default behaviour.
     *
     * @param isAllowed are invalid 3 word addresses allowed
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun allowInvalid3wa(isAllowed: Boolean): W3WAutoSuggestEditText {
        this.allowInvalid3wa = isAllowed
        return this
    }

    /**
     * Allow EditText to keep any text user types, default is false, by default EditText will be cleared if not a valid 3 word address, set to true to ignore this default behaviour.
     *
     * @param isAllowed are invalid 3 word addresses allowed
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun triggerVoice(): W3WAutoSuggestEditText {
        handleVoiceClick()
        return this
    }
//endregion
}

enum class VoiceScreenType {
    Inline,
    AnimatedPopup,
    Fullscreen
}
