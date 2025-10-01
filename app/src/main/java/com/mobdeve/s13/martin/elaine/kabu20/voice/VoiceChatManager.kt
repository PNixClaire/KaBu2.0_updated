package com.mobdeve.s13.martin.elaine.kabu20.voice

import android.app.Activity
import android.util.Log
import com.unity3d.player.UnityPlayer
import org.json.JSONArray
import org.json.JSONObject

class VoiceChatManager (
    private val activity: Activity
){
    private var greeted = false
    private val messages = JSONArray().apply {
        put(JSONObject().apply {
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
                        "REMEMBER: Be cohesive. Try to refer to previous parts of the conversation naturally." +
                        "SESSION START: Always begin with a natural greeting (1-2 sentences), warm, friendly, and food-related if possible," +
                        "STYLE RULE: Do NOT use emojis or describe emojis. Never output words like 'smiling face' or 'emoji'. Speak naturally without symbols."
            )
        })
    }

    private var stt: STTClient? = null
    private val llm = LLMClient(onToken = {/*if we had a text UI we'd put it here*/})
    private val tts = TTSClient(activity)

    private var listening = false

    fun generateGreeting() {
        if (greeted) return
        greeted = true

        llm.chatStream(
            messages,
            onSentence = { sentence ->
                activity.runOnUiThread {
                    val isLast = sentence.endsWith(".") || sentence.endsWith("?") || sentence.endsWith("!")
                    tts.speak(
                        text = sentence,
                        isLastSentence = isLast, // let the last spoken chunk trigger STT
                        onStart = { Log.d("VoiceChat", "KaBu speaking: $sentence") },
                        onDone = {
                            if (isLast) {
                                Log.d("VoiceChat", "Greeting finished. Listening now...")
                                startListening()
                            }
                        },
                        onError = { err -> Log.e("VoiceChat", "TTS error: $err") }
                    )
                }
            },
            onDone = { reply ->
                Log.d("VoiceChat", "Greeting generation complete: $reply")
                // Do nothing here except logging, sentences already spoken by onSentence
            },
            onError = { err ->
                Log.e("VoiceChat", "Greeting generation error: $err")
                activity.runOnUiThread {
                    tts.speak(
                        text = "Hi there! I'm KaBu. How have you been?",
                        isLastSentence = true,
                        onDone = { startListening() }
                    )
                }
            }
        )
    }



    //make kabu prompt the user
    fun playGreeting(greeting: String){
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", greeting)
        })

        activity.runOnUiThread{
            tts.speak(
                text = greeting,
                onStart = {
                    Log.d("VoiceChat", "Kabu greets user...")
                    triggerTalking()
                },
                onDone = {
                    Log.d("VoiceChat: ", "Listening now...")
                    triggerIdle()
                    startListening()
                },
                onError = { err ->
                    Log.e("VoiceChat ", "TTS error: $err")
                    triggerIdle()
                    startListening()
                }
            )
        }
    }

    fun startListening() {
        if (listening) return
        listening = true

        stt = STTClient(
            activity,
            onPartial = { /* optional live captions */ },
            onFinal = { finalText ->
                listening = false

                if (finalText.isBlank()) return@STTClient
                Log.d("VoiceChat", "User said: $finalText")

                // Save user message
                messages.put(JSONObject().apply {
                    put("role", "user")
                    put("content", finalText)
                })

                // Ask LLM for KaBu's reply
                llm.chatStream(
                    messages,
                    onSentence = { sentence ->
                        activity.runOnUiThread {
                            val isLast = sentence.endsWith(".") || sentence.endsWith("?") || sentence.endsWith("!")
                            tts.speak(
                                text = sentence,
                                isLastSentence = isLast,
                                onStart = {
                                    Log.d("VoiceChat", "KaBu starts speaking sentence...")
                                    triggerTalking()
                                },
                                onDone = {
                                    Log.d("VoiceChat", "KaBu finished sentence.")
                                    if (isLast) {
                                        Log.d("VoiceChat", "Reply done. Listening again...")
                                        triggerIdle()
                                        startListening()
                                    }
                                },
                                onError = { err ->
                                    Log.e("VoiceChat", "TTS error: $err")
                                    if (isLast) {
                                        triggerIdle()
                                        startListening()
                                    }
                                },
                            )
                        }
                    },
                    onDone = { reply ->
                        if (reply.isNotBlank()) {
                            // Save KaBu's reply once, but don’t play it again
                            messages.put(JSONObject().apply {
                                put("role", "assistant")
                                put("content", reply)
                            })
                            Log.d("VoiceChat", "KaBu full reply: $reply")
                        } else {
                            triggerIdle()
                        }
                    },
                    onError = { err ->
                        Log.e("VoiceChat", "LLM error: $err")
                        activity.runOnUiThread { triggerIdle() }
                    },
                )
            },
            onError = { err ->
                Log.e("VoiceChat", "STT error: $err")
            },fallbackTTS = { line, after ->
                activity.runOnUiThread {
                    // Always say the fixed fallback line, not the old reply
                    tts.speak(
                        text = line,
                        isLastSentence = true,
                        onDone = {
                            Log.d("VoiceChat", "Fallback spoken: $line")
                            triggerIdle()
                            after() // restart listening
                        },
                        onStart = { triggerTalking() }
                    )
                }
            }
        ).also { it.start() }
    }


    fun stoplistening(){
        listening = false
        stt?.stop()
    }

    fun stopAllAudio(){
        tts.stop()
        triggerIdle()
    }

    //UNITY ANIMATION TRIGGERS
    private fun triggerTalking(){
        try{
            UnityPlayer.UnitySendMessage("kabu_happy_neutral", "PlayTalking", "")
        } catch (e: Exception){
            Log.e("VoiceChat", "Unity talking failed: ${e.message}")
        }
    }

    private fun triggerIdle(){
        try{
            UnityPlayer.UnitySendMessage("kabu_happy_neutral", "PlayIdle", "")
        } catch (e: Exception){
            Log.e("VoiceChat", "Unity idle failed: ${e.message}")
        }
    }
}