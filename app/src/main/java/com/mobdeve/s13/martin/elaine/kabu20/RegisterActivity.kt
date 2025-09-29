package com.mobdeve.s13.martin.elaine.kabu20

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mobdeve.s13.martin.elaine.kabu20.R
import com.mobdeve.s13.martin.elaine.kabu20.databinding.ActivityRegisterBinding
import com.mobdeve.s13.martin.elaine.kabu20.models.UserData
import java.security.MessageDigest

class RegisterActivity : AppCompatActivity() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding: ActivityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.LoginNowTV.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        viewBinding.RegBtn.setOnClickListener {
            val registerUsername = viewBinding.regUsernameInput.text.toString()
            val registerPass1 = viewBinding.regPassInput.text.toString()
            val registerPass2 = viewBinding.regPassConfirmInput.text.toString()
            val registerEmail = viewBinding.editTextTextEmailAddress.text.toString()

            if(registerUsername.isNotEmpty() && registerPass1.isNotEmpty() && registerPass2.isNotEmpty()){
                if(registerPass2 == registerPass1){
                    registerUser(registerUsername, registerPass1, registerEmail)
                }else{
                    Toast.makeText(this@RegisterActivity, "Password DO NOT match", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this@RegisterActivity, "All fields are mandatory", Toast.LENGTH_SHORT).show()
            }
        }

        //Add/link firebase reference
        firebaseDatabase = FirebaseDatabase.getInstance("https://kabu2-84239-default-rtdb.asia-southeast1.firebasedatabase.app/")
        databaseReference = firebaseDatabase.reference.child("Users")

        val db = firebaseDatabase.reference
        db.child("test").setValue("Hello Firebase!")
            .addOnSuccessListener {
                Log.d("FirebaseTest", "Write succeeded")
            }
            .addOnFailureListener {
                Log.e("FirebaseTest", "Write failed: ${it.message}")
            }

    }

    private fun registerUser(username: String, password: String, email: String){
        val hashedPassword = hashPassword(password)
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //if the data does not exist it will create a new UserData
                if(!snapshot.exists()){
                    val id = databaseReference.push().key
                    var userData = UserData(id, username, hashedPassword,email)
                    databaseReference.child(id!!).setValue(userData)
                    Toast.makeText(this@RegisterActivity, "User successfully registered!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, Login::class.java))
                    finish()
                }else{
                    Toast.makeText(this@RegisterActivity, "User already exist", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegisterActivity, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()

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