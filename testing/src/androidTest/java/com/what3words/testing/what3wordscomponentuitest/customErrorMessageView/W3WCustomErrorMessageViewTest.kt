package com.what3words.testing.what3wordscomponentuitest.customErrorMessageView

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import com.what3words.testing.what3wordscomponentuitest.utils.hasItemCountGreaterThanZero
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilGone
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisible
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class W3WCustomErrorMessageViewTest {
    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun testCustomErrorMessageViewIsShown() {
        val threeWordAddress = "index.home.r"

        // type index.home.r into auto suggest edit text
        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(click(), typeTextIntoFocusedView(threeWordAddress))


        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultPicker))
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))

        // give focus to the clip to country edit text
        Espresso.onView(withId(R.id.textClipToCountry))
            .perform(scrollTo(), click())

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())

        // check that the default error message view is shown for just 5 seconds
        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultErrorMessage))
            .perform(waitUntilGone(delay = 5000))

        Espresso.onView(withId(R.id.checkboxCustomError))
            .perform(scrollTo(), click())

        // type index.home.r into auto suggest edit text
        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo(), click(), typeTextIntoFocusedView(threeWordAddress))


        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultPicker))
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))

        // give focus to the clip to country edit text
        Espresso.onView(withId(R.id.textClipToCountry))
            .perform(scrollTo(), click())

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())

        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultErrorMessage))
            .check(matches(not(isDisplayed())))

        // check that the custom error message view is shown for just 5 seconds
        Espresso.onView(withId(R.id.suggestionError))
            .perform(scrollTo(), waitUntilGone(delay = 5000))
    }

}