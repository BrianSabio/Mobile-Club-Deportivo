package com.example.mobile_club_deportivo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.dao.UsuarioDAO
import com.example.mobile_club_deportivo.app.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: ClubDeportivoDatabase
    private lateinit var usuarioDAO: UsuarioDAO
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        session = SessionManager(this)
        
        // Si ya está logueado, saltamos al menú principal
        if (session.estaLogueado()) {
            irAlMenu()
            return
        }

        setContentView(R.layout.activity_main)

        // Inicialización usando Singleton
        dbHelper = ClubDeportivoDatabase.getInstance(this)
        usuarioDAO = UsuarioDAO(dbHelper)

        val etUsername = findViewById<EditText>(R.id.et_login_username)
        val etPassword = findViewById<EditText>(R.id.et_login_password)
        val btnLogin = findViewById<Button>(R.id.btn_login_submit)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty()) {
                etUsername.error = getString(R.string.login_error_usuario)
                etUsername.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = getString(R.string.login_error_clave)
                etPassword.requestFocus()
                return@setOnClickListener
            }

            val loginExitoso = usuarioDAO.validarUsuario(username, password)

            if (loginExitoso) {
                session.iniciarSesion(username)
                irAlMenu()
            } else {
                Toast.makeText(this, getString(R.string.login_error_credenciales), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irAlMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}