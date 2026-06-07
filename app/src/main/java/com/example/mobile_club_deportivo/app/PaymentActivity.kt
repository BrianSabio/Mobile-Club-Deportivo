package com.example.mobile_club_deportivo.app

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
                    etSearch.error = getString(R.string.payment_error_busqueda)
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
            Toast.makeText(this, getString(R.string.payment_success_busqueda), Toast.LENGTH_SHORT).show()
        } else {
            layoutSocio.visibility = View.GONE
            layoutNoSocio.visibility = View.GONE
            Toast.makeText(this, getString(R.string.payment_error_no_encontrado), Toast.LENGTH_LONG).show()
        }
    }

    private fun actualizarVistaSocio(cliente: Cliente) {
        findViewById<TextView>(R.id.tv_payment_member_name).text = getString(R.string.payment_tv_nombre_socio, "${cliente.nombre} ${cliente.apellido}")
        // Acceso por ID explícito: seguro ante cambios de orden en el XML
        findViewById<TextView>(R.id.tv_payment_member_dni).text   = getString(R.string.payment_tv_dni, cliente.dni)
        findViewById<TextView>(R.id.tv_payment_member_nro).text   = getString(R.string.payment_tv_nro_socio, cliente.numeroSocio.toString())

        // Lógica de deuda real
        val tieneDeuda  = cobroDAO.tieneDeuda(cliente.idCliente)
        val tvEstado    = findViewById<TextView>(R.id.tv_payment_member_estado)
        val tvDeuda     = findViewById<TextView>(R.id.tv_payment_member_deuda)
        
        if (tieneDeuda) {
            tvEstado.text = getString(R.string.payment_tv_con_deuda)
            // Fix #12b: usar color semántico con buen contraste en lugar de Color.RED puro
            tvEstado.setTextColor(ContextCompat.getColor(this, R.color.estado_inactivo))
            tvDeuda.text = getString(R.string.payment_tv_deuda_pendiente, AppConfig.MONTO_CUOTA_SOCIO.toString())
            findViewById<Button>(R.id.btn_payment_pay_fee).isEnabled = true
        } else {
            tvEstado.text = getString(R.string.payment_tv_sin_deuda)
            // Fix #12b: usar color semántico con buen contraste en lugar de Color.GREEN puro
            tvEstado.setTextColor(ContextCompat.getColor(this, R.color.estado_activo))
            tvDeuda.text = getString(R.string.payment_tv_sin_deuda_desc)
            findViewById<Button>(R.id.btn_payment_pay_fee).isEnabled = false
        }
    }

    private fun actualizarVistaNoSocio(cliente: Cliente) {
        findViewById<TextView>(R.id.tv_payment_non_member_name).text = getString(R.string.payment_tv_nombre_no_socio, "${cliente.nombre} ${cliente.apellido}")
        // Acceso por ID explícito: seguro ante cambios de orden en el XML
        findViewById<TextView>(R.id.tv_payment_non_member_dni).text      = getString(R.string.payment_tv_dni, cliente.dni)
        findViewById<TextView>(R.id.tv_payment_non_member_tel).text      = getString(R.string.payment_tv_telefono, cliente.telefono)
        // Un No Socio siempre paga por actividad cada vez
        // Se usa AppConfig.DESC_ACTIVIDAD_GYM como fuente única de verdad
        findViewById<TextView>(R.id.tv_payment_non_member_actividad).text = getString(R.string.payment_tv_actividad_gimnasio, AppConfig.DESC_ACTIVIDAD_GYM)
        findViewById<TextView>(R.id.tv_payment_non_member_monto).text    = getString(R.string.payment_tv_monto_pagar, AppConfig.MONTO_ACTIVIDAD_NO_SOCIO.toString())
    }

    private fun realizarCobro() {
        val cliente = clienteActual ?: return
        
        // Determinar método de pago seleccionado con 3 opciones
        val rgMethod = if (cliente.tipo == TipoCliente.SOCIO) {
            findViewById<RadioGroup>(R.id.rg_payment_method_member)
        } else {
            findViewById<RadioGroup>(R.id.rg_payment_method_non_member)
        }

        val medioPago = when (rgMethod.checkedRadioButtonId) {
            R.id.rb_cash_member, R.id.rb_cash_non_member -> getString(R.string.payment_rb_efectivo)
            R.id.rb_debit_member, R.id.rb_debit_non_member -> getString(R.string.payment_rb_debito)
            else -> getString(R.string.payment_rb_credito)
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaHoy = sdf.format(Date())
        
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        val fechaVenc = sdf.format(cal.time)

        // Lógica profesional basada en AppConfig
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
            Toast.makeText(this, getString(R.string.payment_success_cobro, medioPago, id), Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, getString(R.string.payment_error_cobro), Toast.LENGTH_SHORT).show()
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