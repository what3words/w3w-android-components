package com.what3words.autosuggestsample.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.what3words.autosuggestsample.R
import com.what3words.autosuggestsample.util.addOnTextChangedListener
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        suggestionEditText.apiKey("YOUR_API_KEY_HERE")
            .returnCoordinates(false)
            .onSelected { suggestion, latitude, longitude ->
                if (suggestion != null) {
                    selectedInfo.text =
                        "words: ${suggestion.words}\ncountry: ${suggestion.country}\nnear: ${suggestion.nearestPlace}\ndistance: ${if (suggestion.distanceToFocusKm == null) "N/A" else suggestion.distanceToFocusKm.toString() + "km"}\nlatitude: $latitude\nlongitude: $longitude"
                } else {
                    selectedInfo.text = ""
                }
            }

        checkboxCoordinates.setOnCheckedChangeListener { _, b ->
            suggestionEditText.returnCoordinates(b)
        }

        checkboxVoiceFullscreen.setOnCheckedChangeListener { _, b ->
            suggestionEditText.voiceFullscreen(b)
        }

        checkboxVoiceEnabled.setOnCheckedChangeListener { _, b ->
            suggestionEditText.voiceEnabled(b)
        }

        textPlaceholder.setText(R.string.input_hint)
        textPlaceholder.addOnTextChangedListener {
            suggestionEditText.hint = it
        }

        textVoicePlaceholder.setText(R.string.voice_placeholder)
        textVoicePlaceholder.addOnTextChangedListener {
            suggestionEditText.voicePlaceholder(it)
        }

        textLanguage.addOnTextChangedListener {
            suggestionEditText.language(it)
        }

        textVoiceLanguage.setText("en")
        textVoiceLanguage.addOnTextChangedListener {
            suggestionEditText.voiceLanguage(it)
        }

        textClipToCountry.addOnTextChangedListener { input ->
            val test = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            suggestionEditText.clipToCountry(test)
        }

        textFocus.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val lat = latLong.getOrNull(0)?.toDoubleOrNull()
            val long = latLong.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && long != null) {
                suggestionEditText.focus(Coordinates(lat, long))
            } else {
                suggestionEditText.focus(null)
            }
        }

        textClipToCircle.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val lat = latLong.getOrNull(0)?.toDoubleOrNull()
            val long = latLong.getOrNull(1)?.toDoubleOrNull()
            val km = latLong.getOrNull(2)?.toDoubleOrNull()
            if (lat != null && long != null) {
                suggestionEditText.clipToCircle(Coordinates(lat, long), km ?: 0.0)
            } else {
                suggestionEditText.clipToCircle(null, null)
            }
        }

        textClipToBoundingBox.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val swLat = latLong.getOrNull(0)?.toDoubleOrNull()
            val swLong = latLong.getOrNull(1)?.toDoubleOrNull()
            val neLat = latLong.getOrNull(2)?.toDoubleOrNull()
            val neLong = latLong.getOrNull(3)?.toDoubleOrNull()
            if (swLat != null && swLong != null && neLat != null && neLong != null) {
                suggestionEditText.clipToBoundingBox(
                    BoundingBox(
                        Coordinates(swLat, swLong),
                        Coordinates(neLat, neLong)
                    )
                )
            } else {
                suggestionEditText.clipToBoundingBox(null)
            }
        }

        textClipToPolygon.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val listCoordinates = mutableListOf<Coordinates>()
            if (latLong.count() % 2 == 0) {
                for (x in 0 until latLong.count() step 2) {
                    if (latLong[x].toDoubleOrNull() != null &&
                        latLong[x + 1].toDoubleOrNull() != null
                    ) {
                        listCoordinates.add(
                            Coordinates(
                                latLong[x].toDouble(),
                                latLong[x + 1].toDouble()
                            )
                        )
                    }
                }
            }
            suggestionEditText.clipToPolygon(
                listCoordinates
            )
        }
    }
}