package com.dot.ui.utils

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricVerification(val activity: FragmentActivity) {

    val executor = ContextCompat.getMainExecutor(activity)
    val biometricManager = BiometricManager.from(activity)
    val biometricPrompt: BiometricPrompt
    var promptInfo: BiometricPrompt.PromptInfo

    var title: String? = null
    var subtitle: String? = null
    var callback: Callback? = null

    private fun isSupported(): Boolean =
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> false
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> false
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> false
            else -> false
        }

    init {
        if (!isSupported()) {
            Log.w(activity::class.java.simpleName, "Biometric Verification not supported. Bypassing verification")
            callback?.onSuccess()
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

    fun authneticate() {
        biometricPrompt.authenticate(promptInfo)
    }

    interface Callback {
        fun onSuccess()
        fun onError(errString: CharSequence)
        fun onFail()
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
}