package com.example.mobile_club_deportivo.app

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_club_deportivo.app.dao.ClienteDAO
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.Cliente
import com.example.mobile_club_deportivo.app.models.EstadoCliente
import java.text.SimpleDateFormat
import java.util.*

class ManageActivity : AppCompatActivity() {

    private lateinit var dbHelper: ClubDeportivoDatabase
    private lateinit var clienteDAO: ClienteDAO
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        // Inicialización
        dbHelper = ClubDeportivoDatabase(this)
        clienteDAO = ClienteDAO(dbHelper)
        container = findViewById(R.id.container_manage_list)

        val btnBack = findViewById<ImageButton>(R.id.btn_manage_back)
        val btnUpdate = findViewById<Button>(R.id.btn_manage_update)
        val etSearch = findViewById<EditText>(R.id.et_manage_search)

        btnBack.setOnClickListener {
            finish()
        }

        btnUpdate.setOnClickListener {
            cargarDatos()
            Toast.makeText(this, "Listado actualizado", Toast.LENGTH_SHORT).show()
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val busqueda = etSearch.text.toString().trim()
                cargarDatos(busqueda)
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
        
        // Actualizar resumen (deudores y fecha)
        actualizarResumen()

        if (clientes.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "No se encontraron clientes"
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

        // Buscamos los TextViews de resumen en el primer LinearLayout interno
        val headerLayout = (findViewById<LinearLayout>(R.id.layout_manage)).getChildAt(1) as? LinearLayout
        headerLayout?.let {
            val tvCant = it.getChildAt(2) as? TextView
            val tvFec = it.getChildAt(3) as? TextView
            tvCant?.text = "${getString(R.string.manage_tv_cantidad_deudores)} $cantDeudores"
            tvFec?.text = "${getString(R.string.manage_tv_fecha_hoy)} $fechaHoy"
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

        // Nombre y Apellido
        val tvNombre = TextView(this)
        tvNombre.text = "${cliente.nombre} ${cliente.apellido}"
        tvNombre.textSize = 20f
        tvNombre.setTypeface(null, Typeface.BOLD)
        tvNombre.setPadding(0, 0, 0, 16)
        layout.addView(tvNombre)

        // Datos
        layout.addView(crearDatoText("DNI: ${cliente.dni}"))
        layout.addView(crearDatoText("Tel: ${cliente.telefono}"))
        layout.addView(crearDatoText("Email: ${cliente.email}"))
        
        if (cliente.numeroSocio != null) {
            layout.addView(crearDatoText("Nro Socio: ${cliente.numeroSocio}"))
        }
        
        val tvEstado = crearDatoText("Estado: ${cliente.estado}")
        if (cliente.estado == EstadoCliente.INACTIVO) {
            tvEstado.setTextColor(Color.RED)
            tvEstado.setTypeface(null, Typeface.BOLD)
        }
        layout.addView(tvEstado)

        // Botones de acción
        val buttonLayout = LinearLayout(this)
        buttonLayout.orientation = LinearLayout.HORIZONTAL
        buttonLayout.gravity = Gravity.END
        buttonLayout.setPadding(0, 16, 0, 0)

        val btnCarnet = Button(this)
        btnCarnet.text = "Carnet"
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
}