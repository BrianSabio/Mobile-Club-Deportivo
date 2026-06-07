package com.example.mobile_club_deportivo.app.dao

import android.content.ContentValues
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.Cobro
import com.example.mobile_club_deportivo.app.models.EstadoCobro
import java.text.SimpleDateFormat
import java.util.*

/**
 * Operaciones de base de datos para la entidad Cobro.
 */
class CobroDAO(private val dbHelper: ClubDeportivoDatabase) {

    /**
     * Registra un nuevo cobro en la base de datos.
     * @param cobro Objeto con los datos del cobro.
     * @return El ID del cobro insertado, o -1 si hubo un error.
     */
    fun registrarCobro(cobro: Cobro): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id_cliente", cobro.idCliente)
            put("id_actividad", cobro.idActividad)
            put("monto", cobro.monto)
            put("fecha_pago", cobro.fechaPago)
            put("fecha_vencimiento", cobro.fechaVencimiento)
            put("medio_pago", cobro.medioPago)
            put("total_cuotas", cobro.totalCuotas)
            put("numero_cuota", cobro.numeroCuota)
            put("estado", cobro.estado.name)
            put("descripcion", cobro.descripcion)
        }
        
        val id = db.insert("COBRO", null, values)
        
        // Si el cobro se registró con éxito y es PAGADO, 
        // podríamos verificar si el cliente debe volver a estar ACTIVO.
        if (id != -1L && cobro.estado == EstadoCobro.PAGADO) {
            actualizarEstadoCliente(cobro.idCliente)
        }
        
        return id
    }

    /**
     * Actualiza el estado del cliente basándose en sus deudas.
     * Si no tiene cobros VENCIDOS, pasa a ACTIVO.
     */
    private fun actualizarEstadoCliente(idCliente: Int) {
        val db = dbHelper.writableDatabase
        
        // Verificar si tiene cobros vencidos
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM COBRO WHERE id_cliente = ? AND estado = 'VENCIDO'",
            arrayOf(idCliente.toString())
        )
        
        var tieneDeuda = false
        if (cursor.moveToFirst()) {
            tieneDeuda = cursor.getInt(0) > 0
        }
        cursor.close()

        val nuevoEstado = if (tieneDeuda) "INACTIVO" else "ACTIVO"
        
        val values = ContentValues().apply {
            put("estado", nuevoEstado)
        }
        db.update("CLIENTE", values, "id_cliente = ?", arrayOf(idCliente.toString()))
    }

    /**
     * Obtiene el último cobro de un cliente para calcular vencimientos.
     */
    fun obtenerUltimoCobro(idCliente: Int): Cobro? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "COBRO",
            null,
            "id_cliente = ?",
            arrayOf(idCliente.toString()),
            null,
            null,
            "fecha_vencimiento DESC",
            "1"
        )

        var cobro: Cobro? = null
        if (cursor.moveToFirst()) {
            cobro = Cobro(
                idCobro = cursor.getInt(cursor.getColumnIndexOrThrow("id_cobro")),
                idCliente = cursor.getInt(cursor.getColumnIndexOrThrow("id_cliente")),
                idActividad = if (cursor.isNull(cursor.getColumnIndexOrThrow("id_actividad"))) null 
                              else cursor.getInt(cursor.getColumnIndexOrThrow("id_actividad")),
                monto = cursor.getDouble(cursor.getColumnIndexOrThrow("monto")),
                fechaPago = cursor.getString(cursor.getColumnIndexOrThrow("fecha_pago")),
                fechaVencimiento = cursor.getString(cursor.getColumnIndexOrThrow("fecha_vencimiento")),
                medioPago = cursor.getString(cursor.getColumnIndexOrThrow("medio_pago")),
                totalCuotas = cursor.getInt(cursor.getColumnIndexOrThrow("total_cuotas")),
                numeroCuota = cursor.getInt(cursor.getColumnIndexOrThrow("numero_cuota")),
                estado = EstadoCobro.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("estado"))),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                fechaRegistro = cursor.getString(cursor.getColumnIndexOrThrow("fecha_registro"))
            )
        }
        cursor.close()
        return cobro
    }
}