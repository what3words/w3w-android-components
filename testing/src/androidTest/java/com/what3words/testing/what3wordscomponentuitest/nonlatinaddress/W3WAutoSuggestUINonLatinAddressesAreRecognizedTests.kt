package com.what3words.testing.what3wordscomponentuitest.nonlatinaddress

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import com.what3words.testing.waitUntilViewShown
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.hasItemCountGreaterThanZero
import com.what3words.testing.waitUntil


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUINonLatinAddressesAreRecognizedTests {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testTextSearch_plainTextSearchWithArabicAddress() {
        waitUntilViewShown(
            withId(R.id.main)
        )

        val threeWordAddress = "القرفة.العامل.أسماك"
        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(replaceText(threeWordAddress))

        Espresso.onView(
            withId(
                com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
            )
        )
            .perform(waitUntil(hasItemCountGreaterThanZero()))
            .perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        Espresso.onView(withId(R.id.selectedInfo))
            .check(matches(withText(containsString(threeWordAddress))))
    }

    @Test
    fun testTextSearch_plainTextSearchWithRussianAddress() {
        waitUntilViewShown(
            withId(R.id.main)
        )

        val threeWordAddress = "обилие.городовой.весенний"
        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(replaceText(threeWordAddress))

        Espresso.onView(
            withId(
                com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
            )
        )
            .perform(waitUntil(hasItemCountGreaterThanZero()))
            .perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        Espresso.onView(withId(R.id.selectedInfo))
            .check(matches(withText(containsString(threeWordAddress))))
    }

    @Test
    fun testTextSearch_plainTextSearchWithNonLatinAddress() {
        waitUntilViewShown(
            withId(R.id.main)
        )

        val threeWordAddress = "postverwaltung.postverwaltung.postverwaltung"
        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(replaceText(threeWordAddress))


        Espresso.onView(
            withId(
                com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
            )
        )
            .perform(waitUntil(hasItemCountGreaterThanZero()))
            .perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        Espresso.onView(withId(R.id.selectedInfo))
            .check(matches(withText(containsString(threeWordAddress))))
    }
}