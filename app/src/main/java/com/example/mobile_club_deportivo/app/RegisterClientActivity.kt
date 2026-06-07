package com.example.mobile_club_deportivo.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_club_deportivo.app.dao.ClienteDAO
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.Cliente
import com.example.mobile_club_deportivo.app.models.TipoCliente
import com.example.mobile_club_deportivo.app.utils.SessionManager
import java.util.*

class RegisterClientActivity : AppCompatActivity() {

    private lateinit var dbHelper: ClubDeportivoDatabase
    private lateinit var clienteDAO: ClienteDAO
    private lateinit var session: SessionManager
    private var fechaSeleccionada: String? = null

    // Clave para preservar la fecha seleccionada al rotar la pantalla
    companion object {
        private const val KEY_FECHA_NAC = "fecha_nacimiento_seleccionada"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_client)

        // Inicialización
        dbHelper   = ClubDeportivoDatabase.getInstance(this)
        clienteDAO = ClienteDAO(dbHelper)
        session    = SessionManager(this)

        // Cargar nombre real en el header
        val tvUser = findViewById<TextView>(R.id.tv_register_username)
        tvUser.text = getString(R.string.global_nombre_usuario, session.getNombreUsuario())

        val btnBack   = findViewById<ImageButton>(R.id.btn_register_back)
        val btnSubmit = findViewById<Button>(R.id.btn_register_submit)

        val etNombre   = findViewById<EditText>(R.id.et_register_nombre_apellido)
        val etDni      = findViewById<EditText>(R.id.et_register_dni)
        val etTelefono = findViewById<EditText>(R.id.et_register_telefono)
        val etEmail    = findViewById<EditText>(R.id.et_register_email)
        val etFechaNac = findViewById<EditText>(R.id.et_register_fecha_nacimiento)

        val rgTipo = findViewById<RadioGroup>(R.id.rg_register_socio)
        val cbApto = findViewById<CheckBox>(R.id.cb_register_apto_fisico)

        // Fix #14: Restaurar la fecha seleccionada si la pantalla fue rotada
        savedInstanceState?.getString(KEY_FECHA_NAC)?.let { fechaGuardada ->
            fechaSeleccionada = fechaGuardada
            etFechaNac.setText(fechaGuardada)
        }

        // Mejora UX: Deshabilitar teclado para fecha y forzar selector de calendario
        etFechaNac.isFocusable = false
        etFechaNac.isClickable = true
        etFechaNac.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year  = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day   = calendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, y, m, d ->
                val mes = String.format(Locale.US, "%02d", m + 1)
                val dia = String.format(Locale.US, "%02d", d)
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
            val dni            = etDni.text.toString().trim()
            val telefono       = etTelefono.text.toString().trim()
            val email          = etEmail.text.toString().trim()
            val aptoFisico     = cbApto.isChecked

            // 1. Validación de Nombre y Apellido (Al menos dos palabras)
            if (nombreCompleto.isEmpty() || !nombreCompleto.contains(" ")) {
                etNombre.error = getString(R.string.register_error_nombre)
                etNombre.requestFocus()
                return@setOnClickListener
            }

            // 2. Validación de DNI: longitud correcta Y contenido numérico
            // Fix #19: agregada validación numérica explícita (defensa en profundidad)
            if (dni.isEmpty() || dni.length < 7 || dni.length > 9 || !dni.all { it.isDigit() }) {
                etDni.error = getString(R.string.register_error_dni)
                etDni.requestFocus()
                return@setOnClickListener
            }

            // 3. Validación de Teléfono (Longitud mínima)
            if (telefono.isEmpty() || telefono.length < 8) {
                etTelefono.error = getString(R.string.register_error_telefono)
                etTelefono.requestFocus()
                return@setOnClickListener
            }

            // 4. Validación de Email usando el estándar de Android
            // Fix #11: reemplazado regex manual por Patterns.EMAIL_ADDRESS (más robusto y mantenido por el framework)
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = getString(R.string.register_error_email)
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // 5. Validación de Fecha de Nacimiento (OPCIONAL según documento técnico)
            // No se bloquea el registro si fechaSeleccionada es null

            // 6. REGLA DE NEGOCIO: Apto físico obligatorio para registrar
            if (!aptoFisico) {
                Toast.makeText(
                    this,
                    getString(R.string.register_error_apto),
                    Toast.LENGTH_LONG
                ).show()
                cbApto.requestFocus()
                return@setOnClickListener
            }

            // Fix #20: usar regex para separar nombre y apellido correctamente
            // Evita apellidos vacíos cuando hay múltiples espacios entre palabras
            val partes   = nombreCompleto.trim().split("\\s+".toRegex(), limit = 2)
            val nombre   = partes[0].trim()
            val apellido = if (partes.size > 1) partes[1].trim() else ""

            if (clienteDAO.existeDni(dni)) {
                etDni.error = getString(R.string.register_error_dni_existe)
                etDni.requestFocus()
                return@setOnClickListener
            }

            val tipo = if (rgTipo.checkedRadioButtonId == R.id.rb_register_socio) {
                TipoCliente.SOCIO
            } else {
                TipoCliente.NO_SOCIO
            }

            val nuevoCliente = Cliente(
                nombre          = nombre,
                apellido        = apellido,
                dni             = dni,
                telefono        = telefono,
                email           = email,
                fechaNacimiento = fechaSeleccionada,
                aptoFisico      = aptoFisico,
                tipo            = tipo
            )

            val resultadoId = clienteDAO.registrarCliente(nuevoCliente)

            if (resultadoId != -1L) {
                Toast.makeText(this, getString(R.string.register_success, resultadoId), Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.register_error_db), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Fix #14: Guarda la fecha seleccionada antes de que el Activity sea destruido
     * (por rotación de pantalla u otras causas). Se restaura en onCreate().
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_FECHA_NAC, fechaSeleccionada)
    }
}