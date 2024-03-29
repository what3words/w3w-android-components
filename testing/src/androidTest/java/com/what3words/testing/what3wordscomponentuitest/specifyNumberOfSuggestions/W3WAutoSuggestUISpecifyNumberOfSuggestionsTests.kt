package com.what3words.testing.what3wordscomponentuitest.specifyNumberOfSuggestions


import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import com.what3words.testing.what3wordscomponentuitest.utils.hasItemCountGreaterThanZero
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisible


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUISpecifyNumberOfSuggestionsTests {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testTextSearch_plainTextSearchWithFiveSuggestions() {
        val threeWordAddress = "filled.count.soap"
        val numberOfSuggestions = "5"

        Espresso.onView(withId(R.id.main))
            .perform(waitUntilVisible())

        Espresso.onView(withId(R.id.holderSpecifyNumberOfSuggestions))
            .perform(waitUntilVisible(), scrollTo())

        Espresso.onView(withId(R.id.textSpecifyNumberOfSuggestions))
            .perform(
                waitUntilVisible(),
                click(),
                typeTextIntoFocusedView(numberOfSuggestions),
                closeSoftKeyboard()
            )

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(
                waitUntilVisible(),
                scrollTo(),
                click(),
                replaceText(threeWordAddress),
                closeSoftKeyboard()
            )

        Espresso.onView(
            withId(
                com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
            )
        )
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .check(matches(hasChildCount(numberOfSuggestions.toInt())))

    }
}