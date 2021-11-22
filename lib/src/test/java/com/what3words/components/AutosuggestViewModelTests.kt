package com.what3words.components

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.what3words.components.models.AutosuggestApiManager
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.components.models.AutosuggestViewModel
import com.what3words.components.models.AutosuggestWithDidyouMean
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
class AutosuggestViewModelTests {
    @MockK
    private lateinit var observerError: Observer<APIResponse.What3WordsError?>

    @MockK
    private lateinit var manager: AutosuggestApiManager

    @MockK
    private lateinit var observerSuggestions: Observer<List<Suggestion>>

    @MockK
    private lateinit var observerDidYouMean: Observer<Suggestion?>

    @MockK
    private lateinit var observerSelected: Observer<SuggestionWithCoordinates>

    @MockK
    private lateinit var observerMultiple: Observer<List<SuggestionWithCoordinates>>

    @MockK
    private lateinit var viewModel: AutosuggestViewModel

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        manager = mockk()
        observerSuggestions = mockk()
        observerError = mockk()
        observerSelected = mockk()
        observerDidYouMean = mockk()
        observerMultiple = mockk()
        viewModel = AutosuggestViewModel(coroutinesTestRule.testDispatcherProvider)
        viewModel.manager = manager

        justRun {
            observerError.onChanged(any())
            observerDidYouMean.onChanged(any())
            observerSuggestions.onChanged(any())
            observerSelected.onChanged(any())
            observerMultiple.onChanged(any())
        }

        viewModel.suggestions.observeForever(observerSuggestions)
        viewModel.error.observeForever(observerError)
        viewModel.didYouMean.observeForever(observerDidYouMean)
        viewModel.selectedSuggestion.observeForever(observerSelected)
        viewModel.multipleSelectedSuggestions.observeForever(observerMultiple)
    }

    @After
    fun tearDown() {
        viewModel.suggestions.removeObserver(observerSuggestions)
        viewModel.error.removeObserver(observerError)
        viewModel.didYouMean.removeObserver(observerDidYouMean)
        viewModel.selectedSuggestion.removeObserver(observerSelected)
        viewModel.multipleSelectedSuggestions.removeObserver(observerMultiple)
    }

    @Test
    fun `autosuggest returns suggestions and livedata is populated correctly`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                AutosuggestLogicManager.Result(AutosuggestWithDidyouMean(suggestions, null))
            }

            viewModel.autosuggest("test")
            assertEquals(suggestions, viewModel.suggestions.value)
            assertNull(viewModel.error.value)
            assertNull(viewModel.didYouMean.value)
        }

    @Test
    fun `autosuggest returns did you mean and livedata is populated correctly`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                AutosuggestLogicManager.Result(
                    AutosuggestWithDidyouMean(
                        null,
                        suggestions.firstOrNull()
                    )
                )
            }

            viewModel.autosuggest("test")
            assertEquals(suggestions.firstOrNull(), viewModel.didYouMean.value)
            assertEquals(null, viewModel.suggestions.value)
            assertEquals(null, viewModel.error.value)
            verify(exactly = 0) {
                observerError.onChanged(any())
            }
            verify(exactly = 0) {
                observerSuggestions.onChanged(any())
            }
            verify(exactly = 1) {
                observerDidYouMean.onChanged(suggestions.firstOrNull())
            }
        }

    @Test
    fun `autosuggest returns an error and livedata is populated correctly`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                AutosuggestLogicManager.Result(
                    APIResponse.What3WordsError.INVALID_KEY
                )
            }

            viewModel.suggestions.observeForever(observerSuggestions)
            viewModel.error.observeForever(observerError)
            viewModel.didYouMean.observeForever(observerDidYouMean)
            viewModel.autosuggest("test")
            assertEquals(APIResponse.What3WordsError.INVALID_KEY, viewModel.error.value)
            assertEquals(null, viewModel.suggestions.value)
            assertEquals(null, viewModel.didYouMean.value)
            verify(exactly = 1) {
                observerError.onChanged(APIResponse.What3WordsError.INVALID_KEY)
            }
            verify(exactly = 0) {
                observerSuggestions.onChanged(any())
            }
            verify(exactly = 0) {
                observerDidYouMean.onChanged(any())
            }
        }

    @Test
    fun `autosuggest selection with coordinates flow`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()

            val suggestionsWithCoordinatesJson =
                ClassLoader.getSystemResource("suggestions-with-coordinates.json").readText()
            val suggestionsWithCoordinates =
                Gson().fromJson(
                    suggestionsWithCoordinatesJson,
                    Array<SuggestionWithCoordinates>::class.java
                )
                    .toList()

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                AutosuggestLogicManager.Result(
                    AutosuggestWithDidyouMean(
                        suggestions,
                        null
                    )
                )
            }

            coEvery {
                manager.selectedWithCoordinates("test", suggestions.first())
            } answers {
                AutosuggestLogicManager.Result(
                    suggestionsWithCoordinates.first()
                )
            }

            viewModel.autosuggest("test")
            assertEquals(suggestions, viewModel.suggestions.value)
            assertNull(viewModel.didYouMean.value)
            assertNull(viewModel.error.value)

            viewModel.onSuggestionClicked("test", suggestions.first(), true)
            assertEquals(
                suggestionsWithCoordinates.first(),
                viewModel.selectedSuggestion.value
            )
            assertNotNull(viewModel.selectedSuggestion.value?.coordinates)

            verify(exactly = 0) {
                observerError.onChanged(any())
            }
            verify(exactly = 1) {
                observerSuggestions.onChanged(suggestions)
            }
            verify(exactly = 0) {
                observerDidYouMean.onChanged(any())
            }
            verify(exactly = 1) {
                observerSelected.onChanged(suggestionsWithCoordinates.first())
            }
        }

    @Test
    fun `autosuggest selection without coordinates flow`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()

            val suggestionsWithCoordinates = SuggestionWithCoordinates(suggestions.first())

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                AutosuggestLogicManager.Result(
                    AutosuggestWithDidyouMean(
                        suggestions,
                        null
                    )
                )
            }

            coEvery {
                manager.selected("test", suggestions.first())
            } answers {
                AutosuggestLogicManager.Result(
                    suggestionsWithCoordinates
                )
            }

            viewModel.autosuggest("test")
            assertEquals(suggestions, viewModel.suggestions.value)
            assertNull(viewModel.didYouMean.value)
            assertNull(viewModel.error.value)

            viewModel.onSuggestionClicked("test", suggestions.first(), false)
            assertEquals(
                suggestionsWithCoordinates,
                viewModel.selectedSuggestion.value
            )
            assertNull(viewModel.selectedSuggestion.value?.coordinates)

            verify(exactly = 0) {
                observerError.onChanged(any())
            }
            verify(exactly = 1) {
                observerSuggestions.onChanged(suggestions)
            }
            verify(exactly = 0) {
                observerDidYouMean.onChanged(any())
            }
            verify(exactly = 1) {
                observerSelected.onChanged(suggestionsWithCoordinates)
            }
        }

    @Test
    fun `autosuggest multiple selection with coordinates flow`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()

            val suggestionsWithCoordinatesJson =
                ClassLoader.getSystemResource("suggestions-with-coordinates.json").readText()
            val suggestionsWithCoordinates =
                Gson().fromJson(
                    suggestionsWithCoordinatesJson,
                    Array<SuggestionWithCoordinates>::class.java
                ).toList()

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                AutosuggestLogicManager.Result(
                    AutosuggestWithDidyouMean(
                        suggestions,
                        null
                    )
                )
            }

            coEvery {
                manager.multipleWithCoordinates("test", suggestions)
            } answers {
                AutosuggestLogicManager.Result(
                    suggestionsWithCoordinates
                )
            }

            viewModel.autosuggest("test")
            assertEquals(suggestions, viewModel.suggestions.value)
            assertNull(viewModel.didYouMean.value)
            assertNull(viewModel.error.value)

            viewModel.onMultipleSuggestionsSelected("test", suggestions, true)
            assertEquals(
                suggestionsWithCoordinates,
                viewModel.multipleSelectedSuggestions.value
            )

            viewModel.multipleSelectedSuggestions.value!!.forEach {
                assertNotNull(it.coordinates)
            }

            verify(exactly = 0) {
                observerError.onChanged(any())
            }
            verify(exactly = 1) {
                observerSuggestions.onChanged(suggestions)
            }
            verify(exactly = 0) {
                observerDidYouMean.onChanged(any())
            }
            verify(exactly = 0) {
                observerSelected.onChanged(any())
            }
            verify(exactly = 1) {
                observerMultiple.onChanged(any())
            }
        }

    @Test
    fun `autosuggest multiple selection without coordinates flow`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()

            val suggestionsWithCoordinates = mutableListOf<SuggestionWithCoordinates>()
            suggestions.forEach {
                suggestionsWithCoordinates.add(SuggestionWithCoordinates(it))
            }

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                AutosuggestLogicManager.Result(
                    AutosuggestWithDidyouMean(
                        suggestions,
                        null
                    )
                )
            }

            coEvery {
                manager.multipleWithCoordinates("test", suggestions)
            } answers {
                AutosuggestLogicManager.Result(
                    suggestionsWithCoordinates
                )
            }

            viewModel.autosuggest("test")
            assertEquals(suggestions, viewModel.suggestions.value)
            assertNull(viewModel.didYouMean.value)
            assertNull(viewModel.error.value)

            viewModel.onMultipleSuggestionsSelected("test", suggestions, true)
            assertEquals(
                suggestionsWithCoordinates,
                viewModel.multipleSelectedSuggestions.value
            )

            viewModel.multipleSelectedSuggestions.value!!.forEach {
                assertNull(it.coordinates)
            }

            verify(exactly = 0) {
                observerError.onChanged(any())
            }
            verify(exactly = 1) {
                observerSuggestions.onChanged(suggestions)
            }
            verify(exactly = 0) {
                observerDidYouMean.onChanged(any())
            }
            verify(exactly = 0) {
                observerSelected.onChanged(any())
            }
            verify(exactly = 1) {
                observerMultiple.onChanged(any())
            }
        }
}
