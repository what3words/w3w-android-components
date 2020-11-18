




# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp;w3w-autosuggest-edittext-android

An Android library to use the [what3words v3 API autosuggest](https://developer.what3words.com/public-api/docs#autosuggest).

![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/master/assets/screen_1.png?raw=true "Screenshot 1")![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/master/assets/screen_2.png?raw=true "Screenshot 2")![alt text](https://github.com/what3words/w3w-autosuggest-edittext-android/blob/master/assets/screen_3.png?raw=true "Screenshot 3")

To obtain an API key, please visit [https://what3words.com/select-plan](https://what3words.com/select-plan) and sign up for an account.

## Installation

The artifact is available through <a href="https://search.maven.org/search?q=g:com.what3words">Maven Central</a>.

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
            .onSelected { suggestion, latitude, longitude ->
                if (suggestion != null) {
                    Log.i("MainActivity","words: ${suggestion.words}, country: ${suggestion.country}, near: ${suggestion.nearestPlace}, latitude: $latitude, longitude: $longitude")
                } else {
                    Log.i("MainActivity","invalid w3w address")
                }
            }
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
                .onSelected((suggestion, latitude, longitude) -> {
                    if (suggestion != null) {
                        Log.i("MainActivity", String.format("words: %s, country: %s, near: %s, latitude: %s, longitude: %s", suggestion.getWords(), suggestion.getCountry(), suggestion.getNearestPlace(), latitude, longitude));
                    } else {
                        Log.i("MainActivity", "invalid w3w address");
                    }
                    return Unit.INSTANCE;
                });
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

You can able voice autosuggest to allow user to say the 3 word address and then suggestions will be displayed using our speech recognition algorithm. By default the voice language is set to English but you can change it via **voiceLanguage** property (for list of available languages please check the proprieties table below).  To enable voice you can do with programmatically or directly on the XML.

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
            .onSelected { suggestion, latitude, longitude ->
                if (suggestion != null) {
                    Log.i("MainActivity","words: ${suggestion.words}, country: ${suggestion.country}, near: ${suggestion.nearestPlace}, latitude: $latitude, longitude: $longitude")
                } else {
                    Log.i("MainActivity","invalid w3w address")
                }
            }
        }
```

## Voice properties:

| property | default value | type | description | XML | Programatically |
|--|--|--|--|--|--|
| voiceEnabled | false | Boolean | Enables voice suggestion to allow the user to say the three word address instead of writing it. | :heavy_check_mark: | :heavy_check_mark:
| voiceFullscreen | false | Boolean | Voice activation will be fullscreen instead of inline. | :heavy_check_mark: | :heavy_check_mark:
| voiceLanguage | *en* | String | Available voice languages: `ar` for Arabic, `cmn` for Mandarin Chinese, `de` for German, `en` Global English (default), `es` for Spanish, `hi` for Hindi, `ja` for Japanese and `ko` for Korean| :heavy_check_mark: | :heavy_check_mark:


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
