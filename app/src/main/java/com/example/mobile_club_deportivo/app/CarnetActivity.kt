package com.example.mobile_club_deportivo.app

import android.content.ContentValues
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
import com.example.mobile_club_deportivo.app.dao.ClienteDAO
import com.example.mobile_club_deportivo.app.dao.CobroDAO
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.Cliente
import com.example.mobile_club_deportivo.app.utils.SessionManager
import java.io.OutputStream

class CarnetActivity : AppCompatActivity() {

    private lateinit var dbHelper: ClubDeportivoDatabase
    private lateinit var clienteDAO: ClienteDAO
    private lateinit var cobroDAO: CobroDAO
    private lateinit var session: SessionManager
    private var clienteActual: Cliente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carnet)

        // Inicialización Singleton
        dbHelper = ClubDeportivoDatabase.getInstance(this)
        clienteDAO = ClienteDAO(dbHelper)
        cobroDAO = CobroDAO(dbHelper)
        session = SessionManager(this)

        val idCliente = intent.getIntExtra("ID_CLIENTE", -1)
        if (idCliente != -1) {
            clienteActual = clienteDAO.obtenerClientes().find { it.idCliente == idCliente }
        }

        if (clienteActual == null) {
            Toast.makeText(this, "Error al cargar datos del cliente", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mostrarDatos()

        findViewById<Button>(R.id.btn_carnet_download).setOnClickListener {
            generarPDF()
        }
    }

    private fun mostrarDatos() {
        val cliente = clienteActual!!
        findViewById<TextView>(R.id.tv_carnet_nombre).text = "${cliente.nombre} ${cliente.apellido}"
        findViewById<TextView>(R.id.tv_carnet_dni).text = getString(R.string.carnet_tv_dni, cliente.dni)
        findViewById<TextView>(R.id.tv_carnet_tipo).text = getString(R.string.carnet_tv_tipo, cliente.tipo.name)
        findViewById<TextView>(R.id.tv_carnet_nro).text = if (cliente.numeroSocio != null) getString(R.string.carnet_tv_nro_socio, cliente.numeroSocio.toString()) else ""
        
        val ultimoCobro = cobroDAO.obtenerUltimoCobro(cliente.idCliente)
        findViewById<TextView>(R.id.tv_carnet_vencimiento).text = getString(R.string.carnet_tv_vencimiento, ultimoCobro?.fechaVencimiento ?: "N/A")
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
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                outputStream = uri?.let { contentResolver.openOutputStream(it) }
            } else {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(path, fileName)
                outputStream = java.io.FileOutputStream(file)
            }

            outputStream?.use {
                document.writeTo(it)
                Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
            }
            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al generar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}