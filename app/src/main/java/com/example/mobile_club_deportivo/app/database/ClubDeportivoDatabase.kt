package com.example.mobile_club_deportivo.app.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mobile_club_deportivo.app.utils.SecurityUtils

/**
 * Clase encargada de la creación y gestión de la base de datos SQLite.
 * Implementa el patrón Singleton para asegurar una única instancia en toda la app.
 */
class ClubDeportivoDatabase private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ClubDeportivo.db"
        private const val DATABASE_VERSION = 1
        
        @Volatile
        private var instance: ClubDeportivoDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos.
         */
        fun getInstance(context: Context): ClubDeportivoDatabase {
            return instance ?: synchronized(this) {
                instance ?: ClubDeportivoDatabase(context.applicationContext).also { instance = it }
            }
        }

        // Sentencias DDL para la creación de tablas
        private const val CREATE_TABLE_USUARIO = """
            CREATE TABLE USUARIO (
                id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_usuario TEXT NOT NULL,
                contrasena_hash TEXT NOT NULL,
                fecha_creacion TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
            )
        """

        private const val CREATE_TABLE_CLIENTE = """
            CREATE TABLE CLIENTE (
                id_cliente INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                apellido TEXT NOT NULL,
                dni TEXT NOT NULL UNIQUE,
                telefono TEXT NOT NULL,
                email TEXT NOT NULL,
                fecha_nacimiento TEXT,
                apto_fisico INTEGER NOT NULL DEFAULT 0,
                tipo TEXT NOT NULL,
                numero_socio INTEGER UNIQUE,
                estado TEXT NOT NULL DEFAULT 'ACTIVO',
                fecha_registro TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
            )
        """

        private const val CREATE_TABLE_ACTIVIDAD = """
            CREATE TABLE ACTIVIDAD (
                id_actividad INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE,
                descripcion TEXT,
                precio_mensual REAL NOT NULL,
                activa INTEGER NOT NULL DEFAULT 1
            )
        """

        private const val CREATE_TABLE_COBRO = """
            CREATE TABLE COBRO (
                id_cobro INTEGER PRIMARY KEY AUTOINCREMENT,
                id_cliente INTEGER NOT NULL,
                id_actividad INTEGER,
                monto REAL NOT NULL,
                fecha_pago TEXT NOT NULL,
                fecha_vencimiento TEXT NOT NULL,
                medio_pago TEXT NOT NULL,
                total_cuotas INTEGER NOT NULL,
                numero_cuota INTEGER NOT NULL,
                estado TEXT NOT NULL DEFAULT 'PAGADO',
                descripcion TEXT,
                fecha_registro TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY(id_cliente) REFERENCES CLIENTE(id_cliente) ON DELETE CASCADE,
                FOREIGN KEY(id_actividad) REFERENCES ACTIVIDAD(id_actividad) ON DELETE SET NULL
            )
        """

        private const val CREATE_TABLE_CARNET = """
            CREATE TABLE CARNET (
                id_carnet INTEGER PRIMARY KEY AUTOINCREMENT,
                id_cliente INTEGER NOT NULL UNIQUE,
                fecha_emision TEXT NOT NULL,
                fecha_vencimiento TEXT NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1,
                fecha_registro TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY(id_cliente) REFERENCES CLIENTE(id_cliente) ON DELETE CASCADE
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Ejecutar creación de tablas
        db.execSQL(CREATE_TABLE_USUARIO)
        db.execSQL(CREATE_TABLE_CLIENTE)
        db.execSQL(CREATE_TABLE_ACTIVIDAD)
        db.execSQL(CREATE_TABLE_COBRO)
        db.execSQL(CREATE_TABLE_CARNET)

        // Crear índices para mejorar el rendimiento de búsquedas frecuentes
        db.execSQL("CREATE INDEX idx_cliente_dni ON CLIENTE(dni)")
        db.execSQL("CREATE INDEX idx_cobro_cliente ON COBRO(id_cliente)")

        // Inserción del usuario administrador inicial (Seed)
        insertarAdminInicial(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Por ahora eliminamos y recreamos en caso de cambio de versión (desarrollo inicial)
        db.execSQL("DROP TABLE IF EXISTS CARNET")
        db.execSQL("DROP TABLE IF EXISTS COBRO")
        db.execSQL("DROP TABLE IF EXISTS ACTIVIDAD")
        db.execSQL("DROP TABLE IF EXISTS CLIENTE")
        db.execSQL("DROP TABLE IF EXISTS USUARIO")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Habilitar soporte para llaves foráneas en SQLite
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys = ON;")
        }
    }

    private fun insertarAdminInicial(db: SQLiteDatabase) {
        val adminUser = "admin"
        val adminPass = "admin"
        val passwordHash = SecurityUtils.sha256(adminPass)

        val sql = "INSERT INTO USUARIO (nombre_usuario, contrasena_hash) VALUES (?, ?)"
        db.execSQL(sql, arrayOf(adminUser, passwordHash))
    }
}