package com.what3words.components.models

import androidx.core.util.Consumer
import com.what3words.androidwrapper.What3WordsAndroidWrapper
import com.what3words.androidwrapper.helpers.DefaultDispatcherProvider
import com.what3words.androidwrapper.helpers.DispatcherProvider
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.javawrapper.What3WordsV3.didYouMean3wa
import com.what3words.javawrapper.What3WordsV3.isPossible3wa
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.SourceApi
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//To be deleted when we migrate to android wrapper 4.0.2 with the new Core Library.
internal class AutosuggestHelper(
    private val api: What3WordsAndroidWrapper,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) {
    private var allowFlexibleDelimiters: Boolean = false
    private var options: AutosuggestOptions? = null
    private var searchJob: Job? = null
    private val splitRegex = Regex("[.｡。･・︒។։။۔።। ,\\\\^_/+'&\\:;|　-]+")

    /**
     * Update AutosuggestHelper query and receive suggestions (strong regex applied) or a did you mean (flexible regex applied) from our Autosuggest API.
     *
     * @param searchText the updated query.
     * @param onSuccessListener the callback for suggestions.
     * @param onFailureListener the callback for API errors [APIResponse.What3WordsError].
     * @param onDidYouMeanListener the callback for did you mean results.
     */
    fun update(
        searchText: String,
        onSuccessListener: Consumer<List<Suggestion>>,
        onFailureListener: Consumer<APIResponse.What3WordsError>?,
        onDidYouMeanListener: Consumer<Suggestion>?
    ) {
        var isDidYouMean = false
        val searchFiltered: String? = when {
            isPossible3wa(searchText) -> searchText
            !allowFlexibleDelimiters && didYouMean3wa(searchText) -> {
                isDidYouMean = true
                searchText.split(splitRegex, 3).joinToString(".")
            }

            allowFlexibleDelimiters && didYouMean3wa(searchText) -> searchText.split(
                splitRegex,
                3
            ).joinToString(".")

            else -> null
        }
        if (searchFiltered == null) {
            onSuccessListener.accept(emptyList())
        } else {
            performAutosuggest(
                searchFiltered,
                isDidYouMean,
                onSuccessListener,
                onFailureListener,
                onDidYouMeanListener
            )
        }
    }

    private fun performAutosuggest(
        finalQuery: String,
        isDidYouMean: Boolean,
        onSuccessListener: Consumer<List<Suggestion>>,
        onFailureListener: Consumer<APIResponse.What3WordsError>? = null,
        onDidYouMeanListener: Consumer<Suggestion>? = null
    ) {
        searchJob?.cancel()
        searchJob = CoroutineScope(dispatchers.io()).launch {
            delay(250)
            val builder = api.autosuggest(finalQuery)
            if (options != null) builder.options(options)
            val res = builder.execute()
            CoroutineScope(dispatchers.main()).launch {
                if (res.isSuccessful) {
                    if (isDidYouMean) {
                        res.suggestions.firstOrNull {
                            it.words.lowercase(Locale.getDefault()) == finalQuery.lowercase(
                                Locale.getDefault()
                            )
                        }?.let {
                            onDidYouMeanListener?.accept(it)
                        }
                    } else {
                        onSuccessListener.accept(res.suggestions)
                    }
                } else {
                    onFailureListener?.accept(res.error)
                }
            }
        }
    }

    /**
     * When suggestion is selected this will provide all three word address information needed (without coordinates).
     *
     * @param rawString the updated raw query.
     * @param suggestion the selected suggestion.
     * @param onSuccessListener the callback for the full suggestion information (without coordinates) [Suggestion].
     */
    fun selected(
        rawString: String,
        suggestion: Suggestion,
        onSuccessListener: Consumer<Suggestion>
    ) {
        CoroutineScope(dispatchers.io()).launch {
            val builder = api.autosuggestionSelection(
                rawString,
                suggestion.words,
                suggestion.rank,
                SourceApi.TEXT
            )
            if (options != null) builder.options(options)
            builder.execute()
        }
        onSuccessListener.accept(suggestion)
    }

    /**
     * When suggestion is selected this will provide all three word address information needed with coordinates.
     * Note that selectedWithCoordinates() will convert the three word address to a lat/lng which will count against your plan's quota.
     *
     * @param rawString the updated raw query.
     * @param suggestion the selected suggestion.
     * @param onSuccessListener the callback for the full suggestion information with coordinates [SuggestionWithCoordinates].
     * @param onFailureListener the callback for API errors [APIResponse.What3WordsError].
     */
    fun selectedWithCoordinates(
        rawString: String,
        suggestion: Suggestion,
        onSuccessListener: Consumer<SuggestionWithCoordinates>,
        onFailureListener: Consumer<APIResponse.What3WordsError>?
    ) {
        CoroutineScope(dispatchers.io()).launch {
            val builder = api.autosuggestionSelection(
                rawString,
                suggestion.words,
                suggestion.rank,
                SourceApi.TEXT
            )
            if (options != null) builder.options(options)
            val builderConvert = api.convertToCoordinates(suggestion.words)
            builder.execute()
            val res = builderConvert.execute()
            CoroutineScope(dispatchers.main()).launch {
                if (res.isSuccessful) {
                    val newSuggestion = SuggestionWithCoordinates(suggestion, res)
                    onSuccessListener.accept(newSuggestion)
                } else {
                    onFailureListener?.accept(res.error)
                }
            }
        }
    }

    /**
     * Set all options at once using [AutosuggestOptions]
     *
     * @param options the [AutosuggestOptions] with all filters/clipping needed to be applied to the search
     * @return a [AutosuggestHelper] instance suitable for invoking a autosuggest API request
     */
    fun options(options: AutosuggestOptions): AutosuggestHelper {
        this.options = options
        return this
    }

    /**
     * Flexible delimiters feature allows our regex to be less precise on delimiters, this means that "filled count soa" or "filled,count,soa" will be parsed to "filled.count.soa" and send to our autosuggest API.
     *
     * @param boolean enables flexible delimiters feature enabled (false by default)
     * @return a [AutosuggestHelper] instance
     */
    fun allowFlexibleDelimiters(boolean: Boolean): AutosuggestHelper {
        allowFlexibleDelimiters = boolean
        return this
    }
}

internal class AutosuggestRepository(private val wrapper: What3WordsAndroidWrapper) {

    private val helper: AutosuggestHelper by lazy {
        AutosuggestHelper(wrapper)
    }

    suspend fun autosuggest(
        query: String,
        options: AutosuggestOptions?,
        allowFlexibleDelimiters: Boolean
    ): Either<APIResponse.What3WordsError, Pair<List<Suggestion>?, Suggestion?>> =
        suspendCoroutine { cont ->
            if (options != null) helper.options(options)
            helper.allowFlexibleDelimiters(allowFlexibleDelimiters)
            helper.update(
                query,
                {
                    cont.resume(Either.Right(Pair(it, null)))
                },
                {
                    cont.resume(Either.Left(it))
                },
                {
                    cont.resume(Either.Right(Pair(null, it)))
                }
            )
        }

    suspend fun autosuggest(
        microphone: Microphone,
        options: AutosuggestOptions,
        voiceLanguage: String
    ): Either<APIResponse.What3WordsError, VoiceAutosuggestRepository> = suspendCoroutine { cont ->
        val builder = wrapper.autosuggest(microphone, voiceLanguage).apply {
            options.nResults?.let {
                this.nResults(it)
            }
            options.focus?.let {
                this.focus(it)
            }
            options.nFocusResults?.let {
                this.nFocusResults(it)
            }
            options.clipToCountry?.let {
                this.clipToCountry(it.toList())
            }
            options.clipToCircle?.let {
                this.clipToCircle(it, options.clipToCircleRadius ?: 0.0)
            }
            options.clipToBoundingBox?.let {
                this.clipToBoundingBox(it)
            }
            options.clipToPolygon?.let { coordinates ->
                this.clipToPolygon(coordinates.toList())
            }
        }
        val voiceManager = VoiceAutosuggestRepository(builder)
        cont.resume(Either.Right(voiceManager))
    }

    suspend fun selected(
        rawQuery: String,
        suggestion: Suggestion
    ): Either<APIResponse.What3WordsError, SuggestionWithCoordinates> = suspendCoroutine { cont ->
        helper.selected(
            rawQuery,
            suggestion
        ) {
            cont.resume(Either.Right(SuggestionWithCoordinates(it)))
        }
    }

    suspend fun selectedWithCoordinates(
        rawQuery: String,
        suggestion: Suggestion
    ): Either<APIResponse.What3WordsError, SuggestionWithCoordinates> = suspendCoroutine { cont ->
        helper.selectedWithCoordinates(
            rawQuery,
            suggestion,
            {
                cont.resume(Either.Right(it))
            },
            {
                cont.resume(Either.Left(it))
            }
        )
    }

    suspend fun multipleWithCoordinates(
        rawQuery: String,
        suggestions: List<Suggestion>
    ): Either<APIResponse.What3WordsError, List<SuggestionWithCoordinates>> =
        suspendCoroutine { cont ->
            val list = mutableListOf<SuggestionWithCoordinates>()
            var allSuccess = true
            suggestions.forEach {
                val res = wrapper.convertToCoordinates(it.words).execute()
                if (res.isSuccessful) {
                    list.add(SuggestionWithCoordinates(it, res.coordinates))
                } else {
                    allSuccess = false
                    cont.resume(Either.Left(res.error))
                    return@forEach
                }
            }
            if (allSuccess) cont.resume(Either.Right(list))
            else cont.resume(Either.Left(APIResponse.What3WordsError.UNKNOWN_ERROR))
        }

    fun isVoiceEnabled(): Boolean {
        return true
    }
}
