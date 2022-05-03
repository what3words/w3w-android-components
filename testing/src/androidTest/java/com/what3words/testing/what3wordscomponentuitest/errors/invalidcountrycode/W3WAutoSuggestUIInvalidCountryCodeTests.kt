package com.what3words.testing.what3wordscomponentuitest.errors.invalidcountrycode


import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.snackbar.Snackbar
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisible
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisibleInParent


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUIInvalidCountryCodeTests {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun testTextSearch_InvalidCountryCodeDisplaysError() {
        val threeWordAddress = "advice.itself.mops"
        Espresso.onView(withId(R.id.main))
            .perform(waitUntilVisible())

        Espresso.onView(withId(R.id.textClipToCountry))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(typeTextIntoFocusedView("GBR"))

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(typeTextIntoFocusedView(threeWordAddress))

        Espresso.onView(withId(R.id.main))
            .perform(waitUntilVisibleInParent<Snackbar.SnackbarLayout>())

        Espresso.onView(withText(CoreMatchers.containsString("BadClipToCountry")))
            .check(matches(isDisplayed()))



        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(isDisplayed()))
    }
}
