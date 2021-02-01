package com.what3words.components.text

import android.os.Build
import com.what3words.components.text.What3WordsV3ServiceSelection.Companion.DEFAULT_ENDPOINT
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.Suggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

internal interface What3WordsV3ServiceSelection {
    companion object {
        const val DEFAULT_ENDPOINT = "https://api.what3words.com/v3/"
    }

    @GET("autosuggest-selection")
    fun autosuggestSelection(
        @Query("key") key: String,
        @Query("selection") words: String,
        @Query("rank") rank: Int,
        @Query("raw-input") query: String,
        @QueryMap options: Map<String, String>
    ): Call<Unit>
}

internal fun populateQueryOptions(
    queryMap: MutableMap<String, String>,
    source: String,
    voiceLanguage: String?,
    focus: Coordinates?,
    language: String?,
    nResults: Int?,
    nFocusResults: Int?,
    clipToCountry: Array<String>?,
    clipToCircle: Coordinates?,
    clipToCircleRadius: Double?,
    clipToBoundingBox: BoundingBox?,
    clipToPolygon: Array<Coordinates>?
) {
    queryMap.clear()
    queryMap["source-api"] = source
    voiceLanguage?.let {
        queryMap["voice-language"] = voiceLanguage
    }
    focus?.let {
        queryMap["focus"] = it.lat.toString() + "," + it.lng.toString()
    }
    language?.let {
        queryMap["language"] = it
    }
    nResults?.let {
        queryMap["n-results"] = it.toString()
    }
    nFocusResults?.let {
        queryMap["n-focus-results"] = it.toString()
    }
    clipToCountry?.let {
        queryMap["clip-to-country"] = it.joinToString(",")
    }
    clipToCircle?.let {
        queryMap["clip-to-circle"] =
            it.lat.toString() + "," + it.lng.toString() + "," + (clipToCircleRadius?.toString()
                ?: "0")
    }
    clipToBoundingBox?.let {
        queryMap["clip-to-bounding-box"] =
            it.sw.lat.toString() + "," + it.sw.lng.toString() + "," + it.ne.lat.toString() + "," + it.ne.lng.toString()
    }
    clipToPolygon?.let { coordinates ->
        queryMap["clip-to-polygon"] =
            coordinates.joinToString(",") { "${it.lat},${it.lng}" }
    }
}

internal fun handleSelectionTrack(
    suggestion: Suggestion,
    input: String,
    queries: Map<String, String>,
    apiKey: String
) {
    CoroutineScope(Dispatchers.IO).launch {
        val builder = OkHttpClient().newBuilder()
        builder.addInterceptor { chain: Interceptor.Chain ->
            val request: Request =
                chain.request().newBuilder().addHeader(
                    "X-W3W-AS-Component",
                    "what3words-Android/${com.what3words.components.BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE})"
                ).build()
            chain.proceed(request)
        }

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(DEFAULT_ENDPOINT)
            .client(builder.build())
            .build()

        val w3wSelection: What3WordsV3ServiceSelection =
            retrofit.create(What3WordsV3ServiceSelection::class.java)

        val call: Call<Unit> =
            w3wSelection.autosuggestSelection(
                apiKey,
                suggestion.words,
                suggestion.rank,
                input,
                queries
            )

        call.enqueue(object : Callback<Unit> {
            override fun onFailure(call: Call<Unit>, t: Throwable) {
            }

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
            }
        })
    }
}
