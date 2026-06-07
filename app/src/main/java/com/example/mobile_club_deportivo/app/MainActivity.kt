package com.example.mobile_club_deportivo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.dao.UsuarioDAO

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: ClubDeportivoDatabase
    private lateinit var usuarioDAO: UsuarioDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de la base de datos y el DAO
        dbHelper = ClubDeportivoDatabase(this)
        usuarioDAO = UsuarioDAO(dbHelper)

        // Vinculación de vistas
        val etUsername = findViewById<EditText>(R.id.et_login_username)
        val etPassword = findViewById<EditText>(R.id.et_login_password)
        val btnLogin = findViewById<Button>(R.id.btn_login_submit)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validaciones básicas de entrada
            if (username.isEmpty()) {
                etUsername.error = "El usuario es obligatorio"
                etUsername.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "La contraseña es obligatoria"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Validación contra la base de datos real
            val loginExitoso = usuarioDAO.validarUsuario(username, password)

            if (loginExitoso) {
                // Navegación al menú principal
                val intent = Intent(this, MainMenuActivity::class.java)
                startActivity(intent)
                finish() // Finaliza MainActivity para no volver atrás al Login
            } else {
                // Feedback de error
                Toast.makeText(
                    this,
                    "Usuario o contraseña incorrectos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}