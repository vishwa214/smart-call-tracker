package com.vishwanth.callmera.security

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors

object BiometricHelper {

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {

        val executor =
            Executors.newSingleThreadExecutor()

        val biometricPrompt =
            BiometricPrompt(
                activity,
                executor,
                object :
                    BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {

                        activity.runOnUiThread {
                            onSuccess()
                        }
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {

                        activity.runOnUiThread {
                            onFailure()
                        }
                    }
                }
            )

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Smart Call Tracker")
                .setSubtitle("Use fingerprint to login")
                .setNegativeButtonText("Use PIN")
                .build()

        biometricPrompt.authenticate(
            promptInfo
        )
    }
}