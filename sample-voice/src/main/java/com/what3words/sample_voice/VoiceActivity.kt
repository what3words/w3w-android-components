package com.what3words.sample_voice

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.what3words.autosuggestsample.util.addOnTextChangedListener
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import kotlinx.android.synthetic.main.activity_voice.*

class VoiceActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice)

        w3wVoice.apiKey("TCRPZKEE")
            .onSuggestions { suggestions ->
                val suggestion = suggestions.firstOrNull()
                if (suggestion != null) {
                    selectedInfo.text =
                        "words: ${suggestion.info.words}\ncountry: ${suggestion.info.country}\nnear: ${suggestion.info.nearestPlace}\ndistance: ${if (suggestion.info.distanceToFocusKm == null) "N/A" else suggestion.info.distanceToFocusKm.toString() + "km"}\nlatitude: ${suggestion.coordinates?.lat}\nlongitude: ${suggestion.coordinates?.lng}"
                } else {
                    selectedInfo.text = ""
                }
            }

        w3wPicker.onSelected { suggestion ->
            if (suggestion != null) {
                selectedInfo.text =
                    "words: ${suggestion.info.words}\ncountry: ${suggestion.info.country}\nnear: ${suggestion.info.nearestPlace}\ndistance: ${if (suggestion.info.distanceToFocusKm == null) "N/A" else suggestion.info.distanceToFocusKm.toString() + "km"}\nlatitude: ${suggestion.coordinates?.lat}\nlongitude: ${suggestion.coordinates?.lng}"
            } else {
                selectedInfo.text = ""
            }
        }

        checkboxCoordinates.setOnCheckedChangeListener { _, b ->
            w3wVoice.returnCoordinates(b)
        }

        checkboxPicker.setOnCheckedChangeListener { _, b ->
            w3wVoice.picker(w3wPicker)
        }

        textVoiceLanguage.setText("en")
        textVoiceLanguage.addOnTextChangedListener {
            w3wVoice.voiceLanguage(it)
        }

        textClipToCountry.addOnTextChangedListener { input ->
            val test = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            w3wVoice.clipToCountry(test)
        }

        textFocus.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val lat = latLong.getOrNull(0)?.toDoubleOrNull()
            val long = latLong.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && long != null) {
                w3wVoice.focus(Coordinates(lat, long))
            } else {
                w3wVoice.focus(null)
            }
        }

        textClipToCircle.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val lat = latLong.getOrNull(0)?.toDoubleOrNull()
            val long = latLong.getOrNull(1)?.toDoubleOrNull()
            val km = latLong.getOrNull(2)?.toDoubleOrNull()
            if (lat != null && long != null) {
                w3wVoice.clipToCircle(Coordinates(lat, long), km ?: 0.0)
            } else {
                w3wVoice.clipToCircle(null, null)
            }
        }

        textClipToBoundingBox.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val swLat = latLong.getOrNull(0)?.toDoubleOrNull()
            val swLong = latLong.getOrNull(1)?.toDoubleOrNull()
            val neLat = latLong.getOrNull(2)?.toDoubleOrNull()
            val neLong = latLong.getOrNull(3)?.toDoubleOrNull()
            if (swLat != null && swLong != null && neLat != null && neLong != null) {
                w3wVoice.clipToBoundingBox(
                    BoundingBox(
                        Coordinates(swLat, swLong),
                        Coordinates(neLat, neLong)
                    )
                )
            } else {
                w3wVoice.clipToBoundingBox(null)
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
            w3wVoice.clipToPolygon(
                listCoordinates
            )
        }
    }
}