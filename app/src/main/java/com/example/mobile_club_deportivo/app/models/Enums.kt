package com.example.mobile_club_deportivo.app.models

/**
 * Define si el cliente paga cuota social o por actividad individual.
 */
enum class TipoCliente {
    SOCIO,
    NO_SOCIO
}

/**
 * Define si el cliente tiene deudas pendientes o está al día.
 */
enum class EstadoCliente {
    ACTIVO,
    INACTIVO
}

/**
 * Define el estado de un registro de pago específico.
 */
enum class EstadoCobro {
    PENDIENTE,
    PAGADO,
    VENCIDO
}