package com.example.mobile_club_deportivo.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_club_deportivo.app.dao.ClienteDAO
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.Cliente
import com.example.mobile_club_deportivo.app.models.TipoCliente
import java.util.*

class RegisterClientActivity : AppCompatActivity() {

    private lateinit var dbHelper: ClubDeportivoDatabase
    private lateinit var clienteDAO: ClienteDAO
    private var fechaSeleccionada: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_client)

        // Inicialización
        dbHelper = ClubDeportivoDatabase(this)
        clienteDAO = ClienteDAO(dbHelper)

        val btnBack = findViewById<ImageButton>(R.id.btn_register_back)
        val btnSubmit = findViewById<Button>(R.id.btn_register_submit)
        
        val etNombre = findViewById<EditText>(R.id.et_register_nombre_apellido)
        val etDni = findViewById<EditText>(R.id.et_register_dni)
        val etTelefono = findViewById<EditText>(R.id.et_register_telefono)
        val etEmail = findViewById<EditText>(R.id.et_register_email)
        val etFechaNac = findViewById<EditText>(R.id.et_register_fecha_nacimiento)
        
        val rgTipo = findViewById<RadioGroup>(R.id.rg_register_socio)
        val cbApto = findViewById<CheckBox>(R.id.cb_register_apto_fisico)

        // Configuración de DatePicker para fecha de nacimiento
        etFechaNac.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, y, m, d ->
                // Formato ISO-8601: YYYY-MM-DD
                val mes = String.format("%02d", m + 1)
                val dia = String.format("%02d", d)
                fechaSeleccionada = "$y-$mes-$dia"
                etFechaNac.setText(fechaSeleccionada)
            }, year, month, day)
            dpd.show()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            val nombreCompleto = etNombre.text.toString().trim()
            val dni = etDni.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val aptoFisico = cbApto.isChecked

            // Validaciones
            if (nombreCompleto.isEmpty()) {
                etNombre.error = "Ingrese el nombre completo"
                etNombre.requestFocus()
                return@setOnClickListener
            }

            if (dni.isEmpty()) {
                etDni.error = "El DNI es obligatorio"
                etDni.requestFocus()
                return@setOnClickListener
            }

            if (telefono.isEmpty()) {
                etTelefono.error = "El teléfono es obligatorio"
                etTelefono.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "El email es obligatorio"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // Separar nombre y apellido (suponiendo primer espacio como separador simple)
            val partes = nombreCompleto.split(" ", limit = 2)
            val nombre = partes[0]
            val apellido = if (partes.size > 1) partes[1] else ""

            // Verificar si el DNI ya existe
            if (clienteDAO.existeDni(dni)) {
                etDni.error = "Este DNI ya se encuentra registrado"
                etDni.requestFocus()
                return@setOnClickListener
            }

            // Determinar tipo de cliente
            val tipo = if (rgTipo.checkedRadioButtonId == R.id.rb_register_socio) {
                TipoCliente.SOCIO
            } else {
                TipoCliente.NO_SOCIO
            }

            // Crear objeto cliente
            val nuevoCliente = Cliente(
                nombre = nombre,
                apellido = apellido,
                dni = dni,
                telefono = telefono,
                email = email,
                fechaNacimiento = fechaSeleccionada,
                aptoFisico = aptoFisico,
                tipo = tipo
            )

            // Guardar en DB
            val resultadoId = clienteDAO.registrarCliente(nuevoCliente)

            if (resultadoId != -1L) {
                Toast.makeText(
                    this,
                    "Cliente registrado con éxito (ID: $resultadoId)",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Error al guardar en la base de datos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}