package com.example.mobile_club_deportivo.app

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_club_deportivo.app.dao.ClienteDAO
import com.example.mobile_club_deportivo.app.dao.CobroDAO
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.*
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var dbHelper: ClubDeportivoDatabase
    private lateinit var clienteDAO: ClienteDAO
    private lateinit var cobroDAO: CobroDAO
    
    private var clienteActual: Cliente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Inicialización
        dbHelper = ClubDeportivoDatabase(this)
        clienteDAO = ClienteDAO(dbHelper)
        cobroDAO = CobroDAO(dbHelper)

        val etSearch = findViewById<EditText>(R.id.et_payment_search)
        val layoutSocio = findViewById<LinearLayout>(R.id.layout_payment_member)
        val layoutNoSocio = findViewById<LinearLayout>(R.id.layout_payment_non_member)
        val btnBack = findViewById<ImageButton>(R.id.btn_payment_back)

        btnBack.setOnClickListener {
            finish()
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val busqueda = etSearch.text.toString().trim()
                if (busqueda.isEmpty()) {
                    etSearch.error = "Ingrese un DNI o Nro Socio"
                    return@setOnEditorActionListener true
                }
                buscarCliente(busqueda)
                true
            } else {
                false
            }
        }

        findViewById<Button>(R.id.btn_payment_pay_fee).setOnClickListener {
            realizarCobro()
        }

        findViewById<Button>(R.id.btn_payment_pay_activity).setOnClickListener {
            realizarCobro()
        }
    }

    private fun buscarCliente(criterio: String) {
        val layoutSocio = findViewById<LinearLayout>(R.id.layout_payment_member)
        val layoutNoSocio = findViewById<LinearLayout>(R.id.layout_payment_non_member)
        
        clienteActual = clienteDAO.buscarPorCriterio(criterio)

        if (clienteActual != null) {
            val cliente = clienteActual!!
            if (cliente.tipo == TipoCliente.SOCIO) {
                layoutSocio.visibility = View.VISIBLE
                layoutNoSocio.visibility = View.GONE
                actualizarVistaSocio(cliente)
            } else {
                layoutSocio.visibility = View.GONE
                layoutNoSocio.visibility = View.VISIBLE
                actualizarVistaNoSocio(cliente)
            }
            Toast.makeText(this, "Cliente encontrado", Toast.LENGTH_SHORT).show()
        } else {
            layoutSocio.visibility = View.GONE
            layoutNoSocio.visibility = View.GONE
            Toast.makeText(this, "Cliente no encontrado", Toast.LENGTH_LONG).show()
        }
    }

    private fun actualizarVistaSocio(cliente: Cliente) {
        findViewById<TextView>(R.id.tv_payment_member_name).text = "${cliente.nombre} ${cliente.apellido}"
        val layout = findViewById<LinearLayout>(R.id.layout_payment_member)
        (layout.getChildAt(1) as TextView).text = "${getString(R.string.payment_tv_dni)} ${cliente.dni}"
        (layout.getChildAt(2) as TextView).text = "${getString(R.string.payment_tv_nro_socio)} ${cliente.numeroSocio}"
        (layout.getChildAt(3) as TextView).text = "${getString(R.string.payment_tv_estado)} ${cliente.estado}"
        (layout.getChildAt(4) as TextView).text = "${getString(R.string.payment_tv_deuda_pendiente)} $1500.00"
    }

    private fun actualizarVistaNoSocio(cliente: Cliente) {
        findViewById<TextView>(R.id.tv_payment_non_member_name).text = "${cliente.nombre} ${cliente.apellido}"
        val layout = findViewById<LinearLayout>(R.id.layout_payment_non_member)
        (layout.getChildAt(1) as TextView).text = "${getString(R.string.payment_tv_dni)} ${cliente.dni}"
        (layout.getChildAt(2) as TextView).text = "${getString(R.string.payment_tv_telefono)} ${cliente.telefono}"
        (layout.getChildAt(3) as TextView).text = "${getString(R.string.payment_tv_actividad_gimnasio)} Gimnasio (Mensual)"
        (layout.getChildAt(4) as TextView).text = "${getString(R.string.payment_tv_monto_pagar)} $2500.00"
    }

    private fun realizarCobro() {
        val cliente = clienteActual ?: return
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaHoy = sdf.format(Date())
        
        // Calculamos vencimiento (1 mes a partir de hoy para este ejemplo)
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        val fechaVenc = sdf.format(cal.time)

        val monto = if (cliente.tipo == TipoCliente.SOCIO) 1500.0 else 2500.0
        
        val nuevoCobro = Cobro(
            idCliente = cliente.idCliente,
            monto = monto,
            fechaPago = fechaHoy,
            fechaVencimiento = fechaVenc,
            medioPago = "Efectivo",
            totalCuotas = 1,
            numeroCuota = 1,
            estado = EstadoCobro.PAGADO,
            descripcion = if (cliente.tipo == TipoCliente.SOCIO) "Cuota Social" else "Actividad Gimnasio"
        )

        val id = cobroDAO.registrarCobro(nuevoCobro)
        if (id != -1L) {
            Toast.makeText(this, "Cobro registrado con éxito. ID: $id", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Error al procesar el cobro", Toast.LENGTH_SHORT).show()
        }
    }
}