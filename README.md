# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp;w3w-android-components

An Android library to use the [what3words v3 API autosuggest](https://developer.what3words.com/public-api/docs#autosuggest).

<img src="https://github.com/what3words/w3w-android-components/blob/dev/assets/components-1-new.gif" width=40% height=40%>

To obtain an API key, please visit [https://what3words.com/select-plan](https://what3words.com/select-plan) and sign up for an account.

## Installation

The artifact is available through [![Maven Central](https://img.shields.io/maven-central/v/com.what3words/w3w-android-components.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.what3words%22%20AND%20a:%22w3w-android-components%22)

### Android minumum SDK support
[![Generic badge](https://img.shields.io/badge/minSdk-23-green.svg)](https://developer.android.com/about/versions/marshmallow/android-6.0/)

### Gradle

```
implementation 'com.what3words:w3w-android-components:3.1.1'
```

## Documentation

See the what3words public API [documentation](https://docs.what3words.com/api/v3/)

## Usage with Jetpack Compose
Checkout the W3WAutoSuggestEditText Compose port [readme](https://github.com/what3words/w3w-android-components/tree/master/compose-sample#readme) if you intend to use the W3WAutoSuggestComponent in Jetpack Compose. 

## Usage with XML

AndroidManifest.xml
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.yourpackage.yourapp">

    <uses-permission android:name="android.permission.INTERNET" />
    ...
```

add this to build.gradle (app level)
```
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```

add this the following proguard rules
```
-keep class com.what3words.javawrapper.request.* { *; }
-keep class com.what3words.javawrapper.response.* { *; }
```

activity_main.xml
```xml
<androidx.constraintlayout.widget.ConstraintLayout
	  xmlns:android="http://schemas.android.com/apk/res/android"
	  xmlns:app="http://schemas.android.com/apk/res-auto"
	  android:layout_width="match_parent"
	  android:layout_height="match_parent">

	 <com.what3words.components.text.W3WAutoSuggestEditText
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
            .onSuggestionSelected { suggestion ->
                if (suggestion != null) {
                    Log.i( "MainActivity","words: ${suggestion.words}, country: ${suggestion.country}, distance: ${suggestion.distanceToFocusKm}, near: ${suggestion.nearestPlace}, latitude: ${suggestion.coordinates?.lat}, longitude: ${suggestion.coordinates?.lng}")
                } else {
                    Log.i("MainActivity", "invalid w3w address")
                }
            }
            .onError { error ->
                Log.e("MainActivity", "${error.key} - ${error.message}")
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
                .onSuggestionSelected(null, null, (SuggestionWithCoordinates suggestion) -> {
                    if (suggestion != null) {
                        Log.i("MainActivity", String.format("words: %s, country: %s, near: %s", suggestion.getWords(), suggestion.getCountry(), suggestion.getNearestPlace()));
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

## General functions:

| Name | Summary | Example |
|---|---|----|
|apiKey|Set your What3Words API Key which will be used to get suggestions and coordinates. **mandatory** |```apiKey("YOUR_API_KEY_HERE")```|
|returnCoordinates|Calls the what3words API to obtain the coordinates for the selected 3 word address (to then use on a map or pass through to a logistic company etc)|```returnCoordinates(true)```|
|onSuggestionSelected|Will provide the user selected 3 word address, if user selects an invalid 3 word address SuggestionWithCoordinates will be null.|```onSuggestionSelected { suggestion -> }```<br>or for custom picker view<br>```onSuggestionSelected(W3WAutoSuggestPicker) { suggestion -> }```|
|onDisplaySuggestions|Callback to update view when suggestion picker is being displayed or not, example, show tips when false hide tips when true|```onDisplaySuggestions { isShowing -> }```|
|onError|Will provide any errors APIResponse.What3WordsError that might happen during the API call.|```onError { error -> }```<br>or for custom error view<br>```onError(W3WAutoSuggestErrorMessage) { error -> }```|
|focus|This is a location, specified as a latitude/longitude (often where the user making the query is). If specified, the results will be weighted to give preference to those near the focus. |```focus(Coordinates(49.180803, -8.001330))```|
|clipToBoundingBox|Clip results to a bounding box specified using co-ordinates.|```clipToBoundingBox(BoundingBox(Coordinates(49.180803, -8.001330),Coordinates(58.470001, 2.158991)))```|
|clipToCircle|Restrict autosuggest results to a circle, specified by Coordinates representing the centre of the circle, plus the radius in kilometres.|```clipToCircle(Coordinates(49.180803, -8.001330), 100.0)```|
|clipToCountry|Restricts autosuggest to only return results inside the countries specified by comma-separated list of uppercase ISO 3166-1 alpha-2 country codes.|```clipToCountry(listOf("GB", "BE"))```|
|clipToPolygon|Restrict autosuggest results to a polygon, specified by a collection of Coordinates.|```clipToPolygon(listOf(Coordinates(49.180803, -8.001330), ..., Coordinates(49.180803, -8.001330)))```|
|correctionMessage|Set end-user correction picker title, default: "Did you mean?"|```correctionMessage("new title")```|
|customCorrectionPicker|Add custom correction view.|```customCorrectionPicker(W3WAutoSuggestCorrectionPicker)```|
|displayUnit|Set end-user display unit, DisplayUnits.SYSTEM (default), DisplayUnits.METRIC, DisplayUnits.IMPERIAL|```displayUnit(DisplayUnits.METRIC)```|
|errorMessage|Set end-user error message for API related issues, default: An error occurred.|```errorMessage("new message")```|
|invalidSelectionMessage|Set end-user invalid address message for when user selects invalid three word address, default: "No valid what3words address found" ||
|onHomeClick|If DrawableStart is set and it's pressed callback will be called, usage example is to have a back button as drawableStart.|```onHomeClick { }```|
|allowFlexibleDelimiters|Allow EditText to accept different delimiters than the what3words standard full stop "index.home.raft", i.e  "index home raft" or "index,home,raft".| ```allowFlexibleDelimiters(true)``` |
|allowInvalid3wa|By default the EditText field will clear an inputted value if a valid 3 word address is not entered. Setting allowInvalid3wa to true stops this behaviour and the value is persisted in the EditText.|```allowInvalid3wa(true)```|
|preferLand|Makes AutoSuggest prefer results on land to those in the sea. This setting is on by default. Use false to disable this setting and receive more suggestions in the sea.|```preferLand(false)```|

## Enable voice autosuggest:

<img src="https://github.com/what3words/w3w-autosuggest-edittext-android/blob/dev/assets/components-2-new.gif" width=35% height=35%>

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

add following proguard rules
```
-keep class com.what3words.androidwrapper.voice.* { *; } //only needed if using voice functionality.
```

activity_main.xml
```XML
 <com.what3words.components.text.W3WAutoSuggestEditText
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

| Name | Summary | Example |
|---|---|----|
|voiceEnabled|Enable voice for autosuggest component.|```voiceEnabled(true)```<br>or for other animations<br>```voiceEnabled(true, VoiceScreenType.AnimatedPopup)```<br>or for custom microphone icon<br>```voiceEnabled(true, VoiceScreenType.Inline, getDrawable(R.drawable.new_mic))```
|voiceLanguage|Available voice languages: `ar` for Arabic, `cmn` for Mandarin Chinese, `de` for German, `en` Global English (default), `es` for Spanish, `hi` for Hindi, `ja` for Japanese and `ko` for Korean | ```voiceLanguage("de")```
|voicePlaceholder|Voice placeholder for fullscreen popup for autosuggest component, default: "Say a 3 word address…"|```voicePlaceholder("new placeholder")```
|toggleVoice|Will trigger the voice programmatically, in cases where the developer wants to start listening without user touching the screen.|```toggleVoice()```

## Voice only:

If you want to use voice-only (no text input) please look at our **voice-sample** app in this repo for examples of how to use our **W3WAutoSuggestVoice component**.


## Advanced usage:

If you want to check different ways to use our component please look at our **advanced-sample** app in this repo for examples of how to use and customize our **W3WAutoSuggestText component**.

![alt text](https://github.com/what3words/w3w-android-components/blob/master/assets/screen_10.png?raw=true "Screenshot 10")

## Styles:

<img src="https://github.com/what3words/w3w-android-components/blob/dev/assets/components-3-new.gif" width=35% height=35%>

We added support for Night mode on version 3.+, if you want to enable Day/Night mode please add the following style to W3WAutoSuggestEditText.
```XML
 <com.what3words.components.text.W3WAutoSuggestEditText
		  android:id="@+id/suggestionEditText"
		  style="@style/Widget.AppCompat.W3WAutoSuggestEditTextDayNight"					
		  android:layout_width="0dp"
		  android:layout_height="wrap_content"
		  app:layout_constraintEnd_toEndOf="parent"
		  app:layout_constraintStart_toStartOf="parent"
		  app:layout_constraintTop_toTopOf="parent" />
```

You can use our base style as parent (Widget.AppCompat.W3WAutoSuggestEditText or Widget.AppCompat.W3WAutoSuggestEditTextDayNight for day/night support) and you can set the custom properties available with XML on the table above and the normal EditText styling, i.e:

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

![alt text](https://github.com/what3words/w3w-android-components/blob/master/assets/screen_4.png?raw=true "Screenshot 4")![alt text](https://github.com/what3words/w3w-android-components/blob/master/assets/screen_5.png?raw=true "Screenshot 5")![alt text](https://github.com/what3words/w3w-android-components/blob/master/assets/screen_6.png?raw=true "Screenshot 6")
