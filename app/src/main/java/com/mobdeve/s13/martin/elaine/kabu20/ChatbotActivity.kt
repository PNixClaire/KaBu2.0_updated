package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import com.mobdeve.s13.martin.elaine.kabu20.databinding.ActivityChatbotBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import okio.Buffer

class ChatbotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotBinding
    private val client = OkHttpClient()
    private val messageHistory = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val systemPrompt = JSONObject().apply{
            put("role", "system")
            put("content", "You are KaBu — a warm, food-loving eating companion." +
                    "Speak naturally and directly, like a caring friend." +
                    "Never show internal thoughts, reasoning steps, or markdown." +
                    "Talk about food, cravings, and comfort. Ask the user's name first for the first interactions." +
                    "Ask if they have eaten yet." +
                    "Pre-meal: if they haven't eaten yet, help them decide. Suggest ideas, ask what they are craving, or talk about go-to meals." +
                    "During meal: if they are eating, ask what it is and how it tastes. Respond ethusiastically and ask casual follow-ups (e.g. their day, funny thoughts, simple check-ins)." +
                    "Post-meal: If they have finished, ask if it was satisfying. Ask if they'll have dessert or something else. If yes, return to Pre-meal." +
                    "Loop: keep talking unless the user clearly says they're done. Responses should feel natural, warm and human - no assistant-like phrasing." +
                    "When they say they are already eating, ask questions about the food, or talk about something casual." +
                    "Limit each reply to 1-2 sentences to feel conversational.")
        }
        messageHistory.put(systemPrompt)
        
        binding.sendbutton.setOnClickListener {
            val userText = binding.editTextText.text.toString().trim()
            
            if(userText.isNotEmpty()){
                addMessage("You: $userText")
                
                //add user message to history
                messageHistory.put(JSONObject().apply { 
                    put("role", "user")
                    put("content", userText)
                })
                
                binding.editTextText.text.clear()
                sendToQwen()
            }
            
        }

        binding.BackBtn.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
        
        //initial greeting
        addMessage("KaBu: Hi there! What is your name?")
    }

    fun Buffer.readStringLine(): String? {
        val index = indexOf('\n'.toByte())
        return if (index == -1L) null else readString(index + 1, Charsets.UTF_8)
    }

    private fun addText(){
        val messageLayout = findViewById<LinearLayout>(R.id.linearLayout_Messages)
        val inputField = findViewById<EditText>(R.id.editTextText)

        val userInput = inputField.text.toString().trim()

        if (userInput.isNotEmpty()) {
            val newTextView = TextView(this)
            newTextView.text = "User: ${userInput}"
            newTextView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            newTextView.setPadding(10, 10, 10, 10)
            newTextView.textSize = 16f
            newTextView.setTextColor(Color.BLACK)
            messageLayout.addView(newTextView)
            inputField.text.clear()
        }
    }
    
    private fun addMessage(message: String){
        val messageLayout = binding.linearLayoutMessages
        val textView = TextView(this).apply { 
            text = message
            textSize = 16f
            setPadding(10,10,10,10)
            setTextColor(resources.getColor(android.R.color.black))
        }
        messageLayout.addView(textView)
    }
    
    private fun sendToQwen(){
        val jsonBody = JSONObject().apply { 
            put("model", "qwen2.5")
            put("messages", messageHistory)
            put("temperature", 0.7)
            put("repeat_penalty", 1.2)
            put("num_predict", 100)
        }
        
        val request = Request.Builder()
            .url("http://192.168.100.9:11434/api/chat") //replace with server IP
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody.toString()))
            .build()

        client.newCall(request).enqueue(object:Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread{
                    addMessage("KaBu: Oops! Something went wrong.")
                    addMessage("Debug: ${e.message}" )
                }
            }

            override fun onResponse(call: Call, response: Response) {
//                val lines = response.body?.string()?.split("\n") ?: return
//                val fullReply = StringBuilder()
//
//                for(line in lines){
//                    if(line.isNotBlank()){
//                        try{
//                            val obj = JSONObject(line)
//                            val chunk = obj.optJSONObject("message")?.optString("content", "")
//                            if(!chunk.isNullOrEmpty()) fullReply.append(chunk)
//                        } catch (_: Exception){}
//                    }
//                }

                val source = response.body?.source()
                source?.request(Long.MAX_VALUE)
                val buffer = source?.buffer

                val fullReply = StringBuilder()

                while (true){
                    val line = buffer?.readStringLine() ?: break

                    if(line.isNotBlank()){
                        try{
                            val obj = JSONObject(line)
                            val chunk = obj.optJSONObject("message")?.optString("content", "")
                            if(!chunk.isNullOrEmpty()){
                                fullReply.append(chunk)
                                runOnUiThread{
                                    binding.textView.text = "KaBu: ${fullReply}"
                                }
                            }
                        } catch (_:  Exception){

                        }
                    }
                }

                val reply = fullReply.toString().trim()
                if (reply.isNotEmpty()){
                    runOnUiThread{
                        addMessage("KaBu: $reply")
                        messageHistory.put(JSONObject().apply {
                            put("role", "assistant")
                            put("content", reply)
                        })
                    }
                }

//                runOnUiThread{
//                    val reply = fullReply.toString().trim()
//                    if(reply.isNotEmpty()){
//                        addMessage("KaBu: $reply")
//
//                        //add assistant reply to history
//                        messageHistory.put(JSONObject().apply {
//                            put("role", "assistant")
//                            put("content", reply)
//                        })
//                    }
//                }
            }
        })
        
    }
}