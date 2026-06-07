package com.example.mobile_club_deportivo.app

import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_club_deportivo.app.dao.CarnetDAO
import com.example.mobile_club_deportivo.app.dao.ClienteDAO
import com.example.mobile_club_deportivo.app.dao.CobroDAO
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.Carnet
import com.example.mobile_club_deportivo.app.models.Cliente
import com.example.mobile_club_deportivo.app.utils.SessionManager
import java.io.OutputStream

class CarnetActivity : AppCompatActivity() {

    private lateinit var dbHelper: ClubDeportivoDatabase
    private lateinit var clienteDAO: ClienteDAO
    private lateinit var cobroDAO: CobroDAO
    private lateinit var carnetDAO: CarnetDAO
    private lateinit var session: SessionManager
    private var clienteActual: Cliente? = null

    // Código de solicitud de permiso para escritura en almacenamiento
    companion object {
        private const val REQUEST_CODE_STORAGE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carnet)

        // Inicialización Singleton
        dbHelper   = ClubDeportivoDatabase.getInstance(this)
        clienteDAO = ClienteDAO(dbHelper)
        cobroDAO   = CobroDAO(dbHelper)
        carnetDAO  = CarnetDAO(dbHelper)
        session    = SessionManager(this)

        // Fix #5b: usar obtenerClientePorId() en vez de obtenerClientes().find{}
        // Evita cargar toda la tabla en memoria para encontrar un solo registro.
        val idCliente = intent.getIntExtra("ID_CLIENTE", -1)
        if (idCliente != -1) {
            clienteActual = clienteDAO.obtenerClientePorId(idCliente)
        }

        if (clienteActual == null) {
            // Fix #9: usar string de strings.xml en lugar de texto hardcodeado
            Toast.makeText(this, getString(R.string.carnet_error_cliente), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mostrarDatos()

        // Botón "Emitir Carnet": registra/actualiza la fila en la tabla CARNET
        // (decisión del usuario: acción explícita, no automática)
        findViewById<Button>(R.id.btn_carnet_emitir).setOnClickListener {
            emitirCarnet()
        }

        // Botón "Descargar PDF": genera el PDF del carnet visual
        findViewById<Button>(R.id.btn_carnet_download).setOnClickListener {
            verificarPermisoYGenerarPDF()
        }
    }

    private fun mostrarDatos() {
        val cliente = clienteActual!!
        findViewById<TextView>(R.id.tv_carnet_nombre).text = "${cliente.nombre} ${cliente.apellido}"
        findViewById<TextView>(R.id.tv_carnet_dni).text    = getString(R.string.carnet_tv_dni, cliente.dni)
        findViewById<TextView>(R.id.tv_carnet_tipo).text   = getString(R.string.carnet_tv_tipo, cliente.tipo.name)
        findViewById<TextView>(R.id.tv_carnet_tel).text    = getString(R.string.carnet_tv_tel, cliente.telefono)
        findViewById<TextView>(R.id.tv_carnet_email).text  = getString(R.string.carnet_tv_email, cliente.email)

        // Número de socio solo si aplica
        val tvNro = findViewById<TextView>(R.id.tv_carnet_nro)
        if (cliente.numeroSocio != null) {
            tvNro.text = getString(R.string.carnet_tv_nro_socio, cliente.numeroSocio.toString())
            tvNro.visibility = View.VISIBLE
        } else {
            tvNro.visibility = View.GONE
        }

        // Fecha de emisión (hoy)
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val hoy = sdf.format(java.util.Date())
        findViewById<TextView>(R.id.tv_carnet_emision).text = getString(R.string.carnet_tv_emision, hoy)

        // Vencimiento según último cobro registrado
        val ultimoCobro = cobroDAO.obtenerUltimoCobro(cliente.idCliente)
        val vencimientoRaw = ultimoCobro?.fechaVencimiento ?: "N/A"

        // Formatear de yyyy-MM-dd a dd/MM/yyyy para mostrar al usuario
        val vencimientoFormateado = try {
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val date = parser.parse(vencimientoRaw)
            if (date != null) sdf.format(date) else vencimientoRaw
        } catch (e: Exception) {
            vencimientoRaw
        }

        findViewById<TextView>(R.id.tv_carnet_vencimiento).text =
            getString(R.string.carnet_tv_vencimiento, vencimientoFormateado)
    }

    /**
     * Registra o actualiza la emisión del carnet en la tabla CARNET de la base de datos.
     * Solo se ejecuta al presionar el botón "Emitir Carnet" (acción explícita del operador).
     */
    private fun emitirCarnet() {
        val cliente = clienteActual ?: return
        val sdfISO = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val hoyISO = sdfISO.format(java.util.Date())
        val ultimoCobro = cobroDAO.obtenerUltimoCobro(cliente.idCliente)

        val carnet = Carnet(
            idCliente        = cliente.idCliente,
            fechaEmision     = hoyISO,
            fechaVencimiento = ultimoCobro?.fechaVencimiento ?: hoyISO,
            activo           = true
        )
        val id = carnetDAO.registrarOActualizarCarnet(carnet)
        if (id != -1L) {
            Toast.makeText(this, getString(R.string.carnet_success_emision), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.carnet_error_emision), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Verifica si se tiene el permiso de escritura antes de generar el PDF.
     * En Android 10+ (API 29) no se necesita permiso para escribir en Downloads vía MediaStore.
     * En Android 7-9 (API 24-28) sí se requiere WRITE_EXTERNAL_STORAGE.
     */
    private fun verificarPermisoYGenerarPDF() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android 7, 8 y 9 necesitan permiso explícito de escritura
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE
                )
                return
            }
        }
        // Android 10+ o permiso ya concedido: generar directamente
        generarPDF()
    }

    /**
     * Callback del sistema tras la solicitud de permiso en runtime.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Permiso concedido: proceder con la generación del PDF
            generarPDF()
        } else {
            // Fix #9: usar string de strings.xml en lugar de texto hardcodeado
            Toast.makeText(this, getString(R.string.carnet_error_permiso), Toast.LENGTH_LONG).show()
        }
    }

    private fun generarPDF() {
        val view = findViewById<View>(R.id.layout_carnet_card)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
        val page = document.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        view.draw(canvas)
        document.finishPage(page)

        val fileName = "Carnet_${clienteActual?.dni}.pdf"

        try {
            val outputStream: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: usar MediaStore (no requiere permiso)
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                outputStream = uri?.let { contentResolver.openOutputStream(it) }
            } else {
                // Android 7-9: escritura directa con permiso previamente concedido
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(path, fileName)
                outputStream = java.io.FileOutputStream(file)
            }

            outputStream?.use {
                document.writeTo(it)
                // Fix #9: usar string de strings.xml en lugar de texto hardcodeado
                Toast.makeText(this, getString(R.string.carnet_success_pdf), Toast.LENGTH_LONG).show()
            }
            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fix #9: usar string de strings.xml con placeholder para el mensaje de error
            Toast.makeText(this, getString(R.string.carnet_error_pdf, e.message), Toast.LENGTH_SHORT).show()
        }
    }
}