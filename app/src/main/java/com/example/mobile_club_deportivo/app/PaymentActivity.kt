package com.example.mobile_club_deportivo.app

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_club_deportivo.app.dao.ClienteDAO
import com.example.mobile_club_deportivo.app.dao.CobroDAO
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.*
import com.example.mobile_club_deportivo.app.utils.AppConfig
import com.example.mobile_club_deportivo.app.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var dbHelper: ClubDeportivoDatabase
    private lateinit var clienteDAO: ClienteDAO
    private lateinit var cobroDAO: CobroDAO
    private lateinit var session: SessionManager
    
    private var clienteActual: Cliente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Inicialización usando Singleton
        dbHelper = ClubDeportivoDatabase.getInstance(this)
        clienteDAO = ClienteDAO(dbHelper)
        cobroDAO = CobroDAO(dbHelper)
        session = SessionManager(this)

        // Cargar nombre real en el header
        val tvUser = findViewById<TextView>(R.id.tv_payment_user)
        tvUser.text = getString(R.string.global_nombre_usuario, session.getNombreUsuario())

        val etSearch = findViewById<EditText>(R.id.et_payment_search)
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
                ocultarTeclado()
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
        (layout.getChildAt(1) as TextView).text = getString(R.string.payment_tv_dni, cliente.dni)
        (layout.getChildAt(2) as TextView).text = getString(R.string.payment_tv_nro_socio, cliente.numeroSocio.toString())
        (layout.getChildAt(3) as TextView).text = getString(R.string.payment_tv_estado, cliente.estado.name)
        
        // Uso de AppConfig para montos profesionales
        (layout.getChildAt(4) as TextView).text = getString(R.string.payment_tv_deuda_pendiente, AppConfig.MONTO_CUOTA_SOCIO.toString())
    }

    private fun actualizarVistaNoSocio(cliente: Cliente) {
        findViewById<TextView>(R.id.tv_payment_non_member_name).text = "${cliente.nombre} ${cliente.apellido}"
        val layout = findViewById<LinearLayout>(R.id.layout_payment_non_member)
        (layout.getChildAt(1) as TextView).text = getString(R.string.payment_tv_dni, cliente.dni)
        (layout.getChildAt(2) as TextView).text = getString(R.string.payment_tv_telefono, cliente.telefono)
        
        (layout.getChildAt(3) as TextView).text = getString(R.string.payment_tv_actividad_gimnasio, "Gimnasio (Mensual)")
        (layout.getChildAt(4) as TextView).text = getString(R.string.payment_tv_monto_pagar, AppConfig.MONTO_ACTIVIDAD_NO_SOCIO.toString())
    }

    private fun realizarCobro() {
        val cliente = clienteActual ?: return
        
        // Determinar método de pago seleccionado
        val rgMethod = if (cliente.tipo == TipoCliente.SOCIO) {
            findViewById<RadioGroup>(R.id.rg_payment_method_member)
        } else {
            findViewById<RadioGroup>(R.id.rg_payment_method_non_member)
        }

        val medioPago = if (rgMethod.checkedRadioButtonId == R.id.rb_cash_member || 
            rgMethod.checkedRadioButtonId == R.id.rb_cash_non_member) {
            getString(R.string.payment_rb_efectivo)
        } else {
            getString(R.string.payment_rb_tarjeta)
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaHoy = sdf.format(Date())
        
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        val fechaVenc = sdf.format(cal.time)

        val monto = if (cliente.tipo == TipoCliente.SOCIO) AppConfig.MONTO_CUOTA_SOCIO else AppConfig.MONTO_ACTIVIDAD_NO_SOCIO
        val desc = if (cliente.tipo == TipoCliente.SOCIO) AppConfig.DESC_CUOTA_SOCIO else AppConfig.DESC_ACTIVIDAD_GYM
        
        val nuevoCobro = Cobro(
            idCliente = cliente.idCliente,
            monto = monto,
            fechaPago = fechaHoy,
            fechaVencimiento = fechaVenc,
            medioPago = medioPago, // Usamos el medio seleccionado
            totalCuotas = 1,
            numeroCuota = 1,
            estado = EstadoCobro.PAGADO,
            descripcion = desc
        )

        val id = cobroDAO.registrarCobro(nuevoCobro)
        if (id != -1L) {
            Toast.makeText(this, "Cobro registrado con éxito via $medioPago. ID: $id", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Error al procesar el cobro", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ocultarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}