# Club Deportivo Móvil 🏆

**Club Deportivo Móvil** es una aplicación nativa para Android desarrollada en Kotlin, diseñada para la gestión integral de socios, cobros y carnetización en clubes deportivos. El sistema permite un control eficiente de los pagos y la situación de deuda de los clientes de forma local y segura.

## 🚀 Características Principales

*   **Autenticación Segura**: Sistema de login para administradores con contraseñas hasheadas en SHA-256.
*   **Gestión de Clientes**: Registro completo de Socios y No Socios con validaciones de datos y apto físico.
*   **Sistema de Cobros Dinámico**:
    *   Soporte para múltiples métodos de pago: Efectivo, Débito y Crédito.
    *   Cálculo automático de deudas y estados en tiempo real.
    *   Configuración centralizada de montos y cuotas.
*   **Carnetización Digital**: Generación de carnets personalizados en formato PDF profesional, incluyendo datos de contacto, logo del club y fecha de vencimiento.
*   **Búsqueda Inteligente**: Filtrado de clientes por nombre, apellido, DNI o número de socio.
*   **Persistencia Local**: Base de datos SQLite optimizada con el patrón Singleton.

## 🛠️ Stack Tecnológico

*   **Lenguaje**: Kotlin
*   **Persistencia**: SQLite (vía `SQLiteOpenHelper`)
*   **UI Layouts**: XML (Estructura basada en `LinearLayout`)
*   **Arquitectura**: Capas (DAO, Models, Utils, Database)
*   **Mínima Versión de Android**: API 24 (Android 7.0)

## 📦 Instalación

1.  Clona este repositorio:
    ```bash
    git clone https://github.com/BrianSabio/Mobile-Club-Deportivo.git
    ```
2.  Abre el proyecto en **Android Studio**.
3.  Sincroniza el proyecto con los archivos de Gradle.
4.  Ejecuta la aplicación en un emulador o dispositivo físico.

## 🔑 Credenciales de Acceso (Por Defecto)

*   **Usuario**: `admin`
*   **Contraseña**: `admin`

---
*Desarrollado por Brian Sabio.*
