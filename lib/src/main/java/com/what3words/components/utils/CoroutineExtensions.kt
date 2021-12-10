package com.what3words.components.utils

import com.what3words.androidwrapper.helpers.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch

fun io(dispatcher : DispatcherProvider, work: suspend (() -> Unit)): Job {
    return CoroutineScope(dispatcher.io()).launch {
        work()
    }
}

fun main(dispatcher: DispatcherProvider, work: suspend (() -> Unit)): Job {
    return CoroutineScope(dispatcher.main()).launch {
        work()
    }
}