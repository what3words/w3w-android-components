package com.what3words.components

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.what3words.components.models.AutosuggestApiManager
import com.what3words.components.models.Either
import com.what3words.components.vm.AutosuggestTextViewModel
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
class AutosuggestTextViewModelTests {
    @MockK
    private lateinit var manager: AutosuggestApiManager

    @MockK
    private lateinit var viewModel: AutosuggestTextViewModel

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        manager = mockk()
        viewModel = AutosuggestTextViewModel(coroutinesTestRule.testDispatcherProvider)
        viewModel.manager = manager
    }

    @Test
    fun `autosuggest returns suggestions and sharedflow is populated correctly`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()
            var didYouMeanResult: Suggestion? = null
            val suggestionsResult: MutableList<Suggestion> = mutableListOf()
            var errorResult: APIResponse.What3WordsError? = null

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                Either.Right(Pair(suggestions, null))
            }

            val jobs = launch {
                launch {
                    viewModel.didYouMean.collect {
                        didYouMeanResult = it
                    }
                }
                launch {
                    viewModel.suggestions.collect {
                        suggestionsResult.addAll(it)
                    }
                }
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
            }

            viewModel.autosuggest("test")
            assertEquals(suggestions, suggestionsResult)
            assertNull(errorResult)
            assertNull(didYouMeanResult)
            jobs.cancel()
        }

    @Test
    fun `autosuggest returns did you mean and sharedflow is populated correctly`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()
            var didYouMeanResult: Suggestion? = null
            val suggestionsResult: MutableList<Suggestion> = mutableListOf()
            var errorResult: APIResponse.What3WordsError? = null

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                Either.Right(
                    Pair(
                        null,
                        suggestions.firstOrNull()
                    )
                )
            }

            val jobs = launch {
                launch {
                    viewModel.didYouMean.collect {
                        didYouMeanResult = it
                    }
                }
                launch {
                    viewModel.suggestions.collect {
                        suggestionsResult.addAll(it)
                    }
                }
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
            }

            viewModel.autosuggest("test", false)
            assertEquals(suggestions.firstOrNull(), didYouMeanResult)
            assertEquals(emptyList<Suggestion>(), suggestionsResult)
            assertEquals(null, errorResult)
            jobs.cancel()
        }

    @Test
    fun `autosuggest returns suggestions with allowFlexibleDelimiter and sharedflow is populated correctly`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()
            var didYouMeanResult: Suggestion? = null
            val suggestionsResult: MutableList<Suggestion> = mutableListOf()
            var errorResult: APIResponse.What3WordsError? = null

            coEvery {
                manager.autosuggest("test", any(), true)
            } answers {
                Either.Right(
                    Pair(
                        suggestions,
                        null
                    )
                )
            }

            val jobs = launch {
                launch {
                    viewModel.didYouMean.collect {
                        didYouMeanResult = it
                    }
                }
                launch {
                    viewModel.suggestions.collect {
                        suggestionsResult.addAll(it)
                    }
                }
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
            }

            viewModel.autosuggest("test", true)
            assertEquals(null, didYouMeanResult)
            assertEquals(suggestions, suggestionsResult)
            assertEquals(null, errorResult)
            jobs.cancel()
        }

    @Test
    fun `autosuggest returns an error and sharedflow is populated correctly`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            var didYouMeanResult: Suggestion? = null
            val suggestionsResult: MutableList<Suggestion> = mutableListOf()
            var errorResult: APIResponse.What3WordsError? = null

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                Either.Left(
                    APIResponse.What3WordsError.INVALID_KEY
                )
            }

            val jobs = launch {
                launch {
                    viewModel.didYouMean.collect {
                        didYouMeanResult = it
                    }
                }
                launch {
                    viewModel.suggestions.collect {
                        suggestionsResult.addAll(it)
                    }
                }
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
            }

            viewModel.autosuggest("test")
            assertEquals(APIResponse.What3WordsError.INVALID_KEY, errorResult)
            assertEquals(emptyList<Suggestion>(), suggestionsResult)
            assertNull(didYouMeanResult)
            jobs.cancel()
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
            var didYouMeanResult: Suggestion? = null
            val suggestionsResult: MutableList<Suggestion> = mutableListOf()
            var errorResult: APIResponse.What3WordsError? = null
            var selectedSuggestionResult: SuggestionWithCoordinates? = null

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                Either.Right(
                    Pair(
                        suggestions,
                        null
                    )
                )
            }

            coEvery {
                manager.selectedWithCoordinates("test", suggestions.first())
            } answers {
                Either.Right(
                    suggestionsWithCoordinates.first()
                )
            }

            val jobs = launch {
                launch {
                    viewModel.didYouMean.collect {
                        didYouMeanResult = it
                    }
                }
                launch {
                    viewModel.suggestions.collect {
                        suggestionsResult.addAll(it)
                    }
                }
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
                launch {
                    viewModel.selectedSuggestion.collect {
                        selectedSuggestionResult = it
                    }
                }
            }

            viewModel.autosuggest("test")
            assertEquals(suggestions, suggestionsResult)
            assertNull(didYouMeanResult)
            assertNull(errorResult)

            viewModel.onSuggestionClicked("test", suggestions.first(), true)
            assertEquals(
                suggestionsWithCoordinates.first(),
                selectedSuggestionResult
            )
            assertNotNull(selectedSuggestionResult?.coordinates)
            jobs.cancel()
        }

    @Test
    fun `autosuggest selection without coordinates flow`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()

            val suggestionsWithCoordinates = SuggestionWithCoordinates(suggestions.first())

            var didYouMeanResult: Suggestion? = null
            val suggestionsResult: MutableList<Suggestion> = mutableListOf()
            var errorResult: APIResponse.What3WordsError? = null
            var selectedSuggestionResult: SuggestionWithCoordinates? = null

            coEvery {
                manager.autosuggest("test", any())
            } answers {
                Either.Right(
                    Pair(
                        suggestions,
                        null
                    )
                )
            }

            coEvery {
                manager.selected("test", suggestions.first())
            } answers {
                Either.Right(
                    suggestionsWithCoordinates
                )
            }

            val jobs = launch {
                launch {
                    viewModel.didYouMean.collect {
                        didYouMeanResult = it
                    }
                }
                launch {
                    viewModel.suggestions.collect {
                        suggestionsResult.addAll(it)
                    }
                }
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
                launch {
                    viewModel.selectedSuggestion.collect {
                        selectedSuggestionResult = it
                    }
                }
            }

            viewModel.autosuggest("test")
            assertEquals(suggestions, suggestionsResult)
            assertNull(didYouMeanResult)
            assertNull(errorResult)

            viewModel.onSuggestionClicked("test", suggestions.first(), false)
            assertEquals(
                suggestionsWithCoordinates,
                selectedSuggestionResult
            )
            assertNull(selectedSuggestionResult?.coordinates)
            jobs.cancel()
        }
}
