package com.what3words.autosuggestsample.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
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
        suggestionEditText.apiKey("TCRPZKEE")
            .onSelected {
                if (it != null) {
                    selectedInfo.text =
                        "words: ${it.words}\ncountry: ${it.country}\nnear: ${it.nearestPlace}\ndistance: ${if (it.distanceToFocusKm == null) "N/A" else it.distanceToFocusKm.toString() + "km"}\nlatitude: ${it.coordinates?.lat}\nlongitude: ${it.coordinates?.lng}"
                } else {
                    selectedInfo.text = ""
                }
            }.onError {
                Log.e("MainActivity", "${it.key} - ${it.message}")
                Snackbar.make(main, "${it.key} - ${it.message}", LENGTH_INDEFINITE).apply {
                    setAction("OK") { dismiss() }
                    show()
                }
            }

        checkboxCoordinates.setOnCheckedChangeListener { _, b ->
            suggestionEditText.returnCoordinates(b)
        }

        checkboxAllowInvalidAddress.setOnCheckedChangeListener { _, b ->
            suggestionEditText.allowInvalid3wa(b)
        }

        checkboxVoiceFullscreen.setOnCheckedChangeListener { _, b ->
            suggestionEditText.voiceFullscreen(b)
        }

        checkboxVoiceEnabled.setOnCheckedChangeListener { _, b ->
            suggestionEditText.voiceEnabled(b)
        }

        checkboxCustomPicker.setOnCheckedChangeListener { _, _ ->
            updateOnSelectedAndOnError()
        }

        checkboxCustomError.setOnCheckedChangeListener { _, _ ->
            updateOnSelectedAndOnError()
        }

        checkboxCustomCorrectionPicker.setOnCheckedChangeListener { _, b ->
            suggestionEditText.customCorrectionPicker(if (b) correctionPicker else null)
        }

        textPlaceholder.setText(R.string.input_hint)
        textPlaceholder.addOnTextChangedListener {
            suggestionEditText.hint = it
        }

        textVoicePlaceholder.setText(R.string.voice_placeholder)
        textVoicePlaceholder.addOnTextChangedListener {
            suggestionEditText.voicePlaceholder(it)
        }

        //how to change fallback text language
        textLanguage.addOnTextChangedListener {
            suggestionEditText.language(it)
        }

        //how to change voicelanguage
        textVoiceLanguage.setText("en")
        textVoiceLanguage.addOnTextChangedListener {
            suggestionEditText.voiceLanguage(it)
        }

        //how to clipToCountry
        textClipToCountry.addOnTextChangedListener { input ->
            val test = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            suggestionEditText.clipToCountry(test)
        }

        //how to apply focus
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

        //how to clipToCircle
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

        //how to clipToBoundingBox
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

        //how to clipToPolygon
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

    //example of how to use a custom suggestion picker and custom error message on your view instead of using the default provided below the W3WAutoSuggestEditText
    private fun updateOnSelectedAndOnError() {
        suggestionEditText.onSelected(
            if (checkboxCustomPicker.isChecked) suggestionPicker else null,
            if (checkboxCustomError.isChecked) suggestionError else null
        ) {
            if (it != null) {
                selectedInfo.text =
                    "words: ${it.words}\ncountry: ${it.country}\nnear: ${it.nearestPlace}\ndistance: ${if (it.distanceToFocusKm == null) "N/A" else it.distanceToFocusKm.toString() + "km"}\nlatitude: ${it.coordinates?.lat}\nlongitude: ${it.coordinates?.lng}"
            } else {
                selectedInfo.text = ""
            }
        }.onError(if (checkboxCustomError.isChecked) suggestionError else null) {
            Log.e("MainActivity", "${it.key} - ${it.message}")
            Snackbar.make(main, "${it.key} - ${it.message}", LENGTH_INDEFINITE).apply {
                setAction("OK") { dismiss() }
                show()
            }
        }
    }
}