package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.MediaRouter
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
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
import com.mobdeve.s13.martin.elaine.kabu20.voice.VoiceChatManager
import android.Manifest


class VideoCallActivity : AppCompatActivity(){

    private lateinit var binding: ActivityVideoCallBinding
    private lateinit var previewView: PreviewView
    private var isCameraOn = true
    private var isMicOn = false
    private var greeted = false

    private lateinit var voice: VoiceChatManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //camera
        startCamera()

        //VoiceChatManager
        voice = VoiceChatManager(this)

        if(!greeted){
            greeted = true
            binding.root.post {
                voice.generateGreeting()
            }
        }

        //buttons
        binding.MenuBtn.setOnClickListener{
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        binding.VidBtn.setOnClickListener{
            if(isCameraOn){
                closeCamera()
                isCameraOn = false
            } else {
                startCamera()
                isCameraOn = true
            }
        }

        binding.MicBtn.setOnClickListener{
            if(!isMicOn){
                voice.startListening()
            } else {
                voice.stoplistening()
                voice.stopAllAudio()
            }
            isMicOn = !isMicOn
            binding.MicBtn.setBackgroundResource(
                if(isMicOn) R.drawable.outline_mic_30
                else R.drawable.outline_mic_off_24
            )
        }
    }

    //turn camera on/off
    private fun startCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
            binding.VidBtn.setBackgroundResource(R.drawable.outline_videocam_off_24)
            return
        }

        previewView = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        binding.user.addView(previewView)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                    // Optional: add SER/FER later
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                binding.VidBtn.setBackgroundResource(R.drawable.outline_videocam_30)
            } catch (e: Exception) {
                Log.e("Camera", "bindToLifecycle failed: ${e.message}")
                binding.VidBtn.setBackgroundResource(R.drawable.outline_videocam_off_24)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun closeCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            binding.VidBtn.setBackgroundResource(R.drawable.outline_videocam_off_24)
            if(::previewView.isInitialized){
                binding.user.removeView(previewView)
            }
        }, ContextCompat.getMainExecutor(this))
    }

}