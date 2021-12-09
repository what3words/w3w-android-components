package com.what3words.components.models

import com.what3words.javawrapper.response.APIResponse

class Result<T> {
    private var error: APIResponse.What3WordsError? = null
    private var data: T? = null

    constructor(data: T) {
        this.data = data
    }

    constructor(error: APIResponse.What3WordsError) {
        this.error = error
    }

    fun isSuccessful() = this.data != null && error == null

    fun error(): APIResponse.What3WordsError? = this.error

    fun data(): T? = this.data
}
