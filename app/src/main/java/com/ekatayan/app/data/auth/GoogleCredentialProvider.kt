package com.ekatayan.app.data.auth

import android.content.Context
import android.app.Activity
import android.content.ContextWrapper
import android.content.MutableContextWrapper
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.ekatayan.app.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.logging.Logger
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

data class GoogleIdentityCredential(
    val idToken: String,
    val nonce: String,
    val email: String?,
    val displayName: String?,
    val profilePictureUrl: String?,
)

class GoogleCredentialException(val reason: Reason) : Exception() {
    enum class Reason { CANCELED, NO_CREDENTIAL, INVALID_TOKEN, CONFIGURATION, UNKNOWN }
}

interface GoogleCredentialProvider {
    suspend fun getCredential(context: Context): GoogleIdentityCredential
}

class UnavailableGoogleCredentialProvider : GoogleCredentialProvider {
    override suspend fun getCredential(context: Context): GoogleIdentityCredential =
        throw GoogleCredentialException(GoogleCredentialException.Reason.CONFIGURATION)
}

@Singleton
class AndroidGoogleCredentialProvider @Inject constructor() : GoogleCredentialProvider {
    private val secureRandom = SecureRandom()

    override suspend fun getCredential(context: Context): GoogleIdentityCredential {
        val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        if (serverClientId.isBlank() || serverClientId == PLACEHOLDER_CLIENT_ID) {
            throw GoogleCredentialException(GoogleCredentialException.Reason.CONFIGURATION)
        }

        val nonce = generateNonce()
        val hashedNonce = sha256(nonce)
        val activityContext = context.findActivity()
            ?: throw GoogleCredentialException(GoogleCredentialException.Reason.CONFIGURATION)
        val manager = CredentialManager.create(activityContext)
        val foregroundContext = MutableContextWrapper(activityContext)
        logger.info("Google credential request started")
        val result = try {
            request(manager, foregroundContext, serverClientId, hashedNonce)
        } catch (_: NoCredentialException) {
            logger.info("Google credential request returned no credential")
            throw GoogleCredentialException(GoogleCredentialException.Reason.NO_CREDENTIAL)
        } catch (e: GetCredentialCancellationException) {
            logger.info("Google credential request canceled")
            throw GoogleCredentialException(GoogleCredentialException.Reason.CANCELED)
        } catch (e: GetCredentialException) {
            logger.warning("Google credential request failed type=${e.javaClass.simpleName}")
            throw GoogleCredentialException(GoogleCredentialException.Reason.UNKNOWN)
        } catch (e: CancellationException) {
            throw e
        }

        val credential = result.credential as? CustomCredential
        if (credential?.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            throw GoogleCredentialException(GoogleCredentialException.Reason.INVALID_TOKEN)
        }
        val google = try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (_: GoogleIdTokenParsingException) {
            throw GoogleCredentialException(GoogleCredentialException.Reason.INVALID_TOKEN)
        }
        if (google.idToken.isBlank()) throw GoogleCredentialException(GoogleCredentialException.Reason.INVALID_TOKEN)
        logger.info("Google credential received; starting Supabase exchange")
        return GoogleIdentityCredential(
            idToken = google.idToken,
            nonce = nonce,
            email = google.id,
            displayName = google.displayName,
            profilePictureUrl = google.profilePictureUri?.toString(),
        )
    }

    private suspend fun request(
        manager: CredentialManager,
        context: Context,
        serverClientId: String,
        hashedNonce: String,
    ) = manager.getCredential(
        context = context,
        request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(serverClientId)
                    .setNonce(hashedNonce)
                    .build(),
            )
            .build(),
    )

    private fun generateNonce(): String = ByteArray(32).also(secureRandom::nextBytes).let {
        Base64.encodeToString(it, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    private companion object {
        const val PLACEHOLDER_CLIENT_ID = "PASTE_YOUR_WEB_CLIENT_ID_HERE"
        val logger: Logger = Logger.getLogger("EkataYanAuth")
    }
}
