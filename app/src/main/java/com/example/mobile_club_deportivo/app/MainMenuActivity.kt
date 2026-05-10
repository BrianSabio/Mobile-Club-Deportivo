package com.example.mobile_club_deportivo.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val btnRegister = findViewById<Button>(R.id.btn_main_register)
        val btnPayment = findViewById<Button>(R.id.btn_main_payment)
        val btnManage = findViewById<Button>(R.id.btn_main_manage)
        val btnLogout = findViewById<Button>(R.id.btn_main_logout)

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterClientActivity::class.java)
            startActivity(intent)

        }

        btnPayment.setOnClickListener {

        }

        btnManage.setOnClickListener {
            val intent = Intent(this, ManageActivity::class.java)
            startActivity(intent)

        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }
    }
}