package com.example.mobile_club_deportivo.app.dao

import android.content.ContentValues
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.models.Cliente
import com.example.mobile_club_deportivo.app.models.EstadoCliente
import com.example.mobile_club_deportivo.app.models.TipoCliente

/**
 * Operaciones de base de datos para la entidad Cliente.
 */
class ClienteDAO(private val dbHelper: ClubDeportivoDatabase) {

    /**
     * Registra un nuevo cliente en la base de datos.
     * @param cliente Objeto con los datos del cliente.
     * @return El ID del cliente insertado, o -1 si hubo un error.
     */
    fun registrarCliente(cliente: Cliente): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nombre", cliente.nombre)
            put("apellido", cliente.apellido)
            put("dni", cliente.dni)
            put("telefono", cliente.telefono)
            put("email", cliente.email)
            put("fecha_nacimiento", cliente.fechaNacimiento)
            put("apto_fisico", if (cliente.aptoFisico) 1 else 0)
            put("tipo", cliente.tipo.name)
            put("estado", cliente.estado.name)
            
            // Si es SOCIO, generamos el número de socio secuencial
            if (cliente.tipo == TipoCliente.SOCIO) {
                put("numero_socio", generarProximoNumeroSocio())
            } else {
                putNull("numero_socio")
            }
        }
        return db.insert("CLIENTE", null, values)
    }

    /**
     * Obtiene el próximo número de socio disponible (MAX + 1).
     */
    private fun generarProximoNumeroSocio(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT MAX(numero_socio) FROM CLIENTE", null)
        var proximo = 1 // Por defecto empezamos en 1
        if (cursor.moveToFirst()) {
            val maxActual = cursor.getInt(0)
            if (maxActual > 0) {
                proximo = maxActual + 1
            }
        }
        cursor.close()
        return proximo
    }

    /**
     * Verifica si un DNI ya existe en el sistema.
     */
    fun existeDni(dni: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "CLIENTE",
            arrayOf("id_cliente"),
            "dni = ?",
            arrayOf(dni),
            null, null, null
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    /**
     * Obtiene todos los clientes registrados, opcionalmente filtrados por nombre o DNI.
     */
    fun obtenerClientes(busqueda: String? = null): List<Cliente> {
        val lista = mutableListOf<Cliente>()
        val db = dbHelper.readableDatabase
        
        val selection = if (!busqueda.isNullOrBlank()) {
            "nombre LIKE ? OR apellido LIKE ? OR dni LIKE ?"
        } else {
            null
        }
        
        val selectionArgs = if (!busqueda.isNullOrBlank()) {
            val query = "%$busqueda%"
            arrayOf(query, query, query)
        } else {
            null
        }

        val cursor = db.query(
            "CLIENTE",
            null, // Traer todas las columnas
            selection,
            selectionArgs,
            null,
            null,
            "apellido ASC, nombre ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val cliente = Cliente(
                    idCliente = cursor.getInt(cursor.getColumnIndexOrThrow("id_cliente")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    apellido = cursor.getString(cursor.getColumnIndexOrThrow("apellido")),
                    dni = cursor.getString(cursor.getColumnIndexOrThrow("dni")),
                    telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow("fecha_nacimiento")),
                    aptoFisico = cursor.getInt(cursor.getColumnIndexOrThrow("apto_fisico")) == 1,
                    tipo = TipoCliente.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("tipo"))),
                    numeroSocio = if (cursor.isNull(cursor.getColumnIndexOrThrow("numero_socio"))) null 
                                 else cursor.getInt(cursor.getColumnIndexOrThrow("numero_socio")),
                    estado = EstadoCliente.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("estado"))),
                    fechaRegistro = cursor.getString(cursor.getColumnIndexOrThrow("fecha_registro"))
                )
                lista.add(cliente)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    /**
     * Cuenta la cantidad de deudores (clientes con estado INACTIVO).
     * Nota: En el futuro esto dependerá de la tabla COBRO, pero por ahora 
     * usamos el campo 'estado' del cliente.
     */
    fun contarDeudores(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM CLIENTE WHERE estado = 'INACTIVO'", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    /**
     * Busca un cliente por su DNI o por su número de socio.
     * Si la entrada es numérica, intenta buscar por ambos.
     */
    fun buscarPorCriterio(criterio: String): Cliente? {
        val db = dbHelper.readableDatabase
        val nroSocio = criterio.toIntOrNull()
        
        val selection = if (nroSocio != null) {
            "dni = ? OR numero_socio = ?"
        } else {
            "dni = ?"
        }
        
        val selectionArgs = if (nroSocio != null) {
            arrayOf(criterio, criterio)
        } else {
            arrayOf(criterio)
        }

        val cursor = db.query(
            "CLIENTE",
            null,
            selection,
            selectionArgs,
            null, null, null
        )

        var cliente: Cliente? = null
        if (cursor.moveToFirst()) {
            cliente = Cliente(
                idCliente = cursor.getInt(cursor.getColumnIndexOrThrow("id_cliente")),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                apellido = cursor.getString(cursor.getColumnIndexOrThrow("apellido")),
                dni = cursor.getString(cursor.getColumnIndexOrThrow("dni")),
                telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow("fecha_nacimiento")),
                aptoFisico = cursor.getInt(cursor.getColumnIndexOrThrow("apto_fisico")) == 1,
                tipo = TipoCliente.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("tipo"))),
                numeroSocio = if (cursor.isNull(cursor.getColumnIndexOrThrow("numero_socio"))) null 
                             else cursor.getInt(cursor.getColumnIndexOrThrow("numero_socio")),
                estado = EstadoCliente.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("estado"))),
                fechaRegistro = cursor.getString(cursor.getColumnIndexOrThrow("fecha_registro"))
            )
        }
        cursor.close()
        return cliente
    }
}