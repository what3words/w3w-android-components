




# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp;w3w-autosuggest-edittext-android

An Android library to use the [what3words v3 API autosuggest](https://developer.what3words.com/public-api/docs#autosuggest).

![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/master/assets/screen_1.png?raw=true "Screenshot 1")![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/master/assets/screen_2.png?raw=true "Screenshot 2")![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/master/assets/screen_3.png?raw=true "Screenshot 3")

To obtain an API key, please visit [https://what3words.com/select-plan](https://what3words.com/select-plan) and sign up for an account.

## Installation

The artifact is available through [![Maven Central](https://img.shields.io/maven-central/v/com.what3words/w3w-autosuggest-edittext-android.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.what3words%22%20AND%20a:%22w3w-autosuggest-edittext-android%22)

### Gradle

```
implementation 'com.what3words:w3w-autosuggest-edittext-android:1.1.0'
```

## Documentation

See the what3words public API [documentation](https://docs.what3words.com/api/v3/)

## Usage

AndroidManifest.xml
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.yourpackage.yourapp">

    <uses-permission android:name="android.permission.INTERNET" />
    ...
```

if **minSdkVersion < 24** add this to build.gradle (app level)
```
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```

activity_main.xml
```xml
<androidx.constraintlayout.widget.ConstraintLayout
	  xmlns:android="http://schemas.android.com/apk/res/android"
	  xmlns:app="http://schemas.android.com/apk/res-auto"
	  android:layout_width="match_parent"
	  android:layout_height="match_parent">

	 <com.what3words.autosuggest.W3WAutoSuggestEditText
		  android:id="@+id/suggestionEditText"
		  android:layout_width="0dp"
		  android:layout_height="wrap_content"
		  app:layout_constraintEnd_toEndOf="parent"
		  app:layout_constraintStart_toStartOf="parent"
		  app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```
Kotlin
```Kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

	suggestionEditText.apiKey("YOUR_API_KEY_HERE")
            .returnCoordinates(false)
            .onSelected { w3wSuggestion ->
                if (w3wSuggestion != null) {
                    Log.i( "MainActivity","words: ${w3wSuggestion.suggestion.words}, country: ${w3wSuggestion.suggestion.country}, distance: ${w3wSuggestion.suggestion.distanceToFocusKm}, near: ${w3wSuggestion.suggestion.nearestPlace}, latitude: ${w3wSuggestion.coordinates?.lat}, longitude: ${w3wSuggestion.coordinates?.lng}"
                    )
                } else {
                    Log.i("MainActivity", "invalid w3w address")
                }
            }
            .onError {
                Log.e("MainActivity", "${it.key} - ${it.message}")
            }
}
```

Java
```Java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        W3WAutoSuggestEditText autoSuggestEditText = findViewById(R.id.suggestionEditText);
        autoSuggestEditText.apiKey("YOUR_API_KEY_HERE")
                .returnCoordinates(false)
                .onSelected(null, null, (W3WSuggestion w3wSuggestion) -> {
                    if (w3wSuggestion != null) {
                        Log.i("MainActivity", String.format("words: %s, country: %s, near: %s", w3wSuggestion.getSuggestion().getWords(), w3wSuggestion.getSuggestion().getCountry(), w3wSuggestion.getSuggestion().getNearestPlace()));
                    } else {
                        Log.i("MainActivity", "invalid w3w address");
                    }
                })
                .onError(null, (APIResponse.What3WordsError what3WordsError) -> Log.e("MainActivity", String.format("%s - %s", what3WordsError.getKey(), what3WordsError.getMessage())));
    }

```

If you run our Enterprise Suite API Server yourself, you may specify the URL to your own server like so:

```Kotlin
 suggestionEditText.apiKey("YOUR_API_KEY_HERE", "https://api.yourserver.com")
```

## General properties:

| property | default value | type | description | XML | Programatically |
|--|--|--|--|--|--|
| apiKey| *N/A* | String | Your what3words API key. **mandatory** |  | :heavy_check_mark:
| hint | *e.g. lock.spout.radar* | String | Placeholder text to display in the input in its default empty state. | :heavy_check_mark:
| errorMessage | *No valid what3words address found* | String | Overwrite the validation error message with a custom value. | :heavy_check_mark: | :heavy_check_mark:
| focus | *N/A* | Coordinates | This is a location, specified as a latitude/longitude (often where the user making the query is). If specified, the results will be weighted to give preference to those near the <code>focus</code> || :heavy_check_mark:
| clipToCountry | *N/A* | String | Clip results to a given country or comma separated list of countries. Example value:"GB,US". || :heavy_check_mark:
| clipToCircle | *N/A* | Coordinates, Int | Clip results to a circle, specified by Coordinate(lat,lng) and kilometres, where kilometres in the radius of the circle. || :heavy_check_mark:
| clipToBoundingBox | *N/A* | BoundingBox | Clip results to a bounding box specified using co-ordinates. || :heavy_check_mark:
| clipToPolygon | *N/A* | List of Coordinates | Clip results to a bounding box specified using co-ordinates. || :heavy_check_mark:
| returnCoordinates | *false* | Boolean | Calls the what3words API to obtain the coordinates for the selected 3 word address (to then use on a map or pass through to a logistic company etc) |:heavy_check_mark:| :heavy_check_mark:
| imageTintColor | *#E11F26* | Color | Changes /// image colour. |:heavy_check_mark:|
| suggestionsListPosition | *BELOW* | Enum | Suggestion list position which can be `below`  (default) the EditText or `above` |:heavy_check_mark:|:heavy_check_mark:|


## Enable voice autosuggest:

![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/add-voice/assets/screen_7.png?raw=true "Screenshot 7")![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/add-voice/assets/screen_8.png?raw=true "Screenshot 8")![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/add-voice/assets/screen_9.png?raw=true "Screenshot 9")

The component also allows for voice input using the what3words Voice API. This feature allows the user to say 3 words and using speech recognition technology displays 3 word address suggestions to the user.

Before enabling Voice AutoSuggest you will need to add a Voice API plan in [your account](https://accounts.what3words.com/billing).

By default the voice language is set to English but this can be changed using the voiceLanguage property (for list of available languages please check the properties table below).
Voice input respects the clipping and focus options applied within the general properties. We recommend applying clipping and focus where possible to display as accurate suggestions as possible.
To enable voice you can do with programmatically or directly in the XML.

AndroidManifest.xml
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.yourpackage.yourapp">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    ...
```

activity_main.xml
```XML
 <com.what3words.autosuggest.W3WAutoSuggestEditText
		  android:id="@+id/suggestionEditText"
		  android:layout_width="0dp"
		  android:layout_height="wrap_content"
		  app:layout_constraintEnd_toEndOf="parent"
		  app:layout_constraintStart_toStartOf="parent"
		  app:layout_constraintTop_toTopOf="parent"
		  app:voiceEnabled="true" />
```
or
```Kotlin
 suggestionEditText.apiKey("YOUR_API_KEY_HERE")
	        .returnCoordinates(false)
	        .voiceEnabled(true)
		...
```

## Voice properties:

| property | default value | type | description | XML | Programatically |
|--|--|--|--|--|--|
| voiceEnabled | false | Boolean | Enables voice suggestion to allow the user to say the three word address instead of writing it. | :heavy_check_mark: | :heavy_check_mark:
| voiceFullscreen | false | Boolean | Voice activation will be fullscreen instead of inline. | :heavy_check_mark: | :heavy_check_mark:
| voiceLanguage | *en* | String | Available voice languages: `ar` for Arabic, `cmn` for Mandarin Chinese, `de` for German, `en` Global English (default), `es` for Spanish, `hi` for Hindi, `ja` for Japanese and `ko` for Korean| :heavy_check_mark: | :heavy_check_mark:

## Voice only:

If you want to use voice-only (no text input) please look at our **voice-sample** app in this repo for examples of how to use our **W3WAutoSuggestVoice component**.

## Styles:

Use our base style as parent and you can set the custom properties available with XML on the table above and the normal EditText styling, i.e:

```xml
<style name="YourCustomStyle" parent="Widget.AppCompat.W3WAutoSuggestEditText">
	<item name="android:textColor">#000000</item>  
	<item name="android:textColorHint">#888888</item>  
	<item name="errorMessage">Your custom error message</item>  
	<item name="android:hint">Your custom placeholder</item>
    <item name="android:textAppearance">@style/YourCustomStyleTextAppearance</item>  
</style>  
  
<style name="YourCustomStyleTextAppearance" parent="TextAppearance.AppCompat">  
	 <item name="android:textSize">22sp</item>  
	 <item name="android:fontFamily">sans-serif-medium</item>
</style>
```

![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/master/assets/screen_4.png?raw=true "Screenshot 4")![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/master/assets/screen_5.png?raw=true "Screenshot 5")![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/master/assets/screen_6.png?raw=true "Screenshot 6")

## Advanced usage

Check our advanced-sample app in this repo to help you implement our component in a different use case.

![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/master/assets/screen_10.png?raw=true "Screenshot 10")

## Full documentation:

### Existing components:

| Name | Summary |
|---|---|
| [W3WAutoSuggestEditText](documentation/com.what3words.autosuggest.text/-w3-w-auto-suggest-edit-text/index.md) | `class W3WAutoSuggestEditText : AppCompatEditText`<br>A [AppCompatEditText](https://developer.android.com/reference/androidx/appcompat/widget/AppCompatEditText) to simplify the integration of what3words text and voice auto-suggest API in your app. |
| [W3WAutoSuggestVoice](documentation/com.what3words.autosuggest.voice/-w3-w-auto-suggest-voice/index.md) | `class W3WAutoSuggestVoice : ConstraintLayout`<br>A [View](https://developer.android.com/reference/android/view/View.html) to simplify the integration of what3words voice auto-suggest API in your app. |
| [W3WAutoSuggestErrorMessage](documentation/com.what3words.autosuggest.error/-w3-w-auto-suggest-error-message/index.md) | `class W3WAutoSuggestErrorMessage : AppCompatTextView`<br>A [AppCompatTextView](https://developer.android.com/reference/androidx/appcompat/widget/AppCompatTextView) styled and ready to show error messages. |
| [W3WAutoSuggestPicker](documentation/com.what3words.autosuggest.picker/-w3-w-auto-suggest-picker/index.md) | `class W3WAutoSuggestPicker : RecyclerView`<br>A [RecyclerView](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView) to show [W3WSuggestion](documentation/com.what3words.autosuggest.utils/-w3-w-suggestion/index.md) returned by w3w auto suggest component modularized to allow developers to choose picker location on the screen. |
