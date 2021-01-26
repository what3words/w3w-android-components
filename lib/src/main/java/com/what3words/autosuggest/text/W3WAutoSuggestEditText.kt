package com.what3words.autosuggest.text

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.autosuggest.BuildConfig
import com.what3words.autosuggest.R
import com.what3words.autosuggest.error.W3WAutoSuggestErrorMessage
import com.what3words.autosuggest.picker.W3WAutoSuggestCorrectionPicker
import com.what3words.autosuggest.picker.W3WAutoSuggestPicker
import com.what3words.autosuggest.utils.InlineVoicePulseLayout
import com.what3words.autosuggest.utils.VoicePulseLayout
import com.what3words.autosuggest.utils.W3WSuggestion
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion

/**
 * A [AppCompatEditText] to simplify the integration of what3words text and voice auto-suggest API in your app.
 */
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

    companion object {
        internal const val DEBOUNCE_MS = 150L
        internal val split_regex = Regex("[.｡。･・︒។։။۔።।,-_/ ]+")
        internal const val regex =
            "^/*[^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}[.｡。･・︒។։။۔።।][^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}[.｡。･・︒។։။۔።।][^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}$";
        internal const val dym_regex =
            "^/*[^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}([.｡。･・︒។։။۔።।,-_/ ]+)[^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}([.｡。･・︒។։။۔።।,-_/ ]+)[^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}$";
    }

    private var isRendered: Boolean = false
    internal var pickedFromVoice: Boolean = false
    private var pickedFromDropDown: Boolean = false
    private var slashesColor: Int = ContextCompat.getColor(context, R.color.w3wRed)
    private var fromPaste: Boolean = false

    internal var isShowingTick: Boolean = false
    internal var key: String? = null
    internal var queryMap: MutableMap<String, String> = mutableMapOf()
    internal var isEnterprise: Boolean = false
    internal var errorMessageText: String? = null
    internal var correctionMessage: String = context.getString(R.string.correction_message)
    internal var invalidSelectionMessageText: String? = null
    internal var lastSuggestions: MutableList<Suggestion> = mutableListOf()
    internal var callback: Consumer<W3WSuggestion?>? =
        null
    internal var errorCallback: Consumer<APIResponse.What3WordsError>? =
        null
    internal var returnCoordinates: Boolean = false
    internal var voiceEnabled: Boolean = false
    internal var voiceFullscreen: Boolean = false
    internal var allowInvalid3wa: Boolean = false
    internal var language: String? = null
    internal var voiceLanguage: String = "en"
    internal var voicePlaceholder: String
    internal var clipToPolygon: Array<Coordinates>? = null
    internal var clipToBoundingBox: BoundingBox? = null
    internal var clipToCircle: Coordinates? = null
    internal var clipToCircleRadius: Double? = null
    internal var clipToCountry: Array<String>? = null
    internal var nFocusResults: Int? = null
    internal var focus: Coordinates? = null
    internal var nResults: Int? = null
    internal var wrapper: What3WordsV3? = null
    internal var builder: VoiceBuilder? = null
    internal var customPicker: W3WAutoSuggestPicker? = null
    internal var customErrorView: AppCompatTextView? = null
    internal var customCorrectionPicker: W3WAutoSuggestCorrectionPicker? = null
    private var customInvalidAddressMessageView: AppCompatTextView? = null

    internal val slashes: Drawable? by lazy {
        val d = ContextCompat.getDrawable(context, R.drawable.ic_slashes)
        if (d != null) {
            val wd: Drawable = DrawableCompat.wrap(d)
            DrawableCompat.setTint(wd, slashesColor)
            wd.setBounds(
                0,
                0,
                this@W3WAutoSuggestEditText.textSize.toInt(),
                this@W3WAutoSuggestEditText.textSize.toInt()
            )
            wd
        } else {
            null
        }
    }

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

    internal val defaultPicker: W3WAutoSuggestPicker by lazy {
        val p = W3WAutoSuggestPicker(context)
        p.setup(wrapper!!, isEnterprise, key!!)
        p.internalCallback { selectedSuggestion ->
            pickedFromDropDown = true
            handleAddressPicked(selectedSuggestion)
        }
    }


    internal val defaultCorrectionPicker: W3WAutoSuggestCorrectionPicker by lazy {
        val p = W3WAutoSuggestCorrectionPicker(context)
        p.setCorrectionMessage(correctionMessage).internalCallback { selectedSuggestion ->
            setText(selectedSuggestion.words)
            p.visibility = GONE
        }
    }

    internal val defaultInvalidAddressMessageView: W3WAutoSuggestErrorMessage by lazy {
        W3WAutoSuggestErrorMessage(context)
    }

    internal val inlineVoicePulseLayout: InlineVoicePulseLayout by lazy {
        InlineVoicePulseLayout(context)
    }

    internal var voicePulseLayout: VoicePulseLayout? = null

    private val watcher by lazy {
        object : TextWatcher {
            private var searchFor = ""

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()

                if (fromPaste) {
                    if (isValid3wa(searchText.removePrefix("///"))) {
                        fromPaste = false
                        setText(searchText.removePrefix("///"))
                    }

                    if (fromPaste) {
                        Uri.parse(searchText).lastPathSegment?.let {
                            if (isValid3wa(it)) {
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

                showImages()
                if (searchText == searchFor) {
                    return
                }

                searchFor = searchText
                if (isValid3wa(searchText)) {
                    if (hasFocus()) {
                        handleAutoSuggest(searchText, searchFor)
                    }
                } else if (isPossible3wa(searchText)) {
                    val words = searchText.split(split_regex, 3).joinToString(".")
                    handleAutoSuggest(words, words, true)
                } else {
                    getPicker().visibility = GONE
                    getPicker().refreshSuggestions(
                        emptyList(),
                        searchFor,
                        emptyMap(),
                        returnCoordinates
                    )
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
                nResults = getInteger(R.styleable.W3WAutoSuggestEditText_nResults, 3)
                language = getString(R.styleable.W3WAutoSuggestEditText_language)
                voicePlaceholder = getString(R.styleable.W3WAutoSuggestEditText_voicePlaceholder)
                    ?: resources.getString(R.string.voice_placeholder)
                slashesColor = getColor(
                    R.styleable.W3WAutoSuggestEditText_imageTintColor,
                    ContextCompat.getColor(context, R.color.w3wRed)
                )
                returnCoordinates =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_returnCoordinates, false)
                voiceEnabled =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_voiceEnabled, false)
                voiceFullscreen =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_voiceFullscreen, false)
                voiceLanguage = getString(R.styleable.W3WAutoSuggestEditText_voiceLanguage) ?: "en"
            } finally {
                this@W3WAutoSuggestEditText.textDirection = TEXT_DIRECTION_LOCALE
                showImages()
                recycle()
            }
        }

        setOnEditorActionListener { _, i, event ->
            if (i == EditorInfo.IME_ACTION_DONE || (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))) {
                handleAddressAutoPicked(lastSuggestions.firstOrNull { it.words == text.toString() })
                true
            } else {
                false
            }
        }

        inlineVoicePulseLayout.onStartVoiceClick {
            if (!isShowingTick && wrapper != null) {
                handleVoice()
            }
        }

        setOnFocusChangeListener { _, isFocused ->
            when {
                !pickedFromDropDown && !isFocused && isReal3wa(text.toString()) -> {
                    handleAddressAutoPicked(lastSuggestions.firstOrNull { it.words == text.toString() })
                }
                !pickedFromDropDown && !isFocused && !isReal3wa(text.toString()) -> {
                    handleAddressAutoPicked(null)
                }
            }
            if (!isFocused) {
                val keyboard: InputMethodManager =
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                keyboard.hideSoftInputFromWindow(windowToken, 0)
                showImages(isReal3wa(text.toString()))
            } else {
                showImages(false)
            }
        }

        addTextChangedListener(watcher)

        viewTreeObserver.addOnGlobalLayoutListener(
            object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    isRendered = true
                    if (customPicker == null) buildSuggestionList()
                    if (customErrorView == null) buildErrorMessage()
                    if (customCorrectionPicker == null) buildCorrection()
                    buildVoice()
                    if (voiceFullscreen) buildBackgroundVoice()
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
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

//region Properties

    /** Set your What3Words API Key which will be used to get suggestions and coordinates (if enabled)
     *
     * @param key your API key from what3words developer dashboard
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun apiKey(key: String): W3WAutoSuggestEditText {
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
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun apiKey(
        key: String,
        endpoint: String,
        headers: Map<String, String> = mapOf()
    ): W3WAutoSuggestEditText {
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

    /**
     * For normal text input, specifies a fallback language, which will help guide AutoSuggest if the input is particularly messy. If specified,
     * this parameter must be a supported 3 word address language as an ISO 639-1 2 letter code. For voice input (see voice section),
     * language must always be specified.
     *
     * @param language the fallback language
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun language(language: String): W3WAutoSuggestEditText {
        this.language = language
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
        this.voiceLanguage = language
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
        focus = coordinates
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
        nResults = n ?: 3
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
        nFocusResults = n
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
        clipToCircle = centre
        clipToCircleRadius = radius
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
        clipToCountry = if (countryCodes.isNotEmpty()) countryCodes.toTypedArray() else null
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
        clipToBoundingBox = boundingBox
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
        clipToPolygon = if (polygon.isNotEmpty()) polygon.toTypedArray() else null
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
        inlineVoicePulseLayout.visibility = if (enabled && !isShowingTick) VISIBLE else GONE
        return this
    }

    /**
     * Enable voice fullscreen popup for autosuggest component
     *
     * @param enabled if voice fullscreen should be enabled
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun voiceFullscreen(
        enabled: Boolean
    ): W3WAutoSuggestEditText {
        this.voiceFullscreen = enabled
        if (enabled && voicePulseLayout == null) buildBackgroundVoice()
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
     * Will provide the user selected 3 word address, if user selects an invalid 3 word address [W3WSuggestion] will be null.
     *
     * @param picker set custom 3 word address picker view [W3WAutoSuggestPicker], default picker will show below [W3WAutoSuggestEditText]
     * @param invalidAddressMessageView set custom invalid address view can be any [AppCompatTextView] or [W3WAutoSuggestErrorMessage], default view will show below [W3WAutoSuggestEditText]
     * @param callback will return [W3WSuggestion] selected by the user.
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun onSelected(
        picker: W3WAutoSuggestPicker? = null,
        invalidAddressMessageView: AppCompatTextView? = null,
        callback: Consumer<W3WSuggestion?>,
    ): W3WAutoSuggestEditText {
        this.callback = callback
        if (picker != null) {
            picker.setup(wrapper!!, isEnterprise, key!!)
            picker.internalCallback { selectedSuggestion ->
                pickedFromDropDown = true
                handleAddressPicked(selectedSuggestion)
            }
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
                setText(selectedSuggestion.words)
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
     * Allow EditText to keep any text user types, default is false, by default EditText will be cleared if not a valid 3 word address, set to true to ignore this default behaviour.
     *
     * @param isAllowed are invalid 3 word addresses allowed
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun allowInvalid3wa(isAllowed: Boolean): W3WAutoSuggestEditText {
        this.allowInvalid3wa = isAllowed
        return this
    }
//endregion
}