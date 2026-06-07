package com.example.mobile_club_deportivo.app.dao

import android.content.ContentValues
import android.database.Cursor
import com.example.mobile_club_deportivo.app.database.ClubDeportivoDatabase
import java.security.MessageDigest

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
        val passwordHash = sha256(passwordPlain)

        val selection = "nombre_usuario = ? AND contrasena_hash = ?"
        val selectionArgs = arrayOf(username, passwordHash)

        val cursor: Cursor = db.query(
            "USUARIO",
            arrayOf("id_usuario"), // Solo necesitamos saber si existe
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    /**
     * Función utilitaria para generar hash SHA-256 (debe coincidir con la de la DB).
     */
    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}