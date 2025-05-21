package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobdeve.s13.martin.elaine.kabu20.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var viewBinding: ActivityMenuBinding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.BackBtn.setOnClickListener {
            startActivity(Intent(this, HomepageActivity::class.java))
            finish()
        }

        viewBinding.NotificationBtn.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            finish()
        }

        viewBinding.SettingsBtn.setOnClickListener {
            startActivity(Intent(this,SettingsActivity::class.java))
            finish()
        }

        viewBinding.SignoutBtn.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }
}