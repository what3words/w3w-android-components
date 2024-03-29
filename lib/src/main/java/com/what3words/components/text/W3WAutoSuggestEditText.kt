package com.what3words.components.text

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.media.AudioFormat
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsAndroidWrapper
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.androidwrapper.helpers.didYouMean3wa
import com.what3words.androidwrapper.helpers.isPossible3wa
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.androidwrapper.voice.VoiceApi
import com.what3words.androidwrapper.voice.VoiceProvider
import com.what3words.components.BuildConfig
import com.what3words.components.R
import com.what3words.components.error.W3WAutoSuggestErrorMessage
import com.what3words.components.error.forceClearAndHide
import com.what3words.components.error.populateAndShow
import com.what3words.components.models.DisplayUnits
import com.what3words.components.models.VoiceScreenType
import com.what3words.components.models.W3WListeningState
import com.what3words.components.picker.W3WAutoSuggestCorrectionPicker
import com.what3words.components.picker.W3WAutoSuggestPicker
import com.what3words.components.utils.IconHolderLayout
import com.what3words.components.utils.VoicePulseLayout
import com.what3words.components.utils.VoicePulseLayoutFullScreen
import com.what3words.components.utils.W3WSuggestion
import com.what3words.components.utils.backwardCompatible
import com.what3words.components.vm.AutosuggestTextViewModel
import com.what3words.components.voice.W3WAutoSuggestVoice
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * A [AppCompatEditText] to simplify the integration of what3words text and voice auto-suggest API in your app.
 */
@SuppressLint("ClickableViewAccessibility")
class W3WAutoSuggestEditText
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.customW3WAutoSuggestEditTextStyle,
    @StyleRes defaultTheme: Int = R.style.W3WAutoSuggestEditTextTheme,
) : AppCompatEditText(
    ContextThemeWrapper(context, defaultTheme),
    attrs,
    defStyleAttr
),
    OnGlobalLayoutListener {

    companion object {
        private val SESSION_ID = UUID.randomUUID().toString()
    }

    private var isDisplayOnly: Boolean = false
    private var sharedFlowJobs: Job? = null
    private var originalPaddingEnd: Int
    private var drawableStartCallback: (() -> Unit)? = null
    internal var drawableStart: Drawable? = null
    private var oldHint: String = ""
    private var focusFromVoice: Boolean = false
    private var isRendered: Boolean = false
    private var pickedFromVoice: Boolean = false
    private var pickedFromDropDown: Boolean = false
    private var fromPaste: Boolean = false
    internal var isShowingTick: Boolean = false
    private var errorMessageText: String? = null
    private var displayUnits: DisplayUnits = DisplayUnits.SYSTEM
    private var correctionMessage: String = context.getString(R.string.correction_message)
    private var invalidSelectionMessageText: String? = null

    internal var lastSuggestions: MutableList<Suggestion> = mutableListOf()

    @Deprecated("", ReplaceWith("callback"))
    private var oldCallback: Consumer<W3WSuggestion?>? =
        null

    private var callback: Consumer<SuggestionWithCoordinates?>? =
        null
    private var errorCallback: Consumer<APIResponse.What3WordsError>? =
        null
    private var onDisplaySuggestions: Consumer<Boolean>? =
        null
    internal var returnCoordinates: Boolean = false
    private var isDayNightEnabled: Boolean = false
    internal var voiceEnabled: Boolean = false
    internal var voiceScreenType: VoiceScreenType = VoiceScreenType.Inline
    private var allowInvalid3wa: Boolean = false
    private var allowFlexibleDelimiters: Boolean = false
    internal var hideSelectedIcon: Boolean = false
    internal var voicePlaceholder: String = ""
    internal var voiceLoadingLabel: String = ""
    internal var voiceErrorLabel: String = ""
    internal var voiceTryAgainLabel: String = ""
    internal var voiceBackgroundColor: Int =
        ContextCompat.getColor(context, R.color.w3wVoiceBackground)
    internal var voiceBackgroundDrawable: Drawable? = null
    internal var voiceIconsColor: Int =
        ContextCompat.getColor(context, R.color.subtextColor)
    internal var iconTint: Int =
        ContextCompat.getColor(context, R.color.iconTintColor)
    internal var voiceLanguage: String
    private var customPicker: W3WAutoSuggestPicker? = null
    private var customErrorView: AppCompatTextView? = null
    private var customCorrectionPicker: W3WAutoSuggestCorrectionPicker? = null
    private var customInvalidAddressMessageView: AppCompatTextView? = null
    private var searchFlowEnabled = false

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

    private val viewModel: AutosuggestTextViewModel by lazy {
        AutosuggestTextViewModel()
    }

    internal val defaultPicker: W3WAutoSuggestPicker by lazy {
        W3WAutoSuggestPicker(
            ContextThemeWrapper(
                context,
                if (isDayNightEnabled) R.style.W3WAutoSuggestPickerDayNight else R.style.W3WAutoSuggestPicker
            )
        ).apply {
            setup(viewModel, displayUnits)
        }
    }

    internal val defaultCorrectionPicker: W3WAutoSuggestCorrectionPicker by lazy {
        W3WAutoSuggestCorrectionPicker(
            ContextThemeWrapper(
                context,
                if (isDayNightEnabled) R.style.W3WAutoSuggestCorrectionPickerDayNight else R.style.W3WAutoSuggestCorrectionPicker
            )
        ).apply {
            setCorrectionMessage(correctionMessage).internalCallback { selectedSuggestion ->
                setText(
                    context.getString(
                        R.string.w3w_slashes_with_address,
                        selectedSuggestion.words
                    )
                )
                this@W3WAutoSuggestEditText.setSelection(this@W3WAutoSuggestEditText.length())
                visibility = GONE
            }
        }
    }

    internal val defaultInvalidAddressMessageView: W3WAutoSuggestErrorMessage by lazy {
        W3WAutoSuggestErrorMessage(
            ContextThemeWrapper(
                context,
                if (isDayNightEnabled) R.style.W3WAutoSuggestErrorMessageDayNight else R.style.W3WAutoSuggestErrorMessage
            )
        )
    }

    internal val iconHolderLayout: IconHolderLayout by lazy {
        IconHolderLayout(context, this.iconTint, this.iconTint).apply {
            this.onResultsCallback {
                handleVoiceSuggestions(it)
            }
            this.onErrorCallback {
                handleVoiceError(it)
            }
            this.onListeningStateChanged {
                if (it == null) return@onListeningStateChanged
                hint = when (it) {
                    W3WListeningState.Connecting -> {
                        this@W3WAutoSuggestEditText.isFocusableInTouchMode = false
                        context.getString(R.string.loading)
                    }
                    W3WListeningState.Started -> {
                        this@W3WAutoSuggestEditText.isFocusableInTouchMode = false
                        voicePlaceholder
                    }
                    W3WListeningState.Stopped -> {
                        this@W3WAutoSuggestEditText.isFocusableInTouchMode = true
                        oldHint
                    }
                }
            }
            this.onClearTextClick {
                this@W3WAutoSuggestEditText.setText(context.getString(R.string.w3w_slashes))
                this@W3WAutoSuggestEditText.setSelection(this@W3WAutoSuggestEditText.length())
            }
        }
    }

    internal var voiceAnimatedPopup: VoicePulseLayout? = null
    internal var voicePulseLayoutFullScreen: VoicePulseLayoutFullScreen? = null

    private fun getPicker(): W3WAutoSuggestPicker {
        return customPicker ?: defaultPicker
    }

    private fun getCorrectionPicker(): W3WAutoSuggestCorrectionPicker {
        return customCorrectionPicker ?: defaultCorrectionPicker
    }

    private fun getInvalidAddressView(): AppCompatTextView {
        return customInvalidAddressMessageView ?: defaultInvalidAddressMessageView
    }

    private fun getErrorView(): AppCompatTextView {
        return customErrorView ?: defaultInvalidAddressMessageView
    }

    private val watcher: TextWatcher by lazy {
        object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChanged(s.toString().trim())
            }

            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit
        }
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.W3WAutoSuggestEditText,
            defStyleAttr, R.style.W3WAutoSuggestEditTextTheme
        ).apply {
            try {
                isDayNightEnabled =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_isDayNightEnabled, false)
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
                voiceErrorLabel = getString(R.styleable.W3WAutoSuggestEditText_voiceErrorLabel)
                    ?: resources.getString(R.string.voice_error_label)
                voiceTryAgainLabel =
                    getString(R.styleable.W3WAutoSuggestEditText_voiceTryAgainLabel)
                        ?: resources.getString(R.string.voice_try_again)
                voiceLoadingLabel = getString(R.styleable.W3WAutoSuggestEditText_voiceLoadingLabel)
                    ?: resources.getString(R.string.loading)
                voiceBackgroundColor = getColor(
                    R.styleable.W3WAutoSuggestEditText_voiceBackgroundColor,
                    ContextCompat.getColor(
                        context,
                        if (isDayNightEnabled) R.color.w3wVoiceBackground else R.color.w3wVoiceBackgroundForceDay
                    )
                )
                val drawableId = getResourceId(
                    R.styleable.W3WAutoSuggestEditText_voiceBackgroundDrawable,
                    -1
                )
                voiceBackgroundDrawable =
                    if (drawableId != -1) ContextCompat.getDrawable(context, drawableId) else null
                voiceIconsColor = getColor(
                    R.styleable.W3WAutoSuggestEditText_voiceIconsColor,
                    ContextCompat.getColor(
                        context,
                        if (isDayNightEnabled) R.color.subtextColor else R.color.subtextColorForceDay
                    )
                )

                iconTint = getColor(
                    R.styleable.W3WAutoSuggestEditText_iconTint,
                    ContextCompat.getColor(
                        context,
                        if (isDayNightEnabled) R.color.iconTintColor else R.color.iconTintColorForceDay
                    )
                )

                returnCoordinates =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_returnCoordinates, false)

                searchFlowEnabled =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_searchFlowEnabled, false)
                voiceEnabled =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_voiceEnabled, false)
                viewModel.options.preferLand =
                    getBoolean(R.styleable.W3WAutoSuggestEditText_preferLand, true)
                voiceScreenType =
                    VoiceScreenType.values()[
                            getInt(
                                R.styleable.W3WAutoSuggestEditText_voiceScreenType,
                                0
                            )
                    ]
                voiceLanguage =
                    getString(R.styleable.W3WAutoSuggestEditText_voiceLanguage) ?: "en"
                displayUnits =
                    DisplayUnits.values()[getInt(R.styleable.W3WAutoSuggestEditText_displayUnit, 0)]
                if (compoundDrawablesRelative.isNotEmpty()) {
                    drawableStart = compoundDrawablesRelative[0]
                }
                oldHint = hint?.toString() ?: ""
                originalPaddingEnd = paddingEnd
            } finally {
                this@W3WAutoSuggestEditText.textDirection = TEXT_DIRECTION_LOCALE
                showImages()
                recycle()
            }
        }

        // all listeners needed below
        iconHolderLayout.onStartVoiceClick {
            handleVoiceClick()
        }

        if (searchFlowEnabled) {
            changeKeyboardImeToSearch()
        } else {
            changeKeyboardImeToDone()
        }

        setOnFocusChangeListener { _, isFocused ->
            when {
                !focusFromVoice && !pickedFromDropDown && !isFocused && isReal3wa(text.toString()) && !searchFlowEnabled -> {
                    viewModel.onSuggestionClicked(
                        text.toString(),
                        getReal3wa(text.toString()),
                        returnCoordinates
                    )
                }
                !allowInvalid3wa && !focusFromVoice && !pickedFromDropDown && !isFocused && !isReal3wa(
                    text.toString()
                ) && !searchFlowEnabled -> {
                    viewModel.onSuggestionClicked(text.toString(), null, returnCoordinates)
                }
                allowInvalid3wa && !focusFromVoice && !pickedFromDropDown && !isFocused && !isReal3wa(
                    text.toString()
                ) && !searchFlowEnabled -> {
                    getPicker().forceClearAndHide()
                }
            }
            if (!isFocused) {
                iconHolderLayout.setClearVisibility(GONE)
                setPaddingRelative(
                    paddingStart,
                    paddingTop,
                    originalPaddingEnd,
                    paddingBottom
                )
                hideKeyboard()
            } else {
                if (voiceEnabled)
                    setPaddingRelative(paddingStart, paddingTop, height * 2, paddingBottom)
                else setPaddingRelative(paddingStart, paddingTop, height, paddingBottom)
                if (this.text.isNullOrEmpty() && !focusFromVoice) {
                    this.setText(
                        context.getString(R.string.w3w_slashes)
                    )
                }
                showKeyboard()
                if (text?.toString()
                        ?.shouldShowClear() == true
                ) iconHolderLayout.setClearVisibility(VISIBLE)
                showImages(false)
            }
            focusFromVoice = false
        }

        this.setOnTouchListener(
            OnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP && drawableStart != null && drawableStartCallback != null) {
                    val textLocation = IntArray(2)
                    this.getLocationOnScreen(textLocation)
                    if (event.rawX <= textLocation[0] + this.totalPaddingLeft) {
                        drawableStartCallback!!.invoke()
                        return@OnTouchListener true
                    }
                }
                false
            }
        )
        addTextChangedListener(watcher)
        viewTreeObserver.addOnGlobalLayoutListener(this)

        // create empty APIManager, will fail in case dev doesn't call apiKey()
        val api = What3WordsV3("", context)
        viewModel.initializeWithWrapper(api)
    }


    /**
     * Since [W3WAutoSuggestEditText] have other views which depends on like [W3WAutoSuggestPicker], [W3WAutoSuggestErrorMessage], [W3WAutoSuggestCorrectionPicker] and multiple [voiceScreenType]'s
     * all of these have to be rendered after [W3WAutoSuggestEditText] is added to the [getViewTreeObserver] so we can use [W3WAutoSuggestEditText.getX], [W3WAutoSuggestEditText.getY], [W3WAutoSuggestEditText.getWidth] and [W3WAutoSuggestEditText.getHeight]
     * to position the dependent views correctly and we want this to run only once hence why we use [isRendered] to check if all views have already been rendered ([getViewTreeObserver].addOnGlobalLayoutListener can be called multiple times).
     *
     * Another issue found is that if first time that [onGlobalLayout] is called and [W3WAutoSuggestEditText.getVisibility] = [View.GONE] all [W3WAutoSuggestEditText.getX], [W3WAutoSuggestEditText.getY], [W3WAutoSuggestEditText.getWidth] and [W3WAutoSuggestEditText.getHeight] will be 0
     * which will be a problem when rendering/positioning the other views.
     * The solution is to check if [W3WAutoSuggestEditText] is [View.VISIBLE] before setting [isRendered] = true and render all the dependent views correctly.
     */
    override fun onGlobalLayout() {
        if (!isRendered && visibility == VISIBLE) {
            isRendered = true
            (parent as? ViewGroup)?.apply {
                if (this is LinearLayout || this is LinearLayoutCompat) {
                    Log.d(
                        "W3WAutoSuggestEditText",
                        "Running a feature reduced W3WAutoSuggestEditText, for full support use relative layouts as parent view, i.e. ConstraintLayout/RelativeLayout."
                    )
                }
            }
            if (customPicker == null) buildSuggestionList()
            if (customErrorView == null) buildErrorMessage()
            if (customCorrectionPicker == null) buildCorrection()
            buildIconHolderLayout()
            when (voiceScreenType) {
                VoiceScreenType.Inline -> {
                    iconHolderLayout.setVoiceVisibility(if (voiceEnabled && !isShowingTick) VISIBLE else INVISIBLE)
                    iconHolderLayout.setup(viewModel.repository, viewModel.getOrInitMicrophone())
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

    //region Text logic

    private fun onTextChanged(searchText: String) {
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

        // remove this when AutosuggestHelper did you mean issue is fixed.
        getCorrectionPicker().forceClearAndHide()

        if (searchText.shouldShowClear()) {
            iconHolderLayout.setClearVisibility(VISIBLE)
        } else {
            iconHolderLayout.setClearVisibility(GONE)
        }

        if (searchText.isPossible3wa() || searchText.didYouMean3wa()) {
            viewModel.autosuggest(searchText, allowFlexibleDelimiters)
        } else {
            onDisplaySuggestions?.accept(false)
            getPicker().forceClearAndHide()
            getCorrectionPicker().forceClearAndHide()
            showImages()
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
    //endregion

    //region SharedFlow logic
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sharedFlowJobs?.cancel()
    }

    /**
     * Since [W3WAutoSuggestEditText] is not lifecycle aware (maybe we should add logic for this in future?) we will use [onAttachedToWindow] to start a [CoroutineScope] using [Dispatchers.Main] and will run a [Job] that collects all [SharedFlow] from [AutosuggestTextViewModel].
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sharedFlowJobs = CoroutineScope(Dispatchers.Main).launch {
            launch {
                viewModel.suggestions.collect {
                    suggestionsObserver(it)
                }
            }
            launch {
                viewModel.didYouMean.collect {
                    didYouMeanObserver(it)
                }
            }
            launch {
                viewModel.selectedSuggestion.collect {
                    selectedSuggestionObserver(it)
                }
            }
            launch {
                viewModel.error.collect {
                    errorObserver(it)
                }
            }
        }
    }

    /**
     * [suggestionsObserver] should be called when [AutosuggestTextViewModel.suggestions] is collected.
     *
     * 1. check [W3WAutoSuggestEditText.hasFocus] is just a safe check to don't show suggestions when [W3WAutoSuggestEditText] loses focus.
     * 2. populates [lastSuggestions] with [suggestions] returned by the [viewModel], [lastSuggestions] keeps the last set suggestions so we can use them on [getReal3wa] and [isReal3wa].
     * 3. if [onDisplaySuggestions] is set it's called to notify the user the [W3WAutoSuggestPicker] is visible.
     * 4. then call [W3WAutoSuggestPicker.populateAndSetVisibility] to add the [suggestions] to [getPicker] and set visibility to [View.VISIBLE] if [List.isNotEmpty] or to [View.GONE] if [List.isEmpty].
     *
     * @param suggestions [List] of [Suggestion] collected from [AutosuggestTextViewModel.suggestions].
     */
    private fun suggestionsObserver(suggestions: List<Suggestion>) {
        if (hasFocus()) {
            lastSuggestions.apply {
                clear()
                addAll(suggestions)
            }
            onDisplaySuggestions?.accept(suggestions.isNotEmpty())
            getPicker().populateAndSetVisibility(
                suggestions,
                text.toString(),
                viewModel.options,
                returnCoordinates
            )
        }
    }

    /**
     * [errorObserver] should be called when [AutosuggestTextViewModel.error] is collected.
     *
     * 1. when [error] is not null should call [AppCompatTextView.populateAndShow] and if set [errorCallback] should be invoked with [APIResponse.What3WordsError].
     * 2. when [error] is null should call [AppCompatTextView.forceClearAndHide].
     *
     * @param error of type [APIResponse.What3WordsError] collected from [AutosuggestTextViewModel.error].
     * [error] can be populated by both Voice and Text flow. Error message should provide developer usable information of why error is happening this is not localised and not shown to end user, just sent to developer via [errorCallback].
     * [getErrorView] will show a generic error message to end user that can be localised via [errorMessageText].
     */
    private fun errorObserver(error: APIResponse.What3WordsError?) {
        if (error != null) {
            getErrorView().populateAndShow(errorMessageText)
            errorCallback?.accept(error) ?: run {
                Log.e("W3WAutoSuggestEditText", error.message)
            }
        } else {
            getErrorView().forceClearAndHide()
        }
    }

    /**
     * [didYouMeanObserver] should be called when [AutosuggestTextViewModel.didYouMean] is collected.
     *
     * 1. when [suggestion] is not null and [W3WAutoSuggestEditText.isFocused] should call [W3WAutoSuggestCorrectionPicker.populateAndShow].
     * 2. when [suggestion] is null or [W3WAutoSuggestEditText.isFocused] is lost should call [W3WAutoSuggestCorrectionPicker.forceClearAndHide].
     *
     * @param suggestion of type [Suggestion] collected from [AutosuggestTextViewModel.didYouMean].
     * [suggestion] is populated by our did you mean logic which uses a more flexible regex in case user doesn't type a strong regex match, i.e index home raft, instead of index.home.raft .
     */
    private fun didYouMeanObserver(suggestion: Suggestion?) {
        if (suggestion != null && hasFocus()) {
            getCorrectionPicker().populateAndShow(suggestion)
        } else {
            getCorrectionPicker().forceClearAndHide()
        }
    }

    /**
     * [selectedSuggestionObserver] should be called when [AutosuggestTextViewModel.selectedSuggestion] is collected.
     *
     * 1. sets [pickedFromDropDown] to true, [pickedFromDropDown] flag helps to determinate that when [W3WAutoSuggestEditText.onFocusChanged] is called that was because a [suggestion] was selected from a [getPicker] ([W3WAutoSuggestPicker]).
     * 2. if [getPicker] is visible and [suggestion] is null is because [W3WAutoSuggestEditText] lost focus while showing suggestions, so we should show [getInvalidAddressView].
     * 3. call [showImages] to show [tick] if [suggestion] is not null.
     * 4. forces to clear and hide [W3WAutoSuggestPicker.forceClearAndHide] and [W3WAutoSuggestCorrectionPicker.forceClearAndHide].
     * 5. if [onDisplaySuggestions] is set it's called to notify the user the [W3WAutoSuggestPicker] is not visible anymore.
     * 6. calls [hideKeyboard] to lose focus since full autosuggest flow is finished.
     * 7. if [suggestion] is not null update [W3WAutoSuggestEditText.setText] with [SuggestionWithCoordinates.words], if [suggestion] is null [W3WAutoSuggestEditText.setText] is emptied.
     * 8. if [callback] is set it's called with the selected [SuggestionWithCoordinates].
     *
     * @param suggestion of type [SuggestionWithCoordinates] collected from [AutosuggestTextViewModel.selectedSuggestion].
     * [suggestion] can be null if [W3WAutoSuggestEditText] loses focus with an invalid 3 word address.
     */
    private fun selectedSuggestionObserver(suggestion: SuggestionWithCoordinates?) {
        pickedFromDropDown = true
        if (getPicker().visibility == VISIBLE && suggestion == null) {
            getInvalidAddressView().populateAndShow(invalidSelectionMessageText)
        }
        showImages(suggestion != null)
        getPicker().forceClearAndHide()
        getCorrectionPicker().forceClearAndHide()
        onDisplaySuggestions?.accept(false)
        hideKeyboard()
        if (suggestion != null) {
            setText(context.getString(R.string.w3w_slashes_with_address, suggestion.words))
        } else {
            text = null
        }
        if (!isDisplayOnly) {
            oldCallback?.accept(suggestion?.backwardCompatible())
            callback?.accept(suggestion)
        } else {
            isDisplayOnly = false
        }
    }
    //endregion

    //region Voice logic
    /**
     * [handleVoiceError] should be called when any of the voice screens [voiceScreenType] returns an error.
     *
     * 1. if [error] is not null will show [getErrorView] which can be the default one or a custom one set on [onSelected] and invoke [errorCallback] if set.
     * 2. set [setHint] back to [oldHint] which is basically the text flow hint.
     * 3. calls [showKeyboard] to trigger focus so user can start using the text search flow.
     *
     * @param error of type [APIResponse.What3WordsError] returned by any of [voiceScreenType].
     * @param error can be null if user force close screen in [VoicePulseLayout.onErrorCallback] and [VoicePulseLayoutFullScreen.onErrorCallback].
     */
    private fun handleVoiceError(error: APIResponse.What3WordsError?) {
        if (error != null) {
            getErrorView().populateAndShow(errorMessageText)
            errorCallback?.accept(error) ?: run {
                Log.e("W3WAutoSuggestEditText", error.message)
            }
        }
        if (!isDisplayOnly) {
            showKeyboard()
        }
    }

    /**
     * [handleVoiceClick] should be called when [InlineVoicePulseLayout.startVoiceClick] callback is invoked or when [toggleVoice] called programmatically.
     *
     * 1. sets [focusFromVoice] to true, this flag helps controlling the voice flow, since user journey is different from text search.
     * 2. checks if [isShowingTick] is false, this is just a safe check since [InlineVoicePulseLayout] and [tick] should never be shown at the same time.
     * 3. since we are in voice flow [hideKeyboard] which will force [W3WAutoSuggestEditText] to lose focus.
     * 4. clear previous query and suggestions by setting [setText] to empty and calling [W3WAutoSuggestPicker.forceClearAndHide].
     * 5. invokes callback (if set) [onDisplaySuggestions] so developer can show/hide any extra tips/layouts.
     *
     * 6. when [voiceScreenType] is:
     * - [VoiceScreenType.Inline] toggles [InlineVoicePulseLayout], this is if not listening starts, if listening stops.
     * - [VoiceScreenType.AnimatedPopup] toggles [VoicePulseLayout], in this case will only start because [VoicePulseLayout] is covering all parent view.
     * - [VoiceScreenType.Fullscreen] toggles [VoicePulseLayoutFullScreen], in this case will only start because [VoicePulseLayoutFullScreen] is covering all parent view.
     */
    private fun handleVoiceClick() {
        focusFromVoice = true
        if (!isShowingTick) {
            hideKeyboard()
            this.setText("")
            getPicker().forceClearAndHide()
            when (voiceScreenType) {
                VoiceScreenType.Inline -> {
                    iconHolderLayout.toggle(
                        viewModel.options,
                        returnCoordinates,
                        voiceLanguage
                    )
                    hint = voicePlaceholder
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

    /**
     * [handleVoiceSuggestions] should be called when any of the voice screens [voiceScreenType] returns suggestions.
     *
     * 1. sets [setHint] back to text default saved on [oldHint]
     *
     * [suggestions] if is empty shows:
     * 1. invalid address error message [getInvalidAddressView] that can be the default or a custom set on [onSelected].
     * 2. invokes callback (if set) [onDisplaySuggestions] so developer can show/hide any extra tips/layouts.
     *
     * [suggestions] if is not empty:
     * 1. shows and populates the suggestions on [getPicker] [W3WAutoSuggestPicker] that can be the default picker or a custom set on [onSelected].
     * 2. [setText] is set with the [Suggestion] with highest [Suggestion.rank]
     * 3. invokes callback (if set) [onDisplaySuggestions] so developer can show/hide any extra tips/layouts.
     *
     * finally, calls [showKeyboard] to trigger focus so user can start using the text search flow.
     * @param suggestions returned by any of [voiceScreenType].
     */
    private fun handleVoiceSuggestions(suggestions: List<Suggestion>) {
        this@W3WAutoSuggestEditText.isFocusableInTouchMode = true
        if (suggestions.isEmpty()) {
            getInvalidAddressView().populateAndShow(invalidSelectionMessageText)
            onDisplaySuggestions?.accept(false)
        } else {
            pickedFromVoice = true
            this.setText(
                context.getString(
                    R.string.w3w_slashes_with_address,
                    suggestions.minByOrNull { it.rank }!!.words
                )
            )
            onDisplaySuggestions?.accept(true)
            // Query empty because we don't want to highlight when using voice.
            getPicker().populateAndSetVisibility(
                suggestions,
                "",
                viewModel.options,
                returnCoordinates
            )
        }
        if (!isDisplayOnly) {
            showKeyboard()
        }
    }

    private fun setupFullScreenVoice() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                buildVoiceFullscreen()
                voicePulseLayoutFullScreen?.let { fullScreenVoice ->
                    fullScreenVoice.setup(viewModel.repository, viewModel.getOrInitMicrophone())
                    fullScreenVoice.onResultsCallback {
                        handleVoiceSuggestions(it)
                    }
                    fullScreenVoice.onErrorCallback {
                        handleVoiceError(it)
                    }
                }
            },
            100
        )
    }

    private fun setupAnimatedPopupVoice() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                buildVoiceAnimatedPopup()
                voiceAnimatedPopup?.let { voiceAnimatedPopup ->
                    voiceAnimatedPopup.setup(viewModel.repository, viewModel.getOrInitMicrophone())
                    voiceAnimatedPopup.onResultsCallback {
                        handleVoiceSuggestions(it)
                    }
                    voiceAnimatedPopup.onErrorCallback {
                        handleVoiceError(it)
                    }
                }
            },
            100
        )
    }
    private fun stopVoiceListener(){
        voiceAnimatedPopup?.stopVoiceListener()
        voicePulseLayoutFullScreen?.stopVoiceListener()
        iconHolderLayout.stopVoiceListener()
    }

    private fun changeKeyboardImeToSearch() {
        this.imeOptions = (EditorInfo.IME_ACTION_SEARCH)
        setOnEditorActionListener { _, i, event ->
            if (i == EditorInfo.IME_ACTION_SEARCH || (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))) {
                clearFocus()
                true
            } else {
                false
            }
        }
    }

    private fun changeKeyboardImeToDone() {
        this.imeOptions =
            (EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_FULLSCREEN or EditorInfo.IME_FLAG_NO_EXTRACT_UI)
        setOnEditorActionListener { _, i, event ->
            if (i == EditorInfo.IME_ACTION_DONE || (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))) {
                clearFocus()
                true
            } else {
                false
            }
        }
    }

    //endregion

    //region Public custom properties

    /** Set your What3Words API Key which will be used to get suggestions and coordinates (if enabled)
     *
     * @param key your API key from what3words developer dashboard
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun apiKey(key: String, voiceProvider: VoiceProvider = VoiceApi(apiKey = key)): W3WAutoSuggestEditText {
        val api = What3WordsV3(
            key,
            voiceProvider = voiceProvider,
            context,
            mapOf(
                "X-W3W-AS-Component" to "what3words-Android/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE})",
                "component_session_id" to SESSION_ID
            )
        )
        viewModel.initializeWithWrapper(api)
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
        val api = What3WordsV3(
            key,
            endpoint,
            context,
            headers.toMutableMap().apply {
                put("component_session_id", SESSION_ID)
            }
        )
        viewModel.initializeWithWrapper(api)
        return this
    }

    /** Set your What3Words API Key and the Enterprise Suite API Server endpoint which will be used to get suggestions and coordinates (if enabled)
     *
     * @param key your API key from what3words developer dashboard
     * @param endpoint your Enterprise API endpoint
     * @param voiceProvider an implementation of the [VoiceProvider] interface that will be used to process voice data.
     * @param headers any custom headers needed for your Enterprise API
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun apiKey(
        key: String,
        endpoint: String,
        voiceProvider: VoiceProvider = VoiceApi(apiKey = key),
        headers: Map<String, String> = mapOf(),
    ): W3WAutoSuggestEditText {
        val api = What3WordsV3(
            apiKey = key,
            endpoint = endpoint,
            context = context,
            headers = headers.toMutableMap().apply {
                put("component_session_id", SESSION_ID)
            },
            voiceProvider = voiceProvider
        )
        viewModel.initializeWithWrapper(api)
        return this
    }

    /** Set your What3Words API Key and the Enterprise Suite API Server endpoint which will be used to get suggestions and coordinates (if enabled)
     *
     * @param key your API key from what3words developer dashboard
     * @param endpoint your Enterprise API endpoint
     * @param voiceEndpoint your custom Voice API endpoint
     * @param headers any custom headers needed for your Enterprise API
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun apiKey(
        key: String,
        endpoint: String,
        voiceEndpoint: String,
        headers: Map<String, String> = mapOf()
    ): W3WAutoSuggestEditText {
        val api = What3WordsV3(
            key,
            endpoint,
            voiceEndpoint,
            context,
            headers.toMutableMap().apply {
                put("component_session_id", SESSION_ID)
            }
        )
        viewModel.initializeWithWrapper(api)
        return this
    }


    /** Set your What3Words Manager with your SDK instance
     *
     * @param logicManager manager created using SDK instead of API
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun sdk(
        wrapper: What3WordsAndroidWrapper
    ): W3WAutoSuggestEditText {
        viewModel.initializeWithWrapper(wrapper)
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
     * give preference to those near the focus. For convenience, longitude is allowed to wrap around the 180 line, so 361 is equivalent to 1.
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
     * Specifies the number of results within the results set which will have a focus. Defaults to [nResults].
     * This allows you to run autosuggest with a mix of focussed and unfocussed results, to give you a "blend" of the two. This is exactly what the old V2
     * standardblend did, and standardblend behaviour can easily be replicated by passing [nFocusResults] (1)
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
     * [radius] in kilometres. For convenience, longitude is allowed to wrap around 180 degrees. For example 181 is equivalent to -179.
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
     * (for example, to restrict to Belgium and the UK, use [clipToCountry] (listOf("GB", "BE")). [clipToCountry] will also accept lowercase
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
        stopVoiceListener()
        this.voiceEnabled = enabled
        voiceScreenType = VoiceScreenType.Inline
        iconHolderLayout.setup(viewModel.repository, viewModel.getOrInitMicrophone())
        iconHolderLayout.setVoiceVisibility(if (voiceEnabled && !isShowingTick) VISIBLE else INVISIBLE)
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
        type: VoiceScreenType,
        micIcon: Drawable? = null
    ): W3WAutoSuggestEditText {
        stopVoiceListener()
        this.voiceEnabled = enabled
        this.voiceScreenType = type
        iconHolderLayout.setVoiceVisibility(if (voiceEnabled && !isShowingTick) VISIBLE else INVISIBLE)
        if (micIcon != null) {
            iconHolderLayout.setCustomIcon(micIcon)
        }
        when (type) {
            VoiceScreenType.Inline -> {
                iconHolderLayout.setup(viewModel.repository, viewModel.getOrInitMicrophone())
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

    /**
     * Enable voice fullscreen popup for autosuggest component
     *
     * @param enabled if voice fullscreen should be enabled
     * @return same [W3WAutoSuggestEditText] instance
     */
    @Deprecated("Use enabledVoice(boolean, screenType, optionalIcon)")
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
    fun onSuggestionSelected(
        picker: W3WAutoSuggestPicker? = null,
        invalidAddressMessageView: AppCompatTextView? = null,
        callback: Consumer<SuggestionWithCoordinates?>,
    ): W3WAutoSuggestEditText {
        this.callback = callback
        if (picker != null) {
            picker.setup(viewModel, displayUnits)
            defaultPicker.forceClearAndHide()
        } else customPicker?.forceClearAndHide()
        this.customInvalidAddressMessageView = invalidAddressMessageView
        this.customPicker = picker
        return this
    }

    @Deprecated("", ReplaceWith("onSuggestionSelected { }"))
    fun onSelected(
        picker: W3WAutoSuggestPicker? = null,
        invalidAddressMessageView: AppCompatTextView? = null,
        callback: Consumer<W3WSuggestion?>,
    ): W3WAutoSuggestEditText {
        this.oldCallback = callback
        if (picker != null) {
            picker.setup(viewModel, displayUnits)
            defaultPicker.forceClearAndHide()
        } else customPicker?.forceClearAndHide()
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
     * Callback to update view when suggestion picker is being displayed or not, example, show tips when false hide tips when true
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
        this.customCorrectionPicker?.forceClearAndHide()
        defaultCorrectionPicker.forceClearAndHide()

        this.customCorrectionPicker = customCorrectionPicker
        this.customCorrectionPicker?.setCorrectionMessage(correctionMessage)
            ?.internalCallback { selectedSuggestion ->
                setText(
                    context.getString(
                        R.string.w3w_slashes_with_address,
                        selectedSuggestion.words
                    )
                )
                this@W3WAutoSuggestEditText.setSelection(this.length())
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
     * DEPRECATED
     */
    @Deprecated("", ReplaceWith("displayUnit(com.what3words.components.models.DisplayUnits)"))
    fun displayUnit(
        units: com.what3words.components.utils.DisplayUnits
    ): W3WAutoSuggestEditText {
        this.displayUnits = units.backwardCompatible()
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
     * This function will trigger the voice programmatically, in some cases developer wants to start listening without user touching the screen.
     *
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun toggleVoice(): W3WAutoSuggestEditText {
        handleVoiceClick()
        return this
    }

    /**
     * Allow EditText to accept different delimiters than the what3words standard full stop "index.home.raft".
     * By default [allowFlexibleDelimiters] is false, when you type an existing three word address with a different delimiter (i.e "index home raft") will trigger our Did You Mean feature, but if you set [allowFlexibleDelimiters] (true) "index home raft" will be parsed to "index.home.raft" and will return the [nResults] suggestions for that query.
     *
     * @param isAllowed if true [W3WAutoSuggestEditText] will accept flexible delimiters and show suggestions, if false will not accept flexible delimiters but if is that three word address exist will show the did you mean feature.
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun allowFlexibleDelimiters(isAllowed: Boolean): W3WAutoSuggestEditText {
        this.allowFlexibleDelimiters = isAllowed
        return this
    }

    /**
     * Search flow will keep the suggestions visible when [W3WAutoSuggestEditText] loses focus, meaning that is not going to check if [W3WAutoSuggestEditText.getText] is a valid 3wa and clear text and show error message if not.
     *
     * @param isEnabled if true [W3WAutoSuggestEditText] will not verify if current text is a valid 3wa on losing focus (normal behaviour) keeping the suggestions visible until user clicks or deletes text.
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun searchFlowEnabled(isEnabled: Boolean): W3WAutoSuggestEditText {
        this.searchFlowEnabled = isEnabled
        if (searchFlowEnabled) {
            changeKeyboardImeToSearch()
        } else {
            changeKeyboardImeToDone()
        }
        return this
    }

    fun hideSelectedIcon(b: Boolean): W3WAutoSuggestEditText {
        this.hideSelectedIcon = b
        return this
    }

    /**
     * Makes AutoSuggest prefer results on land to those in the sea.
     * This setting is on by default. Use false to disable this setting and receive more suggestions in the sea.                                                                                                                                                                             t to keep any text user types, default is false, by default EditText will be cleared if not a valid 3 word address, set to true to ignore this default behaviour.
     *
     * @param isPreferred if true, autosuggest results will be restricted to land and vice-versa
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun preferLand(isPreferred: Boolean): W3WAutoSuggestEditText {
        viewModel.options.preferLand = isPreferred
        return this
    }

    /**
     * Display a suggestion selected by one of our other components, i.e: map-component. This will make the integration between both easier making giving a way to select by search text/voice or map click.
     *
     * @param suggestion [SuggestionWithCoordinates] returned by other components, i.e.: map-components
     * @return same [W3WAutoSuggestEditText] instance
     */
    fun display(suggestion: SuggestionWithCoordinates): W3WAutoSuggestEditText {
        isDisplayOnly = true
        viewModel.display(suggestion)
        return this
    }

    fun options(options: AutosuggestOptions): W3WAutoSuggestEditText {
        viewModel.options = options
        return this
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
    ): W3WAutoSuggestEditText {
        val optimalRecordingRate = Microphone.getOptimalSampleRate(preferredSampleRate = recordingRate)
        viewModel.microphone = Microphone(optimalRecordingRate, encoding, channel, audioSource)
        iconHolderLayout.microphone(optimalRecordingRate, encoding, channel, audioSource)
        voiceAnimatedPopup?.microphone(optimalRecordingRate, encoding, channel, audioSource)
        voicePulseLayoutFullScreen?.microphone(optimalRecordingRate, encoding, channel, audioSource)
        return this
    }

    //endregion
}
