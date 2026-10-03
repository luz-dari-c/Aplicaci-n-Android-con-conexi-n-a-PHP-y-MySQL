# Aplicación Android conectada a PHP y MySQL mediante HTTP

Proyecto académico desarrollado con el objetivo de aprender a conectar una aplicación Android con un servicio web en PHP y una base de datos MySQL mediante peticiones HTTP y respuestas en formato JSON.

## Descripción

La aplicación permite gestionar usuarios desde una aplicación Android.

El sistema está compuesto por tres partes principales:

- Aplicación móvil desarrollada en Android Studio con Java.
- Servicio web desarrollado en PHP.
- Base de datos MySQL administrada mediante XAMPP.

La comunicación entre Android y PHP se realiza mediante peticiones HTTP. El servidor procesa las solicitudes, consulta o modifica la base de datos y devuelve las respuestas en formato JSON.

## Funcionalidades

La aplicación permite:

- Registrar nuevos usuarios.
- Iniciar sesión con correo y contraseña.
- Consultar todos los usuarios registrados.
- Modificar los datos del usuario autenticado.
- Cerrar sesión.
- Validar campos obligatorios antes de enviar información.
- Mostrar mensajes de confirmación o error.

## Arquitectura general

```text
Aplicación Android
       |
       | HTTP
       v
Servicio PHP
       |
       v
Base de datos MySQL
