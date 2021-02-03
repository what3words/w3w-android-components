package com.what3words.sample_voice

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.what3words.autosuggestsample.util.addOnTextChangedListener
import com.what3words.components.utils.W3WSuggestion
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import kotlinx.android.synthetic.main.activity_voice.*

class VoiceActivity : AppCompatActivity() {

    private fun showSuggestion(selected: W3WSuggestion?) {
        if (selected != null) {
            selectedInfo.text =
                "words: ${selected.suggestion.words}\ncountry: ${selected.suggestion.country}\nnear: ${selected.suggestion.nearestPlace}\ndistance: ${if (selected.suggestion.distanceToFocusKm == null) "N/A" else selected.suggestion.distanceToFocusKm.toString() + "km"}\nlatitude: ${selected.coordinates?.lat}\nlongitude: ${selected.coordinates?.lng}"
        } else {
            selectedInfo.text = ""
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice)

        w3wVoice.apiKey("YOUR_WHAT3WORDS_API_KEY_HERE")
            .onResults(w3wPicker) { selected ->
                showSuggestion(selected)
            }.onError {
                Log.e("MainActivity", "${it.key} - ${it.message}")
                Snackbar.make(main, "${it.key} - ${it.message}", LENGTH_INDEFINITE).apply {
                    setAction("OK") { dismiss() }
                    show()
                }
            }.onListening {
                Log.i("MainActivity", if (it) "listening" else "stopped")
            }

        checkboxCoordinates.setOnCheckedChangeListener { _, b ->
            w3wVoice.returnCoordinates(b)
        }

        checkboxPicker.setOnCheckedChangeListener { _, b ->
            if (b) {
                w3wVoice.onResults(w3wPicker) {
                    showSuggestion(it)
                }
            } else {
                w3wVoice.onResults { suggestionsList ->
                    //create/populate your own recyclerview or pick the top ranked suggestion
                    showSuggestion(suggestionsList.minByOrNull { it.suggestion.rank })
                }
            }
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