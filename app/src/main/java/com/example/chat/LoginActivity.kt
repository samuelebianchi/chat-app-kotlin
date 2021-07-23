package com.example.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val button_login = findViewById<Button>(R.id.login_button)
        button_login.setOnClickListener {
            val email_signin = findViewById<EditText>(R.id.email_login)
            val email_val = email_signin.text.toString()
            val password_signin = findViewById<EditText>(R.id.password_login)
            val password_val = password_signin.text.toString()

            Log.d("LoginActivity", "Email is "+email_val)
            Log.d("LoginActivity", "Password is $password_val")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email_val, password_val)
                .addOnCompleteListener{
                    val intent = Intent(this, ChatListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    FirebaseMessaging.getInstance().subscribeToTopic("/topics/${FirebaseAuth.getInstance().uid}")
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Log.d("LoginActivity", "Error")
                }

        }

        val register_textview = findViewById<TextView>(R.id.register_textview)
        register_textview.setOnClickListener{
            finish()
        }
    }
}