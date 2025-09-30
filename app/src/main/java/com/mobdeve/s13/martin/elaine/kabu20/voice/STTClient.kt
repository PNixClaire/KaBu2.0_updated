package com.mobdeve.s13.martin.elaine.kabu20.voice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.ui.input.key.KeyEventType

//This is for the STT client
class STTClient (
    private val activity: Activity,
    private val onPartial: (String) -> Unit, //for live captions
    private val onFinal: (String) -> Unit, //when user finished a phrase
    private val onError: (String) -> Unit
){
    private var sr: SpeechRecognizer? = null
    private var listening = false

    /*
    * - checks if SpeechRecognizer is available
    * - creates a recognizer and attaches a RecognitionListerner
    * - Starts listening with a configured RecognizerIntent
    * */
    fun start(lang: String = "en-US"){
        if(listening) return

        if(!SpeechRecognizer.isRecognitionAvailable(activity)){
            onError("SpeechRecognizer not available")
            return
        }

        sr = SpeechRecognizer.createSpeechRecognizer(activity).apply {
            setRecognitionListener(object : RecognitionListener{

                override fun onReadyForSpeech(params: Bundle?){}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onEvent(eventType: Int, params: Bundle?) {}

                //when recognizer thinks you're mid-sentence, gives early guesses
                override fun onPartialResults(partialResults: Bundle?) {
                    val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!list.isNullOrEmpty()) onPartial(list[0])
                }

                //fires when recognizer thinks you're done; gives final text
                override fun onResults(results: Bundle?) {
                    val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = list?.firstOrNull().orEmpty()
                    if(text.isNotBlank()) onFinal(text) else onError("No STT result")
                }

                override fun onError(error: Int) {
                    val message = when (error){
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        else -> "STT error code: $error"
                    }
                    onError(message)
                }
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        sr?.startListening(intent)
        listening = true
    }

    //clean up and release recognizer
    fun stop(){
        listening = false
        sr?.cancel()
        sr?.destroy()
        sr = null
    }
}