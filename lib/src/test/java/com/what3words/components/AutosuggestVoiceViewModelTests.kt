package com.what3words.components

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.components.models.AutosuggestApiManager
import com.what3words.components.models.Result
import com.what3words.components.models.VoiceAutosuggestManager
import com.what3words.components.vm.AutosuggestVoiceViewModel
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
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
class AutosuggestVoiceViewModelTests {
    @MockK
    private lateinit var observerError: Observer<APIResponse.What3WordsError?>

    @MockK
    private lateinit var manager: AutosuggestApiManager

    @MockK
    private lateinit var voiceManager: VoiceAutosuggestManager

    @MockK
    private lateinit var observerSuggestions: Observer<List<Suggestion>>

    @MockK
    private lateinit var observerDidYouMean: Observer<Suggestion?>

    @MockK
    private lateinit var observerSelected: Observer<SuggestionWithCoordinates>

    @MockK
    private lateinit var observerMultiple: Observer<List<SuggestionWithCoordinates>>

    @MockK
    private lateinit var viewModel: AutosuggestVoiceViewModel

    @MockK
    private lateinit var microphone: Microphone

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        manager = mockk()
        microphone = mockk()
        observerSuggestions = mockk()
        voiceManager = mockk()
        observerError = mockk()
        observerSelected = mockk()
        observerDidYouMean = mockk()
        observerMultiple = mockk()
        viewModel = AutosuggestVoiceViewModel(coroutinesTestRule.testDispatcherProvider)
        viewModel.manager = manager
        viewModel.microphone = microphone

        justRun {
            observerError.onChanged(any())
            observerDidYouMean.onChanged(any())
            observerSuggestions.onChanged(any())
            observerSelected.onChanged(any())
            observerMultiple.onChanged(any())
            voiceManager.updateOptions(any())
        }

        viewModel.suggestions.observeForever(observerSuggestions)
        viewModel.error.observeForever(observerError)
        viewModel.selectedSuggestion.observeForever(observerSelected)
        viewModel.multipleSelectedSuggestions.observeForever(observerMultiple)
    }

    @After
    fun tearDown() {
        viewModel.suggestions.removeObserver(observerSuggestions)
        viewModel.error.removeObserver(observerError)
        viewModel.selectedSuggestion.removeObserver(observerSelected)
        viewModel.multipleSelectedSuggestions.removeObserver(observerMultiple)
    }

    @Test
    fun `autosuggest returns manager and livedata is populated correctly`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            coEvery {
                manager.autosuggest(microphone, any(), any())
            } answers {
                Result(voiceManager)
            }

            viewModel.autosuggest("en")
            Assert.assertEquals(voiceManager, viewModel.voiceManager.value)
            Assert.assertNull(viewModel.error.value)
        }

    @Test
    fun `autosuggest returns an error and livedata is populated correctly`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {

            coEvery {
                manager.autosuggest(microphone, any(), any())
            } answers {
                Result(
                    APIResponse.What3WordsError.INVALID_KEY
                )
            }

            viewModel.suggestions.observeForever(observerSuggestions)
            viewModel.error.observeForever(observerError)
            viewModel.autosuggest("en")
            Assert.assertEquals(APIResponse.What3WordsError.INVALID_KEY, viewModel.error.value)
            Assert.assertEquals(null, viewModel.suggestions.value)
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
                manager.autosuggest(microphone, any(), any())
            } answers {
                Result(voiceManager)
            }

            coEvery {
                voiceManager.startListening()
            } answers {
                Result(suggestions)
            }

            coEvery {
                manager.selectedWithCoordinates("test", suggestions.first())
            } answers {
                Result(
                    suggestionsWithCoordinates.first()
                )
            }

            viewModel.autosuggest("test")
            viewModel.startListening()
            Assert.assertEquals(suggestions, viewModel.suggestions.value)
            Assert.assertNull(viewModel.error.value)

            viewModel.onSuggestionClicked("test", suggestions.first(), true)
            Assert.assertEquals(
                suggestionsWithCoordinates.first(),
                viewModel.selectedSuggestion.value
            )
            Assert.assertNotNull(viewModel.selectedSuggestion.value?.coordinates)

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
                manager.autosuggest(microphone, any(), any())
            } answers {
                Result(voiceManager)
            }

            coEvery {
                voiceManager.startListening()
            } answers {
                Result(suggestions)
            }

            coEvery {
                manager.selected("test", suggestions.first())
            } answers {
                Result(
                    suggestionsWithCoordinates
                )
            }

            viewModel.autosuggest("test")
            viewModel.startListening()
            Assert.assertEquals(suggestions, viewModel.suggestions.value)
            Assert.assertNull(viewModel.error.value)

            viewModel.onSuggestionClicked("test", suggestions.first(), false)
            Assert.assertEquals(
                suggestionsWithCoordinates,
                viewModel.selectedSuggestion.value
            )
            Assert.assertNull(viewModel.selectedSuggestion.value?.coordinates)

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
                manager.autosuggest(microphone, any(), any())
            } answers {
                Result(voiceManager)
            }

            coEvery {
                voiceManager.startListening()
            } answers {
                Result(suggestions)
            }

            coEvery {
                manager.multipleWithCoordinates("test", suggestions)
            } answers {
                Result(
                    suggestionsWithCoordinates
                )
            }

            viewModel.autosuggest("test")
            viewModel.startListening()
            Assert.assertEquals(suggestions, viewModel.suggestions.value)
            Assert.assertNull(viewModel.error.value)

            viewModel.onMultipleSuggestionsSelected("test", suggestions, true)
            Assert.assertEquals(
                suggestionsWithCoordinates,
                viewModel.multipleSelectedSuggestions.value
            )

            viewModel.multipleSelectedSuggestions.value!!.forEach {
                Assert.assertNotNull(it.coordinates)
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
                manager.autosuggest(microphone, any(), any())
            } answers {
                Result(voiceManager)
            }

            coEvery {
                voiceManager.startListening()
            } answers {
                Result(suggestions)
            }

            coEvery {
                manager.multipleWithCoordinates("test", suggestions)
            } answers {
                Result(
                    suggestionsWithCoordinates
                )
            }

            viewModel.autosuggest("test")
            viewModel.startListening()
            Assert.assertEquals(suggestions, viewModel.suggestions.value)
            Assert.assertNull(viewModel.error.value)

            viewModel.onMultipleSuggestionsSelected("test", suggestions, true)
            Assert.assertEquals(
                suggestionsWithCoordinates,
                viewModel.multipleSelectedSuggestions.value
            )

            viewModel.multipleSelectedSuggestions.value!!.forEach {
                Assert.assertNull(it.coordinates)
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
