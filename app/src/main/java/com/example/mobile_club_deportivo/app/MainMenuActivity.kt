package com.example.mobile_club_deportivo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_club_deportivo.app.utils.SessionManager

class MainMenuActivity : AppCompatActivity() {
    
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        
        session = SessionManager(this)
        
        // Cargar nombre de usuario real
        val tvUser = findViewById<TextView>(R.id.tv_register_username)
        tvUser.text = getString(R.string.global_nombre_usuario, session.getNombreUsuario())

        val btnRegister = findViewById<Button>(R.id.btn_main_register)
        val btnPayment = findViewById<Button>(R.id.btn_main_payment)
        val btnManage = findViewById<Button>(R.id.btn_main_manage)
        val btnLogout = findViewById<Button>(R.id.btn_main_logout)

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterClientActivity::class.java)
            startActivity(intent)
        }

        btnPayment.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }

        btnManage.setOnClickListener {
            val intent = Intent(this, ManageActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            session.cerrarSesion()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}