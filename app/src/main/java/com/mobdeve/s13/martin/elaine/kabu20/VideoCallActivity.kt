package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.MediaRouter
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobdeve.s13.martin.elaine.kabu20.databinding.ActivityVideoCallBinding

class VideoCallActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityVideoCallBinding
    private lateinit var previewView: PreviewView //Camera preview

    private lateinit var audioRecord: AudioRecord
    private var isRecording = false

    private var isCameraOn = true
    private var camPermission = false

    private var isMicOn = true
    private var audioPermission = false

    private val RECORD_AUDIO_REQUEST_CODE = 1002
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        /* Checks for the app's permission to use the camera
        * if the app already has a permission to use the camera,
        * it opens the camera instantly (startCamera function)
        * else
        * it will request for permission
        * TODO: Fix the permission request.
        *  If the user did not allow the camera,
        *  then make sure that the camera
        *  is CLOSE, and the user won't be
        *  able to access it.
        *  ** Add a permission checker on the startCamera()
        *  ** Fix the logic for camPermission
        * */
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
            camPermission = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                1001)
        }

        /* Checks for the app's permission to use the microphone
        * if the app already has a permission to use the microphone,
        * it opens the camera instantly (startAudio function)
        * else
        * it will request for permission
        * TODO: Fix the permission request.
        *  If the user did not allow the microphone,
        *  then make sure that the microphone
        *  is CLOSE, and the user won't be
        *  able to access it.
        *  ** Add a permission checker on the startAudio()
        *  ** Fix the logic for audioPermission
        * */
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            startAudio()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE
            )
        }

        /* Menu Button */
        viewBinding.MenuBtn.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        /* Camera Button (on and off) */
        viewBinding.VidBtn.setOnClickListener{
            if(isCameraOn){
                closeCamera()
                isCameraOn = false
            }else{
                startCamera()
                isCameraOn = true
            }
        }

         viewBinding.MicBtn.setOnClickListener {
             if(isMicOn){
                 viewBinding.VidBtn.setBackgroundResource(R.drawable.outline_mic_off_24)
             }else{
                 viewBinding.VidBtn.setBackgroundResource(R.drawable.outline_mic_30)
             }
         }


    }


    private fun startAudio() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("Audio", "Microphone permission not granted")
            return
        }

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e("Audio", "Invalid buffer size")
            return
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, // Audio source
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("Audio", "AudioRecord initialization failed")
            return
        }

        audioRecord.startRecording()
        isRecording = true

        val audioBuffer = ByteArray(bufferSize)

        Thread {
            while (isRecording) {
                val read = audioRecord.read(audioBuffer, 0, audioBuffer.size)
                if (read > 0) {
                    // TODO: Send audioBuffer to STT engine
                }
            }
        }.start()
    }

    private fun stopAudio() {
        isRecording = false
        if (::audioRecord.isInitialized) {
            audioRecord.stop()
            audioRecord.release()
        }
    }

    private fun closeCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }, ContextCompat.getMainExecutor(this))

        viewBinding.VidBtn.setBackgroundResource(R.drawable.outline_videocam_off_24)

        viewBinding.user.removeView(previewView)
    }


    private fun startCamera() {
        previewView = PreviewView(this)

        previewView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        viewBinding.user.addView(previewView)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                    // TODO: Send frame to facial emotion recognition API
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))

        viewBinding.VidBtn.setBackgroundResource(R.drawable.outline_videocam_30)

    }

    // Optional: handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
    }
}