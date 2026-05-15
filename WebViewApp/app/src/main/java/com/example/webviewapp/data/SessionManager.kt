package com.example.webviewapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encapsula el almacenamiento seguro del token de sesión.
 *
 * Usa EncryptedSharedPreferences sobre Tink. La clave maestra se guarda en
 * el Android Keystore (hardware-backed cuando el dispositivo lo soporta), por
 * lo que no es extraíble incluso con root. El esquema AES256_GCM cifra valores
 * y AES256_SIV deriva claves deterministas para los nombres (para permitir
 * lookups por key sin filtrar contenido).
 *
 * Nota: androidx.security:security-crypto está en estado "deprecated/alpha" según
 * la doc oficial más reciente. Para producción nueva, evaluar Tink + DataStore
 * o jetpack DataStore con cifrado manual. Para fines didácticos y de transición
 * sigue siendo perfectamente válido.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun hasToken(): Boolean = !getToken().isNullOrBlank()

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_FILENAME = "secure_session_prefs"
        private const val KEY_TOKEN = "auth_token"
    }
}
