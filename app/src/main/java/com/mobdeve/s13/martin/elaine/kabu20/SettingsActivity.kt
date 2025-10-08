package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobdeve.s13.martin.elaine.kabu20.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding: ActivitySettingsBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.btnSettingsBack.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        val optionsArray = resources.getStringArray(R.array.voice_options_array)

        val prefs = getSharedPreferences("KaBuPrefs", MODE_PRIVATE)
        val previousVoice = prefs.getString("KaBu_Voice", "Male") ?: "Male"
        val idx = optionsArray.indexOf(previousVoice).coerceAtLeast(0)
        viewBinding.voiceOptionsSpinner.setSelection(idx)

        viewBinding.voiceOptionsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedVoice = parent.getItemAtPosition(position).toString()
                val prefs = getSharedPreferences("KaBuPrefs", MODE_PRIVATE)
                prefs.edit().putString("KaBu_Voice", selectedVoice).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

    }
}