package com.example.mobile_club_deportivo.app.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.Carnet

/**
 * Operaciones de base de datos para la entidad Carnet.
 * Garantiza que cada emisión de carnet quede registrada en la tabla CARNET.
 */
class CarnetDAO(private val dbHelper: ClubDeportivoDatabase) {

    /**
     * Registra o actualiza el carnet de un cliente.
     * Usa INSERT OR REPLACE para manejar el UNIQUE constraint de id_cliente:
     * si ya existe un carnet para ese cliente, lo reemplaza con los datos nuevos.
     * @return el ID de la fila insertada/reemplazada, o -1 si falló.
     */
    fun registrarOActualizarCarnet(carnet: Carnet): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id_cliente",        carnet.idCliente)
            put("fecha_emision",     carnet.fechaEmision)
            put("fecha_vencimiento", carnet.fechaVencimiento)
            put("activo",            if (carnet.activo) 1 else 0)
        }
        // CONFLICT_REPLACE: si ya existe un carnet para este cliente (UNIQUE id_cliente),
        // elimina el registro anterior e inserta el nuevo con los datos actualizados.
        return db.insertWithOnConflict(
            "CARNET",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /**
     * Obtiene el carnet vigente de un cliente, o null si no tiene carnet emitido.
     */
    fun obtenerCarnetPorCliente(idCliente: Int): Carnet? {
        val db = dbHelper.readableDatabase
        return db.query(
            "CARNET",
            null,
            "id_cliente = ?",
            arrayOf(idCliente.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                Carnet(
                    idCarnet         = cursor.getInt(cursor.getColumnIndexOrThrow("id_carnet")),
                    idCliente        = cursor.getInt(cursor.getColumnIndexOrThrow("id_cliente")),
                    fechaEmision     = cursor.getString(cursor.getColumnIndexOrThrow("fecha_emision")),
                    fechaVencimiento = cursor.getString(cursor.getColumnIndexOrThrow("fecha_vencimiento")),
                    activo           = cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1,
                    fechaRegistro    = cursor.getString(cursor.getColumnIndexOrThrow("fecha_registro"))
                )
            } else {
                null
            }
        }
    }
}
