package com.mobdeve.s13.martin.elaine.kabu20.voice

import android.app.Activity
import android.util.Log
import com.unity3d.player.UnityPlayer
import org.json.JSONArray
import org.json.JSONObject

class VoiceChatManager (
    private val activity: Activity
){
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
                        "REMEMBER: Be cohesive. Try to refer to previous parts of the conversation naturally."
            )
        })
    }

    private var stt: STTClient? = null
    private val llm = LLMClient(onToken = {/*if we had a text UI we'd put it here*/})
    private val tts = TTSClient(activity)

    private var listening = false

    fun startListening() {
        if (listening) return
        listening = true

        stt = STTClient(
            activity,
            onPartial = { /* optional: log or debug partial speech */ },
            onFinal = { finalText ->
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
                    onDone = { reply ->
                        if (reply.isNotBlank()) {
                            // Save KaBu's reply
                            messages.put(JSONObject().apply {
                                put("role", "assistant")
                                put("content", reply)
                            })
                            Log.d("VoiceChat", "KaBu reply: $reply")

                            // Play reply with animation
                            activity.runOnUiThread {
                                triggerTalking()
                                tts.speak(reply) {
                                    triggerIdle()
                                }
                            }
                        } else {
                            triggerIdle()
                        }
                    },
                    onError = { err ->
                        Log.e("VoiceChat", "LLM error: $err")
                        activity.runOnUiThread { triggerIdle() }
                    }
                )
            },
            onError = { err ->
                Log.e("VoiceChat", "STT error: $err")
                // Could auto-restart STT here if you want
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