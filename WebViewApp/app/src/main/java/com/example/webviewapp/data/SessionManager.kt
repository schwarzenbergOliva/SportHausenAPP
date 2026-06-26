package com.example.webviewapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.webviewapp.network.SessionData

/**
 * Almacenamiento seguro de la sesión sobre EncryptedSharedPreferences (Tink +
 * Android Keystore). Guarda las 4 piezas que el WebView debe inyectar en el
 * localStorage de la web: authToken, user (JSON), userType (role) y userId.
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

    fun saveSession(session: SessionData) {
        prefs.edit()
            .putString(KEY_TOKEN, session.authToken)
            .putString(KEY_USER, session.userJson)
            .putString(KEY_USER_TYPE, session.userType)
            .putString(KEY_USER_ID, session.userId)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getUserJson(): String? = prefs.getString(KEY_USER, null)
    fun getUserType(): String? = prefs.getString(KEY_USER_TYPE, null)
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun hasToken(): Boolean = !getToken().isNullOrBlank()

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_FILENAME = "secure_session_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "user_json"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_USER_ID = "user_id"
    }
}
