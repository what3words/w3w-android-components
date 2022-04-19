package com.what3words.testing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.what3words.testing.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        with(binding) {
            w3wAutoSuggestEditText.apiKey(BuildConfig.W3W_API_KEY)
                .onSuggestionSelected { suggestionWithCoordinates ->
                    suggestionWithCoordinates.let {
                        if (it == null) {
                            w3wSuggestionInfo.text = ""
                            return@let
                        }
                        w3wSuggestionInfo.text = String.format(
                            resources.getString(R.string.text_search_result),
                            suggestionWithCoordinates!!.words,
                            suggestionWithCoordinates.country,
                            suggestionWithCoordinates.nearestPlace,
                            if (suggestionWithCoordinates.distanceToFocusKm == null) "N/A" else "${suggestionWithCoordinates.distanceToFocusKm}km",
                            "${suggestionWithCoordinates.coordinates?.lat}",
                            "${suggestionWithCoordinates.coordinates?.lng}"
                        )
                    }
                }
                .onError {
                    Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                }
            setContentView(root)
        }
    }
}