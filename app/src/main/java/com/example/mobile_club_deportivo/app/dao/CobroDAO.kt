package com.example.mobile_club_deportivo.app.dao

import android.content.ContentValues
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.Cobro
import com.example.mobile_club_deportivo.app.models.EstadoCobro

/**
 * Operaciones de base de datos para la entidad Cobro.
 */
class CobroDAO(private val dbHelper: ClubDeportivoDatabase) {

    /**
     * Registra un nuevo cobro en la base de datos.
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
        
        if (id != -1L && cobro.estado == EstadoCobro.PAGADO) {
            actualizarEstadoCliente(cobro.idCliente)
        }
        
        return id
    }

    /**
     * Actualiza el estado del cliente basándose en sus deudas.
     */
    private fun actualizarEstadoCliente(idCliente: Int) {
        val db = dbHelper.writableDatabase
        
        val tieneDeuda = db.rawQuery(
            "SELECT COUNT(*) FROM COBRO WHERE id_cliente = ? AND estado = 'VENCIDO'",
            arrayOf(idCliente.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) > 0 else false
        }

        val values = ContentValues().apply {
            put("estado", if (tieneDeuda) "INACTIVO" else "ACTIVO")
        }
        db.update("CLIENTE", values, "id_cliente = ?", arrayOf(idCliente.toString()))
    }

    /**
     * Obtiene el último cobro de un cliente para calcular vencimientos.
     */
    fun obtenerUltimoCobro(idCliente: Int): Cobro? {
        val db = dbHelper.readableDatabase
        return db.query(
            "COBRO",
            null,
            "id_cliente = ?",
            arrayOf(idCliente.toString()),
            null,
            null,
            "fecha_vencimiento DESC",
            "1"
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                Cobro(
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
            } else {
                null
            }
        }
    }
}