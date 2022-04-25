package com.dot.customizations.picker.applock

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricVerification(activity: FragmentActivity) {

    private val executor = ContextCompat.getMainExecutor(activity)
    private val biometricManager = BiometricManager.from(activity)
    private val biometricPrompt: BiometricPrompt
    private var promptInfo: BiometricPrompt.PromptInfo

    var title: String? = null
    var subtitle: String? = null
    var callback: Callback? = null

    private fun isSupported(): Boolean =
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }

    init {
        if (!isSupported()) {
            Log.e(
                activity::class.java.simpleName,
                "Biometric Verification not supported."
            )
            callback?.onError("Biometric Verification not supported.")
        }
        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    callback?.onError(errString)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    callback?.onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback?.onFail()
                }
            })
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title.toString())
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    }

    fun authenticate() {
        biometricPrompt.authenticate(promptInfo)
    }

    fun setCallback(callback: Callback): BiometricVerification {
        this.callback = callback
        return this
    }

    fun setTitle(title: String): BiometricVerification {
        this.title = title
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        return this
    }

    fun setSubtitle(subtitle: String): BiometricVerification {
        this.subtitle = subtitle
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title.toString())
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        return this
    }

    interface Callback {
        fun onSuccess()
        fun onError(errString: CharSequence)
        fun onFail()
    }

    companion object {
        fun isSupported(context: Context): Boolean =
            when (BiometricManager.from(context)
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS -> true
                else -> false
            }
    }
}