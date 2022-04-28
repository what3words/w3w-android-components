package com.what3words.testing.what3wordscomponentuitest.errors.invalidapikey


import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.snackBarIsVisible
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisible
import org.junit.Before


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUITest_ClipToPolygon {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUpInvalidKey() {
        activityScenarioRule.scenario.onActivity {
            it.resetW3WApiKey("AUV282345")
        }
    }

    @Test
    fun testTextSearch_invalidApiKeyDisplaysError() {
        val threeWordAddress = "filled.count.soap"
        Espresso.onView(withId(R.id.main))
            .perform(waitUntilVisible())

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click(), typeTextIntoFocusedView(threeWordAddress))

        Espresso.onView(withId(R.id.main))
            .perform(waitUntilVisible(matcher = snackBarIsVisible(), checkForChildren = true))

        Espresso.onView(withText(CoreMatchers.containsString("InvalidKey - Authentication failed")))
            .check(matches(isDisplayed()))

        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(isDisplayed()))
    }


}
