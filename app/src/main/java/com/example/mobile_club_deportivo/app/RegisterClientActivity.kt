package com.example.mobile_club_deportivo.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterClientActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_client)

        val btnBack = findViewById<ImageButton>(R.id.btn_register_back)
        val btnSubmit = findViewById<Button>(R.id.btn_register_submit)
        val etNombre = findViewById<EditText>(R.id.et_register_nombre_apellido)
        val etDni = findViewById<EditText>(R.id.et_register_dni)

        btnBack.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val dni = etDni.text.toString().trim()

            if (nombre.isEmpty()) {
                etNombre.error = "Ingrese el nombre completo"
                etNombre.requestFocus()
                return@setOnClickListener
            }

            if (dni.isEmpty()) {
                etDni.error = "El número de documento es obligatorio"
                etDni.requestFocus()
                return@setOnClickListener
            }

            if (dni.length < 7) {
                etDni.error = "Ingrese un número de DNI válido"
                etDni.requestFocus()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Cliente $nombre registrado exitosamente",
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }
}