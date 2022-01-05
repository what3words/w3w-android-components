//[lib](../../../index.md)/[com.what3words.components.voice](../index.md)/[W3WAutoSuggestVoice](index.md)/[microphone](microphone.md)

# microphone

[androidJvm]\
fun [microphone](microphone.md)(recordingRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), encoding: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), channel: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), audioSource: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)): [W3WAutoSuggestVoice](index.md)

Set a custom Microphone setup i.e: recording rate, encoding, channel in, etc.

#### Return

same [W3WAutoSuggestVoice](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| recordingRate | your custom recording rate |
| encoding | your custom encoding i.e [AudioFormat.ENCODING_PCM_16BIT](https://developer.android.com/reference/kotlin/android/media/AudioFormat.html#encoding_pcm_16bit) |
| channel | your custom channel_in i.e [AudioFormat.CHANNEL_IN_MONO](https://developer.android.com/reference/kotlin/android/media/AudioFormat.html#channel_in_mono) |
| audioSource | your audioSource i.e [MediaRecorder.AudioSource.MIC](https://developer.android.com/reference/kotlin/android/media/MediaRecorder.AudioSource.html#mic) |
