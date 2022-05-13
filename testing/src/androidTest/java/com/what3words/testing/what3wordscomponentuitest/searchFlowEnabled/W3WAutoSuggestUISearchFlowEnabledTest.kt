package com.what3words.testing.what3wordscomponentuitest.searchFlowEnabled


import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import com.what3words.testing.what3wordscomponentuitest.utils.hasItemCountGreaterThanZero
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisible
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUISearchFlowEnabledTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSearchFlowEnabled() {
        val threeWordAddress = "filled.count.soa"

        Espresso.onView(withId(R.id.main))
            .perform(waitUntilVisible())

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(
                waitUntilVisible(),
                click(), typeTextIntoFocusedView(threeWordAddress),
                closeSoftKeyboard()
            )

        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultPicker))
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .check(matches(isDisplayed()))

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(pressImeActionButton())

        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultPicker))
            .check(matches(not(isDisplayed())))

        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultErrorMessage))
            .perform(waitUntilVisible())

        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultErrorMessage))
            .perform(waitUntilVisible())

        Espresso.onView(withId(R.id.checkboxSearchFlowEnabled))
            .perform(scrollTo(), click())

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo(), click(), replaceText(threeWordAddress), closeSoftKeyboard())

        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultPicker))
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .check(matches(isDisplayed()))

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(pressImeActionButton())

        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultPicker))
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .check(matches(isDisplayed()))

//        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultErrorMessage))
//            .check(matches(not(isDisplayed())))

        Espresso.onView(
            withId(
                com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
            )
        )
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

        Espresso.onView(withId(R.id.selectedInfo))
            .check(matches(withText(containsString("filled.count.soap"))))
    }
}