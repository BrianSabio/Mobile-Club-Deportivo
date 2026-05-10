package com.example.mobile_club_deportivo.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val etSearch = findViewById<EditText>(R.id.et_payment_search)
        val layoutSocio = findViewById<LinearLayout>(R.id.layout_payment_member)
        val layoutNoSocio = findViewById<LinearLayout>(R.id.layout_payment_non_member)
        val btnBack = findViewById<ImageButton>(R.id.btn_payment_back)

        btnBack.setOnClickListener {
            finish()
        }

        etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {

                val busqueda = etSearch.text.toString().trim()

                if (busqueda.isEmpty()) {
                    etSearch.error = "Por favor, ingrese un número"
                    return@setOnEditorActionListener true // Cortamos la ejecución aquí
                }

                // Simula búsqueda
                when (busqueda) {
                    "123" -> {
                        // Simula Socio encontrado
                        layoutSocio.visibility = View.VISIBLE
                        layoutNoSocio.visibility = View.GONE
                        Toast.makeText(this, "Socio identificado", Toast.LENGTH_SHORT).show()
                    }
                    "456" -> {
                        // Simula No Socio encontrado
                        layoutSocio.visibility = View.GONE
                        layoutNoSocio.visibility = View.VISIBLE
                        Toast.makeText(this, "Cliente No Socio identificado", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        layoutSocio.visibility = View.GONE
                        layoutNoSocio.visibility = View.GONE
                        Toast.makeText(this, "Cliente no encontrado", Toast.LENGTH_LONG).show()
                    }
                }
                true
            } else {
                false
            }
        }

        findViewById<Button>(R.id.btn_payment_pay_fee).setOnClickListener {
            Toast.makeText(this, "No se pudo generar el cobro", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_payment_pay_activity).setOnClickListener {
            Toast.makeText(this, "No se pudo generar el cobro", Toast.LENGTH_SHORT).show()
        }
    }
}