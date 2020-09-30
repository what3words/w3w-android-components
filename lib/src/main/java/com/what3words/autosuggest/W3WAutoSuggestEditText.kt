package com.what3words.autosuggest

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.autosuggest.util.MyDividerItemDecorator
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.Suggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

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
        private const val DEBOUNCE_MS = 150L
        private const val regex =
            "^/*[^0-9`~!@#$%^&*()+\\-_=\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}[・.。][^0-9`~!@#$%^&*()+\\-_=\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}[・.。][^0-9`~!@#$%^&*()+\\-_=\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}$"
    }

    private var key: String? = null
    private var queryMap: MutableMap<String, String> = mutableMapOf()
    private var isEnterprise: Boolean = false
    private var pickedFromDropDown: Boolean = false
    private var fromPaste: Boolean = false
    private var errorMessageText: String? = null
    private var slashesColor: Int = ContextCompat.getColor(context, R.color.w3wRed)
    private var lastSuggestions: MutableList<Suggestion> = mutableListOf()
    private var callback: ((suggestion: Suggestion?, latitude: Double?, longitude: Double?) -> Unit)? =
        null
    private var returnCoordinates: Boolean = false
    private var language: String = "en"
    private var clipToPolygon: Array<Coordinates>? = null
    private var clipToBoundingBox: BoundingBox? = null
    private var clipToCircle: Coordinates? = null
    private var clipToCircleRadius: Double? = null
    private var clipToCountry: Array<String>? = null
    private var nFocusResults: Int? = null
    private var focus: Coordinates? = null
    private var nResults: Int = 3
    private var wrapper: What3WordsV3? = null

    private val slashes: Drawable? by lazy {
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

    private val tick: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_tick).apply {
            this?.setBounds(
                0,
                0,
                (this@W3WAutoSuggestEditText.textSize * 1.20).toInt(),
                (this@W3WAutoSuggestEditText.textSize * 1.20).toInt()
            )
        }
    }

    private val recyclerView: RecyclerView by lazy {
        RecyclerView(context)
    }

    private val errorMessage: TextView by lazy {
        TextView(context)
    }

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

    private val suggestionsAdapter: SuggestionsAdapter by lazy {
        SuggestionsAdapter(this.typeface, this.currentTextColor) { suggestion ->
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
                language = getString(R.styleable.W3WAutoSuggestEditText_language) ?: "en"
                slashesColor = getColor(
                    R.styleable.W3WAutoSuggestEditText_imageTintColor,
                    ContextCompat.getColor(context, R.color.w3wRed)
                )
                returnCoordinates =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_returnCoordinates, false)
            } finally {
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
            }
        }

        addTextChangedListener(watcher)

        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                buildSuggestionList()
                buildErrorMessage()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun buildSuggestionList() {
        val params = ViewGroup.MarginLayoutParams(
            width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        recyclerView.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            this.x = this@W3WAutoSuggestEditText.x
            this.y =
                this@W3WAutoSuggestEditText.y + this@W3WAutoSuggestEditText.height + resources.getDimensionPixelSize(
                    R.dimen.input_margin
                )
            layoutParams = params
            val linear = LinearLayoutManager(context)
            background = getDrawable(context, R.drawable.bg_white_border_gray)
            resources.getDimensionPixelSize(R.dimen.tiny_margin).let {
                setPadding(it, it, it, it)
            }
            layoutManager = linear
            setHasFixedSize(true)
            ResourcesCompat.getDrawable(resources, R.drawable.divider, null)?.let {
                addItemDecoration(
                    MyDividerItemDecorator(
                        it
                    )
                )
            }
            adapter = suggestionsAdapter
            visibility = GONE
        }
        (parent as? ViewGroup)?.apply {
            addView(recyclerView)
        }
    }

    private fun buildErrorMessage() {
        val params = ViewGroup.MarginLayoutParams(
            width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        errorMessage.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            text = errorMessageText
            setBackgroundResource(R.drawable.bg_item)
            setTextColor(ContextCompat.getColor(context, R.color.w3wError))
            this.x = this@W3WAutoSuggestEditText.x
            this.y =
                this@W3WAutoSuggestEditText.y + this@W3WAutoSuggestEditText.height - resources.getDimensionPixelSize(
                    R.dimen.tiny_margin
                )
            layoutParams = params
            setPadding(
                resources.getDimensionPixelSize(R.dimen.xlarge_margin),
                resources.getDimensionPixelSize(R.dimen.medium_margin),
                resources.getDimensionPixelSize(R.dimen.xlarge_margin),
                resources.getDimensionPixelSize(R.dimen.medium_margin)
            )
            visibility = View.GONE
        }
        (parent as? ViewGroup)?.addView(errorMessage)
    }

    private fun showImages(showTick: Boolean = false) {
        setCompoundDrawables(
            slashes,
            null,
            if (showTick) tick else null,
            null
        )
    }

    private fun isPossible3wa(query: String): Boolean {
        Pattern.compile(regex).also {
            return it.matcher(query).find()
        }
    }

    private fun isReal3wa(query: String): Boolean {
        return lastSuggestions.any { it.words == query }
    }

    private fun handleAutoSuggest(searchText: String, searchFor: String) {
        CoroutineScope(IO).launch {
            delay(DEBOUNCE_MS)  //debounce timeOut
            if (searchText != searchFor)
                return@launch

            if (wrapper == null) throw Exception("Please use apiKey")
            queryMap.clear()
            queryMap["n-results"] = nResults.toString()
            val res =
                wrapper!!.autosuggest(searchFor).language(language).nResults(nResults)
                    .apply {
                        focus?.let {
                            this.focus(it)
                            queryMap["focus"] = it.lat.toString() + "," + it.lng.toString()
                        }
                        nFocusResults?.let {
                            this.nFocusResults(it)
                            queryMap["n-focus-results"] = it.toString()
                        }
                        clipToCountry?.let {
                            this.clipToCountry(*it)
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
                            this.clipToPolygon(*coordinates)
                            queryMap["clip-to-polygon"] =
                                coordinates.joinToString(",") { "${it.lat},${it.lng}" }
                        }
                    }.execute()

            CoroutineScope(Main).launch {
                if (res != null && res.suggestions != null && hasFocus()) {
                    lastSuggestions.apply {
                        clear()
                        addAll(res.suggestions)
                    }
                    recyclerView.visibility = if (res.suggestions.isEmpty()) GONE else View.VISIBLE
                    suggestionsAdapter.refreshSuggestions(res.suggestions, searchFor)
                }
            }
        }
    }

    private fun handleAddressPicked(suggestion: Suggestion?) {
        if (recyclerView.visibility == View.VISIBLE && suggestion == null) {
            recyclerView.visibility = GONE
            errorMessage.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                errorMessage.visibility = View.GONE
            }, 5000)
        } else {
            recyclerView.visibility = GONE
        }
        showImages(suggestion != null)
        suggestionsAdapter.refreshSuggestions(emptyList(), null)
        clearFocus()
        val originalQuery = text.toString()
        setText(suggestion?.words)

        if (suggestion == null) callback?.invoke(null, null, null)
        else {
            if (!isEnterprise) handleSelectionTrack(suggestion, originalQuery, queryMap, key!!)
            if (!returnCoordinates) callback?.invoke(suggestion, null, null)
            else {
                CoroutineScope(IO).launch {
                    val res = wrapper!!.convertToCoordinates(suggestion.words).execute()
                    CoroutineScope(Main).launch {
                        callback?.invoke(
                            suggestion,
                            res.coordinates.lat,
                            res.coordinates.lng
                        )
                    }
                }
            }
        }
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