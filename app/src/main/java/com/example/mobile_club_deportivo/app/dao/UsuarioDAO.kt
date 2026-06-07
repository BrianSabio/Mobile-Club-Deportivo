package com.example.mobile_club_deportivo.app.dao

import android.database.Cursor
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import com.example.mobile_club_deportivo.app.utils.SecurityUtils

/**
 * Clase encargada de las operaciones de base de datos para la entidad Usuario.
 */
class UsuarioDAO(private val dbHelper: ClubDeportivoDatabase) {

    /**
     * Valida las credenciales de un usuario.
     * @param username Nombre de usuario ingresado.
     * @param passwordPlain Contraseña en texto plano ingresada.
     * @return True si las credenciales son válidas, false en caso contrario.
     */
    fun validarUsuario(username: String, passwordPlain: String): Boolean {
        val db = dbHelper.readableDatabase
        val passwordHash = SecurityUtils.sha256(passwordPlain)

        val selection = "nombre_usuario = ? AND contrasena_hash = ?"
        val selectionArgs = arrayOf(username, passwordHash)

        return db.query(
            "USUARIO",
            arrayOf("id_usuario"),
            selection,
            selectionArgs,
            null,
            null,
            null
        ).use { cursor ->
            cursor.count > 0
        }
    }
}