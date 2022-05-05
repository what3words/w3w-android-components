package com.what3words.testing

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import autosuggestsample.util.addOnTextChangedListener
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.what3words.components.models.VoiceScreenType
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.testing.databinding.ActivityMainBinding
import com.what3words.testing.databinding.ActivityMainBinding.inflate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var apiKey = BuildConfig.W3W_API_KEY

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        setUpW3W()

        binding.checkboxCoordinates.setOnCheckedChangeListener { _, b ->
            binding.suggestionEditText.returnCoordinates(b)
        }

        binding.checkboxPreferLand.setOnCheckedChangeListener { _, b ->
            binding.suggestionEditText.preferLand(b)
        }

        binding.checkboxAllowInvalidAddress.setOnCheckedChangeListener { _, b ->
            binding.suggestionEditText.allowInvalid3wa(b)
        }

        binding.checkboxAllowFlexibleDelimiters.setOnCheckedChangeListener { _, b ->
            binding.suggestionEditText.allowFlexibleDelimiters(b)
        }

        binding.checkboxSearchFlowEnabled.setOnCheckedChangeListener { _, b ->
            binding.suggestionEditText.searchFlowEnabled(b)
        }

        binding.checkboxVoiceDisabled.isChecked = true

        binding.checkboxCustomPicker.setOnCheckedChangeListener { _, _ ->
            updateOnSelectedAndOnError()
        }

        binding.checkboxCustomError.setOnCheckedChangeListener { _, _ ->
            updateOnSelectedAndOnError()
        }

        binding.checkboxCustomCorrectionPicker.setOnCheckedChangeListener { _, b ->
            binding.suggestionEditText.customCorrectionPicker(if (b) binding.correctionPicker else null)
        }



        binding.textPlaceholder.setText(R.string.input_hint)
        binding.textPlaceholder.addOnTextChangedListener {
            binding.suggestionEditText.hint = it
        }

        binding.textVoicePlaceholder.setText(R.string.voice_placeholder)
        binding.textVoicePlaceholder.addOnTextChangedListener {
            binding.suggestionEditText.voicePlaceholder(it)
        }

        // how to change fallback text language
        binding.textLanguage.addOnTextChangedListener {
            binding.suggestionEditText.language(it)
        }

        // how to change voicelanguage
        binding.textVoiceLanguage.setText("en")
        binding.textVoiceLanguage.addOnTextChangedListener {
            binding.suggestionEditText.voiceLanguage(it)
        }

        // how to clipToCountry
        binding.textClipToCountry.addOnTextChangedListener { input ->
            val test = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            binding.suggestionEditText.clipToCountry(test)
        }

        binding.closeButton.setOnClickListener {
            binding.suggestionEditText.setText("")
        }

        // how to apply focus
        binding.textFocus.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val lat = latLong.getOrNull(0)?.toDoubleOrNull()
            val long = latLong.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && long != null) {
                binding.suggestionEditText.focus(Coordinates(lat, long))
            } else {
                binding.suggestionEditText.focus(null)
            }
        }

        // how to clipToCircle
        binding.textClipToCircle.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val lat = latLong.getOrNull(0)?.toDoubleOrNull()
            val long = latLong.getOrNull(1)?.toDoubleOrNull()
            val km = latLong.getOrNull(2)?.toDoubleOrNull()
            if (lat != null && long != null) {
                binding.suggestionEditText.clipToCircle(Coordinates(lat, long), km ?: 0.0)
            } else {
                binding.suggestionEditText.clipToCircle(null, null)
            }
        }

        // how to clipToBoundingBox
        binding.textClipToBoundingBox.addOnTextChangedListener { input ->
            val latLong = input.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
            val swLat = latLong.getOrNull(0)?.toDoubleOrNull()
            val swLong = latLong.getOrNull(1)?.toDoubleOrNull()
            val neLat = latLong.getOrNull(2)?.toDoubleOrNull()
            val neLong = latLong.getOrNull(3)?.toDoubleOrNull()
            if (swLat != null && swLong != null && neLat != null && neLong != null) {
                binding.suggestionEditText.clipToBoundingBox(
                    BoundingBox(
                        Coordinates(swLat, swLong),
                        Coordinates(neLat, neLong)
                    )
                )
            } else {
                binding.suggestionEditText.clipToBoundingBox(null)
            }
        }

        // how to clipToPolygon
        binding.textClipToPolygon.addOnTextChangedListener { input ->
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
            binding.suggestionEditText.clipToPolygon(
                listCoordinates
            )
        }

        // how to specify number of suggestions
        binding.textSpecifyNumberOfSuggestions.addOnTextChangedListener { input ->
            val suggestionsCount = input.replace("\\s".toRegex(), "")
            val isValidInput = suggestionsCount.all { Character.isDigit(it) }

            if (isValidInput) {
                binding.suggestionEditText.nResults(suggestionsCount.toInt())
            }
        }
        setContentView(binding.root)
    }

    fun resetW3WApiKey(key: String) {
        apiKey = key
        setUpW3W()
    }

    private fun setUpW3W() {
        binding.suggestionEditText.apiKey(
            key = apiKey,
            endpoint = "https://api.london.preprod.w3w.io/v3/"
        )
            .onDisplaySuggestions {
            }
            .onSuggestionSelected {
                if (it != null) {
                    binding.selectedInfo.text = resources.getString(
                        R.string.suggestion_info_placeholder,
                        it.words,
                        it.country,
                        it.nearestPlace,
                        if (it.distanceToFocusKm == null) "N/A" else "${it.distanceToFocusKm}km",
                        it.coordinates?.lat.toString(),
                        it.coordinates?.lng.toString()
                    )
                } else {
                    binding.selectedInfo.text = ""
                }
            }
            .onError {
                Log.e("MainActivity", "${it.key} - ${it.message}")
                Snackbar.make(
                    binding.main, "${it.key} - ${it.message}",
                    BaseTransientBottomBar.LENGTH_INDEFINITE
                ).apply {
                    setAction("OK") { dismiss() }
                    show()
                }
            }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        startActivity(Intent.makeRestartActivityTask(this.intent?.component))
    }

    // example of how to use a custom suggestion picker and custom error message on your view instead of using the default provided below the W3WAutoSuggestEditText
    private fun updateOnSelectedAndOnError() {
        binding.suggestionEditText.onSuggestionSelected(
            if (binding.checkboxCustomPicker.isChecked) binding.suggestionPicker else null,
            if (binding.checkboxCustomError.isChecked) binding.suggestionError else null
        ) {
            if (it != null) {
                binding.selectedInfo.text = resources.getString(
                    R.string.suggestion_info_placeholder,
                    it.words,
                    it.country,
                    it.nearestPlace,
                    if (it.distanceToFocusKm == null) "N/A" else "${it.distanceToFocusKm}km",
                    it.coordinates?.lat.toString(),
                    it.coordinates?.lng.toString()
                )
            } else {
                binding.selectedInfo.text = ""
            }
        }.onError(if (binding.checkboxCustomError.isChecked) binding.suggestionError else null) {
            Log.e("MainActivity", "${it.key} - ${it.message}")
            Snackbar.make(
                binding.main, "${it.key} - ${it.message}",
                BaseTransientBottomBar.LENGTH_INDEFINITE
            ).apply {
                setAction("OK") { dismiss() }
                show()
            }
        }
    }

    fun onRadioButtonClicked(view: View) {
        // Is the button now checked?

        if (view !is RadioButton) {
            return
        }

        binding.suggestionEditText.voiceEnabled(false, VoiceScreenType.Inline)
        // Check which radio button was clicked
        when (view.id) {
            R.id.checkboxVoiceEnabled ->
                binding.suggestionEditText.voiceEnabled(true, VoiceScreenType.Inline)
            R.id.checkboxVoicePopup ->
                binding.suggestionEditText.voiceEnabled(true, VoiceScreenType.AnimatedPopup)
            R.id.checkboxVoiceFullscreen ->
                binding.suggestionEditText.voiceEnabled(true, VoiceScreenType.Fullscreen)
        }
    }
}