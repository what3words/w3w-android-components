package com.what3words.autosuggest.text

import android.os.Build
import com.what3words.autosuggest.BuildConfig
import com.what3words.autosuggest.text.What3WordsV3ServiceSelection.Companion.DEFAULT_ENDPOINT
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


interface What3WordsV3ServiceSelection {
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
                    "what3words-Android/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE})"
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
