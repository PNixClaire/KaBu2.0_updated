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
import com.mobdeve.s13.martin.elaine.kabu20.databinding.ActivityChatbotBinding

class ChatbotActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var viewBinding: ActivityChatbotBinding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.sendbutton.setOnClickListener {
            addText()
        }

        viewBinding.BackBtn.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
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
}