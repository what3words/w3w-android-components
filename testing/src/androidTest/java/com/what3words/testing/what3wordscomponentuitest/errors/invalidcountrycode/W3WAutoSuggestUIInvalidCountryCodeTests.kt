package com.what3words.testing.what3wordscomponentuitest.errors.invalidcountrycode

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import org.hamcrest.CoreMatchers
import com.what3words.testing.waitUntilViewShown
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.snackBarIsVisible
import com.what3words.testing.waitUntil


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUIInvalidCountryCodeTests {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun testTextSearch_InvalidCountryCodeDisplaysError() {
        val threeWordAddress = "advice.itself.mops"
        waitUntilViewShown(
            withId(R.id.main)
        )

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

        Espresso.onView(withChild(withId(R.id.main)))
            .perform(waitUntil(snackBarIsVisible()))

        Espresso.onView(withText(CoreMatchers.containsString("BadClipToCountry")))
            .check(matches(isDisplayed()))



        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text))

            .check(matches(isDisplayed()))
    }


}
