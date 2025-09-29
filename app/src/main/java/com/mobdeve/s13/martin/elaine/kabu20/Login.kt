package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
//import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobdeve.s13.martin.elaine.kabu20.databinding.ActivityLoginBinding
import com.mobdeve.s13.martin.elaine.kabu20.models.UserData
import java.security.MessageDigest

class Login : ComponentActivity() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
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
//            startActivity(Intent(this, SplashScreenActivity2::class.java))
//            finish()

            val loginUsername = viewBinding.loginUsernameInput.text.toString()
            val loginPass = viewBinding.loginPasswordInput.text.toString()
            loginUser(loginUsername, loginPass)
        }

        //Add/link firebase reference
        firebaseDatabase = FirebaseDatabase.getInstance("https://kabu2-84239-default-rtdb.asia-southeast1.firebasedatabase.app/")
        databaseReference = firebaseDatabase.reference.child("Users")
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

    private fun loginUser(username: String, password: String){
        val hashedPassword = hashPassword(password)
        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(userSnapshot in snapshot.children){
                        val storedPassword = userSnapshot.child("password").getValue(String::class.java)
                        val userData = userSnapshot.getValue(UserData::class.java)
                        if(userData != null && storedPassword == hashedPassword){
                            Toast.makeText(this@Login, "Login successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Login, SplashScreenActivity2::class.java)
                            intent.putExtra("username", username)
                            intent.putExtra("userId", userData.id)
                            startActivity(intent)
                            finish()
                            return

                        }else{
                            Toast.makeText(this@Login, "Incorrect password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }else {
                    Toast.makeText(this@Login, "No user found with that username", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

}