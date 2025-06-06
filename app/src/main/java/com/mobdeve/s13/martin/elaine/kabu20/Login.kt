package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
//import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobdeve.s13.martin.elaine.kabu20.databinding.ActivityLoginBinding

class Login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding: ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        applyFont()

        viewBinding.SignUpNowTV.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        viewBinding.SubmitBtn.setOnClickListener {//go to second splash page
            startActivity(Intent(this, SplashScreenActivity2::class.java))
            finish()
        }
    }


    private fun applyFont(){
        // Getting all the preloaded font styles
        val irish_grover: Typeface? = ResourcesCompat.getFont(this, R.font.irish_grover)
        val lexend_regular: Typeface? = ResourcesCompat.getFont(this, R.font.lexend_regular)

        // Applying luckiest_guy font to text views
        val title_tv: TextView = findViewById(R.id.Title_TV)
        val title2_tv: TextView = findViewById(R.id.Title2_TV)
        title_tv.typeface = irish_grover
        title2_tv.typeface = irish_grover


        val username_tv: TextView = findViewById(R.id.Username_TV)
        val username_input: EditText = findViewById(R.id.loginUsername_input)
        val password_tv: TextView = findViewById(R.id.Password_TV)
        val password_input: EditText = findViewById(R.id.loginPassword_input)
        val submit_btn: Button = findViewById(R.id.Submit_btn)

        username_tv.typeface = lexend_regular
        username_input.typeface = lexend_regular
        password_tv.typeface = lexend_regular
        password_input.typeface = lexend_regular
        submit_btn.typeface = lexend_regular


    }
}