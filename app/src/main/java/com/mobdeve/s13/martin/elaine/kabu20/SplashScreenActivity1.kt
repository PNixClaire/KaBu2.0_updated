package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobdeve.s13.martin.elaine.kabu20.databinding.ActivitySplashScreen1Binding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SplashScreenActivity1 : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreen1Binding

    private val client = OkHttpClient.Builder()
        . connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreen1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        //fade-in
        binding.splashScreen1.alpha = 0f
        binding.splashScreen1.animate().setDuration(3000).alpha(1f)

        //preload model
        preloadModel()
    }

    //this laods the model during splash screen before directing the user to the home page
    private fun preloadModel(){
        val jsonBody = JSONObject().apply {
            put("model", "qwen2.5")
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", "Hello")
            }))
            put("temperature", 0.0)
            put("num_predict", 10)
        }
        val request = Request.Builder()
            .url("http://192.168.100.9:11434/api/chat") //TODO: change this to your machine IP address
            .post(jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                goToNextScreen()
            }

            override fun onResponse(call: Call, response: Response) {
                goToNextScreen()
            }
        })
    }

    private fun goToNextScreen(){
        runOnUiThread{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}