package com.example.mobile_club_deportivo.app.models

/**
 * Representa al administrador único del sistema.
 */
data class Usuario(
    val idUsuario: Int = 0,
    val nombreUsuario: String,
    val contrasenaHash: String,
    val fechaCreacion: String // Formato ISO-8601
)

/**
 * Entidad central para socios y no socios.
 */
data class Cliente(
    val idCliente: Int = 0,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val telefono: String,
    val email: String,
    val fechaNacimiento: String? = null, // Formato ISO-8601
    val aptoFisico: Boolean = false,
    val tipo: TipoCliente,
    val numeroSocio: Int? = null, // NULL si es NO_SOCIO
    val estado: EstadoCliente = EstadoCliente.ACTIVO,
    val fechaRegistro: String = "" // Timestamp generado por la DB
)

/**
 * Referencia para actividades individuales de No Socios.
 */
data class Actividad(
    val idActividad: Int = 0,
    val nombre: String,
    val descripcion: String? = null,
    val precioMensual: Double,
    val activa: Boolean = true
)

/**
 * Registro de pago o cuota individual.
 */
data class Cobro(
    val idCobro: Int = 0,
    val idCliente: Int,
    val idActividad: Int? = null, // Solo para NO_SOCIO
    val monto: Double,
    val fechaPago: String,
    val fechaVencimiento: String,
    val medioPago: String,
    val totalCuotas: Int, // 1, 3, 6, 12
    val numeroCuota: Int,
    val estado: EstadoCobro = EstadoCobro.PAGADO,
    val descripcion: String? = null,
    val fechaRegistro: String = ""
)

/**
 * Representación del carnet asociado a un cliente.
 */
data class Carnet(
    val idCarnet: Int = 0,
    val idCliente: Int,
    val fechaEmision: String,
    val fechaVencimiento: String,
    val activo: Boolean = true,
    val fechaRegistro: String = ""
)