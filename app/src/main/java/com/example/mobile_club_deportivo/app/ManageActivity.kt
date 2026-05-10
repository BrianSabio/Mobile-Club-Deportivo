package com.example.mobile_club_deportivo.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton

class ManageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        val btnBack = findViewById<ImageButton>(R.id.btn_manage_back)

        btnBack.setOnClickListener {
            finish()
        }
    }
}