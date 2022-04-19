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
                        w3wSuggestionInfo.text =
                            "words: ${suggestionWithCoordinates!!.words}\ncountry: ${suggestionWithCoordinates.country}\nnear: ${suggestionWithCoordinates.nearestPlace}\ndistance: ${if (suggestionWithCoordinates.distanceToFocusKm == null) "N/A" else suggestionWithCoordinates.distanceToFocusKm.toString() + "km"}\nlatitude: ${suggestionWithCoordinates.coordinates?.lat}\nlongitude: ${suggestionWithCoordinates.coordinates?.lng}"

                    }
                }
                .onError {
                    Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                }
            setContentView(root)
        }
    }
}