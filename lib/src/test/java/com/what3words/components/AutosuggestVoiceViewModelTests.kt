package com.what3words.components

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.components.models.AutosuggestRepository
import com.what3words.components.models.Either
import com.what3words.components.models.VoiceAutosuggestRepository
import com.what3words.components.vm.AutosuggestVoiceViewModel
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
internal class AutosuggestVoiceViewModelTests {

    @MockK
    private lateinit var repository: AutosuggestRepository

    @MockK
    private lateinit var voiceRepository: VoiceAutosuggestRepository

    @MockK
    internal lateinit var viewModel: AutosuggestVoiceViewModel

    @MockK
    private lateinit var microphone: Microphone

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        repository = mockk()
        microphone = mockk()
        voiceRepository = mockk()
        viewModel = AutosuggestVoiceViewModel(coroutinesTestRule.testDispatcherProvider)
        viewModel.repository = repository

        justRun {
            voiceRepository.updateOptions(any())
            microphone.onListening(any())
            microphone.onError(any())
        }

        every {
            microphone.onListening(any())
        } answers {
            microphone
        }

        every {
            microphone.onError(any())
        } answers {
            microphone
        }

        viewModel.setMicrophone(microphone)
    }

    @Test
    fun `autosuggest returns manager and sharedflow is populated correctly`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            var errorResult: APIResponse.What3WordsError? = null

            coEvery {
                repository.autosuggest(microphone, any(), any())
            } answers {
                Either.Right(voiceRepository)
            }

            coEvery {
                voiceRepository.startListening()
            } answers {
                Either.Right(emptyList())
            }

            val jobs = launch {
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
            }

            viewModel.autosuggest("en")
            Assert.assertEquals(voiceRepository, viewModel.voiceManager)
            Assert.assertNull(errorResult)
            jobs.cancel()
        }

    @Test
    fun `autosuggest returns an error and sharedflow is populated correctly`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            var errorResult: APIResponse.What3WordsError? = null
            val suggestionsResult: MutableList<Suggestion> = mutableListOf()

            coEvery {
                repository.autosuggest(microphone, any(), any())
            } answers {
                Either.Left(
                    APIResponse.What3WordsError.INVALID_KEY
                )
            }

            coEvery {
                voiceRepository.startListening()
            } answers {
                Either.Right(emptyList())
            }

            val jobs = launch {
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
                launch {
                    viewModel.suggestions.collect {
                        suggestionsResult.addAll(it)
                    }
                }
            }

            viewModel.autosuggest("en")
            Assert.assertEquals(APIResponse.What3WordsError.INVALID_KEY, errorResult)
            Assert.assertEquals(emptyList<Suggestion>(), suggestionsResult)
            jobs.cancel()
        }

    @Test
    fun `autosuggest selection with coordinates flow`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            var errorResult: APIResponse.What3WordsError? = null
            val suggestionsResult: MutableList<Suggestion> = mutableListOf()
            var selectedSuggestionResult: SuggestionWithCoordinates? = null

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
                repository.autosuggest(microphone, any(), any())
            } answers {
                Either.Right(voiceRepository)
            }

            coEvery {
                voiceRepository.startListening()
            } answers {
                Either.Right(suggestions)
            }

            coEvery {
                repository.selectedWithCoordinates("test", suggestions.first())
            } answers {
                Either.Right(
                    suggestionsWithCoordinates.first()
                )
            }

            val jobs = launch {
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
                launch {
                    viewModel.suggestions.collect {
                        suggestionsResult.addAll(it)
                    }
                }
                launch {
                    viewModel.selectedSuggestion.collect {
                        selectedSuggestionResult = it
                    }
                }
            }

            viewModel.autosuggest("test")
            Assert.assertEquals(suggestions, suggestionsResult)
            Assert.assertNull(errorResult)

            viewModel.onSuggestionClicked("test", suggestions.first(), true)
            Assert.assertEquals(
                suggestionsWithCoordinates.first(),
                selectedSuggestionResult
            )
            Assert.assertNotNull(selectedSuggestionResult?.coordinates)
            jobs.cancel()
        }

    @Test
    fun `autosuggest selection without coordinates flow`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            var errorResult: APIResponse.What3WordsError? = null
            val suggestionsResult: MutableList<Suggestion> = mutableListOf()
            var selectedSuggestionResult: SuggestionWithCoordinates? = null

            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()

            val suggestionsWithCoordinates = SuggestionWithCoordinates(suggestions.first())

            coEvery {
                repository.autosuggest(microphone, any(), any())
            } answers {
                Either.Right(voiceRepository)
            }

            coEvery {
                voiceRepository.startListening()
            } answers {
                Either.Right(suggestions)
            }

            coEvery {
                repository.selected("test", suggestions.first())
            } answers {
                Either.Right(
                    suggestionsWithCoordinates
                )
            }

            val jobs = launch {
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
                launch {
                    viewModel.suggestions.collect {
                        suggestionsResult.addAll(it)
                    }
                }
                launch {
                    viewModel.selectedSuggestion.collect {
                        selectedSuggestionResult = it
                    }
                }
            }

            viewModel.autosuggest("test")
            Assert.assertEquals(suggestions, suggestionsResult)
            Assert.assertNull(errorResult)

            viewModel.onSuggestionClicked("test", suggestions.first(), false)
            Assert.assertEquals(
                suggestionsWithCoordinates,
                selectedSuggestionResult
            )
            Assert.assertNull(selectedSuggestionResult?.coordinates)
            jobs.cancel()
        }

    @Test
    fun `autosuggest multiple selection with coordinates flow`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            var errorResult: APIResponse.What3WordsError? = null
            val suggestionsResult: MutableList<Suggestion> = mutableListOf()
            val multipleSuggestionsResult: MutableList<SuggestionWithCoordinates> = mutableListOf()

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
                repository.autosuggest(microphone, any(), any())
            } answers {
                Either.Right(voiceRepository)
            }

            coEvery {
                voiceRepository.startListening()
            } answers {
                Either.Right(suggestions)
            }

            coEvery {
                repository.multipleWithCoordinates("test", suggestions)
            } answers {
                Either.Right(
                    suggestionsWithCoordinates
                )
            }

            val jobs = launch {
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
                launch {
                    viewModel.suggestions.collect {
                        suggestionsResult.addAll(it)
                    }
                }
                launch {
                    viewModel.multipleSelectedSuggestions.collect {
                        multipleSuggestionsResult.addAll(it)
                    }
                }
            }

            viewModel.autosuggest("test")
            Assert.assertEquals(suggestions, suggestionsResult)
            Assert.assertNull(errorResult)

            viewModel.onMultipleSuggestionsSelected("test", suggestions, true)
            Assert.assertEquals(
                suggestionsWithCoordinates,
                multipleSuggestionsResult
            )

            multipleSuggestionsResult.forEach {
                Assert.assertNotNull(it.coordinates)
            }
            jobs.cancel()
        }

    @Test
    fun `autosuggest multiple selection without coordinates flow`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            var errorResult: APIResponse.What3WordsError? = null
            val suggestionsResult: MutableList<Suggestion> = mutableListOf()
            val multipleSuggestionsResult: MutableList<SuggestionWithCoordinates> = mutableListOf()

            val suggestionsJson =
                ClassLoader.getSystemResource("suggestions.json").readText()
            val suggestions =
                Gson().fromJson(suggestionsJson, Array<Suggestion>::class.java).toList()

            val suggestionsWithCoordinates = mutableListOf<SuggestionWithCoordinates>()
            suggestions.forEach {
                suggestionsWithCoordinates.add(SuggestionWithCoordinates(it))
            }

            coEvery {
                repository.autosuggest(microphone, any(), any())
            } answers {
                Either.Right(voiceRepository)
            }

            coEvery {
                voiceRepository.startListening()
            } answers {
                Either.Right(suggestions)
            }

            coEvery {
                repository.multipleWithCoordinates("test", suggestions)
            } answers {
                Either.Right(
                    suggestionsWithCoordinates
                )
            }

            val jobs = launch {
                launch {
                    viewModel.error.collect {
                        errorResult = it
                    }
                }
                launch {
                    viewModel.suggestions.collect {
                        suggestionsResult.addAll(it)
                    }
                }
                launch {
                    viewModel.multipleSelectedSuggestions.collect {
                        multipleSuggestionsResult.addAll(it)
                    }
                }
            }

            viewModel.autosuggest("test")
            Assert.assertEquals(suggestions, suggestionsResult)
            Assert.assertNull(errorResult)

            viewModel.onMultipleSuggestionsSelected("test", suggestions, true)
            Assert.assertEquals(
                suggestionsWithCoordinates,
                multipleSuggestionsResult
            )

            multipleSuggestionsResult.forEach {
                Assert.assertNull(it.coordinates)
            }
            jobs.cancel()
        }
}
