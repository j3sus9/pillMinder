# 💊 PillMinder - Gestión de Medicamentos

**Documentación del Proyecto**
**Grupo:** GR1-A

## 👥 Integrantes
* **Jesús Repiso Rio**
* **Máximo Prados Meléndez**
* **Pablo Galvez Castillo**

---

## 📄 Introducción

### Tema y Finalidad
**PillMinder** es una aplicación nativa para dispositivos Android diseñada con el objetivo principal de facilitar y mejorar la gestión de la toma de medicamentos.

Su finalidad es servir como un recordatorio fiable y un asistente personal para cualquier usuario que siga un tratamiento médico, asegurando que las dosis se administren a las horas correctas y en la cantidad adecuada.

### ¿Qué problema soluciona?
La adherencia a un tratamiento médico es fundamental para su eficacia. En la vida cotidiana, es común olvidar tomar una pastilla, especialmente con múltiples dosis o medicamentos. PillMinder reduce la carga cognitiva del usuario para:

* **Mejorar la adherencia al tratamiento:** Minimizando el riesgo de olvidos mediante notificaciones puntuales.
* **Simplificar la gestión:** Centralizando nombre, dosis, horarios y stock en un solo lugar.
* **Aportar tranquilidad:** Ofreciendo un sistema de recordatorio fiable para pacientes y cuidadores.

---

## 🚀 Funcionalidades Principales

La aplicación cuenta con un conjunto de funcionalidades orientadas a una gestión completa:

### 1. Gestión de Medicamentos
* **Creación y Edición:** Permite especificar nombre, dosis (ej. 1, 500), tipo de dosis (comprimido, ml, gota, etc.) y stock disponible.
* **Programación de Múltiples Tomas:** Configuración de varias alarmas para un mismo medicamento a lo largo del día.
* **Gestión de Horarios Flexible:**
    * Añadir nuevas horas fácilmente.
    * **Eliminación rápida:** Pulsación larga sobre el campo de horas para borrar una toma específica o todas.

### 2. Sistema Técnico y Notificaciones
* **Alarmas Precisas:** Utilización de `AlarmManager` de Android para programar alarmas que se activan incluso con la app cerrada o el dispositivo en reposo.
* **Persistencia en la Nube (Firebase):** Uso de **Firebase Firestore** para almacenar los datos. Esto garantiza que la información no se pierde al cambiar de dispositivo y está sincronizada con la cuenta del usuario.

### 3. Internacionalización (Multi-idioma)
La aplicación se adapta automáticamente al idioma del dispositivo (**Español** e **Inglés**).
* Textos externalizados en `strings.xml`.
* Uso de recursos `<plurals>` para gestionar singulares y plurales (ej. "1 pastilla" vs "2 pastillas").
* Gestión interna de claves no traducibles en base de datos para mantener la consistencia, traducidas visualmente mediante la clase `FormatUtils.java`.

---

## ⚙️ Requisitos de Instalación y Configuración

Para la correcta ejecución del proyecto en un entorno local, es necesario configurar el acceso a los servicios de Firebase.

### ⚠️ Archivo `google-services.json`
La conexión con la base de datos en la nube (Firestore) y el sistema de autenticación requiere credenciales específicas. Por motivos de seguridad, el archivo `google-services.json` **no se incluye en este repositorio público**.

Para compilar y ejecutar la aplicación correctamente, siga estos pasos:

1.  **Localizar el archivo:** Utilice el archivo `google-services.json` adjunto en la entrega de la práctica.
2.  **Copiar al proyecto:** Coloque dicho archivo manualmente dentro de la carpeta `app` en la raíz del proyecto.
    * Ruta: `.../PillMinder/app/google-services.json`
3.  **Sincronizar:** Abra el proyecto en Android Studio y pulse "Sync Project with Gradle Files".

> **Nota:** Si no se incluye este archivo, la aplicación fallará inmediatamente al intentar conectar con los servicios de Google.

---

## 📖 Guía de Uso

### 🔐 Registro e Inicio de Sesión
1.  Al abrir la app, verás la pantalla de autenticación.
2.  **Nuevos usuarios:** Introduce correo y contraseña (mín. 6 caracteres) y pulsa **"Registrarse"**.
3.  **Usuarios existentes:** Introduce credenciales y pulsa **"Iniciar Sesión"**.
    * *Nota:* El inicio de sesión sincroniza tus datos en la nube. La sesión se mantiene abierta automáticamente.

### ➕ Añadir un Nuevo Medicamento
1.  En la pantalla principal, pulsa el botón flotante **'+'**.
2.  Rellena el formulario: Nombre, Cantidad de dosis, Tipo de dosis y Stock total.
3.  Pulsa sobre **“Hora de la toma”** para abrir el selector de reloj.
4.  Elige hora/minuto y acepta. Repite para añadir más tomas.
5.  Pulsa **“GUARDAR MEDICAMENTO”**. Las alarmas se programan automáticamente.

### ✏️ Editar un Medicamento
1.  Pulsa sobre cualquier medicamento en la lista principal.
2.  Modifica cualquier campo (dosis, stock, nombre).
3.  **Gestión de horas:**
    * *Clic corto:* Añadir nueva hora.
    * *Clic largo (Mantener pulsado):* Eliminar una hora concreta o limpiar todas.
4.  Pulsa **“ACTUALIZAR CAMBIOS”** para reprogramar las alarmas.

### 🔔 Notificaciones
No requieres acción extra. La app funciona en segundo plano y enviará una notificación con el nombre del medicamento a la hora exacta programada.
