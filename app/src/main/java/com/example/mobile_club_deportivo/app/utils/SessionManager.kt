package com.example.mobile_club_deportivo.app.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestiona la persistencia de la sesión del usuario administrador.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "ClubDeportivoPrefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }

    /**
     * Guarda los datos de la sesión tras un login exitoso.
     */
    fun iniciarSesion(username: String) {
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Limpia la sesión.
     */
    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    /**
     * Retorna el nombre del usuario logueado o un valor por defecto.
     */
    fun getNombreUsuario(): String {
        return prefs.getString(KEY_USERNAME, "Invitado") ?: "Invitado"
    }

    /**
     * Verifica si hay una sesión activa.
     */
    fun estaLogueado(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
}