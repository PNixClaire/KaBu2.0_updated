package com.mobdeve.s13.martin.elaine.kabu20.voice

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Error
import java.util.concurrent.TimeUnit

class TTSClient (
    private  val context: Context,
    baseUrl: String = "http://192.168.100.10:5000/tts" //IP
){
    //handles HTTP request/response
    private  val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val url = baseUrl
    private var mediaPlayer: MediaPlayer? = null

    //sends text to flask server and gets audio back
    fun speak(
        text: String,
        voice: String = "en_US-kathleen-low",
        onError: (String) -> Unit = {},
        onDone: () -> Unit = {},
        onStart: () -> Unit = {}
    ){
        val filename = "tts_${System.currentTimeMillis()}.wav"

        //text, voice and filename for audio
        val json = JSONObject().apply {
            put("text", text)
            put("voice", voice)
            put("filename", filename)
        }

        val req = Request.Builder()
            .url(url)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), json.toString()))
            .build()

        //get audio
        client.newCall(req).enqueue(object : Callback{

            override fun onFailure(call: Call, e: IOException) {
                onError("TTS network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if(!response.isSuccessful || response.body == null){
                    onError("TTS HTTP error ${response.code}")
                    return
                }

                //store audio
                val audio = File(context.filesDir, filename)
                response.body!!.byteStream().use {
                        input -> FileOutputStream(audio).use {
                        out -> input.copyTo(out)
                }
                }

                //play audio + animation
                play(audio, onError, onDone, onStart)
            }
        })
    }

    //play the new audio
    private fun play(
        file: File,
        onError: (String) -> Unit,
        onDone: () -> Unit,
        onStart: () -> Unit
    ){
        try{
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.path)

                setOnPreparedListener{
                    onStart()
                    it.start()
                }

                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                    onDone()
                }

                prepareAsync()
            }
        } catch (e: Exception){
            onError("TTS play error: ${e.message}")
        }
    }

    //stop manually - not used yet
    fun stop(){
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}