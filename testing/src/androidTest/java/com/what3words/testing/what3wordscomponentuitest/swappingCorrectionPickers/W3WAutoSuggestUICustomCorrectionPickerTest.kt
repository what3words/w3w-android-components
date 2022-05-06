package com.what3words.testing.what3wordscomponentuitest.swappingCorrectionPickers


import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.components.picker.W3WAutoSuggestCorrectionPicker
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisibleInParent
import org.hamcrest.CoreMatchers.not


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUICustomCorrectionPickerTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSwappingDefaultCorrectionPickerWithCustomCorrectionPicker() {
        val spaceSeparatedThreeWordsAddress = "index home raft"

        // turn on the custom correction picker
        Espresso.onView(withId(R.id.checkboxCustomCorrectionPicker))
            .perform(scrollTo(), click())

        // type into the auto-suggest edit text
        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(
                scrollTo(),
                click(),
                typeTextIntoFocusedView(spaceSeparatedThreeWordsAddress)
            )

        // wait and check that the custom correction picker is visible
        Espresso.onView(withId(R.id.correctionPicker))
            .perform(
                waitUntilVisibleInParent<W3WAutoSuggestCorrectionPicker>()
            ).check(matches(isDisplayed()))

        // turn off the custom correction picker
        Espresso.onView(withId(R.id.checkboxCustomCorrectionPicker))
            .perform(scrollTo(), click())

        // clear text from auto-suggest edit text
        Espresso.onView(withId(R.id.btnClear))
            .perform(scrollTo(), click())

        // type into the auto-suggest edit text
        Espresso.onView(withId(R.id.suggestionEditText))
            .check(matches(isDisplayed()))
            .perform(click(), typeTextIntoFocusedView(spaceSeparatedThreeWordsAddress))

        // wait and check that the default correction picker is visible
        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultCorrectionPicker))
            .perform(
                waitUntilVisibleInParent<W3WAutoSuggestCorrectionPicker>()
            ).check(matches(isDisplayed()))

        // check that the custom correction picker is not visible
        Espresso.onView(withId(R.id.correctionPicker))
            .check(matches(not(isDisplayed())))
    }


}