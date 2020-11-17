package com.what3words.autosuggest

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.autosuggest.voiceutils.InlineVoicePulseLayout
import com.what3words.autosuggest.voiceutils.VoicePulseLayout
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.Suggestion


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
        internal const val regex =
            "^/*[^0-9`~!@#$%^&*()+\\-_=\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}[・.。][^0-9`~!@#$%^&*()+\\-_=\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}[・.。][^0-9`~!@#$%^&*()+\\-_=\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}$"
    }

    private var isRendered: Boolean = false
    internal var pickedFromVoice: Boolean = false
    private var pickedFromDropDown: Boolean = false
    private var slashesColor: Int = ContextCompat.getColor(context, R.color.w3wRed)
    private var fromPaste: Boolean = false

    internal var suggestionsListPosition: SuggestionsListPosition = SuggestionsListPosition.BELOW
    internal var isShowingTick: Boolean = false
    internal var key: String? = null
    internal var queryMap: MutableMap<String, String> = mutableMapOf()
    internal var isEnterprise: Boolean = false
    internal var errorMessageText: String? = null
    internal var lastSuggestions: MutableList<Suggestion> = mutableListOf()
    internal var callback: ((suggestion: Suggestion?, latitude: Double?, longitude: Double?) -> Unit)? =
        null
    internal var returnCoordinates: Boolean = false
    internal var voiceEnabled: Boolean = false
    internal var voiceFullscreen: Boolean = false
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

    internal val recyclerView: RecyclerView by lazy {
        RecyclerView(context)
    }

    internal val errorMessage: TextView by lazy {
        TextView(context)
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
                    fromPaste = false
                    setText(searchText.removePrefix("///"))
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
                if (searchText == searchFor)
                    return

                searchFor = searchText
                if (isPossible3wa(searchText)) {
                    if (hasFocus()) {
                        showImages()
                        handleAutoSuggest(searchText, searchFor)
                    }
                } else {
                    recyclerView.visibility = GONE
                    suggestionsAdapter.refreshSuggestions(emptyList(), searchFor)
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit
        }
    }

    internal val suggestionsAdapter: SuggestionsAdapter by lazy {
        SuggestionsAdapter(
            this.typeface,
            this.currentTextColor
        ) { suggestion ->
            pickedFromDropDown = true
            handleAddressPicked(suggestion)
        }
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
                suggestionsListPosition = SuggestionsListPosition.values()[this.getInt(
                    R.styleable.W3WAutoSuggestEditText_suggestionsListPosition,
                    0
                )]
            } finally {
                this@W3WAutoSuggestEditText.textDirection = TEXT_DIRECTION_LOCALE
                showImages()
                recycle()
            }
        }

        setOnEditorActionListener { _, i, event ->
            if (i == EditorInfo.IME_ACTION_DONE || (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))) {
                handleAddressPicked(lastSuggestions.firstOrNull { it.words == text.toString() })
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
                    handleAddressPicked(lastSuggestions.firstOrNull { it.words == text.toString() })
                }
                !pickedFromDropDown && !isFocused && !isReal3wa(text.toString()) -> {
                    handleAddressPicked(null)
                }
            }
            if (!isFocused) {
                val keyboard: InputMethodManager =
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                keyboard.hideSoftInputFromWindow(windowToken, 0)
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
                    buildSuggestionList()
                    buildErrorMessage()
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
     * @ return a
    { @link W3WAutoSuggestEditText } instance
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
     * @ return a
    { @link W3WAutoSuggestEditText } instance
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

    fun language(language: String): W3WAutoSuggestEditText {
        this.language = language
        return this
    }

    fun voiceLanguage(language: String): W3WAutoSuggestEditText {
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
    fun focus(coordinates: Coordinates?): W3WAutoSuggestEditText {
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
    fun nResults(n: Int?): W3WAutoSuggestEditText {
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
    fun nFocusResults(n: Int?): W3WAutoSuggestEditText {
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
    ): W3WAutoSuggestEditText {
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
    fun clipToCountry(countryCodes: List<String>): W3WAutoSuggestEditText {
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
    ): W3WAutoSuggestEditText {
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
    ): W3WAutoSuggestEditText {
        clipToPolygon = if (polygon.isNotEmpty()) polygon.toTypedArray() else null
        return this
    }

    /**
     * Enable autosuggest results to return coordinates
     *
     * @param returnCoordinates if callback should return coordinates
     * @return a {@link W3WAutoSuggestEditText} instance
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
     * @return a {@link W3WAutoSuggestEditText} instance
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
     * @return a {@link W3WAutoSuggestEditText} instance
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
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun voicePlaceholder(
        placeholder: String
    ): W3WAutoSuggestEditText {
        this.voicePlaceholder = placeholder
        return this
    }

    /**
     * Set position of the suggestion list.
     *
     * @param position BELOW to be below EditText (default), ABOVE to be above.
     * @return a {@link W3WAutoSuggestEditText} instance
     */
    fun suggestionsListPosition(
        position: SuggestionsListPosition
    ): W3WAutoSuggestEditText {
        this.suggestionsListPosition = position
        return this
    }

    fun errorMessage(
        error: String
    ): W3WAutoSuggestEditText {
        this.errorMessageText = error
        return this
    }

    fun onSelected(callback: (selectedSuggestion: Suggestion?, latitude: Double?, longitude: Double?) -> Unit): W3WAutoSuggestEditText {
        this.callback = callback
        return this
    }
//endregion
}

enum class SuggestionsListPosition {
    BELOW, ABOVE
}