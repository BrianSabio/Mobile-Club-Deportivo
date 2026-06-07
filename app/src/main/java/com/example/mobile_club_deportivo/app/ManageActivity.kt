package com.example.mobile_club_deportivo.app

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_club_deportivo.app.dao.ClienteDAO
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.Cliente
import com.example.mobile_club_deportivo.app.models.EstadoCliente
import com.example.mobile_club_deportivo.app.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class ManageActivity : AppCompatActivity() {

    private lateinit var dbHelper: ClubDeportivoDatabase
    private lateinit var clienteDAO: ClienteDAO
    private lateinit var container: LinearLayout
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        // Inicialización
        dbHelper = ClubDeportivoDatabase.getInstance(this)
        clienteDAO = ClienteDAO(dbHelper)
        container = findViewById(R.id.container_manage_list)
        session = SessionManager(this)

        // Cargar nombre real en el header
        val tvUser = findViewById<TextView>(R.id.tv_manage_username)
        tvUser.text = getString(R.string.global_nombre_usuario, session.getNombreUsuario())

        val btnBack = findViewById<ImageButton>(R.id.btn_manage_back)
        val btnUpdate = findViewById<Button>(R.id.btn_manage_update)
        val etSearch = findViewById<EditText>(R.id.et_manage_search)

        btnBack.setOnClickListener {
            finish()
        }

        btnUpdate.setOnClickListener {
            cargarDatos()
            Toast.makeText(this, getString(R.string.manage_toast_actualizado), Toast.LENGTH_SHORT).show()
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val busqueda = etSearch.text.toString().trim()
                cargarDatos(busqueda)
                ocultarTeclado() // Mejora UX: Ocultar teclado tras búsqueda
                true
            } else {
                false
            }
        }

        cargarDatos()
    }

    private fun cargarDatos(busqueda: String? = null) {
        container.removeAllViews()
        val clientes = clienteDAO.obtenerClientes(busqueda)
        
        actualizarResumen()

        if (clientes.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = getString(R.string.manage_error_vacio)
            tvEmpty.gravity = Gravity.CENTER
            tvEmpty.setPadding(0, 50, 0, 0)
            container.addView(tvEmpty)
            return
        }

        for (cliente in clientes) {
            val itemLayout = crearItemCliente(cliente)
            container.addView(itemLayout)
        }
    }

    private fun actualizarResumen() {
        val cantDeudores = clienteDAO.contarDeudores()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaHoy = sdf.format(Date())

        val headerLayout = (findViewById<LinearLayout>(R.id.layout_manage)).getChildAt(1) as? LinearLayout
        headerLayout?.let {
            val tvCant = it.getChildAt(2) as? TextView
            val tvFec = it.getChildAt(3) as? TextView
            tvCant?.text = getString(R.string.manage_tv_cantidad_deudores, cantDeudores)
            tvFec?.text = getString(R.string.manage_tv_fecha_hoy, fechaHoy)
        }
    }

    private fun crearItemCliente(cliente: Cliente): LinearLayout {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        layout.setBackgroundResource(R.drawable.bg_contenedor_borde)
        
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 24)
        layout.layoutParams = params

        val tvNombre = TextView(this)
        tvNombre.text = getString(R.string.payment_tv_nombre_socio, "${cliente.nombre} ${cliente.apellido}")
        tvNombre.textSize = 20f
        tvNombre.setTypeface(null, Typeface.BOLD)
        tvNombre.setPadding(0, 0, 0, 16)
        layout.addView(tvNombre)

        // Uso de strings con placeholders
        layout.addView(crearDatoText(getString(R.string.manage_tv_dni, cliente.dni)))
        layout.addView(crearDatoText(getString(R.string.manage_tv_tel, cliente.telefono)))
        
        if (cliente.numeroSocio != null) {
            layout.addView(crearDatoText(getString(R.string.manage_tv_numero_socio, cliente.numeroSocio.toString())))
        }
        
        val tvEstado = crearDatoText(getString(R.string.manage_tv_estado, cliente.estado.name))
        if (cliente.estado == EstadoCliente.INACTIVO) {
            tvEstado.setTextColor(Color.RED)
            tvEstado.setTypeface(null, Typeface.BOLD)
        }
        layout.addView(tvEstado)

        val buttonLayout = LinearLayout(this)
        buttonLayout.orientation = LinearLayout.HORIZONTAL
        buttonLayout.gravity = Gravity.END
        buttonLayout.setPadding(0, 16, 0, 0)

        val btnCarnet = Button(this)
        btnCarnet.text = getString(R.string.manage_btn_carnet)
        btnCarnet.setOnClickListener {
            val intent = android.content.Intent(this, CarnetActivity::class.java)
            intent.putExtra("ID_CLIENTE", cliente.idCliente)
            startActivity(intent)
        }
        buttonLayout.addView(btnCarnet)

        layout.addView(buttonLayout)
        return layout
    }

    private fun crearDatoText(texto: String): TextView {
        val tv = TextView(this)
        tv.text = texto
        tv.textSize = 14f
        tv.setPadding(0, 0, 0, 4)
        return tv
    }

    private fun ocultarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}