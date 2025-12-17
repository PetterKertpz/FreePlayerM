package com.example.freeplayerm.core.auth

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
        // Este es el ID de tipo "Web client" que Google necesita para generar el Token
        val serverClientId = "1055393121843-3f1a2uqhenuug5gf03539p87pn51l49e.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false) // Obliga a mostrar la selección de cuenta
            .setNonce(UUID.randomUUID().toString())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                val credencialFirebase = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)
                val usuarioFirebase = auth.signInWithCredential(credencialFirebase).await().user
                    ?: return SignInResult.Error("El usuario de Firebase es nulo.")

                SignInResult.Success(
                    DatosUsuarioGoogle(
                        userId = usuarioFirebase.uid,
                        correo = usuarioFirebase.email,
                        nombreUsuario = usuarioFirebase.displayName,
                        fotoPerfilUrl = usuarioFirebase.photoUrl?.toString()
                    )
                )
            } else {
                SignInResult.Error("Tipo de credencial no reconocido.")
            }
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No hay credenciales guardadas o el usuario canceló.")
            SignInResult.Cancelled
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Error de Credential Manager: ${e.message}")
            // Este es el mensaje que verás si el SHA-1 no coincide
            SignInResult.Error("Error de seguridad de Google. Revisa el SHA-1 en la consola.")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error inesperado en login", e)
            SignInResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun cerrarSesion() {
        try {
            auth.signOut()
            credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión", e)
        }
    }
}