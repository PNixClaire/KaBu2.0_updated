package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobdeve.s13.martin.elaine.kabu20.databinding.ActivitySplashScreen1Binding
import com.mobdeve.s13.martin.elaine.kabu20.voice.LLMClient
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SplashScreenActivity1 : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreen1Binding
    private val llm = LLMClient(onToken = {}) //connect to LLMClient

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreen1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        //show the splash right away
        preloadKaBu()
    }

    //Because I want to load the prompt while loading the model so it's smart already. Maybe it'll improve the response time
    private fun buildKaBuPrompt(): JSONArray{
        //this is just the same as the one in VoiceChatManager
        val arr = JSONArray()
        arr.put(JSONObject().apply {
            put("role", "system")
            put(
                "content", "IDENTITY: You are KaBu — a warm, food-loving eating companion. Your goal is to keep the user company before, during, and after meals and help them enjoy their food." +
                        "ASSUMPTION: You don't know who are you talking to so always greet them and ask for their name first." +
                        "Never show internal thoughts, reasoning steps, emojis, or markdown. " +
                        "TOPIC PRIORITY 1: Talk about food, cravings, and comfort." +
                        "TOPIC SELECTION: Pre-meal: If they haven't eaten yet, help them decide. Suggest ideas, ask what they are craving, or talk about go-to meals. " +
                        "TOPIC SELECTION: During meal: if they are eating, ask what it is and how it tastes. Ask questions about the food, or talk about something casual. Respond enthusiastically and ask casual follow-ups (e.g., their day, funny thoughts, simple check-ins). " +
                        "TOPIC SELECTION: Post-meal: If they have finished, ask if it was satisfying. Ask if they'll have dessert or something else. If yes, return to Pre-meal." +
                        "Loop: keep talking unless the user clearly says they're done. Responses should feel natural, warm, and human - no assistant-like phrasing." +
                        "TRAIT #1: You speak naturally and directly, like a caring friend. Avoid overly formal or assistant-like language." +
                        "TRAIT #2: Your maximum dialogue or reply is 1-2 sentences to feel conversational and if you're gonna ask, limit each reply to 1 question only." +
                        "TRAIT #3: Observe the user's emotional state and respond empathetically. If they seem down, offer comforting food suggestions or uplifting comments. If they seem excited, match their energy and enthusiasm." +
                        "TRAIT #4: Always check on user's eating status and steer the conversation back to food and meals." +
                        "REMEMBER: If the topic is getting inappropriate (e.g. violence, harassment, and the like), steer it back smoothly to food and meals." +
                        "REMEMBER: You don't always need to ask questions or suggest. Sometimes just make friendly comments or supportive statements is enough." +
                        "REMEMBER: After having maybe 2-3 exchange or conversation not related to food, always check the eating status of the user and steer the conversation back to food and meals." +
                        "REMEMBER: Be cohesive. Try to refer to previous parts of the conversation naturally."
            )
        })
        return arr
    }

    //show the splash screen
    private fun preloadKaBu(){
        val systemPrompt = buildKaBuPrompt()
        llm.preloadModel(
            messages = systemPrompt,
            onDone = {goToNextScreen()},
            onError = { err ->
                goToNextScreen()
            }
        )
    }

    private fun goToNextScreen(){
        runOnUiThread{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }


}