package com.mobdeve.s13.martin.elaine.kabu20.voice

import android.app.Activity
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

//This is for the STT client
class STTClient (
    private val activity: Activity,
    private val onPartial: (String) -> Unit, //for live captions
    private val onFinal: (String, File?) -> Unit, //when user finished a phrase
    private val onError: (String) -> Unit,
    private val fallbackTTS: ((String, () -> Unit) -> Unit)? = null
){
    private var sr: SpeechRecognizer? = null
    private var listening = false

    //audio record
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private var audioRecord: AudioRecord? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var recordingThread: Thread? = null

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

        startAudioRecording() //SER

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
                    listening = false
                    stopAudioRecording() //SER

                    val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = list?.firstOrNull().orEmpty()

                    if(text.isNotBlank()) onFinal(text, audioFile) else {
                        fallbackTTS?.invoke("Sorry, I didn't catch that. Can you please repeat that for me?"){
                            start(lang)
                        }
                    }
                }

                override fun onError(error: Int) {
                    listening = false
                    stopAudioRecording()
                    onError("STT error: $error")

                    fallbackTTS?.invoke("Sorry, I didn’t catch that. Can you repeat that for me?") {
                        start(lang)
                    }
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

    //SER audio record handling
    private fun startAudioRecording(){
        audioFile = File(activity.cacheDir, "stt_audio_${System.currentTimeMillis()}.wav")

        //ignore this error
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        isRecording = true
        audioRecord?.startRecording()

        recordingThread = Thread{
            try {
                FileOutputStream(audioFile!!).use { out ->
                    val buffer = ByteArray(bufferSize)
                    while (isRecording){
                        val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                        if(read > 0){
                            out.write(buffer, 0, read)
                        }
                    }
                }
            } catch (e: Exception){
                Log.e("STT", "Recording error: ${e.message}")
            }
        }
        recordingThread?.start()
    }

    private fun stopAudioRecording(){
        try {
            isRecording = false
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            recordingThread?.join()
            recordingThread = null
        } catch (_: Exception){}
    }
}