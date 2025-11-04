# FirestoreAPI-Activity-Duvier-Tavera-407575
---

## 🧩 ComposeAPIApp

Aplicación Android creada con **Jetpack Compose** que permite **gestionar usuarios** (agregar, listar y eliminar) mediante comunicación con un servidor **Node.js + Express**.

---

### 🚀 Características principales

✅ **Agregar usuarios:** Envía datos al servidor (nombre y correo electrónico).
✅ **Listar usuarios:** Muestra todos los usuarios registrados.
✅ **Eliminar usuarios:** Permite borrar un usuario mediante su ID.
✅ **Mensajes interactivos:** Notificaciones mediante *Snackbars* en cada acción.
✅ **Diseño moderno:** Interfaz desarrollada con **Material 3** y **Jetpack Compose**.

---

### 🏗️ Arquitectura general

```
┌─────────────────────────────┐
│        Android App          │
│     (Jetpack Compose)       │
│                             │
│  ┌───────────────────────┐  │
│  │  MainActivity.kt      │  │
│  │  - sendUserToAPI()    │  │
│  │  - getUsersFromAPI()  │  │
│  │  - deleteUserFromAPI()│  │
│  │  UI: UserForm()       │  │
│  └───────────────────────┘  │
│             │                │
│   HTTP      ▼                │
│  (POST/GET/DELETE)           │
└─────────────┬────────────────┘
              │
┌─────────────▼──────────────┐
│     Node.js + Express      │
│                            │
│  server.js                 │
│  - POST /users             │
│  - GET /users              │
│  - DELETE /users/:id       │
│                            │
│  Datos almacenados en      │
│  memoria (array users[])   │
└────────────────────────────┘
```

### 📱 Frontend (Android - Jetpack Compose)

Archivo principal: **MainActivity.kt**

Contiene:

* `sendUserToAPI()` → POST `/users`
* `getUsersFromAPI()` → GET `/users`
* `deleteUserFromAPI()` → DELETE `/users/:id`
* Composable `UserForm()` → UI para agregar, listar y eliminar usuarios.

---

### 🖼️ Ejemplo de uso

1. Escribe nombre y correo.
2. Toca **“Add User”** → usuario agregado.
3. Presiona **“Refresh List”** → se muestran todos los usuarios.
4. Usa el ícono 🗑️ para eliminar un usuario.


