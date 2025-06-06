package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobdeve.s13.martin.elaine.kabu20.databinding.ActivitySplashScreen1Binding

class SplashScreenActivity1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewBinding: ActivitySplashScreen1Binding = ActivitySplashScreen1Binding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.splashScreen1.alpha = 0f
        viewBinding.splashScreen1.animate().setDuration(3000).alpha(1f).withEndAction {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }
}