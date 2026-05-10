package com.example.mobile_club_deportivo.app

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ManageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        val btnBack = findViewById<ImageButton>(R.id.btn_manage_back)
        val btnUpdate = findViewById<Button>(R.id.btn_manage_update)
        val etSearch = findViewById<EditText>(R.id.et_manage_search)

        btnBack.setOnClickListener {
            finish()
        }

        btnUpdate.setOnClickListener {
            Toast.makeText(this, "Actualizando listado", Toast.LENGTH_SHORT).show()
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {

                val busqueda = etSearch.text.toString().trim()

                if (busqueda.isEmpty()) {
                    etSearch.error = "Ingrese un nombre"
                    etSearch.requestFocus()
                    return@setOnEditorActionListener true
                }

                // Simula búsqueda
                Toast.makeText(this, "Buscando cliente: $busqueda", Toast.LENGTH_SHORT).show()

                true
            } else {
                false
            }
        }
    }
}