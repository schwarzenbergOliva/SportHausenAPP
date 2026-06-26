# SportHausen App (Android)

App Android nativa (Kotlin 2.1 + Jetpack Compose) que hace **login nativo** y
luego carga la **web React** dentro de un `WebView`, ya con la sesión activa.

```
SplashActivity ─► ¿hay token? ─► sí ─► MainActivity (WebView con la web logueada)
                              └─ no ─► LoginActivity (Compose) ─► MainActivity
```

## Arquitectura del sistema (3 capas)

```
App Android ──login──► Backend Node/Express (BFF) ──► Xano (BaaS + datos)
     │                         (normaliza roles)
     └─ inyecta sesión en localStorage ─► WebView ─► Frontend React (SPA)
```

## Hand-off de sesión (lo importante)

La web guarda la sesión en `localStorage` con **4 claves**: `authToken`, `user`
(JSON), `userType` (rol) y `userId`. Tras el login nativo, la app obtiene esos
datos del backend y los **inyecta en `localStorage` antes de que arranque la SPA**
con `WebViewCompat.addDocumentStartJavaScript`. Así React monta ya autenticado y
redirige al dashboard del rol:

- `luchador`  → `/panel/luchador`
- `booker`    → `/dashboard/booker`
- `agrupacion`→ `/dashboard/agrupacion`

## Configuración (URLs)

En `gradle.properties` (o por defecto apuntan al **emulador**, `10.0.2.2`):

```properties
BACKEND_BASE_URL=http://10.0.2.2:3000      # backend Node/Express
FRONTEND_BASE_URL=http://10.0.2.2:5173     # web React (Vite dev)
```

> En producción, ambos deben servirse por **HTTPS** y se cambian estas dos URLs.
> El `network_security_config.xml` solo permite HTTP (cleartext) hacia
> `10.0.2.2`/`localhost` para desarrollo.

## Cómo probar (emulator-first)

1. Levanta el **backend Node** en tu PC (`npm run dev`, puerto 3000).
2. Levanta el **frontend React** (`npm run dev`, Vite en 5173) apuntando su
   `VITE_API_URL` al backend.
3. Abre esta app en Android Studio y ejecútala en un **emulador** (API 24+).
4. Login → la app inyecta la sesión y carga la web en el dashboard del rol.

## Estructura

```
data/        SessionManager (sesión cifrada: 4 claves)
network/     ApiService, AuthRepository, RetrofitClient, modelos del backend
ui/login/    LoginScreen + LoginViewModel (login nativo)
ui/webview/  WebViewScreen + SecureWebViewClient (hand-off + WebView)
ui/components Branding (logo + fondo)
ui/theme/    Material 3 theme
```
