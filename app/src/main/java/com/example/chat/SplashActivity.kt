package com.example.chat

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

class SplashActivity : AppCompatActivity() {

    private lateinit var app_name: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        getSupportActionBar()?.hide();

        app_name = findViewById(R.id.app_title)
        val anim: Animation = AnimationUtils.loadAnimation(this, R.anim.myanim)
        app_name.animation = anim

        Thread {
            Thread.sleep(2000)
            val intent = Intent(this, ChatListActivity::class.java)
            startActivity(intent)
            finish()
        }.start()
    }
}