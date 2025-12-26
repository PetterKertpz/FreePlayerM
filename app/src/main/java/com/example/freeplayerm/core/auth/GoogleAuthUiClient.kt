package com.example.freeplayerm.core.auth

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

data class DatosUsuarioGoogle(
    val userId: String,
    val correo: String?,
    val nombreUsuario: String?,
    val fotoPerfilUrl: String?
)

sealed class SignInResult {
    data class Success(val data: DatosUsuarioGoogle) : SignInResult()
    data class Error(val message: String) : SignInResult()
    object Cancelled : SignInResult()
}

class GoogleAuthUiClient(
    private val context: Context,
    private val credentialManager: CredentialManager
) {
    private val auth = Firebase.auth
    private val TAG = "GoogleAuthUiClient"

    suspend fun iniciarSesion(): SignInResult {
        // IMPORTANTE: Este debe ser el WEB Client ID de Google Cloud Console, no el Android Client ID
        val serverClientId = "1055393121843-p1ob2qacorbvq5kk5ojmgu73n3s0mjrv.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Permite elegir cualquier cuenta
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .setNonce(UUID.randomUUID().toString())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            Log.d(TAG, "Iniciando solicitud de credenciales con serverClientId: $serverClientId")
            Log.d(TAG, "Context: ${context.packageName}")

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            Log.d(TAG, "Credencial obtenida: ${credential.type}")

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                Log.d(TAG, "Token ID obtenido, autenticando con Firebase...")

                val credencialFirebase = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)
                val usuarioFirebase = auth.signInWithCredential(credencialFirebase).await().user
                    ?: return SignInResult.Error("El usuario de Firebase es nulo.")

                Log.d(TAG, "Login exitoso: ${usuarioFirebase.email}")

                SignInResult.Success(
                    DatosUsuarioGoogle(
                        userId = usuarioFirebase.uid,
                        correo = usuarioFirebase.email,
                        nombreUsuario = usuarioFirebase.displayName,
                        fotoPerfilUrl = usuarioFirebase.photoUrl?.toString()
                    )
                )
            } else {
                Log.e(TAG, "Tipo de credencial no reconocido: ${credential.type}")
                SignInResult.Error("Tipo de credencial no reconocido.")
            }
        } catch (e: NoCredentialException) {
            Log.e(TAG, "NoCredentialException - Posibles causas:", e)
            Log.e(TAG, "1. No hay cuentas Google en el dispositivo")
            Log.e(TAG, "2. SHA-1 no configurado en Firebase/Google Cloud")
            Log.e(TAG, "3. Web Client ID incorrecto")
            SignInResult.Error(
                "No se encontraron cuentas de Google. Verifica que:\n" +
                        "• Tienes una cuenta Google en el dispositivo\n" +
                        "• La app está correctamente configurada"
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException: ${e.type} - ${e.message}", e)
            val errorMsg = when {
                e.message?.contains("16") == true ->
                    "Error de configuración. El SHA-1 puede no estar registrado."
                e.message?.contains("10") == true ->
                    "Error de desarrollador. Verifica la configuración de OAuth."
                else -> "Error al obtener credenciales: ${e.message}"
            }
            SignInResult.Error(errorMsg)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException: ${e.message}", e)
            SignInResult.Error("Error al obtener credenciales: ${e.message}")
        } catch (e: CancellationException) {
            Log.w(TAG, "Operación cancelada por el usuario")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado: ${e.message}", e)
            SignInResult.Error("Error inesperado: ${e.localizedMessage}")
        }
    }

    suspend fun cerrarSesion() {
        try {
            auth.signOut()
            credentialManager.clearCredentialState(
                androidx.credentials.ClearCredentialStateRequest()
            )
            Log.d(TAG, "Sesión cerrada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión", e)
        }
    }
}