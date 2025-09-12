package com.example.freeplayerm.core.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.freeplayerm.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

// (Los data class no cambian)
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

    // Esta es la única función que debe existir. No usa 'pendingIntent'.
    suspend fun iniciarSesion(): SignInResult {
        val serverClientId = context.getString(R.string.default_web_client_id)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
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
                SignInResult.Error("Error: Credencial de tipo inesperado.")
            }
        } catch (e: GetCredentialException) {
            e.printStackTrace()
            if (e.type == "androidx.credentials.exceptions.public.UserCancellationException") {
                SignInResult.Cancelled
            } else {
                SignInResult.Error("Error de Credential Manager: ${e.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult.Error("Error inesperado: ${e.message}")
        }
    }
}