# WebViewApp

App Android nativa en Kotlin con flujo:

```
SplashActivity ──► ¿hay token? ──► sí ─► MainActivity (WebView)
                                  └── no ─► LoginActivity (Compose) ─► MainActivity
```

## Stack

- **Kotlin 2.1** + **Jetpack Compose** (BOM 2024.12)
- **EncryptedSharedPreferences** (Tink + Keystore) para el token
- **Retrofit 2** + **kotlinx.serialization** para el placeholder de API
- **AndroidX WebKit** para configuración moderna del WebView
- `minSdk 24`, `targetSdk 35`

## Cómo correr

1. Abrir con Android Studio Iguana o superior.
2. `File → Sync Project with Gradle Files`.
3. Run en un emulador con API 24+.

Por defecto el login funciona en **modo simulado** (acepta cualquier email con `@`
y password de 6+ caracteres y devuelve un token UUID). Para activar Retrofit
contra tu backend real:

1. Edita `RetrofitClient.kt` y pon tu `BASE_URL`.
2. En `AuthRepository.kt` cambia `USE_FAKE_AUTH = false`.

## Puntos clave de implementación

### 1. Token cifrado
`data/SessionManager.kt` envuelve `EncryptedSharedPreferences` con `MasterKey`
(API actual, no la deprecated `MasterKeys`). La clave maestra vive en el Keystore.

### 2. Cookie injection en el WebView
En `ui/webview/WebViewScreen.kt`, dentro de `LaunchedEffect`:

```kotlin
CookieManager.getInstance().apply {
    setAcceptCookie(true)
    setCookie(url, "session_token=$authToken; Path=/; Secure; SameSite=Lax")
    flush()                       // ⚠ obligatorio antes de loadUrl
}
```

El orden importa: `setCookie → flush → loadUrl`. Sin `flush()` el WebView puede
hacer el primer request antes de que la cookie esté persistida.

### 3. Back button → historial del WebView
```kotlin
BackHandler(enabled = webViewRef?.canGoBack() == true) {
    webViewRef?.goBack()
}
```
Cuando `canGoBack()` devuelve false, el `BackHandler` se desactiva y el sistema
gestiona el back normalmente (sale de la activity).

### 4. Progress indicator durante la carga
`WebChromeClient.onProgressChanged()` actualiza un `mutableFloatStateOf` que
pinta un `LinearProgressIndicator` en Compose. Esto cubre el gap entre "HTML
descargado" y "CSS aplicado".

### 5. Splash sin parpadeo
`SplashActivity` usa el SplashScreen API oficial y decide en `onCreate()` —
antes de `setContent` — a qué activity ir.
