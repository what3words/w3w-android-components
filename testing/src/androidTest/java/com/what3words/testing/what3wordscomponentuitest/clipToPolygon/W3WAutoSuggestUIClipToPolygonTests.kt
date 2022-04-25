package com.what3words.testing.what3wordscomponentuitest.clipToPolygon

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
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
import com.what3words.testing.waitUntilViewShown
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.hasItemCountGreaterThanZero
import com.what3words.testing.waitUntil
import org.hamcrest.CoreMatchers.not


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUIClipToPolygonTests {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    private val polygon = "51.598583, -0.040604, 51.600691, " +
            "-0.01670,51.600143, -0.007478 ,51.581851, 0.000835 ," +
            "51.581851, 0.000836 ,51.581851, 0.000837 ,51.581851, 0.000838 ," +
            "51.581851, 0.000839 ,51.575373, -0.012646 ,51.570550, -0.025094 ," +
            "51.587274, -0.040544 ,51.598583, -0.040691 ,51.598583, -0.040592 ," +
            "51.598583, -0.040593 ,51.598583, -0.040594 ,51.598583, -0.040595 ," +
            "51.598583, -0.040596 ,51.598583, -0.040597 ,51.598583, -0.040598 ," +
            "51.598583, -0.040599 ,51.598583, -0.040600 ,51.598583, -0.040601 ," +
            "51.598583, -0.040602 ,51.598583, -0.040603 ,51.598583, -0.040604"

    @Test
    fun testTextSearch_clipToPolygonContainAddressInsidePolygon() {
        val threeWordAddress = "advice.itself.mops"
        waitUntilViewShown(
            withId(R.id.main)
        )
        Espresso.onView(withId(R.id.textClipToPolygon))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(typeTextIntoFocusedView(polygon))


        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(typeTextIntoFocusedView(threeWordAddress))


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
    fun testTextSearch_clipToPolygonDoesNotContainAddressOutsidePolygon() {
        val searchAddress = "advice.itself.mops"
        val notContainedAddress = "decent.chains.pages"

        waitUntilViewShown(
            withId(R.id.main)
        )
        Espresso.onView(withId(R.id.textClipToPolygon))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(typeTextIntoFocusedView(polygon))

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(typeTextIntoFocusedView(searchAddress))


        Espresso.onView(
            withId(
                com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
            )
        )
            .perform(waitUntil(hasItemCountGreaterThanZero()))

        val itemCount = getSuggestionCount()
        for (i in 0 until itemCount) {
            Espresso.onView(withId(R.id.suggestionEditText))
                .perform(click())
                .perform(replaceText((searchAddress)))

            Espresso.onView(
                withId(
                    com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
                )
            )
                .perform(waitUntil(hasItemCountGreaterThanZero()))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(i))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0,
                        click()
                    )
                )

            Espresso.onView(withId(R.id.selectedInfo))
                .check(matches(withText(not(containsString(notContainedAddress)))))

            Espresso.onView(withId(R.id.suggestionEditText))
                .perform(click())
                .perform(clearText())
        }

    }

    private fun getSuggestionCount(): Int {
        var itemCount = 0
        activityScenarioRule.scenario.onActivity {
            val suggestionPicker = it.findViewById<RecyclerView>(R.id.w3wAutoSuggestDefaultPicker)
            itemCount = suggestionPicker.adapter?.itemCount ?: itemCount
        }
        return itemCount
    }
}
