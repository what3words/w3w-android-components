package com.what3words.components.compose.wrapper

import com.what3words.components.models.AutosuggestLogicManager

sealed class AutoSuggestConfiguration {
    /**
     * @property apiKey your API key from what3words developer dashboard
     * */
    data class Api(
        val apiKey: String,
    ) : AutoSuggestConfiguration()

    /**
     * @property apiKey your API key from what3words developer dashboard
     * @property endpoint your Enterprise API endpoint
     * @property headers any custom headers needed for your Enterprise API
     * */
    data class ApiWithEnterpriseEndpoint(
        val apiKey: String,
        val endpoint: String,
        val headers: Map<String, String> = mapOf()
    ) : AutoSuggestConfiguration()

    /**
     * @property apiKey your API key from what3words developer dashboard
     * @property endpoint your Enterprise API endpoint
     * @property voiceEndpoint your custom Voice API endpoint
     * @property headers any custom headers needed for your Enterprise API
     * */
    data class ApiWithEnterpriseAndVoiceEndpoint(
        val apiKey: String,
        val endpoint: String,
        val voiceEndpoint: String,
        val headers: Map<String, String> = mapOf()
    ) : AutoSuggestConfiguration()

    /**
     * @property logicManager manager created using SDK instead of API
     * **/
    data class Sdk(val logicManager: AutosuggestLogicManager) : AutoSuggestConfiguration()
}