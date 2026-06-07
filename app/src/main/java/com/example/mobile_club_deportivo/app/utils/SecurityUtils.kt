package com.example.mobile_club_deportivo.app.utils

import java.security.MessageDigest

/**
 * Utilitarios de seguridad centralizados.
 */
object SecurityUtils {

    /**
     * Genera un hash SHA-256 de una cadena de entrada.
     */
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}