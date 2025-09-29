package com.mobdeve.s13.martin.elaine.kabu20.voice

import android.app.Activity
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.provider.MediaStore.Audio
import android.util.Log
import com.google.common.primitives.Bytes
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.ByteString
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import okio.ByteString.Companion.toByteString


//This is for the STT client
class STTClient (
    private val activity: Activity,
    private val onPartial: (String) -> Unit, //for live captions
    private val onFinal: (String) -> Unit, //when user finished a phrase
    private val onError: (String) -> Unit
){
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var webSocket: WebSocket? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)//keep the connection open
        .build()

    fun start(
        serverUrl: String = "ws://192.168.100.10:6006/ws_asr"
    ){
        if (isRecording) return

        //open websocket
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("STT", "Message: $text")
                if (text.contains("\"type\":\"partial\"")) {
                    val partial = Regex("\"text\":\"(.*?)\"").find(text)?.groupValues?.get(1) ?: ""
                    if (partial.isNotBlank()) {
                        activity.runOnUiThread { onPartial(partial) }
                    }
                }
                if (text.contains("\"type\":\"final\"")) {
                    val final = Regex("\"text\":\"(.*?)\"").find(text)?.groupValues?.get(1) ?: ""
                    if (final.isNotBlank()) {
                        activity.runOnUiThread { onFinal(final) }
                    }
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("STT", "WS error: ${t.message}")
                activity.runOnUiThread { onError("WS error: ${t.message}") }
            }
        })

        //start mic
        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val buffersSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        //ignore this error
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            buffersSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED){
            onError("AudioRecord init failed")
            return
        }

        audioRecord?.startRecording()
        isRecording = true

        Thread{
            val buffer = ByteArray(buffersSize)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    webSocket?.send(buffer.toByteString(0, read))
                }
            }

            webSocket?.send("{\"event\":\"end\"}")
        }.start()

    }

    fun stop(){
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        webSocket?.close(100, "bye")
        webSocket = null
    }

}